package org.figuramc.figura.gui.widgets.lists;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.gui.screens.AbstractPanelScreen;
import org.figuramc.figura.gui.screens.AvatarWizardScreen;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.SearchBar;
import org.figuramc.figura.gui.widgets.avatar.AbstractAvatarWidget;
import org.figuramc.figura.gui.widgets.avatar.AvatarFolderWidget;
import org.figuramc.figura.gui.widgets.avatar.AvatarWidget;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class AvatarList extends AbstractList {
   private final HashMap<Path, AbstractAvatarWidget> avatars = new HashMap<>();
   private final ArrayList<AbstractAvatarWidget> avatarList = new ArrayList<>();
   private int totalHeight = 0;
   private String filter = "";
   public static Path selectedEntry;

   public AvatarList(int x, int y, int width, int height, AbstractPanelScreen parentScreen) {
      super(x, y, width, height);
      this.children.add(new SearchBar(x + 4, y + 4, width - 8, 20, s -> {
         if (!this.filter.equals(s)) {
            this.scrollBar.setScrollProgress(0.0);
         }

         this.filter = s;
      }));
      this.children
         .add(
            new Button(
               x + width / 2 - 46,
               y + 28,
               20,
               20,
               0,
               0,
               20,
               FiguraIdentifier.of("textures/gui/new_avatar.png"),
               60,
               20,
               FiguraText.of("gui.wardrobe.new_avatar.tooltip"),
               button -> Minecraft.getInstance().gui.setScreen(new AvatarWizardScreen(parentScreen))
            )
         );
      this.children
         .add(
            new Button(
               x + width / 2 - 10,
               y + 28,
               20,
               20,
               0,
               0,
               20,
               FiguraIdentifier.of("textures/gui/unselect.png"),
               60,
               20,
               FiguraText.of("gui.wardrobe.unselect.tooltip"),
               button -> {
                  AvatarManager.loadLocalAvatar(null);
                  selectedEntry = null;
               }
            )
         );
      this.children
         .add(
            new Button(
               x + width / 2 + 26,
               y + 28,
               20,
               20,
               0,
               0,
               20,
               FiguraIdentifier.of("textures/gui/folder.png"),
               60,
               20,
               FiguraText.of("gui.wardrobe.folder.tooltip"),
               button -> Util.getPlatform().openUri(LocalAvatarFetcher.getLocalAvatarDirectory().toUri())
            )
         );
      this.scrollBar.setY(y + 48);
      this.scrollBar.setHeight(height - 52);
      this.updateScissors(1, 49, -2, -50);
      this.loadContents();
      this.scrollToSelected();
   }

   @Override
   public void tick() {
      this.loadContents();
      super.tick();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
      this.enableScissors(gui);
      this.totalHeight = 2;

      for (AbstractAvatarWidget avatar : this.avatarList) {
         this.totalHeight = this.totalHeight + avatar.getHeight() + 2;
      }

      int entryHeight = this.avatarList.isEmpty() ? 0 : this.totalHeight / this.avatarList.size();
      this.scrollBar.setVisible(this.totalHeight > height - 49);
      this.scrollBar.setScrollRatio(entryHeight, this.totalHeight - (height - 49));
      int xOffset = this.scrollBar.isVisible() ? 4 : 11;
      int yOffset = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -49.0, this.totalHeight - height)) : 49;
      boolean hidden = false;

      for (AbstractAvatarWidget avatar : this.avatarList) {
         if (!hidden) {
            avatar.setX(x + xOffset);
            avatar.setY(y + yOffset);
            if (avatar.getY() + avatar.getHeight() > y + this.scissorsY) {
               avatar.extractRenderState(gui, mouseX, mouseY, delta);
            }

            yOffset += avatar.getHeight() + 2;
            if (yOffset > height) {
               hidden = true;
            }
         }
      }

      gui.disableScissor();
      super.extractRenderState(gui, mouseX, mouseY, delta);
      if (!LocalAvatarFetcher.isLoaded()) {
         UIHelper.renderLoading(gui, x + width / 2, y + height / 2);
      }
   }

   private void loadContents() {
      LocalAvatarFetcher.reloadAvatars();
      HashSet<Path> missingPaths = new HashSet<>(this.avatars.keySet());

      for (LocalAvatarFetcher.AvatarPath avatar : List.copyOf(LocalAvatarFetcher.ALL_AVATARS)) {
         Path path = avatar.getTheActualPathForThis();
         if (avatar.search(this.filter)) {
            AbstractAvatarWidget widget = this.avatars.get(path);
            if (widget != null) {
               widget.update(avatar, this.filter);
            }

            missingPaths.remove(path);
            this.avatars
               .computeIfAbsent(
                  path,
                  p -> {
                     int width = this.getWidth() - 22;
                     AbstractAvatarWidget entry = avatar instanceof LocalAvatarFetcher.FolderPath folder
                        ? new AvatarFolderWidget(0, width, folder, this)
                        : new AvatarWidget(0, width, avatar, this);
                     this.avatarList.add(entry);
                     this.children.add(entry);
                     return entry;
                  }
               );
         }
      }

      for (Path missingPath : missingPaths) {
         AbstractAvatarWidget obj = this.avatars.remove(missingPath);
         this.avatarList.remove(obj);
         this.children.remove(obj);
      }

      this.avatarList.sort(AbstractAvatarWidget::compareTo);
      this.children
         .sort(
            (children1, children2) -> children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2
               ? avatar1.compareTo(avatar2)
               : 0
         );
   }

   public void updateScroll() {
      double pastScroll = (this.totalHeight - this.getHeight()) * this.scrollBar.getScrollProgress();
      this.totalHeight = 2;

      for (AbstractAvatarWidget avatar : this.avatarList) {
         this.totalHeight = this.totalHeight + avatar.getHeight() + 2;
      }

      this.scrollBar.setScrollProgress(pastScroll / (this.totalHeight - this.getHeight()));
   }

   public void scrollToSelected() {
      double y = 0.0;
      this.totalHeight = 0;

      for (AbstractAvatarWidget avatar : this.avatarList) {
         if (avatar.isOf(selectedEntry)) {
            y = this.totalHeight;
         } else {
            this.totalHeight = this.totalHeight + avatar.getHeight() + 2;
         }
      }

      this.scrollBar.setScrollProgressNoAnim(y / this.totalHeight);
   }

   @Override
   public List<? extends GuiEventListener> contents() {
      return this.avatarList;
   }
}
