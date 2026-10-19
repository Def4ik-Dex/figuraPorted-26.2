package org.figuramc.figura.gui.widgets.lists;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.IconButton;
import org.figuramc.figura.gui.widgets.ScrollBarWidget;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class NetworkFilterList extends AbstractList {
   private static final int LIST_ELEMENT_Y_GAP = 4;
   private final ConfigType.NetworkFilterConfig config;
   private final List<NetworkFilterList.NetworkFilterEntry> entries = new ArrayList<>();
   private final Button addEntryButton;
   private final TextField searchTextField;
   private int scroll;
   private int listElementsHeightDifference;

   public NetworkFilterList(int x, int y, int width, int height, ConfigType.NetworkFilterConfig config) {
      super(x, y, width, height);
      this.scrollBar.setVisible(true);
      this.scrollBar.setHeight(height - 56);
      this.scrollBar.setY(y + 28);
      this.scrollBar.setAction(this::onScroll);
      this.config = config;
      this.children
         .add(
            this.addEntryButton = new Button(
               x + 4, y + height - 24, width - 8, 20, FiguraText.of("gui.network_filter.list.add_filter_entry"), null, this::onEntryAddClick
            )
         );

      for (NetworkingAPI.Filter filter : config.getFilters()) {
         NetworkFilterList.NetworkFilterEntry entry;
         this.entries.add(entry = new NetworkFilterList.NetworkFilterEntry(this, x, y, width - 8 - this.scrollBar.getWidth(), 20, filter));
         this.children.add(entry);
      }

      this.children.add(this.searchTextField = new TextField(x + 4, y + 4, width - 8, 20, TextField.HintType.SEARCH, this::onSearch));
      this.repositionContents();
      this.updateContentsHeightDiff();
      this.updateScrollBar();
   }

   private void onSearch(String s) {
      if (!s.isBlank() && !s.isEmpty()) {
         this.entries.forEach(e -> e.setVisible(e.getSourceFilter().getSource().contains(s)));
      } else {
         this.entries.forEach(e -> e.setVisible(true));
      }

      this.repositionContents();
      this.updateContentsHeightDiff();
      this.updateScrollBar();
   }

   private void onScroll(ScrollBarWidget scrollBarWidget) {
      this.scroll = (int)(scrollBarWidget.getScrollProgress() * this.listElementsHeightDifference);
      this.repositionContents();
   }

   private void onDelete(NetworkFilterList.NetworkFilterEntry networkFilterEntry) {
      this.entries.remove(networkFilterEntry);
      this.children.remove(networkFilterEntry);
      this.config.getFilters().remove(networkFilterEntry.getSourceFilter());
      this.repositionContents();
      this.updateContentsHeightDiff();
      this.updateScrollBar();
   }

   private void onEntryAddClick(net.minecraft.client.gui.components.Button button) {
      NetworkFilterList.NetworkFilterEntry entry = new NetworkFilterList.NetworkFilterEntry(
         this, this.getX() + 4, this.entries.size() * 24, this.getWidth() - 22, 20
      );
      this.entries.add(entry);
      this.children.add(entry);
      this.config.getFilters().add(entry.getSourceFilter());
      this.repositionContents();
      this.updateContentsHeightDiff();
      this.updateScrollBar();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
      super.extractRenderState(gui, mouseX, mouseY, delta);
      this.updateScissors(4, 28, -18, -56);
      this.enableScissors(gui);

      for (NetworkFilterList.NetworkFilterEntry entry : this.contents()) {
         if (entry.isVisible()) {
            entry.extractRenderState(gui, mouseX, mouseY, delta);
         }
      }

      gui.disableScissor();
   }

   @Override
   public void setWidth(int width) {
      super.setWidth(width);
      this.repositionChildren();
   }

   @Override
   public void setHeight(int height) {
      super.setHeight(height);
      this.repositionChildren();
      this.updateContentsHeightDiff();
      this.updateScrollBar();
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.repositionChildren();
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.repositionChildren();
   }

   private void updateScrollBar() {
      if (!this.entries.isEmpty() && this.listElementsHeightDifference > 0) {
         this.scrollBar.setVisible(true);
         this.scrollBar.setScrollProgress(Math.min(1.0, (double)this.scroll / this.listElementsHeightDifference));
         this.scrollBar.setScrollRatio(20.0, this.listElementsHeightDifference);
      } else {
         this.scrollBar.setVisible(false);
      }
   }

   private void repositionChildren() {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      this.scrollBar.setX(x + width - 14);
      this.scrollBar.setY(y + 28);
      this.scrollBar.setHeight(height - 56);
      this.addEntryButton.setX(x + 4);
      this.addEntryButton.setY(y + height - 24);
      this.addEntryButton.setWidth(width - 8);
      this.searchTextField.setX(x + 4);
      this.searchTextField.setY(y + 4);
      this.searchTextField.setWidth(width - 8);
      this.repositionContents();
   }

   private void updateContentsHeightDiff() {
      int listElementsTotalHeight = 0;

      for (NetworkFilterList.NetworkFilterEntry entry : this.entries) {
         if (entry.isVisible()) {
            listElementsTotalHeight += 24;
         }
      }

      if (listElementsTotalHeight > 0) {
         listElementsTotalHeight -= 4;
      }

      this.listElementsHeightDifference = Math.max(0, listElementsTotalHeight - (this.getHeight() - 60));
   }

   private void repositionContents() {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int yOffset = 28 - this.scroll;

      for (NetworkFilterList.NetworkFilterEntry entry : this.contents()) {
         if (entry.isVisible()) {
            entry.setX(x + 4);
            entry.setY(y + yOffset);
            entry.setWidth(width - 22);
            yOffset += 24;
         }
      }
   }

   @Override
   public List<NetworkFilterList.NetworkFilterEntry> contents() {
      return this.entries;
   }

   public static class NetworkFilterEntry extends AbstractContainerElement {
      private static final Identifier deleteButtonLocation = Identifier.fromNamespaceAndPath("figura", "textures/gui/delete.png");
      private final NetworkingAPI.Filter sourceFilter;
      private final IconButton deleteButton;
      private final TextField filterTextField;
      private final NetworkFilterList parent;

      public NetworkFilterEntry(NetworkFilterList parent, int x, int y, int width, int height) {
         this(parent, x, y, width, height, new NetworkingAPI.Filter("example.com"));
      }

      public NetworkFilterEntry(NetworkFilterList parent, int x, int y, int width, int height, NetworkingAPI.Filter sourceFilter) {
         super(x, y, width, height);
         this.parent = parent;
         this.sourceFilter = sourceFilter;
         this.children.add(this.filterTextField = new TextField(x, y, width - 118, 20, TextField.HintType.IP, this::onSourceChange));
         this.children
            .add(
               this.deleteButton = new IconButton(
                  x + width - 20, y, 20, 20, 0, 0, 24, deleteButtonLocation, 72, 24, FiguraText.of("gui.network_filter.list.delete"), null, this::onDelete
               )
            );
         this.filterTextField.getField().setValue(sourceFilter.getSource());
      }

      private void onDelete(net.minecraft.client.gui.components.Button button) {
         this.parent.onDelete(this);
      }

      @Override
      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         super.extractRenderState(gui, mouseX, mouseY, delta);
      }

      @Override
      public int getHeight() {
         return 20;
      }

      private void onSourceChange(String s) {
         this.sourceFilter.setSource(s);
      }

      private void repositionChildren() {
         int x = this.getX();
         int y = this.getY();
         int width = this.getWidth();
         this.filterTextField.setX(x);
         this.filterTextField.setY(y);
         this.filterTextField.setWidth(width - 118);
         this.deleteButton.setX(x + width - 20);
         this.deleteButton.setY(y);
      }

      @Override
      public void setX(int x) {
         super.setX(x);
         this.repositionChildren();
      }

      @Override
      public void setY(int y) {
         super.setY(y);
         this.repositionChildren();
      }

      @Override
      public void setWidth(int width) {
         super.setWidth(width);
         this.repositionChildren();
      }

      @Override
      public void setHeight(int height) {
         super.setHeight(height);
         this.repositionChildren();
      }

      public NetworkingAPI.Filter getSourceFilter() {
         return this.sourceFilter;
      }
   }
}
