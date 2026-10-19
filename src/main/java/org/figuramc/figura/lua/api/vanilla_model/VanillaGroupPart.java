package org.figuramc.figura.lua.api.vanilla_model;

import java.util.Collection;
import java.util.HashMap;
import net.minecraft.client.model.EntityModel;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;

@LuaWhitelist
@LuaTypeDoc(name = "VanillaModelGroup", value = "vanilla_group_part")
public class VanillaGroupPart extends VanillaPart {
   private final Collection<VanillaPart> cachedParts;
   private final HashMap<String, VanillaPart> partMap = new HashMap<>();

   public VanillaGroupPart(Avatar owner, String name, VanillaPart... parts) {
      super(owner, name);

      for (VanillaPart part : parts) {
         this.partMap.put(part.name, part);
      }

      this.cachedParts = this.partMap.values();
   }

   @Override
   public void save(EntityModel<?> model) {
      for (VanillaPart part : this.cachedParts) {
         part.save(model);
      }
   }

   @Override
   public void preTransform(EntityModel<?> model) {
      for (VanillaPart part : this.cachedParts) {
         part.preTransform(model);
      }
   }

   @Override
   public void posTransform(EntityModel<?> model) {
      for (VanillaPart part : this.cachedParts) {
         part.posTransform(model);
      }
   }

   @Override
   public void restore(EntityModel<?> model) {
      for (VanillaPart part : this.cachedParts) {
         part.restore(model);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "visible"),
      aliases = "visible",
      value = "vanilla_group_part.set_visible"
   )
   @Override
   public VanillaPart setVisible(Boolean visible) {
      for (VanillaPart part : this.cachedParts) {
         part.setVisible(visible);
      }

      return super.setVisible(visible);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "pos"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "pos",
      value = "vanilla_group_part.set_pos"
   )
   @Override
   public VanillaPart setPos(Object x, Double y, Double z) {
      for (VanillaPart part : this.cachedParts) {
         part.setPos(x, y, z);
      }

      return super.setPos(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "rot"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "rot",
      value = "vanilla_group_part.set_rot"
   )
   @Override
   public VanillaPart setRot(Object x, Double y, Double z) {
      for (VanillaPart part : this.cachedParts) {
         part.setRot(x, y, z);
      }

      return super.setRot(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "offsetRot"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "offsetRot",
      value = "vanilla_group_part.set_offset_rot"
   )
   @Override
   public VanillaPart setOffsetRot(Object x, Double y, Double z) {
      for (VanillaPart part : this.cachedParts) {
         part.setOffsetRot(x, y, z);
      }

      return super.setOffsetRot(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "scale"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "scale",
      value = "vanilla_group_part.set_scale"
   )
   @Override
   public VanillaPart setScale(Object x, Double y, Double z) {
      for (VanillaPart part : this.cachedParts) {
         part.setScale(x, y, z);
      }

      return super.setScale(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "offsetScale"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "offsetScale",
      value = "vanilla_group_part.set_offset_scale"
   )
   @Override
   public VanillaPart setOffsetScale(Object x, Double y, Double z) {
      for (VanillaPart part : this.cachedParts) {
         part.setOffsetScale(x, y, z);
      }

      return super.setOffsetScale(x, y, z);
   }

   @LuaWhitelist
   public Object __index(String key) {
      return this.partMap.get(key);
   }

   @Override
   public String toString() {
      return "VanillaModelGroup";
   }
}
