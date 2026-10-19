package org.figuramc.figura.lua;

import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class NbtToLua {
   private static final HashMap<Class<?>, Function<Tag, LuaValue>> CONVERTERS = new HashMap<Class<?>, Function<Tag, LuaValue>>() {
      {
         this.put(ByteTag.class, tag -> LuaValue.valueOf(((ByteTag)tag).byteValue()));
         this.put(ShortTag.class, tag -> LuaValue.valueOf(((ShortTag)tag).shortValue()));
         this.put(IntTag.class, tag -> LuaValue.valueOf(((IntTag)tag).intValue()));
         this.put(LongTag.class, tag -> LuaValue.valueOf(((LongTag)tag).longValue()));
         this.put(FloatTag.class, tag -> LuaValue.valueOf(((FloatTag)tag).floatValue()));
         this.put(DoubleTag.class, tag -> LuaValue.valueOf(((DoubleTag)tag).doubleValue()));
         this.put(CompoundTag.class, tag -> {
            LuaTable table = new LuaTable();
            CompoundTag compound = (CompoundTag)tag;

            for (String key : compound.keySet()) {
               table.set(key, NbtToLua.convert(compound.get(key)));
            }

            return table;
         });
         this.put(ByteArrayTag.class, tag -> NbtToLua.fromCollection((CollectionTag)tag));
         this.put(IntArrayTag.class, tag -> NbtToLua.fromCollection((CollectionTag)tag));
         this.put(LongArrayTag.class, tag -> NbtToLua.fromCollection((CollectionTag)tag));
         this.put(ListTag.class, tag -> NbtToLua.fromCollection((CollectionTag)tag));
      }
   };

   private static LuaValue fromCollection(CollectionTag tag) {
      LuaTable table = new LuaTable();
      int i = 1;

      for (Tag children : tag) {
         table.set(i, convert(children));
         i++;
      }

      return table;
   }

   public static LuaValue convert(Tag tag) {
      if (tag == null) {
         return null;
      }

      Class<?> clazz = tag.getClass();
      Function<Tag, LuaValue> builder = CONVERTERS.get(clazz);
      return (LuaValue)(builder == null ? LuaValue.valueOf((String)tag.asString().get()) : builder.apply(tag));
   }

   public static CompoundTag convertToNbt(DataComponentMap components) {
      if (components == null) {
         return null;
      }

      CompoundTag tag = new CompoundTag();
      DynamicOps<Tag> dynamicOps = WorldAPI.getCurrentWorld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
      components.forEach(typedDataComponent -> {
         Optional<Tag> optional = typedDataComponent.encodeValue(dynamicOps).result();
         Identifier resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typedDataComponent.type());
         if (optional.isPresent() && resourceLocation != null) {
            tag.put(resourceLocation.toString(), optional.get());
         }
      });
      return tag;
   }
}
