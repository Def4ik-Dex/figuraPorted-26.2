package org.figuramc.figura.gui.widgets;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class StatusWidget implements FiguraWidget, FiguraTickable, GuiEventListener {
   public static final String STATUS_INDICATORS = "-*/+";
   public static final List<String> STATUS_NAMES = List.of("size", "texture", "script", "backend");
   public static final List<Style> TEXT_COLORS = List.of(
      Style.EMPTY.withColor(ChatFormatting.WHITE),
      Style.EMPTY.withColor(ChatFormatting.RED),
      Style.EMPTY.withColor(ChatFormatting.YELLOW),
      Style.EMPTY.withColor(ChatFormatting.GREEN)
   );
   private final Font font;
   protected final int count;
   protected int status = 0;
   private Component scriptError;
   private Component disconnectedReason;
   private int x;
   private int y;
   private int width;
   private int height;
   private boolean visible = true;
   private boolean background = true;

   public StatusWidget(int x, int y, int width) {
      this(x, y, width, STATUS_NAMES.size());
   }

   protected StatusWidget(int x, int y, int width, int count) {
      this.x = x;
      this.y = y;
      this.font = Minecraft.getInstance().font;
      this.width = width;
      this.height = 9 + 5;
      this.count = count;
   }

   @Override
   public void tick() {
      if (this.isVisible()) {
         Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
         boolean empty = avatar == null || avatar.nbt == null;
         this.status = empty ? 0 : (avatar.fileSize > NetworkStuff.getSizeLimit() ? 1 : (avatar.fileSize > NetworkStuff.getSizeLimit() * 0.75 ? 2 : 3));
         int texture = !empty && avatar.hasTexture ? 3 : 0;
         this.status += texture << 2;
         int script = empty ? 0 : (avatar.scriptError ? 1 : (avatar.luaRuntime == null ? 0 : (avatar.versionStatus > 0 ? 2 : 3)));
         this.status += script << 4;
         this.scriptError = script == 1 ? avatar.errorText.copy() : null;
         int backend = NetworkStuff.backendStatus;
         this.status += backend << 6;
         String dc = NetworkStuff.disconnectedReason;
         this.disconnectedReason = backend == 1 && dc != null && !dc.isBlank() ? Component.literal(dc) : null;
      }
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         int x = this.getX();
         int y = this.getY();
         int width = this.getWidth();
         int height = this.getHeight();
         boolean background = this.hasBackground();
         if (background) {
            UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
         }

         boolean hovered = this.isMouseOver(mouseX, mouseY);
         double spacing = (double)width / this.count;
         double hSpacing = spacing * 0.5;

         for (int i = 0; i < this.count; i++) {
            int xx = (int)(x + spacing * i + hSpacing);
            Component text = this.getStatusIcon(i);
            gui.text(this.font, text, xx - this.font.width(text) / 2, y + (background ? 3 : 0), UIHelper.adjustColor(16777215));
            if (hovered && mouseX >= xx - hSpacing && mouseX < xx + hSpacing && mouseY >= y && mouseY < y + 9 + (background ? 3 : 0)) {
               UIHelper.setTooltip(this.getTooltipFor(i));
            }
         }
      }
   }

   public MutableComponent getStatusIcon(int type) {
      return Component.literal(String.valueOf("-*/+".charAt(this.status >> type * 2 & 3))).setStyle(Style.EMPTY.withFont(new Resource(UIHelper.UI_FONT)));
   }

   public Component getTooltipFor(int i) {
      int color = this.status >> i * 2 & 3;
      String part = "gui.status." + STATUS_NAMES.get(i);
      MutableComponent info;
      if (i == 0) {
         double size = NetworkStuff.getSizeLimit();
         info = FiguraText.of(part + "." + color, MathUtils.asFileSize(size));
      } else {
         info = FiguraText.of(part + "." + color);
      }

      MutableComponent text = FiguraText.of(part).append("\n• ").append(info).setStyle(TEXT_COLORS.get(color));
      if (i == 2 && color == 1 && this.scriptError != null) {
         text.append("\n\n").append(FiguraText.of("gui.status.reason")).append("\n• ").append(this.scriptError);
      }

      if (i == 3 && this.disconnectedReason != null) {
         text.append("\n\n").append(FiguraText.of("gui.status.reason")).append("\n• ").append(this.disconnectedReason);
      }

      return text;
   }

   public boolean hasBackground() {
      return this.background;
   }

   public void setBackground(boolean background) {
      this.background = background;
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY);
   }

   public void setFocused(boolean bl) {
   }

   public boolean isFocused() {
      return false;
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   @Override
   public void setX(int x) {
      this.x = x;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public void setY(int y) {
      this.y = y;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public void setWidth(int width) {
      this.width = width;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   @Override
   public int getHeight() {
      return this.height;
   }
}
