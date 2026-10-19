package org.figuramc.figura.backend2;

import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;

public class AuthHandler {
   public static void auth(boolean reAuth) {
      NetworkStuff.async(() -> {
         if (reAuth || !NetworkStuff.isConnected()) {
            FiguraMod.LOGGER.info("Authenticating with Figura server...");
            NetworkStuff.backendStatus = 2;
            Minecraft minecraft = Minecraft.getInstance();
            User user = minecraft.getUser();

            try {
               String username = user.getName();
               String serverID = getServerID(username);
               FiguraMod.debug("Joining \"{}\" on server \"{}\"", username, serverID);
               minecraft.services().sessionService().joinServer(user.getProfileId(), user.getAccessToken(), serverID);
               NetworkStuff.authSuccess(getToken(serverID));
            } catch (AuthenticationUnavailableException e) {
               NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.serversUnavailable").getString());
            } catch (InvalidCredentialsException e) {
               NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.invalidSession").getString());
            } catch (InsufficientPrivilegesException e) {
               NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges").getString());
            } catch (UserBannedException e) {
               NetworkStuff.authFail(Component.translatable("disconnect.loginFailedInfo.userBanned").getString());
            } catch (Exception e) {
               NetworkStuff.authFail(e.getMessage());
            }
         }
      });
   }

   protected static String request(HttpRequest request) throws Exception {
      HttpResponse<String> response = NetworkStuff.client.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() != 200) {
         throw new Exception(response.body());
      } else {
         return response.body();
      }
   }

   private static String getServerID(String username) throws Exception {
      return request(HttpRequest.newBuilder(HttpAPI.getUri("/auth/id?username=" + username)).build());
   }

   private static String getToken(String serverID) throws Exception {
      return request(HttpRequest.newBuilder(HttpAPI.getUri("/auth/verify?id=" + serverID)).build());
   }
}
