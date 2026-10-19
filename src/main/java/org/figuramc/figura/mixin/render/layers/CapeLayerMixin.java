package org.figuramc.figura.mixin.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.ducks.PlayerModelCapeAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.mixin.ClientAvatarStateAccessor;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AvatarRenderState, PlayerModel> {
   @Shadow
   @Final
   private HumanoidModel<AvatarRenderState> model;
   @Unique
   private Avatar avatar;

   public CapeLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent) {
      super(renderLayerParent);
   }

   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
      at = @At("HEAD")
   )
   private void preRender(
      PoseStack pose, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState playerRenderState, float f, float g, CallbackInfo ci
   ) {
      ItemStack itemStack = playerRenderState.chestEquipment;
      if (!playerRenderState.isInvisible && !itemStack.is(Items.ELYTRA)) {
         this.avatar = AvatarManager.getAvatar(playerRenderState);
         if (this.avatar != null) {
            ModelPart fakeCloak = ((PlayerModelCapeAccessor)this.model).figura$getFakeCloak();
            ModelPart realCloak = ((PlayerModelCapeAccessor)this.model).figura$getCloak();
            fakeCloak.loadPose(realCloak.storePose());
            if (AvatarManager.getEntity(playerRenderState) instanceof AbstractClientPlayer entity) {
               float var34 = ((FiguraEntityRenderStateExtension)playerRenderState).figura$getTickDelta();
               ClientAvatarStateAccessor entityState = (ClientAvatarStateAccessor)entity.avatarState();
               double d = Mth.lerp(var34, entityState.figura$xCloakO(), entityState.figura$xCloak()) - Mth.lerp(var34, entity.xo, entity.getX());
               double e = Mth.lerp(var34, entityState.figura$yCloakO(), entityState.figura$yCloak()) - Mth.lerp(var34, entity.yo, entity.getY());
               double m = Mth.lerp(var34, entityState.figura$zCloakO(), entityState.figura$zCloak()) - Mth.lerp(var34, entity.zo, entity.getZ());
               float n = Mth.rotLerp(var34, entity.yBodyRotO, entity.yBodyRot);
               n = (float)Math.toRadians(n);
               double o = Mth.sin(n);
               double p = -Mth.cos(n);
               float q = (float)e * 10.0F;
               q = Mth.clamp(q, -6.0F, 32.0F);
               float r = (float)(d * o + m * p) * 100.0F;
               r = Mth.clamp(r, 0.0F, 150.0F);
               float s = (float)(d * p - m * o) * 100.0F;
               s = Mth.clamp(s, -20.0F, 20.0F);
               r = Math.max(r, 0.0F);
               float t = Mth.lerp(var34, entityState.figura$bobO(), entityState.figura$bob());
               q += Mth.sin(Mth.lerp(var34, entityState.figura$walkDistO(), entityState.figura$walkDist()) * 6.0F) * 32.0F * t;
               if (itemStack.isEmpty() || this.avatar.luaRuntime != null && !this.avatar.luaRuntime.vanilla_model.CHESTPLATE_BODY.checkVisible()) {
                  if (entity.isCrouching()) {
                     q += 25.0F;
                     fakeCloak.y = 2.25F;
                     fakeCloak.z = -0.25F;
                  } else {
                     fakeCloak.y = 0.0F;
                     fakeCloak.z = 0.0F;
                  }
               } else if (entity.isCrouching()) {
                  q += 25.0F;
                  fakeCloak.y = 0.85F;
                  fakeCloak.z = 0.15F;
               } else {
                  fakeCloak.y = -1.0F;
                  fakeCloak.z = 1.0F;
               }

               float finalR = r;
               float finalS = s;
               float finalQ = q;
               FiguraSubmitCallBackExtension submitCallBackExtension = (FiguraSubmitCallBackExtension)(Object)realCloak;
               submitCallBackExtension.figura$addPreRenderingCallback(
                  poseStackx -> {
                     fakeCloak.setRotation(
                        (float)Math.toRadians(6.0F + finalR / 2.0F + finalQ), (float)(-Math.toRadians(finalS / 2.0F)), (float)Math.toRadians(finalS / 2.0F)
                     );
                     if (this.avatar.luaRuntime != null) {
                        VanillaPart part = this.avatar.luaRuntime.vanilla_model.CAPE;
                        part.save(this.model);
                        if (this.avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                           part.preTransform(this.model);
                        }
                     }

                     if (RenderUtils.vanillaModelAndScript(this.avatar)) {
                        this.avatar.luaRuntime.vanilla_model.CAPE.posTransform(this.model);
                     }

                     return true;
                  }
               );
               PoseStack poseStack = new PoseStack();
               poseStack.pushPose();
               poseStack.last().set(pose.last());
               ((NodeCollectorExtension)submitNodeCollector)
                  .submitFiguraModel(
                     this.avatar,
                     playerRenderState,
                     (avatar, renderState, multiBufferSource) -> {
                        fakeCloak.setRotation(
                           (float)Math.toRadians(6.0F + finalR / 2.0F + finalQ), (float)(-Math.toRadians(finalS / 2.0F)), (float)Math.toRadians(finalS / 2.0F)
                        );
                        if (avatar.luaRuntime != null) {
                           VanillaPart part = avatar.luaRuntime.vanilla_model.CAPE;
                           part.save(this.model);
                           if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                              part.preTransform(this.model);
                           }
                        }

                        avatar.capeRender(entity, multiBufferSource, poseStack, renderState.lightCoords, var34, fakeCloak);
                        return null;
                     }
                  );
               submitCallBackExtension.figura$addPostRenderingCallback(() -> {
                  if (this.avatar != null) {
                     if (this.avatar.luaRuntime != null) {
                        this.avatar.luaRuntime.vanilla_model.CAPE.restore(this.model);
                     }

                     this.avatar = null;
                  }
               });
            }
         }
      }
   }
}
