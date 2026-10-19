package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage.NameTagSubmit;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer.Storage;
import org.figuramc.figura.ducks.NameTagFeatureRenderer$StorageExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagFeatureRendererMixin {
   @Inject(method = "renderTranslucent", at = @At("TAIL"))
   private void renderOutlineTexts(
      CallbackInfo ci, @Local(argsOnly = true) BufferSource bufferSource, @Local(argsOnly = true) Font font, @Local Storage storage
   ) {
      NameTagFeatureRenderer$StorageExtension storageExt = (NameTagFeatureRenderer$StorageExtension)storage;

      for (NameTagSubmit outlineText : storageExt.getOutlineSubmits()) {
         font.drawInBatch8xOutline(
            outlineText.text().getVisualOrderText(),
            outlineText.x(),
            outlineText.y(),
            outlineText.color(),
            outlineText.backgroundColor(),
            outlineText.pose(),
            bufferSource,
            outlineText.lightCoords()
         );
      }
   }
}
