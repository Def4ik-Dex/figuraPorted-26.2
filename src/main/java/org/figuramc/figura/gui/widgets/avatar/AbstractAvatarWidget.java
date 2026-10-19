package org.figuramc.figura.gui.widgets.avatar;

import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.util.Util;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAvatarWidget extends AbstractContainerElement implements Comparable<AbstractAvatarWidget> {
   protected static final int SPACING = 6;
   protected static final Component FAVOURITE = Component.literal("★").withStyle(Style.EMPTY.withFont(new Resource(UIHelper.UI_FONT)));
   protected static final Component ADD_FAVOURITE = FiguraText.of("gui.context.favorite.add");
   protected static final Component REMOVE_FAVOURITE = FiguraText.of("gui.context.favorite.remove");
   protected final AvatarList parent;
   protected final int depth;
   protected final ContextMenu context;
   protected LocalAvatarFetcher.AvatarPath avatar;
   protected Button button;
   protected String filter = "";
   protected boolean favourite;

   public AbstractAvatarWidget(int depth, int width, int height, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
      super(0, 0, width, height);
      this.parent = parent;
      this.avatar = avatar;
      this.depth = depth;
      this.context = new ContextMenu(this);
      this.favourite = avatar.isFavourite();
      this.context.addAction(this.favourite ? REMOVE_FAVOURITE : ADD_FAVOURITE, null, button -> {
         this.favourite = !this.favourite;
         avatar.setFavourite(this.favourite);
         button.setMessage(this.favourite ? REMOVE_FAVOURITE : ADD_FAVOURITE);
         this.context.updateDimensions();
      });
      this.context.addAction(FiguraText.of("gui.context.open_folder"), null, button -> {
         try {
            Util.getPlatform().openUri(avatar.getFSPath().toUri());
         } catch (Exception e) {
            FiguraMod.debug("failed to open avatar folder: ", e.getMessage());
            Util.getPlatform().openUri(LocalAvatarFetcher.getLocalAvatarDirectory().toUri());
         }
      });
      this.context.addAction(FiguraText.of("gui.context.copy_path"), null, button -> {
         Minecraft.getInstance().keyboardHandler.setClipboard(avatar.getFSPath().toString());
         FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
      });
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible() && this.button.isVisible()) {
         super.extractRenderState(gui, mouseX, mouseY, delta);
         if (this.favourite) {
            Font font = Minecraft.getInstance().font;
            int width = font.width(FAVOURITE);
            int x = this.getX() + this.getWidth() - width;
            int y = this.getY() + 2;
            gui.text(font, FAVOURITE, x, y, UIHelper.adjustColor(16777215), false);
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 9) {
               UIHelper.setTooltip(FiguraText.of("gui.favorited").append(" ").append(FAVOURITE));
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      int button = mouseButtonEvent.button();
      if (!this.isMouseOver(mouseX, mouseY)) {
         return false;
      }

      if (super.mouseClicked(mouseButtonEvent, bl)) {
         return true;
      }

      if (button == 1) {
         this.context.setX((int)mouseX);
         this.context.setY((int)mouseY);
         this.context.setVisible(true);
         UIHelper.setContext(this.context);
         return true;
      }

      if (UIHelper.getContext() == this.context) {
         this.context.setVisible(false);
      }

      return false;
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
   }

   public void update(LocalAvatarFetcher.AvatarPath path, String filter) {
      this.avatar = path;
      this.filter = filter.toLowerCase(Locale.US);
   }

   public Component getName() {
      return Component.literal(this.avatar.getName());
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.button.setX(x);
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.button.setY(y);
   }

   public boolean filtered() {
      return this.getName().getString().toLowerCase(Locale.US).contains(this.filter.toLowerCase(Locale.US));
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible && this.filtered());
   }

   public int compareTo(@NotNull AbstractAvatarWidget other) {
      if (this.favourite && !other.favourite) {
         return -1;
      } else if (other.favourite && !this.favourite) {
         return 1;
      } else if (this instanceof AvatarFolderWidget && other instanceof AvatarWidget) {
         return -1;
      } else {
         return this instanceof AvatarWidget && other instanceof AvatarFolderWidget
            ? 1
            : this.getName().getString().toLowerCase(Locale.US).compareTo(other.getName().getString().toLowerCase(Locale.US));
      }
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this
         || obj instanceof AbstractAvatarWidget other && other.avatar != null && this.avatar != null && this.avatar.getPath().equals(other.avatar.getPath());
   }

   public boolean isOf(Path path) {
      return this.avatar != null && this.avatar.getTheActualPathForThis().equals(path);
   }
}
