package org.figuramc.figura.avatar.local;

import com.sun.nio.file.ExtendedWatchEventModifier;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.parsers.AvatarMetadataParser;
import org.figuramc.figura.parsers.BlockbenchModelParser;
import org.figuramc.figura.parsers.LuaScriptParser;
import org.figuramc.figura.utils.FiguraResourceListener;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.IOUtils;

public class LocalAvatarLoader {
   public static final boolean IS_WINDOWS = Util.getPlatform() == OS.WINDOWS;
   private static final HashMap<Path, WatchKey> KEYS = new HashMap<>();
   private static CompletableFuture<Void> tasks;
   private static Path lastLoadedPath;
   private static LocalAvatarLoader.LoadState loadState = LocalAvatarLoader.LoadState.UNKNOWN;
   private static String loadError;
   private static WatchService watcher;
   public static final HashMap<Identifier, CompoundTag> CEM_AVATARS = new HashMap<>();
   public static final FiguraResourceListener AVATAR_LISTENER = FiguraResourceListener.createResourceListener(
      "cem",
      manager -> {
         CEM_AVATARS.clear();
         AvatarManager.clearCEMAvatars();

         for (Entry<Identifier, Resource> cem : manager.listResources(
               "cem", location -> location.getNamespace().equals("figura") && location.getPath().endsWith(".nbt")
            )
            .entrySet()) {
            Identifier key = cem.getKey();
            String[] split = key.getPath().split("/");
            if (split.length > 1) {
               String namespace = split[split.length - 2];
               String path = split[split.length - 1];
               Identifier id = Identifier.fromNamespaceAndPath(namespace, path.substring(0, path.length() - 4));

               CompoundTag nbt;
               try {
                  nbt = NbtIo.readCompressed(cem.getValue().open(), NbtAccounter.unlimitedHeap());
               } catch (Exception e) {
                  FiguraMod.LOGGER.error("Failed to load " + id + " avatar", e);
                  continue;
               }

               FiguraMod.LOGGER.info("Loaded CEM model for " + id);
               CEM_AVATARS.put(id, nbt);
            }
         }
      }
   );

   protected static void async(Runnable toRun) {
      if (tasks != null && !tasks.isDone()) {
         tasks.thenRun(toRun);
      } else {
         tasks = CompletableFuture.runAsync(toRun);
      }
   }

   public static void loadAvatar(Path path, UserData target) {
      loadError = null;
      loadState = LocalAvatarLoader.LoadState.UNKNOWN;
      resetWatchKeys();

      try {
         path = path == null ? null : (path.getFileSystem() == FileSystems.getDefault() ? Path.of(path.toFile().getCanonicalPath()) : path.normalize());
      } catch (IOException var3) {
      }

      lastLoadedPath = path;
      if (path != null && target != null) {
         addWatchKey(path, KEYS::put);
         queueAvatarLoad(path, target, true);
      }
   }

   public static void loadPreviewAvatar(Path path, UserData target) {
      if (path != null && target != null) {
         try {
            path = path.getFileSystem() == FileSystems.getDefault() ? Path.of(path.toFile().getCanonicalPath()) : path.normalize();
         } catch (IOException var3) {
         }

         queueAvatarLoad(path, target, false);
      }
   }

   private static void queueAvatarLoad(Path finalPath, UserData target, boolean showErrors) {
      async(() -> {
         try {
            CompoundTag nbt = new CompoundTag();
            loadState = LocalAvatarLoader.LoadState.SCRIPTS;
            loadScripts(finalPath, nbt);
            loadState = LocalAvatarLoader.LoadState.SOUNDS;
            loadSounds(finalPath, nbt);
            CompoundTag textures = new CompoundTag();
            ListTag animations = new ListTag();
            BlockbenchModelParser modelParser = new BlockbenchModelParser();
            loadState = LocalAvatarLoader.LoadState.MODELS;
            CompoundTag models = loadModels(finalPath, finalPath, modelParser, textures, animations, "");
            models.putString("name", "models");
            loadState = LocalAvatarLoader.LoadState.METADATA;
            String _meta = IOUtils.readFile(finalPath.resolve("avatar.json"));
            AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(_meta);
            CompoundTag metaNBT = AvatarMetadataParser.parse(metadata, _meta, IOUtils.getFileNameOrEmpty(finalPath));
            nbt.put("metadata", metaNBT);
            metaNBT.putString("uuid", target.id.toString());
            AvatarMetadataParser.injectToModels(metadata, models);
            AvatarMetadataParser.injectToTextures(metadata, textures);
            if (!models.isEmpty()) {
               nbt.put("models", models);
            }

            if (!textures.isEmpty()) {
               nbt.put("textures", textures);
            }

            if (!animations.isEmpty()) {
               nbt.put("animations", animations);
            }

            CompoundTag metadataTag = nbt.getCompoundOrEmpty("metadata");
            if (metadataTag.contains("resources_paths")) {
               loadResources(nbt, metadataTag.getListOrEmpty("resources_paths"), finalPath);
               metadataTag.remove("resource_paths");
            }

            target.loadAvatar(nbt);
         } catch (Throwable e) {
            loadError = e.getMessage();
            FiguraMod.LOGGER.error("Failed to load avatar from " + finalPath, e);
            if (showErrors) {
               FiguraToast.sendToast(FiguraText.of("toast.load_error"), FiguraText.of("gui.load_error." + getLoadState()), FiguraToast.ToastType.ERROR);
            }
         }
      });
   }

   private static void loadResources(CompoundTag nbt, ListTag pathsTag, Path parentPath) {
      ArrayList<PathMatcher> pathMatchers = new ArrayList<>();
      FileSystem fs = FileSystems.getDefault();

      for (int i = 0; i < pathsTag.size(); i++) {
         pathMatchers.add(fs.getPathMatcher("glob:".concat(pathsTag.getStringOr(i, ""))));
      }

      Map<String, Path> pathMap = new HashMap<>();
      matchPathsRecursive(pathMap, parentPath, parentPath, pathMatchers);
      CompoundTag resourcesTag = new CompoundTag();

      for (String p : pathMap.keySet()) {
         try (FileInputStream fis = new FileInputStream(pathMap.get(p).toFile())) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(baos);

            int i;
            while ((i = fis.read()) != -1) {
               gos.write(i);
            }

            gos.close();
            resourcesTag.put(unixifyPath(p), new ByteArrayTag(baos.toByteArray()));
            baos.close();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      nbt.put("resources", resourcesTag);
   }

   private static String unixifyPath(String original) {
      Path p = Path.of(original);
      String[] components = new String[p.getNameCount()];

      for (int i = 0; i < components.length; i++) {
         components[i] = p.getName(i).toString();
      }

      return String.join("/", components);
   }

   private static void matchPathsRecursive(Map<String, Path> pathMap, Path parent, Path current, ArrayList<PathMatcher> matchers) {
      File f = current.toFile();
      if (f.isFile()) {
         Path relative = parent.toAbsolutePath().relativize(current.toAbsolutePath()).normalize();

         for (PathMatcher m : matchers) {
            if (m.matches(relative)) {
               pathMap.put(relative.toString(), current);
               break;
            }
         }
      } else {
         for (File fl : current.toFile().listFiles()) {
            matchPathsRecursive(pathMap, parent, fl.toPath(), matchers);
         }
      }
   }

   private static void loadScripts(Path path, CompoundTag nbt) throws IOException {
      List<Path> scripts = IOUtils.getFilesByExtension(path, ".lua");
      if (!scripts.isEmpty()) {
         CompoundTag scriptsNbt = new CompoundTag();
         String pathRegex = path.toString().isEmpty() ? "\\Q\\E" : Pattern.quote(path + path.getFileSystem().getSeparator());

         for (Path script : scripts) {
            String name = script.toString().replaceFirst(pathRegex, "").replaceAll("[/\\\\]", ".");
            name = name.substring(0, name.length() - 4);
            scriptsNbt.put(name, LuaScriptParser.parseScript(name, IOUtils.readFile(script)));
         }

         nbt.put("scripts", scriptsNbt);
      }
   }

   private static void loadSounds(Path path, CompoundTag nbt) throws IOException {
      List<Path> sounds = IOUtils.getFilesByExtension(path, ".ogg");
      if (!sounds.isEmpty()) {
         CompoundTag soundsNbt = new CompoundTag();
         String pathRegex = Pattern.quote(path.toString().isEmpty() ? path.toString() : path + path.getFileSystem().getSeparator());

         for (Path sound : sounds) {
            String name = sound.toString().replaceFirst(pathRegex, "").replaceAll("[/\\\\]", ".");
            name = name.substring(0, name.length() - 4);
            soundsNbt.putByteArray(name, IOUtils.readFileBytes(sound));
         }

         nbt.put("sounds", soundsNbt);
      }
   }

   private static CompoundTag loadModels(
      Path avatarFolder, Path currentFile, BlockbenchModelParser parser, CompoundTag textures, ListTag animations, String folders
   ) throws Exception {
      CompoundTag result = new CompoundTag();
      List<Path> subFiles = IOUtils.listPaths(currentFile);
      ListTag children = new ListTag();
      if (subFiles != null) {
         for (Path file : subFiles) {
            if (!IOUtils.isHidden(file)) {
               String name = IOUtils.getFileNameOrEmpty(file);
               if (Files.isDirectory(file)) {
                  CompoundTag subfolder = loadModels(avatarFolder, file, parser, textures, animations, folders + name + ".");
                  if (!subfolder.isEmpty()) {
                     subfolder.putString("name", name);
                     BlockbenchModelParser.parseParent(name, subfolder);
                     children.add(subfolder);
                  }
               } else if (file.toString().toLowerCase(Locale.US).endsWith(".bbmodel")) {
                  BlockbenchModelParser.ModelData data = parser.parseModel(
                     avatarFolder, file, IOUtils.readFile(file), name.substring(0, name.length() - 8), folders
                  );
                  children.add(data.modelNbt());
                  animations.addAll(data.animationList());
                  CompoundTag dataTag = data.textures();
                  if (!dataTag.isEmpty()) {
                     if (textures.isEmpty()) {
                        textures.put("data", new ListTag());
                        textures.put("src", new CompoundTag());
                     }

                     textures.getListOrEmpty("data").addAll(dataTag.getListOrEmpty("data"));
                     textures.getCompoundOrEmpty("src").merge(dataTag.getCompoundOrEmpty("src"));
                  }
               }
            }
         }
      }

      if (!children.isEmpty()) {
         result.put("chld", children);
      }

      return result;
   }

   public static void tick() {
      WatchEvent<?> event = null;
      boolean reload = false;

      label49:
      for (Entry<Path, WatchKey> entry : KEYS.entrySet()) {
         WatchKey key = entry.getValue();
         if (key.isValid()) {
            Iterator var5 = key.pollEvents().iterator();

            while (true) {
               if (var5.hasNext()) {
                  WatchEvent<?> watchEvent = (WatchEvent<?>)var5.next();
                  Kind<?> kind = watchEvent.kind();
                  if (kind == StandardWatchEventKinds.OVERFLOW) {
                     continue;
                  }

                  event = watchEvent;
                  Path path = entry.getKey().resolve((Path)event.context());
                  String name = IOUtils.getFileNameOrEmpty(path);
                  if (IOUtils.isHiddenAvatarResource(path) || !Files.isDirectory(path) && !name.matches("(.*(\\.lua|\\.bbmodel|\\.ogg|\\.png)$|avatar\\.json)")
                     )
                   {
                     continue;
                  }

                  if (kind == StandardWatchEventKinds.ENTRY_CREATE && !IS_WINDOWS) {
                     addWatchKey(path, KEYS::put);
                  }

                  reload = true;
               }

               if (reload) {
                  break label49;
               }
               break;
            }
         }
      }

      if (reload) {
         FiguraMod.debug("Detected file changes in the Avatar directory (" + event.context().toString() + "), reloading!");
         AvatarManager.loadLocalAvatar(lastLoadedPath);
      }
   }

   public static void resetWatchKeys() {
      lastLoadedPath = null;

      for (WatchKey key : KEYS.values()) {
         key.cancel();
      }

      KEYS.clear();
   }

   protected static void addWatchKey(Path path, BiConsumer<Path, WatchKey> consumer) {
      if (watcher != null && path != null && path.getFileSystem() == FileSystems.getDefault()) {
         if (Files.isDirectory(path) && !IOUtils.isHidden(path)) {
            try {
               Kind<?>[] events = new Kind[]{StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
               WatchKey key = IS_WINDOWS ? path.register(watcher, events, ExtendedWatchEventModifier.FILE_TREE) : path.register(watcher, events);
               consumer.accept(path, key);
               List<Path> children = IOUtils.listPaths(path);
               if (children == null || IS_WINDOWS) {
                  return;
               }

               for (Path child : children) {
                  addWatchKey(child, consumer);
               }
            } catch (Exception e) {
               FiguraMod.LOGGER.error("Failed to register watcher for " + path, e);
            }
         }
      }
   }

   public static Path getLastLoadedPath() {
      return lastLoadedPath;
   }

   public static String getLoadState() {
      return loadState.name().toLowerCase(Locale.US);
   }

   public static String getLoadError() {
      return loadError;
   }

   static {
      try {
         watcher = FileSystems.getDefault().newWatchService();
      } catch (Exception e) {
         FiguraMod.LOGGER.error("Failed to initialize the watcher service", e);
      }
   }

   private enum LoadState {
      UNKNOWN,
      SCRIPTS,
      SOUNDS,
      MODELS,
      METADATA;
   }
}
