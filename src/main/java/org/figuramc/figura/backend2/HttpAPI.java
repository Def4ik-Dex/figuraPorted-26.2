package org.figuramc.figura.backend2;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;

public class HttpAPI {
   private final String token;

   public HttpAPI(String token) {
      this.token = token;
   }

   protected static URI getUri(String url) {
      return URI.create(getBackendAddress() + "/" + url);
   }

   protected static String getBackendAddress() {
      return "https://" + getBackendAddressWithPort() + "/api";
   }

   private static String getBackendAddressWithPort() {
      ServerAddress backendIP = ServerAddress.parseString(Configs.SERVER_IP.value);
      boolean hasPort = Configs.SERVER_IP.value.matches("[^:]+:\\d+");
      if (hasPort) {
         try {
            return backendIP.getHost() + ":" + backendIP.getPort();
         } catch (IllegalStateException var3) {
         }
      }

      return backendIP.getHost();
   }

   protected Builder header(String url) {
      return HttpRequest.newBuilder(getUri(url)).header("user-agent", "Figura/" + FiguraMod.VERSION).header("token", this.token);
   }

   protected static void runString(HttpRequest request, BiConsumer<Integer, String> consumer) {
      try {
         requestDebug(request);
         HttpResponse<String> response = NetworkStuff.client.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
         int code = response.statusCode();
         if (code == 401) {
            NetworkStuff.reAuth();
         }

         consumer.accept(code, response.body());
      } catch (Exception e) {
         if (!e.getMessage().contains("GOAWAY received")) {
            FiguraMod.LOGGER.error("", e);
         }
      }
   }

   protected static void runStream(HttpRequest request, BiConsumer<Integer, InputStream> consumer) {
      try {
         requestDebug(request);
         HttpResponse<InputStream> response = NetworkStuff.client.send(request, BodyHandlers.ofInputStream());
         int code = response.statusCode();
         if (code == 401) {
            NetworkStuff.reAuth();
         }

         consumer.accept(code, response.body());
      } catch (Exception e) {
         if (!e.getMessage().contains("GOAWAY received")) {
            FiguraMod.LOGGER.error("", e);
         }
      }
   }

   private static void requestDebug(HttpRequest msg) {
      if (NetworkStuff.debug) {
         FiguraMod.debug("Sent Http request:\n\t" + msg.uri().toString() + "\n\t" + msg.headers().map().toString());
      }
   }

   public HttpRequest checkAuth() {
      return this.header("").build();
   }

   public HttpRequest getUser(UUID id) {
      return this.header(id.toString()).build();
   }

   public HttpRequest getLimits() {
      return this.header("limits").build();
   }

   public HttpRequest getVersion() {
      return this.header("version").build();
   }

   public HttpRequest getMotd() {
      return this.header("motd").build();
   }

   public HttpRequest getAvatar(UUID owner, String id) {
      return this.header(owner.toString() + "/" + id).build();
   }

   public HttpRequest uploadAvatar(String id, byte[] bytes) {
      return this.header(id).PUT(BodyPublishers.ofByteArray(bytes)).header("Content-Type", "application/octet-stream").build();
   }

   public HttpRequest deleteAvatar(String id) {
      return this.header(id).DELETE().build();
   }

   public HttpRequest setEquipped(String json) {
      return this.header("equip").POST(BodyPublishers.ofString(json)).header("Content-Type", "application/json").build();
   }
}
