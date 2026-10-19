package org.figuramc.figura.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.math.vector.FiguraVec3;

public class VanillaModelData {
   public final Map<ParentType, VanillaModelData.PartData> partMap = new HashMap<ParentType, VanillaModelData.PartData>() {
      {
         for (ParentType value : ParentType.values()) {
            if (value.provider != null) {
               this.put(value, new VanillaModelData.PartData());
            }
         }
      }
   };

   public void update(EntityModel<?> vanillaModel) {
      for (Entry<ParentType, VanillaModelData.PartData> entry : this.partMap.entrySet()) {
         ParentType parent = entry.getKey();
         if (vanillaModel != null) {
            this.update(parent, vanillaModel);
         }
      }
   }

   public void update(ParentType parent, EntityModel<?> model) {
      ModelPart part = parent.provider.func.apply(model);
      if (part != null) {
         this.update(parent, part);
      }
   }

   public void update(ParentType parent, ModelPart part) {
      VanillaModelData.PartData data = this.partMap.get(parent);
      if (data != null) {
         data.updateFromPart(part);
      }
   }

   public static class PartData {
      public final FiguraVec3 pos = FiguraVec3.of();
      public final FiguraVec3 rot = FiguraVec3.of();
      public final FiguraVec3 scale = FiguraVec3.of();
      public Boolean visible = null;

      private void updateFromPart(ModelPart model) {
         this.pos.set(model.x, model.y, -model.z);
         this.rot.set(Math.toDegrees(-model.xRot), Math.toDegrees(-model.yRot), Math.toDegrees(model.zRot));
         this.scale.set(model.xScale, model.yScale, model.zScale);
         this.visible = model.visible;
      }
   }
}
