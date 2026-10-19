package org.figuramc.figura.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Entity.class, priority = 999)
public class EntityMixin {
   @ModifyReturnValue(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
   private Vec3 getEyePosition(Vec3 original) {
      return this.figura$offsetEyePos(original);
   }

   private Vec3 figura$offsetEyePos(Vec3 original) {
      Avatar avatar = AvatarManager.getAvatar((Entity)(Object)this);
      if (avatar != null && avatar.luaRuntime != null) {
         FiguraVec3 vec = avatar.luaRuntime.renderer.eyeOffset;
         return vec != null ? original.add(vec.asVec3()) : original;
      } else {
         return original;
      }
   }
}
