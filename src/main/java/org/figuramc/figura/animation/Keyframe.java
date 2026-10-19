package org.figuramc.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class Keyframe implements Comparable<Keyframe> {
   private final Avatar owner;
   private final Animation animation;
   private final float time;
   private final Interpolation interpolation;
   private final FiguraVec3 targetA;
   private final FiguraVec3 targetB;
   private final String[] aCode;
   private final String[] bCode;
   private final String chunkName;
   private final FiguraVec3 bezierLeft;
   private final FiguraVec3 bezierRight;
   private final FiguraVec3 bezierLeftTime;
   private final FiguraVec3 bezierRightTime;

   public Keyframe(
      Avatar owner,
      Animation animation,
      float time,
      Interpolation interpolation,
      Pair<FiguraVec3, String[]> a,
      Pair<FiguraVec3, String[]> b,
      FiguraVec3 bezierLeft,
      FiguraVec3 bezierRight,
      FiguraVec3 bezierLeftTime,
      FiguraVec3 bezierRightTime
   ) {
      this.owner = owner;
      this.animation = animation;
      this.time = time;
      this.interpolation = interpolation;
      this.targetA = (FiguraVec3)a.getFirst();
      this.targetB = (FiguraVec3)b.getFirst();
      this.aCode = (String[])a.getSecond();
      this.bCode = (String[])b.getSecond();
      this.chunkName = animation.getName() + " keyframe (" + time + "s)";
      this.bezierLeft = bezierLeft;
      this.bezierRight = bezierRight;
      this.bezierLeftTime = bezierLeftTime;
      this.bezierRightTime = bezierRightTime;
   }

   public FiguraVec3 getTargetA(float delta) {
      return this.targetA != null
         ? this.targetA.copy()
         : FiguraVec3.of(this.parseStringData(this.aCode[0], delta), this.parseStringData(this.aCode[1], delta), this.parseStringData(this.aCode[2], delta));
   }

   public FiguraVec3 getTargetB(float delta) {
      return this.targetB != null
         ? this.targetB.copy()
         : FiguraVec3.of(this.parseStringData(this.bCode[0], delta), this.parseStringData(this.bCode[1], delta), this.parseStringData(this.bCode[2], delta));
   }

   private float parseStringData(String data, float delta) {
      FiguraMod.pushProfiler(data);

      try {
         return FiguraMod.popReturnProfiler(Float.parseFloat(data));
      } catch (Exception ignored) {
         if (data == null) {
            return FiguraMod.popReturnProfiler(0.0F);
         }

         try {
            LuaValue val = this.owner.loadScript(this.chunkName, "return " + data);
            if (val == null) {
               return FiguraMod.popReturnProfiler(0.0F);
            } else {
               Varargs args = this.owner.run(val, this.owner.animation, delta, this.animation);
               if (args.isnumber(1)) {
                  return FiguraMod.popReturnProfiler(args.tofloat(1));
               } else {
                  throw new Exception();
               }
            }
         } catch (Exception ignored2) {
            try {
               LuaValue val = this.owner.loadScript(this.chunkName, data);
               if (val == null) {
                  return FiguraMod.popReturnProfiler(0.0F);
               } else {
                  Varargs args = this.owner.run(val, this.owner.animation, delta, this.animation);
                  if (args.isnumber(1)) {
                     return FiguraMod.popReturnProfiler(args.tofloat(1));
                  } else {
                     throw new LuaError("Failed to parse data from [" + this.chunkName + "], expected number, but got " + args.arg(1).typename());
                  }
               }
            } catch (Exception e) {
               if (this.owner.luaRuntime != null) {
                  this.owner.luaRuntime.error(e);
               }

               return FiguraMod.popReturnProfiler(0.0F);
            }
         }
      }
   }

   public float getTime() {
      return this.time;
   }

   public Interpolation getInterpolation() {
      return this.interpolation;
   }

   public FiguraVec3 getBezierLeft() {
      return this.bezierLeft.copy();
   }

   public FiguraVec3 getBezierRight() {
      return this.bezierRight.copy();
   }

   public FiguraVec3 getBezierLeftTime() {
      return this.bezierLeftTime.copy();
   }

   public FiguraVec3 getBezierRightTime() {
      return this.bezierRightTime.copy();
   }

   public int compareTo(Keyframe other) {
      return Float.compare(this.getTime(), other.getTime());
   }
}
