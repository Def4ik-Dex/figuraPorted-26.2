package org.figuramc.figura.gui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class AvatarInfoWidget implements FiguraWidget, FiguraTickable, GuiEventListener {
   private static final MutableComponent UNKNOWN = Component.literal("?").setStyle(ColorUtils.Colors.AWESOME_BLUE.style);
   private static final MutableComponent ELLIPSIS = TextUtils.ELLIPSIS.copy().setStyle(ColorUtils.Colors.AWESOME_BLUE.style);
   private static final List<Component> TITLES = List.of(
      FiguraText.of("gui.name").withStyle(ChatFormatting.UNDERLINE),
      FiguraText.of("gui.authors").withStyle(ChatFormatting.UNDERLINE),
      FiguraText.of("gui.size").withStyle(ChatFormatting.UNDERLINE),
      FiguraText.of("gui.complexity").withStyle(ChatFormatting.UNDERLINE)
   );
   private int x;
   private int y;
   private int width;
   private int height;
   private boolean visible = true;
   private final int maxSize;
   private final Font font;
   private final List<Component> values = new ArrayList<Component>() {
      {
         for (Component ignored : AvatarInfoWidget.TITLES) {
            this.add(AvatarInfoWidget.UNKNOWN);
         }
      }
   };

   public AvatarInfoWidget(int x, int y, int width, int maxSize) {
      this.x = x;
      this.y = y;
      this.font = Minecraft.getInstance().font;
      this.width = width;
      this.height = (9 + 4) * TITLES.size() * 2 + 4;
      this.maxSize = maxSize;
   }

   @Override
   public void tick() {
      if (this.visible) {
         Style accent = FiguraMod.getAccentColor();
         ELLIPSIS.setStyle(accent);
         UNKNOWN.setStyle(accent);
         Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
         if (avatar != null && avatar.nbt != null) {
            this.values.set(0, avatar.name != null && !avatar.name.isBlank() ? Emojis.applyEmojis(Component.literal(avatar.name).setStyle(accent)) : UNKNOWN);
            this.values
               .set(1, avatar.authors != null && !avatar.authors.isBlank() ? Emojis.applyEmojis(Component.literal(avatar.authors).setStyle(accent)) : UNKNOWN);
            this.values.set(2, Component.literal(MathUtils.asFileSize(avatar.fileSize)).setStyle(accent));
            this.values.set(3, Component.literal(String.valueOf(avatar.complexity.pre)).setStyle(accent));
         } else {
            for (int i = 0; i < TITLES.size(); i++) {
               this.values.set(i, UNKNOWN);
            }
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.visible) {
         int x = this.x + this.width / 2;
         int y = this.y + 4;
         int height = 9 + 4;
         int maxLines = (this.maxSize - 8) / height;
         int authorFreeLines = maxLines - 7;
         Component authors = this.values.get(1);
         List<Component> authorLines = authors == null ? Collections.emptyList() : TextUtils.splitText(authors, "\n");
         int authorUsedLines = Math.min(authorLines.size(), authorFreeLines);
         int newHeight = height * TITLES.size() * 2 + 4 + height * (authorUsedLines - 1);
         this.height = Math.min(newHeight + height, this.maxSize);
         y += (this.height - newHeight) / 2;
         UIHelper.blitSliced(gui, this.x, this.y, this.width, this.height, UIHelper.OUTLINE_FILL);

         for (int i = 0; i < TITLES.size(); i++) {
            Component title = TITLES.get(i);
            if (title != null) {
               gui.centeredText(this.font, title, x, y, UIHelper.adjustColor(16777215));
            }

            y += height;
            Component value = this.values.get(i);
            if (value == null) {
               y += height;
            } else if (i != 1) {
               Component toRender = TextUtils.trimToWidthEllipsis(this.font, value, this.width - 10, ELLIPSIS);
               gui.centeredText(this.font, toRender, x, y, UIHelper.adjustColor(16777215));
               if (value != toRender && UIHelper.isMouseOver(this.x, y - height, this.width, height * 2 - 4, mouseX, mouseY)) {
                  UIHelper.setTooltip(value);
               }

               y += height;
            } else {
               for (int j = 0; j < authorUsedLines; j++) {
                  Component text = authorLines.get(j);
                  Component newText = TextUtils.trimToWidthEllipsis(this.font, text, this.width - 10, ELLIPSIS);
                  if (j == authorUsedLines - 1 && authorLines.size() > authorUsedLines) {
                     text = value;
                     newText = ELLIPSIS;
                  }

                  if (text != newText && UIHelper.isMouseOver(this.x, y, this.width, height, mouseX, mouseY)) {
                     UIHelper.setTooltip(text);
                  }

                  gui.centeredText(this.font, newText, x, y, UIHelper.adjustColor(16777215));
                  y += height;
               }
            }
         }
      }
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void setFocused(boolean bl) {
   }

   public boolean isFocused() {
      return false;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public void setX(int x) {
      this.x = x;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public void setY(int y) {
      this.y = y;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void setWidth(int width) {
      this.width = width;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }
}
