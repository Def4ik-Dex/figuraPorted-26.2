package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeStorage.NameTagSubmit;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer.Storage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.ducks.NameTagFeatureRenderer$StorageExtension;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Storage.class)
public class NameTagFeatureRenderer$StorageMixin implements NameTagFeatureRenderer$StorageExtension {
   @Unique
   final List<NameTagSubmit> figura$outlineSubmits = new ArrayList<>();
   @Unique
   Avatar figura$avatar;
   @Unique
   EntityNameplateCustomization figura$custom;
   @Unique
   List<Component> figura$textList;
   @Unique
   boolean figura$hasCustomNameplate;
   @Unique
   boolean figura$enabled;
   @Unique
   boolean figura$isRenderingName;

   @Inject(at = @At("HEAD"), method = "add")
   private void setupAvatar(
      PoseStack poseStack, Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      this.figura$avatar = ((CameraRenderStateExtension)cameraRenderState).figura$getAvatar();
      this.figura$isRenderingName = ((CameraRenderStateExtension)cameraRenderState).figura$isRenderingNameTag();
      ((CameraRenderStateExtension)cameraRenderState).figura$setAvatar(null);
      ((CameraRenderStateExtension)cameraRenderState).figura$setRenderingNameTag(false);
      if (this.figura$avatar != null) {
         this.figura$custom = this.figura$avatar != null && this.figura$avatar.luaRuntime != null ? this.figura$avatar.luaRuntime.nameplate.ENTITY : null;
         this.figura$hasCustomNameplate = this.figura$custom != null && this.figura$avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;
         this.figura$enabled = Configs.ENTITY_NAMEPLATE.value > 0 && !AvatarManager.panic && this.figura$hasCustomNameplate;
         this.figura$textList = TextUtils.splitText(component, "\n");
      }
   }

   @Inject(at = @At("TAIL"), method = "add")
   private void clearAvatar(
      PoseStack poseStack, Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      this.figura$avatar = null;
      this.figura$isRenderingName = false;
      this.figura$custom = null;
      this.figura$hasCustomNameplate = false;
      this.figura$enabled = false;
      this.figura$textList = null;
   }

   @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V"), method = "add")
   private void modifyPivot(PoseStack instance, double x, double y, double z, Operation<Void> original) {
      FiguraVec3 pivot = FiguraVec3.of(x, y, z);
      if (this.figura$enabled && this.figura$avatar != null) {
         FiguraMod.pushProfiler("pivot");
         if (this.figura$hasCustomNameplate && this.figura$custom.getPivot() != null) {
            pivot = this.figura$custom.getPivot();
         }
      }

      original.call(new Object[]{instance, pivot.x, pivot.y, pivot.z});
   }

   @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", shift = Shift.AFTER), method = "add")
   private void modifyPos(
      PoseStack matrices, Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (this.figura$enabled && this.figura$avatar != null) {
         FiguraMod.popPushProfiler("position");
         if (this.figura$hasCustomNameplate && this.figura$custom.getPos() != null) {
            FiguraVec3 pos = this.figura$custom.getPos();
            matrices.translate(pos.x, pos.y, pos.z);
         }
      }
   }

   @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), method = "add")
   private void modifyScale(PoseStack instance, float x, float y, float z, Operation<Void> original) {
      FiguraVec3 scaleVec = FiguraVec3.of(x, y, z);
      if (this.figura$enabled && this.figura$avatar != null) {
         FiguraMod.popPushProfiler("scale");
         if (this.figura$hasCustomNameplate && this.figura$custom.getScale() != null) {
            scaleVec.multiply(this.figura$custom.getScale());
         }
      }

      original.call(new Object[]{instance, (float)scaleVec.x, (float)scaleVec.y, (float)scaleVec.z});
   }

   @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;"), method = "add")
   private void setShadowMatrix(
      PoseStack matrices,
      Vec3 vec3,
      int i,
      Component component,
      boolean bl,
      int j,
      double d,
      CameraRenderState cameraRenderState,
      CallbackInfo ci,
      @Share("textMatrix") LocalRef<Matrix4f> textMatrix
   ) {
      if (this.figura$enabled && this.figura$avatar != null && this.figura$hasCustomNameplate && this.figura$custom.shadow) {
         textMatrix.set(matrices.last().pose());
         if (this.figura$enabled && this.figura$avatar != null && this.figura$hasCustomNameplate && this.figura$custom.shadow) {
            matrices.pushPose();
            textMatrix.set(matrices.last().pose());
            matrices.popPose();
         }
      }
   }

   @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
   private <E> boolean drawWithColor(List<E> instance, E e, Operation<Boolean> original) {
      NameTagSubmit submit = (NameTagSubmit)e;
      Font font = Minecraft.getInstance().font;
      if (this.figura$enabled && this.figura$avatar != null && this.figura$hasCustomNameplate) {
         int light = this.figura$custom.light != null ? this.figura$custom.light : submit.lightCoords();
         int backgroundColor = this.figura$custom.background != null ? this.figura$custom.background : submit.backgroundColor();
         boolean deadmau = submit.text().getString().equals("deadmau5");
         if (this.figura$isRenderingName) {
            for (int i = 0; i < this.figura$textList.size(); i++) {
               Component text1 = this.figura$textList.get(i);
               if (!text1.getString().isEmpty()) {
                  int line = i - this.figura$textList.size() + 1;
                  float x = -font.width(text1) / 2.0F;
                  float y = (deadmau ? -10.0F : 0.0F) + (9 + 1) * line;
                  original.call(
                     new Object[]{instance, new NameTagSubmit(submit.pose(), x, y, text1, light, submit.color(), backgroundColor, submit.distanceToCameraSq())}
                  );
               }
            }

            return true;
         } else {
            return (Boolean)original.call(
               new Object[]{
                  instance,
                  new NameTagSubmit(submit.pose(), submit.x(), submit.y(), submit.text(), light, submit.color(), backgroundColor, submit.distanceToCameraSq())
               }
            );
         }
      } else {
         return (Boolean)original.call(new Object[]{instance, e});
      }
   }

   @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
   private <E> boolean drawWithOutline(List<E> instance, E e, Operation<Boolean> original, @Share("textMatrix") LocalRef<Matrix4f> textMatrix) {
      NameTagSubmit submit = (NameTagSubmit)e;
      Font font = Minecraft.getInstance().font;
      Matrix4f pose = new Matrix4f(submit.pose());
      int color = submit.color();
      boolean deadmau = submit.text().getString().equals("deadmau5");
      Matrix4f shadowMatrix = textMatrix.get() != null ? (Matrix4f)textMatrix.get() : pose;
      if (this.figura$enabled && this.figura$avatar != null && this.figura$hasCustomNameplate && this.figura$custom.outline) {
         int outlineColor = this.figura$custom.outlineColor != null ? this.figura$custom.outlineColor : 2105376;
         if (this.figura$isRenderingName) {
            for (int i = 0; i < this.figura$textList.size(); i++) {
               Component text1 = this.figura$textList.get(i);
               if (!text1.getString().isEmpty()) {
                  int line = i - this.figura$textList.size() + 1;
                  float x = -font.width(text1) / 2.0F;
                  float y = (deadmau ? -10.0F : 0.0F) + (9 + 1) * line;
                  this.figura$outlineSubmits.add(new NameTagSubmit(pose, x, y, text1, submit.lightCoords(), color, outlineColor, submit.distanceToCameraSq()));
               }
            }
         } else {
            this.figura$outlineSubmits
               .add(new NameTagSubmit(pose, submit.x(), submit.y(), submit.text(), submit.lightCoords(), color, outlineColor, submit.distanceToCameraSq()));
         }

         return (Boolean)original.call(
            new Object[]{
               instance,
               new NameTagSubmit(
                  shadowMatrix, submit.x(), submit.y(), Component.empty(), submit.lightCoords(), color, submit.backgroundColor(), submit.distanceToCameraSq()
               )
            }
         );
      } else if (this.figura$enabled && this.figura$avatar != null && this.figura$hasCustomNameplate && this.figura$isRenderingName) {
         for (int i = 0; i < this.figura$textList.size(); i++) {
            Component text1 = this.figura$textList.get(i);
            if (!text1.getString().isEmpty()) {
               int line = i - this.figura$textList.size() + 1;
               float x = -font.width(text1) / 2.0F;
               float y = (deadmau ? -10.0F : 0.0F) + (9 + 1) * line;
               original.call(
                  new Object[]{
                     instance, new NameTagSubmit(shadowMatrix, x, y, text1, submit.lightCoords(), color, submit.backgroundColor(), submit.distanceToCameraSq())
                  }
               );
            }
         }

         return true;
      } else {
         return (Boolean)original.call(
            new Object[]{
               instance,
               new NameTagSubmit(
                  shadowMatrix, submit.x(), submit.y(), submit.text(), submit.lightCoords(), color, submit.backgroundColor(), submit.distanceToCameraSq()
               )
            }
         );
      }
   }

   @Inject(method = "clear", at = @At("HEAD"))
   private void clearOutlineSubmits(CallbackInfo ci) {
      this.figura$outlineSubmits.clear();
   }

   @Override
   public List<NameTagSubmit> getOutlineSubmits() {
      return this.figura$outlineSubmits;
   }
}
