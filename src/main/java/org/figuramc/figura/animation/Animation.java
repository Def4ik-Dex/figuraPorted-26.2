package org.figuramc.figura.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "Animation", value = "animation")
public class Animation {
   private final Avatar owner;
   public final String modelName;
   @LuaWhitelist
   @LuaFieldDoc("animation.name")
   public final String name;
   protected final List<Entry<FiguraModelPart, List<Animation.AnimationChannel>>> animationParts = new ArrayList<>();
   private final Map<Float, String> codeFrames = new HashMap<>();
   private final TimeController controller = new TimeController();
   public Animation.PlayState playState = Animation.PlayState.STOPPED;
   private float time = 0.0F;
   private boolean inverted = false;
   private float lastTime = 0.0F;
   protected float frameTime = 0.0F;
   protected float length;
   protected float blend;
   protected float offset;
   protected float speed = 1.0F;
   protected float startDelay;
   protected float loopDelay;
   protected int override;
   protected int priority = 0;
   protected Animation.LoopMode loop;

   public Animation(
      Avatar owner,
      String modelName,
      String name,
      Animation.LoopMode loop,
      boolean override,
      float length,
      float offset,
      float blend,
      float startDelay,
      float loopDelay
   ) {
      this.owner = owner;
      this.modelName = modelName;
      this.name = name;
      this.loop = loop;
      this.override = override ? 7 : 0;
      this.length = length;
      this.offset = offset;
      this.blend = blend;
      this.startDelay = startDelay;
      this.loopDelay = loopDelay;
   }

   public void addAnimation(FiguraModelPart part, Animation.AnimationChannel anim) {
      Entry<FiguraModelPart, List<Animation.AnimationChannel>> entry = null;

      for (Entry<FiguraModelPart, List<Animation.AnimationChannel>> listEntry : this.animationParts) {
         if (listEntry.getKey() == part) {
            entry = listEntry;
            break;
         }
      }

      if (entry == null) {
         entry = new SimpleEntry<>(part, new ArrayList<>());
         this.animationParts.add(entry);
      }

      entry.getValue().add(anim);
      this.animationParts.sort(Entry.comparingByKey());
   }

   public void tick() {
      this.controller.tick();
      this.time = this.time + this.controller.getDiff() * this.speed;
      switch (this.loop) {
         case LOOP:
            if (!this.inverted && this.time > this.length + this.loopDelay) {
               this.time = this.time - (this.length + this.loopDelay - this.offset);
            } else if (this.inverted && this.time < this.offset - this.loopDelay) {
               this.time = this.time + (this.length + this.loopDelay - this.offset);
            }
            break;
         case ONCE:
            if (!this.inverted && this.time >= this.length || this.inverted && this.time <= 0.0F) {
               this.stop();
            }
            break;
         case HOLD:
            this.time = this.inverted ? Math.max(this.time, this.offset) : Math.min(this.time, this.length);
            if (!this.inverted && this.time >= this.length) {
               this.playState = Animation.PlayState.HOLDING;
            } else if (this.inverted && this.time <= 0.0F) {
               this.playState = Animation.PlayState.HOLDING;
            }
      }

      this.lastTime = this.frameTime;
      this.frameTime = Math.max(this.time, this.offset);
      if (this.inverted) {
         this.playCode(this.frameTime, this.lastTime);
      } else {
         this.playCode(this.lastTime, this.frameTime);
      }
   }

   public void playCode(float minTime, float maxTime) {
      if (this.owner.luaRuntime != null && !this.codeFrames.keySet().isEmpty()) {
         if (maxTime < minTime) {
            float len = this.length + 0.001F;
            this.playCode(Math.min(minTime, len), len);
            minTime = this.offset;
         }

         for (Float codeTime : this.codeFrames.keySet()) {
            if (codeTime >= minTime && codeTime < maxTime) {
               try {
                  LuaValue value = this.owner.loadScript("animations." + this.modelName + "." + this.name, this.codeFrames.get(codeTime));
                  this.owner.run(value, this.owner.animation, this);
               } catch (Exception e) {
                  this.owner.luaRuntime.error(e);
               }
            }
         }
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.is_playing")
   public boolean isPlaying() {
      return this.playState == Animation.PlayState.PLAYING;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.is_paused")
   public boolean isPaused() {
      return this.playState == Animation.PlayState.PAUSED;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.is_stopped")
   public boolean isStopped() {
      return this.playState == Animation.PlayState.STOPPED;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.is_holding")
   public boolean isHolding() {
      return this.playState == Animation.PlayState.HOLDING;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.play")
   public Animation play() {
      switch (this.playState) {
         case STOPPED:
            this.controller.init();
            this.time = this.inverted ? this.length + this.startDelay : this.offset - this.startDelay;
            this.lastTime = this.time;
            this.frameTime = 0.0F;
            break;
         case PAUSED:
            this.controller.resume();
            break;
         default:
            return this;
      }

      this.playState = Animation.PlayState.PLAYING;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.pause")
   public Animation pause() {
      this.controller.pause();
      this.playState = Animation.PlayState.PAUSED;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.stop")
   public Animation stop() {
      this.controller.reset();
      this.playState = Animation.PlayState.STOPPED;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.restart")
   public Animation restart() {
      this.stop();
      this.play();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "bool")},
      aliases = "playing",
      value = "animation.set_playing"
   )
   public Animation setPlaying(boolean bool) {
      return bool ? this.play() : this.stop();
   }

   @LuaWhitelist
   public Animation playing(boolean bool) {
      return this.setPlaying(bool);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_time")
   public float getTime() {
      return this.time;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "time"), aliases = "time", value = "animation.set_time")
   public Animation setTime(float time) {
      this.time = time;
      this.lastTime = time;
      this.frameTime = Math.max(time, this.offset);
      return this;
   }

   @LuaWhitelist
   public Animation time(float time) {
      return this.setTime(time);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_play_state")
   public String getPlayState() {
      return this.playState.name();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = {Float.class, String.class}, argumentNames = {"time", "code"}),
      aliases = "code",
      value = "animation.new_code"
   )
   public Animation newCode(float time, @LuaNotNil String data) {
      this.codeFrames.put(Math.max(time, 0.0F), data);
      return this;
   }

   @LuaWhitelist
   public Animation code(float time, @LuaNotNil String data) {
      return this.newCode(time, data);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_blend")
   public float getBlend() {
      return this.blend;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "blend"), aliases = "blend", value = "animation.set_blend")
   public Animation setBlend(float blend) {
      this.blend = blend;
      return this;
   }

   @LuaWhitelist
   public Animation blend(float blend) {
      return this.setBlend(blend);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_offset")
   public float getOffset() {
      return this.offset;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "offset"), aliases = "offset", value = "animation.set_offset")
   public Animation setOffset(float offset) {
      this.offset = offset;
      return this;
   }

   @LuaWhitelist
   public Animation offset(float offset) {
      return this.setOffset(offset);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_start_delay")
   public float getStartDelay() {
      return this.startDelay;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "delay"),
      aliases = "startDelay",
      value = "animation.set_start_delay"
   )
   public Animation setStartDelay(float delay) {
      this.startDelay = delay;
      return this;
   }

   @LuaWhitelist
   public Animation startDelay(float delay) {
      return this.setStartDelay(delay);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_loop_delay")
   public float getLoopDelay() {
      return this.loopDelay;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "delay"),
      aliases = "loopDelay",
      value = "animation.set_loop_delay"
   )
   public Animation setLoopDelay(float delay) {
      this.loopDelay = delay;
      return this;
   }

   @LuaWhitelist
   public Animation loopDelay(float delay) {
      return this.setLoopDelay(delay);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_length")
   public float getLength() {
      return this.length;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "length"), aliases = "length", value = "animation.set_length")
   public Animation setLength(float length) {
      this.length = length;
      return this;
   }

   @LuaWhitelist
   public Animation length(float length) {
      return this.setLength(length);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "override"),
      aliases = "override",
      value = "animation.set_override"
   )
   public Animation setOverride(boolean override) {
      this.override = override ? 7 : 0;
      return this;
   }

   @LuaWhitelist
   public Animation override(boolean override) {
      return this.setOverride(override);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_override_rot")
   public boolean getOverrideRot() {
      return (this.override & 1) == 1;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_override_pos")
   public boolean getOverridePos() {
      return (this.override & 2) == 2;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_override_scale")
   public boolean getOverrideScale() {
      return (this.override & 4) == 4;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "override"),
      aliases = "overrideRot",
      value = "animation.set_override_rot"
   )
   public Animation setOverrideRot(boolean override) {
      this.override = override ? this.override | 1 : this.override & 6;
      return this;
   }

   @LuaWhitelist
   public Animation overrideRot(boolean override) {
      return this.setOverrideRot(override);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "override"),
      aliases = "overridePos",
      value = "animation.set_override_pos"
   )
   public Animation setOverridePos(boolean override) {
      this.override = override ? this.override | 2 : this.override & 5;
      return this;
   }

   @LuaWhitelist
   public Animation overridePos(boolean override) {
      return this.setOverridePos(override);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "override"),
      aliases = "overrideScale",
      value = "animation.set_override_scale"
   )
   public Animation setOverrideScale(boolean override) {
      this.override = override ? this.override | 4 : this.override & 3;
      return this;
   }

   @LuaWhitelist
   public Animation overrideScale(boolean override) {
      this.setOverrideScale(override);
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_loop")
   public String getLoop() {
      return this.loop.name();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "loop"), aliases = "loop", value = "animation.set_loop")
   public Animation setLoop(@LuaNotNil String loop) {
      try {
         this.loop = Animation.LoopMode.valueOf(loop.toUpperCase(Locale.US));
         return this;
      } catch (Exception ignored) {
         throw new LuaError("Illegal LoopMode: \"" + loop + "\".");
      }
   }

   @LuaWhitelist
   public Animation loop(@LuaNotNil String loop) {
      return this.setLoop(loop);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_priority")
   public int getPriority() {
      return this.priority;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "priority"),
      aliases = "priority",
      value = "animation.set_priority"
   )
   public Animation setPriority(int priority) {
      this.priority = priority;
      return this;
   }

   @LuaWhitelist
   public Animation priority(int priority) {
      return this.setPriority(priority);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_speed")
   public float getSpeed() {
      return this.speed;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "speed"), aliases = "speed", value = "animation.set_speed")
   public Animation setSpeed(Float speed) {
      if (speed == null) {
         speed = 1.0F;
      }

      this.speed = speed;
      this.inverted = speed < 0.0F;
      if (this.inverted && this.time >= this.length && this.playState == Animation.PlayState.HOLDING) {
         this.playState = Animation.PlayState.PLAYING;
      } else if (!this.inverted && this.time <= 0.0F && this.playState == Animation.PlayState.HOLDING) {
         this.playState = Animation.PlayState.PLAYING;
      }

      return this;
   }

   @LuaWhitelist
   public Animation speed(Float speed) {
      return this.setSpeed(speed);
   }

   @LuaWhitelist
   @LuaMethodDoc("animation.get_name")
   public String getName() {
      return this.name;
   }

   @LuaWhitelist
   public Object __index(String arg) {
      if (arg == null) {
         return null;
      } else {
         return arg.equals("name") ? this.name : null;
      }
   }

   @Override
   public String toString() {
      return this.name + " (Animation)";
   }

   public record AnimationChannel(TransformType type, Keyframe... keyframes) {
   }

   public enum LoopMode {
      LOOP,
      ONCE,
      HOLD;
   }

   public enum PlayState {
      STOPPED,
      PAUSED,
      PLAYING,
      HOLDING;
   }
}
