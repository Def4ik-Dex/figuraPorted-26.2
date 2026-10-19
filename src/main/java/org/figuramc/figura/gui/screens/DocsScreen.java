package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

public class DocsScreen extends AbstractPanelScreen {
   private final Screen sourcePanel;

   public DocsScreen(AbstractPanelScreen parentScreen) {
      super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.docs"));
      this.sourcePanel = parentScreen;
   }

   @Override
   public Class<? extends Screen> getSelectedPanel() {
      return (Class<? extends Screen>)this.sourcePanel.getClass();
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(
         new Label(
            Component.empty().append("Still not finished :s"),
            this.width / 2,
            this.height / 2,
            3.0F,
            200,
            true,
            TextUtils.Alignment.CENTER,
            ColorUtils.Colors.AWESOME_BLUE.hex
         )
      );
   }
}
