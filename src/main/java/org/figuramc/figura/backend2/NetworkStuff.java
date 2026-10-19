package org.figuramc.figura.backend2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.backend2.trust.KeyStoreHelper;
import org.figuramc.figura.backend2.websocket.C2SMessageHandler;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.RefilledNumber;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.Version;
import org.jetbrains.annotations.Nullable;

public class NetworkStuff {
   protected static final HttpClient client = HttpClient.newHttpClient();
   protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
   private static final ConcurrentLinkedQueue<NetworkStuff.Request<HttpAPI>> API_REQUESTS = new ConcurrentLinkedQueue<>();
   private static final ConcurrentLinkedQueue<NetworkStuff.Request<WebSocket>> WS_REQUESTS = new ConcurrentLinkedQueue<>();
   private static final List<UUID> SUBSCRIPTIONS = new ArrayList<>();
   private static CompletableFuture<Void> tasks;
   private static final int RECONNECT = 6000;
   private static int authCheck = 6000;
   protected static HttpAPI api;
   protected static WebSocket ws;
   public static int backendStatus = 1;
   public static String disconnectedReason;
   public static boolean debug = false;
   @Nullable
   public static Component motd;
   public static int lastPing;
   public static int pingsSent;
   public static int pingsReceived;
   public static Version latestVersion;
   private static final RefilledNumber uploadRate = new RefilledNumber();
   private static final RefilledNumber downloadRate = new RefilledNumber();
   private static int maxAvatarSize = Integer.MAX_VALUE;

   public static void tick() {
      uploadRate.tick();
      downloadRate.tick();
      authCheck--;
      if (authCheck <= 0) {
         authCheck = 6000;
         if (!isConnected()) {
            reAuth();
         } else if (!checkWS()) {
            reAuth();
         } else {
            checkAPI();
         }
      }

      tickSubscriptions();
      if (isConnected()) {
         processRequests();
      }

      if (lastPing > 0 && FiguraMod.ticks - lastPing >= 20) {
         pingsReceived = 0;
         pingsSent = 0;
         lastPing = 0;
      }
   }

   private static void tickSubscriptions() {
      ClientPacketListener connection = Minecraft.getInstance().getConnection();
      if (connection == null) {
         unsubscribeAll();
      } else {
         List<UUID> unsub = new ArrayList<>(SUBSCRIPTIONS);

         for (UUID uuid : connection.getOnlinePlayerIds()) {
            unsub.remove(uuid);
            if (!SUBSCRIPTIONS.contains(uuid)) {
               SUBSCRIPTIONS.add(uuid);
               subscribe(uuid);
            }
         }

         for (UUID uuid : unsub) {
            SUBSCRIPTIONS.remove(uuid);
            unsubscribe(uuid);
         }
      }
   }

   private static void processRequests() {
      NetworkStuff.Request<HttpAPI> request;
      if (!API_REQUESTS.isEmpty()) {
         while ((request = API_REQUESTS.poll()) != null) {
            NetworkStuff.Request<HttpAPI> finalRequest = request;
            async(() -> finalRequest.consumer.accept(api));
         }
      }

      if (!WS_REQUESTS.isEmpty()) {
         NetworkStuff.Request<WebSocket> wsRequest;
         while ((wsRequest = WS_REQUESTS.poll()) != null) {
            NetworkStuff.Request<WebSocket> finalRequest = wsRequest;
            async(() -> finalRequest.consumer.accept(ws));
         }
      }
   }

   protected static void async(Runnable toRun) {
      if (tasks != null && !tasks.isDone()) {
         tasks.thenRun(toRun);
      } else {
         tasks = CompletableFuture.runAsync(toRun);
      }
   }

   private static boolean checkUUID(UUID id) {
      if (id.version() != 4) {
         FiguraMod.debug("Voiding request for non v4 UUID \"" + id + "\" (v" + id.version() + ")");
         return true;
      } else {
         return false;
      }
   }

   public static void auth() {
      authCheck = 6000;
      AuthHandler.auth(false);
      fetchMOTD();
   }

   public static void reAuth() {
      authCheck = 6000;
      AuthHandler.auth(true);
      fetchMOTD();
   }

   protected static void authSuccess(String token) {
      FiguraMod.LOGGER.info("Successfully authed with the Figura auth server!");
      disconnectedReason = null;
      connect(token);
   }

   protected static void authFail(String reason) {
      FiguraMod.LOGGER.warn("Failed to auth with the Figura auth server! {}", reason == null ? "" : reason);
      disconnect(reason);
   }

   public static void connect(String token) {
      if (isConnected()) {
         disconnect(null);
      }

      backendStatus = 2;
      connectAPI(token);
      connectWS(token);
   }

   private static void fetchMOTD() {
      queueString(Util.NIL_UUID, HttpAPI::getMotd, (code, data) -> {
         responseDebug("motd", code, data);
         if (data != null) {
            motd = Emojis.applyEmojis(TextUtils.tryParseJson(data));
         }
      });
   }

   public static void disconnect(String reason) {
      backendStatus = 1;
      disconnectedReason = reason;
      disconnectAPI();
      disconnectWS();
   }

   private static void queueString(UUID owner, Function<HttpAPI, HttpRequest> request, BiConsumer<Integer, String> consumer) {
      API_REQUESTS.add(new NetworkStuff.Request<>(owner, api -> HttpAPI.runString(request.apply(api), consumer)));
   }

   private static void queueStream(UUID owner, Function<HttpAPI, HttpRequest> request, BiConsumer<Integer, InputStream> consumer) {
      API_REQUESTS.add(new NetworkStuff.Request<>(owner, api -> HttpAPI.runStream(request.apply(api), consumer)));
   }

   public static void clear(UUID requestOwner) {
      API_REQUESTS.removeIf(request -> request.owner.equals(requestOwner));
   }

   private static void responseDebug(String src, int code, String data) {
      if (debug) {
         FiguraMod.debug("Got response of \"" + src + "\" with code " + code + ":\n\t" + data);
      }
   }

   private static void connectAPI(String token) {
      api = new HttpAPI(token);
      checkVersion();
      setLimits();
   }

   private static void disconnectAPI() {
      api = null;
      clear(Util.NIL_UUID);
   }

   private static void checkAPI() {
      async(() -> {
         if (api == null) {
            reAuth();
         } else {
            HttpAPI.runString(api.checkAuth(), (code, data) -> {
               if (code != 200) {
                  reAuth();
               }
            });
         }
      });
   }

   public static void checkVersion() {
      queueString(Util.NIL_UUID, HttpAPI::getVersion, (code, data) -> {
         responseDebug("checkVersion", code, data);
         JsonObject json = JsonParser.parseString(data).getAsJsonObject();
         int config = Configs.UPDATE_CHANNEL.value;
         latestVersion = new Version(json.get(config <= 1 ? "release" : "prerelease").getAsString());
         if (config != 0) {
            if (latestVersion.compareTo(FiguraMod.VERSION) > 0) {
               FiguraToast.sendToast(FiguraText.of("toast.new_version"), latestVersion);
            }
         }
      });
   }

   public static void setLimits() {
      queueString(Util.NIL_UUID, HttpAPI::getLimits, (code, data) -> {
         responseDebug("setLimits", code, data);
         JsonObject json = JsonParser.parseString(data).getAsJsonObject();
         JsonObject rate = json.getAsJsonObject("rate");
         uploadRate.set(rate.get("upload").getAsInt() * 0.95);
         downloadRate.set(rate.get("download").getAsInt() * 0.95);
         JsonObject limits = json.getAsJsonObject("limits");
         maxAvatarSize = limits.get("maxAvatarSize").getAsInt();
      });
   }

   public static void getUser(UserData user) {
      if (!checkUUID(user.id)) {
         queueString(user.id, api -> api.getUser(user.id), (code, data) -> {
            responseDebug("getUser", code, data);
            if (code != 200) {
               if (code == 404 && Configs.CONNECTION_TOASTS.value) {
                  FiguraToast.sendToast(FiguraText.of("backend.user_not_found"), FiguraToast.ToastType.ERROR);
               }
            } else {
               JsonObject json = JsonParser.parseString(data).getAsJsonObject();
               ArrayList<Pair<String, Pair<String, UUID>>> avatars = new ArrayList<>();

               for (JsonElement element : json.getAsJsonArray("equipped")) {
                  JsonObject entry = element.getAsJsonObject();
                  UUID owner = UUID.fromString(entry.get("owner").getAsString());
                  avatars.add(Pair.of(entry.get("hash").getAsString(), Pair.of(entry.get("id").getAsString(), owner)));
               }

               JsonObject badges = json.getAsJsonObject("equippedBadges");
               Pair<BitSet, BitSet> badgesPair = Badges.emptyBadges();
               JsonArray pride = badges.getAsJsonArray("pride");
               BitSet prideSet = (BitSet)badgesPair.getFirst();

               for (int i = 0; i < pride.size(); i++) {
                  prideSet.set(i, pride.get(i).getAsInt() >= 1);
               }

               JsonArray special = badges.getAsJsonArray("special");
               BitSet specialSet = (BitSet)badgesPair.getSecond();

               for (int i = 0; i < special.size(); i++) {
                  specialSet.set(i, special.get(i).getAsInt() >= 1);
               }

               user.loadData(avatars, badgesPair);
            }
         });
      }
   }

   public static void uploadAvatar(Avatar avatar) {
      if (avatar != null && avatar.nbt != null) {
         if (avatar.id != null) {
         }

         String id = "avatar";

         try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(avatar.nbt, baos);
            queueString(Util.NIL_UUID, api -> api.uploadAvatar(id, baos.toByteArray()), (code, data) -> {
               responseDebug("uploadAvatar", code, data);
               if (code == 200) {
                  equipAvatar(List.of(Pair.of(avatar.owner, id)));
                  AvatarManager.localUploaded = true;
               }

               switch (code) {
                  case 200:
                     FiguraToast.sendToast(FiguraText.of("backend.upload_success"));
                     break;
                  case 413:
                     FiguraToast.sendToast(FiguraText.of("backend.upload_too_big"), FiguraToast.ToastType.ERROR);
                     break;
                  case 507:
                     FiguraToast.sendToast(FiguraText.of("backend.upload_too_many"), FiguraToast.ToastType.ERROR);
                     break;
                  default:
                     FiguraToast.sendToast(FiguraText.of("backend.upload_error"), FiguraToast.ToastType.ERROR);
               }
            });
            uploadRate.use();
            baos.close();
         } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
         }
      }
   }

   public static void deleteAvatar(String avatar) {
      if (avatar != null) {
      }

      String id = "avatar";
      queueString(Util.NIL_UUID, api -> api.deleteAvatar(id), (code, data) -> {
         responseDebug("deleteAvatar", code, data);
         switch (code) {
            case 200:
               FiguraToast.sendToast(FiguraText.of("backend.delete_success"));
               break;
            case 404:
               FiguraToast.sendToast(FiguraText.of("backend.avatar_not_found"), FiguraToast.ToastType.ERROR);
               break;
            default:
               FiguraToast.sendToast(FiguraText.of("backend.delete_error"), FiguraToast.ToastType.ERROR);
         }
      });
   }

   public static void equipAvatar(List<Pair<UUID, String>> avatars) {
      JsonArray json = new JsonArray();

      for (Pair<UUID, String> avatar : avatars) {
         JsonObject obj = new JsonObject();
         obj.addProperty("owner", ((UUID)avatar.getFirst()).toString());
         obj.addProperty("id", (String)avatar.getSecond());
         json.add(obj);
      }

      queueString(Util.NIL_UUID, api -> api.setEquipped(GSON.toJson(json)), (code, data) -> {
         responseDebug("equipAvatar", code, data);
         if (code != 200 && Configs.CONNECTION_TOASTS.value) {
            FiguraToast.sendToast(FiguraText.of("backend.equip_error"), FiguraToast.ToastType.ERROR);
         }
      });
   }

   public static void getAvatar(UserData target, UUID owner, String id, String hash) {
      if (!checkUUID(target.id)) {
         queueStream(target.id, api -> api.getAvatar(owner, id), (code, stream) -> {
            String s;
            try {
               s = code == 200 ? "<avatar data>" : new String(stream.readAllBytes());
            } catch (Exception e) {
               s = e.getMessage();
            }

            responseDebug("getAvatar", code, s);
            if (code == 200) {
               try {
                  CompoundTag nbt = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
                  CacheAvatarLoader.save(hash, nbt);
                  target.loadAvatar(nbt);
               } catch (Exception e) {
                  FiguraMod.LOGGER.error("Failed to load avatar for " + target.id, e);
               }
            }
         });
         downloadRate.use();
      }
   }

   private static void connectWS(String token) {
      if (ws != null) {
         ws.disconnect();
      }

      try {
         ws = KeyStoreHelper.websocketWithBackendCertificates(token);
         ws.connect();
      } catch (WebSocketException e) {
         FiguraMod.LOGGER.error(e.getMessage());
      }
   }

   private static void disconnectWS() {
      if (ws != null) {
         ws.disconnect();
      }

      ws = null;
   }

   private static boolean checkWS() {
      return ws != null && ws.isOpen() && backendStatus == 3;
   }

   public static void sendPing(int id, boolean sync, byte[] data) {
      if (AvatarManager.localUploaded && isConnected()) {
         try {
            ByteBuffer buffer = C2SMessageHandler.ping(id, sync, data);
            ws.sendBinary(buffer.array());
            pingsSent++;
            if (lastPing == 0) {
               lastPing = FiguraMod.ticks;
            }
         } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to send ping", e);
         }
      }
   }

   private static void subscribe(UUID id) {
      if (!checkUUID(id) && checkWS()) {
         WS_REQUESTS.add(new NetworkStuff.Request<>(Util.NIL_UUID, client -> {
            try {
               ByteBuffer buffer = C2SMessageHandler.sub(id);
               client.sendBinary(buffer.array());
               if (debug) {
                  FiguraMod.debug("Subbed to " + id);
               }
            } catch (Exception e) {
               FiguraMod.LOGGER.error("Failed to subscribe to " + id, e);
            }
         }));
      }
   }

   private static void unsubscribe(UUID id) {
      if (!checkUUID(id) && checkWS()) {
         WS_REQUESTS.add(new NetworkStuff.Request<>(Util.NIL_UUID, client -> {
            try {
               ByteBuffer buffer = C2SMessageHandler.unsub(id);
               client.sendBinary(buffer.array());
               if (debug) {
                  FiguraMod.debug("Unsubbed to " + id);
               }
            } catch (Exception e) {
               FiguraMod.LOGGER.error("Failed to unsubscribe to " + id, e);
            }
         }));
      }
   }

   public static void subscribeAll() {
      for (UUID uuid : SUBSCRIPTIONS) {
         subscribe(uuid);
      }
   }

   public static void unsubscribeAll() {
      for (UUID uuid : SUBSCRIPTIONS) {
         unsubscribe(uuid);
      }

      SUBSCRIPTIONS.clear();
   }

   private static InputStream request(HttpRequest request) throws Exception {
      HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
      return response.body();
   }

   public static InputStream getResourcesHashes(String version) throws Exception {
      return request(HttpRequest.newBuilder(HttpAPI.getUri("/assets/" + version)).timeout(Duration.ofSeconds(15L)).build());
   }

   public static InputStream getResource(String version, String resource) throws Exception {
      return request(HttpRequest.newBuilder(HttpAPI.getUri("/assets/" + version + "/" + resource)).build());
   }

   public static boolean isConnected() {
      return api != null && checkWS();
   }

   public static boolean canUpload() {
      return isConnected() && uploadRate.check();
   }

   public static int getSizeLimit() {
      return isConnected() ? maxAvatarSize : Integer.MAX_VALUE;
   }

   private record Request<T>(UUID owner, Consumer<T> consumer) {
      @Override
      public boolean equals(Object o) {
         return this == o ? true : o instanceof NetworkStuff.Request request && this.owner.equals(request.owner);
      }
   }
}
