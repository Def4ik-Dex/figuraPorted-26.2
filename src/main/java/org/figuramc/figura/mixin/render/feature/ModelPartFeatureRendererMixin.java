package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage.ModelPartSubmit;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPartFeatureRenderer.class)
public class ModelPartFeatureRendererMixin {
   @Shadow
   @Final
   private PoseStack poseStack;

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
         ordinal = 0
      ),
      cancellable = true
   )
   private <S> void figura$preRender(
      Map<RenderType, List<ModelPartSubmit>> modelPartSubmits,
      BufferSource bufferSource,
      OutlineBufferSource outlineBufferSource,
      BufferSource crumblingBufferSource,
      CallbackInfo ci,
      @Local ModelPartSubmit modelSubmit
   ) {
      FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)modelSubmit;

      for (BiFunction<MultiBufferSource, PoseStack, Boolean> callback : callBackExtension.figura$getPreRenderingCallbacks()) {
         if (!callback.apply(bufferSource, this.poseStack)) {
            ci.cancel();
         }
      }

      if (ci.isCancelled()) {
         for (Runnable callback : callBackExtension.figura$getPostRenderingCallbacks()) {
            callback.run();
         }

         callBackExtension.figura$getPostRenderingCallbacks().clear();
      }

      callBackExtension.figura$getPreRenderingCallbacks().clear();
   }

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
         ordinal = 0,
         shift = Shift.AFTER
      )
   )
   private <S> void figura$postRender(
      Map<RenderType, List<ModelPartSubmit>> modelPartSubmits,
      BufferSource bufferSource,
      OutlineBufferSource outlineBufferSource,
      BufferSource crumblingBufferSource,
      CallbackInfo ci,
      @Local ModelPartSubmit modelSubmit
   ) {
      FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)modelSubmit;

      for (Runnable callback : callBackExtension.figura$getPostRenderingCallbacks()) {
         callback.run();
      }

      callBackExtension.figura$getPostRenderingCallbacks().clear();
   }
}
