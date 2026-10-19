package org.figuramc.figura.gui.widgets.lists;

import com.mojang.blaze3d.platform.InputConstants.Key;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigKeyBind;
import org.figuramc.figura.config.ConfigManager;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.screens.ConfigScreen;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.gui.widgets.config.CategoryWidget;
import org.figuramc.figura.gui.widgets.config.InputElement;
import org.figuramc.figura.utils.ui.UIHelper;

public class ConfigList extends AbstractList {
   private final List<CategoryWidget> configs = new ArrayList<>();
   public final ConfigScreen parentScreen;
   public KeyMapping focusedBinding;
   private int totalHeight = 0;

   public ConfigList(int x, int y, int width, int height, ConfigScreen parentScreen) {
      super(x, y, width, height);
      this.parentScreen = parentScreen;
      this.updateList();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
      this.enableScissors(gui);
      this.totalHeight = -4;
      int visibleConfig = 0;

      for (CategoryWidget config : this.configs) {
         if (config.isVisible()) {
            this.totalHeight = this.totalHeight + config.getHeight() + 8;
            visibleConfig++;
         }
      }

      int entryHeight = visibleConfig == 0 ? 0 : this.totalHeight / visibleConfig;
      this.scrollBar.setVisible(this.totalHeight > height);
      this.scrollBar.setScrollRatio(entryHeight, this.totalHeight - height);
      int xOffset = this.scrollBar.isVisible() ? 4 : 11;
      int yOffset = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -4.0, this.totalHeight - height)) : 4;

      for (CategoryWidget config : this.configs) {
         if (config.isVisible()) {
            config.setX(x + xOffset);
            config.setY(y + yOffset);
            yOffset += config.getHeight() + 8;
         }
      }

      super.extractRenderState(gui, mouseX, mouseY, delta);
      gui.disableScissor();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      for (CategoryWidget categoryWidget : this.configs) {
         for (GuiEventListener children : categoryWidget.children()) {
            if (children instanceof InputElement inputElement) {
               TextField field = inputElement.getTextField();
               field.getField().setFocused(field.isEnabled() && field.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y()));
            }
         }
      }

      return super.mouseClicked(mouseButtonEvent, bl);
   }

   public void updateList() {
      for (CategoryWidget config : this.configs) {
         this.children.remove(config);
      }

      this.configs.clear();

      for (ConfigType.Category category : ConfigManager.CATEGORIES_REGISTRY.values()) {
         CategoryWidget widget = new CategoryWidget(this.getWidth() - 22, category, this);

         for (ConfigType<?> config : category.children) {
            widget.addConfig(config);
         }

         this.configs.add(widget);
         this.children.add(widget);
      }

      for (CategoryWidget config : this.configs) {
         config.setShowChildren(config.isShowingChildren());
      }
   }

   public void updateScroll() {
      double pastScroll = (this.totalHeight - this.getHeight()) * this.scrollBar.getScrollProgress();
      this.totalHeight = -4;

      for (CategoryWidget config : this.configs) {
         if (config.isVisible()) {
            this.totalHeight = this.totalHeight + config.getHeight() + 8;
         }
      }

      this.scrollBar.setScrollProgress(pastScroll / (this.totalHeight - this.getHeight()));
   }

   public boolean hasChanges() {
      for (CategoryWidget config : this.configs) {
         if (config.isChanged()) {
            return true;
         }
      }

      return false;
   }

   public boolean updateKey(Key key) {
      if (this.focusedBinding == null) {
         return false;
      }

      this.focusedBinding.setKey(key);
      if (this.focusedBinding instanceof ConfigKeyBind) {
         ((ConfigKeyBind)this.focusedBinding).saveConfigChanges();
      }

      this.focusedBinding = null;
      FiguraMod.processingKeybind = false;
      this.updateKeybinds();
      return true;
   }

   public void updateKeybinds() {
      for (CategoryWidget widget : this.configs) {
         widget.updateKeybinds();
      }
   }

   public void updateSearch(String query) {
      for (CategoryWidget widget : this.configs) {
         widget.updateFilter(query);
      }
   }
}
