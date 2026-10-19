package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraProjectileRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTridentRenderer.class)
public abstract class TridentRendererMixin<T extends ThrownTrident, S extends ThrownTridentRenderState> extends EntityRenderer<T, S> {
   @Shadow
   @Final
   private TridentModel model;

   protected TridentRendererMixin(Context ctx) {
      super(ctx);
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         shift = Shift.BEFORE,
         target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
      ),
      method = "submit(Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
      cancellable = true
   )
   private void render(
      ThrownTridentRenderState thrownTridentRenderState,
      PoseStack pose,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState,
      CallbackInfo ci
   ) {
      Integer id = ((FiguraEntityRenderStateExtension)thrownTridentRenderState).figura$getEntityId();
      if (id != null) {
         Projectile trident = (Projectile)AvatarManager.ENTITY_CACHE.computeIfAbsent(id, id2 -> Minecraft.getInstance().level.getEntity(id2));
         if (trident != null) {
            float tickDelta = ((FiguraProjectileRenderStateExtension)thrownTridentRenderState).figura$getTickDelta();
            Entity owner = trident.getOwner();
            if (owner != null) {
               Avatar avatar = AvatarManager.getAvatar(owner);
               if (avatar != null && avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) != 0) {
                  FiguraSubmitCallBackExtension figura$submitCallback = (FiguraSubmitCallBackExtension)(Object)this.model;
                  figura$submitCallback.figura$addPreRenderingCallback(poseStack -> {
                     FiguraMod.pushProfiler("figura");
                     FiguraMod.pushProfiler(avatar);
                     FiguraMod.pushProfiler("tridentRender");
                     FiguraMod.pushProfiler("event");
                     boolean bool = avatar.tridentRenderEvent(tickDelta, EntityAPI.wrap(trident));
                     FiguraMod.popPushProfiler("render");
                     if (!bool && !avatar.renderTrident(poseStack, submitNodeCollector, tickDelta, thrownTridentRenderState.lightCoords)) {
                        FiguraMod.popProfiler(4);
                        return true;
                     } else {
                        poseStack.popPose();
                        return false;
                     }
                  });
               }
            }
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      method = "extractRenderState(Lnet/minecraft/world/entity/projectile/arrow/ThrownTrident;Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;F)V"
   )
   void appendFiguraProperties(T trident, S tridentState, float f, CallbackInfo ci) {
      ((FiguraProjectileRenderStateExtension)tridentState).figura$setTickDelta(f);
   }
}
