package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.utils.FiguraText;

public class BrowserScreen extends AbstractPanelScreen {
   public BrowserScreen(Screen parentScreen) {
      super(parentScreen, FiguraText.of("gui.panels.title.browser"));
   }

   @Override
   public void init() {
      super.init();
      int y = -84;
      int var10003 = this.width / 2 - 50;
      int var10004 = this.height / 2;
      y += 24;
      this.addRenderableWidget(
         new Button(
            var10003,
            var10004 + y,
            100,
            20,
            Component.literal("default toast"),
            FiguraText.of("backend.error"),
            button -> FiguraToast.sendToast("default", "test", FiguraToast.ToastType.DEFAULT)
         )
      );
      var10003 = this.width / 2 - 50;
      var10004 = this.height / 2;
      y += 24;
      this.addRenderableWidget(
         new Button(
            var10003,
            var10004 + y,
            100,
            20,
            Component.literal("error toast"),
            Component.literal("test2"),
            button -> FiguraToast.sendToast("error", "test", FiguraToast.ToastType.ERROR)
         )
      );
      var10003 = this.width / 2 - 50;
      var10004 = this.height / 2;
      y += 24;
      this.addRenderableWidget(
         new Button(
            var10003,
            var10004 + y,
            100,
            20,
            Component.literal("warning toast"),
            Component.literal("test3\novo"),
            button -> FiguraToast.sendToast("warning", "test", FiguraToast.ToastType.WARNING)
         )
      );
      var10003 = this.width / 2 - 50;
      var10004 = this.height / 2;
      y += 24;
      this.addRenderableWidget(
         new Button(
            var10003,
            var10004 + y,
            100,
            20,
            Component.literal("cheese toast"),
            Component.literal("test4\n\nhehe"),
            button -> FiguraToast.sendToast("cheese", "test", FiguraToast.ToastType.CHEESE)
         )
      );
   }
}
