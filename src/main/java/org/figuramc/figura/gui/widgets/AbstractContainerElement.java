package org.figuramc.figura.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.utils.ui.UIHelper;

public abstract class AbstractContainerElement extends AbstractContainerEventHandler implements FiguraTickable, FiguraWidget, NarratableEntry {
   public static final Component HOVERED_ARROW = Component.literal("•");
   protected final List<GuiEventListener> children = new ArrayList<>();
   private int x;
   private int y;
   private int width;
   private int height;
   private boolean hovered = false;
   private boolean visible = true;

   public AbstractContainerElement(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   @Override
   public void tick() {
      for (GuiEventListener listener : this.children) {
         if (listener instanceof FiguraTickable tickable) {
            tickable.tick();
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      for (GuiEventListener listener : this.children) {
         if (listener instanceof Renderable widget) {
            widget.extractRenderState(gui, mouseX, mouseY, delta);
         }
      }
   }

   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      GuiEventListener widget = null;

      for (GuiEventListener children : List.copyOf(this.children())) {
         boolean clicked = children.mouseClicked(mouseButtonEvent, bl);
         children.setFocused(clicked);
         if (clicked) {
            widget = children;
         }
      }

      if (this.getFocused() != widget) {
         this.setFocused(widget);
      }

      if (widget != null) {
         if (mouseButtonEvent.button() == 0) {
            this.setDragging(true);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY);
   }

   public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
      return this.getFocused() != null && this.getFocused().mouseDragged(mouseButtonEvent, deltaX, deltaY);
   }

   public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
      boolean bool = this.getFocused() != null && this.getFocused().mouseReleased(mouseButtonEvent);
      if (bool) {
         this.setFocused(null);
      }

      this.setDragging(false);
      return bool;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
      boolean ret = false;

      for (GuiEventListener child : this.children()) {
         if (child.isMouseOver(mouseX, mouseY)) {
            ret = ret || child.mouseScrolled(mouseX, mouseY, amount, d);
         }
      }

      return ret;
   }

   public void setHovered(boolean hovered) {
      this.hovered = hovered;
   }

   public boolean isHovered() {
      return this.hovered;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;

      for (GuiEventListener listener : this.children) {
         if (listener instanceof FiguraWidget drawable) {
            drawable.setVisible(visible);
         } else if (listener instanceof AbstractWidget widget) {
            widget.visible = visible;
         }
      }
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setX(int x) {
      this.x = x;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public void setY(int y) {
      this.y = y;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public void setWidth(int width) {
      this.width = width;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   public List<? extends GuiEventListener> children() {
      return this.children;
   }

   public void setFocused(boolean focused) {
      if (!focused) {
         this.setFocused(null);
      }

      super.setFocused(focused);
   }

   public void updateNarration(NarrationElementOutput output) {
   }

   public NarrationPriority narrationPriority() {
      return NarrationPriority.NONE;
   }
}
