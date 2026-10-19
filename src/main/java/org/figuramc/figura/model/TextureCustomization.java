package org.figuramc.figura.model;

import com.mojang.blaze3d.buffers.GpuBuffer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBufferSlice.MappedView;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.mixin.render.MissingTextureAtlasSpriteAccessor;
import org.figuramc.figura.mixin.render.TextureAtlasAccessor;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.luaj.vm2.LuaError;

public class TextureCustomization {
   private final FiguraTextureSet.OverrideType first;
   private final Object second;

   public TextureCustomization(FiguraTextureSet.OverrideType first, Object second) {
      this.first = first;
      this.second = second;
   }

   public FiguraTextureSet.OverrideType getOverrideType() {
      return this.first;
   }

   public Object getValue() {
      return this.second;
   }

   public FiguraTexture getTexture(Avatar avatar, FiguraTextureSet textureSet) {
      if (avatar.render == null) {
         return null;
      }

      Identifier resourceLocation = textureSet.getOverrideTexture(avatar.owner, this);
      String name = resourceLocation.toString();
      if (avatar.renderer.customTextures.containsKey(name)) {
         return avatar.renderer.customTextures.get(name);
      }

      try {
         AbstractTexture atlas = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation);
         if (!(atlas instanceof TextureAtlas)) {
            throw new LuaError("Resource is not a texture atlas: " + resourceLocation);
         }

         GpuTexture atlasGpuTexture = atlas.getTexture();
         TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor)atlas;
         NativeImage nativeImage = new NativeImage(atlasAccessor.figura$getWidth(), atlasAccessor.figura$getHeight(), false);
         int width = atlasAccessor.figura$getWidth();
         int height = atlasAccessor.figura$getHeight();
         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
         GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Atlas Read Buffer", 9, width * height * atlasGpuTexture.getFormat().blockSize());
         encoder.copyTextureToBuffer(atlasGpuTexture, gpuBuffer, 0L, () -> {

             GpuBufferSlice slice = new GpuBufferSlice(gpuBuffer, 0L, gpuBuffer.size()); 
             MappedView readView = slice.map(true, false); 

            try {
               for (int k = 0; k < height; k++) {
                  for (int l = 0; l < width; l++) {
                     int m = readView.data().getInt((l + k * width) * atlasGpuTexture.getFormat().blockSize());
                     nativeImage.setPixelABGR(l, height - k - 1, m | 0xFF000000);
                  }
               }
            } catch (Throwable t$) {
               if (readView != null) {
                  try {
                     readView.close(); 
                  } catch (Throwable x2) {
                     t$.addSuppressed(x2);
                  }
               }

               throw t$;
            }

            if (readView != null) {
               readView.close();
            }

            gpuBuffer.close();
         }, 0);
         return avatar.registerTexture(name, nativeImage, false);
      } catch (Exception var14) {
         try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
            NativeImage image = resource.isPresent() ? NativeImage.read(resource.get().open()) : MissingTextureAtlasSpriteAccessor.generateImage(16, 16);
            return avatar.registerTexture(name, image, false);
         } catch (Exception e) {
            throw new LuaError(e.getMessage());
         }
      }
   }
}
