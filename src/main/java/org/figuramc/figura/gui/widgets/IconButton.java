package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.utils.ui.UIHelper;

public class IconButton extends Button {
   public IconButton(
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
      Component text,
      Component tooltip,
      OnPress pressAction
   ) {
      super(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, text, tooltip, pressAction);
   }

   @Override
   protected void renderTexture(GuiGraphicsExtractor gui, float delta) {
      this.renderDefaultTexture(gui, delta);
      UIHelper.enableBlend();
      int size = this.getTextureSize();
      gui.blit(
         RenderPipelines.GUI_TEXTURED,
         this.texture,
         this.getX() + 2,
         this.getY() + (this.getHeight() - size) / 2,
         this.u.intValue(),
         this.v.intValue(),
         size,
         size,
         this.regionSize,
         this.regionSize,
         this.textureWidth,
         this.textureHeight
      );
   }

   @Override
   protected void renderText(GuiGraphicsExtractor gui, float delta) {
      int size = this.getTextureSize();
      UIHelper.renderCenteredScrollingText(
         gui, this.getMessage(), this.getX() + 4 + size, this.getY(), this.getWidth() - 6 - size, this.getHeight(), this.getTextColor()
      );
   }

   protected int getTextureSize() {
      return Math.min(this.getWidth(), this.getHeight()) - 4;
   }
}
