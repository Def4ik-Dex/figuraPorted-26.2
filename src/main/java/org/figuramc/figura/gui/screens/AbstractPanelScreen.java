package org.figuramc.figura.gui.screens;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.FiguraRemovable;
import org.figuramc.figura.gui.widgets.FiguraTickable;
import org.figuramc.figura.gui.widgets.PanelSelectorWidget;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.mixin.gui.ScreenAccessor;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public abstract class AbstractPanelScreen extends Screen {
   public static final List<Identifier> BACKGROUNDS = List.of(
      FiguraIdentifier.of("textures/gui/background/background_0.png"),
      FiguraIdentifier.of("textures/gui/background/background_1.png"),
      FiguraIdentifier.of("textures/gui/background/background_2.png")
   );
   protected final Screen parentScreen;
   public PanelSelectorWidget panels;
   public ContextMenu contextMenu;
   public Component tooltip;
   private static final String EGG = "ĉĉĈĈćĆćĆBAā";
   private String egg = "ĉĉĈĈćĆćĆBAā";

   protected AbstractPanelScreen(Screen parentScreen, Component title) {
      super(title);
      this.parentScreen = parentScreen;
   }

   public Class<? extends Screen> getSelectedPanel() {
      return (Class<? extends Screen>)this.getClass();
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(this.panels = new PanelSelectorWidget(this.parentScreen, 0, 0, this.width, this.getSelectedPanel()));
      this.contextMenu = null;
      this.tooltip = null;
   }

   public void tick() {
      for (Renderable renderable : this.renderables()) {
         if (renderable instanceof FiguraTickable tickable) {
            tickable.tick();
         }
      }

      this.renderables().removeIf(r -> r instanceof FiguraRemovable removable && removable.isRemoved());
      super.tick();
   }

   public List<Renderable> renderables() {
      return ((ScreenAccessor)this).getRenderables();
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      this.renderBackground(gui, delta);
      super.extractRenderState(gui, mouseX, mouseY, delta);
      this.renderOverlays(gui, mouseX, mouseY, delta);
   }

   public void renderBackground(GuiGraphicsExtractor gui, float delta) {
      float speed = Configs.BACKGROUND_SCROLL_SPEED.tempValue * 0.125F;

      for (Identifier background : BACKGROUNDS) {
         UIHelper.renderAnimatedBackground(gui, background, 0.0F, 0.0F, this.width, this.height, 64.0F, 64.0F, speed, delta);
         speed = (float)(speed / 0.5);
      }

      gui.nextStratum();
   }

   public void renderOverlays(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (Configs.GUI_FPS.value) {
         gui.text(Minecraft.getInstance().font, ClientAPI.getFPS() + " fps", 1, 1, UIHelper.adjustColor(16777215));
      }

      if (this.contextMenu != null && this.contextMenu.isVisible()) {
         Matrix3x2fStack pose = gui.pose();
         pose.pushMatrix();
         gui.nextStratum();
         this.contextMenu.extractRenderState(gui, mouseX, mouseY, delta);
         pose.popMatrix();
      }

      if (this.tooltip != null) {
         UIHelper.renderTooltip(gui, this.tooltip, mouseX, mouseY, true);
      }

      this.tooltip = null;
   }

   public void onClose() {
      this.minecraft.gui.setScreen(this.parentScreen);
   }

   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      if (this.contextMenuClick(mouseButtonEvent, bl)) {
         return true;
      }

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

   public boolean contextMenuClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
      if (this.contextMenu != null && this.contextMenu.isVisible()) {
         boolean clicked = this.contextMenu.mouseClicked(mouseButtonEvent, false);
         if (!clicked && this.contextMenu.parent != null && this.contextMenu.parent.mouseClicked(mouseButtonEvent, bl)) {
            return true;
         }

         this.contextMenu.setVisible(false);
         return clicked;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
      return this.getFocused() != null && this.getFocused().mouseDragged(mouseButtonEvent, d, e);
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
      if (this.contextMenu != null) {
         this.contextMenu.setVisible(false);
      }

      boolean ret = false;

      for (GuiEventListener child : this.children()) {
         if (child.isMouseOver(mouseX, mouseY)) {
            ret = ret || child.mouseScrolled(mouseX, mouseY, amount, d);
         }
      }

      return ret;
   }

   public boolean keyPressed(KeyEvent keyEvent) {
      this.egg = this.egg + (char)keyEvent.key();
      this.egg = this.egg.substring(1);
      if ("ĉĉĈĈćĆćĆBAā".equals(this.egg)) {
         Minecraft.getInstance().gui.setScreen(new GameScreen(this));
         return true;
      } else if (this.children().contains(this.panels) && this.panels.cycleTab(keyEvent.key())) {
         return true;
      } else if (keyEvent.key() == 256 && this.contextMenu != null && this.contextMenu.isVisible()) {
         this.contextMenu.setVisible(false);
         return true;
      } else {
         return super.keyPressed(keyEvent);
      }
   }

   protected void extractBlurredBackground(GuiGraphicsExtractor guiGraphics) {
   }
}
