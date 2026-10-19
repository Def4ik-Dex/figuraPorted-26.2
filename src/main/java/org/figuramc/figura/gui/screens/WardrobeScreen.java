package org.figuramc.figura.gui.screens;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.AvatarInfoWidget;
import org.figuramc.figura.gui.widgets.BackendMotdWidget;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.EntityPreview;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.LoadingErrorWidget;
import org.figuramc.figura.gui.widgets.StatusWidget;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class WardrobeScreen extends AbstractPanelScreen {
   private static final Component DEBUG_MOTD_FALLBACK = Component.literal("No motd could be loaded.\n\n")
      .append(
         "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n"
      )
      .withStyle(ChatFormatting.GRAY)
      .append(
         Component.literal("(This is some text you can hover)\n")
            .withStyle(Style.EMPTY.withColor(-847456).withHoverEvent(new ShowText(Component.literal("hi chat"))))
      )
      .append(
         Component.literal("(This is some text you can click on)\n")
            .withStyle(Style.EMPTY.withClickEvent(new OpenUrl(URI.create("https://github.com/FiguraMC/Figura"))))
      )
      .append(Component.literal("(This is only visible in debug mode)").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
   private Label panic;
   private Button upload;
   private Button delete;
   private Button back;
   private AvatarInfoWidget infoWidget;
   private BackendMotdWidget motdWidget;

   public WardrobeScreen(Screen parentScreen) {
      super(parentScreen, FiguraText.of("gui.panels.title.wardrobe"));
   }

   @Override
   protected void init() {
      super.init();
      Minecraft minecraft = Minecraft.getInstance();
      int middle = this.width / 2;
      int panels = this.getPanels();
      int modelBgSize = Math.min(this.width - panels * 2 - 16, this.height - 96);
      panels = Math.max((this.width - modelBgSize) / 2 - 8, panels);
      AvatarList avatarList = new AvatarList(4, 28, panels, this.height - 32, this);
      this.addRenderableWidget(avatarList);
      int entitySize = 11 * modelBgSize / 29;
      int entityX = middle - modelBgSize / 2;
      int entityY = this.height / 2 - modelBgSize / 2;
      EntityPreview entity = new EntityPreview(entityX, entityY, modelBgSize, modelBgSize, entitySize, -15.0F, 30.0F, minecraft.player, this);
      this.addRenderableWidget(entity);
      int buttX = entity.getX() + entity.getWidth() / 2;
      int buttY = entity.getY() + entity.getHeight() + 4;
      this.addRenderableWidget(
         this.upload = new Button(
            buttX - 48,
            buttY,
            24,
            24,
            0,
            0,
            24,
            FiguraIdentifier.of("textures/gui/upload.png"),
            72,
            24,
            FiguraText.of("gui.wardrobe.upload.tooltip"),
            button -> {
               Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());

               try {
                  LocalAvatarLoader.loadAvatar(null, null);
               } catch (Exception var3x) {
               }

               NetworkStuff.uploadAvatar(avatar);
               AvatarList.selectedEntry = null;
            }
         )
      );
      this.upload.setActive(false);
      this.addRenderableWidget(
         new Button(
            buttX - 12,
            buttY,
            24,
            24,
            0,
            0,
            24,
            FiguraIdentifier.of("textures/gui/reload.png"),
            72,
            24,
            FiguraText.of("gui.wardrobe.reload.tooltip"),
            button -> {
               AvatarManager.clearAvatars(FiguraMod.getLocalPlayerUUID());

               try {
                  LocalAvatarLoader.loadAvatar(null, null);
               } catch (Exception var2x) {
               }

               AvatarManager.localUploaded = true;
               AvatarList.selectedEntry = null;
               NetworkStuff.auth();
            }
         )
      );
      this.addRenderableWidget(
         this.delete = new Button(
            buttX + 24,
            buttY,
            24,
            24,
            0,
            0,
            24,
            FiguraIdentifier.of("textures/gui/delete.png"),
            72,
            24,
            FiguraText.of("gui.wardrobe.delete.tooltip"),
            button -> NetworkStuff.deleteAvatar(null)
         )
      );
      this.delete.setActive(false);
      StatusWidget statusWidget = new StatusWidget(entity.getX() + entity.getWidth() - 64, 0, 64);
      statusWidget.setY(entity.getY() - statusWidget.getHeight() - 4);
      this.addRenderableOnly(statusWidget);
      this.addRenderableOnly(new LoadingErrorWidget(statusWidget.getX() - 18, statusWidget.getY(), 14));
      MutableComponent versionText = FiguraText.of().append(" " + FiguraMod.VERSION.noBuildString()).withStyle(ChatFormatting.ITALIC);
      int versionStatus = NetworkStuff.latestVersion != null ? NetworkStuff.latestVersion.compareTo(FiguraMod.VERSION) : 0;
      boolean oldVersion = versionStatus > 0;
      if (oldVersion) {
         versionText.append(" ")
            .append(
               Component.literal("=").withStyle(Style.EMPTY.withFont(new Resource(UIHelper.UI_FONT)).withItalic(false).applyLegacyFormat(ChatFormatting.WHITE))
            )
            .withStyle(
               Style.EMPTY
                  .applyFormat(ChatFormatting.AQUA)
                  .withHoverEvent(
                     new ShowText(
                        FiguraText.of("gui.new_version.tooltip", Component.literal(NetworkStuff.latestVersion.toString()).withStyle(ChatFormatting.GREEN))
                     )
                  )
                  .withClickEvent(
                     new TextUtils.FiguraClickEvent(
                        UIHelper.openURL(
                           NetworkStuff.latestVersion.pre == null ? FiguraMod.Links.Modrinth.url + "/versions" : FiguraMod.Links.Github.url + "/releases"
                        )
                     )
                  )
            );
      } else if (versionStatus < 0) {
         versionText.withStyle(
            Style.EMPTY
               .withHoverEvent(
                  new ShowText(
                     FiguraText.of("gui.old_version.tooltip", Component.literal(NetworkStuff.latestVersion.toString()).withStyle(ChatFormatting.LIGHT_PURPLE))
                  )
               )
         );
      }

      Label version = new Label(versionText, middle, this.height - 4, TextUtils.Alignment.CENTER);
      this.addRenderableWidget(version);
      if (!oldVersion) {
         version.setAlpha(51);
      }

      version.setY(version.getRawY() - version.getHeight());
      int rightSide = Math.min(panels, 134);
      this.back = new Button(this.width - rightSide - 4, this.height - 24, rightSide, 20, FiguraText.of("gui.done"), null, bx -> this.onClose());
      this.addRenderableWidget(this.back);
      rightSide = panels / 2 + 52;
      Button avatarSettings;
      this.addRenderableWidget(
         avatarSettings = new Button(
            this.width - rightSide,
            28,
            24,
            24,
            0,
            0,
            24,
            FiguraIdentifier.of("textures/gui/avatar_settings.png"),
            72,
            24,
            FiguraText.of("gui.avatar_settings.tooltip").append("\n").append(FiguraText.of("gui.not_available_yet").withStyle(ChatFormatting.RED)),
            bx -> {}
         )
      );
      avatarSettings.setActive(false);
      Button sounds = new Button(
         this.width - rightSide + 36,
         28,
         24,
         24,
         0,
         0,
         24,
         FiguraIdentifier.of("textures/gui/sound.png"),
         72,
         24,
         FiguraText.of("gui.wardrobe.sound.tooltip"),
         button -> Minecraft.getInstance().gui.setScreen(new SoundScreen(this))
      );
      this.addRenderableWidget(sounds);
      Button keybinds = new Button(
         this.width - rightSide + 72,
         28,
         24,
         24,
         0,
         0,
         24,
         FiguraIdentifier.of("textures/gui/keybind.png"),
         72,
         24,
         FiguraText.of("gui.wardrobe.keybind.tooltip"),
         button -> Minecraft.getInstance().gui.setScreen(new KeybindScreen(this))
      );
      this.addRenderableWidget(keybinds);
      this.addRenderableOnly(this.infoWidget = new AvatarInfoWidget(this.width - panels - 4, 56, panels, this.back.getY() - 60));
      if (this.motdWidget != null) {
         this.removeWidget(this.motdWidget);
         this.motdWidget = null;
      }

      this.updateMotdWidget();
      this.addRenderableWidget(
         this.panic = new Label(
            FiguraText.of("gui.panic", Configs.PANIC_BUTTON.keyBind.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW),
            middle,
            version.getRawY(),
            TextUtils.Alignment.CENTER,
            0
         )
      );
      this.panic.setY(this.panic.getRawY() - this.panic.getHeight());
      this.panic.setVisible(false);
   }

   private int getPanels() {
      return Math.min(this.width / 3, 256) - 8;
   }

   private void updateMotdWidget() {
      int panels = this.getPanels();
      int infoBottom = this.infoWidget.getY() + this.infoWidget.getHeight();
      int width = panels - 8;
      int height = this.back.getY() - infoBottom - 16;
      int x = this.width - panels;
      int y = infoBottom + 8;
      this.infoWidget.tick();
      if (this.motdWidget == null) {
         Component motd = NetworkStuff.motd == null ? DEBUG_MOTD_FALLBACK : NetworkStuff.motd;
         if (!FiguraMod.debugModeEnabled() && motd == DEBUG_MOTD_FALLBACK) {
            return;
         }

         this.motdWidget = (BackendMotdWidget)this.addRenderableWidget(new BackendMotdWidget(x, y, width, height, motd, this.font));
      } else {
         this.motdWidget.setPosition(x, y);
         this.motdWidget.setWidth(width);
         this.motdWidget.setHeight(height);
         Component motd = NetworkStuff.motd == null ? DEBUG_MOTD_FALLBACK : NetworkStuff.motd;
         if (!FiguraMod.debugModeEnabled() && motd == DEBUG_MOTD_FALLBACK) {
            return;
         }

         this.motdWidget.setMessage(motd);
      }

      this.motdWidget.visible = this.motdWidget.getHeight() > 48;
   }

   @Override
   public void tick() {
      super.tick();
      this.panic.setVisible(AvatarManager.panic);
      Avatar avatar;
      this.upload
         .setActive(
            NetworkStuff.canUpload()
               && !AvatarManager.localUploaded
               && (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) != null
               && avatar.nbt != null
               && avatar.loaded
         );
      this.delete.setActive(NetworkStuff.isConnected() && AvatarManager.localUploaded);
      this.updateMotdWidget();
   }

   public void removed() {
      super.removed();
      LocalAvatarFetcher.save();
   }

   public void onFilesDrop(List<Path> paths) {
      super.onFilesDrop(paths);
      StringBuilder packs = new StringBuilder();

      for (int i = 0; i < paths.size(); i++) {
         if (i > 0) {
            packs.append("\n");
         }

         packs.append(IOUtils.getFileNameOrEmpty(paths.get(i)));
      }

      this.minecraft.gui.setScreen(new FiguraConfirmScreen(confirmed -> {
         if (confirmed) {
            try {
               LocalAvatarFetcher.loadExternal(paths);
               FiguraToast.sendToast(FiguraText.of("toast.wardrobe_copy.success", paths.size()));
            } catch (Exception e) {
               FiguraToast.sendToast(FiguraText.of("toast.wardrobe_copy.error"), FiguraToast.ToastType.ERROR);
               FiguraMod.LOGGER.error("Failed to copy files", e);
            }
         }

         this.minecraft.gui.setScreen(this);
      }, FiguraText.of("gui.wardrobe.drop_files"), packs.toString(), this));
   }
}
