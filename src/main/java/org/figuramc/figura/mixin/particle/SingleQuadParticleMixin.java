package org.figuramc.figura.mixin.particle;

import net.minecraft.client.particle.SingleQuadParticle;
import org.figuramc.figura.ducks.SingleQuadParticleAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin implements SingleQuadParticleAccessor {
   @Shadow
   protected float quadSize;

   @Override
   public void figura$fixQuadSize() {
      this.quadSize = 0.2F;
   }

   @Invoker("setAlpha")
   @Override
   public abstract void figura$setParticleAlpha(float var1);

   @Accessor
   public abstract float getRCol();

   @Accessor
   public abstract float getGCol();

   @Accessor
   public abstract float getBCol();

   @Accessor
   public abstract float getAlpha();
}
