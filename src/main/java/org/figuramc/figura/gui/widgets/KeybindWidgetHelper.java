package org.figuramc.figura.gui.widgets;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.scores.TeamColor;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class KeybindWidgetHelper {
   private Component tooltip;
   private boolean vanillaConflict;
   private boolean avatarConflict;

   public void renderConflictBars(GuiGraphicsExtractor gui, int x, int y, int width, int height) {
      if (this.vanillaConflict || this.avatarConflict) {
         if (this.avatarConflict) {
            gui.fill(x, y, x + width, y + height, TextColor.YELLOW.getValue() | 0xFF000000);
            x -= width + 4;
         }

         if (this.vanillaConflict) {
            gui.fill(x, y, x + width, y + height, TextColor.RED.getValue() | 0xFF000000);
         }
      }
   }

   public void renderTooltip() {
      if (this.tooltip != null) {
         UIHelper.setTooltip(this.tooltip);
      }
   }

   public void setTooltip(FiguraKeybind keybind, List<FiguraKeybind> keyBindings) {
      MutableComponent text = Component.empty();
      Component avatar = this.checkForAvatarConflicts(keybind, keyBindings);
      boolean hasAvatarConflict = avatar != null && !avatar.getString().isBlank();
      if (hasAvatarConflict) {
         text.append(avatar);
      }

      Component vanilla = this.checkForVanillaConflicts(keybind);
      if (vanilla != null && !vanilla.getString().isBlank()) {
         if (hasAvatarConflict) {
            text.append("\n");
         }

         text.append(vanilla);
      }

      this.setTooltipTail(text);
   }

   public void setTooltip(KeyMapping keybind) {
      MutableComponent text = Component.empty();
      Component vanilla = this.checkForVanillaConflicts(keybind);
      if (vanilla != null && !vanilla.getString().isBlank()) {
         text.append(vanilla);
      }

      this.setTooltipTail(text);
   }

   private void setTooltipTail(Component text) {
      if (!this.vanillaConflict && !this.avatarConflict) {
         this.tooltip = null;
      } else {
         this.tooltip = FiguraText.of("gui.duplicate_keybind", text);
      }
   }

   public Component getText(boolean isDefault, boolean isSelected, Component initialMessage) {
      MutableComponent message = initialMessage.copy();
      if (!isDefault && !isSelected) {
         message.withStyle(FiguraMod.getAccentColor());
      } else {
         message.withStyle(ChatFormatting.WHITE);
      }

      if (isSelected) {
         message.withStyle(ChatFormatting.UNDERLINE);
      }

      if (this.avatarConflict || this.vanillaConflict) {
         MutableComponent left = Component.literal("[ ").withStyle(this.vanillaConflict ? ChatFormatting.RED : ChatFormatting.YELLOW);
         MutableComponent right = Component.literal(" ]").withStyle(this.avatarConflict ? ChatFormatting.YELLOW : ChatFormatting.RED);
         message = left.append(message).append(right);
      }

      if (isSelected) {
         message = Component.literal("> ").append(message).append(" <").withStyle(FiguraMod.getAccentColor());
      }

      return message;
   }

   public Component checkForAvatarConflicts(FiguraKeybind keybind, List<FiguraKeybind> keyBindings) {
      this.avatarConflict = false;
      int id = keybind.getID();
      if (id == -1) {
         return null;
      }

      MutableComponent message = Component.empty();

      for (FiguraKeybind keyBinding : keyBindings) {
         if (keyBinding != keybind && keyBinding.getID() == id) {
            this.avatarConflict = true;
            message.append(Component.literal("\n• ").withStyle(ChatFormatting.YELLOW).append(keyBinding.getName()));
         }
      }

      return message;
   }

   public Component checkForVanillaConflicts(FiguraKeybind keybind) {
      this.vanillaConflict = false;
      if (keybind.getID() == -1) {
         return null;
      }

      String keyName = keybind.getKey();
      MutableComponent message = Component.empty();

      for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
         if (key.saveString().equals(keyName)) {
            this.vanillaConflict = true;
            message.append(
               Component.literal("\n• ")
                  .withStyle(ChatFormatting.RED)
                  .append(key.getCategory().label())
                  .append(": ")
                  .append(Component.translatable(key.getName()))
            );
         }
      }

      return message;
   }

   public Component checkForVanillaConflicts(KeyMapping keybind) {
      this.vanillaConflict = false;
      if (keybind.isUnbound()) {
         return null;
      }

      String keyName = keybind.saveString();
      MutableComponent message = Component.empty();

      for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
         if (key != keybind && key.saveString().equals(keyName)) {
            this.vanillaConflict = true;
            message.append(
               Component.literal("\n• ")
                  .withStyle(ChatFormatting.RED)
                  .append(key.getCategory().label())
                  .append(": ")
                  .append(Component.translatable(key.getName()))
            );
         }
      }

      return message;
   }
}
