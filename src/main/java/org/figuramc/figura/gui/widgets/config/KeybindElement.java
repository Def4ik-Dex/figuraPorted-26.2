package org.figuramc.figura.gui.widgets.config;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigKeyBind;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.KeybindWidgetHelper;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;

public class KeybindElement extends AbstractConfigElement {
   private final KeybindWidgetHelper helper = new KeybindWidgetHelper();
   private final KeyMapping binding;
   private final ParentedButton button;

   public KeybindElement(int width, ConfigType.KeybindConfig config, ConfigList parentList, CategoryWidget parentCategory) {
      super(width, config, parentList, parentCategory);
      this.binding = config.keyBind;
      this.children.add(0, this.button = new ParentedButton(0, 0, 90, 20, this.binding.getTranslatedKeyMessage(), this, button -> {
         parentList.focusedBinding = this.binding;
         FiguraMod.processingKeybind = true;
         this.updateText();
      }));
      this.button.setActive(FiguraMod.debugModeEnabled() || !config.disabled);
      this.children.remove(this.resetButton);
      this.children
         .add(this.resetButton = new ParentedButton(this.getX() + width - 60, this.getY(), 60, 20, Component.translatable("controls.reset"), this, button -> {
            this.binding.setKey(this.binding.getDefaultKey());
            ((ConfigKeyBind)this.binding).saveConfigChanges();
            parentList.updateKeybinds();
         }));
      this.updateText();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         this.helper.renderConflictBars(gui, this.button.getX() - 8, this.button.getY() + 2, 4, 16);
         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      boolean bool = super.isMouseOver(mouseX, mouseY);
      if (bool && this.button.isMouseOver(mouseX, mouseY)) {
         this.helper.renderTooltip();
      }

      return bool;
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.button.setX(x + this.getWidth() - 154);
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.button.setY(y);
   }

   @Override
   public boolean isDefault() {
      return this.binding.isDefault();
   }

   @Override
   public boolean isChanged() {
      return false;
   }

   public void updateText() {
      this.helper.setTooltip(this.binding);
      boolean isDefault = this.isDefault();
      this.resetButton.setActive(!isDefault);
      boolean selected = this.parentList.focusedBinding == this.binding;
      Component text = this.helper.getText(isDefault, selected, this.binding.getTranslatedKeyMessage());
      this.button.setMessage(text);
   }
}
