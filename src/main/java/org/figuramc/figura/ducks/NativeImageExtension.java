package org.figuramc.figura.ducks;

import java.io.IOException;

public interface NativeImageExtension {
   byte[] figura$asByteArray() throws IOException;

   int figura$getPixelABGR(int var1, int var2);

   void figura$setPixelABGR(int var1, int var2, int var3);
}
