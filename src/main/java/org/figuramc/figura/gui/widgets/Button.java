package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

public class Button extends net.minecraft.client.gui.components.Button implements FiguraWidget {
   private static final Identifier TEXTURE = FiguraIdentifier.of("textures/gui/button.png");
   protected Integer u;
   protected Integer v;
   protected final Integer textureWidth;
   protected final Integer textureHeight;
   protected final Integer regionSize;
   protected final Identifier texture;
   protected Component tooltip;
   protected Tooltip actualTooltip;
   private boolean hasBackground = true;

   public Button(
      int x,
      int y,
      int width,
      int height,
      Integer u,
      Integer v,
      Integer regionSize,
      Identifier texture,
      Integer textureWidth,
      Integer textureHeight,
      Component text,
      Component tooltip,
      OnPress pressAction
   ) {
      super(x, y, width, height, text, pressAction, DEFAULT_NARRATION);
      this.u = u;
      this.v = v;
      this.regionSize = regionSize;
      this.texture = texture;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.tooltip = tooltip;
   }

   public Button(int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
      this(x, y, width, height, null, null, null, null, null, null, text, tooltip, pressAction);
   }

   public Button(
      int x,
      int y,
      int width,
      int height,
      int u,
      int v,
      int regionSize,
      Identifier texture,
      int textureWidth,
      int textureHeight,
      Component tooltip,
      OnPress pressAction
   ) {
      this(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, Component.empty(), tooltip, pressAction);
   }

   public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         this.setHovered(this.isMouseOver(mouseX, mouseY));
         if (this.texture != null) {
            this.renderTexture(gui, delta);
         } else {
            this.renderDefaultTexture(gui, delta);
         }

         this.renderText(gui, delta);
      }
   }

   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      return this.isHoveredOrFocused() && this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y()) && super.mouseClicked(mouseButtonEvent, bl);
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      boolean over = UIHelper.isMouseOver(this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY);
      if (over && this.tooltip != null) {
         UIHelper.setTooltip(this.tooltip);
      }

      return over;
   }

   protected void renderDefaultTexture(GuiGraphicsExtractor gui, float delta) {
      UIHelper.blitSliced(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getU() * 16.0F, this.getV() * 16.0F, 16, 16, 48, 32, TEXTURE);
   }

   protected void renderTexture(GuiGraphicsExtractor gui, float delta) {
      int u = this.u + this.getU() * this.regionSize;
      int v = this.v + this.getV() * this.regionSize;
      UIHelper.enableBlend();
      int size = this.regionSize;
      gui.blit(
         RenderPipelines.GUI_TEXTURED,
         this.texture,
         this.getX() + this.getWidth() / 2 - size / 2,
         this.getY() + this.getHeight() / 2 - size / 2,
         u,
         v,
         size,
         size,
         this.textureWidth,
         this.textureHeight
      );
   }

   protected void renderText(GuiGraphicsExtractor gui, float delta) {
      UIHelper.renderCenteredScrollingText(gui, this.getMessage(), this.getX() + 1, this.getY(), this.getWidth() - 2, this.getHeight(), this.getTextColor());
   }

   protected void renderVanillaBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      Component message = this.getMessage();
      this.setMessage(Component.empty());
      super.extractDefaultSprite(gui);
      this.setMessage(message);
   }

   protected int getU() {
      if (!this.isActive()) {
         return 0;
      } else {
         return this.isHoveredOrFocused() ? 2 : 1;
      }
   }

   protected int getV() {
      return this.hasBackground ? 0 : 1;
   }

   protected int getTextColor() {
      return (!this.isActive() ? TextColor.DARK_GRAY : TextColor.WHITE).getValue();
   }

   public void setTooltip(Component tooltip) {
      this.tooltip = tooltip;
      this.actualTooltip = Tooltip.create(tooltip);
   }

   public Tooltip getTooltip() {
      return this.actualTooltip;
   }

   public Component tooltip() {
      return this.tooltip;
   }

   public void shouldHaveBackground(boolean bool) {
      this.hasBackground = bool;
   }

   public void setHovered(boolean hovered) {
      this.isHovered = hovered;
   }

   public void run() {
      this.playDownSound(Minecraft.getInstance().getSoundManager());
      this.onPress(null);
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
   public int getX() {
      return super.getX();
   }

   @Override
   public void setX(int x) {
      super.setX(x);
   }

   @Override
   public int getY() {
      return super.getY();
   }

   @Override
   public void setY(int y) {
      super.setY(y);
   }

   @Override
   public int getWidth() {
      return super.getWidth();
   }

   @Override
   public void setWidth(int width) {
      super.setWidth(width);
   }

   @Override
   public int getHeight() {
      return super.getHeight();
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }
}
