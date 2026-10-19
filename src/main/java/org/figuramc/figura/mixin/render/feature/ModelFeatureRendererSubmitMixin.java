package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelFeatureRenderer.Submit.class)
public class ModelFeatureRendererSubmitMixin implements FiguraSubmitCallBackExtension {
   @Unique
   private final List<Function<PoseStack, Boolean>> figura$preRenderingCallbacks = new ArrayList<>();
   @Unique
   private final List<Runnable> figura$postRenderingCallbacks = new ArrayList<>();
   @Unique
   private boolean figura$preventAnimSetup;

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

   @Override
   public boolean figura$getPreventAnimSetup() {
      return this.figura$preventAnimSetup;
   }

   @Override
   public void figura$setPreventAnimSetup(boolean prevent) {
      this.figura$preventAnimSetup = prevent;
   }
}
