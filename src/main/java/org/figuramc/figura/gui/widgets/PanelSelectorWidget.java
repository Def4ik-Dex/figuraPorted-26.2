package org.figuramc.figura.gui.widgets;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.entries.FiguraScreen;
import org.figuramc.figura.gui.screens.BrowserScreen;
import org.figuramc.figura.gui.screens.ConfigScreen;
import org.figuramc.figura.gui.screens.HelpScreen;
import org.figuramc.figura.gui.screens.PermissionsScreen;
import org.figuramc.figura.gui.screens.ProfileScreen;
import org.figuramc.figura.gui.screens.WardrobeScreen;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class PanelSelectorWidget extends AbstractContainerElement {
   public static final Identifier BACKGROUND = FiguraIdentifier.of("textures/gui/panels_background.png");
   private static final List<Function<Screen, Pair<Screen, PanelSelectorWidget.PanelIcon>>> PANELS = new ArrayList<Function<Screen, Pair<Screen, PanelSelectorWidget.PanelIcon>>>() {
      {
         this.add(s -> Pair.of(new ProfileScreen(s), PanelSelectorWidget.PanelIcon.PROFILE));
         this.add(s -> Pair.of(new BrowserScreen(s), PanelSelectorWidget.PanelIcon.BROWSER));
         this.add(s -> Pair.of(new WardrobeScreen(s), PanelSelectorWidget.PanelIcon.WARDROBE));
         this.add(s -> Pair.of(new PermissionsScreen(s), PanelSelectorWidget.PanelIcon.PERMISSIONS));
         this.add(s -> Pair.of(new ConfigScreen(s), PanelSelectorWidget.PanelIcon.SETTINGS));
         this.add(s -> Pair.of(new HelpScreen(s), PanelSelectorWidget.PanelIcon.HELP));
      }
   };
   private static final List<Integer> PANELS_BLACKLIST = List.of(0, 1);
   private final List<PanelSelectorWidget.PanelButton> buttons = new ArrayList<>();
   private PanelSelectorWidget.PanelButton selected;

   public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, Class<? extends Screen> selected) {
      super(x, y, width, 28);
      int buttonCount = PANELS.size() - (FiguraMod.debugModeEnabled() ? 0 : PANELS_BLACKLIST.size());
      int buttonWidth = Math.min(Math.max((width - 4) / buttonCount - 4, 24), 96) + 4;
      int spacing = (width - (4 + buttonWidth * buttonCount)) / 2;

      for (int i = 0; i < PANELS.size(); i++) {
         if (FiguraMod.debugModeEnabled() || !PANELS_BLACKLIST.contains(i)) {
            Pair<Screen, PanelSelectorWidget.PanelIcon> panel = PANELS.get(i).apply(parentScreen);
            Screen s = (Screen)panel.getFirst();
            PanelSelectorWidget.PanelIcon icon = (PanelSelectorWidget.PanelIcon)panel.getSecond();
            int buttonX = 4 + buttonWidth * this.buttons.size() + spacing;
            this.createPanelButton(s, icon, s.getClass() == selected, buttonX, buttonWidth - 4);
         }
      }

      if (FiguraMod.debugModeEnabled()) {
         for (int i : PANELS_BLACKLIST) {
            PanelSelectorWidget.PanelButton button = this.buttons.get(i);
            button.setMessage(button.getMessage().copy().withStyle(ChatFormatting.RED));
         }
      }
   }

   public static void initEntryPoints(Set<FiguraScreen> set) {
      for (FiguraScreen figuraScreen : set) {
         PanelSelectorWidget.PanelIcon icon = figuraScreen.getPanelIcon();
         PANELS.add(s -> Pair.of(figuraScreen.getScreen(s), icon == null ? PanelSelectorWidget.PanelIcon.OTHER : icon));
      }
   }

   private void createPanelButton(Screen panel, PanelSelectorWidget.PanelIcon icon, boolean toggled, int x, int width) {
      PanelSelectorWidget.PanelButton button = new PanelSelectorWidget.PanelButton(
         x, this.getY(), width, this.getHeight() - 4, panel.getTitle(), icon, this, bx -> Minecraft.getInstance().gui.setScreen(panel)
      );
      button.shouldHaveBackground(false);
      if (toggled) {
         this.selected = button;
      }

      this.buttons.add(button);
      this.children.add(button);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      UIHelper.blitSliced(gui, this.getX(), this.getY(), this.selected.getX() - this.getX(), this.getHeight() - 4, BACKGROUND);
      UIHelper.blitSliced(
         gui,
         this.selected.getX() + this.selected.getWidth(),
         this.getY(),
         this.getWidth() - this.selected.getX() - this.selected.getWidth(),
         this.getHeight() - 4,
         BACKGROUND
      );
      super.extractRenderState(gui, mouseX, mouseY, delta);
   }

   public boolean cycleTab(int keyCode) {
      if (IOUtils.hasControlDown()) {
         int i = this.getNextPanel(keyCode);
         if (i >= 0 && i < this.buttons.size()) {
            PanelSelectorWidget.PanelButton button = this.buttons.get(i);
            button.run();
            return true;
         }
      }

      return false;
   }

   private int getNextPanel(int keyCode) {
      if (keyCode >= 49 && keyCode <= 57) {
         return keyCode - 49;
      } else if (keyCode == 258) {
         int index = this.buttons.indexOf(this.selected);
         int i = IOUtils.hasShiftDown() ? index - 1 : index + 1;
         return Math.floorMod(i, this.buttons.size());
      } else {
         return -1;
      }
   }

   private static class PanelButton extends IconButton {
      public static final Identifier TEXTURE = FiguraIdentifier.of("textures/gui/panels_button.png");
      public static final Identifier ICONS = FiguraIdentifier.of("textures/gui/panels.png");
      private final PanelSelectorWidget parent;

      public PanelButton(
         int x, int y, int width, int height, Component text, PanelSelectorWidget.PanelIcon icon, PanelSelectorWidget parent, OnPress pressAction
      ) {
         super(x, y, width, height, 20 * icon.uv, 0, 20, ICONS, 140, 20, text, null, pressAction);
         this.parent = parent;
      }

      @Override
      public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         super.extractContents(gui, mouseX, mouseY, delta);
         boolean iconOnly = this.iconsOnly();
         if (iconOnly && this.isMouseOver(mouseX, mouseY)) {
            UIHelper.setTooltip(this.getMessage());
         }
      }

      @Override
      protected void renderTexture(GuiGraphicsExtractor gui, float delta) {
         UIHelper.blitSliced(
            gui,
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            this.isSelected() ? 24.0F : 0.0F,
            this.isHoveredOrFocused() ? 24.0F : 0.0F,
            24,
            24,
            48,
            48,
            TEXTURE
         );
         UIHelper.enableBlend();
         int size = this.getTextureSize();
         gui.blit(
            RenderPipelines.GUI_TEXTURED,
            this.texture,
            this.getX() + (this.iconsOnly() ? (this.getWidth() - size) / 2 : 2),
            this.getY() + (this.getHeight() - size) / 2 + (!this.isSelected() ? 2 : 0),
            this.u.intValue(),
            this.v.intValue(),
            size,
            size,
            this.regionSize,
            this.regionSize,
            this.textureWidth,
            this.textureHeight
         );
      }

      @Override
      protected void renderText(GuiGraphicsExtractor gui, float delta) {
         if (!this.iconsOnly()) {
            int size = this.getTextureSize();
            int offset = !this.isSelected() ? 3 : 0;
            Component message = (Component)(this.isSelected() ? this.getMessage().copy().withStyle(ChatFormatting.UNDERLINE) : this.getMessage());
            UIHelper.renderCenteredScrollingText(
               gui, message, this.getX() + 4 + size, this.getY() + offset, this.getWidth() - 6 - size, this.getHeight(), this.getTextColor()
            );
         }
      }

      private boolean iconsOnly() {
         return this.getWidth() < 72;
      }

      private boolean isSelected() {
         return this.parent.selected == this;
      }
   }

   public enum PanelIcon {
      PROFILE(0),
      BROWSER(1),
      WARDROBE(2),
      PERMISSIONS(3),
      SETTINGS(4),
      HELP(5),
      OTHER(6);

      public final int uv;

      PanelIcon(int uv) {
         this.uv = uv;
      }
   }
}
