package org.figuramc.figura.gui.widgets.avatar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.gui.widgets.ContainerButton;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.utils.ui.UIHelper;

public class AvatarFolderWidget extends AbstractAvatarWidget {
   private final HashMap<String, AbstractAvatarWidget> entries = new HashMap<>();
   private final ArrayList<AbstractAvatarWidget> sortedEntries = new ArrayList<>();

   public AvatarFolderWidget(final int depth, int width, LocalAvatarFetcher.FolderPath avatar, AvatarList parent) {
      super(depth, width, 20, avatar, parent);
      final AvatarFolderWidget instance = this;
      this.button = new ContainerButton(parent, this.getX(), this.getY(), width, 20, this.getName(), null, button -> {
         this.toggleEntries(this.showChildren());
         parent.updateScroll();
      }) {
         @Override
         protected void renderText(GuiGraphicsExtractor gui, float delta) {
            int x = this.getX();
            int widthx = this.getWidth();
            int space = Math.max(6 * depth - 2, 0);
            this.setX(x + space);
            this.setWidth(widthx - space);
            super.renderText(gui, delta);
            this.setX(x);
            this.setWidth(widthx);
            if (this.tooltip() == this.getMessage()) {
               this.setTooltip(instance.getName());
            }
         }

         @Override
         public void setHovered(boolean hovered) {
            if (!hovered && UIHelper.getContext() == AvatarFolderWidget.this.context && AvatarFolderWidget.this.context.isVisible()) {
               hovered = true;
            }

            super.setHovered(hovered);
         }
      };
      this.children.add(this.button);
      this.update(avatar, "");
      boolean expanded = avatar.isExpanded();
      ((ContainerButton)this.button).setToggled(expanded);
      this.button.shouldHaveBackground(false);
      this.toggleEntries(expanded);
      parent.updateScroll();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible() && this.button.isVisible()) {
         if (this.showChildren()) {
            int y0 = this.parent.getY() + this.parent.scissorsY;
            int y1 = y0 + this.parent.getHeight() + this.parent.scissorsHeight;

            for (AbstractAvatarWidget value : this.entries.values()) {
               value.button.setVisible(value.getY() < y1 && value.getY() + value.getHeight() > y0);
            }

            super.extractRenderState(gui, mouseX, mouseY, delta);

            for (AbstractAvatarWidget value : this.entries.values()) {
               value.button.setVisible(true);
            }
         } else {
            super.extractRenderState(gui, mouseX, mouseY, delta);
         }
      }
   }

   @Override
   public void update(LocalAvatarFetcher.AvatarPath path, String filter) {
      super.update(path, filter);
      if (path instanceof LocalAvatarFetcher.FolderPath folderPath) {
         for (AbstractAvatarWidget value : this.entries.values()) {
            value.filter = this.filter;
         }

         HashSet<String> missingPaths = new HashSet<>(this.entries.keySet());

         for (LocalAvatarFetcher.AvatarPath child : folderPath.getChildren()) {
            String str = child.getPath() + child.getName();
            if (child.search(filter)) {
               AbstractAvatarWidget childEntry = this.entries.get(str);
               if (childEntry != null) {
                  childEntry.update(child, filter);
               }

               missingPaths.remove(str);
               this.entries
                  .computeIfAbsent(
                     str,
                     s -> {
                        AbstractAvatarWidget entry = child instanceof LocalAvatarFetcher.FolderPath folder
                           ? new AvatarFolderWidget(this.depth + 1, this.getWidth(), folder, this.parent)
                           : new AvatarWidget(this.depth + 1, this.getWidth(), child, this.parent);
                        this.children.add(entry);
                        entry.setVisible(this.showChildren());
                        return entry;
                     }
                  );
            }
         }

         for (String str : missingPaths) {
            this.children.remove(this.entries.remove(str));
         }

         this.sortedEntries.clear();
         this.sortedEntries.addAll(this.entries.values());
         this.children
            .sort(
               (children1, children2) -> children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2
                  ? avatar1.compareTo(avatar2)
                  : 0
            );
         this.sortedEntries.sort(AbstractAvatarWidget::compareTo);
         this.updateHeight();
      }
   }

   public void toggleEntries(boolean toggle) {
      toggle = toggle && this.showChildren();
      this.avatar.setExpanded(toggle);

      for (AbstractAvatarWidget widget : this.entries.values()) {
         widget.setVisible(toggle);
         if (widget instanceof AvatarFolderWidget folder) {
            folder.toggleEntries(toggle);
         }
      }

      this.updateHeight();
   }

   private void updateHeight() {
      int height = 20;
      boolean show = this.showChildren();

      for (AbstractAvatarWidget entry : this.entries.values()) {
         if (entry instanceof AvatarFolderWidget folder) {
            folder.updateHeight();
         }

         if (show) {
            height += entry.getHeight() + 2;
         }
      }

      this.setHeight(height);
   }

   @Override
   public void setX(int x) {
      super.setX(x);

      for (AbstractAvatarWidget widget : this.sortedEntries) {
         if (widget.isVisible()) {
            widget.setX(x);
         }
      }
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      y = 22;

      for (AbstractAvatarWidget widget : this.sortedEntries) {
         if (widget.isVisible()) {
            widget.setY(this.getY() + y);
            y += widget.getHeight() + 2;
         }
      }
   }

   @Override
   public boolean filtered() {
      boolean result = super.filtered();

      for (AbstractAvatarWidget value : this.entries.values()) {
         if (result) {
            break;
         }

         result = value.filtered();
      }

      return result;
   }

   public boolean showChildren() {
      return ((ContainerButton)this.button).isToggled();
   }
}
