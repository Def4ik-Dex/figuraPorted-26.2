package org.figuramc.figura.ducks;

import org.figuramc.figura.avatar.Avatar;

public interface CameraRenderStateExtension {
   void figura$setAvatar(Avatar var1);

   Avatar figura$getAvatar();

   boolean figura$isRenderingNameTag();

   void figura$setRenderingNameTag(boolean var1);
}
