package org.figuramc.figura.gui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.NotNull;

public class FiguraToast implements Toast {
   private final FiguraToast.ToastType type;
   private Component title;
   private Component message;
   private boolean update;
   private long startTime;
   private Visibility visibility;

   public FiguraToast(Component title, Component message, FiguraToast.ToastType type) {
      this.type = type;
      this.update(title, message, false);
   }

   public void update(Component title, Component message, boolean update) {
      this.title = Component.empty().setStyle(this.type.style).append(title);
      this.message = message;
      this.update = update;
   }

   public void extractRenderState(GuiGraphicsExtractor gui, Font font, long startTime) {
      int titleTime = Math.round(Configs.TOAST_TITLE_TIME.value * 1000.0F);
      long timeDiff = startTime - this.startTime;
      UIHelper.enableBlend();
      int frame = Configs.REDUCED_MOTION.value ? 0 : (int)(FiguraMod.ticks / 5.0F % this.type.frames);
      gui.blit(
         RenderPipelines.GUI_TEXTURED,
         this.type.texture,
         0,
         0,
         0.0F,
         frame * this.height(),
         this.width(),
         this.height(),
         this.width(),
         this.height() * this.type.frames
      );
      if (this.message.getString().isBlank()) {
         this.renderText(this.title, font, gui, 255);
      } else if (this.title.getString().isBlank()) {
         this.renderText(this.message, font, gui, 255);
      } else {
         List<FormattedCharSequence> a = font.split(this.title, this.width() - this.type.spacing - 1);
         List<FormattedCharSequence> b = font.split(this.message, this.width() - this.type.spacing - 1);
         if (a.size() == 1 && b.size() == 1) {
            int y = Math.round(this.height() / 2.0F - 9.0F - 1.0F);
            gui.text(font, this.title, this.type.spacing, y, UIHelper.adjustColor(16777215));
            gui.text(font, this.message, this.type.spacing, y * 2 + 4, UIHelper.adjustColor(16777215));
         } else if (timeDiff < titleTime) {
            this.renderText(this.title, font, gui, Math.round(Math.min(Math.max((float)(titleTime - timeDiff) / 300.0F, 0.0F), 1.0F) * 255.0F));
         } else {
            this.renderText(this.message, font, gui, Math.round(Math.min(Math.max((float)(timeDiff - titleTime) / 300.0F, 0.0F), 1.0F) * 255.0F));
         }
      }
   }

   @NotNull
   public Visibility getWantedVisibility() {
      return this.visibility;
   }

   public void update(ToastManager toastManager, long startTime) {
      int time = Math.round(Configs.TOAST_TIME.value * 1000.0F);
      if (this.update) {
         if (startTime - this.startTime < time) {
            Visibility.SHOW.playSound(Minecraft.getInstance().getSoundManager());
         }

         this.startTime = startTime;
         this.update = false;
      }

      long timeDiff = startTime - this.startTime;
      this.visibility = timeDiff < time ? Visibility.SHOW : Visibility.HIDE;
   }

   public void renderText(Component text, Font font, GuiGraphicsExtractor gui, int alpha) {
      List<FormattedCharSequence> list = font.split(text, this.width() - this.type.spacing - 1);
      if (list.size() == 1) {
         gui.text(font, text, this.type.spacing, Math.round(this.height() / 2.0F - 9.0F / 2.0F), UIHelper.adjustColor(16777215 + (alpha << 24)));
      } else {
         int y = Math.round(this.height() / 2.0F - 9.0F - 1.0F);

         for (int i = 0; i < list.size(); i++) {
            gui.text(font, list.get(i), this.type.spacing, y * (i + 1) + 4 * i, UIHelper.adjustColor(16777215 + (alpha << 24)));
         }
      }
   }

   public Object getToken() {
      return this.type;
   }

   public int width() {
      return this.type.width;
   }

   public int height() {
      return 32;
   }

   public static void sendToast(Object title) {
      sendToast(title, Component.empty());
   }

   public static void sendToast(Object title, FiguraToast.ToastType type) {
      sendToast(title, Component.empty(), type);
   }

   public static void sendToast(Object title, Object message) {
      sendToast(title, message, FiguraToast.ToastType.DEFAULT);
   }

   public static void sendToast(Object title, Object message, FiguraToast.ToastType type) {
      Component text = (Component)(title instanceof Component t ? t : Component.translatable(title.toString()));
      Component text2 = (Component)(message instanceof Component m ? m : Component.translatable(message.toString()));
      if (type == FiguraToast.ToastType.DEFAULT && Configs.EASTER_EGGS.value) {
         Calendar calendar = FiguraMod.CALENDAR;
         calendar.setTime(new Date());
         if (calendar.get(5) == 1 && calendar.get(2) == 3 || Math.random() < 1.0E-4) {
            type = FiguraToast.ToastType.CHEESE;
         }
      }

      ToastManager toasts = Minecraft.getInstance().gui.toastManager();
      FiguraToast toast = (FiguraToast)toasts.getToast(FiguraToast.class, type);
      FiguraMod.debug("Sent toast: \"{}\", \"{}\" of type: \"{}\"", text.getString(), text2.getString(), type.name());
      if (toast != null) {
         toast.update(text, text2, true);
      } else {
         toasts.addToast(new FiguraToast(text, text2, type));
      }
   }

   public enum ToastType {
      DEFAULT(FiguraIdentifier.of("textures/gui/toast/default.png"), 4, 160, 31, 5636095),
      WARNING(FiguraIdentifier.of("textures/gui/toast/warning.png"), 4, 160, 31, 16776960),
      ERROR(FiguraIdentifier.of("textures/gui/toast/error.png"), 4, 160, 31, 16711680),
      CHEESE(FiguraIdentifier.of("textures/gui/toast/cheese.png"), 1, 160, 31, ColorUtils.Colors.CHEESE.hex);

      private final Identifier texture;
      private final int frames;
      private final Style style;
      private final int width;
      private final int spacing;

      ToastType(Identifier texture, int frames, int width, int spacing, int color) {
         this.texture = texture;
         this.frames = frames;
         this.width = width;
         this.spacing = spacing;
         this.style = Style.EMPTY.withColor(color);
      }
   }
}
