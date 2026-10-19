package org.figuramc.figura.gui.widgets.lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.screens.PermissionsScreen;
import org.figuramc.figura.gui.widgets.SearchBar;
import org.figuramc.figura.gui.widgets.SwitchButton;
import org.figuramc.figura.gui.widgets.permissions.AbstractPermPackElement;
import org.figuramc.figura.gui.widgets.permissions.CategoryPermPackElement;
import org.figuramc.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class PlayerList extends AbstractList {
   private final HashMap<UUID, PlayerPermPackElement> players = new HashMap<>();
   private final HashSet<UUID> missingPlayers = new HashSet<>();
   private final ArrayList<AbstractPermPackElement> permissionsList = new ArrayList<>();
   public final PermissionsScreen parent;
   private final SearchBar searchBar;
   private final SwitchButton showFigura;
   private final SwitchButton showDisconnected;
   private static boolean showFiguraBl;
   private static boolean showDisconnectedBl;
   private final int entryWidth;
   private int totalHeight = 0;
   private AbstractPermPackElement maxCategory;
   public AbstractPermPackElement selectedEntry;
   private String filter = "";

   public PlayerList(int x, int y, int width, int height, PermissionsScreen parent) {
      super(x, y, width, height);
      this.updateScissors(1, 24, -2, -25);
      this.parent = parent;
      this.entryWidth = Math.min(width - this.scrollBar.getWidth() - 12, 174);
      this.scrollBar.setY(y + 28);
      this.scrollBar.setHeight(height - 32);
      this.children.add(this.searchBar = new SearchBar(x + 4, y + 4, width - 56, 20, s -> {
         if (!this.filter.equals(s)) {
            this.scrollBar.setScrollProgress(0.0);
         }

         this.filter = s;
      }));
      this.children
         .add(
            this.showFigura = new SwitchButton(
               x + width - 48,
               y + 4,
               20,
               20,
               0,
               0,
               20,
               FiguraIdentifier.of("textures/gui/show_figura.png"),
               60,
               40,
               FiguraText.of("gui.permissions.figura_only.tooltip"),
               button -> showFiguraBl = ((SwitchButton)button).isToggled()
            )
         );
      this.showFigura.setToggled(showFiguraBl);
      this.children
         .add(
            this.showDisconnected = new SwitchButton(
               x + width - 24,
               y + 4,
               20,
               20,
               0,
               0,
               20,
               FiguraIdentifier.of("textures/gui/show_disconnected.png"),
               60,
               40,
               FiguraText.of("gui.permissions.disconnected.tooltip"),
               button -> showDisconnectedBl = ((SwitchButton)button).isToggled()
            )
         );
      this.showDisconnected.setToggled(showDisconnectedBl);
      this.loadGroups();
      this.loadPlayers();
      this.selectLocalPlayer();
   }

   @Override
   public void tick() {
      this.loadPlayers();
      super.tick();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
      this.totalHeight = 0;

      for (AbstractPermPackElement pack : this.permissionsList) {
         if (pack.isVisible()) {
            this.totalHeight = this.totalHeight + pack.getHeight() + 8;
         }
      }

      boolean hasScrollbar = this.totalHeight > height - 32;
      this.scrollBar.setVisible(hasScrollbar);
      this.scrollBar
         .setScrollRatio(this.permissionsList.isEmpty() ? 0.0 : (float)this.totalHeight / this.permissionsList.size(), this.totalHeight - (height - 32));
      this.scissorsWidth = hasScrollbar ? -this.scrollBar.getWidth() - 5 : -2;
      this.enableScissors(gui);
      int xOffset = (width - this.entryWidth - (this.scrollBar.isVisible() ? 13 : 0)) / 2;
      int playerY = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -32.0, this.totalHeight - height)) : 32;
      int minY = y + this.scissorsY;
      int maxY = minY + height + this.scissorsHeight;

      for (AbstractPermPackElement pack : this.permissionsList) {
         if (pack.isVisible()) {
            pack.setX(x + xOffset);
            pack.setY(y + playerY);
            if (pack.getY() + pack.getHeight() > minY && pack.getY() < maxY) {
               pack.extractRenderState(gui, mouseX, mouseY, delta);
            }

            playerY += pack.getHeight() + 8;
         }
      }

      gui.disableScissor();
      super.extractRenderState(gui, mouseX, mouseY, delta);
   }

   @Override
   public List<? extends GuiEventListener> contents() {
      return this.permissionsList;
   }

   private void loadGroups() {
      for (PermissionPack container : PermissionManager.CATEGORIES.values()) {
         CategoryPermPackElement group = new CategoryPermPackElement(this.entryWidth, container, this);
         this.permissionsList.add(group);
         this.children.add(group);
         this.maxCategory = group;
      }
   }

   private void loadPlayers() {
      this.missingPlayers.clear();
      this.missingPlayers.addAll(this.players.keySet());
      ClientPacketListener connection = Minecraft.getInstance().getConnection();
      List<UUID> playerList = connection == null ? List.of() : new ArrayList<>(connection.getOnlinePlayerIds());

      for (UUID uuid : playerList) {
         PlayerInfo player = connection.getPlayerInfo(uuid);
         if (player != null) {
            String name = player.getProfile().name();
            Identifier skin = player.getSkin().body().texturePath();
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            if ((name.toLowerCase(Locale.US).contains(this.filter.toLowerCase(Locale.US)) || uuid.toString().contains(this.filter.toLowerCase(Locale.US)))
               && (!this.showFigura.isToggled() || FiguraMod.isLocal(uuid) || avatar != null && avatar.nbt != null)) {
               this.missingPlayers.remove(uuid);
               PlayerPermPackElement element = this.players.computeIfAbsent(uuid, uuid1 -> {
                  PlayerPermPackElement entryx = new PlayerPermPackElement(this.entryWidth, name, PermissionManager.get(uuid1), skin, uuid1, this);
                  this.permissionsList.add(entryx);
                  this.children.add(entryx);
                  return entryx;
               });
               element.disconnected = false;
            }
         }
      }

      if (this.filter.isEmpty() && this.showDisconnected.isToggled()) {
         for (Avatar avatar : AvatarManager.getLoadedAvatars()) {
            UUID id = avatar.owner;
            if (!playerList.contains(id)) {
               this.missingPlayers.remove(id);
               PlayerPermPackElement element = this.players.computeIfAbsent(id, uuid -> {
                  PlayerPermPackElement entryx = new PlayerPermPackElement(this.entryWidth, avatar.entityName, PermissionManager.get(uuid), null, uuid, this);
                  this.permissionsList.add(entryx);
                  this.children.add(entryx);
                  return entryx;
               });
               element.disconnected = true;
            }
         }
      }

      for (UUID missingID : this.missingPlayers) {
         PlayerPermPackElement entry = this.players.remove(missingID);
         this.permissionsList.remove(entry);
         this.children.remove(entry);
      }

      this.sortList();
      if (this.selectedEntry instanceof PlayerPermPackElement player && this.missingPlayers.contains(player.getOwner())) {
         this.selectLocalPlayer();
      }
   }

   private void sortList() {
      this.permissionsList.sort(AbstractPermPackElement::compareTo);
      this.children
         .sort(
            (element1, element2) -> element1 instanceof AbstractPermPackElement container1 && element2 instanceof AbstractPermPackElement container2
               ? container1.compareTo(container2)
               : 0
         );
   }

   private void selectLocalPlayer() {
      PlayerPermPackElement local = Minecraft.getInstance().player != null ? this.players.get(Minecraft.getInstance().player.getUUID()) : null;
      if (local != null) {
         local.onPress(null);
      } else {
         this.maxCategory.onPress(null);
      }

      this.scrollToSelected();
   }

   public void updateScroll() {
      double pastScroll = (this.totalHeight - this.getHeight()) * this.scrollBar.getScrollProgress();
      this.totalHeight = 0;

      for (AbstractPermPackElement pack : this.permissionsList) {
         if (pack.isVisible()) {
            this.totalHeight = this.totalHeight + pack.getHeight() + 8;
         }
      }

      this.scrollBar.setScrollProgress(pastScroll / (this.totalHeight - this.getHeight()));
   }

   @Override
   public void setY(int y) {
      super.setY(y);
      this.scrollBar.setY(y + 28);
      this.searchBar.setY(y + 4);
      this.showFigura.setY(y + 4);
      this.showDisconnected.setY(y + 4);
   }

   public int getCategoryAt(double y) {
      int ret = -1;

      for (AbstractPermPackElement element : this.permissionsList) {
         if (element instanceof CategoryPermPackElement group && group.isVisible() && y >= group.getY()) {
            ret++;
         }
      }

      return Math.max(ret, 0);
   }

   public void scrollToSelected() {
      double y = 0.0;
      this.totalHeight = 0;

      for (AbstractPermPackElement pack : this.permissionsList) {
         if (!(pack instanceof PlayerPermPackElement) || pack.isVisible()) {
            if (pack == this.selectedEntry) {
               y = this.totalHeight;
            } else {
               this.totalHeight = this.totalHeight + pack.getHeight() + 8;
            }
         }
      }

      this.scrollBar.setScrollProgressNoAnim(y / this.totalHeight);
   }
}
