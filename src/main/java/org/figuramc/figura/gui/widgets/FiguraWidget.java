package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.components.Renderable;

public interface FiguraWidget extends Renderable {
   boolean isVisible();

   void setVisible(boolean var1);

   int getX();

   void setX(int var1);

   int getY();

   void setY(int var1);

   int getWidth();

   void setWidth(int var1);

   int getHeight();

   void setHeight(int var1);
}
