package org.figuramc.figura.permissions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

public abstract class PermissionPack {
   public String name;
   private boolean visible = true;
   private final Map<Permissions, Integer> permissions = new HashMap<>();
   private final Map<String, Map<Permissions, Integer>> customPermissions = new HashMap<>();

   public PermissionPack(String name) {
      this.name = name;
   }

   public abstract MutableComponent getCategoryName();

   public abstract int getColor();

   public abstract Permissions.Category getCategory();

   public abstract void setCategory(PermissionPack.CategoryPermissionPack var1);

   public void loadNbt(CompoundTag nbt) {
      CompoundTag perms = nbt.getCompoundOrEmpty("permissions");

      for (Permissions setting : Permissions.DEFAULT) {
         if (perms.contains(setting.name)) {
            this.permissions.put(setting, perms.getIntOr(setting.name, 0));
         }
      }

      CompoundTag custom = nbt.getCompoundOrEmpty("custom");

      for (Entry<String, Collection<Permissions>> entry : PermissionManager.CUSTOM_PERMISSIONS.entrySet()) {
         String key = entry.getKey();
         Map<Permissions, Integer> map = new HashMap<>();
         CompoundTag customNbt = custom.getCompoundOrEmpty(key);

         for (Permissions setting : entry.getValue()) {
            if (customNbt.contains(setting.name)) {
               map.put(setting, customNbt.getIntOr(setting.name, 0));
            }
         }

         this.customPermissions.put(key, map);
      }
   }

   public void writeNbt(CompoundTag nbt) {
      nbt.putString("name", this.name);
      CompoundTag perms = new CompoundTag();

      for (Entry<Permissions, Integer> entry : this.permissions.entrySet()) {
         perms.putInt(entry.getKey().name, entry.getValue());
      }

      nbt.put("permissions", perms);
      CompoundTag custom = new CompoundTag();

      for (Entry<String, Map<Permissions, Integer>> entry : this.customPermissions.entrySet()) {
         CompoundTag customNbt = new CompoundTag();

         for (Entry<Permissions, Integer> entry2 : entry.getValue().entrySet()) {
            customNbt.putInt(entry2.getKey().name, entry2.getValue());
         }

         custom.put(entry.getKey(), customNbt);
      }

      nbt.put("custom", custom);
   }

   public int get(Permissions permissions) {
      Integer setting = this.permissions.get(permissions);
      if (setting != null) {
         return setting;
      }

      for (Map<Permissions, Integer> value : this.customPermissions.values()) {
         setting = value.get(permissions);
         if (setting != null) {
            return setting;
         }
      }

      return -1;
   }

   public void insert(Permissions permissions, Integer value, String id) {
      if (Permissions.DEFAULT.contains(permissions)) {
         this.permissions.put(permissions, value);
      } else {
         Map<Permissions, Integer> map = this.customPermissions.getOrDefault(id, new HashMap<>());
         map.put(permissions, value);
         this.customPermissions.put(id, map);
      }
   }

   public boolean hasChanges() {
      boolean bool = !this.permissions.isEmpty();
      if (!bool) {
         for (Map<Permissions, Integer> value : this.customPermissions.values()) {
            if (!value.isEmpty()) {
               return true;
            }
         }
      }

      return bool;
   }

   public boolean isChanged(Permissions permissions) {
      if (this.permissions.containsKey(permissions)) {
         return true;
      }

      for (Map<Permissions, Integer> map : this.customPermissions.values()) {
         if (map.containsKey(permissions)) {
            return true;
         }
      }

      return false;
   }

   public void reset(Permissions permissions) {
      this.permissions.remove(permissions);

      for (Map<Permissions, Integer> map : this.customPermissions.values()) {
         map.remove(permissions);
      }
   }

   public void clear() {
      this.permissions.clear();
      this.customPermissions.clear();
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public Map<Permissions, Integer> getPermissions() {
      return this.permissions;
   }

   public Map<String, Map<Permissions, Integer>> getCustomPermissions() {
      return this.customPermissions;
   }

   public static class CategoryPermissionPack extends PermissionPack {
      public final Permissions.Category category;

      public CategoryPermissionPack(Permissions.Category category) {
         super(category.name());
         this.category = category;
      }

      @Override
      public MutableComponent getCategoryName() {
         return this.category.text.copy();
      }

      @Override
      public int getColor() {
         return this.category.color;
      }

      @Override
      public Permissions.Category getCategory() {
         return this.category;
      }

      @Override
      public void setCategory(PermissionPack.CategoryPermissionPack newParent) {
      }

      @Override
      public int get(Permissions permissions) {
         int result = super.get(permissions);
         return result != -1 ? result : permissions.getDefault(this.getCategory());
      }
   }

   public static class PlayerPermissionPack extends PermissionPack {
      public PermissionPack.CategoryPermissionPack category;

      public PlayerPermissionPack(PermissionPack.CategoryPermissionPack category, String name) {
         super(name);
         this.category = category;
      }

      @Override
      public MutableComponent getCategoryName() {
         return this.category.getCategoryName();
      }

      @Override
      public int getColor() {
         return this.category.getColor();
      }

      @Override
      public Permissions.Category getCategory() {
         return this.category.getCategory();
      }

      @Override
      public void setCategory(PermissionPack.CategoryPermissionPack newParent) {
         this.category = newParent;
      }

      @Override
      public void writeNbt(CompoundTag nbt) {
         if (this.getCategory() != Permissions.Category.BLOCKED) {
            super.writeNbt(nbt);
         } else {
            nbt.putString("name", this.name);
         }

         nbt.putString("category", this.category.name);
      }

      @Override
      public int get(Permissions permissions) {
         int result = super.get(permissions);
         return result != -1 ? result : this.category.get(permissions);
      }

      @Override
      public boolean isVisible() {
         return this.category.isVisible();
      }
   }
}
