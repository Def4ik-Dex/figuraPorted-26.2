package org.figuramc.figura.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.ducks.SuggestionsListAccessor;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuggestionsList.class)
public class SuggestionsListMixin implements SuggestionsListAccessor {
   @Shadow
   @Final
   private Rect2i rect;
   @Unique
   private boolean figuraList;
   @Unique
   private static GuiGraphicsExtractor gui;

   @Inject(at = @At("HEAD"), method = "extractRenderState")
   private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
      gui = graphics;
   }

   @ModifyArg(
      method = "extractRenderState",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"),
      index = 2
   )
   private int voidTextDraw(Font font, String text, int x, int y, int color) {
      if (this.figuraList && gui != null) {
         Component emoji = Emojis.applyEmojis(Component.literal(text));
         if (emoji.getString().equals(text)) {
            return x;
         }

         gui.text(font, emoji, x + 4 - font.width(emoji) / 2, y, color);
         return x + 8 + font.width(" ");
      } else {
         return x;
      }
   }

   @Override
   public void figura$setFiguraList(boolean bool) {
      this.figuraList = bool;
      if (bool) {
         this.rect.setWidth(this.rect.getWidth() + 8 + Minecraft.getInstance().font.width(" "));
      }
   }
}
