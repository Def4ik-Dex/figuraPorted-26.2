package org.figuramc.figura.entries;

import net.minecraft.client.gui.screens.Screen;
import org.figuramc.figura.gui.widgets.PanelSelectorWidget;

public interface FiguraScreen {
   Screen getScreen(Screen var1);

   default PanelSelectorWidget.PanelIcon getPanelIcon() {
      return PanelSelectorWidget.PanelIcon.OTHER;
   }
}
