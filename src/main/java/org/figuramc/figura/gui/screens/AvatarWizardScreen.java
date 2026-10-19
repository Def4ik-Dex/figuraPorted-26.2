package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.lists.AvatarWizardList;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.wizards.AvatarWizard;

public class AvatarWizardScreen extends AbstractPanelScreen {
   private final Screen sourcePanel;
   private final AvatarWizard wizard = new AvatarWizard();
   private Button build;

   public AvatarWizardScreen(AbstractPanelScreen parentScreen) {
      super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.avatar_wizard"));
      this.sourcePanel = parentScreen;
   }

   @Override
   public Class<? extends Screen> getSelectedPanel() {
      return (Class<? extends Screen>)this.sourcePanel.getClass();
   }

   @Override
   protected void init() {
      super.init();
      this.addRenderableWidget(new Button(this.width / 2 - 122, this.height - 24, 120, 20, FiguraText.of("gui.cancel"), null, button -> this.onClose()));
      this.addRenderableWidget(this.build = new Button(this.width / 2 + 4, this.height - 24, 120, 20, FiguraText.of("gui.create"), null, button -> {
         try {
            this.wizard.build();
            FiguraToast.sendToast(FiguraText.of("toast.avatar_wizard.success"));
         } catch (Exception e) {
            FiguraToast.sendToast(FiguraText.of("toast.avatar_wizard.error"), FiguraToast.ToastType.ERROR);
            FiguraMod.LOGGER.error("", e);
         }

         this.onClose();
      }));
      int width = Math.min(this.width - 8, 420) / 2;
      this.addRenderableWidget(new AvatarWizardList((this.width - width) / 2, 28, width, this.height - 56, this.wizard));
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.sourcePanel);
   }

   @Override
   public void tick() {
      super.tick();
      this.build.setActive(this.wizard.canBuild());
   }
}
