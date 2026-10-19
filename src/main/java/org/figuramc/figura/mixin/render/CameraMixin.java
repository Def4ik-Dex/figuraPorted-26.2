package org.figuramc.figura.mixin.render;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   private float xRot;
   @Shadow
   private float yRot;
   @Shadow
   private Entity entity;
   @Shadow
   private float fovModifier;
   @Unique
   private Avatar avatar;

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void move(float var1, float var2, float var3);

   @Inject(method = "alignWithEntity", at = @At("HEAD"))
   private void setupAvatarVar(float partialTicks, CallbackInfo ci) {
      this.avatar = AvatarManager.getAvatar(this.entity);
   }

   @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", shift = Shift.AFTER), require = 1)
   private void setupRot(float partialTicks, CallbackInfo ci) {
      if (!RenderUtils.vanillaModelAndScript(this.avatar)) {
         this.avatar = null;
      } else {
         float x = this.xRot;
         float y = this.yRot;
         FiguraVec3 rot = this.avatar.luaRuntime.renderer.cameraRot;
         if (rot != null && rot.notNaN()) {
            x = (float)rot.x;
            y = (float)rot.y;
         }

         FiguraVec3 offset = this.avatar.luaRuntime.renderer.cameraOffsetRot;
         if (offset != null && offset.notNaN()) {
            x += (float)offset.x;
            y += (float)offset.y;
         }

         this.setRotation(y, x);
      }
   }

   @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 0)
   private double setupPivotX(double originalX) {
      if (RenderUtils.vanillaModelAndScript(this.avatar)) {
         double x = originalX;
         FiguraVec3 piv = this.avatar.luaRuntime.renderer.cameraPivot;
         if (piv != null && piv.notNaN()) {
            x = piv.x;
         }

         FiguraVec3 offset = this.avatar.luaRuntime.renderer.cameraOffsetPivot;
         if (offset != null && offset.notNaN()) {
            x += offset.x;
         }

         return x;
      } else {
         return originalX;
      }
   }

   @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 1)
   private double setupPivotY(double originalY) {
      if (RenderUtils.vanillaModelAndScript(this.avatar)) {
         double y = originalY;
         FiguraVec3 piv = this.avatar.luaRuntime.renderer.cameraPivot;
         if (piv != null && piv.notNaN()) {
            y = piv.y;
         }

         FiguraVec3 offset = this.avatar.luaRuntime.renderer.cameraOffsetPivot;
         if (offset != null && offset.notNaN()) {
            y += offset.y;
         }

         return y;
      } else {
         return originalY;
      }
   }

   @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 2)
   private double setupPivotZ(double originalZ) {
      if (RenderUtils.vanillaModelAndScript(this.avatar)) {
         double z = originalZ;
         FiguraVec3 piv = this.avatar.luaRuntime.renderer.cameraPivot;
         if (piv != null && piv.notNaN()) {
            z = piv.z;
         }

         FiguraVec3 offset = this.avatar.luaRuntime.renderer.cameraOffsetPivot;
         if (offset != null && offset.notNaN()) {
            z += offset.z;
         }

         return z;
      } else {
         return originalZ;
      }
   }

   @Inject(method = "alignWithEntity", at = @At("RETURN"))
   private void setupPos(float partialTicks, CallbackInfo ci) {
      if (RenderUtils.vanillaModelAndScript(this.avatar)) {
         FiguraVec3 pos = this.avatar.luaRuntime.renderer.cameraPos;
         if (pos != null && pos.notNaN()) {
            this.move((float)(-pos.z), (float)pos.y, (float)(-pos.x));
         }

         this.avatar = null;
      }
   }

   @Inject(method = "tickFov", at = @At("RETURN"))
   private void tickFov(CallbackInfo ci) {
      Avatar avatar = AvatarManager.getAvatar(this.entity);
      if (RenderUtils.vanillaModelAndScript(avatar)) {
         Float fov = avatar.luaRuntime.renderer.fov;
         if (fov != null) {
            this.fovModifier = fov;
         }
      }
   }

   @Inject(method = "extractRenderState", at = @At("RETURN"))
   private void extractRenderState(CameraRenderState cameraRenderState, float tickDelta, CallbackInfo ci) {
      Avatar avatar = AvatarManager.getAvatar(this.entity);
      if (RenderUtils.vanillaModelAndScript(avatar)) {
         Matrix4f vanillaViewRotation = new Matrix4f(cameraRenderState.viewRotationMatrix);
         FiguraMat4 mat = avatar.luaRuntime.renderer.cameraMat;
         if (mat != null) {
            cameraRenderState.viewRotationMatrix.set(mat.toMatrix4f()).mul(vanillaViewRotation);
         } else {
            float z = 0.0F;
            FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
            if (rot != null && rot.notNaN()) {
               z = (float)rot.z;
            }

            FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
            if (offset != null && offset.notNaN()) {
               z += (float)offset.z;
            }

            if (z != 0.0F) {
               cameraRenderState.viewRotationMatrix.rotation(Axis.ZP.rotationDegrees(z)).mul(vanillaViewRotation);
            }
         }
      }
   }

   @Inject(method = "xRot", at = @At("HEAD"), cancellable = true)
   private void xRot(CallbackInfoReturnable<Float> cir) {
      if (UIHelper.paperdoll) {
         cir.setReturnValue(0.0F);
      }
   }

   @Inject(method = "yRot", at = @At("HEAD"), cancellable = true)
   private void yRot(CallbackInfoReturnable<Float> cir) {
      if (UIHelper.paperdoll) {
         cir.setReturnValue(0.0F);
      }
   }
}
