package org.figuramc.figura.mixin.render.layers.elytra;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraModel.class)
public interface ElytraModelAccessor {
   @Accessor
   ModelPart getLeftWing();

   @Accessor
   ModelPart getRightWing();
}
