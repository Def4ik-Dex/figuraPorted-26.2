package org.figuramc.figura.gui.widgets.permissions;

import java.util.Locale;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class AbstractPermPackElement extends Button implements Comparable<AbstractPermPackElement>, FiguraWidget {
   protected final PlayerList parent;
   protected final PermissionPack pack;
   protected float scale = 1.0F;

   protected AbstractPermPackElement(int width, int height, PermissionPack pack, PlayerList parent) {
      super(0, 0, width, height, Component.empty(), null, bx -> {});
      this.parent = parent;
      this.pack = pack;
   }

   protected void animate(float delta, boolean anim) {
      if (anim) {
         float lerpDelta = MathUtils.magicDelta(0.2F, delta);
         this.scale = Mth.lerp(lerpDelta, this.scale, 1.2F);
      } else {
         float lerpDelta = MathUtils.magicDelta(0.3F, delta);
         this.scale = Mth.lerp(lerpDelta, this.scale, 1.0F);
      }
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      int width = this.getWidth();
      int height = this.getHeight();
      int x = this.getX() + width / 2;
      int y = this.getY() + height / 2;
      width = (int)(width * (this.scale / 2.0F));
      height = (int)(height * (this.scale / 2.0F));
      return this.parent.isInsideScissors(mouseX, mouseY)
         && this.isActive()
         && this.isVisible()
         && UIHelper.isMouseOver(x - width, y - height, width * 2, height * 2, mouseX, mouseY);
   }

   public void onPress(InputWithModifiers inputWithModifiers) {
      this.parent.selectedEntry = this;
      this.parent.parent.updatePermissions(this.pack);
   }

   public PermissionPack getPack() {
      return this.pack;
   }

   public int compareTo(AbstractPermPackElement other) {
      int len = Permissions.Category.values().length;
      int i;
      if (this instanceof PlayerPermPackElement p && p.dragged) {
         i = Math.min(p.index, len - 1);
      } else {
         i = this.pack.getCategory().index;
      }

      int j;
      if (other instanceof PlayerPermPackElement p && p.dragged) {
         j = Math.min(p.index, len - 1);
      } else {
         j = other.pack.getCategory().index;
      }

      int comp = Integer.compare(i, j);
      if (comp == 0) {
         if (this instanceof CategoryPermPackElement && other instanceof PlayerPermPackElement) {
            return -1;
         }

         if (this instanceof PlayerPermPackElement && other instanceof CategoryPermPackElement) {
            return 1;
         }

         if (this instanceof PlayerPermPackElement player1 && other instanceof PlayerPermPackElement player2) {
            Avatar avatar1 = AvatarManager.getAvatarForPlayer(player1.getOwner());
            Avatar avatar2 = AvatarManager.getAvatarForPlayer(player2.getOwner());
            if (avatar1 != null && avatar2 == null) {
               return -1;
            }

            if (avatar1 == null && avatar2 != null) {
               return 1;
            }

            return player1.getName().toLowerCase(Locale.US).compareTo(player2.getName().toLowerCase(Locale.US));
         }
      }

      return comp;
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      this.pack.setVisible(visible);
   }
}
