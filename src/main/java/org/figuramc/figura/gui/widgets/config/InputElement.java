package org.figuramc.figura.gui.widgets.config;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.InputType;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class InputElement extends AbstractConfigElement {
   private final TextField textField;
   private final InputType inputType;

   public InputElement(int width, ConfigType.InputConfig<?> config, ConfigList parentList, CategoryWidget parentCategory) {
      super(width, config, parentList, parentCategory);
      this.inputType = config.inputType;
      this.textField = new InputElement.InputField(0, 0, 90, 20, this.inputType.hint, this, text -> {
         if (this.inputType.validator.test(text)) {
            config.setTempValue(text);
         }
      });
      this.updateTextFieldText(this.formatText(config.tempValue));
      this.textField.getField().moveCursorToStart(IOUtils.hasShiftDown());
      this.textField.setEnabled(FiguraMod.debugModeEnabled() || !config.disabled);
      this.children.add(0, this.textField);
      this.children.remove(this.resetButton);
      this.children
         .add(this.resetButton = new ParentedButton(this.getX() + width - 60, this.getY(), 60, 20, Component.translatable("controls.reset"), this, button -> {
            config.resetTemp();
            this.updateTextFieldText(this.formatText(config.tempValue));
         }));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         this.resetButton.setActive(!this.isDefault());
         int color = 16777215;
         String text = this.textField.getField().getValue();
         if (!this.inputType.validator.test(text)) {
            color = 16733525;
         } else if (!text.equals(this.formatText(this.initValue))) {
            TextColor textColor = FiguraMod.getAccentColor().getColor();
            color = textColor == null ? ColorUtils.Colors.AWESOME_BLUE.hex : textColor.getValue();
         }

         this.textField.setColor(color);
         this.textField.setBorderColour(-16777216 + color);
         super.extractRenderState(gui, mouseX, mouseY, delta);
         if (this.inputType == InputType.HEX_COLOR) {
            int x = this.getX() + this.getWidth() - 178;
            int y = this.getY();
            if (this.getTextField().isFocused()) {
               UIHelper.fillRounded(gui, x, y, 20, 20, this.getTextField().getBorderColour());
            } else {
               UIHelper.blitSliced(gui, x, y, 20, 20, UIHelper.OUTLINE);
            }

            UIHelper.fillRounded(gui, x + 1, y + 1, 18, 18, (Integer)this.config.tempValue + -16777216);
         }
      }
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.textField.setX(x + this.getWidth() - 154);
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.textField.setY(y);
   }

   @Override
   public boolean isDefault() {
      return this.textField.getField().getValue().equals(this.formatText(this.config.defaultValue));
   }

   @Override
   public boolean isChanged() {
      return !this.textField.getField().getValue().equals(this.formatText(this.initValue));
   }

   public TextField getTextField() {
      return this.textField;
   }

   public void updateTextFieldText(String text) {
      this.textField.getField().setValue(text);
   }

   private String formatText(Object configValue) {
      return this.inputType == InputType.HEX_COLOR ? String.format("#%06X", (Integer)configValue) : configValue.toString();
   }

   private static class InputField extends TextField {
      private final InputElement parent;

      public InputField(int x, int y, int width, int height, TextField.HintType hint, InputElement parent, Consumer<String> changedListener) {
         super(x, y, width, height, hint, changedListener);
         this.parent = parent;
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isHovered() && super.isMouseOver(mouseX, mouseY);
      }
   }
}
