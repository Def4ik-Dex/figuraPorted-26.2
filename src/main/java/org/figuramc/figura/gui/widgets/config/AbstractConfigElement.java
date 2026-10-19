package org.figuramc.figura.gui.widgets.config;

import java.util.Locale;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.ui.UIHelper;

public abstract class AbstractConfigElement extends AbstractContainerElement {
   protected final ConfigType<?> config;
   protected final ConfigList parentList;
   protected final CategoryWidget parentCategory;
   protected Button resetButton;
   protected Object initValue;
   private String filter = "";

   public AbstractConfigElement(int width, ConfigType<?> config, ConfigList parentList, CategoryWidget parentCategory) {
      super(0, 0, width, 20);
      this.config = config;
      this.parentList = parentList;
      this.parentCategory = parentCategory;
      this.initValue = config.value;
      this.children.add(this.resetButton = new ParentedButton(0, 0, 60, 20, Component.translatable("controls.reset"), this, button -> config.resetTemp()));
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         Font font = Minecraft.getInstance().font;
         int textY = this.getY() + this.getHeight() / 2 - 9 / 2;
         this.setHovered(this.isMouseOver(mouseX, mouseY));
         if (this.isHovered()) {
            gui.text(font, HOVERED_ARROW, (int)(this.getX() + 8 - font.width(HOVERED_ARROW) / 2.0F), textY, UIHelper.adjustColor(16777215));
         }

         this.renderTitle(gui, font, textY);
         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }

   public void renderTitle(GuiGraphicsExtractor gui, Font font, int y) {
      gui.text(
              font, this.config.name, this.getX() + 16, y, UIHelper.adjustColor((this.config.disabled ? TextColor.DARK_GRAY : TextColor.WHITE).getValue())
      );
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      boolean over = this.parentList.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      if (over && mouseX < this.getX() + this.getWidth() - 158) {
         UIHelper.setTooltip(this.getTooltip());
      }

      return over;
   }

   public MutableComponent getTooltip() {
      return this.config.tooltip.copy();
   }

   public boolean isDefault() {
      return this.config.isDefault();
   }

   public boolean isChanged() {
      return !Objects.equals(this.config.tempValue, this.initValue);
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.resetButton.setX(x + this.getWidth() - 60);
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.resetButton.setY(y);
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible && this.parentCategory.isShowingChildren() && this.matchesFilter());
   }

   public void updateFilter(String query) {
      this.filter = query;
   }

   public boolean matchesFilter() {
      return this.config.name.getString().toLowerCase(Locale.US).contains(this.filter)
         || this.config.tooltip.getString().toLowerCase(Locale.US).contains(this.filter);
   }
}
