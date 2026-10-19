package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MissingTextureAtlasSprite.class)
public interface MissingTextureAtlasSpriteAccessor {
   @Invoker("generateMissingImage")
   static NativeImage generateImage(int width, int height) {
      throw new AssertionError();
   }
}
