package org.figuramc.figura.backend2.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

public class S2CMessageHandler {
   public static final byte AUTH = 0;
   public static final byte PING = 1;
   public static final byte EVENT = 2;
   public static final byte TOAST = 3;
   public static final byte CHAT = 4;
   public static final byte NOTICE = 5;

   public static void handle(ByteBuffer bytes) {
      if (bytes.hasRemaining()) {
         byte b = bytes.get();
         if (NetworkStuff.debug) {
            FiguraMod.debug("Got ws message of type: " + b);
         }

         switch (b) {
            case 0:
               auth();
               break;
            case 1:
               ping(bytes);
               break;
            case 2:
               event(bytes);
               break;
            case 3:
               toast(bytes);
               break;
            case 4:
               chat(bytes);
               break;
            case 5:
               notice(bytes);
         }
      }
   }

   private static void auth() {
      FiguraMod.LOGGER.info("Connected to Figura ws backend");
      NetworkStuff.backendStatus = 3;
      NetworkStuff.subscribeAll();
      if (Configs.CONNECTION_TOASTS.value) {
         FiguraToast.sendToast(FiguraText.of("backend.connected"));
      }
   }

   private static void ping(ByteBuffer bytes) {
      UUID uuid = new UUID(bytes.getLong(), bytes.getLong());
      Avatar avatar = AvatarManager.getLoadedAvatar(uuid);
      if (avatar != null) {
         int id = bytes.getInt();
         bytes.get();
         byte[] data = new byte[bytes.remaining()];
         bytes.get(data);
         avatar.runPing(id, data);
         NetworkStuff.pingsReceived++;
         if (NetworkStuff.lastPing == 0) {
            NetworkStuff.lastPing = FiguraMod.ticks;
         }
      }
   }

   private static void event(ByteBuffer bytes) {
      UUID uuid = new UUID(bytes.getLong(), bytes.getLong());
      AvatarManager.reloadAvatar(uuid);
   }

   private static void toast(ByteBuffer bytes) {
      byte type = bytes.get();
      String[] str = StandardCharsets.UTF_8.decode(bytes).toString().split("\u0000", 2);
      FiguraToast.sendToast(str[0], str.length > 1 ? str[1] : "", FiguraToast.ToastType.values()[type]);
   }

   private static void chat(ByteBuffer bytes) {
      String message = StandardCharsets.UTF_8.decode(bytes).toString();
      FiguraMod.sendChatMessage(
         Component.empty()
            .append(Component.literal("-- Figura backend message --\n\n").withStyle(ColorUtils.Colors.SOFT_BLUE.style))
            .append(TextUtils.tryParseJson(message))
      );
   }

   private static void notice(ByteBuffer bytes) {
      if (Configs.CONNECTION_TOASTS.value) {
         byte type = bytes.get();
         switch (type) {
            case 0:
               FiguraToast.sendToast(FiguraText.of("backend.warning"), FiguraText.of("backend.ping_size"), FiguraToast.ToastType.ERROR);
               break;
            case 1:
               FiguraToast.sendToast(FiguraText.of("backend.warning"), FiguraText.of("backend.ping_rate"), FiguraToast.ToastType.ERROR);
         }
      }
   }
}
