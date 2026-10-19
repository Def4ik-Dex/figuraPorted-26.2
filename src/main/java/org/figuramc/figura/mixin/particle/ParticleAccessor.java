package org.figuramc.figura.mixin.particle;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
   @Accessor
   void setGravity(float var1);

   @Accessor
   float getGravity();

   @Accessor
   void setHasPhysics(boolean var1);

   @Accessor
   boolean getHasPhysics();

   @Accessor
   void setXo(double var1);

   @Accessor
   void setYo(double var1);

   @Accessor
   void setZo(double var1);

   @Accessor("x")
   double getX();

   @Accessor("y")
   double getY();

   @Accessor("z")
   double getZ();

   @Accessor
   float getBbWidth();

   @Accessor
   double getXd();

   @Accessor
   double getYd();

   @Accessor
   double getZd();
}
