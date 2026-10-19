package org.figuramc.figura.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AABB.class)
public interface AABBInvoker {
   @Invoker("getDirection")
   static Direction getDirection(
      AABB box, Vec3 intersectingVector, double[] traceDistanceResult, @Nullable Direction approachDirection, double deltaX, double deltaY, double deltaZ
   ) {
      throw new AssertionError();
   }
}
