package org.figuramc.figura.gui.widgets.permissions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class CategoryPermPackElement extends AbstractPermPackElement {
   private static final Identifier BACKGROUND = FiguraIdentifier.of("textures/gui/group_permissions.png");
   private boolean enabled;

   public CategoryPermPackElement(int width, PermissionPack pack, PlayerList parent) {
      super(width, 20, pack, parent);
      this.enabled = pack.isVisible();
   }

   @Override
   public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      Matrix3x2fStack pose = gui.pose();
      int width = this.getWidth();
      int height = this.getHeight();
      pose.pushMatrix();
      pose.translate(this.getX() + width / 2.0F, this.getY() + height / 2.0F);
      pose.scale(this.scale, this.scale);
      this.animate(delta, this.isMouseOver(mouseX, mouseY) || this.isFocused());
      int x = -width / 2;
      int y = -height / 2;
      if (this.parent.selectedEntry == this) {
         UIHelper.fillRounded(gui, x - 1, y - 1, width + 2, height + 2, -1);
      }

      UIHelper.renderHalfTexture(gui, x, y, width, height, 0.0F, this.enabled ? 20.0F : 0.0F, 174, 20, 174, 40, BACKGROUND);
      Component text = this.pack.getCategoryName().append(this.pack.hasChanges() ? "*" : "");
      Font font = Minecraft.getInstance().font;
      UIHelper.renderOutlineText(gui, font, text, x + width / 2 - font.width(text) / 2, y + height / 2 - 9 / 2, 16777215, 0);
      pose.popMatrix();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      return this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y()) && super.mouseClicked(mouseButtonEvent, bl);
   }

   @Override
   public void onPress(InputWithModifiers inputWithModifiers) {
      if (this.parent.selectedEntry == this) {
         this.enabled = !this.enabled;
         this.pack.setVisible(this.enabled);
         this.parent.updateScroll();
      }

      super.onPress(inputWithModifiers);
   }

   @Override
   public boolean isVisible() {
      return true;
   }
}
