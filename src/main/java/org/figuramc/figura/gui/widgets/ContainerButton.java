package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.figuramc.figura.gui.widgets.lists.AbstractList;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class ContainerButton extends SwitchButton {
   private final AbstractList parent;

   public ContainerButton(AbstractList parent, int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
      super(x, y, width, height, text, tooltip, pressAction);
      this.parent = parent;
   }

   @Override
   protected void renderText(GuiGraphicsExtractor gui, float delta) {
      Font font = Minecraft.getInstance().font;
      int color = UIHelper.adjustColor(this.getTextColor());
      Component arrow = this.toggled ? UIHelper.DOWN_ARROW : UIHelper.UP_ARROW;
      int arrowWidth = font.width(arrow);
      Component message = TextUtils.trimToWidthEllipsis(
         font, this.getMessage(), this.getWidth() - arrowWidth - 6, TextUtils.ELLIPSIS.copy().withStyle(this.getMessage().getStyle())
      );
      gui.text(font, message, this.getX() + arrowWidth + 6, (int)(this.getY() + this.getHeight() / 2.0F - 9.0F / 2.0F), color);
      gui.text(font, arrow, this.getX() + 3, (int)(this.getY() + this.getHeight() / 2.0F - 9.0F / 2.0F), color);
      if (message != this.getMessage()) {
         this.setTooltip(this.getMessage());
      }
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
   }

   @Override
   protected int getTextColor() {
      return !this.isToggled() ? TextColor.DARK_GRAY.getValue() : super.getTextColor();
   }
}
