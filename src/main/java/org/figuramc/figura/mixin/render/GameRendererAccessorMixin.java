package org.figuramc.figura.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.figuramc.figura.ducks.GameRendererAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRenderer.class)
public abstract class GameRendererAccessorMixin implements GameRendererAccessor {
   @Shadow
   @Final
   private GuiRenderer guiRenderer;
   @Shadow
   @Final
   private SubmitNodeStorage handAndScreenSubmitNodeStorage;

   @Override
   public double figura$getFov(Camera camera, float tickDelta, boolean changingFov) {
      return camera.getFov();
   }

   @Override
   public GuiRenderer figura$getGuiRenderer() {
      return this.guiRenderer;
   }

   @Override
   public SubmitNodeStorage figura$getHandAndScreenSubmitNodeStorage() {
      return this.handAndScreenSubmitNodeStorage;
   }
}
