package org.figuramc.figura.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

public class ContextMenu extends AbstractContainerElement {
   public static final Identifier BACKGROUND = FiguraIdentifier.of("textures/gui/context.png");
   private final int minWidth;
   private final List<ContextMenu.ContextButton> entries = new ArrayList<>();
   public GuiEventListener parent;
   private ContextMenu nestedContext;

   public ContextMenu(GuiEventListener parent, int minWidth) {
      super(0, 0, minWidth, 2);
      this.minWidth = minWidth;
      this.parent = parent;
      this.setVisible(false);
   }

   public ContextMenu(GuiEventListener parent) {
      this(parent, 0);
   }

   public ContextMenu() {
      this(null);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         UIHelper.blitSliced(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0.0F, 0.0F, 16, 16, 48, 16, BACKGROUND);
         int i = 0;
         int y = this.getY() + 1;

         while (i < this.entries.size()) {
            int height = this.entries.get(i).getHeight();
            UIHelper.blitSliced(gui, this.getX() + 1, y, this.getWidth() - 2, height, i % 2 == 1 ? 32.0F : 16.0F, 0.0F, 16, 16, 48, 16, BACKGROUND);
            this.entries.get(i).extractRenderState(gui, mouseX, mouseY, delta);
            y += height;
            i++;
         }

         if (this.nestedContext != null) {
            this.nestedContext.extractRenderState(gui, mouseX, mouseY, delta);
            if (this.nestedContext.parent instanceof Button button) {
               button.setHovered(true);
            }
         }
      }
   }

   public void addAction(Component name, Component tooltip, OnPress action) {
      this.addElement(new ContextMenu.ContextButton(this.getX(), this.getY() + this.getHeight(), name, tooltip, this, action));
   }

   public void addDivisor() {
      this.addElement(new ContextMenu.ContextDivisor(this.getX(), this.getY() + this.getHeight()));
   }

   public void addTab(Component name, Component tooltip, ContextMenu context) {
      ContextMenu.ContextButton button = new ContextMenu.TabButton(this.getX(), this.getY() + this.getHeight(), name, tooltip, this, context);
      this.addElement(button);
      context.parent = button;
      this.children.add(context);
   }

   private void addElement(ContextMenu.ContextButton element) {
      this.children.add(element);
      this.entries.add(element);
      this.updateDimensions();
   }

   private void clearNest() {
      if (this.nestedContext != null) {
         this.nestedContext.clearNest();
         this.nestedContext = null;
      }
   }

   public void updateDimensions() {
      this.setWidth(this.minWidth);
      this.setHeight(2);

      for (ContextMenu.ContextButton entry : this.entries) {
         this.setWidth(Math.max(entry.getTrueWidth() + 8, this.getWidth()));
         this.setHeight(this.getHeight() + entry.getHeight());
      }

      for (ContextMenu.ContextButton entry : this.entries) {
         entry.setWidth(this.getWidth() - 2);
      }
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      this.clearNest();
   }

   @Override
   public void setX(int x) {
      int realWidth = x + this.getWidth();
      int clientWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
      if (realWidth > clientWidth) {
         x -= realWidth - clientWidth;
      }

      super.setX(x);

      for (ContextMenu.ContextButton button : this.entries) {
         button.setX(x + 1);
         if (button instanceof ContextMenu.TabButton tab) {
            tab.context.setX(tab.getX() + tab.getWidth());
         }
      }
   }

   @Override
   public void setY(int y) {
      int realHeight = y + this.getHeight();
      int clientHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
      if (realHeight > clientHeight) {
         y -= realHeight - clientHeight;
      }

      super.setY(y);
      int heigth = y + 1;

      for (ContextMenu.ContextButton button : this.entries) {
         button.setY(heigth);
         heigth += button.getHeight();
         if (button instanceof ContextMenu.TabButton tab) {
            tab.context.setY(tab.getY() - 1);
         }
      }
   }

   public List<? extends AbstractWidget> getEntries() {
      return this.entries;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      boolean result = super.mouseClicked(mouseButtonEvent, bl);
      this.setFocused(null);
      return result;
   }

   private static class ContextButton extends Button {
      protected final ContextMenu parent;

      public ContextButton(int x, int y, Component text, Component tooltip, ContextMenu parent, OnPress pressAction) {
         super(x, y, 0, 16, text, tooltip, pressAction);
         this.shouldHaveBackground(false);
         this.parent = parent;
      }

      protected ContextButton(int x, int y, int height) {
         super(x, y, 0, height, Component.empty(), null, button -> {});
         this.shouldHaveBackground(false);
         this.parent = null;
      }

      @Override
      protected void renderText(GuiGraphicsExtractor gui, float delta) {
         Font font = Minecraft.getInstance().font;
         gui.text(
            font, this.getMessage(), this.getX() + 3, (int)(this.getY() + this.getHeight() / 2.0F - 9.0F / 2.0F), UIHelper.adjustColor(this.getTextColor())
         );
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         if (UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY, true)) {
            UIHelper.setTooltip(this.tooltip);
            this.parent.clearNest();
            return true;
         } else {
            return false;
         }
      }

      public int getTrueWidth() {
         return Minecraft.getInstance().font.width(this.getMessage());
      }
   }

   private static class ContextDivisor extends ContextMenu.ContextButton {
      public ContextDivisor(int x, int y) {
         super(x, y, 9);
      }

      @Override
      public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         gui.fill(this.getX() + 4, this.getY() + 4, this.getX() + this.getWidth() - 4, this.getY() + 5, -16777216 + TextColor.DARK_GRAY.getValue());
      }

      @Override
      public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
         return false;
      }
   }

   private static class TabButton extends ContextMenu.ContextButton {
      private static final Component ARROW = Component.literal(">").setStyle(Style.EMPTY.withFont(new Resource(UIHelper.UI_FONT)));
      private final ContextMenu context;

      public TabButton(int x, int y, Component text, Component tooltip, ContextMenu parent, ContextMenu context) {
         super(x, y, text, tooltip, parent, button -> {});
         this.context = context;
         this.context.setVisible(true);
      }

      @Override
      public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         super.extractContents(gui, mouseX, mouseY, delta);
         Font font = Minecraft.getInstance().font;
         gui.text(
            font,
            ARROW,
            this.getX() + this.getWidth() - font.width(ARROW) - 3,
            (int)(this.getY() + this.getHeight() / 2.0F - 9.0F / 2.0F),
            UIHelper.adjustColor(this.getTextColor())
         );
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         boolean mouseOver = UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY, true);
         if (!mouseOver && this.parent.nestedContext != this.context) {
            return false;
         }

         UIHelper.setTooltip(this.tooltip);
         this.parent.nestedContext = this.context;
         if (mouseOver) {
            this.context.nestedContext = null;
         }

         return true;
      }

      @Override
      public int getTrueWidth() {
         return super.getTrueWidth() + Minecraft.getInstance().font.width(Component.literal(" ").append(ARROW));
      }
   }
}
