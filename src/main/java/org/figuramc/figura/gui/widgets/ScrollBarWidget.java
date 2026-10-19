package org.figuramc.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class ScrollBarWidget extends AbstractWidget implements FiguraWidget {
   public static final Identifier SCROLLBAR_TEXTURE = FiguraIdentifier.of("textures/gui/scrollbar.png");
   protected final int headHeight = 20;
   protected final int headWidth = 10;
   protected boolean isScrolling = false;
   protected boolean vertical = true;
   protected double scrollPos;
   protected double scrollPrecise;
   protected double scrollRatio = 1.0;
   protected ScrollBarWidget.OnPress onPress;

   public ScrollBarWidget(int x, int y, int width, int height, double initialValue) {
      super(x, y, width, height, Component.empty());
      this.scrollPrecise = initialValue;
      this.scrollPos = initialValue;
   }

   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
         return false;
      }

      if (mouseButtonEvent.button() == 0) {
         double mouseX = mouseButtonEvent.x();
         double mouseY = mouseButtonEvent.y();
         double scrollPos = Mth.lerp(this.scrollPrecise, 0.0, (this.vertical ? this.getHeight() - 20 : this.getWidth() - 10) + 2.0);
         if ((!this.vertical || !(mouseY < this.getY() + scrollPos)) && !(mouseY > this.getY() + scrollPos + 20.0)) {
            if (!this.vertical && mouseX < this.getX() + scrollPos || mouseX > this.getX() + scrollPos + 10.0) {
               this.scroll(-(this.getX() + scrollPos + 5.0 - mouseX));
            }
         } else {
            this.scroll(-(this.getY() + scrollPos + 10.0 - mouseY));
         }

         this.isScrolling = true;
         this.playDownSound(Minecraft.getInstance().getSoundManager());
         return true;
      } else {
         return super.mouseClicked(mouseButtonEvent, bl);
      }
   }

   public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
      if (mouseButtonEvent.button() == 0 && this.isScrolling) {
         this.isScrolling = false;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      if (this.isScrolling) {
         if (this.vertical) {
            if (Math.signum(deltaY) == -1.0) {
               if (mouseY <= this.getY() + this.getHeight()) {
                  this.scroll(deltaY);
                  return true;
               }
            } else if (mouseY >= this.getY()) {
               this.scroll(deltaY);
               return true;
            }
         } else if (Math.signum(deltaX) == -1.0) {
            if (mouseX <= this.getX() + this.getWidth()) {
               this.scroll(deltaX);
               return true;
            }
         } else if (mouseX >= this.getX()) {
            this.scroll(deltaX);
            return true;
         }
      }

      return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
      if (!this.isActive()) {
         return false;
      }

      this.scroll(-amount - d * (this.vertical ? this.getHeight() : this.getWidth()) * 0.05 * this.scrollRatio);
      return true;
   }

   public boolean keyPressed(KeyEvent keyEvent) {
      if (!this.isActive()) {
         return false;
      } else {
         int keyCode = keyEvent.key();
         if (keyCode > 261 && keyCode < 266) {
            this.scroll((keyCode % 2 == 0 ? 1 : -1) * (this.vertical ? this.getHeight() : this.getWidth()) * 0.05 * this.scrollRatio);
            return true;
         } else {
            return super.keyPressed(keyEvent);
         }
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY);
   }

   protected void scroll(double amount) {
      this.scrollPrecise = this.scrollPrecise + amount / ((this.vertical ? this.getHeight() - 20 : this.getWidth() - 10) + 2.0);
      this.setScrollProgress(this.scrollPrecise);
   }

   protected void lerpPos(float delta) {
      float lerpDelta = MathUtils.magicDelta(0.2F, delta);
      this.scrollPos = Mth.lerp(lerpDelta, this.scrollPos, this.getScrollProgress());
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         this.isHovered = this.isMouseOver(mouseX, mouseY);
         UIHelper.enableBlend();
         int x = this.getX();
         int y = this.getY();
         int width = this.getWidth();
         int height = this.getHeight();
         gui.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TEXTURE, x, y, 10.0F, this.isScrolling ? 20.0F : 0.0F, width, 1, 10, 1, 20, 40);
         gui.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TEXTURE, x, y + 1, 10.0F, this.isScrolling ? 21.0F : 1.0F, width, height - 2, 10, 18, 20, 40);
         gui.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TEXTURE, x, y + height - 1, 10.0F, this.isScrolling ? 39.0F : 19.0F, width, 1, 10, 1, 20, 40);
         this.lerpPos(delta);
         gui.blit(
            RenderPipelines.GUI_TEXTURED,
            SCROLLBAR_TEXTURE,
            x,
            (int)(y + Math.round(Mth.lerp(this.scrollPos, 0.0, height - 20))),
            0.0F,
            !this.isHoveredOrFocused() && !this.isScrolling ? 0.0F : 20.0F,
            10,
            20,
            20,
            40
         );
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput builder) {
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   @Override
   public int getX() {
      return super.getX();
   }

   @Override
   public void setX(int x) {
      super.setX(x);
   }

   @Override
   public int getY() {
      return super.getY();
   }

   @Override
   public void setY(int y) {
      super.setY(y);
   }

   @Override
   public int getWidth() {
      return super.getWidth();
   }

   @Override
   public void setWidth(int width) {
      super.setWidth(width);
   }

   @Override
   public int getHeight() {
      return super.getHeight();
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public double getScrollProgress() {
      return this.scrollPrecise;
   }

   public void setScrollProgress(double amount) {
      this.setScrollProgress(amount, false);
   }

   public void setScrollProgressNoAnim(double amount) {
      this.setScrollProgress(amount, false);
      this.scrollPos = this.scrollPrecise;
   }

   public void setScrollProgress(double amount, boolean force) {
      amount = Double.isNaN(amount) ? 0.0 : amount;
      this.scrollPrecise = force ? amount : Mth.clamp(amount, 0.0, 1.0);
      if (this.onPress != null) {
         this.onPress.onPress(this);
      }
   }

   public void setAction(ScrollBarWidget.OnPress onPress) {
      this.onPress = onPress;
   }

   public void setScrollRatio(double entryHeight, double heightDiff) {
      this.scrollRatio = (this.getHeight() + entryHeight) / (heightDiff / 2.0);
   }

   public interface OnPress {
      void onPress(ScrollBarWidget var1);
   }
}
