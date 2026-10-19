package org.figuramc.figura.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import org.figuramc.figura.ducks.NativeImageExtension;
import org.lwjgl.stb.STBImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin implements NativeImageExtension {
   @Shadow
   protected abstract boolean writeToChannel(WritableByteChannel var1) throws IOException;

   @Shadow
   protected abstract int getPixelABGR(int var1, int var2);

   @Shadow
   protected abstract void setPixelABGR(int var1, int var2, int var3);

   @Unique
   @Override
   public byte[] figura$asByteArray() throws IOException {
      try (
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
      ) {
         if (!this.writeToChannel(writableByteChannel)) {
            throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
         } else {
            return byteArrayOutputStream.toByteArray();
         }
      }
   }

   @Override
   public int figura$getPixelABGR(int i, int j) {
      return this.getPixelABGR(i, j);
   }

   @Override
   public void figura$setPixelABGR(int i, int j, int color) {
      this.setPixelABGR(i, j, color);
   }
}
