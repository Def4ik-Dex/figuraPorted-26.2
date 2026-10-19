package org.figuramc.figura.gui.widgets.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.screens.ConfigScreen;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.ContainerButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.ui.UIHelper;

public class CategoryWidget extends AbstractContainerElement {
   protected final List<AbstractConfigElement> entries = new ArrayList<>();
   private final ConfigType.Category config;
   private final ConfigList parent;
   private ContainerButton parentConfig;

   public CategoryWidget(int width, ConfigType.Category config, ConfigList parent) {
      super(0, 0, width, 20);
      this.config = config;
      this.parent = parent;
      this.parentConfig = new ContainerButton(
         parent, 0, 0, width, 20, (Component)(config == null ? Component.empty() : config.name), config == null ? null : config.tooltip, button -> {
            boolean toggled = this.parentConfig.isToggled();
            this.setShowChildren(toggled);
            ConfigScreen.CATEGORY_DATA.put(config, toggled);
            parent.updateScroll();
         }
      );
      Boolean expanded = ConfigScreen.CATEGORY_DATA.get(config);
      this.parentConfig.setToggled(expanded == null || expanded);
      this.parentConfig.shouldHaveBackground(false);
      this.children.add(this.parentConfig);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         if (this.parentConfig.isToggled() && this.entries.size() > 0) {
            gui.fill(this.getX(), this.getY() + 21, this.getX() + this.getWidth(), this.getY() + this.getHeight(), 301989887);
         }

         if (this.config == Configs.PAPERDOLL) {
            this.parent.parentScreen.renderPaperdoll = this.parentConfig.isToggled()
               && this.parent.isMouseOver(mouseX, mouseY)
               && this.isMouseOver(mouseX, mouseY);
         }

         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY);
   }

   public void addConfig(ConfigType<?> config) {
      if (!config.hidden || FiguraMod.debugModeEnabled()) {
         int width = this.getWidth();
         AbstractConfigElement element;
         if (config instanceof ConfigType.BoolConfig boolConfig) {
            element = new BooleanElement(width, boolConfig, this.parent, this);
         } else if (config instanceof ConfigType.EnumConfig enumConfig) {
            element = new EnumElement(width, enumConfig, this.parent, this);
         } else if (config instanceof ConfigType.InputConfig<?> inputConfig) {
            element = new InputElement(width, inputConfig, this.parent, this);
         } else if (config instanceof ConfigType.KeybindConfig keybindConfig) {
            element = new KeybindElement(width, keybindConfig, this.parent, this);
         } else {
            if (!(config instanceof ConfigType.ButtonConfig buttonConfig)) {
               return;
            }

            element = new ButtonElement(width, buttonConfig, this.parent, this);
         }

         this.setHeight(super.getHeight() + 22);
         this.children.add(element);
         this.entries.add(element);
      }
   }

   public boolean isChanged() {
      for (AbstractConfigElement entry : this.entries) {
         if (entry.isChanged()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int getHeight() {
      if (!this.parentConfig.isToggled()) {
         return 20;
      }

      int height = 20;

      for (AbstractConfigElement entry : this.entries) {
         if (entry.isVisible()) {
            height += entry.getHeight() + 2;
         }
      }

      return height;
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.parentConfig.setX(x);

      for (AbstractConfigElement entry : this.entries) {
         entry.setX(x);
      }
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.parentConfig.setY(y);
      int i = 0;

      for (AbstractConfigElement entry : this.entries) {
         if (entry.isVisible()) {
            entry.setY(y + 22 * (i + 1));
            i++;
         }
      }
   }

   public void setShowChildren(boolean bool) {
      this.parentConfig.setToggled(bool);

      for (AbstractConfigElement element : this.entries) {
         element.setVisible(bool);
      }
   }

   public boolean isShowingChildren() {
      return this.parentConfig.isToggled();
   }

   public void updateKeybinds() {
      for (AbstractConfigElement element : this.entries) {
         if (element instanceof KeybindElement keybind) {
            keybind.updateText();
         }
      }
   }

   public void updateFilter(String query) {
      boolean visible = false;

      for (AbstractConfigElement entry : this.entries) {
         entry.updateFilter(query);
         visible |= entry.matchesFilter();
      }

      this.setVisible(visible);
      if (visible) {
         this.setY(this.getY());
      }

      this.parent.updateScroll();
   }
}
