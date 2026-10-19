package org.figuramc.figura;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.compat.GeckoLibCompat;
import org.figuramc.figura.compat.IrisCompat;
import org.figuramc.figura.compat.SimpleVCCompat;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.entries.EntryPointManager;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.gui.widgets.DebugEntryFigura;
import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.figuramc.figura.mixin.MinecraftAccesor;
import org.figuramc.figura.mixin.gui.DebugScreenEntriesAccessor;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.resources.FiguraRuntimeResources;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraModMetadata;
import org.figuramc.figura.utils.FiguraResourceListener;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.PlatformUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.Version;
import org.figuramc.figura.wizards.AvatarWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiguraMod {
   public static final String MOD_ID = "figura";
   public static final String MOD_NAME = "Figura";
   public static final FiguraModMetadata METADATA = FiguraModMetadata.getMetadataForMod("figura");
   public static final Version VERSION = new Version(PlatformUtils.getFiguraModVersionString());
   public static final Calendar CALENDAR = Calendar.getInstance();
   public static final Path GAME_DIR = PlatformUtils.getGameDir().normalize();
   public static final Logger LOGGER = LoggerFactory.getLogger("Figura");
   public static final float VERTEX_OFFSET = -5.0E-4F;
   public static int ticks;
   public static Entity extendedPickEntity;
   public static Component splashText;
   public static boolean parseMessages = true;
   public static boolean processingKeybind;
   public static Category FIGURA_KEY_CATEGORY = new Category(FiguraIdentifier.of("keys"));
   public static Identifier FIGURA_DEBUG_KEY = Identifier.fromNamespaceAndPath("figura", "figura_debug_info");

   public static boolean debugModeEnabled() {
      return Configs.DEBUG_MODE != null && Configs.DEBUG_MODE.value;
   }

   public static void onClientInit() {
      EntryPointManager.init();
      PermissionManager.init();
      LocalAvatarFetcher.init();
      CacheAvatarLoader.init();
      FiguraDocsManager.init();
      FiguraRuntimeResources.init();
      DebugScreenEntriesAccessor.figura$invokeRegister(FIGURA_DEBUG_KEY, new DebugEntryFigura());
      GeckoLibCompat.init();
      SimpleVCCompat.init();
      if (PlatformUtils.isModLoaded("iris")) {
         IrisCompat.assignPipelinesToIrisPrograms();
      }
   }

   public static List<FiguraResourceListener> getResourceListeners() {
      List<FiguraResourceListener> listeners = new ArrayList<>();
      listeners.add(LocalAvatarLoader.AVATAR_LISTENER);
      listeners.add(Emojis.RESOURCE_LISTENER);
      listeners.add(AvatarWizard.RESOURCE_LISTENER);
      listeners.add(AvatarManager.RESOURCE_RELOAD_EVENT);
      return listeners;
   }

   public static void tick() {
      pushProfiler("network");
      NetworkStuff.tick();
      popPushProfiler("files");
      LocalAvatarLoader.tick();
      LocalAvatarFetcher.tick();
      if (Minecraft.getInstance().player != null) {
         popPushProfiler("avatars");
         AvatarManager.tickLoadedAvatars();
      }

      popPushProfiler("chatPrint");
      FiguraLuaPrinter.printChatFromQueue();
      popPushProfiler("emojiAnim");
      Emojis.tickAnimations();
      popProfiler();
      ticks++;
   }

   public static void debug(String str, Object... args) {
      if (debugModeEnabled()) {
         LOGGER.info("[DEBUG] " + str, args);
      } else {
         LOGGER.debug(str, args);
      }
   }

   public static Path getFiguraDirectory() {
      String config = Configs.MAIN_DIR.value;
      Path p = config.isBlank() ? GAME_DIR.resolve("figura") : Path.of(config);
      return IOUtils.createDirIfNeeded(p);
   }

   public static Path getCacheDirectory() {
      return IOUtils.getOrCreateDir(getFiguraDirectory(), Configs.LOCAL_ASSETS.value ? "local_cache" : "cache");
   }

   public static UUID getLocalPlayerUUID() {
      Entity player = Minecraft.getInstance().player;
      return player != null ? player.getUUID() : Minecraft.getInstance().getUser().getProfileId();
   }

   public static boolean isLocal(UUID other) {
      return getLocalPlayerUUID().equals(other);
   }

    public static void sendChatMessage(Component message) {
        if (Minecraft.getInstance().gui != null) {
            parseMessages = false;
            Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(TextUtils.replaceTabs(message));
            parseMessages = true;
        } else {
            LOGGER.info(message.getString());
        }
    }

   public static Style getAccentColor() {
      Avatar avatar = AvatarManager.getAvatarForPlayer(getLocalPlayerUUID());
      int color = avatar != null
         ? ColorUtils.rgbToInt(ColorUtils.userInputHex(avatar.color, ColorUtils.Colors.AWESOME_BLUE.vec))
         : ColorUtils.Colors.AWESOME_BLUE.hex;
      return Style.EMPTY.withColor(color);
   }

   public static void pushProfiler(String name) {
      ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetMetricsRecorder().getProfiler().push(name);
   }

   public static void pushProfiler(Avatar avatar) {
      ((MinecraftAccesor)Minecraft.getInstance())
         .figura$invokeGetMetricsRecorder()
         .getProfiler()
         .push(avatar.entityName.isBlank() ? avatar.owner.toString() : avatar.entityName);
   }

   public static void popPushProfiler(String name) {
      ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetMetricsRecorder().getProfiler().popPush(name);
   }

   public static void popProfiler() {
      ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetMetricsRecorder().getProfiler().pop();
   }

   public static <T> T popReturnProfiler(T var) {
      ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetMetricsRecorder().getProfiler().pop();
      return var;
   }

   public static void popProfiler(int times) {
      ProfilerFiller profiler = ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetMetricsRecorder().getProfiler();

      for (int i = 0; i < times; i++) {
         profiler.pop();
      }
   }

   public enum Links {
      Wiki("https://wiki.figuramc.org/", ColorUtils.Colors.AWESOME_BLUE.style),
      Kofi("https://ko-fi.com/skyrina", ColorUtils.Colors.KOFI.style),
      OpenCollective("https://opencollective.com/figura", ColorUtils.Colors.KOFI.style),
      Discord("https://discord.figuramc.org/", ColorUtils.Colors.DISCORD.style),
      Github("https://github.com/FiguraMC/Figura", ColorUtils.Colors.GITHUB.style),
      Modrinth("https://modrinth.com/mod/figura", ColorUtils.Colors.MODRINTH.style),
      Curseforge("https://www.curseforge.com/minecraft/mc-mods/figura", ColorUtils.Colors.CURSEFORGE.style),
      LuaManual("https://www.lua.org/manual/5.2/manual.html", ColorUtils.Colors.LUA_LOG.style);

      public final String url;
      public final Style style;

      Links(String url, Style style) {
         this.url = url;
         this.style = style;
      }
   }
}
