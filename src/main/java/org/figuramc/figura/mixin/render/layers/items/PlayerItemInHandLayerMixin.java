package org.figuramc.figura.mixin.render.layers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin<S extends AvatarRenderState, M extends EntityModel<S> & ArmedModel<S> & HeadedModel>
   extends ItemInHandLayer<S, M> {
   @Unique
   S figura$renderState;

   public PlayerItemInHandLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
      super(renderLayerParent);
   }

   @Inject(
      method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
      at = @At("HEAD")
   )
   void captureState(
      S avatarRenderState,
      ItemStackRenderState itemStackRenderState,
      ItemStack itemStack,
      HumanoidArm humanoidArm,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int i,
      CallbackInfo ci
   ) {
      this.figura$renderState = avatarRenderState;
   }

   @Inject(method = "renderItemHeldToEye", at = @At("HEAD"), cancellable = true)
   private void adjustSpyglassVisibility(
      S avatarRenderState, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci
   ) {
      ItemStackRenderState itemStackRenderState = avatarRenderState.heldOnHead;
      if (!itemStackRenderState.isEmpty()) {
         boolean left = humanoidArm == HumanoidArm.LEFT;
         Avatar av = AvatarManager.getAvatar(this.figura$renderState);
         if (RenderUtils.renderArmItem(av, left, ci)) {
            if (av.pivotPartRender(
               left ? ParentType.LeftSpyglassPivot : ParentType.RightSpyglassPivot,
               stack -> {
                  float s = 10.0F;
                  stack.scale(s, s, s);
                  stack.translate(0.0F, 0.0F, 0.4375F);
                  ItemTransform transform = ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$getItemTransform();
                  NodeCollectorExtension nodeCollectorExtension = (NodeCollectorExtension)submitNodeCollector;
                  nodeCollectorExtension.submitFiguraModel(
                     av,
                     avatarRenderState,
                     (avatar, entityState, multibufferSource) -> {
                        if (!avatar.itemRenderEvent(
                           ItemStackAPI.verify(((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$getItemStack()),
                           ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$getDisplayContext().name(),
                           FiguraVec3.fromVec3f(transform.translation()),
                           FiguraVec3.of(transform.rotation().z(), transform.rotation().y(), transform.rotation().x()),
                           FiguraVec3.fromVec3f(transform.scale()),
                           ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$isLeftHanded(),
                           stack,
                           submitNodeCollector,
                           light,
                           OverlayTexture.NO_OVERLAY
                        )) {
                           itemStackRenderState.submit(stack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, entityState.outlineColor);
                        }

                        return null;
                     }
                  );
               }
            )) {
               ci.cancel();
            }
         }
      }
   }
}
