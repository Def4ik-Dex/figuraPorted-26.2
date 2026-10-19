package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PoseStack.class)
public interface PoseStackAccessor {
   @Accessor("poses")
   @Final
   List<Pose> getPoseStack();

   @Accessor("lastIndex")
   int getLastIndex();
}
