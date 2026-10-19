package org.figuramc.figura.ducks;

import com.geckolib.cache.model.GeoBone;
import org.figuramc.figura.avatar.Avatar;
import org.joml.Matrix4f;

public interface GeckolibGeoArmorAccessor {
   Avatar figura$getAvatar();

   void figura$setEntityRenderTranslations(Matrix4f var1);

   void figura$setModelRenderTranslations(Matrix4f var1);

   float figura$getScaleWidth();

   float figura$getScaleHeight();

   GeoBone figura$getHeadBone();

   GeoBone figura$getLeftLegBone();

   GeoBone figura$getRightLegBone();

   GeoBone figura$getLeftArmBone();

   GeoBone figura$getRightArmBone();

   GeoBone figura$getBodyBone();

   GeoBone figura$getLeftBootBone();

   GeoBone figura$getRightBootBone();
}
