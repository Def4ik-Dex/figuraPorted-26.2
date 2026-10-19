package org.figuramc.figura.ducks;

import org.figuramc.figura.model.rendering.EntityRenderMode;

public interface GuiEntityRenderStateExtension {
   double getXPos();

   void setXPos(double var1);

   double getYPos();

   void setYPos(double var1);

   EntityRenderMode getRenderMode();

   void setRenderMode(EntityRenderMode var1);
}
