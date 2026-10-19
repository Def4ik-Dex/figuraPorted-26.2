package org.figuramc.figura.mixin.gui;

import net.minecraft.client.renderer.PlayerSkinRenderCache.RenderInfo;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.PlayerHeadRenderInfoExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderInfo.class)
public class PlayerHeadRenderInfoMixin implements PlayerHeadRenderInfoExtension {
   @Unique
   Avatar figura$avatar = null;

   @Override
   public Avatar figura$getAvatar() {
      return this.figura$avatar;
   }

   @Override
   public void figura$setAvatar(Avatar avatar) {
      this.figura$avatar = avatar;
   }
}
