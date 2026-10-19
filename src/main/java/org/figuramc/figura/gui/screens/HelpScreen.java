package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.IconButton;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.ParticleWidget;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class HelpScreen extends AbstractPanelScreen {
   public static final Identifier ICONS = FiguraIdentifier.of("textures/gui/help_icons.png");
   public static final String LUA_VERSION = "5.2 - Figura";
   private IconButton kofi;

   public HelpScreen(Screen parentScreen) {
      super(parentScreen, FiguraText.of("gui.panels.title.help"));
   }

   @Override
   protected void init() {
      super.init();
      int lineHeight = 9;
      int middle = this.width / 2;
      int labelWidth = Math.min(this.width - 8, 420);
      int y = 28;
      Style color = FiguraMod.getAccentColor();
      this.addRenderableWidget(new HelpScreen.Title(FiguraText.of("gui.help.docs").withStyle(color), middle, y, labelWidth));
      IconButton docs;
      int var7;
      this.addRenderableWidget(
         docs = new IconButton(
            middle - 60,
            var7 = y + lineHeight + 4,
            120,
            24,
            20,
            0,
            20,
            ICONS,
            60,
            40,
            FiguraText.of("gui.help.ingame_docs"),
            null,
            button -> this.minecraft.gui.setScreen(new DocsScreen(this))
         )
      );
      docs.setActive(false);
      int var10003 = middle - 60;
      y = var7 + 28;
      this.addRenderableWidget(
         new IconButton(
            var10003,
            y,
            120,
            24,
            0,
            0,
            20,
            ICONS,
            60,
            40,
            FiguraText.of("gui.help.lua_manual"),
            null,
            bx -> UIHelper.openURL(FiguraMod.Links.LuaManual.url).run()
         )
      );
      var10003 = middle - 60;
      y += 28;
      this.addRenderableWidget(
         new IconButton(
            var10003,
            y,
            120,
            24,
            40,
            0,
            20,
            ICONS,
            60,
            40,
            FiguraText.of("gui.help.external_wiki"),
            null,
            bx -> UIHelper.openURL(FiguraMod.Links.Wiki.url).run()
         )
      );
      MutableComponent var16 = FiguraText.of("gui.help.links").withStyle(color);
      y += 28;
      this.addRenderableWidget(new HelpScreen.Title(var16, middle, y, labelWidth));
      int var11;
      this.addRenderableWidget(
         new IconButton(
            middle - 124,
            var11 = y + lineHeight + 4,
            80,
            24,
            0,
            20,
            20,
            ICONS,
            60,
            40,
            Component.literal("Discord"),
            null,
            bx -> UIHelper.openURL(FiguraMod.Links.Discord.url).run()
         )
      );
      this.addRenderableWidget(
         new IconButton(
            middle - 40, var11, 80, 24, 20, 20, 20, ICONS, 60, 40, Component.literal("GitHub"), null, bx -> UIHelper.openURL(FiguraMod.Links.Github.url).run()
         ) {
            @Override
            public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
               double mouseX = mouseButtonEvent.x();
               double mouseY = mouseButtonEvent.y();
               if (Configs.EASTER_EGGS.value && this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && mouseButtonEvent.button() == 1) {
                  int dim = this.getTextureSize();
                  int x = (int)(Math.random() * dim) + this.getX() + 2;
                  int yx = (int)(Math.random() * dim) + this.getY() + 2;
                  HelpScreen.this.addRenderableOnly(new ParticleWidget(x, yx, ParticleTypes.HEART));
                  boolean purr = Math.random() < 0.95;
                  HelpScreen.this.minecraft
                     .getSoundManager()
                     .play(SimpleSoundInstance.forUI(purr ? SoundEvents.CAT_PURR_BABY : SoundEvents.CAT_AMBIENT_BABY, 1.0F));
                  return false;
               } else {
                  return super.mouseClicked(mouseButtonEvent, bl);
               }
            }
         }
      );
      this.addRenderableWidget(
         this.kofi = new IconButton(
            middle + 44, var11, 80, 24, 40, 20, 20, ICONS, 60, 40, Component.literal("Ko-fi"), null, b -> UIHelper.openURL(FiguraMod.Links.Kofi.url).run()
         )
      );
      MutableComponent var17 = FiguraText.of("gui.help.about").withStyle(color);
      y = var11 + 28;
      this.addRenderableWidget(new HelpScreen.Title(var17, middle, y, labelWidth));
      int var13;
      this.addRenderableWidget(
         new Label(
            FiguraText.of("gui.help.lua_version", Component.literal("5.2 - Figura").withStyle(color)),
            middle,
            var13 = y + lineHeight + 4,
            TextUtils.Alignment.CENTER
         )
      );
      this.addRenderableWidget(
         new Label(
            FiguraText.of("gui.help.figura_version", Component.literal(FiguraMod.VERSION.toString()).withStyle(color)),
            middle,
            y = var13 + lineHeight + 4,
            TextUtils.Alignment.CENTER
         )
      );
      this.addRenderableWidget(new Button(middle - 60, this.height - 24, 120, 20, FiguraText.of("gui.done"), null, bx -> this.onClose()));
   }

   @Override
   public void tick() {
      super.tick();
      if (FiguraMod.ticks % 5 == 0 && this.kofi.isHoveredOrFocused()) {
         int x = (int)(Math.random() * this.kofi.getWidth()) + this.kofi.getX();
         int y = (int)(Math.random() * this.kofi.getHeight()) + this.kofi.getY();
         this.addRenderableOnly(new ParticleWidget(x, y, ParticleTypes.HAPPY_VILLAGER));
      }
   }

   private static class Title extends Label {
      private final int width;

      public Title(Object text, int x, int y, int width) {
         super(text, x, y, TextUtils.Alignment.CENTER);
         this.width = width;
      }

      @Override
      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         int x = this.getRawX();
         int y = this.getRawY();
         int y0 = y + this.getHeight() / 2;
         int y1 = y0 + 1;
         int x0 = x - this.width / 2;
         int x1 = x - this.getWidth() / 2 - 4;
         gui.fill(x0, y0, x1, y1, -1);
         x0 = x + this.getWidth() / 2 + 4;
         x1 = x + this.width / 2;
         gui.fill(x0, y0, x1, y1, -1);
         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }
}
