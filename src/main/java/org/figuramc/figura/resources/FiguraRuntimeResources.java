package org.figuramc.figura.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.IOUtils;

public class FiguraRuntimeResources {
   private static String ASSETS_VERSION;
   public static final PathPackResources PACK = new PathPackResources(
      new PackLocationInfo("Figura runtime resource pack", Component.literal("Figura Runtime Resources"), PackSource.BUILT_IN, Optional.empty()),
      getRootDirectory()
   );
   private static CompletableFuture<Void> future;

   public static Path getRootDirectory() {
      return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "resources");
   }

   public static Path getAssetsDirectory() {
      return IOUtils.getOrCreateDir(getRootDirectory(), "assets/figura");
   }

   public static String getAssetsVersion() {
      if (ASSETS_VERSION == null) {
         ASSETS_VERSION = FiguraMod.METADATA.getCustomValueAsString("assets_version");
      }

      return ASSETS_VERSION;
   }

   public static void clearCache() {
      IOUtils.deleteFile(getRootDirectory());
   }

   public static CompletableFuture<Void> init() {
      return future = CompletableFuture.runAsync(() -> {
         FiguraMod.LOGGER.info("Fetching backend resources...");
         Path hashesPath = getRootDirectory().resolve("hashes.json");

         JsonObject oldHashes;
         try (BufferedReader reader = Files.newBufferedReader(hashesPath)) {
            oldHashes = JsonParser.parseReader(reader).getAsJsonObject();
         } catch (Exception ignored) {
            oldHashes = new JsonObject();
         }

         JsonObject hashes;
         try (InputStream stream = NetworkStuff.getResourcesHashes(getAssetsVersion())) {
            byte[] bytes = stream.readAllBytes();
            String s = new String(bytes);
            hashes = JsonParser.parseString(s).getAsJsonObject();

            try (OutputStream fs = Files.newOutputStream(hashesPath)) {
               fs.write(bytes);
            } catch (Exception e) {
               FiguraMod.LOGGER.error("Failed to save resource hashes", e);
            }
         } catch (Exception ignored) {
            FiguraMod.LOGGER.warn("Failed to fetch backend resources");
            return;
         }

         for (Entry<String, JsonElement> entry : hashes.entrySet()) {
            String key = entry.getKey();
            JsonElement oldHash = oldHashes.get(key);

            try {
               if (oldHash == null || !oldHash.getAsString().equals(entry.getValue().getAsString())) {
                  getAndSaveResource(key);
               }
            } catch (Exception e) {
               FiguraMod.debug("Failed to download resource \"" + key + "\"", e);
            }
         }

         File assetsDirectoryFile = getAssetsDirectory().toFile();
         JsonObject directoryJsonObject = constructAssetsDirectoryJsonObject(assetsDirectoryFile.listFiles());

         for (Entry<String, JsonElement> entry : directoryJsonObject.entrySet()) {
            if (!hashes.has(entry.getKey()) && !Configs.LOCAL_ASSETS.value) {
               IOUtils.deleteFile(getAssetsDirectory().resolve(entry.getKey()));
            }
         }
      });
   }

   public static JsonObject constructAssetsDirectoryJsonObject(File[] files) {
      JsonObject object = new JsonObject();

      for (File file : files) {
         if (file.isDirectory()) {
            for (Entry<String, JsonElement> entry : constructAssetsDirectoryJsonObject(file.listFiles()).entrySet()) {
               object.addProperty(entry.getKey(), entry.getValue().getAsString());
            }
         } else if (!file.isHidden()) {
            object.addProperty(getAssetsDirectory().toUri().relativize(file.toURI()).getPath(), "0");
         }
      }

      return object;
   }

   private static void getAndSaveResource(String path) throws Exception {
      if (!Configs.LOCAL_ASSETS.value) {
         Path target = getAssetsDirectory().resolve(path);
         IOUtils.createDirIfNeeded(target.getParent());

         try (
            InputStream resource = NetworkStuff.getResource(getAssetsVersion(), path);
            OutputStream fs = Files.newOutputStream(target);
         ) {
            fs.write(resource.readAllBytes());
            FiguraMod.debug("Downloaded resource \"" + path + "\"");
         }
      }
   }

   public static void joinFuture() {
      if (future != null && !future.isDone()) {
         future.join();
      }
   }
}
