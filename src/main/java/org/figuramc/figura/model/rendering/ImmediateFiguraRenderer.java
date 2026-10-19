package org.figuramc.figura.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.FiguraModelPartReader;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.PartCustomization;
import org.figuramc.figura.model.TextureCustomization;
import org.figuramc.figura.model.rendering.texture.FiguraRenderTypes;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.figuramc.figura.model.rendertasks.RenderTask;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class ImmediateFiguraRenderer extends FiguraRenderer {
   protected final PartCustomization.PartCustomizationStack customizationStack = new PartCustomization.PartCustomizationStack();
   public static final FiguraMat4 CAMERA_POS_TO_WORLD_MATRIX = FiguraMat4.of();
   private static final PartCustomization pivotOffsetter = new PartCustomization();
   protected static final ImmediateFiguraRenderer.VertexBuffer VERTEX_BUFFER = new ImmediateFiguraRenderer.VertexBuffer();
   private static final FiguraVec4 pos = FiguraVec4.of();
   private static final FiguraVec3 normal = FiguraVec3.of();
   private static final FiguraVec3 uv = FiguraVec3.of(0.0, 0.0, 1.0);

   public ImmediateFiguraRenderer(Avatar avatar) {
      super(avatar);
      this.root = FiguraModelPartReader.read(avatar, avatar.nbt.getCompoundOrEmpty("models"), this.textureSets, false);
      this.sortParts();
   }

   public void checkEmpty() {
      if (!this.customizationStack.isEmpty()) {
         throw new IllegalStateException("Customization stack not empty!");
      }
   }

   @Override
   public int render() {
      return this.commonRender(1.5);
   }

   @Override
   public int renderSpecialParts() {
      return this.commonRender(0.0);
   }

   @Override
   public void updateMatrices() {
      this.isRendering = true;
      PartCustomization customization = this.setupRootCustomization(1.5);
      this.customizationStack.push(customization);
      CAMERA_POS_TO_WORLD_MATRIX.set(FiguraRenderer.worldToCameraPosMatrix().invert());
      this.calculatePartMatrices(this.root);
      this.customizationStack.pop();
      this.checkEmpty();
      this.isRendering = false;
   }

   @Override
   public int getComplexity() {
      int prev = this.avatar.complexity.remaining;
      int[] remainingComplexity = new int[]{prev};
      if (this.root.customization.visible) {
         if (this.currentFilterScheme.parentType.isSeparate) {
            List<FiguraModelPart> parts = this.separatedParts.get(this.currentFilterScheme.parentType);
            if (parts != null) {
               for (FiguraModelPart part : parts) {
                  if (this.currentFilterScheme.parentType != ParentType.Item || part == this.itemToRender) {
                     this.getPartComplexity(part, remainingComplexity, this.currentFilterScheme.initialValue);
                  }
               }
            }
         } else {
            this.getPartComplexity(this.root, remainingComplexity, this.currentFilterScheme.initialValue);
         }
      }

      return prev - Math.max(remainingComplexity[0], 0);
   }

   protected int commonRender(double vertOffset) {
      this.isRendering = true;
      int irisConfig = !UIHelper.paperdoll && ClientAPI.hasShaderPackMod() ? Configs.IRIS_COMPATIBILITY_FIX.value : 0;
      this.doIrisEmissiveFix = irisConfig >= 2 && ClientAPI.hasShaderPack()
         || this.avatar.renderMode != EntityRenderMode.RENDER && this.avatar.renderMode != EntityRenderMode.WORLD;
      this.offsetRenderLayers = irisConfig >= 1;

      for (FiguraTextureSet set : this.textureSets) {
         set.uploadIfNeeded();
      }

      for (FiguraTexture texture : this.customTextures.values()) {
         texture.uploadIfDirty(false, false);
      }

      int config = Configs.RENDER_DEBUG_PARTS_PIVOT.value;
      if (Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) && (this.avatar.isHost || config >= 2)) {
         shouldRenderPivots = config;
      } else {
         shouldRenderPivots = 0;
      }

      if (this.allowMatrixUpdate) {
         CAMERA_POS_TO_WORLD_MATRIX.set(FiguraRenderer.worldToCameraPosMatrix().invert());
      }

      int prev = this.avatar.complexity.remaining;
      int[] remainingComplexity = new int[]{prev};
      if (this.root.customization.visible) {
         if (!this.currentFilterScheme.parentType.isSeparate) {
            PartCustomization customization = this.setupRootCustomization(vertOffset);
            this.customizationStack.push(customization);
            this.renderPart(this.root, remainingComplexity, this.currentFilterScheme.initialValue);
            this.customizationStack.pop();
         } else {
            List<FiguraModelPart> parts = this.separatedParts.get(this.currentFilterScheme.parentType);
            if (parts != null) {
               boolean renderLayer = !this.currentFilterScheme.parentType.isRenderLayer;
               if (renderLayer) {
                  PartCustomization customization = this.setupRootCustomization(vertOffset);
                  this.customizationStack.push(customization);
                  this.customizationStack.push(this.root.customization);
               }

               for (FiguraModelPart part : parts) {
                  if (this.currentFilterScheme.parentType != ParentType.Item || part == this.itemToRender) {
                     boolean saved = part.savedCustomization != null;
                     if (saved) {
                        this.customizationStack.push(part.savedCustomization);
                        part.savedCustomization = null;
                     }

                     this.renderPart(part, remainingComplexity, this.currentFilterScheme.initialValue);
                     if (saved) {
                        this.customizationStack.pop();
                     }
                  }
               }

               if (renderLayer) {
                  this.customizationStack.pop();
                  this.customizationStack.pop();
               }
            }
         }

         FiguraMod.pushProfiler("draw");
         FiguraMod.pushProfiler("primary");
         VERTEX_BUFFER.consume(true, this.bufferSource);
         FiguraMod.popPushProfiler("secondary");
         VERTEX_BUFFER.consume(false, this.bufferSource);
         FiguraMod.popProfiler(2);
         this.checkEmpty();
      }

      this.isRendering = false;
      if (this.dirty) {
         this.clean();
      }

      return prev - Math.max(remainingComplexity[0], 0);
   }

   protected PartCustomization setupRootCustomization(double vertOffset) {
      PartCustomization customization = new PartCustomization();
      customization.setPrimaryRenderType(FiguraRenderTypes.TRANSLUCENT);
      customization.setSecondaryRenderType(FiguraRenderTypes.EMISSIVE);
      double s = 0.0625;
      customization.positionMatrix.scale(s, s, s);
      customization.positionMatrix.rotateZ(180.0);
      customization.positionMatrix.translate(0.0, vertOffset, 0.0);
      customization.normalMatrix.rotateZ(180.0);
      customization.positionMatrix.multiply(this.posMat);
      customization.normalMatrix.multiply(this.normalMat);
      customization.light = this.light;
      customization.alpha = this.alpha;
      customization.overlay = this.overlay;
      customization.primaryTexture = new TextureCustomization(FiguraTextureSet.OverrideType.PRIMARY, null);
      customization.secondaryTexture = new TextureCustomization(FiguraTextureSet.OverrideType.SECONDARY, null);
      return customization;
   }

   protected boolean renderPart(FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
      FiguraMod.pushProfiler(part.name);
      PartCustomization custom = part.customization;
      FiguraMod.pushProfiler("predicate");
      Boolean thisPassedPredicate = this.currentFilterScheme.test(part.parentType, prevPredicate);
      if (thisPassedPredicate != null && custom.visible) {
         FiguraMod.popPushProfiler("copyVanillaPart");
         part.applyVanillaTransforms(this.vanillaModelData);
         part.applyExtraTransforms(this.customizationStack.peek());
         FiguraMod.popPushProfiler("checkVanillaVisible");
         if (!this.ignoreVanillaVisibility && custom.vanillaVisible != null && !custom.vanillaVisible) {
            FiguraMod.popPushProfiler("removeVanillaTransforms");
            part.resetVanillaTransforms();
            FiguraMod.popProfiler(2);
            return true;
         }

         if (part.preRender != null) {
            FiguraMod.popPushProfiler("preRenderFunction");
            this.avatar.run(part.preRender, this.avatar.render, this.tickDelta, this.avatar.renderMode.name(), part);
         }

         FiguraMod.popPushProfiler("calculatePartMatrices");
         custom.recalculate();
         FiguraMat4 positionCopy = null;
         FiguraMat3 normalCopy = null;
         boolean voidMatrices = !this.allowHiddenTransforms && !prevPredicate;
         if (voidMatrices) {
            FiguraMod.popPushProfiler("clearMatrices");
            positionCopy = custom.positionMatrix.copy();
            normalCopy = custom.normalMatrix.copy();
            custom.positionMatrix.reset();
            custom.normalMatrix.reset();
         }

         FiguraMod.popPushProfiler("pushCustomizationStack");
         this.customizationStack.push(custom);
         if (voidMatrices) {
            FiguraMod.popPushProfiler("restoreMatrices");
            custom.positionMatrix.set(positionCopy);
            custom.normalMatrix.set(normalCopy);
         }

         if (thisPassedPredicate) {
            FiguraMod.popPushProfiler("worldMatrices");
            if (this.allowMatrixUpdate) {
               FiguraMat4 mat = this.partToWorldMatrices(custom);
               part.savedPartToWorldMat.set(mat);
            }

            FiguraMod.popPushProfiler("calculateLight");
            if (custom.light != null) {
               this.updateLight = false;
               pivotOffsetter.light = custom.light;
            } else {
               Level l;
               if (this.updateLight && (l = Minecraft.getInstance().level) != null) {
                  FiguraVec3 pos = part.savedPartToWorldMat.apply(0.0, 0.0, 0.0);
                  int block = l.getBrightness(LightLayer.BLOCK, pos.asBlockPos());
                  int sky = l.getBrightness(LightLayer.SKY, pos.asBlockPos());
                  this.customizationStack.peek().light = LightCoordsUtil.pack(block, sky);
               }
            }

            if (custom.alpha != null) {
               pivotOffsetter.alpha = custom.alpha;
            }

            if (custom.overlay != null) {
               pivotOffsetter.overlay = custom.overlay;
            }
         }

         if (part.midRender != null) {
            FiguraMod.popPushProfiler("midRenderFunction");
            this.avatar.run(part.midRender, this.avatar.render, this.tickDelta, this.avatar.renderMode.name(), part);
         }

         FiguraMod.popPushProfiler("pushVertices");
         boolean breakRender = thisPassedPredicate && !part.pushVerticesImmediate(this, remainingComplexity);
         FiguraMod.popPushProfiler("extras");
         if (!breakRender && thisPassedPredicate) {
            boolean renderPivot = shouldRenderPivots > 0;
            boolean renderTasks = !part.renderTasks.isEmpty();
            boolean renderPivotParts = part.parentType.isPivot && this.allowPivotParts;
            if (renderPivot || renderTasks || renderPivotParts) {
               FiguraMod.pushProfiler("fixMatricesPivot");
               FiguraVec3 pivot = custom.getPivot().copy().add(custom.getOffsetPivot());
               pivotOffsetter.setPos(pivot);
               pivotOffsetter.recalculate();
               this.customizationStack.push(pivotOffsetter);
               PartCustomization peek = this.customizationStack.peek();
               if (renderPivot) {
                  FiguraMod.popPushProfiler("renderPivotCube");
                  this.renderPivot(part, peek);
               }

               if (renderTasks) {
                  FiguraMod.popPushProfiler("renderTasks");
                  int light = peek.light;
                  int overlay = peek.overlay;
                  this.interceptRendersIntoFigura = false;

                  for (RenderTask task : part.renderTasks.values()) {
                     if (task.shouldRender()) {
                        int neededComplexity = task.getComplexity();
                        if (neededComplexity > remainingComplexity[0]) {
                           break;
                        }

                        FiguraMod.pushProfiler(task.getName());
                        task.render(this.customizationStack, this.bufferSource, light, overlay);
                        remainingComplexity[0] -= neededComplexity;
                        FiguraMod.popProfiler();
                     }
                  }

                  this.interceptRendersIntoFigura = true;
               }

               if (renderPivotParts && part.parentType.isPivot) {
                  FiguraMod.popPushProfiler("savePivotParts");
                  this.savePivotTransform(part.parentType, peek);
               }

               this.customizationStack.pop();
               FiguraMod.popProfiler();
            }
         }

         FiguraMod.popPushProfiler("children");

         for (FiguraModelPart child : List.copyOf(part.children)) {
            if (!this.renderPart(child, remainingComplexity, thisPassedPredicate)) {
               breakRender = true;
               break;
            }
         }

         FiguraMod.popPushProfiler("removeVanillaTransforms");
         part.resetVanillaTransforms();
         if (part.postRender != null) {
            FiguraMod.popPushProfiler("postRenderFunction");
            this.avatar.run(part.postRender, this.avatar.render, this.tickDelta, this.avatar.renderMode.name(), part);
         }

         this.customizationStack.pop();
         FiguraMod.popProfiler(2);
         return !breakRender;
      } else {
         if (part.parentType.isRenderLayer) {
            part.savedCustomization = this.customizationStack.peek();
         }

         FiguraMod.popProfiler(2);
         return true;
      }
   }

   protected boolean getPartComplexity(FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
      PartCustomization custom = part.customization;
      Boolean thisPassedPredicate = this.currentFilterScheme.test(part.parentType, prevPredicate);
      if (thisPassedPredicate == null || !custom.visible) {
         return true;
      }

      if (!this.ignoreVanillaVisibility && custom.vanillaVisible != null && !custom.vanillaVisible) {
         return true;
      }

      FiguraMod.popPushProfiler("pushVertices");
      boolean breakRender = thisPassedPredicate && !part.calculateComplexity(remainingComplexity);
      if (!breakRender && thisPassedPredicate) {
         boolean renderTasks = !part.renderTasks.isEmpty();
         if (renderTasks) {
            for (RenderTask task : part.renderTasks.values()) {
               if (task.shouldRender()) {
                  int neededComplexity = task.getComplexity();
                  if (neededComplexity > remainingComplexity[0]) {
                     break;
                  }

                  FiguraMod.pushProfiler(task.getName());
                  remainingComplexity[0] -= neededComplexity;
                  FiguraMod.popProfiler();
               }
            }
         }
      }

      for (FiguraModelPart child : List.copyOf(part.children)) {
         if (!this.getPartComplexity(child, remainingComplexity, thisPassedPredicate)) {
            breakRender = true;
            break;
         }
      }

      return !breakRender;
   }

   protected void renderPivot(FiguraModelPart part, PartCustomization customization) {
      boolean group = part.customization.partType == PartCustomization.PartType.GROUP;
      FiguraVec3 color = group ? ColorUtils.Colors.FIGURA_BLUE.vec : ColorUtils.Colors.AWESOME_BLUE.vec;
      double boxSize = group ? 0.0625 : 0.03125;
      boxSize /= Math.max(Math.cbrt(part.savedPartToWorldMat.det()), 0.02);
      final double finalBoxSize = boxSize;
      PoseStack stack = customization.copyIntoGlobalPoseStack();
      this.bufferSource.submitCustomGeometry(
         stack,
         RenderTypes.LINES,
         (pose, vertexConsumer) -> renderLineBox(
            pose,
            vertexConsumer,
            -finalBoxSize,
            -finalBoxSize,
            -finalBoxSize,
            finalBoxSize,
            finalBoxSize,
            finalBoxSize,
            (float)color.x,
            (float)color.y,
            (float)color.z,
            1.0F
         )
      );
   }

   public static void renderLineBox(
      Pose pose, VertexConsumer vertices, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a
   ) {
      float f = (float)x1;
      float h = (float)y1;
      float i = (float)z1;
      float j = (float)x2;
      float k = (float)y2;
      float l = (float)z2;
      vertices.addVertex(pose, f, h, i).setColor(r, g, b, a).setNormal(pose, 1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, j, h, i).setColor(r, g, b, a).setNormal(pose, 1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, f, h, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
      vertices.addVertex(pose, f, k, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
      vertices.addVertex(pose, f, h, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertices.addVertex(pose, f, h, l).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertices.addVertex(pose, j, h, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
      vertices.addVertex(pose, j, k, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
      vertices.addVertex(pose, j, k, i).setColor(r, g, b, a).setNormal(pose, -1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, f, k, i).setColor(r, g, b, a).setNormal(pose, -1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, f, k, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertices.addVertex(pose, f, k, l).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertices.addVertex(pose, f, k, l).setColor(r, g, b, a).setNormal(pose, 0.0F, -1.0F, 0.0F);
      vertices.addVertex(pose, f, h, l).setColor(r, g, b, a).setNormal(pose, 0.0F, -1.0F, 0.0F);
      vertices.addVertex(pose, f, h, l).setColor(r, g, b, a).setNormal(pose, 1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, j, h, l).setColor(r, g, b, a).setNormal(pose, 1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, j, h, l).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, -1.0F);
      vertices.addVertex(pose, j, h, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, -1.0F);
      vertices.addVertex(pose, f, k, l).setColor(r, g, b, a).setNormal(pose, 1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, j, k, l).setColor(r, g, b, a).setNormal(pose, 1.0F, 0.0F, 0.0F);
      vertices.addVertex(pose, j, h, l).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
      vertices.addVertex(pose, j, k, l).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
      vertices.addVertex(pose, j, k, i).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertices.addVertex(pose, j, k, l).setColor(r, g, b, a).setNormal(pose, 0.0F, 0.0F, 1.0F);
   }

   protected void savePivotTransform(ParentType parentType, PartCustomization customization) {
      FiguraMat4 currentPosMat = customization.getPositionMatrix();
      FiguraMat3 currentNormalMat = customization.getNormalMatrix();
      ConcurrentLinkedQueue<Pair<FiguraMat4, FiguraMat3>> queue = this.pivotCustomizations.computeIfAbsent(parentType, p -> new ConcurrentLinkedQueue<>());
      queue.add(new Pair(currentPosMat, currentNormalMat));
   }

   protected FiguraMat4 partToWorldMatrices(PartCustomization cust) {
      FiguraMat4 customizePeek = this.customizationStack.peek().positionMatrix.copy();
      customizePeek.multiply(CAMERA_POS_TO_WORLD_MATRIX);
      FiguraVec3 piv = cust.getPivot();
      FiguraMat4 translation = FiguraMat4.of();
      translation.translate(piv);
      customizePeek.rightMultiply(translation);
      return customizePeek;
   }

   protected void calculatePartMatrices(FiguraModelPart part) {
      FiguraMod.pushProfiler(part.name);
      PartCustomization custom = part.customization;
      FiguraMod.pushProfiler("predicate");
      Boolean thisPassedPredicate = this.currentFilterScheme.test(part.parentType, true);
      if (thisPassedPredicate == null) {
         FiguraMod.popProfiler(2);
      } else {
         FiguraMod.popPushProfiler("copyVanillaPart");
         part.applyVanillaTransforms(this.vanillaModelData);
         part.applyExtraTransforms(this.customizationStack.peek());
         FiguraMod.popPushProfiler("calculatePartMatrices");
         custom.recalculate();
         FiguraMod.popPushProfiler("applyOnStack");
         this.customizationStack.push(custom);
         if (thisPassedPredicate) {
            FiguraMod.popPushProfiler("worldMatrices");
            FiguraMat4 mat = this.partToWorldMatrices(custom);
            part.savedPartToWorldMat.set(mat);
         }

         FiguraMod.popPushProfiler("children");

         for (FiguraModelPart child : part.children) {
            this.calculatePartMatrices(child);
         }

         part.resetVanillaTransforms();
         this.customizationStack.pop();
         FiguraMod.popProfiler(2);
      }
   }

   public void pushFaces(int faceCount, int[] remainingComplexity, FiguraTextureSet textureSet, List<Vertex> vertices) {
      if (faceCount != 0 && !vertices.isEmpty()) {
         PartCustomization customization = this.customizationStack.peek();
         ImmediateFiguraRenderer.VertexData primary = this.getTexture(customization, textureSet, true);
         ImmediateFiguraRenderer.VertexData secondary = this.getTexture(customization, textureSet, false);
         if (primary.renderType == null && secondary.renderType == null) {
            remainingComplexity[0] += faceCount;
         } else {
            if (primary.renderType != null) {
               this.pushToBuffer(faceCount, primary, customization, textureSet, vertices);
            }

            if (secondary.renderType != null) {
               this.pushToBuffer(faceCount, secondary, customization, textureSet, vertices);
            }
         }
      }
   }

   private ImmediateFiguraRenderer.VertexData getTexture(PartCustomization customization, FiguraTextureSet textureSet, boolean primary) {
      FiguraRenderTypes types = primary ? customization.getPrimaryRenderType() : customization.getSecondaryRenderType();
      TextureCustomization texture = primary ? customization.primaryTexture : customization.secondaryTexture;
      ImmediateFiguraRenderer.VertexData ret = new ImmediateFiguraRenderer.VertexData();
      if (types == FiguraRenderTypes.NONE) {
         return ret;
      }

      Identifier id = textureSet.getOverrideTexture(this.avatar.owner, texture);
      ret.color = primary ? customization.color : customization.color2;
      ret.primary = primary;
      if (id != null) {
         if (this.translucent) {
            ret.renderType = RenderTypes.entityTranslucentCullItemTarget(id);
            return ret;
         }

         if (this.glowing) {
            ret.renderType = RenderTypes.outline(id);
            return ret;
         }
      }

      if (types == null) {
         return ret;
      }

      if (this.offsetRenderLayers && !primary && types.isOffset()) {
         ret.vertexOffset = -5.0E-4F;
      }

      if (this.doIrisEmissiveFix && types == FiguraRenderTypes.EMISSIVE) {
         ret.fullBright = true;
         ret.renderType = FiguraRenderTypes.TRANSLUCENT_CULL.get(id);
      } else {
         ret.renderType = types.get(id);
      }

      return ret;
   }

   private void pushToBuffer(
      int faceCount, ImmediateFiguraRenderer.VertexData vertexData, PartCustomization customization, FiguraTextureSet textureSet, List<Vertex> vertices
   ) {
      int vertCount = faceCount * 4;
      FiguraVec3 uvFixer = FiguraVec3.of();
      uvFixer.set(textureSet.getWidth(), textureSet.getHeight(), 1.0);
      int overlay = customization.overlay;
      int light = vertexData.fullBright ? 15728880 : customization.light;
      VERTEX_BUFFER.getBufferFor(
         vertexData.renderType,
         vertexData.primary,
         vertexConsumer -> {
            for (int i = 0; i < vertCount; i++) {
               Vertex vertex = vertices.get(i);
               pos.set(vertex.x, vertex.y, vertex.z, 1.0);
               pos.transform(customization.positionMatrix);
               pos.add(pos.normalized().scale(vertexData.vertexOffset));
               normal.set(vertex.nx, vertex.ny, vertex.nz);
               normal.transform(customization.normalMatrix);
               uv.set(vertex.u, vertex.v, 1.0);
               uv.divide(uvFixer);
               uv.transform(customization.uvMatrix);
               vertexConsumer.addVertex((float)pos.x, (float)pos.y, (float)pos.z)
                  .setColor((float)vertexData.color.x, (float)vertexData.color.y, (float)vertexData.color.z, customization.alpha)
                  .setUv((float)uv.x, (float)uv.y)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal((float)normal.x, (float)normal.y, (float)normal.z);
            }
         }
      );
   }

   private static class VertexBuffer {
      private final HashMap<RenderType, List<Consumer<VertexConsumer>>> primaryBuffers = new LinkedHashMap<>();
      private final HashMap<RenderType, List<Consumer<VertexConsumer>>> secondaryBuffers = new LinkedHashMap<>();

      public void getBufferFor(RenderType renderType, boolean primary, Consumer<VertexConsumer> consumer) {
         HashMap<RenderType, List<Consumer<VertexConsumer>>> buffer = primary ? this.primaryBuffers : this.secondaryBuffers;
         List<Consumer<VertexConsumer>> list = buffer.computeIfAbsent(renderType, renderType1 -> new ArrayList<>());
         list.add(consumer);
      }

      public void consume(boolean primary, SubmitNodeCollector bufferSource) {
         HashMap<RenderType, List<Consumer<VertexConsumer>>> map = primary ? this.primaryBuffers : this.secondaryBuffers;

         for (Entry<RenderType, List<Consumer<VertexConsumer>>> entry : map.entrySet()) {
            PoseStack poseStack = new PoseStack();
            bufferSource.submitCustomGeometry(poseStack, entry.getKey(), (pose, vertexConsumer) -> {
               for (Consumer<VertexConsumer> consumer : entry.getValue()) {
                  consumer.accept(vertexConsumer);
               }
            });
         }

         map.clear();
      }
   }

   private static class VertexData {
      public RenderType renderType;
      public boolean fullBright;
      public float vertexOffset;
      public FiguraVec3 color;
      public boolean primary;
   }
}
