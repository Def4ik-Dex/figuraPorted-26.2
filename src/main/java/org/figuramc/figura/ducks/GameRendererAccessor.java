package org.figuramc.figura.ducks;

import net.minecraft.client.Camera;
import net.minecraft.client.gui.render.GuiRenderer;

public interface GameRendererAccessor {
   double figura$getFov(Camera var1, float var2, boolean var3);

   GuiRenderer figura$getGuiRenderer();

   net.minecraft.client.renderer.SubmitNodeStorage figura$getHandAndScreenSubmitNodeStorage();
}
