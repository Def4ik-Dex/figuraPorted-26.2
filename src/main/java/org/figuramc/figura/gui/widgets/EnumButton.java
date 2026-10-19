package org.figuramc.figura.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class EnumButton extends AbstractContainerElement {
   private final Button button;
   private int value;
   private final ContextMenu contextMenu;
   private final Consumer<Integer> onSelect;
   private final ArrayList<Component> names = new ArrayList<>();

   public EnumButton(int x, int y, int width, int height, String translationString, int defaultValue, int length, Consumer<Integer> onSelect) {
      super(x, y, width, height);
      this.value = defaultValue;
      ArrayList<Component> tooltips = new ArrayList<>();

      for (int i = 0; i < length; i++) {
         this.names.add(FiguraText.of("%s.%s".formatted(translationString, i)));
         tooltips.add(FiguraText.of("%s.%s.tooltip".formatted(translationString, i)));
      }

      this.children.add(this.button = new Button(x, y, width, height, this.names.get(this.value % length), tooltips.get(this.value % length), this::onClick));
      this.contextMenu = new ContextMenu();
      this.contextMenu.setX(this.button.getX());
      this.contextMenu.setY(this.button.getY() + this.button.getHeight());
      this.onSelect = onSelect;

      for (int i = 0; i < length; i++) {
         Component c = this.names.get(i);
         Component t = tooltips.get(i);
         int finalI = i;
         this.contextMenu.addAction(c, t, b -> {
            this.button.setMessage(c);
            this.button.setTooltip(t);
            onSelect.accept(finalI);
            this.updateContextText();
            this.value = finalI;
         });
      }
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.button.setX(x);
      this.contextMenu.setX(this.button.getX());
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.button.setY(y);
      this.contextMenu.setY(this.button.getY() + this.button.getHeight());
   }

   @Override
   public void setWidth(int width) {
      super.setWidth(width);
      this.button.setWidth(width);
   }

   @Override
   public void setHeight(int height) {
      super.setHeight(height);
      this.button.setHeight(height);
      this.contextMenu.setY(this.button.getY() + this.button.getHeight());
   }

   private void updateContextText() {
      List<? extends AbstractWidget> entries = this.contextMenu.getEntries();

      for (int i = 0; i < this.names.size(); i++) {
         Component text = this.names.get(i);
         if (i == this.value % this.names.size()) {
            text = Component.empty().setStyle(FiguraMod.getAccentColor()).withStyle(ChatFormatting.UNDERLINE).append(text);
         }

         entries.get(i).setMessage(text);
      }
   }

   private void onClick(net.minecraft.client.gui.components.Button button) {
      this.contextMenu.setVisible(!this.contextMenu.isVisible());
      if (this.contextMenu.isVisible()) {
         UIHelper.setContext(this.contextMenu);
      }
   }
}
