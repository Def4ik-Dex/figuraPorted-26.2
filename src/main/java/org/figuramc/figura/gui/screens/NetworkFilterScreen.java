package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.figuramc.figura.config.ConfigManager;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.lists.NetworkFilterList;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

public class NetworkFilterScreen extends AbstractPanelScreen {
   private final ConfigType.NetworkFilterConfig config = Configs.NETWORK_FILTER;
   private final Label titleLabel = new Label(this.getTitle(), 0, 0, TextUtils.Alignment.CENTER);
   private NetworkFilterList networkFilterList;

   public NetworkFilterScreen(Screen parentScreen) {
      super(parentScreen, FiguraText.of("gui.network_filter"));
   }

   @Override
   protected void init() {
      this.titleLabel.setX(this.width / 2);
      this.titleLabel.setY(8 + this.titleLabel.getHeight() / 2);
      int listWidth = Math.min(420, this.width - 8);
      this.addRenderableWidget(
         this.networkFilterList = new NetworkFilterList(
            (this.width - listWidth) / 2,
            this.titleLabel.getY() + this.titleLabel.getHeight() / 2 + 8,
            listWidth,
            this.height - (this.titleLabel.getY() + this.titleLabel.getHeight() / 2 + 16),
            this.config
         )
      );
      this.addRenderableOnly(this.titleLabel);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      super.extractRenderState(gui, mouseX, mouseY, delta);
   }

   protected void repositionElements() {
      this.titleLabel.setX(this.width / 2);
      this.titleLabel.setY(8 + this.titleLabel.getHeight() / 2);
      int listWidth = Math.min(420, this.width - 8);
      this.networkFilterList.setX((this.width - listWidth) / 2);
      this.networkFilterList.setY(this.titleLabel.getY() + this.titleLabel.getHeight() / 2 + 8);
      this.networkFilterList.setWidth(listWidth);
      this.networkFilterList.setHeight(this.height - (this.titleLabel.getY() + this.titleLabel.getHeight() / 2 + 16));
   }

   public void removed() {
      ConfigManager.applyConfig();
      ConfigManager.saveConfig();
   }
}
