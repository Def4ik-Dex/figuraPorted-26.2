package org.figuramc.figura.utils;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.mixin.ClientLevelInvoker;
import org.figuramc.figura.mixin.EntityAccessor;
import org.figuramc.figura.mixin.gui.PlayerTabOverlayAccessor;

public class EntityUtils {
   public static Entity getEntityByUUID(UUID uuid) {
      if (Minecraft.getInstance().level == null) {
         return null;
      }

      Entity entity = (Entity)((ClientLevelInvoker)Minecraft.getInstance().level).getEntityGetter().get(uuid);
      if (entity != null) {
         return entity;
      }

      ObjectIterator var2 = AvatarManager.ENTITY_CACHE.values().iterator();

      while (var2.hasNext()) {
         Entity cached = (Entity)var2.next();
         if (uuid.equals(cached.getUUID())) {
            return cached;
         }
      }

      return null;
   }

   public static Entity getViewedEntity(float distance) {
      Entity entity = Minecraft.getInstance().getCameraEntity();
      if (entity == null) {
         return null;
      }

      float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
      Vec3 entityEye = entity.getEyePosition(tickDelta);
      Vec3 viewVec = entity.getViewVector(tickDelta).scale(distance);
      AABB box = entity.getBoundingBox().expandTowards(viewVec).inflate(1.0, 1.0, 1.0);
      Vec3 raycastEnd = entityEye.add(viewVec);
      BlockHitResult blockResult = ((EntityAccessor)entity).getLevel().clip(new ClipContext(entityEye, raycastEnd, Block.VISUAL, Fluid.NONE, entity));
      double raycastDistanceSquared;
      if (blockResult != null) {
         raycastDistanceSquared = blockResult.getLocation().distanceToSqr(entityEye);
      } else {
         raycastDistanceSquared = distance * distance;
      }

      EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
         entity, entityEye, raycastEnd, box, entity1 -> !entity1.isSpectator() && entity1.isPickable(), raycastDistanceSquared
      );
      return entityHitResult != null ? entityHitResult.getEntity() : null;
   }

   public static PlayerInfo getPlayerInfo(UUID uuid) {
      ClientPacketListener connection = Minecraft.getInstance().getConnection();
      return connection == null ? null : connection.getPlayerInfo(uuid);
   }

   public static String getNameForUUID(UUID uuid) {
      PlayerInfo player = getPlayerInfo(uuid);
      if (player != null) {
         return player.getProfile().name();
      }

      Entity e = getEntityByUUID(uuid);
      return e != null ? e.getName().getString() : null;
   }

   public static Map<String, UUID> getPlayerList() {
      ClientPacketListener connection = Minecraft.getInstance().getConnection();
      if (connection != null && !connection.getOnlinePlayerIds().isEmpty()) {
         Map<String, UUID> playerList = new HashMap<>();

         for (UUID uuid : connection.getOnlinePlayerIds()) {
            PlayerInfo player = connection.getPlayerInfo(uuid);
            if (player != null) {
               playerList.put(player.getProfile().name(), uuid);
            }
         }

         return playerList;
      } else {
         return Map.of();
      }
   }

    public static List<PlayerInfo> getTabList() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null ? new ArrayList<>(connection.getOnlinePlayers()) : Collections.emptyList();
    }
   
    
    
    




   public static boolean checkInvalidPlayer(UUID id) {
      if (id.version() != 4) {
         return true;
      }

      PlayerInfo playerInfo = getPlayerInfo(id);
      if (playerInfo == null) {
         return false;
      }

      GameProfile profile = playerInfo.getProfile();
      String name = profile.name();
      return name != null && (name.isBlank() || name.charAt(0) == 0);
   }
}
