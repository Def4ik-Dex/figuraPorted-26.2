package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFeatureRenderer.Submit.class)
public class ItemFeatureRendererSubmitMixin implements FiguraSubmitCallBackExtension {
   @Unique
   private final List<Function<PoseStack, Boolean>> figura$preRenderingCallbacks = new ArrayList<>();
   @Unique
   private final List<Runnable> figura$postRenderingCallbacks = new ArrayList<>();

   @Override
   public void figura$addPreRenderingCallback(Function<PoseStack, Boolean> callback) {
      this.figura$preRenderingCallbacks.add(callback);
   }

   @Override
   public List<Function<PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
      return this.figura$preRenderingCallbacks;
   }

   @Override
   public void figura$addPostRenderingCallback(Runnable callback) {
      this.figura$postRenderingCallbacks.add(callback);
   }

   @Override
   public List<Runnable> figura$getPostRenderingCallbacks() {
      return this.figura$postRenderingCallbacks;
   }
}
