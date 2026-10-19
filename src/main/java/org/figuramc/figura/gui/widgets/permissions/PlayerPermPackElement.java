package org.figuramc.figura.gui.widgets.permissions;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class PlayerPermPackElement extends AbstractPermPackElement {
   public static final Identifier UNKNOWN = FiguraIdentifier.of("textures/gui/unknown_portrait.png");
   private static final Identifier BACKGROUND = FiguraIdentifier.of("textures/gui/player_permissions.png");
   private static final Component DC_TEXT = FiguraText.of("gui.permissions.disconnected").withStyle(ChatFormatting.RED);
   private final String name;
   private final Identifier skin;
   private final UUID owner;
   private final ContextMenu context;
   private final Label nameLabel;
   private final PlayerStatusWidget status;
   public boolean disconnected = false;
   public boolean dragged = false;
   public int anchorX;
   public int anchorY;
   public int initialY;
   public int index;

   public PlayerPermPackElement(int width, String name, PermissionPack pack, Identifier skin, UUID owner, PlayerList parent) {
      super(width, 40, pack, parent);
      this.name = name;
      this.skin = skin;
      this.owner = owner;
      this.context = new ContextMenu(this);
      this.nameLabel = new Label(name, 0, 0, 0);
      this.status = new PlayerStatusWidget(0, 0, 70, owner);
      this.generateContext();
   }

   private void generateContext() {
      this.context.addAction(FiguraText.of("gui.context.copy_name"), null, button -> {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getName());
         FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
      });
      this.context.addAction(FiguraText.of("gui.context.copy_uuid"), null, button -> {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getOwner().toString());
         FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
      });
      this.context.addAction(FiguraText.of("gui.context.reload"), null, button -> {
         AvatarManager.reloadAvatar(this.owner);
         FiguraToast.sendToast(FiguraText.of("toast.reload"));
      });
      ContextMenu permissionsContext = new ContextMenu();

      for (Permissions.Category category : Permissions.Category.values()) {
         PermissionPack.CategoryPermissionPack categoryPack = PermissionManager.CATEGORIES.get(category);
         permissionsContext.addAction(categoryPack.getCategoryName(), null, button -> {
            this.pack.setCategory(categoryPack);
            if (this.parent.selectedEntry == this) {
               this.parent.parent.updatePermissions(this.pack);
            }
         });
      }

      this.context.addTab(FiguraText.of("gui.context.set_permissions"), null, permissionsContext);
   }

   public void renderDragged(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      int oX = this.getX();
      int oY = this.getY();
      this.setX(mouseX - (this.anchorX - oX));
      this.setY(mouseY - (this.anchorY - oY) + (this.initialY - oY));
      super.extractRenderState(gui, mouseX, mouseY, delta);
      this.setX(oX);
      this.setY(oY);
   }

   @Override
   public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.dragged) {
         UIHelper.fillRounded(gui, this.getX() - 1, this.getY() - 1, this.getWidth() + 2, this.getHeight() + 2, 1090519039);
      } else {
         Matrix3x2fStack pose = gui.pose();
         int width = this.getWidth();
         int height = this.getHeight();
         pose.pushMatrix();
         float tx = this.getX() + width / 2.0F;
         float ty = this.getY() + height / 2.0F;
         pose.translate(tx, ty);
         pose.scale(this.scale, this.scale);
         this.animate(delta, UIHelper.getContext() == this.context && this.context.isVisible() || this.isMouseOver(mouseX, mouseY) || this.isFocused());
         int x = -width / 2;
         int y = -height / 2;
         mouseX = (int)((mouseX - tx) / this.scale);
         mouseY = (int)((mouseY - ty) / this.scale);
         if (this.parent.selectedEntry == this) {
            ArrayList<PermissionPack> list = new ArrayList<>(PermissionManager.CATEGORIES.values());
            int color = (this.dragged ? list.get(Math.min(this.index, list.size() - 1)) : this.pack).getColor();
            UIHelper.fillRounded(gui, x - 1, y - 1, width + 2, height + 2, color + -16777216);
         }

         UIHelper.renderHalfTexture(gui, x, y, width, height, 174, BACKGROUND);
         Component name = null;
         boolean head = false;
         Avatar avatar = AvatarManager.getAvatarForPlayer(this.owner);
         if (avatar != null) {
            NameplateCustomization custom = avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;
            if (custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1) {
               name = custom.getJson().copy();
            }

            boolean upsideDown = EntityUtils.getEntityByUUID(this.owner) instanceof LivingEntity entity && RenderUtils.isEntityUpsideDown(entity);
            head = avatar.submitPortraitDraw(gui, this.skin, x + 4, y + 4, Math.round(32.0F * this.scale), 64.0F, upsideDown);
         }

         if (!head) {
            if (this.skin != null) {
               UIHelper.enableBlend();
               gui.blit(RenderPipelines.GUI_TEXTURED, this.skin, x + 4, y + 4, 8.0F, 8.0F, 32, 32, 8, 8, 64, 64);
               GlStateManager._enableBlend(0);
               gui.blit(RenderPipelines.GUI_TEXTURED, this.skin, x + 4, y + 4, 40.0F, 8.0F, 32, 32, 8, 8, 64, 64);
               GlStateManager._disableBlend(0);
            } else {
               UIHelper.blit(gui, x + 4, y + 4, 32, 32, UNKNOWN);
            }
         }

         Font font = Minecraft.getInstance().font;
         Component ogName = Component.literal(this.name);
         if (name == null) {
            name = ogName;
         }

         name = TextUtils.replaceInText(name, "\\$\\{name\\}", ogName);
         Component var23 = TextUtils.splitText(name, "\n").get(0);
         Component var24 = Component.empty()
            .append(var23.copy().withStyle(Style.EMPTY.withHoverEvent(new ShowText(Component.literal(this.name + "\n" + this.owner)))));
         Component var25 = Badges.appendBadges(var24, this.owner, false);
         Component badges = Badges.fetchBadges(this.owner);
         if (!badges.getString().isEmpty()) {
            badges = Component.literal(" ").append(badges);
         }

         this.nameLabel.setText(TextUtils.trimToWidthEllipsis(font, var25, width - 44 - font.width(badges), TextUtils.ELLIPSIS).copy().append(badges));
         this.nameLabel.setX(x + 40);
         this.nameLabel.setY(y + 4);
         this.nameLabel.extractRenderState(gui, mouseX, mouseY, delta);
         if (avatar != null && avatar.nbt != null) {
            this.status.tick();
            this.status.setX(x + 40);
            this.status.setY(y + 6 + 9);
            this.status.extractRenderState(gui, mouseX, mouseY, delta);
         }

         int textY = y + height - 9 - 4;
         gui.text(font, this.pack.getCategoryName().append(this.pack.hasChanges() ? "*" : ""), x + 40, textY, UIHelper.adjustColor(16777215));
         if (this.disconnected) {
            gui.text(font, DC_TEXT, x + width - font.width(DC_TEXT) - 4, textY, UIHelper.adjustColor(16777215));
         }

         pose.popMatrix();
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      if (!this.isMouseOver(mouseX, mouseY)) {
         return false;
      }

      if (mouseButtonEvent.button() == 1) {
         this.context.setX((int)mouseX);
         this.context.setY((int)mouseY);
         this.context.setVisible(true);
         UIHelper.setContext(this.context);
         return true;
      }

      if (UIHelper.getContext() == this.context) {
         this.context.setVisible(false);
      }

      return super.mouseClicked(mouseButtonEvent, bl);
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return !this.dragged && super.isMouseOver(mouseX, mouseY);
   }

   public String getName() {
      return this.name;
   }

   public UUID getOwner() {
      return this.owner;
   }

   @Override
   public boolean isVisible() {
      return super.isVisible() && this.pack.isVisible();
   }
}
