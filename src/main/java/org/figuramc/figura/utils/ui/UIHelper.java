package org.figuramc.figura.utils.ui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.gui.screens.AbstractPanelScreen;
import org.figuramc.figura.gui.screens.FiguraConfirmScreen;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.mixin.gui.GuiGraphicsExtractorAccessor;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.TextUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public final class UIHelper {
   public static final Identifier OUTLINE_FILL = FiguraIdentifier.of("textures/gui/outline_fill.png");
   public static final Identifier OUTLINE = FiguraIdentifier.of("textures/gui/outline.png");
   public static final Identifier TOOLTIP = FiguraIdentifier.of("textures/gui/tooltip.png");
   public static final Identifier UI_FONT = FiguraIdentifier.of("ui");
   public static final Identifier SPECIAL_FONT = FiguraIdentifier.of("special");
   public static final Component UP_ARROW = Component.literal("^").withStyle(Style.EMPTY.withFont(new Resource(UI_FONT)));
   public static final Component DOWN_ARROW = Component.literal("V").withStyle(Style.EMPTY.withFont(new Resource(UI_FONT)));
   private static int previousFBO = -1;
   public static boolean paperdoll = false;
   public static float fireRot = 0.0F;
   public static float dollScale = 1.0F;
   private static GpuBuffer buffer = null;

   private UIHelper() {
   }

   private static void setFiguraLighting() {
      if (buffer == null) {
         GpuDevice gpuDevice = RenderSystem.getDevice();
         int paddedSize = Mth.roundToward(Lighting.UBO_SIZE, gpuDevice.getDeviceInfo().limits().minUniformOffsetAlignment());
         buffer = gpuDevice.createBuffer(() -> "Figura Lighting UBO", 136, paddedSize);
         Vector3f lighting0 = (Vector3f)Util.make(new Vector3f(-0.2F, -1.0F, 1.0F), Vector3f::normalize);
         Vector3f lighting1 = (Vector3f)Util.make(new Vector3f(-0.2F, 0.4F, 0.3F), Vector3f::normalize);
         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, Lighting.UBO_SIZE).putVec3(lighting0).putVec3(lighting1).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(0L, paddedSize), byteBuffer);
         } catch (Throwable var8) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }
      }
   }

   public static void drawEntity(
      float x,
      float y,
      float scale,
      float pitch,
      float yaw,
      LivingEntity entity,
      GuiGraphicsExtractor gui,
      Vector3f offset,
      EntityRenderMode renderMode,
      int x1,
      int y1,
      int x2,
      int y2
   ) {
      drawEntity(x, y, scale, pitch, yaw, entity, gui, offset, renderMode, x1, y1, x2, y2, 1.0F);
   }

   public static void drawEntity(
      float x,
      float y,
      float scale,
      float pitch,
      float yaw,
      LivingEntity entity,
      GuiGraphicsExtractor gui,
      Vector3f offset,
      EntityRenderMode renderMode,
      int x1,
      int y1,
      int x2,
      int y2,
      float previewAlpha
   ) {
      float headX = entity.getXRot();
      float headY = entity.yHeadRot;
      boolean invisible = entity.isInvisible();
      float bodyY = entity.yBodyRot;
      if (entity.getVehicle() instanceof LivingEntity l) {
         bodyY = l.yBodyRot;
      }

      double xPos = 0.0;
      double yPos = 0.0;
      float xRot;
      float yRot;
      switch (renderMode) {
         case PAPERDOLL:
            xRot = pitch;
            yRot = yaw + bodyY + 180.0F;
            yPos--;
            if (entity.isFallFlying()) {
               xPos += Mth.triangleWave((float)Math.toRadians(270.0), (float) (Math.PI * 2));
            }

            if (entity.isAutoSpinAttack() || entity.isVisuallySwimming() || entity.isFallFlying()) {
               yPos++;
               entity.setXRot(0.0F);
            }

            Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ENTITY_IN_UI);
            if (Configs.PAPERDOLL_INVISIBLE.value) {
               entity.setInvisible(false);
            }
            break;
         case FIGURA_GUI:
            xRot = pitch;
            yRot = yaw + bodyY + 180.0F;
            if (!Configs.PREVIEW_HEAD_ROTATION.value) {
               entity.setXRot(0.0F);
               entity.yHeadRot = bodyY;
            }

            yPos -= 1.5;
            useFiguraLighting();
            entity.setInvisible(false);
            break;
         default:
            xRot = pitch;
            yRot = yaw + bodyY + 180.0F;
            entity.setXRot(-xRot);
            entity.yHeadRot = -yaw + bodyY;
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ENTITY_IN_UI);
      }

      Matrix3x2fStack pose = gui.pose();
      gui.nextStratum();
      pose.pushMatrix();
      pose.translate(x, y);
      pose.scale(scale, scale);
      Avatar avatar = AvatarManager.getAvatar(entity);
      if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.getRootRotationAllowed()) {
         yRot = yaw;
      }

      Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
      Quaternionf quaternion2 = Axis.YP.rotationDegrees(yRot);
      Quaternionf quaternion3 = Axis.XP.rotationDegrees(xRot);
      quaternion3.mul(quaternion2);
      quaternion.mul(quaternion3);
      Minecraft minecraft = Minecraft.getInstance();
      EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
      DebugScreenEntryList entryList = Minecraft.getInstance().debugEntries;
      DebugScreenEntryStatus renderHitboxes = entryList.getStatus(DebugScreenEntries.ENTITY_HITBOXES);
      entryList.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.NEVER);
      CameraRenderState cameraRenderState = new CameraRenderState();
      cameraRenderState.orientation = quaternion3;
      paperdoll = true;
      fireRot = -yRot;
      dollScale = scale;
      if (avatar != null) {
         avatar.renderMode = renderMode;
      }

      EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      @SuppressWarnings("unchecked")
      EntityRenderer<? super LivingEntity, LivingEntityRenderState> entityRenderer = (EntityRenderer)entityRenderDispatcher.getRenderer(entity);
      LivingEntityRenderState entityRenderState = (LivingEntityRenderState)entityRenderer.createRenderState();
      entityRenderer.extractRenderState(entity, entityRenderState, 1.0F);
      ((FiguraEntityRenderStateExtension)entityRenderState).figura$setPreviewAlpha(Mth.clamp(previewAlpha, 0.0F, 1.0F));
      GuiEntityRenderState state = new GuiEntityRenderState(
         entityRenderState,
         offset,
         quaternion,
         quaternion3,
         x1,
         y1,
         x2,
         y2,
         scale / entity.getScale(),
         ((GuiGraphicsExtractorAccessor)gui).figura$getScissorStack().peek()
      );
      ((GuiEntityRenderStateExtension)(Object)state).setRenderMode(renderMode);
      ((GuiEntityRenderStateExtension)(Object)state).setXPos(xPos);
      ((GuiEntityRenderStateExtension)(Object)state).setYPos(yPos);
      ((GuiGraphicsExtractorAccessor)gui).figura$getRenderState().addPicturesInPictureState(state);
      entryList.setStatus(DebugScreenEntries.ENTITY_HITBOXES, renderHitboxes);
      pose.popMatrix();
      Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ITEMS_3D);
      entity.setXRot(headX);
      entity.yHeadRot = headY;
      entity.setInvisible(invisible);
   }

   public static void enableBlend() {
      GlStateManager._enableBlend(0);
       
      GlStateManager._blendFuncSeparate(770, 771, 1, 0);
   }

   public static void blit(GuiGraphicsExtractor gui, int x, int y, int width, int height, Identifier texture) {
      gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, 1, 1, 1, 1);
   }

   public static void renderAnimatedBackground(
      GuiGraphicsExtractor gui,
      Identifier texture,
      float x,
      float y,
      float width,
      float height,
      float textureWidth,
      float textureHeight,
      double speed,
      float delta
   ) {
      if (speed != 0.0) {
         double d = (FiguraMod.ticks + delta) * speed;
         x = (float)(x - d % textureWidth);
         y = (float)(y - d % textureHeight);
      }

      width += textureWidth;
      height += textureHeight;
      if (speed < 0.0) {
         x -= textureWidth;
         y -= textureHeight;
      }

      renderBackgroundTexture(gui, texture, x, y, width, height, textureWidth, textureHeight);
   }

   public static void renderBackgroundTexture(
      GuiGraphicsExtractor gui, Identifier texture, float x, float y, float width, float height, float textureWidth, float textureHeight
   ) {
      float u1 = width / textureWidth;
      float v1 = height / textureHeight;
      quad(gui, gui.pose(), x, y, width, height, -999.0F, 0.0F, u1, 0.0F, v1, texture);
   }

   public static void fillRounded(GuiGraphicsExtractor gui, int x, int y, int width, int height, int color) {
      gui.fill(x + 1, y, x + width - 1, y + 1, color);
      gui.fill(x, y + 1, x + width, y + height - 1, color);
      gui.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
   }

   public static void fillOutline(GuiGraphicsExtractor gui, int x, int y, int width, int height, int color) {
      gui.fill(x + 1, y, x + width - 1, y + 1, color);
      gui.fill(x, y + 1, x + 1, y + height - 1, color);
      gui.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
      gui.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
   }

   public static void blitSliced(GuiGraphicsExtractor gui, int x, int y, int width, int height, Identifier texture) {
      blitSliced(gui, x, y, width, height, 0.0F, 0.0F, 15, 15, 15, 15, texture);
   }

   public static void blitSliced(
      GuiGraphicsExtractor gui,
      int x,
      int y,
      int width,
      int height,
      float u,
      float v,
      int regionWidth,
      int regionHeight,
      int textureWidth,
      int textureHeight,
      Identifier texture
   ) {
      Matrix3x2f pose = gui.pose();
      float rWidthThird = regionWidth / 3.0F;
      float rHeightThird = regionHeight / 3.0F;
      quad(gui, pose, x, y, rWidthThird, rHeightThird, u, v, rWidthThird, rHeightThird, textureWidth, textureHeight, texture);
      quad(
         gui,
         pose,
         x + rWidthThird,
         y,
         width - rWidthThird * 2.0F,
         rHeightThird,
         u + rWidthThird,
         v,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x + width - rWidthThird,
         y,
         rWidthThird,
         rHeightThird,
         u + rWidthThird * 2.0F,
         v,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x,
         y + rHeightThird,
         rWidthThird,
         height - rHeightThird * 2.0F,
         u,
         v + rHeightThird,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x + rWidthThird,
         y + rHeightThird,
         width - rWidthThird * 2.0F,
         height - rHeightThird * 2.0F,
         u + rWidthThird,
         v + rHeightThird,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x + width - rWidthThird,
         y + rHeightThird,
         rWidthThird,
         height - rHeightThird * 2.0F,
         u + rWidthThird * 2.0F,
         v + rHeightThird,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x,
         y + height - rHeightThird,
         rWidthThird,
         rHeightThird,
         u,
         v + rHeightThird * 2.0F,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x + rWidthThird,
         y + height - rHeightThird,
         width - rWidthThird * 2.0F,
         rHeightThird,
         u + rWidthThird,
         v + rHeightThird * 2.0F,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
      quad(
         gui,
         pose,
         x + width - rWidthThird,
         y + height - rHeightThird,
         rWidthThird,
         rHeightThird,
         u + rWidthThird * 2.0F,
         v + rHeightThird * 2.0F,
         rWidthThird,
         rHeightThird,
         textureWidth,
         textureHeight,
         texture
      );
   }

   public static void renderHalfTexture(GuiGraphicsExtractor gui, int x, int y, int width, int height, int textureWidth, Identifier texture) {
      renderHalfTexture(gui, x, y, width, height, 0.0F, 0.0F, textureWidth, 1, textureWidth, 1, texture);
   }

   public static void renderHalfTexture(
      GuiGraphicsExtractor gui,
      int x,
      int y,
      int width,
      int height,
      float u,
      float v,
      int regionWidth,
      int regionHeight,
      int textureWidth,
      int textureHeight,
      Identifier texture
   ) {
      enableBlend();
      int w = width / 2;
      gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, height, w, regionHeight, textureWidth, textureHeight);
      x += w;
      if (width % 2 == 1) {
         w++;
      }

      gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u + regionWidth - w, v, w, height, w, regionHeight, textureWidth, textureHeight);
   }

   public static void renderSprite(GuiGraphicsExtractor gui, int x, int y, int z, int width, int height, TextureAtlasSprite sprite) {
      gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, z);
   }

   private static void quad(
      GuiGraphicsExtractor gui,
      Matrix3x2f pose,
      float x,
      float y,
      float width,
      float height,
      float u,
      float v,
      float regionWidth,
      float regionHeight,
      int textureWidth,
      int textureHeight,
      @Nullable Identifier texture
   ) {
      float u0 = u / textureWidth;
      float v0 = v / textureHeight;
      float u1 = (u + regionWidth) / textureWidth;
      float v1 = (v + regionHeight) / textureHeight;
      quad(gui, pose, x, y, width, height, 0.0F, u0, u1, v0, v1, texture);
   }

   private static void quad(
      GuiGraphicsExtractor gui,
      Matrix3x2f pose,
      float x,
      float y,
      float width,
      float height,
      float z,
      float u0,
      float u1,
      float v0,
      float v1,
      @Nullable Identifier texture
   ) {
      float x1 = x + width;
      float y1 = y + height;
      TextureSetup setup;
      if (texture != null) {
         AbstractTexture gpuTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
         setup = TextureSetup.singleTexture(gpuTexture.getTextureView(), gpuTexture.getSampler());
      } else {
         setup = TextureSetup.noTexture();
      }

      ((GuiGraphicsExtractorAccessor)gui)
         .figura$getRenderState()
         .addBlitToCurrentLayer(
            new BlitRenderState(
               RenderPipelines.GUI_TEXTURED,
               setup,
               pose,
               (int)x,
               (int)y,
               (int)x1,
               (int)y1,
               u0,
               u1,
               v0,
               v1,
               -1,
               ((GuiGraphicsExtractorAccessor)gui).figura$getScissorStack().peek()
            )
         );
   }

   public static void renderWithoutScissors(GuiGraphicsExtractor gui, Consumer<GuiGraphicsExtractor> toRun) {
      gui.enableScissor(0, 0, 1, 1);
      RenderSystem.disableScissorForRenderTypeDraws();
      toRun.accept(gui);
      gui.disableScissor();
   }

   public static void highlight(GuiGraphicsExtractor gui, FiguraWidget widget, Component text) {
      if (Minecraft.getInstance().gui.screen() instanceof AbstractPanelScreen panel) {
         int screenW = panel.width;
         int screenH = panel.height;
         int var16 = widget.getX();
         int y = widget.getY();
         int width = widget.getWidth();
         int height = widget.getHeight();
         int color = -587202560;
         gui.fill(0, 0, var16, y + height, color);
         gui.fill(var16 + width, y, screenW, screenH, color);
         gui.fill(var16, 0, screenW, y, color);
         gui.fill(0, y + height, var16 + width, screenH, color);
         fillOutline(gui, Math.max(var16 - 1, 0), Math.max(y - 1, 0), Math.min(width + 2, screenW), Math.min(height + 2, screenH), -1);
         if (text != null) {
            int bottomDistance = screenH - (y + height);
            int rightDistance = screenW - (var16 + width);
            int verArea = y * screenW - bottomDistance * screenW;
            int horArea = var16 * screenH - rightDistance * screenH;
            FiguraVec4 square = new FiguraVec4();
            if (Math.abs(verArea) > Math.abs(horArea)) {
               if (verArea >= 0) {
                  square.set(0.0, 0.0, screenW, y);
               } else {
                  square.set(0.0, y + height, screenW, bottomDistance);
               }
            } else if (horArea >= 0) {
               square.set(0.0, 0.0, var16, screenH);
            } else {
               square.set(var16 + width, 0.0, rightDistance, screenH);
            }
         }
      }
   }

   public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
      return isMouseOver(x, y, width, height, mouseX, mouseY, false);
   }

   public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY, boolean force) {
      ContextMenu context = force ? null : getContext();
      return (context == null || !context.isVisible()) && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   public static void renderOutlineText(GuiGraphicsExtractor gui, Font textRenderer, Component text, int x, int y, int color, int outline) {
      color = adjustColor(color);
      outline = adjustColor(outline);

      for (int ox = -1; ox <= 1; ox++) {
         for (int oy = -1; oy <= 1; oy++) {
            if (ox != 0 || oy != 0) {
               gui.text(textRenderer, text, x + ox, y + oy, outline, false);
            }
         }
      }

      gui.text(textRenderer, text, x, y, color, false);
   }

   public static void renderTooltip(GuiGraphicsExtractor gui, Component tooltip, int mouseX, int mouseY, boolean background) {
      Minecraft minecraft = Minecraft.getInstance();
      int screenX = minecraft.getWindow().getGuiScaledWidth();
      int screenY = minecraft.getWindow().getGuiScaledHeight();
      boolean reduced = Configs.REDUCED_MOTION.value;
      int x = reduced ? 0 : mouseX;
      int y = reduced ? screenY : mouseY - 12;
      Font font = minecraft.font;
      List<FormattedCharSequence> text = TextUtils.wrapTooltip(tooltip, font, x, screenX, 12);
      int height = 9 * text.size();
      x += 12;
      y = Math.min(Math.max(y, 0), screenY - height);
      int width = TextUtils.getWidth(text, font);
      if (x + width > screenX) {
         x = Math.max(x - width - 24, 0);
      }

      if (reduced) {
         x += (screenX - width) / 2;
         if (background) {
            y -= 4;
         }
      }

      gui.pose().pushMatrix();
      gui.nextStratum();
      if (background) {
         blitSliced(gui, x - 4, y - 4, width + 8, height + 8, TOOLTIP);
      }

      for (int i = 0; i < text.size(); i++) {
         FormattedCharSequence charSequence = text.get(i);
         gui.text(font, charSequence, x, y + 9 * i, adjustColor(16777215));
      }

      gui.pose().popMatrix();
   }

   public static void renderScrollingText(GuiGraphicsExtractor gui, Component text, int x, int y, int width, int color) {
      Font font = Minecraft.getInstance().font;
      int textWidth = font.width(text);
      int textX = x;
      color = adjustColor(color);
      if (textWidth <= width) {
         gui.text(font, text, textX, y, color);
      } else {
         textX += getTextScrollingOffset(textWidth, width, false);
         gui.enableScissor(x, y, x + width, y + 9);
         gui.text(font, text, textX, y, color);
         gui.disableScissor();
      }
   }

   public static void renderCenteredScrollingText(GuiGraphicsExtractor gui, Component text, int x, int y, int width, int height, int color) {
      Font font = Minecraft.getInstance().font;
      int textWidth = font.width(text);
      int textX = x + width / 2;
      int textY = y + height / 2 - 9 / 2;
      color = adjustColor(color);
      if (textWidth <= width) {
         gui.centeredText(font, text, textX, textY, color);
      } else {
         textX += getTextScrollingOffset(textWidth, width, true);
         gui.enableScissor(x, y, x + width, y + height);
         gui.centeredText(font, text, textX, textY, color);
         gui.disableScissor();
      }
   }

   private static int getTextScrollingOffset(int textWidth, int width, boolean centered) {
      float speed = Configs.TEXT_SCROLL_SPEED.tempValue;
      int scrollLen = textWidth - width;
      int startingOffset = (int)Math.ceil(scrollLen / 2.0);
      int stopDelay = (int)(Configs.TEXT_SCROLL_DELAY.tempValue.intValue() * speed);
      int time = scrollLen + stopDelay;
      int totalTime = time * 2;
      int ticks = (int)(FiguraMod.ticks * speed);
      int currentTime = ticks % time;
      int dir = ticks % totalTime > time - 1 ? 1 : -1;
      int clamp = Math.min(Math.max(currentTime - stopDelay, 0), scrollLen);
      return (startingOffset - clamp) * dir - (centered ? 0 : startingOffset);
   }

   public static int adjustColor(int argbColor) {
      return (argbColor & -67108864) == 0 ? ARGB.opaque(argbColor) : argbColor;
   }

   public static Runnable openURL(String url) {
      Minecraft minecraft = Minecraft.getInstance();
      return () -> minecraft.gui.setScreen(new FiguraConfirmScreen.FiguraConfirmLinkScreen(bl -> {
         if (bl) {
            Util.getPlatform().openUri(url);
         }
      }, url, minecraft.gui.screen()));
   }

   public static void renderLoading(GuiGraphicsExtractor gui, int x, int y) {
      Component text = Component.literal(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)).withStyle(Style.EMPTY.withFont(new Resource(Badges.FONT)));
      Font font = Minecraft.getInstance().font;
      gui.text(font, text, x - font.width(text) / 2, y - 9 / 2, adjustColor(-1), false);
   }

   public static void setContext(ContextMenu context) {
      if (Minecraft.getInstance().gui.screen() instanceof AbstractPanelScreen panelScreen) {
         panelScreen.contextMenu = context;
      }
   }

   public static ContextMenu getContext() {
      return Minecraft.getInstance().gui.screen() instanceof AbstractPanelScreen panelScreen ? panelScreen.contextMenu : null;
   }

   public static void setTooltip(Component text) {
      if (Minecraft.getInstance().gui.screen() instanceof AbstractPanelScreen panelScreen) {
         panelScreen.tooltip = text;
      }
   }

   public static void setTooltip(Style style) {
      if (style != null && style.getHoverEvent() != null) {
         Component text = style.getHoverEvent() instanceof ShowText ? ((ShowText)style.getHoverEvent()).value() : null;
         if (text != null) {
            setTooltip(text);
         }
      }
   }

   public static void useFiguraLighting() {
      setFiguraLighting();
      RenderSystem.setShaderLights(buffer.slice());
   }
}
