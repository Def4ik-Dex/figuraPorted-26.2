package org.figuramc.figura.mixin.render.nodeRenderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Function;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage.FlameSubmit;
import net.minecraft.client.renderer.SubmitNodeStorage.ItemSubmit;
import net.minecraft.client.renderer.SubmitNodeStorage.ModelPartSubmit;
import net.minecraft.client.renderer.SubmitNodeStorage.ModelSubmit;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer.Storage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.FlameSubmitExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraSubmission;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin implements NodeCollectorExtension {
   @Unique
   List<FiguraSubmission> figuraSubmissions = new ArrayList<>();

   @Override
   @SuppressWarnings("unchecked")
   public <S extends EntityRenderState> void submitFiguraModel(Avatar avatar, S renderState, TriFunction<Avatar, S, net.minecraft.client.renderer.SubmitNodeCollector, Void> renderer) {
      this.figuraSubmissions.add(new FiguraSubmission(avatar, renderState, (TriFunction)renderer));
   }

   @Override
   public List<FiguraSubmission> getFiguraSubmissions() {
      return this.figuraSubmissions;
   }

   @Inject(
      method = "submitModel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;)V"
      )
   )
   private <S> void figura$onSubmitModel(
           Model<? super S> model,
           S object,
           PoseStack poseStack,
           RenderType renderType,
           int i,
           int j,
           int k,
           @Nullable TextureAtlasSprite textureAtlasSprite,
           int l,
           @Nullable CrumblingOverlay crumblingOverlay,
           CallbackInfo ci,
           @Local ModelFeatureRenderer.Submit<S> modelSubmit 
   ) {
      FiguraSubmitCallBackExtension modelSubmissionExtension = (FiguraSubmitCallBackExtension)modelSubmit;
      FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension)model;

      for (Function<PoseStack, Boolean> callback : modelExtension.figura$getPreRenderingCallbacks()) {
         modelSubmissionExtension.figura$addPreRenderingCallback(callback);
      }

      for (Runnable callback : modelExtension.figura$getPostRenderingCallbacks()) {
         modelSubmissionExtension.figura$addPostRenderingCallback(callback);
      }

      modelSubmissionExtension.figura$setPreventAnimSetup(modelExtension.figura$getPreventAnimSetup());
      modelExtension.figura$setPreventAnimSetup(false);
      modelExtension.figura$getPreRenderingCallbacks().clear();
      modelExtension.figura$getPostRenderingCallbacks().clear();
   }

   @WrapOperation(
      method = "submitModelPart",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/feature/ModelPartFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;)V"
      )
   )
   private void figura$onSubmitModelPart(Storage instance, RenderType key, Submit modelPartSubmit, Operation<Void> original) {
      FiguraSubmitCallBackExtension modelSubmissionExtension = (FiguraSubmitCallBackExtension)modelPartSubmit;
      FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension)modelPartSubmit.modelPart();

      for (Function<PoseStack, Boolean> callback : modelExtension.figura$getPreRenderingCallbacks()) {
         modelSubmissionExtension.figura$addPreRenderingCallback(callback);
      }

      for (Runnable callback : modelExtension.figura$getPostRenderingCallbacks()) {
         modelSubmissionExtension.figura$addPostRenderingCallback(callback);
      }

      modelSubmissionExtension.figura$setPreventAnimSetup(modelExtension.figura$getPreventAnimSetup());
      modelExtension.figura$setPreventAnimSetup(false);
      modelExtension.figura$getPreRenderingCallbacks().clear();
      modelExtension.figura$getPostRenderingCallbacks().clear();
      original.call(new Object[]{instance, key, modelPartSubmit});
   }

   @WrapOperation(method = "submitItem", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
   private <E> boolean figura$onSubmitModelPart(List<E> instance, E e, Operation<Boolean> original) {
      ItemFeatureRenderer.Submit itemSubmit = (ItemFeatureRenderer.Submit)e;
      FiguraSubmitCallBackExtension itemSubmissionExtension = (FiguraSubmitCallBackExtension)itemSubmit;
      FiguraSubmitCallBackExtension displayContextExtension = (FiguraSubmitCallBackExtension)itemSubmit.displayContext();

      for (Function<PoseStack, Boolean> callback : displayContextExtension.figura$getPreRenderingCallbacks()) {
         itemSubmissionExtension.figura$addPreRenderingCallback(callback);
      }

      for (Runnable callback : displayContextExtension.figura$getPostRenderingCallbacks()) {
         itemSubmissionExtension.figura$addPostRenderingCallback(callback);
      }

      itemSubmissionExtension.figura$setPreventAnimSetup(displayContextExtension.figura$getPreventAnimSetup());
      displayContextExtension.figura$setPreventAnimSetup(false);
      displayContextExtension.figura$getPreRenderingCallbacks().clear();
      displayContextExtension.figura$getPostRenderingCallbacks().clear();
      return (Boolean)original.call(new Object[]{instance, e});
   }

   @WrapOperation(method = "submitFlame", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
   private <E> boolean figura$onSubmitFlame(List<E> instance, E e, Operation<Boolean> original) {
      FlameFeatureRenderer.Submit flameSubmit = (FlameFeatureRenderer.Submit)e;
      FlameSubmitExtension itemSubmissionExtension = (FlameSubmitExtension)flameSubmit;
      Avatar avatar = AvatarManager.getAvatar(flameSubmit.entityRenderState());
      itemSubmissionExtension.figura$setAvatar(avatar);
      return (Boolean)original.call(new Object[]{instance, e});
   }

   @Inject(method = "clear", at = @At("HEAD"))
   private void figura$clearSubmissions(CallbackInfo ci) {
      this.figuraSubmissions.clear();
   }
}
