package org.figuramc.figura.lua.api.entity;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.mixin.LivingEntityAccessor;

@LuaWhitelist
@LuaTypeDoc(name = "LivingEntityAPI", value = "living_entity")
public class LivingEntityAPI<T extends LivingEntity> extends EntityAPI<T> {
   public LivingEntityAPI(T entity) {
      super(entity);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "delta")},
      value = "living_entity.get_body_yaw"
   )
   public double getBodyYaw(Float delta) {
      this.checkEntity();
      if (delta == null) {
         delta = 1.0F;
      }

      return Mth.lerp(delta, this.entity.yBodyRotO, this.entity.yBodyRot);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "offhand")},
      value = "living_entity.get_held_item"
   )
   public ItemStackAPI getHeldItem(boolean offhand) {
      this.checkEntity();
      return ItemStackAPI.verify(offhand ? this.entity.getOffhandItem() : this.entity.getMainHandItem());
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_active_item")
   public ItemStackAPI getActiveItem() {
      this.checkEntity();
      return ItemStackAPI.verify(this.entity.getUseItem());
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_active_item_time")
   public int getActiveItemTime() {
      this.checkEntity();
      return this.entity.getTicksUsingItem();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_health")
   public float getHealth() {
      this.checkEntity();
      return this.entity.getHealth();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_max_health")
   public float getMaxHealth() {
      this.checkEntity();
      return this.entity.getMaxHealth();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_armor")
   public float getArmor() {
      this.checkEntity();
      return this.entity.getArmorValue();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_death_time")
   public float getDeathTime() {
      this.checkEntity();
      return this.entity.deathTime;
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_arrow_count")
   public int getArrowCount() {
      this.checkEntity();
      return this.entity.getArrowCount();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_stinger_count")
   public int getStingerCount() {
      this.checkEntity();
      return this.entity.getStingerCount();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_left_handed")
   public boolean isLeftHanded() {
      this.checkEntity();
      return this.entity.getMainArm() == HumanoidArm.LEFT;
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_using_item")
   public boolean isUsingItem() {
      this.checkEntity();
      return this.entity.isUsingItem();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_active_hand")
   public String getActiveHand() {
      this.checkEntity();
      return this.entity.getUsedItemHand().name();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_climbing")
   public boolean isClimbing() {
      this.checkEntity();
      return this.entity.onClimbable();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_swing_time")
   public int getSwingTime() {
      this.checkEntity();
      return this.entity.swingTime;
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_swinging_arm")
   public boolean isSwingingArm() {
      this.checkEntity();
      return this.entity.swinging;
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_swing_arm")
   public String getSwingArm() {
      this.checkEntity();
      return this.entity.swinging ? this.entity.swingingArm.name() : null;
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_swing_duration")
   public int getSwingDuration() {
      this.checkEntity();
      return ((LivingEntityAccessor)this.entity).getSwingDuration();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_absorption_amount")
   public float getAbsorptionAmount() {
      this.checkEntity();
      return this.entity.getAbsorptionAmount();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_sensitive_to_water")
   public boolean isSensitiveToWater() {
      this.checkEntity();
      return this.entity.isSensitiveToWater();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.get_entity_category")
   public String getEntityCategory() {
      this.checkEntity();
      EntityType<?> mobType = this.entity.getType();
      if (mobType.builtInRegistryHolder().is(EntityTypeTags.ARTHROPOD)) {
         return "ARTHROPOD";
      } else if (mobType.builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
         return "UNDEAD";
      } else if (mobType.builtInRegistryHolder().is(EntityTypeTags.AQUATIC)) {
         return "WATER";
      } else {
         return mobType.builtInRegistryHolder().is(EntityTypeTags.ILLAGER) ? "ILLAGER" : "UNDEFINED";
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_gliding")
   public boolean isGliding() {
      this.checkEntity();
      return this.entity.isFallFlying();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_blocking")
   public boolean isBlocking() {
      this.checkEntity();
      return this.entity.isBlocking();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.is_visually_swimming")
   public boolean isVisuallySwimming() {
      this.checkEntity();
      return this.entity.isVisuallySwimming();
   }

   @LuaWhitelist
   @LuaMethodDoc("living_entity.riptide_spinning")
   public boolean riptideSpinning() {
      this.checkEntity();
      return this.entity.isAutoSpinAttack();
   }

   @Override
   public String toString() {
      this.checkEntity();
      return (this.entity.hasCustomName() ? this.entity.getCustomName().getString() + " (" + this.getType() + ")" : this.getType()) + " (LivingEntity)";
   }
}
