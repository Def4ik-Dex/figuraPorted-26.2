package org.figuramc.figura.gui.widgets;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class SearchBar extends TextField {
   public static final Identifier CLEAR_TEXTURE = FiguraIdentifier.of("textures/gui/search_clear.png");
   public static final Component SEARCH_ICON = Component.literal("\ud83d\udd0e")
      .withStyle(Style.EMPTY.withFont(new Resource(UIHelper.UI_FONT)).applyFormats(new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
   private final Button clearButton = new Button(
      this.getX() + this.getWidth() - 18,
      this.getY() + (this.getHeight() - 16) / 2,
      16,
      16,
      0,
      0,
      16,
      CLEAR_TEXTURE,
      48,
      16,
      FiguraText.of("gui.clear"),
      button -> {
         this.getField().setValue("");
         this.setFocused(false);
      }
   );

   public SearchBar(int x, int y, int width, int height, Consumer<String> changedListener) {
      super(x, y, width, height, TextField.HintType.SEARCH, changedListener);
      this.children.add(this.clearButton);
      this.getField().setWidth(this.getField().getWidth() - 16);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      this.clearButton.setVisible(!this.getField().getValue().isEmpty());
      super.extractRenderState(gui, mouseX, mouseY, delta);
   }

   @Override
   protected void renderHint(GuiGraphicsExtractor gui) {
      super.renderHint(gui);
      Font font = Minecraft.getInstance().font;
      gui.text(
         font,
         SEARCH_ICON,
         this.getX() + this.getWidth() - font.width(SEARCH_ICON) - 4,
         this.getY() + (int)((this.getHeight() - 9 + 1) / 2.0F),
         UIHelper.adjustColor(16777215)
      );
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      return (!this.clearButton.isVisible() || !this.clearButton.mouseClicked(mouseButtonEvent, bl)) && super.mouseClicked(mouseButtonEvent, bl);
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return (!this.clearButton.isVisible() || !this.clearButton.isMouseOver(mouseX, mouseY)) && super.isMouseOver(mouseX, mouseY);
   }
}
