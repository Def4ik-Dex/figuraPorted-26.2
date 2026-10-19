package org.figuramc.figura.ducks;

import net.minecraft.resources.Identifier;

public interface GameEffects {
   Identifier[] EFFECTS = new Identifier[]{
      Identifier.parse("shaders/post/blur.json"),
      Identifier.parse("shaders/post/entity_outline.json"),
      Identifier.parse("shaders/post/invert.json"),
      Identifier.parse("shaders/post/blur.json"),
      Identifier.parse("shaders/post/creeper.json"),
      Identifier.parse("shaders/post/spider.json")
   };

   static Identifier[] getEffects() {
      return EFFECTS;
   }
}
