package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock.Type;
import net.minecraft.world.level.block.SkullBlock.Types;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.PlayerHeadRenderInfoExtension;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.ducks.SkullBlockRendererHelper;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState>, PlayerHeadRenderInfoExtension {
   @Unique
   private static Avatar avatar;
   @Unique
   private static SkullBlockRenderState block;

   @Inject(at = @At("HEAD"), method = "submitSkull", cancellable = true)
   private static void renderSkull(
      float animationProgress,
      PoseStack stack,
      SubmitNodeCollector submitNodeCollector,
      int light,
      SkullModelBase model,
      RenderType renderLayer,
      int outlineColor,
      CrumblingOverlay crumblingOverlay,
      CallbackInfo ci
   ) {
      if (avatar == null) {
         avatar = SkullBlockRendererHelper.getAvatar();
         SkullBlockRendererHelper.setAvatar(null);
      }

      SkullBlockRenderState localBlock = block;
      block = null;
      ItemStack localItem = SkullBlockRendererAccessor.getItem();
      SkullBlockRendererAccessor.setItem(null);
      Entity localEntity = SkullBlockRendererAccessor.getEntity();
      SkullBlockRendererAccessor.setEntity(null);
      SkullBlockRendererAccessor.SkullRenderMode localMode = SkullBlockRendererAccessor.getRenderMode();
      SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.OTHER);
      Avatar localAvatar = avatar;
      avatar = null;
      if (localAvatar != null && localAvatar.permissions.get(Permissions.CUSTOM_SKULL) != 0) {
         float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
         FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension)(Object)model;
         modelExtension.figura$addPreRenderingCallback(
            poseStack -> {
               FiguraMod.pushProfiler("figura");
               FiguraMod.pushProfiler(localAvatar);
               FiguraMod.pushProfiler("skullRender");
               BlockStateAPI b = localBlock == null
                  ? null
                  : new BlockStateAPI(((BlockEntityRenderStateAccessor)localBlock).figura$getBlockState(), localBlock.blockPos);
               ItemStackAPI i = localItem != null ? ItemStackAPI.verify(localItem) : null;
               EntityAPI<?> e = localEntity != null ? EntityAPI.wrap(localEntity) : null;
               String m = localMode.name();
               FiguraMod.pushProfiler(localBlock != null ? localBlock.blockPos.toString() : String.valueOf(i));
               FiguraMod.pushProfiler("event");
               boolean bool = localAvatar.skullRenderEvent(tickDelta, b, i, e, m);
               FiguraMod.popPushProfiler("render");
               if (!bool && !localAvatar.skullRender(stack, submitNodeCollector, light, null, 0.0F)) {
                  FiguraMod.popProfiler(5);
                  return true;
               } else {
                  return false;
               }
            }
         );
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/model/object/skull/SkullModelBase;Lnet/minecraft/client/renderer/rendertype/RenderType;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
      ),
      method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
   )
   public void render(
      SkullBlockRenderState skullBlockRenderState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState,
      CallbackInfo ci
   ) {
      block = skullBlockRenderState;
      SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.BLOCK);
   }

   public boolean shouldRenderOffScreen() {
      Avatar localAvatar = avatar;
      return localAvatar != null && localAvatar.permissions != null
         ? localAvatar.permissions.get(Permissions.OFFSCREEN_RENDERING) == 1
         : BlockEntityRenderer.super.shouldRenderOffScreen();
   }

   @Inject(at = @At("HEAD"), method = "resolveSkullRenderType")
   private static void getRenderType(Type type, SkullBlockEntity skullBlockEntity, CallbackInfoReturnable<RenderType> cir) {
      if (type == Types.PLAYER) {
         ResolvableProfile profile = skullBlockEntity.getOwnerProfile();
         if (profile != null) {
            avatar = AvatarManager.getAvatarForPlayer(profile.partialProfile().id());
         }
      }
   }
}
