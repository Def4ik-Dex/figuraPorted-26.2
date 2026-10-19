package org.figuramc.figura.gui.widgets;

import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class TextField extends AbstractContainerElement {
   public static final Identifier BACKGROUND = FiguraIdentifier.of("textures/gui/text_field.png");
   public static final int ENABLED_COLOR = TextColor.WHITE.getValue();
   public static final int DISABLED_COLOR = TextColor.DARK_GRAY.getValue();
   private final TextField.HintType hint;
   private final EditBox field;
   private int borderColour = -1;
   private boolean enabled = true;

   public TextField(int x, int y, int width, int height, TextField.HintType hint, Consumer<String> changedListener) {
      super(x, y, width, height);
      this.hint = hint;
      this.field = new EditBox(Minecraft.getInstance().font, x + 4, y + (height - 8) / 2, width - 12, height - (height - 8) / 2, Component.empty());
      this.field.setMaxLength(32767);
      this.field.setBordered(false);
      this.field.setResponder(changedListener);
      this.children.add(this.field);
   }

   @Override
   public void tick() {
      super.tick();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         UIHelper.blitSliced(
            gui,
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            !this.isEnabled() ? 0.0F : (this.isMouseOver(mouseX, mouseY) ? 32.0F : 16.0F),
            0.0F,
            16,
            16,
            48,
            16,
            BACKGROUND
         );
         if (this.isFocused()) {
            UIHelper.fillOutline(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.borderColour);
         }

         if (this.hint != null && this.field.getValue().isEmpty() && !this.field.isFocused()) {
            this.renderHint(gui);
         }

         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }

   protected void renderHint(GuiGraphicsExtractor gui) {
      Font font = Minecraft.getInstance().font;
      gui.text(
         font,
         this.hint.hint.copy().append(TextUtils.ELLIPSIS).withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}),
         this.getX() + 4,
         this.getY() + (int)((this.getHeight() - 9 + 1) / 2.0F),
         UIHelper.adjustColor(16777215)
      );
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      if (this.isEnabled() && this.isMouseOver(mouseX, mouseY)) {
         mouseX = Mth.clamp(mouseX, this.field.getX(), this.field.getX() + this.field.getWidth() - 1);
         mouseY = Mth.clamp(mouseY, this.field.getY(), this.field.getY() + this.field.getHeight() - 1);
         return super.mouseClicked(new MouseButtonEvent(mouseX, mouseY, mouseButtonEvent.buttonInfo()), bl);
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
      return !this.field.isFocused();
   }

   @Override
   public void setX(int x) {
      super.setX(x);
      this.field.setX(x + 4);
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.field.setY(y + (this.getHeight() - 8) / 2);
   }

   public void setBorderColour(int borderColour) {
      this.borderColour = borderColour;
   }

   public int getBorderColour() {
      return this.borderColour;
   }

   public EditBox getField() {
      return this.field;
   }

   @Override
   public void updateNarration(NarrationElementOutput output) {
      this.field.updateNarration(output);
   }

   @Override
   public NarrationPriority narrationPriority() {
      return this.field.narrationPriority();
   }

   @Override
   public void setVisible(boolean visible) {
      if (visible != this.isVisible()) {
         super.setVisible(visible);
         this.field.setFocused(false);
      }
   }

   public void setColor(int color) {
      this.field.setTextColor(UIHelper.adjustColor(this.enabled ? color : DISABLED_COLOR));
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      this.setColor(ENABLED_COLOR);
   }

   @Override
   public void setFocused(boolean bl) {
      this.field.setFocused(bl);
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isFocused() {
      return this.isEnabled() && this.field.isFocused();
   }

   public enum HintType {
      ANY,
      INT,
      POSITIVE_INT,
      FLOAT,
      POSITIVE_FLOAT,
      HEX_COLOR,
      FOLDER_PATH,
      IP,
      SEARCH,
      NAME;

      private final Component hint = FiguraText.of("gui.text_hint." + this.name().toLowerCase(Locale.US));
   }
}
