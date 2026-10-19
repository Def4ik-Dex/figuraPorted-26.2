package org.figuramc.figura.mixin.gui;

import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Line.class)
public class GuiMessageLineMixin implements GuiMessageAccessor {
   @Unique
   private int color = 0;

   @Override
   public void figura$setColor(int color) {
      this.color = color;
   }

   @Override
   public int figura$getColor() {
      return this.color;
   }
}
