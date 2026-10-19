package org.figuramc.figura.mixin.compat;

import com.geckolib.animatable.GeoItem;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.GeoArmorRenderer.RenderData;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GeckolibGeoArmorAccessor;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = GeoArmorRenderer.class, remap = false)
public abstract class GeckolibGeoArmorRendererMixin<T extends Item & GeoItem> implements GeckolibGeoArmorAccessor {
   @Unique
   private Avatar figura$avatar;

   @Inject(
      method = "captureDefaultRenderState",
      at = @At("HEAD")
   )
   private <R extends HumanoidRenderState & GeoRenderState> void figura$prepAvatar(
      T animatable, RenderData renderData, R renderState, float partialTick, CallbackInfo ci
   ) {
      Entity entity = renderData.entity();
      if (entity != null) {
         this.figura$avatar = AvatarManager.getAvatar(entity);
      } else {
         this.figura$avatar = null;
      }
   }

   @Unique
   @Override
   public Avatar figura$getAvatar() {
      return this.figura$avatar;
   }

   @Accessor("entityRenderTranslations")
   @Override
   public abstract void figura$setEntityRenderTranslations(Matrix4f var1);

   @Accessor("modelRenderTranslations")
   @Override
   public abstract void figura$setModelRenderTranslations(Matrix4f var1);

   @Accessor("scaleWidth")
   @Override
   public abstract float figura$getScaleWidth();

   @Accessor("scaleHeight")
   @Override
   public abstract float figura$getScaleHeight();

   @Accessor("headBone")
   @Override
   public abstract GeoBone figura$getHeadBone();

   @Accessor("leftLegBone")
   @Override
   public abstract GeoBone figura$getLeftLegBone();

   @Accessor("rightLegBone")
   @Override
   public abstract GeoBone figura$getRightLegBone();

   @Accessor("leftArmBone")
   @Override
   public abstract GeoBone figura$getLeftArmBone();

   @Accessor("rightArmBone")
   @Override
   public abstract GeoBone figura$getRightArmBone();

   @Accessor("bodyBone")
   @Override
   public abstract GeoBone figura$getBodyBone();

   @Accessor("leftBootBone")
   @Override
   public abstract GeoBone figura$getLeftBootBone();

   @Accessor("rightBootBone")
   @Override
   public abstract GeoBone figura$getRightBootBone();
}
