package org.figuramc.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.lists.KeybindList;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.FiguraText;

public class KeybindScreen extends AbstractPanelScreen {
   private final Screen sourcePanel;
   private KeybindList list;

   public KeybindScreen(AbstractPanelScreen parentScreen) {
      super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.keybind"));
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
      Button reset;
      this.addRenderableWidget(reset = new Button(this.width / 2 - 122, this.height - 24, 120, 20, FiguraText.of("gui.reset_all"), null, button -> {
         if (owner != null && owner.luaRuntime != null) {
            for (FiguraKeybind keybind : owner.luaRuntime.keybinds.keyBindings) {
               keybind.resetDefaultKey();
            }

            this.list.updateBindings();
         }
      }));
      reset.setActive(false);
      this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 24, 120, 20, FiguraText.of("gui.done"), null, bx -> this.onClose()));
      int listWidth = Math.min(this.width - 8, 420);
      this.addRenderableWidget(this.list = new KeybindList((this.width - listWidth) / 2, 28, listWidth, this.height - 56, owner, reset));
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.sourcePanel);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      return this.list.updateKey(Type.MOUSE.getOrCreate(mouseButtonEvent.button())) || super.mouseClicked(mouseButtonEvent, bl);
   }

   @Override
   public boolean keyPressed(KeyEvent keyEvent) {
      return this.list.updateKey(keyEvent.key() == 256 ? InputConstants.UNKNOWN : InputConstants.getKey(keyEvent)) || super.keyPressed(keyEvent);
   }
}
