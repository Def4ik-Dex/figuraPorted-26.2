package org.figuramc.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.screens.AvatarScreen;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3f;

public class EntityPreview extends AbstractContainerElement {
   public static final Identifier UNKNOWN = FiguraIdentifier.of("textures/gui/unknown_entity.png");
   public static final Identifier OVERLAY = FiguraIdentifier.of("textures/gui/entity_overlay.png");
   private LivingEntity entity;
   private final float pitch;
   private final float yaw;
   private final float scale;
   private SwitchButton button;
   private boolean isRotating = false;
   private float anchorX = 0.0F;
   private float anchorY = 0.0F;
   private float anchorAngleX = 0.0F;
   private float anchorAngleY = 0.0F;
   private float angleX;
   private float angleY;
   private float scaledValue = 0.0F;
   private float scaledPrecise = 0.0F;
   private static final float SCALE_FACTOR = 1.1F;
   private boolean isDragging = false;
   private int modelX;
   private int modelY;
   private float dragDeltaX;
   private float dragDeltaY;
   private float dragAnchorX;
   private float dragAnchorY;

   public EntityPreview(int x, int y, int width, int height, float scale, float pitch, float yaw, LivingEntity entity, Screen parentScreen) {
      super(x, y, width, height);
      this.scale = scale;
      this.pitch = pitch;
      this.yaw = yaw;
      this.entity = entity;
      this.modelX = width / 2;
      this.modelY = height / 2;
      this.angleX = pitch;
      this.angleY = yaw;
      this.children
         .add(
            this.button = new SwitchButton(
               x + 4, y + 4, 16, 16, 0, 0, 16, FiguraIdentifier.of("textures/gui/expand.png"), 48, 32, FiguraText.of("gui.expand"), bx -> {
                  if (this.button.isToggled()) {
                     Minecraft.getInstance().gui.setScreen(new AvatarScreen(scale, pitch, yaw, this.entity, parentScreen));
                  } else {
                     Minecraft.getInstance().gui.setScreen(parentScreen);
                  }
               }
            )
         );
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         int x = this.getX();
         int y = this.getY();
         int width = this.getWidth();
         int height = this.getHeight();
         if (!this.button.isToggled()) {
            UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
            UIHelper.blit(gui, x + 1, y + 1, width - 2, height, OVERLAY);
         }

         gui.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);
         Matrix3x2fStack pose = gui.pose();
         if (this.entity != null) {
            pose.pushMatrix();
            this.scaledValue = Mth.lerp((float)(1.0 - Math.pow(0.5, delta)), this.scaledValue, this.scaledPrecise);
            UIHelper.drawEntity(
               x + this.modelX,
               y + this.modelY,
               this.scale + this.scaledValue,
               this.angleX,
               this.angleY,
               this.entity,
               gui,
               new Vector3f(),
               EntityRenderMode.FIGURA_GUI,
               x,
               y,
               x + width,
               y + height
            );
            pose.popMatrix();
         } else {
            int s = Math.min(width, height) * 2 / 3;
            UIHelper.enableBlend();
            gui.blit(
               RenderPipelines.GUI_TEXTURED,
               UNKNOWN,
               x + (width - s) / 2,
               y + (height - s) / 2,
               0.0F,
               64 * ((int)(FiguraMod.ticks / 3.0F) % 8),
               s,
               s,
               64,
               64,
               64,
               512
            );
         }

         gui.disableScissor();
         super.extractRenderState(gui, mouseX, mouseY, delta);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      int button = mouseButtonEvent.button();
      if (!this.isVisible() || !this.isMouseOver(mouseX, mouseY)) {
         return false;
      }

      if (super.mouseClicked(mouseButtonEvent, bl)) {
         return true;
      }

      switch (button) {
         case 0:
            this.anchorX = (float)mouseX;
            this.anchorY = (float)mouseY;
            this.anchorAngleX = this.angleX;
            this.anchorAngleY = this.angleY;
            this.isRotating = true;
            return true;
         case 1:
            this.dragDeltaX = (float)mouseX;
            this.dragDeltaY = (float)mouseY;
            this.dragAnchorX = this.modelX;
            this.dragAnchorY = this.modelY;
            this.isDragging = true;
            return true;
         case 2:
            this.isRotating = false;
            this.isDragging = false;
            this.anchorX = 0.0F;
            this.anchorY = 0.0F;
            this.anchorAngleX = 0.0F;
            this.anchorAngleY = 0.0F;
            this.angleX = this.pitch;
            this.angleY = this.yaw;
            this.scaledValue = 0.0F;
            this.scaledPrecise = 0.0F;
            this.modelX = this.getWidth() / 2;
            this.modelY = this.getHeight() / 2;
            return true;
         default:
            return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
      int button = mouseButtonEvent.button();
      if (button == 0) {
         this.isRotating = false;
         return true;
      } else if (button == 1) {
         this.isDragging = false;
         return true;
      } else {
         return super.mouseReleased(mouseButtonEvent);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      if (!this.isRotating) {
         if (this.isDragging) {
            float x = (float)(mouseX - this.dragDeltaX);
            float y = (float)(mouseY - this.dragDeltaY);
            this.modelX = (int)(this.dragAnchorX + x);
            this.modelY = (int)(this.dragAnchorY + y);
            return true;
         } else {
            return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
         }
      } else {
         this.angleX = (float)(this.anchorAngleX + (this.anchorY - mouseY) / (3.0 / Minecraft.getInstance().getWindow().getGuiScale()));
         this.angleY = (float)(this.anchorAngleY - (this.anchorX - mouseX) / (3.0 / Minecraft.getInstance().getWindow().getGuiScale()));
         if (this.angleX >= 360.0F || this.angleX <= -360.0F) {
            this.anchorY = (float)mouseY;
            this.anchorAngleX = 0.0F;
            this.angleX = 0.0F;
         }

         if (this.angleY >= 360.0F || this.angleY <= -360.0F) {
            this.anchorX = (float)mouseX;
            this.anchorAngleY = 0.0F;
            this.angleY = 0.0F;
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
      if (!this.isVisible()) {
         return false;
      }

      if (super.mouseScrolled(mouseX, mouseY, amount, d)) {
         return true;
      }

      float scaleDir = amount + d > 0.0 ? 1.1F : 0.9090909F;
      this.scaledPrecise = (this.scale + this.scaledPrecise) * scaleDir - this.scale;
      return true;
   }

   public void setEntity(LivingEntity entity) {
      this.entity = entity;
   }

   public void setToggled(boolean toggled) {
      this.button.setToggled(toggled);
      this.button.setTooltip(toggled ? FiguraText.of("gui.minimise") : FiguraText.of("gui.expand"));
   }
}
