package org.figuramc.figura.mixin.render;

import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TextureAtlas.class, priority = 900)
public interface TextureAtlasAccessor {
   @Accessor("texturesByName")
   Map<Identifier, TextureAtlasSprite> figura$getTexturesByName();

   @Invoker("getWidth")
   int figura$getWidth();

   @Invoker("getHeight")
   int figura$getHeight();
}
