package org.figuramc.figura.avatar.local;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Util;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.cards.CardBackground;
import org.figuramc.figura.parsers.AvatarMetadataParser;
import org.figuramc.figura.utils.FileTexture;
import org.figuramc.figura.utils.IOUtils;

public class LocalAvatarFetcher {
   public static final List<LocalAvatarFetcher.AvatarPath> ALL_AVATARS = new ArrayList<>();
   private static final Map<String, LocalAvatarFetcher.Properties> SAVED_DATA = new HashMap<>();
   private static final Map<Path, WatchKey> WATCHED_KEYS = new ConcurrentHashMap<>();
   private static boolean requireReload = true;
   private static boolean loaded;

   public static void loadAvatars() {
      loaded = false;
      FiguraMod.debug("Reloading Avatar List...");
      LocalAvatarFetcher.FolderPath root = new LocalAvatarFetcher.FolderPath(getLocalAvatarDirectory());
      root.fetch();
      ALL_AVATARS.clear();
      ALL_AVATARS.addAll(root.getChildren());
      loaded = true;
   }

   public static CompletableFuture<Void> reloadAvatars() {
      if (!requireReload) {
         return CompletableFuture.completedFuture(null);
      }

      requireReload = false;
      return CompletableFuture.runAsync(LocalAvatarFetcher::loadAvatars);
   }

   public static void tick() {
      boolean reload = false;

      for (Entry<Path, WatchKey> entry : WATCHED_KEYS.entrySet()) {
         WatchKey key = entry.getValue();
         if (key.isValid()) {
            for (WatchEvent<?> event : key.pollEvents()) {
               Kind<?> kind = event.kind();
               if (kind != StandardWatchEventKinds.OVERFLOW) {
                  if (kind == StandardWatchEventKinds.ENTRY_CREATE && !LocalAvatarLoader.IS_WINDOWS) {
                     Path child = entry.getKey().resolve((Path)event.context());
                     LocalAvatarLoader.addWatchKey(child, WATCHED_KEYS::put);
                  }

                  reload = true;
               }
            }

            if (reload) {
               break;
            }
         }
      }

      if (reload) {
         requireReload = true;
      }
   }

   public static void init() {
      load();
      LocalAvatarLoader.addWatchKey(getLocalAvatarDirectory(), WATCHED_KEYS::put);
   }

   public static void reinit() {
      WATCHED_KEYS.clear();
      SAVED_DATA.clear();
      ALL_AVATARS.clear();
      requireReload = true;
      loaded = false;
      init();
   }

   public static void load() {
      IOUtils.readCacheFile("avatars", nbt -> {
         for (Tag tag : nbt.getListOrEmpty("properties")) {
            CompoundTag compound = (CompoundTag)tag;
            String path = compound.getStringOr("path", "");
            LocalAvatarFetcher.Properties properties = new LocalAvatarFetcher.Properties();
            properties.expanded = compound.getBooleanOr("expanded", false);
            properties.favourite = compound.getBooleanOr("favourite", false);
            SAVED_DATA.put(path, properties);
         }
      });
   }

   public static void save() {
      IOUtils.saveCacheFile("avatars", nbt -> {
         ListTag properties = new ListTag();

         for (Entry<String, LocalAvatarFetcher.Properties> entry : SAVED_DATA.entrySet()) {
            CompoundTag compound = new CompoundTag();
            LocalAvatarFetcher.Properties prop = entry.getValue();
            if (!prop.expanded) {
               compound.putBoolean("expanded", false);
            }

            if (prop.favourite) {
               compound.putBoolean("favourite", true);
            }

            if (!compound.isEmpty()) {
               compound.putString("path", entry.getKey());
               properties.add(compound);
            }
         }

         nbt.put("properties", properties);
      });
   }

   public static void clearCache() {
      IOUtils.deleteCacheFile("avatars");
   }

   public static Path getLocalAvatarDirectory() {
      return IOUtils.getOrCreateDir(FiguraMod.getFiguraDirectory(), "avatars");
   }

   public static boolean isAvatar(Path path) {
      if (!Files.exists(path)) {
         return false;
      }

      Path metadata = path.resolve("avatar.json");
      return Files.exists(metadata) && !Files.isDirectory(metadata);
   }

   public static void loadExternal(List<Path> paths) throws IOException {
      for (Path path : paths) {
         Path dest = getLocalAvatarDirectory();

         try (Stream<Path> stream = Files.walk(path)) {
            for (Path p : stream.toList()) {
               Util.copyBetweenDirs(path.getParent(), dest, p);
            }
         }
      }
   }

   public static boolean isLoaded() {
      return loaded && !isReloadRequired();
   }

   public static boolean isReloadRequired() {
      return requireReload;
   }

   public static class AvatarPath {
      protected final Path path;
      protected final Path folder;
      protected final Path theActualPathForThis;
      protected final String name;
      protected final String description;
      protected final CardBackground background;
      protected LocalAvatarFetcher.Properties properties;
      protected final Path iconPath;
      protected boolean iconLoaded;
      protected FileTexture iconTexture;

      protected AvatarPath(Path path, Path folder, Path theActualPathForThis, String name) {
         this.path = path;
         this.folder = folder;
         this.theActualPathForThis = theActualPathForThis;
         this.name = name;
         this.description = "";
         this.background = CardBackground.DEFAULT;
         this.iconPath = null;
         this.properties = LocalAvatarFetcher.SAVED_DATA.computeIfAbsent(path.toAbsolutePath().toString(), __ -> new LocalAvatarFetcher.Properties());
      }

      public AvatarPath(Path path, Path folder) {
         this(path, folder, path);
      }

      public AvatarPath(Path path, Path folder, Path theActualPathForThis) {
         this.path = path;
         this.folder = folder;
         this.theActualPathForThis = theActualPathForThis;
         this.properties = LocalAvatarFetcher.SAVED_DATA.computeIfAbsent(path.toAbsolutePath().toString(), __ -> new LocalAvatarFetcher.Properties());
         String filename = IOUtils.getFileNameOrEmpty(path);
         String name = filename;
         String description = "";
         CardBackground bg = CardBackground.DEFAULT;
         Path iconPath = null;
         if (!(this instanceof LocalAvatarFetcher.FolderPath)) {
            try {
               String str = IOUtils.readFile(path.resolve("avatar.json"));
               AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(str);
               name = !Configs.WARDROBE_FILE_NAMES.value && metadata.name != null && !metadata.name.isBlank() ? metadata.name : filename;
               description = metadata.description == null ? "" : metadata.description;
               bg = CardBackground.parse(metadata.background);
            } catch (Exception e) {
               FiguraMod.LOGGER.error("Failed to load metadata for \"" + path + "\"", e);
            }

            Path p = path.resolve("avatar.png");
            if (Files.exists(p)) {
               iconPath = p;
            }
         }

         this.name = name;
         this.description = description;
         this.background = bg;
         this.iconPath = iconPath;
      }

      public boolean search(String query) {
         String q = query.toLowerCase(Locale.US);
         return this.getName().toLowerCase(Locale.US).contains(q) || IOUtils.getFileNameOrEmpty(this.path).contains(q);
      }

      public Path getPath() {
         return this.path;
      }

      public Path getFolder() {
         return this.folder;
      }

      public Path getFSPath() {
         Path path = this.getPath();
         Path folder = this.getFolder();
         return path.getFileSystem() == folder.getFileSystem() ? path : folder;
      }

      public Path getTheActualPathForThis() {
         return this.theActualPathForThis;
      }

      public String getName() {
         return this.name;
      }

      public String getDescription() {
         return this.description;
      }

      public CardBackground getBackground() {
         return this.background;
      }

      public FileTexture getIcon() {
         if (!this.iconLoaded) {
            this.iconLoaded = true;

            try {
               if (this.iconPath != null) {
                  this.iconTexture = FileTexture.of(this.iconPath);
               }
            } catch (Exception e) {
               FiguraMod.LOGGER.error("Failed to load icon for \"" + this.path + "\"", e);
            }
         }

         return this.iconTexture;
      }

      public boolean isExpanded() {
         return this.properties.expanded;
      }

      public void setExpanded(boolean expanded) {
         this.properties.expanded = expanded;
         this.saveProperties();
      }

      public boolean isFavourite() {
         return this.properties.favourite;
      }

      public void setFavourite(boolean favourite) {
         this.properties.favourite = favourite;
         this.saveProperties();
      }

      private void saveProperties() {
         String key = this.path.toAbsolutePath().toString();
         LocalAvatarFetcher.SAVED_DATA.put(key, this.properties);
      }
   }

   public static class FolderPath extends LocalAvatarFetcher.AvatarPath {
      protected final List<LocalAvatarFetcher.AvatarPath> children = new ArrayList<>();
      protected final FileSystem fileSystem;

      public FolderPath(FileSystem fileSystem, Path folder, Path path) {
         super(fileSystem.getPath(""), folder, path, IOUtils.getFileNameOrEmpty(path));
         this.fileSystem = fileSystem;
      }

      public FolderPath(Path path, Path folder) {
         super(path, folder);
         this.fileSystem = path.getFileSystem();
      }

      public FolderPath(Path path) {
         this(path, path);
      }

      public boolean fetch() {
         List<Path> files = IOUtils.listPaths(this.getPath());
         if (files == null) {
            return false;
         }

         boolean found = false;
         Path folderPath = this.path.getFileSystem() == FileSystems.getDefault() ? this.path : this.folder;

         for (Path path : files) {
            if (LocalAvatarFetcher.isAvatar(path)) {
               this.children.add(new LocalAvatarFetcher.AvatarPath(path, folderPath));
               found = true;
            } else if (Files.isDirectory(path)) {
               LocalAvatarFetcher.FolderPath folder = new LocalAvatarFetcher.FolderPath(path, folderPath);
               if (folder.fetch()) {
                  this.children.add(folder);
                  found = true;
               }
            } else if (IOUtils.getFileNameOrEmpty(path).endsWith(".zip")) {
               try {
                  FileSystem opened = FileSystems.newFileSystem(path);
                  if ("jar".equalsIgnoreCase(opened.provider().getScheme())) {
                     Path newPath = opened.getPath("");
                     if (LocalAvatarFetcher.isAvatar(newPath)) {
                        this.children.add(new LocalAvatarFetcher.AvatarPath(newPath, folderPath, path));
                        found = true;
                     } else {
                        LocalAvatarFetcher.FolderPath folder = new LocalAvatarFetcher.FolderPath(opened, folderPath, path);
                        if (folder.fetch()) {
                           this.children.add(folder);
                           found = true;
                        } else {
                           opened.close();
                        }
                     }
                  } else {
                     opened.close();
                  }
               } catch (IOException var9) {
               }
            }
         }

         return found;
      }

      @Override
      public boolean search(String query) {
         boolean result = super.search(query);

         for (LocalAvatarFetcher.AvatarPath child : this.children) {
            if (result) {
               break;
            }

            result = child.search(query);
         }

         return result;
      }

      public List<LocalAvatarFetcher.AvatarPath> getChildren() {
         return this.children;
      }
   }

   private static class Properties {
      public boolean expanded = true;
      public boolean favourite;
   }
}
