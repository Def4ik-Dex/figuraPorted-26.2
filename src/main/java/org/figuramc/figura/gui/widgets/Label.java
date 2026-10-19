package org.figuramc.figura.gui.widgets;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class Label implements FiguraWidget, GuiEventListener, NarratableEntry {
   private final Font font;
   private Component rawText;
   private List<Component> formattedText;
   public TextUtils.Alignment alignment;
   public Integer outlineColor;
   public Integer backgroundColor;
   private Integer alpha;
   private int alphaPrecise = 255;
   public int maxWidth;
   public boolean wrap;
   private Style hovered;
   private int x;
   private int y;
   private int width;
   private int height;
   private float scale;
   private boolean visible = true;
   public boolean centerVertically;

   public Label(Object text, int x, int y, float scale, int maxWidth, boolean wrap, TextUtils.Alignment alignment, Integer outlineColor) {
      this.font = Minecraft.getInstance().font;
      this.rawText = (Component)(text instanceof Component c ? c : Component.literal(String.valueOf(text)));
      this.x = x;
      this.y = y;
      this.scale = scale;
      this.maxWidth = maxWidth;
      this.wrap = wrap;
      this.alignment = alignment;
      this.outlineColor = outlineColor;
      this.updateText();
   }

   public Label(Object text, int x, int y, int outlineColor) {
      this(text, x, y, 1.0F, -1, false, TextUtils.Alignment.LEFT, outlineColor);
   }

   public Label(Object text, int x, int y, TextUtils.Alignment alignment) {
      this(text, x, y, 1.0F, -1, false, alignment, null);
   }

   public Label(Object text, int x, int y, TextUtils.Alignment alignment, int outlineColor) {
      this(text, x, y, 1.0F, -1, false, alignment, outlineColor);
   }

   public Label(Object text, int x, int y, int maxWidth, boolean wrap, TextUtils.Alignment alignment) {
      this(text, x, y, 1.0F, maxWidth, wrap, alignment, null);
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      this.hovered = null;
      if (this.isVisible()) {
         this.renderBackground(gui);
         this.renderText(gui, mouseX, mouseY, delta);
      }
   }

   private void renderBackground(GuiGraphicsExtractor gui) {
      if (this.backgroundColor != null) {
         int x = this.getX();
         int y = this.getY();
         gui.fill(x, y, x + this.width, y + this.height, this.backgroundColor);
      }
   }

   private void renderText(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      Matrix3x2fStack pose = gui.pose();
      pose.pushMatrix();
      pose.translate(this.x, this.getY());
      pose.scale(this.scale, this.scale);
      if (this.alpha != null) {
         float lerpDelta = MathUtils.magicDelta(0.6F, delta);
         this.alphaPrecise = (int)Mth.lerp(lerpDelta, this.alphaPrecise, this.isMouseOver(mouseX, mouseY) ? 255.0F : this.alpha.intValue());
      }

      int y = 0;
      int height = 9;

      for (Component text : this.formattedText) {
         int x = -this.alignment.apply(this.font, text);
         int width = this.font.width(text);
         if (mouseX >= this.x + x * this.scale
            && mouseX < this.x + (x + width) * this.scale
            && mouseY >= this.y + y * this.scale
            && mouseY < this.y + (y + height) * this.scale) {
            int pos = (int)((mouseX - this.x - x * this.scale) / this.scale);
            this.hovered = TextUtils.componentStyleAtWidth(this.font, text, pos);
            ClickEvent event = this.hovered != null ? this.hovered.getClickEvent() : null;
            if (event != null) {
               text = TextUtils.replaceStyle(text, Style.EMPTY.withUnderlined(true), style -> event.equals(style.getClickEvent()));
            }

            UIHelper.setTooltip(this.hovered);
         }

         if (this.outlineColor != null) {
            UIHelper.renderOutlineText(gui, this.font, text, x, y, 16777215, this.outlineColor);
         } else {
            gui.text(this.font, text, x, y, UIHelper.adjustColor(16777215 + (this.alphaPrecise << 24)));
         }

         y += height;
      }

      pose.popMatrix();
   }

   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      if (this.hovered != null && Minecraft.getInstance().gui.screen() != null && this.hovered.getClickEvent() != null) {
         Screen.defaultHandleGameClickEvent(this.hovered.getClickEvent(), Minecraft.getInstance(), Minecraft.getInstance().gui.screen());
         return true;
      } else {
         return GuiEventListener.super.mouseClicked(mouseButtonEvent, bl);
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      if (!this.isVisible()) {
         return false;
      }

      int x = this.getX();
      int y = this.getY();
      return mouseX >= x && mouseX < x + this.width && mouseY >= y && mouseY < y + this.height ? true : GuiEventListener.super.isMouseOver(mouseX, mouseY);
   }

   public void setFocused(boolean focused) {
   }

   public boolean isFocused() {
      return false;
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public NarrationPriority narrationPriority() {
      return NarrationPriority.HOVERED;
   }

   public void updateNarration(NarrationElementOutput builder) {
      builder.add(NarratedElementType.POSITION, this.rawText);
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   public void setScale(float scale) {
      this.scale = scale;
      this.updateText();
   }

   public void setText(Component text) {
      this.rawText = text;
      this.updateText();
   }

   private void updateText() {
      this.formattedText = TextUtils.formatInBounds(this.rawText, this.font, (int)(this.maxWidth / this.scale), this.wrap);
      this.width = (int)(TextUtils.getWidth(this.formattedText, this.font) * this.scale);
      this.height = (int)(9 * this.formattedText.size() * this.scale);
   }

   @Override
   public int getX() {
      int x = this.x;
      if (this.alignment == TextUtils.Alignment.RIGHT) {
         x -= this.width;
      } else if (this.alignment == TextUtils.Alignment.CENTER) {
         x -= this.width / 2;
      }

      return x;
   }

   public int getRawX() {
      return this.x;
   }

   @Override
   public void setX(int x) {
      this.x = x;
   }

   @Override
   public int getY() {
      int y = this.y;
      if (this.centerVertically) {
         y -= this.height / 2;
      }

      return y;
   }

   public int getRawY() {
      return this.y;
   }

   @Override
   public void setY(int y) {
      this.y = y;
   }

   @Override
   public void setWidth(int width) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setHeight(int height) {
      throw new UnsupportedOperationException();
   }

   public void setAlpha(int alpha) {
      this.alpha = this.alphaPrecise = alpha;
   }
}
