package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.PianoWidget;
import org.figuramc.figura.gui.widgets.lists.SoundsList;
import org.figuramc.figura.utils.FiguraText;

public class SoundScreen extends AbstractPanelScreen {
   private final Screen sourcePanel;
   private PianoWidget piano;

   public SoundScreen(AbstractPanelScreen parentScreen) {
      super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.sound"));
      this.sourcePanel = parentScreen;
   }

   @Override
   public Class<? extends Screen> getSelectedPanel() {
      return (Class<? extends Screen>)this.sourcePanel.getClass();
   }

   @Override
   protected void init() {
      super.init();
      Avatar owner = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
      int listWidth = Math.min(this.width - 8, 420);
      int listX = (this.width - listWidth) / 2;
      SoundsList list;
      this.addRenderableWidget(list = new SoundsList(listX, 28, listWidth, this.height - 120, owner));
      this.addRenderableWidget(this.piano = new PianoWidget(listX, this.height - 88, listWidth, 60, list::getSound));
      this.addRenderableWidget(new Button(this.width / 2 - 60, this.height - 24, 120, 20, FiguraText.of("gui.done"), null, bx -> this.onClose()));
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.sourcePanel);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      this.piano.pressed = mouseButtonEvent.button() == 0;
      return super.mouseClicked(mouseButtonEvent, bl);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
      this.piano.pressed = false;
      return super.mouseReleased(mouseButtonEvent);
   }
}
