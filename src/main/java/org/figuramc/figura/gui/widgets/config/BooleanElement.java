package org.figuramc.figura.gui.widgets.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.SwitchButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;

public class BooleanElement extends AbstractConfigElement {
   private final ParentedButton button;

   public BooleanElement(int width, ConfigType.BoolConfig config, ConfigList parentList, CategoryWidget parentCategory) {
      super(width, config, parentList, parentCategory);
      this.children
         .add(
            0,
            this.button = new ParentedButton(
               0, 0, 90, 20, config.tempValue ? SwitchButton.ON : SwitchButton.OFF, this, button -> config.tempValue = !config.tempValue
            )
         );
      this.button.setActive(FiguraMod.debugModeEnabled() || !config.disabled);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         this.resetButton.setActive(!this.isDefault());
         Component text = (Boolean)this.config.tempValue ? SwitchButton.ON : SwitchButton.OFF;
         if (this.isChanged()) {
            text = text.copy().setStyle(FiguraMod.getAccentColor());
         }

         this.button.setMessage(text);
         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
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
}
