package org.figuramc.figura.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.figuramc.figura.mixin.MinecraftAccesor;

public class TimeController {
   private long lastTime;
   private long time;
   private long pauseTime;

   public void init() {
      this.pauseTime = 0L;
      this.lastTime = this.time = Util.getMillis();
   }

   public void tick() {
      this.lastTime = this.time;
      this.time = Util.getMillis();
   }

   public void reset() {
      this.lastTime = this.time = this.pauseTime = 0L;
   }

   public void pause() {
      this.lastTime = this.time;
      this.pauseTime = Util.getMillis();
   }

   public void resume() {
      long diff = Util.getMillis() - this.pauseTime;
      this.lastTime += diff;
      this.time += diff;
   }

   public float getDiff() {
      return Minecraft.getInstance().isPaused()
         ? 0.0F
         : (float)(this.time - this.lastTime) / ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetTickTargetMillis(50.0F) / 20.0F;
   }
}
