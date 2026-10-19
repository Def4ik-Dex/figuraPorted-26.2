package org.figuramc.figura.mixin.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.Model;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Model.class)
public class ModelMixin implements FiguraSubmitCallBackExtension {
   @Unique
   private final List<Function<PoseStack, Boolean>> figura$preRenderingCallback = new ArrayList<>();
   @Unique
   private final List<Runnable> figura$postRenderingCallback = new ArrayList<>();

   @Override
   public void figura$addPreRenderingCallback(Function<PoseStack, Boolean> callback) {
      this.figura$preRenderingCallback.add(callback);
   }

   @Override
   public void figura$addPostRenderingCallback(Runnable callback) {
      this.figura$postRenderingCallback.add(callback);
   }

   @Override
   public List<Runnable> figura$getPostRenderingCallbacks() {
      return this.figura$postRenderingCallback;
   }

   @Override
   public List<Function<PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
      return this.figura$preRenderingCallback;
   }
}
