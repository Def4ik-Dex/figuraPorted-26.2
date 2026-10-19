package org.figuramc.figura.utils;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class FiguraText extends TranslatableContents {
   public FiguraText() {
      super("figura", "Figura", NO_ARGS);
   }

   public FiguraText(String string) {
      super("figura." + string, null, NO_ARGS);
   }

   public FiguraText(String string, Object... args) {
      super("figura." + string, null, args);
   }

   public static MutableComponent of() {
      return MutableComponent.create(new FiguraText());
   }

   public static MutableComponent of(String string) {
      return MutableComponent.create(new FiguraText(string));
   }

   public static MutableComponent of(String string, Object... args) {
      return MutableComponent.create(new FiguraText(string, args));
   }
}
