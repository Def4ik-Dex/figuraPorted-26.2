package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.LivingEntityRendererAccessor;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.gui.PopupMenu;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.rendering.PartFilterScheme;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<S>>
   extends EntityRenderer<T, S>
   implements RenderLayerParent<S, M> {
   @Shadow
   @Final
   protected List<RenderLayer<S, M>> layers;
   @Shadow
   @Final
   protected ItemModelResolver itemModelResolver;
   @Unique
   private Avatar currentAvatar;
   @Unique
   private Matrix4f lastPose;

   protected LivingEntityRendererMixin(Context context) {
      super(context);
   }

   @Shadow
   public static int getOverlayCoords(LivingEntityRenderState arg, float whiteOverlayProgress) {
      return 0;
   }

   @Shadow
   protected abstract float getWhiteOverlayProgress(S var1);

   @Shadow
   protected abstract boolean isBodyVisible(S var1);

   @Shadow
   @NotNull
   public abstract M getModel();

   @Inject(
      at = @At("HEAD"),
      method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
   )
   private void onRender(
      S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      this.currentAvatar = AvatarManager.getAvatar(livingEntityRenderState);
      if (this.currentAvatar != null) {
         this.lastPose = poseStack.last().pose();
      }
   }

   @ModifyArg(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
      ),
      method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      index = 5
   )
   private int customOverlay(int thing) {
      return LivingEntityRendererAccessor.overrideOverlay.orElse(thing);
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
         shift = Shift.BEFORE
      ),
      method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      cancellable = true
   )
   private void setFiguraCallbacks(
      S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (this.currentAvatar != null) {
         Avatar localAvatar = this.currentAvatar;
         M model = this.getModel();
         FiguraSubmitCallBackExtension submitCallBackExtension = (FiguraSubmitCallBackExtension)(Object)model;
         if (Avatar.firstPerson) {
            submitCallBackExtension.figura$addPreRenderingCallback(pose -> false);
         } else {
            submitCallBackExtension.figura$addPreRenderingCallback(pose -> {
               this.figura$transformParts(localAvatar, model);
               this.figura$doPosTransform(localAvatar, model);
               return true;
            });
            submitCallBackExtension.figura$addPostRenderingCallback(() -> {
               if (localAvatar.luaRuntime != null) {
                  localAvatar.luaRuntime.vanilla_model.PLAYER.restore(this.getModel());
               }
            });
         }
      }
   }

   @Inject(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V", shift = Shift.AFTER),
      method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      cancellable = true
   )
   private void submitFiguraModel(
      S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (this.currentAvatar != null) {
         Avatar localAvatar = this.currentAvatar;
         M model = this.getModel();
         if (Avatar.firstPerson) {
            localAvatar.updateMatrices(model, poseStack);
            this.currentAvatar = null;
            this.lastPose = null;
            poseStack.popPose();
            ci.cancel();
         } else {
            boolean showBody = this.isBodyVisible(livingEntityRenderState);
            boolean translucent = !showBody && !livingEntityRenderState.isInvisibleToPlayer;
            boolean glowing = !showBody && livingEntityRenderState.appearsGlowing();
            boolean invisible = !translucent && !showBody && !glowing;
            Integer id = livingEntityRenderState instanceof AvatarRenderState playerRenderState
               ? playerRenderState.id
               : ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getEntityId();
            if (id != null) {
               Map<ModelPart, PartPose> modelState = RenderUtils.captureModelState(model);
               Integer entityIdBoxed = ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getEntityId();
               if (entityIdBoxed != null) {
                  int entityId = entityIdBoxed;
                  float tickDelta = ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getTickDelta();
                  int overlay = getOverlayCoords(livingEntityRenderState, this.getWhiteOverlayProgress(livingEntityRenderState));
                  Entity entity = AvatarManager.getEntity(livingEntityRenderState);
                  if (entity != null) {
                     float previewAlpha = ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getPreviewAlpha();
                     NodeCollectorExtension nodeCollectorExtension = (NodeCollectorExtension)submitNodeCollector;
                     Matrix4f lastPs = new Matrix4f(this.lastPose);
                     PoseStack poseStack2 = new PoseStack();
                     poseStack2.pushPose();
                     poseStack2.last().set(poseStack.last());
                     nodeCollectorExtension.submitFiguraModel(
                        localAvatar,
                        livingEntityRenderState,
                        (avatar, livingEntityState, bufferSource) -> {
                           RenderUtils.restoreModelPoseState(model, modelState);
                           PartFilterScheme filter = invisible ? PartFilterScheme.PIVOTS : PartFilterScheme.MODEL;
                           FiguraMod.pushProfiler("figura");
                           FiguraMod.pushProfiler(avatar);
                           FiguraMod.pushProfiler("calculateMatrix");
                           Matrix4f diff = new Matrix4f(lastPs).invert().mul(poseStack2.last().pose());
                           FiguraMat4 poseMatrix = new FiguraMat4().set(diff);
                           FiguraMod.popPushProfiler("render");
                           this.figura$transformParts(avatar, model);
                           FiguraMod.popPushProfiler("renderEvent");
                           avatar.renderEvent(tickDelta, poseMatrix);
                           avatar.render(
                              entity,
                              livingEntityState.yRot,
                              tickDelta,
                              (translucent ? 0.15F : 1.0F) * previewAlpha,
                              poseStack2,
                              bufferSource,
                              livingEntityState.lightCoords,
                              overlay,
                              model,
                              filter,
                              translucent || previewAlpha < 0.999F,
                              glowing
                           );
                           FiguraMod.popPushProfiler("postRenderEvent");
                           avatar.postRenderEvent(tickDelta, poseMatrix);
                           FiguraMod.popProfiler(3);
                           if (avatar.luaRuntime != null) {
                              avatar.luaRuntime.vanilla_model.PLAYER.restore(model);
                           }

                           return null;
                        }
                     );
                  }
               }
            }
         }
      }
   }

   @Inject(
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
      method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
   )
   private void nullifyAvatar(
      S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (this.currentAvatar != null) {
         this.currentAvatar = null;
         this.lastPose = null;
      }
   }

   @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
   private void shouldShowName(T livingEntity, double d, CallbackInfoReturnable<Boolean> cir) {
      if (UIHelper.paperdoll) {
         cir.setReturnValue(Configs.PREVIEW_NAMEPLATE.value);
      } else if (!true || livingEntity.getUUID().equals(PopupMenu.getEntityId())) {
         cir.setReturnValue(false);
      } else if (!AvatarManager.panic) {
         if (Configs.SELF_NAMEPLATE.value && livingEntity == Minecraft.getInstance().player) {
            cir.setReturnValue(true);
         } else if (Configs.NAMEPLATE_RENDER.value == 2 || Configs.NAMEPLATE_RENDER.value == 1 && livingEntity != FiguraMod.extendedPickEntity) {
            cir.setReturnValue(false);
         }
      }
   }

   @Unique
   void figura$transformParts(Avatar currentAvatar, M model) {
      if (currentAvatar.luaRuntime != null) {
         VanillaPart part = currentAvatar.luaRuntime.vanilla_model.PLAYER;
         part.save(model);
         if (currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
            part.preTransform(model);
         }
      }
   }

   @Unique
   void figura$doPosTransform(Avatar currentAvatar, M model) {
      if (currentAvatar.luaRuntime != null && currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
         currentAvatar.luaRuntime.vanilla_model.PLAYER.posTransform(model);
      }
   }

   @Inject(
      method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;clear()V", ordinal = 0, shift = Shift.AFTER)
   )
   private void shouldShowName(T entity, S livingEntityRenderState, float f, CallbackInfo ci) {
      this.itemModelResolver.updateForLiving(livingEntityRenderState.headItem, entity.getItemBySlot(EquipmentSlot.HEAD), ItemDisplayContext.HEAD, entity);
   }

   @Inject(method = "isEntityUpsideDown", at = @At("HEAD"), cancellable = true)
   private static void isEntityUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
      Avatar avatar = AvatarManager.getAvatar(entity);
      if (RenderUtils.vanillaModelAndScript(avatar)) {
         Boolean upsideDown = avatar.luaRuntime.renderer.upsideDown;
         if (upsideDown != null) {
            cir.setReturnValue(upsideDown);
         }
      }
   }
}
