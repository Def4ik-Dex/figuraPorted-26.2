package org.figuramc.figura.mixin.particle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import org.figuramc.figura.ducks.ParticleEngineAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin implements ParticleEngineAccessor {
   @Shadow
   @Final
   private ParticleResources resourceManager;
   @Unique
   private final HashMap<Particle, UUID> particleMap = new HashMap<>();

   @Shadow
   @Nullable
   protected abstract <T extends ParticleOptions> Particle makeParticle(T var1, double var2, double var4, double var6, double var8, double var10, double var12);

   @Shadow
   public abstract void add(Particle var1);

   @Override
   public <T extends ParticleOptions> Particle figura$makeParticle(
      T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ
   ) {
      return this.makeParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
   }

   @Override
   public void figura$spawnParticle(Particle particle, UUID owner) {
      this.particleMap.put(particle, owner);
      this.add(particle);
   }

   @Override
   public void figura$clearParticles(UUID owner) {
      Iterator<Entry<Particle, UUID>> iterator = this.particleMap.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<Particle, UUID> entry = iterator.next();
         if (owner == null || entry.getValue().equals(owner)) {
            if (entry.getKey() != null) {
               entry.getKey().remove();
            }

            iterator.remove();
         }
      }
   }

   @Override
   public void figura$removeParticle(Particle particle) {
      this.particleMap.remove(particle);
   }

   @Override
   public ParticleResources figura$getParticleResources() {
      return this.resourceManager;
   }
}
