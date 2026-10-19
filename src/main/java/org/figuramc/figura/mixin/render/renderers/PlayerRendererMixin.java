package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.EntityRendererAccessor;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> implements EntityRendererAccessor {
   @Unique
   private Avatar avatar;
   @Unique
   boolean isNameRendering;
   @Unique
   boolean hasScore;

   public PlayerRendererMixin(Context context, PlayerModel entityModel, float shadowRadius) {
      super(context, entityModel, shadowRadius);
   }

   @Override
   public boolean figura$isRenderingName() {
      return this.isNameRendering;
   }

   @Override
   public boolean figura$hasScore() {
      return this.hasScore;
   }

   @Inject(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V")
   )
   private void pushProfilerForRender(
      AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      FiguraMod.popPushProfiler("render");
      FiguraMod.pushProfiler("scoreboard");
   }

   @Inject(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      at = @At("TAIL")
   )
   private void popProfiler(
      AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      this.isNameRendering = false;
      FiguraMod.popProfiler(5);
   }

   @Inject(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void renderNameTag(
      AvatarRenderState playerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      this.hasScore = playerRenderState.scoreText != null;
      this.isNameRendering = false;
      int config = Configs.ENTITY_NAMEPLATE.value;
      if (config != 0
         && !AvatarManager.panic
         && AvatarManager.getEntity(playerRenderState) instanceof Player player
         && !(this.entityRenderDispatcher.distanceToSqr(player) > 4096.0)) {
         Avatar avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
         EntityNameplateCustomization custom = avatar != null && avatar.luaRuntime != null ? avatar.luaRuntime.nameplate.ENTITY : null;
         boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;
         if (custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 0) {
            avatar.noPermissions.add(Permissions.NAMEPLATE_EDIT);
         } else if (avatar != null) {
            avatar.noPermissions.remove(Permissions.NAMEPLATE_EDIT);
         }

         if (hasCustom && !custom.visible) {
            ci.cancel();
         } else {
            if (hasCustom) {
               FiguraMod.pushProfiler("figura");
               FiguraMod.pushProfiler(player.getName().getString());
               FiguraMod.pushProfiler("nameplate");
            }
         }
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         shift = Shift.BEFORE,
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"
      ),
      method = "renderHand"
   )
   private void onRenderHand(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier resourceLocation, ModelPart modelPart, boolean bl, CallbackInfo ci
   ) {
      this.avatar = AvatarManager.getAvatarForPlayer(Minecraft.getInstance().player.getUUID());
      PlayerModel model = (PlayerModel)this.getModel();
      Map<ModelPart, PartPose> modelState = RenderUtils.captureModelState(model);
      Avatar localAvatar = this.avatar;
      Function<PoseStack, Boolean> lambda = stack -> {
         if (localAvatar != null && localAvatar.luaRuntime != null) {
            VanillaPart part = localAvatar.luaRuntime.vanilla_model.PLAYER;
            RenderUtils.restoreModelPoseState(model, modelState);
            part.save(model);
            if (localAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
               part.preTransform(model);
               part.posTransform(model);
            }
         }

         return true;
      };
      ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPreRenderingCallback(lambda);
      ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPostRenderingCallback(() -> {
         if (localAvatar != null && localAvatar.luaRuntime != null) {
            localAvatar.luaRuntime.vanilla_model.PLAYER.restore(model);
         }
      });
   }

   @WrapOperation(
      method = "renderHand",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"
      )
   )
   private void figura$replaceFirstPersonArm(
      SubmitNodeCollector submitNodeCollector,
      ModelPart modelPart,
      PoseStack poseStack,
      RenderType renderType,
      int light,
      int overlay,
      TextureAtlasSprite sprite,
      Operation<Void> original
   ) {
      Avatar avatar = AvatarManager.getAvatarForPlayer(Minecraft.getInstance().player.getUUID());
      PlayerModel model = (PlayerModel)this.getModel();
      ParentType arm = modelPart == model.leftArm ? ParentType.LeftArm : modelPart == model.rightArm ? ParentType.RightArm : null;
      if (avatar == null
         || avatar.renderer == null
         || arm == null
         || !avatar.renderer.hasModelPart(arm)) {
         original.call(submitNodeCollector, modelPart, poseStack, renderType, light, overlay, sprite);
      }
   }

   @Inject(at = @At("RETURN"), method = "renderHand")
   private void postRenderHand(
      PoseStack stack, SubmitNodeCollector submitNodeCollector, int light, Identifier resourceLocation, ModelPart arm, boolean bl, CallbackInfo ci
   ) {
      if (this.avatar != null) {
         NodeCollectorExtension nodeCollectorExt = (NodeCollectorExtension)submitNodeCollector;
         float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
         PoseStack copy = new PoseStack();
         copy.pushPose();
         copy.last().set(stack.last());
         PlayerModel playerModel = (PlayerModel)this.getModel();
         Map<ModelPart, PartPose> modelState = RenderUtils.captureModelState(playerModel);
         nodeCollectorExt.submitFiguraModel(this.avatar, null, (playerAvatar, state, bufferSource) -> {
            RenderUtils.restoreModelPoseState(playerModel, modelState);
            if (playerAvatar != null && playerAvatar.luaRuntime != null) {
               VanillaPart part = playerAvatar.luaRuntime.vanilla_model.PLAYER;
               PlayerModel model = (PlayerModel)this.getModel();
               part.save(model);
               if (playerAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                  part.preTransform(model);
                  part.posTransform(model);
               }
            }

            playerAvatar.firstPersonRender(copy, bufferSource, Minecraft.getInstance().player, playerModel, arm, light, delta);
            if (playerAvatar.luaRuntime != null) {
               playerAvatar.luaRuntime.vanilla_model.PLAYER.restore(playerModel);
            }

            return null;
         });
         this.avatar = null;
      }
   }

   @Inject(
      method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void setupRotations(AvatarRenderState playerRenderState, PoseStack matrices, float f, float g, CallbackInfo cir) {
      Avatar avatar = AvatarManager.getAvatar(playerRenderState);
      if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.getRootRotationAllowed()) {
         cir.cancel();
      }
   }
}
