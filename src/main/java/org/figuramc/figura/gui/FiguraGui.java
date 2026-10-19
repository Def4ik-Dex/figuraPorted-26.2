package org.figuramc.figura.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GameRendererAccessor;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FiguraGui {
   private static final ProjectionMatrixBuffer guiProjectionMatrixBuffer = new ProjectionMatrixBuffer("gui");

   public static void onRender(GuiGraphicsExtractor guiGraphics, float tickDelta, CallbackInfo ci) {
      if (!AvatarManager.panic) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler("popupMenu");
         PopupMenu.render(guiGraphics);
         FiguraMod.popProfiler();
         Entity entity = Minecraft.getInstance().getCameraEntity();
         Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);
         Window window = Minecraft.getInstance().getWindow();
         GpuBufferSlice previousProjectionMatrix = RenderSystem.getProjectionMatrixBuffer();
         ProjectionType previousProjectionType = RenderSystem.getProjectionType();
         RenderSystem.setProjectionMatrix(
            guiProjectionMatrixBuffer.getBuffer(
               new Matrix4f()
                  .setOrtho(0.0F, (float)window.getWidth() / window.getGuiScale(), (float)window.getHeight() / window.getGuiScale(), 0.0F, 1000.0F, 11000.0F)
            ),
            ProjectionType.ORTHOGRAPHIC
         );
         Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
         matrix4fStack.pushMatrix();
         matrix4fStack.translation(0.0F, 0.0F, -11000.0F);
         if (avatar != null) {
            PoseStack stack = new PoseStack();
            stack.pushPose();
            stack.setIdentity();
            stack.last().pose().mul(guiGraphics.pose());
            avatar.hudRender(
               stack,
               ((GameRendererAccessor)Minecraft.getInstance().gameRenderer).figura$getHandAndScreenSubmitNodeStorage(),
               entity,
               tickDelta
            );
            stack.popPose();
            if (avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderHUD) {
               renderOverlays(guiGraphics);
               ci.cancel();
            }
         }

         matrix4fStack.popMatrix();
         RenderSystem.setProjectionMatrix(previousProjectionMatrix, previousProjectionType);
         FiguraMod.popProfiler();
      }
   }

   public static void renderOverlays(GuiGraphicsExtractor guiGraphics) {
      FiguraMod.pushProfiler("figura");
      FiguraMod.pushProfiler("paperdoll");
      PaperDoll.render(guiGraphics, false);
      FiguraMod.popPushProfiler("actionWheel");
      ActionWheel.render(guiGraphics);
      FiguraMod.popProfiler(2);
   }
}
