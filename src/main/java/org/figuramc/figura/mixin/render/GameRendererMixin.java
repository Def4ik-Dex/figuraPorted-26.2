package org.figuramc.figura.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GameRendererAccessor;
import org.figuramc.figura.gui.FiguraPortraitRenderer;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.utils.RenderUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private boolean effectActive;
   @Shadow
   @Nullable
   private Identifier postEffectId;
   @Shadow
   @Final
   private CrossFrameResourcePool resourcePool;
   @Shadow
   @Final
   private GuiRenderer guiRenderer;
   @Shadow
   @Final
   private net.minecraft.client.renderer.SubmitNodeStorage handAndScreenSubmitNodeStorage;
   @Unique
   private boolean avatarPostShader = false;
   @Unique
   private boolean hasShaders;

   @Shadow
   public abstract void checkEntityPostEffect(Entity var1);

   @Shadow
   public abstract Minecraft getMinecraft();

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = Shift.AFTER))
   private void render(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
      Entity entity = this.minecraft.getCameraEntity();
      Avatar avatar = AvatarManager.getAvatar(entity);
      if (!RenderUtils.vanillaModelAndScript(avatar)) {
         if (this.avatarPostShader) {
            this.avatarPostShader = false;
            this.checkEntityPostEffect(entity);
         }
      } else {
         Identifier resource = avatar.luaRuntime.renderer.postShader;
         if (resource == null) {
            if (this.avatarPostShader) {
               this.avatarPostShader = false;
               this.checkEntityPostEffect(entity);
            }
         } else {
            try {
               this.avatarPostShader = true;
               this.effectActive = true;
               if (this.postEffectId == null || !this.postEffectId.equals(resource)) {
                  PostChain postchain = this.minecraft.getShaderManager().getPostChain(resource, LevelTargetBundle.MAIN_TARGETS);
                  if (postchain != null) {
                     postchain.process(this.minecraft.gameRenderer.mainRenderTarget(), this.resourcePool);
                  }
               }
            } catch (Exception ignored) {
               this.effectActive = false;
               avatar.luaRuntime.renderer.postShader = null;
            }
         }
      }
   }

   @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
   private void checkEntityPostEffect(Entity entity, CallbackInfo ci) {
      if (this.avatarPostShader) {
         ci.cancel();
      }
   }

   @Override
   public double figura$getFov(Camera camera, float tickDelta, boolean changingFov) {
      return camera.getFov();
   }

   @Inject(method = "renderLevel", at = @At("HEAD"))
   private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
      this.hasShaders = ClientAPI.hasShaderPack();
   }

   @ModifyArg(
      method = "renderLevel",
      index = 0,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      )
   )
   private CameraRenderState renderLevelBobHurt(CameraRenderState cameraRenderState) {
      Avatar avatar = AvatarManager.getAvatar((Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()));
      return cameraRenderState;
   }

   @ModifyArg(
      method = "<init>",
      index = 4,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"
      )
   )
   private List<PictureInPictureRenderer<?>> addPortraitRenderer(List<PictureInPictureRenderer<?>> list, @Local BufferSource source) {
      List<PictureInPictureRenderer<?>> newList = new ArrayList<>(list);
      newList.add(new FiguraPortraitRenderer(source));
      return newList;
   }

   @WrapOperation(
      method = "renderLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      )
   )
   private void figura$stopBobView(GameRenderer instance, CameraRenderState cameraRenderState, PoseStack stack, Operation<GameRenderer> original) {
      Avatar avatar = AvatarManager.getAvatar((Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()));
      if (!RenderUtils.vanillaModelAndScript(avatar) || this.hasShaders) {
         original.call(new Object[]{instance, cameraRenderState, stack});
      }
   }

   @WrapOperation(
      method = "renderLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      )
   )
   private void figura$stopBobHurt(GameRenderer instance, CameraRenderState cameraRenderState, PoseStack stack, Operation<GameRenderer> original) {
      Avatar avatar = AvatarManager.getAvatar((Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()));
      if (!RenderUtils.vanillaModelAndScript(avatar) || this.hasShaders) {
         original.call(new Object[]{instance, cameraRenderState, stack});
      }
   }

   @ModifyExpressionValue(
      method = "renderLevel",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/OptionsRenderState;screenEffectScale:F")
   )
   private float figura$disableConfusionOnMatrix(float screenEffectScale) {
      Avatar avatar = AvatarManager.getAvatar((Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()));
      return RenderUtils.vanillaModelAndScript(avatar) && !this.hasShaders ? 0.0F : screenEffectScale;
   }

   @Override
   public GuiRenderer figura$getGuiRenderer() {
      return this.guiRenderer;
   }
}
