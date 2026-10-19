package org.figuramc.figura.utils.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.figuramc.figura.utils.PlatformUtils;

public class PlatformUtilsImpl {
   public static Path getGameDir() {
      return FabricLoader.getInstance().getGameDir();
   }

   public static String getFiguraModVersionString() {
      return ((ModContainer)FabricLoader.getInstance().getModContainer("figura").get()).getMetadata().getVersion().getFriendlyString();
   }

   public static Path getConfigDir() {
      return FabricLoader.getInstance().getConfigDir();
   }

   public static boolean isModLoaded(String modId) {
      return FabricLoader.getInstance().isModLoaded(modId);
   }

   public static String getModVersion(String modId) {
      return ((ModContainer)FabricLoader.getInstance().getModContainer(modId).get()).getMetadata().getVersion().getFriendlyString();
   }

   public static PlatformUtils.ModLoader getModLoader() {
      return PlatformUtils.ModLoader.FABRIC;
   }

   public static InputStream loadFileFromRoot(String path) throws FileNotFoundException {
      File file = ((Path)((ModContainer)FabricLoader.getInstance().getModContainer("figura").get()).findPath(path).get()).toFile();
      return new FileInputStream(file);
   }
}
