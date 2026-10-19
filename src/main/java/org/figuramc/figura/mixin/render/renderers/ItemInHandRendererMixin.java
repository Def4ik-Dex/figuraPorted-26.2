package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelPart;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
   @Shadow
   private ItemStack mainHandItem;
   @Unique
   Avatar avatar;

   @Shadow
   protected abstract void renderPlayerArm(PoseStack var1, SubmitNodeCollector var2, int var3, float var4, float var5, HumanoidArm var6);

   @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
   private void onRenderHandsWithItems(
      float tickDelta, PoseStack matrices, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light, CallbackInfo ci
   ) {
      this.avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
      if (this.avatar != null) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this.avatar);
         FiguraMod.pushProfiler("renderEvent");
         this.avatar.renderMode = EntityRenderMode.FIRST_PERSON;
         this.avatar.renderEvent(tickDelta, new FiguraMat4().set(matrices.last().pose()));
         FiguraMod.popProfiler(3);
      }
   }

   @Inject(method = "submitHandsWithItems", at = @At("RETURN"))
   private void afterRenderHandsWithItems(
      float tickDelta, PoseStack matrices, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light, CallbackInfo ci
   ) {
      if (this.avatar != null) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this.avatar);
         FiguraMod.pushProfiler("postRenderEvent");
         this.avatar.postRenderEvent(tickDelta, new FiguraMat4().set(matrices.last().pose()));
         this.avatar = null;
         FiguraMod.popProfiler(3);
      }
   }

   @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
   private void renderArmWithItem(
      AbstractClientPlayer player,
      float tickDelta,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      PoseStack matrices,
      SubmitNodeCollector submitNodeCollector,
      int light,
      CallbackInfo ci
   ) {
      if (!player.isScoping() && this.avatar != null && this.avatar.luaRuntime != null) {
         boolean main = hand == InteractionHand.MAIN_HAND;
         HumanoidArm arm = main ? player.getMainArm() : player.getMainArm().getOpposite();
         Boolean armVisible = arm == HumanoidArm.LEFT ? this.avatar.luaRuntime.renderer.renderLeftArm : this.avatar.luaRuntime.renderer.renderRightArm;
         boolean willRenderItem = !item.isEmpty();
         boolean willRenderArm = !willRenderItem && main || item.is(Items.FILLED_MAP) || !willRenderItem && this.mainHandItem.is(Items.FILLED_MAP);
         if (willRenderArm && !willRenderItem && armVisible != null && !armVisible) {
            ci.cancel();
         } else {
            if (!willRenderArm && !player.isInvisible() && armVisible != null && armVisible) {
               matrices.pushPose();
               this.renderPlayerArm(matrices, submitNodeCollector, light, equipProgress, swingProgress, arm);
               matrices.popPose();
            }

            VanillaModelPart part = arm == HumanoidArm.LEFT ? this.avatar.luaRuntime.vanilla_model.LEFT_ITEM : this.avatar.luaRuntime.vanilla_model.RIGHT_ITEM;
            if (willRenderItem && !part.checkVisible()) {
               ci.cancel();
            }
         }
      }
   }

   @Inject(
      method = "renderItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
      )
   )
   private void renderItem(
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext itemDisplayContext,
      PoseStack matrices,
      SubmitNodeCollector submitNodeCollector,
      int light,
      CallbackInfo ci
   ) {
      if (stack.getItem() instanceof BlockItem bl && bl.getBlock() instanceof AbstractSkullBlock) {
         SkullBlockRendererAccessor.setEntity(entity);

         SkullBlockRendererAccessor.setRenderMode(
            switch (itemDisplayContext) {
               case FIRST_PERSON_LEFT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.FIRST_PERSON_LEFT_HAND;
               case FIRST_PERSON_RIGHT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.FIRST_PERSON_RIGHT_HAND;
               case THIRD_PERSON_LEFT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_LEFT_HAND;
               case THIRD_PERSON_RIGHT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_RIGHT_HAND;
               default -> itemDisplayContext.leftHand()
                  ? SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_LEFT_HAND
                  : SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_RIGHT_HAND;
            }
         );
      }
   }
}
