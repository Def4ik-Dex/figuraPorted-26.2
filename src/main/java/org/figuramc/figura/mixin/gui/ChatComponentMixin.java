package org.figuramc.figura.mixin.gui;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.GuiMessage.Line;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MessageSignature;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Debug(export = true, print = true)
@Mixin(value = ChatComponent.class, priority = 400)
public class ChatComponentMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Unique
   private Integer color;
   @Unique
   private static int currColor;

   @Inject(
      method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
      at = @At("HEAD"),
      locals = LocalCapture.CAPTURE_FAILSOFT,
      cancellable = true
   )
   private void injectAddMessage(
      Component message,
      MessageSignature signature,
      GuiMessageSource source,
      GuiMessageTag tag,
      CallbackInfo ci,
      @Local(argsOnly = true) LocalRef<Component> localMessageRef
   ) {
      Component modifiedMessage = this.modifyMessage(message);
      if (modifiedMessage == null) {
         ci.cancel();
      } else {
         localMessageRef.set(modifiedMessage);
      }
   }

   private Component modifyMessage(Component message) {
      this.color = null;
      if (AvatarManager.panic) {
         return message;
      }

      Avatar localPlayer = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
      if (localPlayer != null) {
         TextUtils.allowScriptEvents = true;
         String json = ((JsonElement)ComponentSerialization.CODEC
               .encodeStart(this.minecraft.player.registryAccess().createSerializationContext(JsonOps.INSTANCE), message)
               .getOrThrow())
            .toString();
         TextUtils.allowScriptEvents = false;
         Pair<String, Integer> event = localPlayer.chatReceivedMessageEvent(message.getString(), json);
         if (event != null) {
            String newMessage = (String)event.getFirst();
            if (newMessage == null) {
               return null;
            }

            if (!json.equals(newMessage)) {
               TextUtils.allowScriptEvents = true;
               message = TextUtils.tryParseJson(newMessage);
               TextUtils.allowScriptEvents = false;
            }

            this.color = (Integer)event.getSecond();
         }
      }

      if (!FiguraMod.parseMessages) {
         return message;
      }

      if (Configs.EMOJIS.value > 0) {
         message = Emojis.applyEmojis(message);
      }

      int config = Configs.CHAT_NAMEPLATE.value;
      if (config == 0) {
         return message;
      }

      message = TextUtils.parseLegacyFormatting(message);
      Map<String, UUID> players = EntityUtils.getPlayerList();
      String owner = null;
      String msgString = message.getString();
      String[] split = msgString.split("\\W+");

      for (String s : split) {
         if (players.containsKey(s)) {
            owner = s;
            break;
         }
      }

      for (Entry<String, UUID> entry : players.entrySet()) {
         String name = entry.getKey();
         if (msgString.toLowerCase(Locale.US).contains(name.toLowerCase(Locale.US))) {
            UUID uuid = entry.getValue();
            boolean isOwner = name.equals(owner);
            Component playerName = Component.literal(name);
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            NameplateCustomization custom = avatar != null && avatar.luaRuntime != null ? avatar.luaRuntime.nameplate.CHAT : null;
            if (custom != null || config >= 2) {
               Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1
                  ? TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ")
                  : playerName;
               Component var28 = TextUtils.replaceInText(replacement, "\\$\\{name\\}", playerName);
               Component emptyReplacement = Badges.appendBadges(var28, uuid, config > 1 && owner == null);
               emptyReplacement = TextUtils.trim(emptyReplacement);
               String quotedName = "(?i)\\b" + Pattern.quote(name) + "\\b";
               message = TextUtils.replaceInText(message, quotedName, emptyReplacement, (s, style) -> true, isOwner ? 1 : 0, Integer.MAX_VALUE);
               if (isOwner) {
                  Component temp = Badges.appendBadges(var28, uuid, config > 1);
                  temp = TextUtils.trim(temp);
                  message = TextUtils.replaceInText(message, quotedName, temp, (s, style) -> true, 1);
               }
            }
         }
      }

      return message;
   }

   @Inject(at = @At("HEAD"), method = "addMessageToQueue", cancellable = true)
   private void addMessageQueue(GuiMessage guiMessage, CallbackInfo ci) {
      if (guiMessage == null || guiMessage.content() == null) {
         ci.cancel();
      }
   }

   @Inject(at = @At("HEAD"), method = "addMessageToDisplayQueue", cancellable = true)
   private void addMessageDisplayQueue(GuiMessage guiMessage, CallbackInfo ci) {
      if (guiMessage == null || guiMessage.content() == null) {
         ci.cancel();
      }
   }

   @ModifyArg(at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V"), method = "addMessageToDisplayQueue")
   private Object addMessagesDisplayQueue(Object message) {
      if (this.color != null) {
         ((GuiMessageAccessor)message).figura$setColor(this.color);
      }

      return message;
   }

   @ModifyArg(at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V"), method = "addMessageToQueue")
   private Object addMessagesQueue(Object message) {
      if (this.color != null) {
         ((GuiMessageAccessor)message).figura$setColor(this.color);
      }

      return message;
   }

   @ModifyVariable(
      at = @At("HEAD"),
      method = "lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
      argsOnly = true
   )
   private static Line grabColor(Line value) {
      currColor = ((GuiMessageAccessor)(Object)value).figura$getColor();
      return value;
   }

   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;fill(IIIII)V", ordinal = 0),
      method = "lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
      index = 4
   )
   private static int textBackgroundOnRender(int color) {
      return color + currColor;
   }

   @ModifyVariable(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V"
      ),
      method = "refreshTrimmedMessages"
   )
   private GuiMessage refreshMessages(GuiMessage message) {
      this.color = ((GuiMessageAccessor)(Object)message).figura$getColor();
      return message;
   }
}
