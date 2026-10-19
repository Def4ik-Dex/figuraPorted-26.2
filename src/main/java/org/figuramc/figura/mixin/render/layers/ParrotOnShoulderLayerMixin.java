package org.figuramc.figura.mixin.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.parrot.Parrot.Variant;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParrotOnShoulderLayer.class)
public abstract class ParrotOnShoulderLayerMixin extends RenderLayer<AvatarRenderState, PlayerModel> {
   @Shadow
   @Final
   private ParrotModel model;

   public ParrotOnShoulderLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent) {
      super(renderLayerParent);
   }

   @Inject(at = @At("HEAD"), method = "submitOnShoulder", cancellable = true)
   private void render(
      PoseStack matrices,
      SubmitNodeCollector submitNodeCollector,
      int light,
      AvatarRenderState playerRenderState,
      Variant variant,
      float yRot,
      float xRot,
      boolean leftShoulder,
      CallbackInfo ci
   ) {
      Avatar avatar = AvatarManager.getAvatar(playerRenderState);
      if (RenderUtils.vanillaModel(avatar)) {
         if (avatar.luaRuntime == null
            || (!leftShoulder || avatar.luaRuntime.vanilla_model.LEFT_PARROT.checkVisible())
               && (leftShoulder || avatar.luaRuntime.vanilla_model.RIGHT_PARROT.checkVisible())) {
            if (avatar.pivotPartRender(
               leftShoulder ? ParentType.LeftParrotPivot : ParentType.RightParrotPivot,
               stack -> {
                  stack.translate(0.0, 24.0, 0.0);
                  float s = 16.0F;
                  stack.scale(s, s, s);
                  stack.mulPose(Axis.XP.rotationDegrees(180.0F));
                  stack.mulPose(Axis.YP.rotationDegrees(180.0F));
                  ParrotRenderState parrotState = new ParrotRenderState();
                  parrotState.ageInTicks = playerRenderState.ageInTicks;
                  parrotState.walkAnimationPos = playerRenderState.walkAnimationPos;
                  parrotState.walkAnimationSpeed = playerRenderState.walkAnimationSpeed;
                  parrotState.yRot = yRot;
                  parrotState.xRot = xRot;
                  submitNodeCollector.submitModel(
                     this.model,
                     parrotState,
                     stack,
                     this.model.renderType(ParrotRenderer.getVariantTexture(variant)),
                     light,
                     OverlayTexture.NO_OVERLAY,
                     playerRenderState.outlineColor,
                     null
                  );
               }
            )) {
               ci.cancel();
            }
         } else {
            ci.cancel();
         }
      }
   }
}
