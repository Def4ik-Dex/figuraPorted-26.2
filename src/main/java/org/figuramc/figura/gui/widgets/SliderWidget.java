package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

public class SliderWidget extends ScrollBarWidget {
   public static final Identifier SLIDER_TEXTURE = FiguraIdentifier.of("textures/gui/slider.png");
   protected final int headHeight = 11;
   protected final int headWidth = 11;
   protected final boolean showSteps;
   private int max;
   private double stepSize;
   private double steppedPos;

   public SliderWidget(int x, int y, int width, int height, double initialValue, int maxValue, boolean showSteps) {
      super(x, y, width, height, initialValue);
      this.vertical = false;
      this.showSteps = showSteps;
      this.steppedPos = initialValue;
      this.setMax(maxValue);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
      if (!this.isActive()) {
         return false;
      }

      this.scroll(this.stepSize * Math.signum(-amount - d) * (this.getWidth() - 11 + 2.0));
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent keyEvent) {
      if (!this.isActive()) {
         return false;
      } else {
         int keyCode = keyEvent.key();
         if (keyCode > 261 && keyCode < 266) {
            this.scroll(this.stepSize * (keyCode % 2 == 0 ? 1 : -1) * Math.max(keyEvent.modifiers() * 10, 1) * (this.getWidth() - 11 + 2.0));
            return true;
         } else {
            return super.keyPressed(keyEvent);
         }
      }
   }

   @Override
   protected void scroll(double amount) {
      super.scroll(amount);
      this.steppedPos = this.getClosestStep();
   }

   private double getClosestStep() {
      double lowest = this.scrollPrecise - this.scrollPrecise % this.stepSize;
      double highest = lowest + this.stepSize;
      double distanceLow = Math.abs(lowest - this.scrollPrecise);
      double distanceHigh = Math.abs(highest - this.scrollPrecise);
      return distanceLow < distanceHigh ? lowest : highest;
   }

   @Override
   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      this.isHovered = this.isMouseOver(mouseX, mouseY);
      UIHelper.enableBlend();
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      gui.blit(RenderPipelines.GUI_TEXTURED, SLIDER_TEXTURE, x, y + 3, this.isScrolling ? 10.0F : 0.0F, 0.0F, width, 5, 5, 5, 33, 16);
      if (this.showSteps) {
         for (int i = 0; i < this.max; i++) {
            gui.blit(
               RenderPipelines.GUI_TEXTURED,
               SLIDER_TEXTURE,
               (int)Math.floor(x + 3 + this.stepSize * i * (width - 11)),
               y + 3,
               this.isScrolling ? 15.0F : 5.0F,
               0.0F,
               5,
               5,
               5,
               5,
               33,
               16
            );
         }
      }

      this.lerpPos(delta);
      gui.blit(
         RenderPipelines.GUI_TEXTURED,
         SLIDER_TEXTURE,
         (int)(x + Math.round(Mth.lerp(this.scrollPos, 0.0, width - 11))),
         y,
         this.isActive() ? (!this.isHoveredOrFocused() && !this.isScrolling ? 11 : 22) : 0.0F,
         5.0F,
         11,
         11,
         33,
         16
      );
   }

   @Override
   public double getScrollProgress() {
      return this.steppedPos;
   }

   @Override
   public void setScrollProgress(double amount, boolean force) {
      this.steppedPos = force ? amount : Mth.clamp(amount, 0.0, 1.0);
      super.setScrollProgress(amount, force);
   }

   public int getMax() {
      return this.max;
   }

   public void setMax(int maxValue) {
      this.max = maxValue;
      this.stepSize = 1.0 / (maxValue - 1);
   }

   public int getIntValue() {
      return (int)Math.round(this.getScrollProgress() * (this.getMax() - 1));
   }
}
