package org.figuramc.figura.ducks;

import java.util.UUID;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;

public interface ParticleEngineAccessor {
   <T extends ParticleOptions> Particle figura$makeParticle(T var1, double var2, double var4, double var6, double var8, double var10, double var12);

   void figura$spawnParticle(Particle var1, UUID var2);

   void figura$clearParticles(UUID var1);

   SpriteSet figura$getParticleSprite(Identifier var1);

   void figura$removeParticle(Particle var1);

   ParticleResources figura$getParticleResources();
}
