package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.gui.widgets.EntityPreview;
import org.figuramc.figura.utils.FiguraText;

public class AvatarScreen extends AbstractPanelScreen {
   private final float scale;
   private final float pitch;
   private final float yaw;
   private final LivingEntity entity;

   public AvatarScreen(float scale, float pitch, float yaw, LivingEntity entity, Screen parentScreen) {
      super(parentScreen, FiguraText.of("gui.panels.title.avatar"));
      this.scale = scale;
      this.pitch = pitch;
      this.yaw = yaw;
      this.entity = entity;
   }

   @Override
   public Class<? extends Screen> getSelectedPanel() {
      return (Class<? extends Screen>)this.parentScreen.getClass();
   }

   @Override
   protected void init() {
      super.init();
      this.removeWidget(this.panels);
      EntityPreview widget = new EntityPreview(0, 0, this.width, this.height, this.scale, this.pitch, this.yaw, this.entity, this.parentScreen);
      widget.setToggled(true);
      this.addRenderableWidget(widget);
   }
}
