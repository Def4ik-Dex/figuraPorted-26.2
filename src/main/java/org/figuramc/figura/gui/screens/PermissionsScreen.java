package org.figuramc.figura.gui.screens;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.EntityPreview;
import org.figuramc.figura.gui.widgets.SliderWidget;
import org.figuramc.figura.gui.widgets.SwitchButton;
import org.figuramc.figura.gui.widgets.lists.PermissionsList;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.gui.widgets.permissions.AbstractPermPackElement;
import org.figuramc.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class PermissionsScreen extends AbstractPanelScreen {
   private PlayerList playerList;
   private EntityPreview entityWidget;
   private SliderWidget slider;
   private PermissionsList permissionsList;
   private SwitchButton expandButton;
   private Button reloadAll;
   private Button back;
   private Button resetButton;
   private SwitchButton precisePermissions;
   private float listYPrecise;
   private float expandYPrecise;
   private float resetYPrecise;
   private boolean expanded;
   private PlayerPermPackElement dragged = null;

   public PermissionsScreen(Screen parentScreen) {
      super(parentScreen, FiguraText.of("gui.panels.title.permissions"));
   }

   @Override
   protected void init() {
      super.init();
      int middle = this.width / 2;
      int listWidth = Math.min(middle - 6, 208);
      int lineHeight = 9;
      int entitySize = (int)Math.min(this.height - 95 - lineHeight * 1.5 - (FiguraMod.debugModeEnabled() ? 24 : 0), listWidth);
      int modelSize = 11 * entitySize / 29;
      int entityX = Math.max(middle + (listWidth - entitySize) / 2 + 1, middle + 2);
      this.entityWidget = new EntityPreview(entityX, 28, entitySize, entitySize, modelSize, -15.0F, 30.0F, Minecraft.getInstance().player, this);
      this.slider = new SliderWidget(
         middle + 2, (int)(this.entityWidget.getY() + this.entityWidget.getHeight() + lineHeight * 1.5 + 20.0), listWidth, 11, 1.0, 5, true
      ) {
         @Override
         public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
            super.extractWidgetRenderState(gui, mouseX, mouseY, delta);
            PermissionPack selectedPack = PermissionsScreen.this.playerList.selectedEntry.getPack();
            MutableComponent text = selectedPack.getCategoryName();
            int x = (int)(this.getX() + this.getWidth() / 2.0F - PermissionsScreen.this.font.width(text) * 0.75F);
            int y = this.getY() - 4 - 9 * 2;
            Matrix3x2fStack pose = gui.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(1.5F, 1.5F);
            UIHelper.renderOutlineText(gui, PermissionsScreen.this.font, text, 0, 0, 16777215, 2105376);
            pose.popMatrix();
            MutableComponent info = Component.literal("?").withStyle(Style.EMPTY.withFont(new Resource(UIHelper.UI_FONT)));
            int color = 4210752;
            int width = PermissionsScreen.this.font.width(info);
            x = Math.min(
               (int)(x + PermissionsScreen.this.font.width(text) * 1.5F + PermissionsScreen.this.font.width("  ")), PermissionsScreen.this.width - width
            );
            y = (int)(y + 9.0F * 0.25F);
            if (UIHelper.isMouseOver(x, y, width, 9, mouseX, mouseY)) {
               color = 16777215;
               UIHelper.setTooltip(selectedPack.getCategory().info);
            }

            color = UIHelper.adjustColor(color);
            gui.text(PermissionsScreen.this.font, info, x, y, color);
         }
      };
      this.permissionsList = new PermissionsList(middle + 2, this.height, listWidth, this.height - 54);
      this.addRenderableWidget(this.playerList = new PlayerList(middle - listWidth - 2, 28, listWidth, this.height - 32, this));
      this.addRenderableWidget(this.entityWidget);
      this.addRenderableWidget(this.slider);
      int bottomButtonsWidth = (listWidth - 24) / 2 - 2;
      this.addRenderableWidget(
         this.reloadAll = new Button(middle + 2, this.height - 24, bottomButtonsWidth, 20, FiguraText.of("gui.permissions.reload_all"), null, bx -> {
            AvatarManager.clearAllAvatars();
            FiguraToast.sendToast(FiguraText.of("toast.reload_all"));
         })
      );
      this.addRenderableWidget(
         this.back = new Button(
            middle + 6 + bottomButtonsWidth, this.height - 24, bottomButtonsWidth, 20, FiguraText.of("gui.done"), null, bx -> this.onClose()
         )
      );
      this.addRenderableWidget(
         this.expandButton = new SwitchButton(
            middle + listWidth - 18,
            this.height - 24,
            20,
            20,
            0,
            0,
            20,
            FiguraIdentifier.of("textures/gui/expand_v.png"),
            60,
            40,
            FiguraText.of("gui.permissions.expand_permissions.tooltip"),
            btn -> {
               this.expanded = this.expandButton.isToggled();
               this.entityWidget.setVisible(!this.expanded);
               this.slider.setVisible(!this.expanded);
               this.slider.setActive(!this.expanded);
               this.reloadAll.setVisible(!this.expanded);
               this.back.setVisible(!this.expanded);
               this.expandButton
                  .setTooltip(
                     this.expanded
                        ? FiguraText.of("gui.permissions.minimize_permissions.tooltip")
                        : FiguraText.of("gui.permissions.expand_permissions.tooltip")
                  );
               this.resetButton.setActive(this.expanded);
            }
         )
      );
      this.addRenderableWidget(this.resetButton = new Button(middle + 2, this.height, 60, 20, FiguraText.of("gui.permissions.reset"), null, btn -> {
         PermissionPack pack = this.playerList.selectedEntry.getPack();
         pack.clear();
         this.updatePermissions(pack);
      }));
      this.addRenderableWidget(
         this.precisePermissions = new SwitchButton(middle + 66, this.height, listWidth - 88, 20, FiguraText.of("gui.permissions.precise"), false) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
               PermissionsScreen.this.permissionsList.precise = this.isToggled();
               PermissionsScreen.this.permissionsList.updateList(PermissionsScreen.this.playerList.selectedEntry.getPack());
            }
         }
      );
      this.precisePermissions.setUnderline(false);
      this.addRenderableWidget(this.permissionsList);
      this.listYPrecise = this.permissionsList.getY();
      this.expandYPrecise = this.expandButton.getY();
      this.resetYPrecise = this.resetButton.getY();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      AbstractPermPackElement entity = this.playerList.selectedEntry;
      Level world = Minecraft.getInstance().level;
      if (world != null && entity instanceof PlayerPermPackElement player) {
         this.entityWidget.setEntity(world.getPlayerByUUID(UUID.fromString(player.getPack().name)));
      } else {
         this.entityWidget.setEntity(null);
      }

      float lerpDelta = MathUtils.magicDelta(0.6F, delta);
      this.listYPrecise = Mth.lerp(lerpDelta, this.listYPrecise, this.expandButton.isToggled() ? 50.0F : this.height + 1);
      this.permissionsList.setY((int)this.listYPrecise);
      this.expandYPrecise = Mth.lerp(lerpDelta, this.expandYPrecise, this.expandButton.isToggled() ? this.listYPrecise - 22.0F : this.listYPrecise - 24.0F);
      this.expandButton.setY((int)this.expandYPrecise);
      this.resetYPrecise = Mth.lerp(lerpDelta, this.resetYPrecise, this.expandButton.isToggled() ? this.listYPrecise - 22.0F : this.height);
      this.resetButton.setY((int)this.resetYPrecise);
      this.precisePermissions.setY((int)this.resetYPrecise);
      super.extractRenderState(gui, mouseX, mouseY, delta);
   }

   @Override
   public void renderOverlays(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.dragged != null && this.dragged.dragged) {
         this.dragged.renderDragged(gui, mouseX, mouseY, delta);
      }

      super.renderOverlays(gui, mouseX, mouseY, delta);
   }

   public void removed() {
      PermissionManager.saveToDisk();
      super.removed();
   }

   @Override
   public boolean keyPressed(KeyEvent keyEvent) {
      if (keyEvent.key() == 256 && this.expandButton.isToggled()) {
         this.expandButton.onPress(keyEvent);
         return true;
      } else {
         return super.keyPressed(keyEvent);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      boolean bool = super.mouseClicked(mouseButtonEvent, bl);
      this.dragged = null;
      double mouseX = mouseButtonEvent.x();
      double mouseY = mouseButtonEvent.y();
      if (mouseButtonEvent.button() == 0 && this.playerList.selectedEntry instanceof PlayerPermPackElement element && element.isMouseOver(mouseX, mouseY)) {
         this.dragged = element;
         element.anchorX = (int)mouseX;
         element.anchorY = (int)mouseY;
         element.initialY = element.getY();
      }

      return bool;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
      if (this.dragged != null) {
         this.dragged.index = this.playerList.getCategoryAt(mouseButtonEvent.y());
         this.dragged.dragged = true;
         return true;
      } else {
         return super.mouseDragged(mouseButtonEvent, d, e);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
      boolean bool = super.mouseReleased(mouseButtonEvent);
      if (this.dragged != null && this.dragged.dragged) {
         PermissionPack pack = this.dragged.getPack();
         Permissions.Category category = Permissions.Category.indexOf(Math.min(this.dragged.index, Permissions.Category.values().length - 1));
         pack.setCategory(PermissionManager.CATEGORIES.get(category));
         this.updatePermissions(pack);
         this.dragged.dragged = false;
         this.dragged = null;
         return bool;
      } else {
         return bool;
      }
   }

   public void updatePermissions(PermissionPack pack) {
      this.slider.setAction(null);
      this.slider.setActive(pack instanceof PermissionPack.PlayerPermissionPack && !this.expanded);
      this.slider.setMax(Permissions.Category.values().length);
      this.slider.setScrollProgress(pack.getCategory().index / (this.slider.getMax() - 1.0));
      this.slider.setAction(scroll -> {
         Permissions.Category category = Permissions.Category.indexOf(((SliderWidget)scroll).getIntValue());
         pack.setCategory(PermissionManager.CATEGORIES.get(category));
         this.permissionsList.updateList(pack);
      });
      this.permissionsList.updateList(pack);
   }
}
