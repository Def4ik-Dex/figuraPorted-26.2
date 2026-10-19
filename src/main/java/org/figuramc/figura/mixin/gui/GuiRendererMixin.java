package org.figuramc.figura.mixin.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.gui.FiguraGuiEntityRenderer;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
   @Shadow
   @Final
   GuiRenderState renderState;
   @Shadow
   @Final
   FeatureRenderDispatcher featureRenderDispatcher;
   @Unique
   FiguraGuiEntityRenderer figura$paperDollRenderer;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void initializePaperDollRenderer(
      GuiRenderState guiRenderState,
      FeatureRenderDispatcher featureRenderDispatcher,
      List<PictureInPictureRenderer<?>> list,
      CallbackInfo ci
   ) {
      this.figura$paperDollRenderer = new FiguraGuiEntityRenderer(Minecraft.getInstance().getEntityRenderDispatcher());
   }

   @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
   private <T extends PictureInPictureRenderState> void renderPaperDoll(T pictureInPictureRenderState, int i, CallbackInfo ci) {
      if (pictureInPictureRenderState instanceof GuiEntityRenderStateExtension extension
         && extension.getRenderMode() != null
         && extension.getRenderMode() == EntityRenderMode.PAPERDOLL) {
         this.figura$paperDollRenderer.prepare((GuiEntityRenderState)pictureInPictureRenderState, this.renderState, this.featureRenderDispatcher, i);
         ci.cancel();
      }
   }

   @Inject(
      method = "close",
      at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = Shift.AFTER)
   )
   private void onClose(CallbackInfo ci) {
      this.figura$paperDollRenderer.close();
   }
}
