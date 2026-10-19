package org.figuramc.figura.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class PopupMenu {
   private static final Identifier BACKGROUND = FiguraIdentifier.of("textures/gui/popup.png");
   private static final Identifier ICONS = FiguraIdentifier.of("textures/gui/popup_icons.png");
   private static final MutableComponent VERSION_WARN = Component.empty()
      .append(Badges.System.WARNING.badge.copy().withStyle(Style.EMPTY.withFont(new Resource(Badges.FONT))))
      .append(" ")
      .append(Badges.System.WARNING.desc.copy().withStyle(ChatFormatting.YELLOW));
   private static final MutableComponent ERROR_WARN = Component.empty()
      .append(Badges.System.ERROR.badge.copy().withStyle(Style.EMPTY.withFont(new Resource(Badges.FONT))))
      .append(" ")
      .append(Badges.System.ERROR.desc.copy().withStyle(ChatFormatting.RED));
   private static final MutableComponent PERMISSION_WARN = Component.empty()
      .append(Badges.System.PERMISSIONS.badge.copy().withStyle(Style.EMPTY.withFont(new Resource(Badges.FONT))))
      .append(" ")
      .append(Badges.System.PERMISSIONS.desc.copy().withStyle(ChatFormatting.BLUE));
   private static final List<Pair<Component, Consumer<UUID>>> BUTTONS = List.of(
      Pair.of(FiguraText.of("popup_menu.cancel"), (Consumer<UUID>)id -> {}), Pair.of(FiguraText.of("popup_menu.reload"), (Consumer<UUID>)id -> {
         AvatarManager.reloadAvatar(id);
         FiguraToast.sendToast(FiguraText.of("toast.reload"));
      }), Pair.of(FiguraText.of("popup_menu.increase_permissions"), (Consumer<UUID>)id -> {
         PermissionPack pack = PermissionManager.get(id);
         if (PermissionManager.increaseCategory(pack)) {
            FiguraToast.sendToast(FiguraText.of("toast.permission_change"), pack.getCategoryName());
         }
      }), Pair.of(FiguraText.of("popup_menu.decrease_permissions"), (Consumer<UUID>)id -> {
         PermissionPack pack = PermissionManager.get(id);
         if (PermissionManager.decreaseCategory(pack)) {
            FiguraToast.sendToast(FiguraText.of("toast.permission_change"), pack.getCategoryName());
         }
      })
   );
   private static final int LENGTH = BUTTONS.size();
   private static int index = 0;
   private static boolean enabled = false;
   private static Entity entity;
   private static UUID id;

   public static void render(GuiGraphicsExtractor gui) {
      if (isEnabled()) {
         if (entity == null) {
            id = null;
         } else {
            id = entity.getUUID();
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && (!entity.isInvisibleTo(minecraft.player) || entity == minecraft.player)) {
               GlStateManager._disableDepthTest();
               Matrix3x2fStack pose = gui.pose();
               pose.pushMatrix();
               FiguraVec3 worldPos = FiguraVec3.fromVec3(entity.getPosition(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)));
               worldPos.add(0.0, entity.getBbHeight() + 0.1F, 0.0);
               FiguraVec4 vec = MathUtils.worldToScreenSpace(worldPos);
               if (!(vec.z < 1.0)) {
                  Window window = minecraft.getWindow();
                  double w = window.getGuiScaledWidth();
                  double h = window.getGuiScaledHeight();
                  double s = Configs.POPUP_SCALE.value.floatValue()
                     * Math.max(
                        Math.min(window.getHeight() * 0.035 / vec.w * (1.0 / window.getGuiScale()), Configs.POPUP_MAX_SIZE.value.floatValue()),
                        Configs.POPUP_MIN_SIZE.value.floatValue()
                     );
                  pose.translate((float)((vec.x + 1.0) / 2.0 * w), (float)((vec.y + 1.0) / 2.0 * h));
                  pose.scale((float)(s * 0.5), (float)(s * 0.5));
                  int width = LENGTH * 18;
                  UIHelper.enableBlend();
                  int frame = Configs.REDUCED_MOTION.value ? 0 : (int)(FiguraMod.ticks / 5.0F % 4.0F);
                  gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, width / -2, -24, 0.0F, frame * 26, width, 26, width, 26, width, 104);
                  pose.translate(0.0F, 0.0F);
                  UIHelper.enableBlend();

                  for (int i = 0; i < LENGTH; i++) {
                     gui.blit(RenderPipelines.GUI_TEXTURED, ICONS, width / -2 + 18 * i, -24, 18 * i, i == index ? 18.0F : 0.0F, 18, 18, 18, 18, width, 36);
                  }

                  Font font = minecraft.font;
                  Component title = (Component)BUTTONS.get(index).getFirst();
                  PermissionPack tc = PermissionManager.get(id);
                  MutableComponent permissionName = tc.getCategoryName().append(tc.hasChanges() ? "*" : "");
                  MutableComponent name = entity.getName().copy();
                  boolean error = false;
                  boolean version = false;
                  boolean noPermissions = false;
                  Component badges = Badges.fetchBadges(id);
                  if (!badges.getString().isEmpty()) {
                     name.append(" ").append(badges);
                  }

                  Avatar avatar = AvatarManager.getAvatarForPlayer(id);
                  if (avatar != null) {
                     error = avatar.scriptError;
                     version = avatar.versionStatus > 0;
                     noPermissions = !avatar.noPermissions.isEmpty();
                  }

                  UIHelper.renderOutlineText(gui, font, name, -font.width(name) / 2, -36, 16777215, 2105376);
                  pose.scale(0.5F, 0.5F);
                  pose.translate(0.0F, 0.0F);
                  UIHelper.renderOutlineText(gui, font, permissionName, -font.width(permissionName) / 2, -54, 16777215, 2105376);
                  gui.text(font, title, -width + 4, -12, UIHelper.adjustColor(16777215));
                  if (error) {
                     UIHelper.renderOutlineText(gui, font, ERROR_WARN, -font.width(ERROR_WARN) / 2, 0, 16777215, 2105376);
                  }

                  if (version) {
                     UIHelper.renderOutlineText(gui, font, VERSION_WARN, -font.width(VERSION_WARN) / 2, error ? 9 : 0, 16777215, 2105376);
                  }

                  if (noPermissions) {
                     UIHelper.renderOutlineText(
                        gui, font, PERMISSION_WARN, -font.width(PERMISSION_WARN) / 2, (error ? 9 : 0) + (version ? 9 : 0), 16777215, 2105376
                     );
                  }

                  pose.popMatrix();
               }
            } else {
               entity = null;
               id = null;
            }
         }
      }
   }

   public static void scroll(double d) {
      index = (int)(index - d + LENGTH) % LENGTH;
   }

   public static void hotbarKeyPressed(int i) {
      if (i < LENGTH && i >= 0) {
         index = i;
      }
   }

   public static void run() {
      if (id != null) {
         ((Consumer)BUTTONS.get(index).getSecond()).accept(id);
      }

      enabled = false;
      entity = null;
      id = null;
      index = 0;
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static void setEnabled(boolean enabled) {
      PopupMenu.enabled = enabled;
   }

   public static boolean hasEntity() {
      return entity != null;
   }

   public static void setEntity(Entity entity) {
      PopupMenu.entity = entity;
   }

   public static UUID getEntityId() {
      return id;
   }
}
