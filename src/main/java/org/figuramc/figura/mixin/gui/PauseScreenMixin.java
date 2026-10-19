package org.figuramc.figura.mixin.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.screens.WardrobeScreen;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
   @Unique
   private static final Identifier FIGURA_ICON = FiguraIdentifier.of("textures/gui/icon.png");
   @Unique
   private LayoutElement lanButton;

   protected PauseScreenMixin(Component title) {
      super(title);
   }

   @Inject(
      method = "createPauseMenu",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"),
      slice = @Slice(
         from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasSingleplayerServer()Z"),
         to = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z")
      ),
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void saveLanButton(CallbackInfo ci, GridLayout gridLayout, RowHelper rowHelper) {
      gridLayout.visitChildren(element -> this.lanButton = element);
   }

   @Inject(method = "createPauseMenu", at = @At("RETURN"))
   private void createPauseMenuButton(CallbackInfo ci) {
      int config = Configs.BUTTON_LOCATION.value;
      int x;
      int y;
      switch (config) {
         case 1:
            x = 4;
            y = 4;
            break;
         case 2:
            x = this.width - 68;
            y = 4;
            break;
         case 3:
            x = 4;
            y = this.height - 24;
            break;
         case 4:
            x = this.width - 68;
            y = this.height - 24;
            break;
         default:
            x = this.lanButton == null ? this.width / 2 + 106 : this.lanButton.getX() + this.lanButton.getWidth() + 4;
            y = this.lanButton == null ? this.height / 4 + 80 : this.lanButton.getY();
      }

      if (config > 0) {
         this.addRenderableWidget(new Button(x, y, 64, 20, FiguraText.of(), null, btn -> this.minecraft.gui.setScreen(new WardrobeScreen(this))) {
            @Override
            public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
               ChatFormatting color;
               if (this.isHoveredOrFocused()) {
                  color = ChatFormatting.AQUA;
               } else if (AvatarManager.panic) {
                  color = ChatFormatting.GRAY;
               } else {
                  color = ChatFormatting.WHITE;
               }

               this.setMessage(this.getMessage().copy().withStyle(color));
               this.renderVanillaBackground(gui, mouseX, mouseY, delta);
               super.extractContents(gui, mouseX, mouseY, delta);
            }

            @Override
            protected void renderDefaultTexture(GuiGraphicsExtractor gui, float delta) {
            }
         });
      } else {
         this.addRenderableWidget(new Button(x, y, 20, 20, 0, 0, 20, FIGURA_ICON, 60, 20, null, btn -> this.minecraft.gui.setScreen(new WardrobeScreen(this))) {
            @Override
            public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
               this.renderVanillaBackground(gui, mouseX, mouseY, delta);
               super.extractContents(gui, mouseX, mouseY, delta);
            }

            @Override
            protected int getU() {
               int u = super.getU();
               return u == 1 && AvatarManager.panic ? 0 : u;
            }
         });
      }
   }
}
