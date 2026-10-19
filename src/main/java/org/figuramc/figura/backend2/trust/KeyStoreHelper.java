package org.figuramc.figura.backend2.trust;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketError;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.backend2.websocket.FiguraWebSocketAdapter;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.PlatformUtils;

public class KeyStoreHelper {
   private static final char[] password = "figuramc".toCharArray();

   public static WebSocket websocketWithBackendCertificates(String token) throws WebSocketException {
      FiguraMod.LOGGER.info("Initializing custom websocket");

      try {
         WebSocketFactory wsFactory = new WebSocketFactory();
         String serverName = ServerAddress.parseString(Configs.SERVER_IP.value).getHost();
         wsFactory.setServerName(serverName);
         WebSocket socket = wsFactory.createSocket(FiguraWebSocketAdapter.getBackendAddress());
         socket.addListener(new FiguraWebSocketAdapter(token));
         socket.addExtension("permessage-deflate");
         return socket;
      } catch (IOException e) {
         FiguraMod.LOGGER.error("Failed to load in the backend's certificates during Websocket creation!", e);
         NetworkStuff.disconnect("Failed to load certificates for the backend :c");
         throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR);
      }
   }

   private static KeyStore getKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
      KeyStore jdkKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      Path ksPath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
      jdkKeyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());
      Map<String, Certificate> jdkTrustStore = Collections.list(jdkKeyStore.aliases()).stream().collect(Collectors.toMap(a -> (String)a, alias -> {
         try {
            return jdkKeyStore.getCertificate(alias);
         } catch (KeyStoreException e) {
            FiguraMod.LOGGER.error("Could not find default certificates!", e);
            return null;
         }
      }));
      InputStream in = PlatformUtils.loadFileFromRoot("figurakeystore.jks");
      KeyStore figuraKeyStore = KeyStore.getInstance("JKS");
      figuraKeyStore.load(in, password);
      if (in != null) {
         in.close();
      }

      Map<String, Certificate> figuraTrustStore = Collections.list(figuraKeyStore.aliases()).stream().collect(Collectors.toMap(a -> (String)a, alias -> {
         try {
            return figuraKeyStore.getCertificate(alias);
         } catch (KeyStoreException e) {
            FiguraMod.LOGGER.error("Could not find default certificates!", e);
            return null;
         }
      }));
      KeyStore mergedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      mergedTrustStore.load(null, new char[0]);

      for (Entry<String, Certificate> entry : jdkTrustStore.entrySet()) {
         mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
      }

      for (Entry<String, Certificate> entry : figuraTrustStore.entrySet()) {
         mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
      }

      return mergedTrustStore;
   }
}
