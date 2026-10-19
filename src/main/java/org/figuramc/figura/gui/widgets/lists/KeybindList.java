package org.figuramc.figura.gui.widgets.lists;

import com.mojang.blaze3d.platform.InputConstants.Key;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.KeybindWidgetHelper;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class KeybindList extends AbstractList {
   private final List<KeybindList.KeybindElement> keybinds = new ArrayList<>();
   private final Avatar owner;
   private final Button resetAllButton;
   private FiguraKeybind focusedKeybind;

   public KeybindList(int x, int y, int width, int height, Avatar owner, Button resetAllButton) {
      super(x, y, width, height);
      this.owner = owner;
      this.resetAllButton = resetAllButton;
      this.updateList();
      Label noOwner;
      this.children
         .add(
            noOwner = new Label(
               FiguraText.of("gui.error.no_avatar").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0
            )
         );
      Label noKeys;
      this.children
         .add(
            noKeys = new Label(
               FiguraText.of("gui.error.no_keybinds").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0
            )
         );
      noOwner.centerVertically = noKeys.centerVertically = true;
      noOwner.setVisible(owner == null);
      noKeys.setVisible(!noOwner.isVisible() && this.keybinds.isEmpty());
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      UIHelper.blitSliced(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), UIHelper.OUTLINE_FILL);
      this.enableScissors(gui);
      if (!this.keybinds.isEmpty()) {
         this.updateEntries();
      }

      super.extractRenderState(gui, mouseX, mouseY, delta);
      gui.disableScissor();
   }

   private void updateEntries() {
      int totalHeight = -4;

      for (KeybindList.KeybindElement keybind : this.keybinds) {
         totalHeight += keybind.getHeight() + 8;
      }

      int entryHeight = this.keybinds.isEmpty() ? 0 : totalHeight / this.keybinds.size();
      this.scrollBar.setVisible(totalHeight > this.getHeight());
      this.scrollBar.setScrollRatio(entryHeight, totalHeight - this.getHeight());
      int xOffset = this.scrollBar.isVisible() ? 4 : 11;
      int yOffset = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -4.0, totalHeight - this.getHeight())) : 4;

      for (KeybindList.KeybindElement keybind : this.keybinds) {
         keybind.setX(this.getX() + xOffset);
         keybind.setY(this.getY() + yOffset);
         yOffset += keybind.getHeight() + 8;
      }
   }

   private void updateList() {
      this.keybinds.forEach(this.children::remove);
      if (this.owner != null && this.owner.luaRuntime != null) {
         for (FiguraKeybind keybind : this.owner.luaRuntime.keybinds.keyBindings) {
            KeybindList.KeybindElement element = new KeybindList.KeybindElement(this.getWidth() - 22, keybind, this);
            this.keybinds.add(element);
            this.children.add(element);
         }

         this.updateBindings();
      }
   }

   public boolean updateKey(Key key) {
      if (this.focusedKeybind == null) {
         return false;
      }

      this.focusedKeybind.setKey(key);
      this.focusedKeybind = null;
      FiguraMod.processingKeybind = false;
      this.updateBindings();
      return true;
   }

   public void updateBindings() {
      boolean active = false;

      for (KeybindList.KeybindElement keybind : this.keybinds) {
         keybind.updateText();
         if (!active && !keybind.keybind.isDefault()) {
            active = true;
         }
      }

      this.resetAllButton.setActive(active);
   }

   private static class KeybindElement extends AbstractContainerElement {
      private final KeybindWidgetHelper helper = new KeybindWidgetHelper();
      private final FiguraKeybind keybind;
      private final KeybindList parent;
      private final Button resetButton;
      private final Button keybindButton;

      public KeybindElement(int width, FiguraKeybind keybind, KeybindList parent) {
         super(0, 0, width, 20);
         this.keybind = keybind;
         this.parent = parent;
         this.children.add(0, this.keybindButton = new ParentedButton(0, 0, 90, 20, keybind.getTranslatedKeyMessage(), this, button -> {
            parent.focusedKeybind = keybind;
            FiguraMod.processingKeybind = true;
            this.updateText();
         }));
         this.children.add(this.resetButton = new ParentedButton(0, 0, 60, 20, Component.translatable("controls.reset"), this, button -> {
            keybind.resetDefaultKey();
            parent.updateBindings();
         }));
      }

      @Override
      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         if (this.isVisible()) {
            this.helper.renderConflictBars(gui, this.keybindButton.getX() - 8, this.keybindButton.getY() + 2, 4, 16);
            Font font = Minecraft.getInstance().font;
            int textY = this.getY() + this.getHeight() / 2 - 9 / 2;
            this.setHovered(this.isMouseOver(mouseX, mouseY));
            if (this.isHovered()) {
               gui.text(font, HOVERED_ARROW, this.getX() + 4, textY, UIHelper.adjustColor(16777215));
               if (this.keybindButton.isHoveredOrFocused()) {
                  this.helper.renderTooltip();
               }
            }

            gui.text(font, this.keybind.getName(), this.getX() + 16, textY, UIHelper.adjustColor(16777215));
            super.extractRenderState(gui, mouseX, mouseY, delta);
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }

      @Override
      public void setX(int x) {
         super.setX(x);
         this.resetButton.setX(x + this.getWidth() - 60);
         this.keybindButton.setX(x + this.getWidth() - 154);
      }

      @Override
      public void setY(int y) {
         super.setY(y);
         this.resetButton.setY(y);
         this.keybindButton.setY(y);
      }

      public void updateText() {
         List<FiguraKeybind> temp = new ArrayList<>();

         for (KeybindList.KeybindElement keybind : this.parent.keybinds) {
            temp.add(keybind.keybind);
         }

         this.helper.setTooltip(this.keybind, temp);
         boolean isDefault = this.keybind.isDefault();
         this.resetButton.setActive(!isDefault);
         boolean selected = this.parent.focusedKeybind == this.keybind;
         Component text = this.helper.getText(isDefault, selected, this.keybind.getTranslatedKeyMessage());
         this.keybindButton.setMessage(text);
      }
   }
}
