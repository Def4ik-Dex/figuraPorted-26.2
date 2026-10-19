package org.figuramc.figura.lua.api.vanilla_model;

import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;

@LuaWhitelist
@LuaTypeDoc(name = "VanillaModelPart", value = "vanilla_model_part")
public class VanillaModelPart extends VanillaPart {
   private final ParentType parentType;
   private final Function<EntityModel<?>, ModelPart> provider;
   private float backupPosX;
   private float backupPosY;
   private float backupPosZ;
   private float backupRotX;
   private float backupRotY;
   private float backupRotZ;
   private float backupScaleX;
   private float backupScaleY;
   private float backupScaleZ;
   private boolean originVisible;
   private boolean saved;
   private final FiguraVec3 originRot = FiguraVec3.of();
   private final FiguraVec3 originPos = FiguraVec3.of();
   private final FiguraVec3 originScale = FiguraVec3.of();

   public VanillaModelPart(Avatar owner, String name, ParentType parentType, Function<EntityModel<?>, ModelPart> provider) {
      super(owner, name);
      this.parentType = parentType;
      this.provider = provider;
   }

   private ModelPart getPart(EntityModel<?> model) {
      return this.provider == null ? null : this.provider.apply(model);
   }

   @Override
   public void save(EntityModel<?> model) {
      this.saved = false;
      ModelPart part = this.getPart(model);
      if (part != null) {
         this.originRot.set(-part.xRot, -part.yRot, part.zRot);
         this.originRot.scale(180.0 / Math.PI);
         FiguraVec3 pivot = this.parentType.offset.copy();
         pivot.subtract(part.x, part.y, part.z);
         pivot.multiply(1.0, -1.0, -1.0);
         this.originPos.set(pivot);
         this.originScale.set(part.xScale, part.yScale, part.zScale);
         this.originVisible = part.visible;
         this.backupPosX = part.x;
         this.backupPosY = part.y;
         this.backupPosZ = part.z;
         this.backupRotX = part.xRot;
         this.backupRotY = part.yRot;
         this.backupRotZ = part.zRot;
         this.backupScaleX = part.xScale;
         this.backupScaleY = part.yScale;
         this.backupScaleZ = part.zScale;
         this.saved = true;
      }
   }

   @Override
   public void preTransform(EntityModel<?> model) {
      if (this.saved) {
         ModelPart part = this.getPart(model);
         if (part != null) {
            if (this.pos != null) {
               part.x = part.x + (float)(-this.pos.x);
               part.y = part.y + (float)(-this.pos.y);
               part.z = part.z + (float)this.pos.z;
            }

            if (this.rot != null) {
               FiguraVec3 rot = this.rot.toRad();
               part.setRotation((float)(-rot.x), (float)(-rot.y), (float)rot.z);
            }

            if (this.offsetRot != null) {
               part.offsetRotation(this.offsetRot.toRad().mul(-1, -1.0, 1.0).asVec3f());
            }

            if (this.scale != null) {
               part.xScale = (float)this.scale.x;
               part.yScale = (float)this.scale.y;
               part.zScale = (float)this.scale.z;
            }

            if (this.offsetScale != null) {
               part.offsetScale(this.offsetScale.asVec3f());
            }
         }
      }
   }

   @Override
   public void posTransform(EntityModel<?> model) {
      if (this.visible != null) {
         ModelPart part = this.getPart(model);
         if (part != null) {
            part.visible = this.visible;
         }
      }
   }

   @Override
   public void restore(EntityModel<?> model) {
      ModelPart part = this.getPart(model);
      if (part != null) {
         part.visible = this.originVisible;
         if (this.saved) {
            part.x = this.backupPosX;
            part.y = this.backupPosY;
            part.z = this.backupPosZ;
            part.xRot = this.backupRotX;
            part.yRot = this.backupRotY;
            part.zRot = this.backupRotZ;
            part.xScale = this.backupScaleX;
            part.yScale = this.backupScaleY;
            part.zScale = this.backupScaleZ;
         }
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_model_part.get_origin_visible")
   public boolean getOriginVisible() {
      return this.originVisible;
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_model_part.get_origin_rot")
   public FiguraVec3 getOriginRot() {
      return this.originRot.copy();
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_model_part.get_origin_pos")
   public FiguraVec3 getOriginPos() {
      return this.originPos.copy();
   }

   @LuaWhitelist
   @LuaMethodDoc("vanilla_model_part.get_origin_scale")
   public FiguraVec3 getOriginScale() {
      return this.originScale.copy();
   }

   @Override
   public String toString() {
      return "VanillaModelPart";
   }
}
