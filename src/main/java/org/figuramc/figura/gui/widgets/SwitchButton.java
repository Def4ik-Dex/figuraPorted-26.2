package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class SwitchButton extends Button {
   public static final Identifier SWITCH_TEXTURE = FiguraIdentifier.of("textures/gui/switch.png");
   public static final Component ON = FiguraText.of("gui.on");
   public static final Component OFF = FiguraText.of("gui.off");
   protected boolean toggled = false;
   private boolean defaultTexture = false;
   private boolean underline = true;
   private float headPos = 0.0F;

   public SwitchButton(int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
      super(x, y, width, height, text, tooltip, pressAction);
   }

   public SwitchButton(
      int x,
      int y,
      int width,
      int height,
      int u,
      int v,
      int interactionOffset,
      Identifier texture,
      int textureWidth,
      int textureHeight,
      Component tooltip,
      OnPress pressAction
   ) {
      super(x, y, width, height, u, v, interactionOffset, texture, textureWidth, textureHeight, tooltip, pressAction);
   }

   public SwitchButton(int x, int y, int width, int height, Component text, boolean toggled) {
      super(x, y, width, height, text, null, button -> {});
      this.toggled = toggled;
      this.headPos = toggled ? 20.0F : 0.0F;
      this.defaultTexture = true;
   }

   public void onPress(InputWithModifiers inputWithModifiers) {
      this.toggled = !this.toggled;
      super.onPress(inputWithModifiers);
   }

   @Override
   protected void renderText(GuiGraphicsExtractor gui, float delta) {
      Component text = (Component)(this.toggled && this.underline ? this.getMessage().copy().withStyle(ChatFormatting.UNDERLINE) : this.getMessage());
      int x = this.getX() + 1;
      int width = this.getWidth() - 2;
      if (this.defaultTexture) {
         x += 31;
         width -= 31;
      }

      UIHelper.renderCenteredScrollingText(gui, text, x, this.getY(), width, this.getHeight(), this.getTextColor());
   }

   @Override
   protected void renderDefaultTexture(GuiGraphicsExtractor gui, float delta) {
      if (!this.defaultTexture) {
         super.renderDefaultTexture(gui, delta);
      } else {
         UIHelper.enableBlend();
         int x = this.getX();
         int y = this.getY();
         gui.blit(
            RenderPipelines.GUI_TEXTURED,
            SWITCH_TEXTURE,
            x + 5,
            y + 5,
            10.0F,
            (this.toggled ? 20.0F : 0.0F) + (this.isHoveredOrFocused() ? 10.0F : 0.0F),
            20,
            10,
            20,
            10,
            30,
            40
         );
         this.headPos = (float)Mth.lerp(1.0 - Math.pow(0.2F, delta), this.headPos, this.toggled ? 20.0 : 0.0);
         gui.blit(
            RenderPipelines.GUI_TEXTURED,
            SWITCH_TEXTURE,
            Math.round(x + this.headPos),
            y,
            0.0F,
            this.isHoveredOrFocused() ? 20.0F : 0.0F,
            10,
            20,
            10,
            20,
            30,
            40
         );
      }
   }

   @Override
   protected int getV() {
      return this.isToggled() ? 1 : super.getV();
   }

   public boolean isToggled() {
      return this.toggled;
   }

   public void setToggled(boolean toggled) {
      this.toggled = toggled;
   }

   public void setUnderline(boolean underline) {
      this.underline = underline;
   }
}
