package org.figuramc.figura.mixin.render.nodeRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage.ModelSubmit;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelSubmit.class)
public class SubmitNodeStorage$ModelSubmitMixin<S> implements FiguraSubmitCallBackExtension {
   @Unique
   private final List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$preRenderingCallback = new ArrayList<>();
   @Unique
   private final List<Runnable> figura$postRenderingCallback = new ArrayList<>();

   @Override
   public void figura$addPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback) {
      this.figura$preRenderingCallback.add(callback);
   }

   @Override
   public void figura$addPostRenderingCallback(Runnable callback) {
      this.figura$postRenderingCallback.add(callback);
   }

   @Override
   public List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
      return this.figura$preRenderingCallback;
   }

   @Override
   public List<Runnable> figura$getPostRenderingCallbacks() {
      return this.figura$postRenderingCallback;
   }
}
