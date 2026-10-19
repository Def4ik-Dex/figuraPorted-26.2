package org.figuramc.figura.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.mixin.render.PoseStackAccessor;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixinFabric {
   @Shadow
   @Final
   private EntityRenderDispatcher entityRenderDispatcher;
   @Shadow
   @Final
   private SubmitNodeStorage submitNodeStorage;
   @Shadow
   @Final
   private FeatureRenderDispatcher featureRenderDispatcher;

   @Inject(
      method = "submitEntities",
      at = @At("HEAD")
   )
   private void renderLevelFirstPerson(
      PoseStack stack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      Camera camera = minecraft.gameRenderer.mainCamera();
      DeltaTracker deltaTracker = minecraft.getDeltaTracker();
      Avatar cameraAvatar = AvatarManager.getAvatar((Entity)(minecraft.getCameraEntity() == null ? minecraft.player : minecraft.getCameraEntity()));
      if (RenderUtils.vanillaModelAndScript(cameraAvatar)) {
         FiguraMat3 normal = cameraAvatar.luaRuntime.renderer.cameraNormal;
         if (normal != null) {
            stack.last().normal().set(normal.toMatrix3f());
         }
      }

      if (!camera.isDetached()) {
         float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
         Entity e = camera.entity();
         Avatar avatar = AvatarManager.getAvatar(e);
         if (avatar != null && e instanceof LivingEntity livingEntity) {
            @SuppressWarnings("unchecked")
            EntityRenderer<LivingEntity, LivingEntityRenderState> entityRenderer = (EntityRenderer)this.entityRenderDispatcher.getRenderer(livingEntity);
            LivingEntityRenderState state = (LivingEntityRenderState)entityRenderer.createRenderState(
               livingEntity, deltaTracker.getGameTimeDeltaPartialTick(minecraft.level.tickRateManager().isEntityFrozen(e))
            );
            avatar.firstPersonWorldRender(e, submitNodeCollector, stack, camera, tickDelta);
            if (Configs.FIRST_PERSON_MATRICES.value) {
               Avatar.firstPerson = true;
               int lastIndex = ((PoseStackAccessor)stack).getLastIndex();
               stack.pushPose();
               Vec3 offset = entityRenderer.getRenderOffset(state);
               Vec3 cam = camera.position();
               stack.translate(
                  Mth.lerp(tickDelta, livingEntity.xOld, livingEntity.getX()) - cam.x() + offset.x(),
                  Mth.lerp(tickDelta, livingEntity.yOld, livingEntity.getY()) - cam.y() + offset.y(),
                  Mth.lerp(tickDelta, livingEntity.zOld, livingEntity.getZ()) - cam.z() + offset.z()
               );
               entityRenderer.submit(state, stack, submitNodeCollector, levelRenderState.cameraRenderState);

               do {
                  stack.popPose();
               } while (((PoseStackAccessor)stack).getLastIndex() > lastIndex);
            }

            this.featureRenderDispatcher.renderAllFeatures(this.submitNodeStorage);
            Avatar.firstPerson = false;
         }
      }
   }
}
