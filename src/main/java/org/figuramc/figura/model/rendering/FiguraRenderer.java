package org.figuramc.figura.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.VanillaModelData;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.joml.Matrix3f;
import org.joml.Matrix4d;
import org.joml.Quaternionf;

public abstract class FiguraRenderer {
   protected final Avatar avatar;
   public FiguraModelPart root;
   protected final Map<ParentType, List<FiguraModelPart>> separatedParts = new ConcurrentHashMap<>();
   protected boolean isRendering;
   protected boolean dirty;
   public Entity entity;
   public float yaw;
   public float tickDelta;
   public int light;
   public int overlay;
   public float alpha;
   public boolean translucent;
   public boolean glowing;
   public FiguraMat4 posMat = FiguraMat4.of();
   public FiguraMat3 normalMat = FiguraMat3.of();

   public SubmitNodeCollector bufferSource;
   public VanillaModelData vanillaModelData = new VanillaModelData();
   public PartFilterScheme currentFilterScheme;
   public final HashMap<ParentType, ConcurrentLinkedQueue<Pair<FiguraMat4, FiguraMat3>>> pivotCustomizations = new HashMap<>(ParentType.values().length);
   protected final List<FiguraTextureSet> textureSets = new ArrayList<>();
   public final HashMap<String, FiguraTexture> textures = new HashMap<>();
   public final HashMap<String, FiguraTexture> customTextures = new HashMap<>();
   protected static int shouldRenderPivots;
   public boolean allowMatrixUpdate = false;
   public boolean allowHiddenTransforms = true;
   public boolean interceptRendersIntoFigura = true;
   public boolean allowPivotParts = true;
   public boolean updateLight = false;
   public boolean doIrisEmissiveFix = false;
   public boolean offsetRenderLayers = false;
   public boolean ignoreVanillaVisibility = false;
   public FiguraModelPart itemToRender;

   public FiguraRenderer(Avatar avatar) {
      this.avatar = avatar;
      CompoundTag nbt = avatar.nbt.getCompoundOrEmpty("textures");
      CompoundTag src = nbt.getCompoundOrEmpty("src");

      for (String key : src.keySet()) {
         byte[] bytes = (byte[])src.getByteArray(key).get();
         if (bytes.length > 0) {
            this.textures.put(key, new FiguraTexture(avatar, key, bytes));
         } else {
            ListTag size = src.getListOrEmpty(key);
            this.textures.put(key, new FiguraTexture(avatar, key, size.getIntOr(0, 0), size.getIntOr(1, 0)));
         }
      }

      ListTag texturesList = nbt.getListOrEmpty("data");

      for (Tag t : texturesList) {
         CompoundTag tag = (CompoundTag)t;
         this.textureSets
            .add(
               new FiguraTextureSet(
                  this.getTextureName(tag),
                  this.textures.get(tag.getStringOr("d", "")),
                  this.textures.get(tag.getStringOr("e", "")),
                  this.textures.get(tag.getStringOr("s", "")),
                  this.textures.get(tag.getStringOr("n", ""))
               )
            );
      }

      avatar.hasTexture = !texturesList.isEmpty();
   }

   private String getTextureName(CompoundTag tag) {
      String s = tag.getStringOr("d", "");
      if (!s.isEmpty()) {
         return s;
      }

      s = tag.getStringOr("e", "");
      if (!s.isEmpty()) {
         return s.substring(0, s.length() - 2);
      }

      s = tag.getStringOr("s", "");
      if (!s.isEmpty()) {
         return s.substring(0, s.length() - 2);
      }

      s = tag.getStringOr("n", "");
      return !s.isEmpty() ? s.substring(0, s.length() - 2) : "";
   }

   public FiguraTexture getTexture(String name) {
      FiguraTexture texture = this.customTextures.get(name);
      if (texture != null) {
         return texture;
      }

      for (Entry<String, FiguraTexture> entry : this.textures.entrySet()) {
         if (entry.getKey().equals(name)) {
            return entry.getValue();
         }
      }

      return null;
   }

   public abstract int render();

   public abstract int getComplexity();

   public abstract int renderSpecialParts();

   public abstract void updateMatrices();

   public boolean hasMainModel() {
      return this.hasMainModel(this.root);
   }

   public boolean hasModelPart(ParentType parentType) {
      return this.hasModelPart(this.root, parentType);
   }

   private boolean hasMainModel(FiguraModelPart part) {
      if (!part.customization.visible || part.parentType.isSeparate) {
         return false;
      }

      if (part.parentType == ParentType.None
         && (part.facesByTexture.stream().anyMatch(faces -> faces > 0) || !part.renderTasks.isEmpty())) {
         return true;
      }

      for (FiguraModelPart child : part.children) {
         if (this.hasMainModel(child)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasModelPart(FiguraModelPart part, ParentType parentType) {
      if (part.getVisible() && part.parentType == parentType) {
         return true;
      }

      for (FiguraModelPart child : part.children) {
         if (this.hasModelPart(child, parentType)) {
            return true;
         }
      }

      return false;
   }

   protected void clean() {
      for (FiguraTextureSet set : this.textureSets) {
         set.clean();
      }

      for (FiguraTexture texture : this.customTextures.values()) {
         texture.close();
      }
   }

   public void invalidate() {
      this.dirty = true;
      if (!this.isRendering) {
         this.clean();
      }
   }

   public void sortParts() {
      this.separatedParts.clear();
      this._sortParts(this.root);
   }

   private void _sortParts(FiguraModelPart part) {
      if (part.parentType.isSeparate) {
         List<FiguraModelPart> list = this.separatedParts.computeIfAbsent(part.parentType, parentType -> new ArrayList<>());
         list.add(part);
      }

      for (FiguraModelPart child : part.children) {
         this._sortParts(child);
      }
   }

   public static FiguraMat4 entityToWorldMatrix(Entity e, float delta) {
      double yaw = e instanceof LivingEntity le
         ? Mth.lerp(delta, le.yBodyRotO, le.yBodyRot)
         : e.getViewYRot(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
      FiguraMat4 result = FiguraMat4.of();
      result.rotateX(180.0 - yaw);
      result.translate(e.getPosition(delta));
      return result;
   }

   public static double getYawOffsetRot(Entity e, float delta) {
      double yaw = e instanceof LivingEntity le
         ? Mth.lerp(delta, le.yBodyRotO, le.yBodyRot)
         : e.getViewYRot(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
      return 180.0 - yaw;
   }

   public static FiguraMat4 worldToViewMatrix() {
      Minecraft client = Minecraft.getInstance();
      Camera camera = client.gameRenderer.mainCamera();
      Quaternionf rot = new Quaternionf(camera.rotation());
      rot.x *= -1.0F;
      rot.z *= -1.0F;
      Matrix3f cameraMat3f = new Matrix3f().rotate(rot);
      FiguraMat4 result = FiguraMat4.of();
      FiguraMat3 cameraMat = FiguraMat3.of().set(cameraMat3f);
      result.multiply(cameraMat.augmented());
      result.scale(-1.0, 1.0, -1.0);
      return result;
   }

   public static FiguraMat4 worldToCameraPosMatrix() {
      Minecraft client = Minecraft.getInstance();
      Camera camera = client.gameRenderer.mainCamera();
      FiguraMat4 result = FiguraMat4.of();
      Vec3 cameraPos = camera.position().scale(-1.0);
      result.translate(cameraPos.x, cameraPos.y, cameraPos.z);
      return result;
   }

    public void setupRenderer(
            PartFilterScheme currentFilterScheme,
            SubmitNodeCollector bufferSource,          
            PoseStack matrices,
            float tickDelta,
            int light,
            float alpha,
            int overlay,
            boolean translucent,
            boolean glowing
    ) {
        this.setupRenderer(currentFilterScheme, bufferSource, tickDelta, light, alpha, overlay, translucent, glowing);
        this.setMatrices(matrices);
    }

   public void setupRenderer(
      PartFilterScheme currentFilterScheme,
      SubmitNodeCollector bufferSource,
      PoseStack matrices,
      float tickDelta,
      int light,
      float alpha,
      int overlay,
      boolean translucent,
      boolean glowing,
      double camX,
      double camY,
      double camZ
   ) {
      this.setupRenderer(currentFilterScheme, bufferSource, tickDelta, light, alpha, overlay, translucent, glowing);
      this.setMatrices(camX, camY, camZ, matrices);
   }

   private void setupRenderer(
      PartFilterScheme currentFilterScheme,
      SubmitNodeCollector bufferSource,
      float tickDelta,
      int light,
      float alpha,
      int overlay,
      boolean translucent,
      boolean glowing
   ) {
      this.currentFilterScheme = currentFilterScheme;
      this.bufferSource = bufferSource;
      this.tickDelta = tickDelta;
      this.light = light;
      this.alpha = alpha;
      this.overlay = overlay;
      this.translucent = translucent;
      this.glowing = glowing;
   }

   public void setMatrices(PoseStack matrices) {
      Pose pose = matrices.last();
      this.posMat.set(pose.pose());
      this.normalMat.set(pose.normal());
   }

   public void setMatrices(double camX, double camY, double camZ, PoseStack matrices) {
      Pose pose = matrices.last();
      Matrix4d posMat = new Matrix4d(pose.pose());
      posMat.translate(-camX, -camY, -camZ);
      posMat.scale(-1.0, -1.0, 1.0);
      this.posMat.set(posMat);
      Matrix3f normalMat = new Matrix3f(pose.normal());
      normalMat.scale(-1.0F, -1.0F, 1.0F);
      this.normalMat.set(normalMat);
   }
}
