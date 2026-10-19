package org.figuramc.figura.avatar;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.lua.api.particle.ParticleAPI;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraResourceListener;
import org.figuramc.figura.utils.FiguraText;

public class AvatarManager {
   private static final Map<UUID, UserData> LOADED_USERS = new ConcurrentHashMap<>();
   private static final Set<UUID> FETCHED_USERS = new HashSet<>();
   private static final Int2ObjectMap<Avatar> LOADED_CEM = new Int2ObjectOpenHashMap();
   public static final FiguraResourceListener RESOURCE_RELOAD_EVENT = FiguraResourceListener.createResourceListener(
      "resource_reload_event", manager -> executeAll("resourceReloadEvent", Avatar::resourceReloadEvent)
   );
   public static boolean localUploaded = true;
   public static boolean panic = false;
   public static Int2ObjectMap<Entity> ENTITY_CACHE = new Int2ObjectOpenHashMap();

   public static void togglePanic() {
      panic = !panic;
      FiguraToast.sendToast(FiguraText.of(panic ? "toast.panic_enabled" : "toast.panic_disabled"), FiguraToast.ToastType.WARNING);
      SoundAPI.getSoundEngine().figura$stopAllSounds();
      ParticleAPI.getParticleEngine().figura$clearParticles(null);
   }

   public static void tickLoadedAvatars() {
      if (!panic) {
         for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
               FiguraMod.pushProfiler(avatar);
               avatar.tick();
               FiguraMod.popProfiler();
            }
         }

         if (!LOADED_CEM.isEmpty()) {
            IntSet toBeRemoved = new IntOpenHashSet();
            IntIterator var5 = LOADED_CEM.keySet().iterator();

            while (var5.hasNext()) {
               int entityId = (Integer)var5.next();
               Entity entity = Minecraft.getInstance().level.getEntity(entityId);
               if (entity != null && entity.isRemoved()) {
                  toBeRemoved.add(entityId);
                  ENTITY_CACHE.remove(entityId);
               } else if (entity == null) {
                  ENTITY_CACHE.remove(entityId);
               }
            }

            var5 = toBeRemoved.iterator();

            while (var5.hasNext()) {
               int entity = (Integer)var5.next();
               LOADED_CEM.remove(entity);
            }

            ObjectIterator var7 = LOADED_CEM.values().iterator();

            while (var7.hasNext()) {
               Avatar avatar = (Avatar)var7.next();
               if (avatar != null) {
                  FiguraMod.pushProfiler(avatar);
                  avatar.tick();
                  FiguraMod.popProfiler();
               }
            }
         }
      }
   }

   public static void executeAll(String src, Consumer<Avatar> consumer) {
      if (!panic) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(src);

         for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
               FiguraMod.pushProfiler(avatar);
               consumer.accept(avatar);
               FiguraMod.popProfiler();
            }
         }

         ObjectIterator var5 = LOADED_CEM.values().iterator();

         while (var5.hasNext()) {
            Avatar avatar = (Avatar)var5.next();
            if (avatar != null) {
               FiguraMod.pushProfiler(avatar);
               consumer.accept(avatar);
               FiguraMod.popProfiler();
            }
         }

         FiguraMod.popProfiler(2);
      }
   }

   public static Avatar getAvatarForPlayer(UUID player) {
      if (!panic && Minecraft.getInstance().level != null) {
         fetchBackend(player);
         UserData user = LOADED_USERS.get(player);
         return user == null ? null : user.getMainAvatar();
      } else {
         return null;
      }
   }

   private static Avatar getAvatarForEntity(Entity entity) {
      Avatar loaded = (Avatar)LOADED_CEM.get(entity.getId());
      if (loaded != null) {
         return loaded;
      }

      Identifier type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      CompoundTag nbt = LocalAvatarLoader.CEM_AVATARS.get(type);
      return nbt == null ? null : loadEntityAvatar(entity, nbt);
   }

   public static Avatar getAvatar(EntityRenderState state) {
      if (panic || Minecraft.getInstance().level == null || state == null) {
         return null;
      }

      if (state instanceof AvatarRenderState playerRenderState) {
         Entity entity = (Entity)ENTITY_CACHE.get(playerRenderState.id);
         if (entity == null) {
            entity = Minecraft.getInstance().level.getEntity(playerRenderState.id);
         }

         return getAvatar(entity);
      } else {
         Integer id = ((FiguraEntityRenderStateExtension)state).figura$getEntityId();
         Entity entity = id != null ? (Entity)ENTITY_CACHE.get(id) : null;
         if (entity == null && id != null) {
            entity = Minecraft.getInstance().level.getEntity(id);
         }

         return getAvatar(entity);
      }
   }

   public static Entity getEntity(EntityRenderState state) {
      if (Minecraft.getInstance().level == null || state == null) {
         return null;
      } else if (state instanceof AvatarRenderState playerRenderState) {
         Entity entity = (Entity)ENTITY_CACHE.get(playerRenderState.id);
         return entity != null ? entity : Minecraft.getInstance().level.getEntity(playerRenderState.id);
      } else {
         Integer id = ((FiguraEntityRenderStateExtension)state).figura$getEntityId();
         Entity entity = id != null ? (Entity)ENTITY_CACHE.get(id) : null;
         return entity == null && id != null ? Minecraft.getInstance().level.getEntity(id) : entity;
      }
   }

   public static Avatar getAvatar(Entity entity) {
      if (!panic && Minecraft.getInstance().level != null && entity != null) {
         UUID uuid = entity.getUUID();
         if (entity instanceof Player) {
            Avatar avatar = getAvatarForPlayer(uuid);
            if (avatar != null) {
               return avatar;
            }
         }

         return getAvatarForEntity(entity);
      } else {
         return null;
      }
   }

   public static Avatar getLoadedAvatar(UUID owner) {
      if (!panic && Minecraft.getInstance().level != null) {
         UserData user = LOADED_USERS.get(owner);
         return user == null ? null : user.getMainAvatar();
      } else {
         return null;
      }
   }

   public static List<Avatar> getLoadedAvatars() {
      List<Avatar> list = new ArrayList<>();

      for (UserData user : LOADED_USERS.values()) {
         Avatar avatar = user.getMainAvatar();
         if (avatar != null && avatar.nbt != null) {
            list.add(avatar);
         }
      }

      return list;
   }

   public static void clearAvatars(UUID id) {
      FETCHED_USERS.remove(id);
      UserData user = LOADED_USERS.get(id);
      if (user != null) {
         user.clear();
      }

      NetworkStuff.clear(id);
      FiguraMod.debug("Cleared avatars of " + id);
   }

   public static void clearCEMAvatars() {
      ObjectIterator var0 = LOADED_CEM.values().iterator();

      while (var0.hasNext()) {
         Avatar avatar = (Avatar)var0.next();
         avatar.clean();
      }

      LOADED_CEM.clear();
   }

   public static void clearAllAvatars() {
      for (UUID id : LOADED_USERS.keySet()) {
         clearAvatars(id);
      }

      LOADED_USERS.clear();
      FETCHED_USERS.clear();
      ENTITY_CACHE.clear();
      clearCEMAvatars();
      localUploaded = true;
      AvatarList.selectedEntry = null;
      LocalAvatarLoader.loadAvatar(null, null);
      FiguraMod.LOGGER.info("Cleared all avatars");
   }

   public static void reloadAvatar(UUID id) {
      if (!localUploaded && FiguraMod.isLocal(id)) {
         loadLocalAvatar(LocalAvatarLoader.getLastLoadedPath());
      } else {
         clearAvatars(id);
      }
   }

   public static void loadLocalAvatar(Path path) {
      UUID id = FiguraMod.getLocalPlayerUUID();
      clearAvatars(id);
      FETCHED_USERS.add(id);
      UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
      LocalAvatarLoader.loadAvatar(path, user);
      localUploaded = false;
   }

   public static void loadPreviewAvatar(UUID id, Path path) {
      clearAvatars(id);
      FETCHED_USERS.add(id);
      UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
      LocalAvatarLoader.loadPreviewAvatar(path, user);
   }

   public static Avatar loadEntityAvatar(Entity entity, CompoundTag nbt) {
      Avatar targetAvatar = new Avatar(entity);
      targetAvatar.load(nbt);
      LOADED_CEM.put(entity.getId(), targetAvatar);
      ENTITY_CACHE.putIfAbsent(entity.getId(), entity);
      return targetAvatar;
   }

   public static Avatar loadEntityAvatar(EntityRenderState entity, CompoundTag nbt) {
      Avatar targetAvatar = new Avatar(entity);
      targetAvatar.load(nbt);
      Integer id = entity instanceof AvatarRenderState playerRenderState
         ? playerRenderState.id
         : ((FiguraEntityRenderStateExtension)entity).figura$getEntityId();
      LOADED_CEM.put(id, targetAvatar);
      ENTITY_CACHE.putIfAbsent(id, WorldAPI.getCurrentWorld().getEntity(id));
      return targetAvatar;
   }

   public static void setAvatar(UUID id, CompoundTag nbt) {
      try {
         UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
         clearAvatars(id);
         user.loadAvatar(nbt);
      } catch (Exception e) {
         FiguraMod.LOGGER.error("Failed to set avatar for " + id, e);
      }
   }

   private static void fetchBackend(UUID id) {
      if (!FETCHED_USERS.contains(id)) {
         FETCHED_USERS.add(id);
         if (EntityUtils.checkInvalidPlayer(id)) {
            FiguraMod.debug("Voiding userdata for " + id);
         } else {
            UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
            FiguraMod.debug("Getting userdata for " + id);
            NetworkStuff.getUser(user);
         }
      }
   }

   public static Pair<BitSet, BitSet> getBadges(UUID id) {
      UserData user = LOADED_USERS.get(id);
      if (user == null) {
         return null;
      }

      Pair<BitSet, BitSet> badges = user.getBadges();
      if (badges != null) {
         return badges;
      }

      badges = Badges.emptyBadges();
      user.loadBadges(badges);
      return badges;
   }

   public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
      LiteralArgumentBuilder<FiguraClientCommandSource> root = LiteralArgumentBuilder.literal("set_avatar");
      RequiredArgumentBuilder<FiguraClientCommandSource, String> target = RequiredArgumentBuilder.argument("target", StringArgumentType.word());
      RequiredArgumentBuilder<FiguraClientCommandSource, String> source = RequiredArgumentBuilder.argument("source", StringArgumentType.word());
      source.executes(context -> {
         String s = StringArgumentType.getString(context, "source");
         String t = StringArgumentType.getString(context, "target");

         UUID sourceUUID;
         UUID targetUUID;
         try {
            sourceUUID = UUID.fromString(s);
            targetUUID = UUID.fromString(t);
         } catch (Exception e) {
            ((FiguraClientCommandSource)context.getSource()).figura$sendError(Component.literal("Failed to parse uuids"));
            return 0;
         }

         UserData user = LOADED_USERS.get(sourceUUID);
         Avatar avatar = user == null ? null : user.getMainAvatar();
         if (avatar == null || avatar.nbt == null) {
            ((FiguraClientCommandSource)context.getSource()).figura$sendError(Component.literal("No source Avatar found"));
            return 0;
         }

         if (LOADED_USERS.get(targetUUID) != null) {
            setAvatar(targetUUID, avatar.nbt);
            if (FiguraMod.isLocal(targetUUID)) {
               localUploaded = true;
            }

            ((FiguraClientCommandSource)context.getSource()).figura$sendFeedback(Component.literal("Set avatar for " + t));
            return 1;
         } else {
            Entity targetEntity = EntityUtils.getEntityByUUID(targetUUID);
            if (targetEntity == null) {
               ((FiguraClientCommandSource)context.getSource()).figura$sendError(Component.literal("Target entity not found"));
               return 0;
            } else {
               loadEntityAvatar(targetEntity, avatar.nbt);
               return 1;
            }
         }
      });
      target.then(source);
      root.then(target);
      return root;
   }
}
