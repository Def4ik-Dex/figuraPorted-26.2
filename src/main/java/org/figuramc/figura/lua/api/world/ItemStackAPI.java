package org.figuramc.figura.lua.api.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaTable;

@LuaWhitelist
@LuaTypeDoc(name = "ItemStack", value = "itemstack")
public class ItemStackAPI {
   public final ItemStack itemStack;
   @LuaWhitelist
   @LuaFieldDoc("itemstack.id")
   public final String id;
   @LuaWhitelist
   @LuaFieldDoc("itemstack.tag")
   public final LuaTable tag;

   public static ItemStackAPI verify(ItemStack itemStack) {
      if (itemStack == null || itemStack == ItemStack.EMPTY) {
         itemStack = Items.AIR.getDefaultInstance();
      }

      return new ItemStackAPI(itemStack);
   }

   public ItemStackAPI(ItemStack itemStack, LuaTable tag) {
      this.itemStack = itemStack;
      this.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
      this.tag = tag;
   }

   public ItemStackAPI(ItemStack itemStack) {
      this.itemStack = itemStack;
      this.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
      LuaTable tag = itemStack.getComponents() != DataComponentMap.EMPTY
         ? (LuaTable)NbtToLua.convert(NbtToLua.convertToNbt(itemStack.getComponents()))
         : new LuaTable();
      LuaUtils.addLegacyNbtNames(tag, tag);
      this.tag = new ReadOnlyLuaTable(tag);
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_id")
   public String getID() {
      return this.id;
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_tag")
   public LuaTable getTag() {
      return this.tag;
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_count")
   public int getCount() {
      return this.itemStack.getCount();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_damage")
   public int getDamage() {
      return this.itemStack.getDamageValue();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_pop_time")
   public int getPopTime() {
      return this.itemStack.getPopTime();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.has_glint")
   public boolean hasGlint() {
      return this.itemStack.hasFoil();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_tags")
   public List<String> getTags() {
      List<String> list = new ArrayList<>();
      Registry<Item> registry = WorldAPI.getCurrentWorld().registryAccess().lookupOrThrow(Registries.ITEM);
      Optional<ResourceKey<Item>> key = registry.getResourceKey(this.itemStack.getItem());
      if (key.isEmpty()) {
         return list;
      }

      for (TagKey<Item> itemTagKey : registry.getOrThrow(key.get()).tags().toList()) {
         list.add(itemTagKey.location().toString());
      }

      return list;
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_block_item")
   public boolean isBlockItem() {
      return this.itemStack.getItem() instanceof BlockItem;
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_food")
   public boolean isFood() {
      return this.itemStack.getComponents().has(DataComponents.FOOD);
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_use_action")
   public String getUseAction() {
      return this.itemStack.getUseAnimation().name();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_name")
   public String getName() {
      return this.itemStack.getHoverName().getString();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_max_count")
   public int getMaxCount() {
      return this.itemStack.getMaxStackSize();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_rarity")
   public String getRarity() {
      return this.itemStack.getRarity().name();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_enchantable")
   public boolean isEnchantable() {
      return this.itemStack.isEnchantable();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_max_damage")
   public int getMaxDamage() {
      return this.itemStack.getMaxDamage();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_damageable")
   public boolean isDamageable() {
      return this.itemStack.isDamageableItem();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_stackable")
   public boolean isStackable() {
      return this.itemStack.isStackable();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_repair_cost")
   public int getRepairCost() {
      return this.itemStack != null && this.itemStack != ItemStack.EMPTY && this.itemStack.getComponents().has(DataComponents.REPAIR_COST)
         ? (Integer)this.itemStack.get(DataComponents.REPAIR_COST)
         : 0;
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_use_duration")
   public int getUseDuration() {
      return this.itemStack.getUseDuration(Minecraft.getInstance().player);
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.to_stack_string")
   public String toStackString() {
      ItemStack stack = this.itemStack;
      String ret = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      String components = LuaUtils.getItemStackString(stack);
      if (!components.isEmpty()) {
         ret = ret + components;
      }

      return ret;
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_armor")
   public boolean isArmor() {
      return this.itemStack.getItem().components().has(DataComponents.EQUIPPABLE);
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.is_tool")
   public boolean isTool() {
      return this.itemStack.getItem().components().has(DataComponents.TOOL);
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_equipment_slot")
   public String getEquipmentSlot() {
      return Minecraft.getInstance().player.getEquipmentSlotForItem(this.itemStack).name();
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.copy")
   public ItemStackAPI copy() {
      return new ItemStackAPI(this.itemStack.copy(), this.tag);
   }

   @LuaWhitelist
   @LuaMethodDoc("itemstack.get_blockstate")
   public BlockStateAPI getBlockstate() {
      return this.itemStack.getItem() instanceof BlockItem blockItem ? new BlockStateAPI(blockItem.getBlock().defaultBlockState(), null) : null;
   }

   @LuaWhitelist
   public boolean __eq(ItemStackAPI other) {
      if (this == other) {
         return true;
      }

      ItemStack t = this.itemStack;
      ItemStack o = other.itemStack;
      if (t.getCount() != o.getCount()) {
         return false;
      }

      if (!t.is(o.getItem())) {
         return false;
      }

      DataComponentMap tag1 = t.getComponents();
      DataComponentMap tag2 = o.getComponents();
      return tag1 == null && tag2 != null ? false : tag1 == null || tag1.equals(tag2);
   }

   @LuaWhitelist
   public Object __index(String arg) {
      if (arg == null) {
         return null;
      }

      return switch (arg) {
         case "id" -> this.id;
         case "tag" -> this.tag;
         default -> null;
      };
   }

   @Override
   public String toString() {
      return this.id + " x" + this.getCount() + " (ItemStack)";
   }
}
