package org.figuramc.figura.gui.widgets.lists;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.ScrollBarWidget;
import org.figuramc.figura.utils.ui.UIHelper;

public abstract class AbstractList extends AbstractContainerElement {
   protected final ScrollBarWidget scrollBar;
   public int scissorsX;
   public int scissorsY;
   public int scissorsWidth;
   public int scissorsHeight;

   public AbstractList(int x, int y, int width, int height) {
      super(x, y, width, height);
      this.updateScissors(1, 1, -2, -2);
      this.children.add(this.scrollBar = new ScrollBarWidget(x + width - 14, y + 4, 10, height - 8, 0.0));
      this.scrollBar.setVisible(false);
   }

   public void updateScissors(int xOffset, int yOffset, int endXOffset, int endYOffset) {
      this.scissorsX = xOffset;
      this.scissorsY = yOffset;
      this.scissorsWidth = endXOffset;
      this.scissorsHeight = endYOffset;
   }

   public boolean isInsideScissors(double mouseX, double mouseY) {
      return UIHelper.isMouseOver(
         this.getX() + this.scissorsX,
         this.getY() + this.scissorsY,
         this.getWidth() + this.scissorsWidth,
         this.getHeight() + this.scissorsHeight,
         mouseX,
         mouseY
      );
   }

   public void enableScissors(GuiGraphicsExtractor gui) {
      int x = this.getX() + this.scissorsX;
      int y = this.getY() + this.scissorsY;
      gui.enableScissor(x, y, x + this.getWidth() + this.scissorsWidth, y + this.getHeight() + this.scissorsHeight);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      for (GuiEventListener child : this.children) {
         if (child instanceof Renderable widget && !this.contents().contains(child)) {
            widget.extractRenderState(gui, mouseX, mouseY, delta);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
      return this.scrollBar.mouseScrolled(mouseX, mouseY, amount, d) || super.mouseScrolled(mouseX, mouseY, amount, d);
   }

   public List<? extends GuiEventListener> contents() {
      return Collections.emptyList();
   }
}
