package org.figuramc.figura.utils;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class FiguraIdentifier {
   private FiguraIdentifier() {
   }

   public static Identifier of(String path) {
      return Identifier.fromNamespaceAndPath("figura", path);
   }

   public static String formatPath(String path) {
      return Util.sanitizeName(path, Identifier::validPathChar);
   }
}
