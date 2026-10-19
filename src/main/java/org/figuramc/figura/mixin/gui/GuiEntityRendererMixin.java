package org.figuramc.figura.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin extends PictureInPictureRenderer<GuiEntityRenderState> {
   @Shadow
   @Final
   private EntityRenderDispatcher entityRenderDispatcher;

   protected GuiEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher) {
      super();
   }

   @WrapOperation(
      method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
      )
   )
   private <S extends EntityRenderState> void setFiguraRenderProperties(
      EntityRenderDispatcher instance,
      S entityRenderState,
      CameraRenderState cameraRenderState,
      double d,
      double e,
      double f,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      Operation<Void> original,
      @Local(argsOnly = true) GuiEntityRenderState guiEntityRenderState
   ) {
      GuiEntityRenderStateExtension extended = (GuiEntityRenderStateExtension)(Object)guiEntityRenderState;
      if (AvatarManager.getAvatar(entityRenderState) != null && extended.getRenderMode() != null) {
         AvatarManager.getAvatar(entityRenderState).renderMode = extended.getRenderMode();
      }

      original.call(
         new Object[]{instance, entityRenderState, cameraRenderState, d + extended.getXPos(), e + extended.getYPos(), f, poseStack, submitNodeCollector}
      );
   }

   @WrapOperation(
      method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V")
   )
   private <S extends EntityRenderState> void setFiguraRenderProperties(
      Lighting instance, Entry entry, Operation<Void> original, @Local(argsOnly = true) GuiEntityRenderState guiEntityRenderState
   ) {
      EntityRenderMode mode = ((GuiEntityRenderStateExtension)(Object)guiEntityRenderState).getRenderMode();
      if (mode == EntityRenderMode.FIGURA_GUI) {
         UIHelper.useFiguraLighting();
      } else {
         original.call(new Object[]{instance, entry});
      }
   }

   @Inject(
      method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
      at = @At("HEAD")
   )
   private void setPaperDollMode(
      GuiEntityRenderState guiEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci
   ) {
      UIHelper.paperdoll = true;
   }

   @Inject(
      method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
      at = @At("TAIL")
   )
   private void disablePaperDollMode(
      GuiEntityRenderState guiEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci
   ) {
      UIHelper.paperdoll = false;
   }
}
