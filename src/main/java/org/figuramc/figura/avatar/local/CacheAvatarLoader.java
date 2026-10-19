package org.figuramc.figura.avatar.local;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.utils.IOUtils;

public class CacheAvatarLoader {
   public static void init() {
      LocalAvatarLoader.async(() -> {
         Path file = getAvatarCacheDirectory();
         if (Files.exists(file) && Files.isDirectory(file)) {
            List<Path> children = IOUtils.listPaths(file);
            if (children != null) {
               for (Path child : children) {
                  try {
                     FileTime time = Files.getLastModifiedTime(child);
                     long diff = System.currentTimeMillis() - time.toMillis();
                     long elapsed = TimeUnit.MILLISECONDS.toDays(diff);
                     if (elapsed > 7L) {
                        if (!Files.deleteIfExists(child)) {
                           throw new Exception();
                        }

                        FiguraMod.debug("Successfully deleted cache avatar \"{}\" with \"{}\" days old", IOUtils.getFileNameOrEmpty(child), elapsed);
                     }
                  } catch (Exception ignored) {
                     FiguraMod.debug("Failed to delete cache avatar \"{}\"", IOUtils.getFileNameOrEmpty(child));
                  }
               }
            }
         }
      });
   }

   public static boolean checkAndLoad(String hash, UserData target) {
      Path p = getAvatarCacheDirectory();
      p = p.resolve(hash + ".nbt");
      if (Files.exists(p)) {
         load(hash, target);
         return true;
      } else {
         return false;
      }
   }

   public static void load(String hash, UserData target) {
      LocalAvatarLoader.async(() -> {
         Path path = getAvatarCacheDirectory().resolve(hash + ".nbt");

         try {
            target.loadAvatar(NbtIo.readCompressed(Files.newInputStream(path), NbtAccounter.unlimitedHeap()));
            FiguraMod.debug("Loaded avatar \"{}\" from cache to \"{}\"", hash, target.id);
         } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load cache avatar: " + hash, e);
         }
      });
   }

   public static void save(String hash, CompoundTag nbt) {
      LocalAvatarLoader.async(() -> {
         Path file = getAvatarCacheDirectory().resolve(hash + ".nbt");

         try {
            NbtIo.writeCompressed(nbt, Files.newOutputStream(file));
            FiguraMod.debug("Saved avatar \"{}\" on cache", hash);
         } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save avatar on cache: " + hash, e);
         }
      });
   }

   public static void clearCache() {
      LocalAvatarLoader.async(() -> {
         Path file = getAvatarCacheDirectory();
         if (Files.exists(file) && Files.isDirectory(file)) {
            List<Path> children = IOUtils.listPaths(file);
            if (children != null) {
               for (Path child : children) {
                  try {
                     if (!Files.deleteIfExists(child)) {
                        throw new Exception();
                     }
                  } catch (Exception ignored) {
                     FiguraMod.debug("Failed to delete cache avatar \"{}\"", IOUtils.getFileNameOrEmpty(child));
                  }
               }

               FiguraMod.debug("Finished clearing avatar cache");
            }
         }
      });
   }

   public static Path getAvatarCacheDirectory() {
      return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "avatars");
   }
}
