package org.figuramc.figura.mixin.gui;

import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiEntityRenderState.class)
public abstract class GuiEntityRenderStateMixin implements GuiEntityRenderStateExtension {
   @Unique
   double figura$xPos = 0.0;
   @Unique
   double figura$yPos = 0.0;
   @Unique
   EntityRenderMode figura$renderMode = EntityRenderMode.MINECRAFT_GUI;

   @Override
   public double getXPos() {
      return this.figura$xPos;
   }

   @Override
   public void setXPos(double xPos) {
      this.figura$xPos = xPos;
   }

   @Override
   public double getYPos() {
      return this.figura$yPos;
   }

   @Override
   public void setYPos(double yPos) {
      this.figura$yPos = yPos;
   }

   @Override
   public EntityRenderMode getRenderMode() {
      return this.figura$renderMode;
   }

   @Override
   public void setRenderMode(EntityRenderMode renderMode) {
      this.figura$renderMode = renderMode;
   }
}
