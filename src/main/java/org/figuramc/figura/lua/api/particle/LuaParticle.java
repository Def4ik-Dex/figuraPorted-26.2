package org.figuramc.figura.lua.api.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.WakeParticle;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.SingleQuadParticleAccessor;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.mixin.particle.ParticleAccessor;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(name = "Particle", value = "particle")
public class LuaParticle {
   private final String name;
   private final Avatar owner;
   private final Particle particle;
   private float power;

   public LuaParticle(String name, Particle particle, Avatar owner) {
      this.name = name;
      this.particle = particle;
      this.owner = owner;
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.spawn")
   public LuaParticle spawn() {
      if (!Minecraft.getInstance().isPaused()) {
         if (this.owner.particlesRemaining.use()) {
            ParticleAPI.getParticleEngine().figura$spawnParticle(this.particle, this.owner.owner);
            this.owner.noPermissions.remove(Permissions.PARTICLES);
         } else {
            this.owner.noPermissions.add(Permissions.PARTICLES);
         }
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.remove")
   public LuaParticle remove() {
      this.particle.remove();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.is_alive")
   public boolean isAlive() {
      return this.particle.isAlive();
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.get_pos")
   public FiguraVec3 getPos() {
      ParticleAccessor p = (ParticleAccessor)this.particle;
      return FiguraVec3.of(p.getX(), p.getY(), p.getZ());
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "pos"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "pos",
      value = "particle.set_pos"
   )
   public LuaParticle setPos(Object x, Double y, Double z) {
      FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
      this.particle.setPos(vec.x, vec.y, vec.z);
      ParticleAccessor p = (ParticleAccessor)this.particle;
      p.setXo(vec.x);
      p.setYo(vec.y);
      p.setZo(vec.z);
      return this;
   }

   @LuaWhitelist
   public LuaParticle pos(Object x, Double y, Double z) {
      return this.setPos(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.get_velocity")
   public FiguraVec3 getVelocity() {
      ParticleAccessor p = (ParticleAccessor)this.particle;
      return FiguraVec3.of(p.getXd(), p.getYd(), p.getZd());
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "velocity"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "velocity",
      value = "particle.set_velocity"
   )
   public LuaParticle setVelocity(Object x, Double y, Double z) {
      FiguraVec3 vec = LuaUtils.parseVec3("setVelocity", x, y, z);
      this.particle.setParticleSpeed(vec.x, vec.y, vec.z);
      return this;
   }

   @LuaWhitelist
   public LuaParticle velocity(Object x, Double y, Double z) {
      return this.setVelocity(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.get_color")
   public FiguraVec4 getColor() {
      if (!(this.particle instanceof SingleQuadParticle)) {
         return FiguraVec4.of(1.0, 1.0, 1.0, 1.0);
      }

      SingleQuadParticleAccessor p = (SingleQuadParticleAccessor)this.particle;
      return FiguraVec4.of(p.figura$getRCol(), p.figura$getGCol(), p.figura$getBCol(), p.figura$getAlpha());
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "rgb"),
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "rgba"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b", "a"})
      },
      aliases = "color",
      value = "particle.set_color"
   )
   public LuaParticle setColor(Object r, Double g, Double b, Double a) {
      FiguraVec4 vec = LuaUtils.parseVec4("setColor", r, g, b, a, 1.0, 1.0, 1.0, 1.0);
      if (this.particle instanceof SingleQuadParticle quadParticle) {
         quadParticle.setColor((float)vec.x, (float)vec.y, (float)vec.z);
         ((SingleQuadParticleAccessor)this.particle).figura$setParticleAlpha((float)vec.w);
      }

      return this;
   }

   @LuaWhitelist
   public LuaParticle color(Object r, Double g, Double b, Double a) {
      return this.setColor(r, g, b, a);
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.get_lifetime")
   public int getLifetime() {
      return this.particle.getLifetime();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "lifetime"),
      aliases = "lifetime",
      value = "particle.set_lifetime"
   )
   public LuaParticle setLifetime(int age) {
      this.particle.setLifetime(Math.max(this.particle instanceof WakeParticle ? Math.min(age, 60) : age, 0));
      return this;
   }

   @LuaWhitelist
   public LuaParticle lifetime(int age) {
      return this.setLifetime(age);
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.get_power")
   public float getPower() {
      return this.power;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "power"), aliases = "power", value = "particle.set_power")
   public LuaParticle setPower(float power) {
      this.particle.setPower(power);
      this.power = power;
      return this;
   }

   @LuaWhitelist
   public LuaParticle power(float power) {
      return this.setPower(power);
   }

   @LuaWhitelist
   @LuaMethodDoc(aliases = "getSize", value = "particle.get_scale")
   public float getScale() {
      return ((ParticleAccessor)this.particle).getBbWidth() / 0.2F;
   }

   @LuaWhitelist
   public float getSize() {
      return this.getScale();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "scale"),
      aliases = {"scale", "setSize", "size"},
      value = "particle.set_scale"
   )
   public LuaParticle setScale(float scale) {
      if (this.particle instanceof SingleQuadParticle quadParticle) {
         ((SingleQuadParticleAccessor)quadParticle).figura$fixQuadSize();
      }

      this.particle.scale(scale);
      return this;
   }

   @LuaWhitelist
   public LuaParticle scale(float scale) {
      return this.setScale(scale);
   }

   @LuaWhitelist
   public LuaParticle setSize(float scale) {
      return this.setScale(scale);
   }

   @LuaWhitelist
   public LuaParticle size(float scale) {
      return this.setScale(scale);
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.get_gravity")
   public float getGravity() {
      return ((ParticleAccessor)this.particle).getGravity();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "gravity"), aliases = "gravity", value = "particle.set_gravity")
   public LuaParticle setGravity(float gravity) {
      ((ParticleAccessor)this.particle).setGravity(gravity);
      return this;
   }

   @LuaWhitelist
   public LuaParticle gravity(float gravity) {
      return this.setGravity(gravity);
   }

   @LuaWhitelist
   @LuaMethodDoc("particle.has_physics")
   public boolean hasPhysics() {
      return ((ParticleAccessor)this.particle).getHasPhysics();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "physics"), aliases = "physics", value = "particle.set_physics")
   public LuaParticle setPhysics(boolean physics) {
      ((ParticleAccessor)this.particle).setHasPhysics(physics);
      return this;
   }

   @LuaWhitelist
   public LuaParticle physics(boolean physics) {
      return this.setPhysics(physics);
   }

   @Override
   public String toString() {
      return this.name + " (Particle)";
   }
}
