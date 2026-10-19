package org.figuramc.figura.mixin.gui;

import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
   @Unique
   private UUID uuid;

   @Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
   private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
      int config = Configs.LIST_NAMEPLATE.value;
      if (config != 0 && !AvatarManager.panic) {
         Component text = (Component)cir.getReturnValue();
         Component name = Component.literal(playerInfo.getProfile().name());
         UUID uuid = playerInfo.getProfile().id();
         Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
         NameplateCustomization custom = avatar != null && avatar.luaRuntime != null ? avatar.luaRuntime.nameplate.LIST : null;
         Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1
            ? TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ")
            : name;
         Component var11 = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);
         var11 = Badges.appendBadges(var11, uuid, config > 1);
         var11 = TextUtils.trim(var11);
         text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(playerInfo.getProfile().name()) + "\\b", var11);
         cir.setReturnValue(text);
      }
   }

   @ModifyArg(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPlayerByUUID(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/Player;"
      ),
      method = "extractRenderState"
   )
   private UUID getPlayerByUUID(UUID id) {
      this.uuid = id;
      return id;
   }

   @ModifyArg(
      method = "extractRenderState",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/components/PlayerFaceExtractor;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"
      ),
      index = 4
   )
   private int doNotDrawFace(GuiGraphicsExtractor guiGraphics, Identifier id, int x, int y, int size, boolean hasHatLayer, boolean upsideDown, int color) {
      if (this.uuid != null) {
         Avatar avatar = AvatarManager.getAvatarForPlayer(this.uuid);
         if (avatar != null && avatar.submitPortraitDraw(guiGraphics, id, x, y, size, 16.0F, upsideDown)) {
            return 0;
         }
      }

      return size;
   }
}
