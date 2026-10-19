package org.figuramc.figura.mixin.render.renderers;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.ducks.EntityRendererAccessor;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> implements EntityRendererAccessor {
   @Unique
   private static final String FIGURA$SUBMIT_NAME_DISPLAY = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V";
   @Unique
   private static final String FIGURA$SUBMIT_NAME_TAG = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V";

   @Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
   private void shouldRender(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
      Avatar avatar = AvatarManager.getAvatar(entity);
      if (avatar != null && avatar.permissions.get(Permissions.OFFSCREEN_RENDERING) == 1) {
         cir.setReturnValue(true);
      }
   }

   @Inject(at = @At("HEAD"), method = "extractRenderState")
   private void extractRenderState(T entity, S entityRenderState, float f, CallbackInfo ci) {
      ((FiguraEntityRenderStateExtension)entityRenderState).figura$setEntityId(entity.getId());
      ((FiguraEntityRenderStateExtension)entityRenderState).figura$setTickDelta(f);
   }

   @ModifyArg(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
         ordinal = 0
      ),
      index = 6
   )
   private CameraRenderState setAvatarForScoreSubmission(CameraRenderState cameraRenderState, @Local(argsOnly = true) EntityRenderState entityRenderState) {
      return this.figura$cameraStateForSubmission(cameraRenderState, entityRenderState, false);
   }

   @ModifyArg(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
         ordinal = 1
      ),
      index = 6
   )
   private CameraRenderState setAvatarForNameSubmission(CameraRenderState cameraRenderState, @Local(argsOnly = true) EntityRenderState entityRenderState) {
      return this.figura$cameraStateForSubmission(cameraRenderState, entityRenderState, true);
   }

   @Inject(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
         ordinal = 1
      )
   )
   private void enableModifyPlayerName(
      EntityRenderState entityRenderState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState,
      int offset,
      CallbackInfo ci
   ) {
      if (entityRenderState instanceof AvatarRenderState) {
         FiguraMod.popPushProfiler("name");
      }
   }

   @ModifyArg(
      method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
         ordinal = 1
      ),
      index = 3
   )
   private Component modifyPlayerNameText(Component text, @Local(argsOnly = true) EntityRenderState entityRenderState) {
      if (!(entityRenderState instanceof AvatarRenderState player)) {
         return text;
      } else {
         int config = Configs.ENTITY_NAMEPLATE.value;
         if (config != 0 && !AvatarManager.panic) {
            Avatar avatar = AvatarManager.getAvatar(player);
            EntityNameplateCustomization custom = avatar != null && avatar.luaRuntime != null ? avatar.luaRuntime.nameplate.ENTITY : null;
            boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;
            Component name = player.nameTag;
            FiguraMod.popPushProfiler("text");
            Component replacement = (Component)(hasCustom && custom.getJson() != null ? custom.getJson().copy() : name);
            Component var10 = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);
            FiguraMod.popPushProfiler("badges");
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(player.id) != null) {
               var10 = Badges.appendBadges(var10, Minecraft.getInstance().level.getEntity(player.id).getUUID(), config > 1);
            }

            FiguraMod.popPushProfiler("applyName");
            return TextUtils.replaceInText(text, "\\b" + Pattern.quote(player.nameTag.getString()) + "\\b", var10);
         } else {
            return text;
         }
      }
   }

   @Unique
   private CameraRenderState figura$cameraStateForSubmission(CameraRenderState cameraRenderState, EntityRenderState entityRenderState, boolean renderingName) {
      Avatar figura$avatar = AvatarManager.getAvatar(entityRenderState);
      if (figura$avatar != null) {
         CameraRenderState replacement = new CameraRenderState();
         replacement.pos = cameraRenderState.pos;
         replacement.blockPos = cameraRenderState.blockPos;
         replacement.initialized = cameraRenderState.initialized;
         replacement.orientation = cameraRenderState.orientation;
         ((CameraRenderStateExtension)replacement).figura$setAvatar(figura$avatar);
         ((CameraRenderStateExtension)replacement).figura$setRenderingNameTag(renderingName);
         return replacement;
      } else {
         return cameraRenderState;
      }
   }
}
