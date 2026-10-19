package org.figuramc.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(name = "VanillaPart", value = "vanilla_part")
public abstract class VanillaPart {
   protected final String name;
   protected final Avatar owner;
   protected Boolean visible;
   protected FiguraVec3 pos;
   protected FiguraVec3 rot;
   protected FiguraVec3 scale;
   protected FiguraVec3 offsetRot;
   protected FiguraVec3 offsetScale;

   public VanillaPart(Avatar owner, String name) {
      this.owner = owner;
      this.name = name;
   }

   public boolean checkVisible() {
      return this.visible == null || this.visible;
   }

   public abstract void save(EntityModel<?> var1);

   public abstract void preTransform(EntityModel<?> var1);

   public abstract void posTransform(EntityModel<?> var1);

   public abstract void restore(EntityModel<?> var1);

   @LuaWhitelist
   @LuaMethodDoc("vanilla_part.get_visible")
   public Boolean getVisible() {
      return this.visible;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "visible"),
      aliases = "visible",
      value = "vanilla_part.set_visible"
   )
   public VanillaPart setVisible(Boolean visible) {
      this.visible = visible;
      return this;
   }

   @LuaWhitelist
   public VanillaPart visible(Boolean visible) {
      return this.setVisible(visible);
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_part.get_pos")
   public FiguraVec3 getPos() {
      return this.pos;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "pos"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "pos",
      value = "vanilla_part.set_pos"
   )
   public VanillaPart setPos(Object x, Double y, Double z) {
      this.pos = LuaUtils.nullableVec3("setPos", x, y, z);
      return this;
   }

   @LuaWhitelist
   public VanillaPart pos(Object x, Double y, Double z) {
      this.setPos(x, y, z);
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_part.get_rot")
   public FiguraVec3 getRot() {
      return this.rot;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "rot"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "rot",
      value = "vanilla_part.set_rot"
   )
   public VanillaPart setRot(Object x, Double y, Double z) {
      this.rot = LuaUtils.nullableVec3("setRot", x, y, z);
      return this;
   }

   @LuaWhitelist
   public VanillaPart rot(Object x, Double y, Double z) {
      this.setRot(x, y, z);
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_part.get_offset_rot")
   public FiguraVec3 getOffsetRot() {
      return this.offsetRot;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "offsetRot"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "offsetRot",
      value = "vanilla_part.set_offset_rot"
   )
   public VanillaPart setOffsetRot(Object x, Double y, Double z) {
      this.offsetRot = LuaUtils.nullableVec3("setOffsetRot", x, y, z);
      return this;
   }

   @LuaWhitelist
   public VanillaPart offsetRot(Object x, Double y, Double z) {
      return this.setOffsetRot(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_part.get_scale")
   public FiguraVec3 getScale() {
      return this.scale;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "scale"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "scale",
      value = "vanilla_part.set_scale"
   )
   public VanillaPart setScale(Object x, Double y, Double z) {
      this.scale = x == null ? null : LuaUtils.parseOneArgVec("setScale", x, y, z, 1.0);
      return this;
   }

   @LuaWhitelist
   public VanillaPart scale(Object x, Double y, Double z) {
      this.setScale(x, y, z);
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_part.get_offset_scale")
   public FiguraVec3 getOffsetScale() {
      return this.offsetScale;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "offsetScale"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "offsetScale",
      value = "vanilla_part.set_offset_scale"
   )
   public VanillaPart setOffsetScale(Object x, Double y, Double z) {
      this.offsetScale = x == null ? null : LuaUtils.parseOneArgVec("setOffsetScale", x, y, z, 1.0);
      return this;
   }

   @LuaWhitelist
   public VanillaPart offsetScale(Object x, Double y, Double z) {
      return this.setOffsetScale(x, y, z);
   }

   @Override
   public String toString() {
      return "VanillaPart";
   }
}
