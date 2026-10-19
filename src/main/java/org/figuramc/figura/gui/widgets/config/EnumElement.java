package org.figuramc.figura.gui.widgets.config;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.ui.UIHelper;

public class EnumElement extends AbstractConfigElement {
   private final List<Component> names;
   private final ParentedButton button;
   private ContextMenu context;

   public EnumElement(int width, ConfigType.EnumConfig config, ConfigList parentList, CategoryWidget parentCategory) {
      super(width, config, parentList, parentCategory);
      this.names = config.enumList;
      int selectedIndex = (Integer)this.config.tempValue % this.names.size();
      this.children
         .add(
            0,
            this.button = new ParentedButton(0, 0, 90, 20, this.names.get(selectedIndex), this, button -> {
               this.context.setVisible(!this.context.isVisible());
               if (this.context.isVisible()) {
                  this.updateContextText();
                  UIHelper.setContext(this.context);
               }
            }) {
               @Override
               protected void renderText(GuiGraphicsExtractor gui, float delta) {
                  Font font = Minecraft.getInstance().font;
                  Component arrow = EnumElement.this.context.isVisible() ? UIHelper.DOWN_ARROW : UIHelper.UP_ARROW;
                  int arrowWidth = font.width(arrow);
                  Component message = this.getMessage();
                  int textWidth = font.width(message);
                  int color = UIHelper.adjustColor(this.getTextColor());
                  UIHelper.renderCenteredScrollingText(
                     gui,
                     message,
                     this.getX() + 1,
                     this.getY(),
                     this.getWidth() - (textWidth <= this.getWidth() - arrowWidth - 9 ? 0 : arrowWidth + 1) - 2,
                     this.getHeight(),
                     color
                  );
                  gui.text(font, arrow, this.getX() + this.getWidth() - arrowWidth - 3, (int)(this.getY() + this.getHeight() / 2.0F - 9.0F / 2.0F), color);
               }

               @Override
               public void setHovered(boolean hovered) {
                  if (!hovered && UIHelper.getContext() == EnumElement.this.context && EnumElement.this.context.isVisible()) {
                     hovered = true;
                  }

                  super.setHovered(hovered);
               }
            }
         );
      this.button.setActive(FiguraMod.debugModeEnabled() || !config.disabled);
      if (config.enumTooltip != null) {
         this.button.setTooltip(config.enumTooltip.get(selectedIndex));
      }

      this.context = new ContextMenu(this.button, this.button.getWidth());

      for (int i = 0; i < this.names.size(); i++) {
         int finalI = i;
         Component tooltip = config.enumTooltip != null ? config.enumTooltip.get(i) : null;
         this.context.addAction(this.names.get(i), tooltip, button1 -> config.tempValue = finalI);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         this.resetButton.setActive(!this.isDefault());
         int selectedIndex = (Integer)this.config.tempValue % this.names.size();
         Component text = this.names.get(selectedIndex);
         if (this.isChanged()) {
            text = text.copy().setStyle(FiguraMod.getAccentColor());
         }

         this.button.setMessage(text);
         List<Component> tooltip = ((ConfigType.EnumConfig)this.config).enumTooltip;
         if (tooltip != null) {
            this.button.setTooltip(tooltip.get(selectedIndex));
         }

         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.button.setX(x + this.getWidth() - 154);
      this.context.setX(this.button.getX() + this.button.getWidth() / 2 - this.context.getWidth() / 2);
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.button.setY(y);
      this.context.setY(this.button.getY() + 20);
   }

   private void updateContextText() {
      List<? extends AbstractWidget> entries = this.context.getEntries();

      for (int i = 0; i < this.names.size(); i++) {
         Component text = this.names.get(i);
         if (i == (Integer)this.config.tempValue % this.names.size()) {
            text = Component.empty().setStyle(FiguraMod.getAccentColor()).withStyle(ChatFormatting.UNDERLINE).append(text);
         }

         entries.get(i).setMessage(text);
      }
   }
}
