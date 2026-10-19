package org.figuramc.figura.lua.api.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.EntityAccessor;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "EntityAPI", value = "entity")
public class EntityAPI<T extends Entity> {
   @NotNull
   protected final UUID entityUUID;
   @NotNull
   protected T entity;
   private String cacheType;

   public EntityAPI(T entity) {
      this.entity = entity;
      this.entityUUID = entity.getUUID();
   }

   public static EntityAPI<?> wrap(Entity e) {
      if (e == null) {
         return null;
      } else if (e instanceof Player p) {
         return new PlayerAPI(p);
      } else {
         return e instanceof LivingEntity le ? new LivingEntityAPI<>(le) : new EntityAPI<>(e);
      }
   }

   protected final boolean checkEntity() {
      boolean thingy = true;
      if (this.entity.isRemoved() || this.getLevel() != Minecraft.getInstance().level) {
         T newEntityInstance = (T)EntityUtils.getEntityByUUID(this.entityUUID);
         thingy = newEntityInstance != null;
         if (thingy) {
            this.entity = newEntityInstance;
         }
      }

      return thingy;
   }

   protected Level getLevel() {
      return ((EntityAccessor)this.entity).getLevel();
   }

   @NotNull
   public T getEntity() {
      return this.entity;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_loaded")
   public boolean isLoaded() {
      return this.checkEntity();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "delta")}, value = "entity.get_pos")
   public FiguraVec3 getPos(Float delta) {
      this.checkEntity();
      if (delta == null) {
         delta = 1.0F;
      }

      return FiguraVec3.fromVec3(this.entity.getPosition(delta));
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "delta")}, value = "entity.get_rot")
   public FiguraVec2 getRot(Float delta) {
      this.checkEntity();
      if (delta == null) {
         delta = 1.0F;
      }

      return FiguraVec2.of(Mth.lerp(delta, this.entity.xRotO, this.entity.getXRot()), Mth.lerp(delta, this.entity.yRotO, this.entity.getYRot()));
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_uuid")
   public String getUUID() {
      return this.entityUUID.toString();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_type")
   public String getType() {
      this.checkEntity();
      return this.cacheType != null ? this.cacheType : (this.cacheType = BuiltInRegistries.ENTITY_TYPE.getKey(this.entity.getType()).toString());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_velocity")
   public FiguraVec3 getVelocity() {
      this.checkEntity();
      return FiguraVec3.of(this.entity.getX() - this.entity.xOld, this.entity.getY() - this.entity.yOld, this.entity.getZ() - this.entity.zOld);
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_look_dir")
   public FiguraVec3 getLookDir() {
      this.checkEntity();
      return FiguraVec3.fromVec3(this.entity.getLookAngle());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_frozen_ticks")
   public int getFrozenTicks() {
      this.checkEntity();
      return this.entity.getTicksFrozen();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_max_air")
   public int getMaxAir() {
      this.checkEntity();
      return this.entity.getMaxAirSupply();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_dimension_name")
   public String getDimensionName() {
      this.checkEntity();
      return this.getLevel().dimension().identifier().toString();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_pose")
   public String getPose() {
      this.checkEntity();
      return this.entity.getPose().toString();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_vehicle")
   public EntityAPI<?> getVehicle() {
      this.checkEntity();
      return wrap(this.entity.getVehicle());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_on_ground")
   public boolean isOnGround() {
      this.checkEntity();
      return this.entity.onGround();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_eye_height")
   public float getEyeHeight() {
      this.checkEntity();
      return this.entity.getEyeHeight(this.entity.getPose());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_bounding_box")
   public FiguraVec3 getBoundingBox() {
      this.checkEntity();
      EntityDimensions dim = this.entity.getDimensions(this.entity.getPose());
      return FiguraVec3.of(dim.width(), dim.height(), dim.width());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_name")
   public String getName() {
      this.checkEntity();
      return this.entity.getName().getString();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_wet")
   public boolean isWet() {
      this.checkEntity();
      return this.entity.isInWaterOrRain();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_in_water")
   public boolean isInWater() {
      this.checkEntity();
      return this.entity.isInWater();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_underwater")
   public boolean isUnderwater() {
      this.checkEntity();
      return this.entity.isUnderWater();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_in_lava")
   public boolean isInLava() {
      this.checkEntity();
      return this.entity.isInLava();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_in_rain")
   public boolean isInRain() {
      this.checkEntity();
      BlockPos blockPos = this.entity.blockPosition();
      return this.getLevel().isRainingAt(blockPos)
         || this.getLevel().isRainingAt(new BlockPos(blockPos.getX(), (int)this.entity.getBoundingBox().maxY, (int)this.entity.getZ()));
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.has_avatar")
   public boolean hasAvatar() {
      this.checkEntity();
      return AvatarManager.getAvatar(this.entity) != null;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_sprinting")
   public boolean isSprinting() {
      this.checkEntity();
      return this.entity.isSprinting();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_eye_y")
   public double getEyeY() {
      this.checkEntity();
      return this.entity.getEyeY();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_glowing")
   public boolean isGlowing() {
      this.checkEntity();
      return this.entity.isCurrentlyGlowing();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_invisible")
   public boolean isInvisible() {
      this.checkEntity();
      return this.entity.isInvisible();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_silent")
   public boolean isSilent() {
      this.checkEntity();
      return this.entity.isSilent();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_sneaking")
   public boolean isSneaking() {
      this.checkEntity();
      return this.entity.isDiscrete();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_crouching")
   public boolean isCrouching() {
      this.checkEntity();
      return this.entity.isCrouching();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "ignoreY")}, value = "entity.is_moving")
   public boolean isMoving(boolean ignoreY) {
      this.checkEntity();
      return this.entity.getX() != this.entity.xOld || !ignoreY && this.entity.getY() != this.entity.yOld || this.entity.getZ() != this.entity.zOld;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_falling")
   public boolean isFalling() {
      this.checkEntity();
      return !this.entity.onGround() && this.entity.getY() < this.entity.yOld;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = int.class, argumentNames = "index"), value = "entity.get_item")
   public ItemStackAPI getItem(int index) {
      this.checkEntity();
      if (--index < 0) {
         return null;
      }

      int i = 0;
      if (this.entity instanceof LivingEntity) {
         for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = ((LivingEntity)this.entity).getItemBySlot(slot);
            if (i == index) {
               return ItemStackAPI.verify(item);
            }

            i++;
         }
      }

      return null;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_nbt")
   public LuaTable getNbt() {
      this.checkEntity();
      TagValueOutput tag = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, WorldAPI.getCurrentWorld().registryAccess());
      this.entity.saveWithoutId(tag);
      return (LuaTable)NbtToLua.convert(tag.buildResult());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_on_fire")
   public boolean isOnFire() {
      this.checkEntity();
      return this.entity.displayFireAnimation();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_alive")
   public boolean isAlive() {
      this.checkEntity();
      return this.entity.isAlive();
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_permission_level")
   public int getPermissionLevel() {
      this.checkEntity();
      if (this.entity instanceof Player p) {
         PermissionSet permissionSet = p.permissions();
         if (permissionSet.hasPermission(Permissions.COMMANDS_OWNER)) {
            return 4;
         }

         if (permissionSet.hasPermission(Permissions.COMMANDS_ADMIN)) {
            return 3;
         }

         if (permissionSet.hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return 2;
         }

         if (permissionSet.hasPermission(Permissions.COMMANDS_MODERATOR)) {
            return 1;
         }
      }

      return 0;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_passengers")
   public List<EntityAPI<?>> getPassengers() {
      this.checkEntity();
      List<EntityAPI<?>> list = new ArrayList<>();

      for (Entity passenger : this.entity.getPassengers()) {
         list.add(wrap(passenger));
      }

      return list;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_controlling_passenger")
   public EntityAPI<?> getControllingPassenger() {
      this.checkEntity();
      return wrap(this.entity.getControllingPassenger());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.get_controlled_vehicle")
   public EntityAPI<?> getControlledVehicle() {
      this.checkEntity();
      return wrap(this.entity.getControlledVehicle());
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.has_container")
   public boolean hasContainer() {
      this.checkEntity();
      return this.entity instanceof ContainerEntity;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.has_inventory")
   public boolean hasInventory() {
      this.checkEntity();
      return this.entity instanceof HasCustomInventoryScreen;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload,
            @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "ignoreLiquids"),
            @LuaMethodOverload(argumentTypes = {Boolean.class, Double.class}, argumentNames = {"ignoreLiquids", "distance"})
      },
      value = "entity.get_targeted_block"
   )
   public Object[] getTargetedBlock(boolean ignoreLiquids, Double distance) {
      this.checkEntity();
      if (distance == null) {
         distance = 20.0;
      }

      distance = Math.max(Math.min(distance, 20.0), -20.0);
      HitResult result = this.entity.pick(distance, 1.0F, !ignoreLiquids);
      return LuaUtils.parseBlockHitResult(result);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "distance")},
      value = "entity.get_targeted_entity"
   )
   public Object[] getTargetedEntity(Double distance) {
      this.checkEntity();
      if (distance == null) {
         distance = 20.0;
      }

      distance = Math.max(Math.min(distance, 20.0), 0.0);
      Vec3 vec3 = this.entity.getEyePosition(1.0F);
      HitResult result = this.entity.pick(distance, 1.0F, false);
      distance = result.getLocation().distanceToSqr(vec3);
      Vec3 vec32 = this.entity.getViewVector(1.0F);
      Vec3 vec33 = vec3.add(vec32.x * distance, vec32.y * distance, vec32.z * distance);
      AABB aABB = this.entity.getBoundingBox().expandTowards(vec32.scale(distance)).inflate(1.0);
      EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.entity, vec3, vec33, aABB, e -> e != this.entity, distance);
      return entityHit != null ? new Object[]{wrap(entityHit.getEntity()), FiguraVec3.fromVec3(entityHit.getLocation())} : null;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = {String.class, Double.class}, argumentNames = {"type", "radius"}),
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "type"),
            @LuaMethodOverload
      },
      value = "entity.get_nearest_entity"
   )
   public EntityAPI<?> getNearestEntity(String type, Double radius) {
      this.checkEntity();
      radius = radius != null ? radius : 20.0;
      EntityType<?> entityType;
      if (type != null) {
         Identifier id = Identifier.tryParse(type);
         if (id == null) {
            throw new LuaError("Invalid entity type: " + type);
         }

         entityType = (EntityType<?>)((Reference)BuiltInRegistries.ENTITY_TYPE.get(id).get()).value();
      } else {
         entityType = null;
      }

      FiguraVec3 pos = this.getPos(1.0F);
      AABB aabb = new AABB(pos.offseted(-radius).asVec3(), pos.offseted(radius).asVec3());
      return this.getLevel()
         .getEntities(this.entity, aabb)
         .stream()
         .filter(e -> entityType == null || e.getType() == entityType)
         .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos.x(), pos.y(), pos.z())))
         .map(EntityAPI::wrap)
         .orElse(null);
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key")}, value = "entity.get_variable")
   public LuaValue getVariable(String key) {
      this.checkEntity();
      Avatar a = AvatarManager.getAvatar(this.entity);
      LuaTable table = a != null && a.luaRuntime != null ? a.luaRuntime.avatar_meta.storedStuff : new LuaTable();
      LuaTable var4 = new ReadOnlyLuaTable(table);
      return (LuaValue)(key == null ? var4 : var4.get(key));
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_living")
   public boolean isLiving() {
      return this instanceof LivingEntityAPI;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity.is_player")
   public boolean isPlayer() {
      return this instanceof PlayerAPI;
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {boolean.class, EntityAPI.class, EntityAPI.class}))
   public boolean __eq(EntityAPI<?> rhs) {
      return this.entity.equals(rhs.entity);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {String.class, EntityAPI.class}))
   public String __tostring() {
      return this.toString();
   }

   @Override
   public String toString() {
      this.checkEntity();
      return (this.entity.hasCustomName() ? this.entity.getCustomName().getString() + " (" + this.getType() + ")" : this.getType()) + " (Entity)";
   }
}
