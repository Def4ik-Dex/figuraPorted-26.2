package org.figuramc.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.config.ConfigManager;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.PaperDoll;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.SearchBar;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class ConfigScreen extends AbstractPanelScreen {
   public static final Map<ConfigType.Category, Boolean> CATEGORY_DATA = new HashMap<>();
   private ConfigList list;
   private Button cancel;
   private final boolean hasPanels;
   public boolean renderPaperdoll;

   public ConfigScreen(Screen parentScreen) {
      this(parentScreen, true);
   }

   public ConfigScreen(Screen parentScreen, boolean enablePanels) {
      super(parentScreen, FiguraText.of("gui.panels.title.settings"));
      this.hasPanels = enablePanels;
   }

   @Override
   protected void init() {
      super.init();
      loadNbt();
      if (!this.hasPanels) {
         this.removeWidget(this.panels);
         Label l;
         this.addRenderableWidget(l = new Label(this.getTitle(), this.width / 2, 14, TextUtils.Alignment.CENTER));
         l.centerVertically = true;
      }

      int width = Math.min(this.width - 8, 420);
      this.list = new ConfigList((this.width - width) / 2, 52, width, this.height - 80, this);
      this.addRenderableWidget(new SearchBar(this.width / 2 - 122, 28, 244, 20, query -> this.list.updateSearch(query.toLowerCase(Locale.US))));
      this.addRenderableWidget(this.list);
      this.addRenderableWidget(this.cancel = new Button(this.width / 2 - 122, this.height - 24, 120, 20, FiguraText.of("gui.cancel"), null, button -> {
         ConfigManager.discardConfig();
         this.list.updateList();
      }));
      this.cancel.setActive(false);
      this.addRenderableWidget(new Button(this.width / 2 + 2, this.height - 24, 120, 20, FiguraText.of("gui.done"), null, button -> this.onClose()));
   }

   @Override
   public void tick() {
      super.tick();
      this.cancel.setActive(this.list.hasChanges());
   }

   public void removed() {
      ConfigManager.applyConfig();
      ConfigManager.saveConfig();
      saveNbt();
      super.removed();
   }

   @Override
   public void renderOverlays(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      super.renderOverlays(gui, mouseX, mouseY, delta);
      if (this.renderPaperdoll) {
         UIHelper.renderWithoutScissors(gui, g -> PaperDoll.render(g, true));
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      return this.list.updateKey(Type.MOUSE.getOrCreate(mouseButtonEvent.button())) || super.mouseClicked(mouseButtonEvent, bl);
   }

   @Override
   public boolean keyPressed(KeyEvent keyEvent) {
      int keyCode = keyEvent.key();
      return this.list.updateKey(keyCode == 256 ? InputConstants.UNKNOWN : InputConstants.getKey(keyEvent)) || super.keyPressed(keyEvent);
   }

   private static void loadNbt() {
      IOUtils.readCacheFile("settings", nbt -> {
         for (Tag tag : nbt.getListOrEmpty("settings")) {
            CompoundTag compound = (CompoundTag)tag;
            String config = (String)compound.getString("config").get();
            boolean expanded = (Boolean)compound.getBoolean("expanded").get();
            CATEGORY_DATA.put(ConfigManager.CATEGORIES_REGISTRY.get(config), expanded);
         }
      });
   }

   private static void saveNbt() {
      IOUtils.saveCacheFile("settings", nbt -> {
         ListTag list = new ListTag();

         for (Entry<ConfigType.Category, Boolean> entry : CATEGORY_DATA.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.putString("config", entry.getKey().id);
            compound.putBoolean("expanded", entry.getValue());
            list.add(compound);
         }

         nbt.put("settings", list);
      });
   }

   public static void clearCache() {
      IOUtils.deleteCacheFile("settings");
   }
}
