package org.figuramc.figura.gui.widgets;

import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.ClickableTextHelper;
import org.figuramc.figura.utils.ui.UIHelper;

public class BackendMotdWidget extends AbstractTextAreaWidget {
   private final Font font;
   private final ClickableTextHelper textHelper;
   private int maxWidth;
   private boolean mouseDown = false;

   public BackendMotdWidget(int i, int j, int k, int l, Component text, Font font) {
      super(i, j, k, l, text, AbstractScrollArea.defaultSettings(9));
      this.font = font;
      this.textHelper = new ClickableTextHelper();
      this.maxWidth = this.getWidth() - this.totalInnerPadding();
   }

   public void setMessage(Component message) {
      super.setMessage(message);
      this.textHelper.setMessage(message);
   }

   public void setWidth(int value) {
      super.setWidth(value);
      int prevWidth = this.maxWidth;
      this.maxWidth = this.getWidth() - this.totalInnerPadding();
      if (this.maxWidth != prevWidth) {
         this.textHelper.markDirty();
      }
   }

   protected int getInnerHeight() {
      Objects.requireNonNull(this.font);
      return this.textHelper.lineCount() * 9;
   }

   protected double scrollRate() {
      Objects.requireNonNull(this.font);
      return 9.0;
   }

   protected void extractBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      UIHelper.blitSliced(
         graphics,
         this.getX() - this.innerPadding(),
         this.getY() - this.innerPadding(),
         this.getWidth() + this.totalInnerPadding(),
         this.getHeight() + this.totalInnerPadding(),
         UIHelper.OUTLINE_FILL
      );
   }

   protected void extractBackground(GuiGraphicsExtractor graphics) {
      UIHelper.blitSliced(
         graphics,
         this.getX() - this.innerPadding(),
         this.getY() - this.innerPadding(),
         this.getWidth() + this.totalInnerPadding(),
         this.getHeight() + this.totalInnerPadding(),
         UIHelper.OUTLINE_FILL
      );
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.visible) {
         if (!this.scrollable()) {
            this.extractBackground(graphics);
            this.extractContents(graphics, mouseX, mouseY, delta);
         } else {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
         }
      }
   }

   protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int xx = this.getX() + this.innerPadding();
      int yy = this.getY() + this.innerPadding();
      int scroll = (int)this.scrollAmount();
      this.textHelper.update(this.font, this.maxWidth);
      this.textHelper
         .visit(
            (text, style, x, y, textWidth, textHeight) -> graphics.text(
               this.font, Component.literal(text).setStyle(style), xx + x, yy + y, UIHelper.adjustColor(-1)
            )
         );
      if (this.withinContentAreaPoint(mouseX, mouseY)) {
         Component tooltip = this.textHelper.getHoverTooltip(xx, yy, mouseX, mouseY + scroll);
         if (tooltip != null) {
            UIHelper.setTooltip(tooltip);
         }

         if (this.mouseDown) {
            String link = this.textHelper.getClickLink(xx, yy, mouseX, mouseY + scroll);
            if (link != null) {
               UIHelper.openURL(link).run();
            }

            this.mouseDown = false;
         }
      }
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      this.mouseDown = super.mouseClicked(mouseButtonEvent, bl);
      return this.mouseDown;
   }

   protected void updateWidgetNarration(NarrationElementOutput builder) {
      builder.add(NarratedElementType.TITLE, this.getMessage());
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public boolean shouldRender() {
      return this.getScrollBarHeight() > 0 && this.height >= 48;
   }

   private int getScrollBarHeight() {
      return Mth.clamp((int)((float)(this.height * this.height) / this.contentHeight()), 32, this.height);
   }

   protected boolean withinContentAreaPoint(double d, double e) {
      return d >= this.getX() && d < this.getX() + this.width && e >= this.getY() && e < this.getY() + this.height;
   }
}
