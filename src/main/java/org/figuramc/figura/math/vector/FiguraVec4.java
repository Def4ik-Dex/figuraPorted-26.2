package org.figuramc.figura.math.vector;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.utils.MathUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "Vector4", value = "vector4")
public class FiguraVec4 extends FiguraVector<FiguraVec4, FiguraMat4> {
   @LuaWhitelist
   @LuaFieldDoc("vector_n.x")
   public double x;
   @LuaWhitelist
   @LuaFieldDoc("vector_n.y")
   public double y;
   @LuaWhitelist
   @LuaFieldDoc("vector_n.z")
   public double z;
   @LuaWhitelist
   @LuaFieldDoc("vector_n.w")
   public double w;

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec4.class), value = "vector_n.reset")
   public FiguraVec4 reset() {
      this.x = this.y = this.z = this.w = 0.0;
      return this;
   }

   public static FiguraVec4 of() {
      return new FiguraVec4();
   }

   public static FiguraVec4 of(double x, double y, double z, double w) {
      return of().set(x, y, z, w);
   }

   public FiguraVec4 set(FiguraVec4 other) {
      return this.set(other.x, other.y, other.z, other.w);
   }

   public FiguraVec4 set(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z", "w"})
      },
      value = "vector_n.set"
   )
   public FiguraVec4 set(@LuaNotNil Object x, double y, double z, double w) {
      if (x instanceof FiguraVec4 vec) {
         return this.set(vec);
      } else if (x instanceof Number n) {
         return this.set(n.doubleValue(), y, z, w);
      } else {
         throw new LuaError("Illegal type to set(): " + x.getClass().getSimpleName());
      }
   }

   public FiguraVec4 add(FiguraVec4 other) {
      return this.add(other.x, other.y, other.z, other.w);
   }

   public FiguraVec4 add(double x, double y, double z, double w) {
      this.x += x;
      this.y += y;
      this.z += z;
      this.w += w;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z", "w"})
      },
      value = "vector_n.add"
   )
   public FiguraVec4 add(@LuaNotNil Object x, double y, double z, double w) {
      if (x instanceof FiguraVec4 vec) {
         return this.add(vec);
      } else if (x instanceof Number n) {
         return this.add(n.doubleValue(), y, z, w);
      } else {
         throw new LuaError("Illegal type to add(): " + x.getClass().getSimpleName());
      }
   }

   public FiguraVec4 subtract(FiguraVec4 other) {
      return this.subtract(other.x, other.y, other.z, other.w);
   }

   public FiguraVec4 subtract(double x, double y, double z, double w) {
      this.x -= x;
      this.y -= y;
      this.z -= z;
      this.w -= w;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z", "w"})
      },
      value = "vector_n.sub"
   )
   public FiguraVec4 sub(@LuaNotNil Object x, double y, double z, double w) {
      if (x instanceof FiguraVec4 vec) {
         return this.subtract(vec);
      } else if (x instanceof Number n) {
         return this.subtract(n.doubleValue(), y, z, w);
      } else {
         throw new LuaError("Illegal type to sub(): " + x.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "factor", returnType = FiguraVec4.class),
      value = "vector_n.offset"
   )
   public FiguraVec4 offset(double factor) {
      this.x += factor;
      this.y += factor;
      this.z += factor;
      this.w += factor;
      return this;
   }

   public FiguraVec4 multiply(FiguraVec4 other) {
      return this.multiply(other.x, other.y, other.z, other.w);
   }

   public FiguraVec4 multiply(double x, double y, double z, double w) {
      this.x *= x;
      this.y *= y;
      this.z *= z;
      this.w *= w;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z", "w"})
      },
      value = "vector_n.mul"
   )
   public FiguraVec4 mul(@LuaNotNil Object x, double y, double z, double w) {
      if (x instanceof FiguraVec4 vec) {
         return this.multiply(vec);
      } else if (x instanceof Number n) {
         return this.multiply(n.doubleValue(), y, z, w);
      } else {
         throw new LuaError("Illegal type to mul(): " + x.getClass().getSimpleName());
      }
   }

   public FiguraVec4 divide(FiguraVec4 other) {
      return this.divide(other.x, other.y, other.z, other.w);
   }

   public FiguraVec4 divide(double x, double y, double z, double w) {
      this.x /= x;
      this.y /= y;
      this.z /= z;
      this.w /= w;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z", "w"})
      },
      value = "vector_n.div"
   )
   public FiguraVec4 div(@LuaNotNil Object x, double y, double z, double w) {
      if (x instanceof FiguraVec4 vec) {
         return this.divide(vec);
      } else if (x instanceof Number n) {
         return this.divide(n.doubleValue(), y, z, w);
      } else {
         throw new LuaError("Illegal type to div(): " + x.getClass().getSimpleName());
      }
   }

   public FiguraVec4 reduce(FiguraVec4 other) {
      return this.reduce(other.x, other.y, other.z, other.w);
   }

   public FiguraVec4 reduce(double x, double y, double z, double w) {
      this.x = (this.x % x + x) % x;
      this.y = (this.y % y + y) % y;
      this.z = (this.z % z + z) % z;
      this.w = (this.w % w + w) % w;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z", "w"})
      },
      value = "vector_n.reduce"
   )
   public FiguraVec4 reduce(@LuaNotNil Object x, double y, double z, double w) {
      if (x instanceof FiguraVec4 vec) {
         return this.reduce(vec);
      } else if (x instanceof Number n) {
         return this.reduce(n.doubleValue(), y, z, w);
      } else {
         throw new LuaError("Illegal type to reduce(): " + x.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "factor", returnType = FiguraVec4.class),
      value = "vector_n.scale"
   )
   public FiguraVec4 scale(double factor) {
      this.x *= factor;
      this.y *= factor;
      this.z *= factor;
      this.w *= factor;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.unpack")
   @Override
   public double[] unpack() {
      return new double[]{this.x, this.y, this.z, this.w};
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat4.class, argumentNames = "mat", returnType = FiguraVec4.class),
      value = "vector_n.transform"
   )
   public FiguraVec4 transform(@LuaNotNil FiguraMat4 mat) {
      return this.set(
         mat.v11 * this.x + mat.v12 * this.y + mat.v13 * this.z + mat.v14 * this.w,
         mat.v21 * this.x + mat.v22 * this.y + mat.v23 * this.z + mat.v24 * this.w,
         mat.v31 * this.x + mat.v32 * this.y + mat.v33 * this.z + mat.v34 * this.w,
         mat.v41 * this.x + mat.v42 * this.y + mat.v43 * this.z + mat.v44 * this.w
      );
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.length_squared")
   @Override
   public double lengthSquared() {
      return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec4.class), value = "vector_n.copy")
   public FiguraVec4 copy() {
      return of(this.x, this.y, this.z, this.w);
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "vec"), value = "vector_n.dot")
   public double dot(@LuaNotNil FiguraVec4 other) {
      return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec4.class), value = "vector_n.normalize")
   public FiguraVec4 normalize() {
      return (FiguraVec4)super.normalize();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec4.class), value = "vector_n.normalized")
   public FiguraVec4 normalized() {
      return (FiguraVec4)super.normalized();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"minLength", "maxLength"}, returnType = FiguraVec4.class),
      value = "vector_n.clamp_length"
   )
   public FiguraVec4 clampLength(Double min, Double max) {
      return (FiguraVec4)super.clampLength(min, max);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"minLength", "maxLength"}, returnType = FiguraVec4.class),
      value = "vector_n.clamped"
   )
   public FiguraVec4 clamped(Double min, Double max) {
      return (FiguraVec4)super.clamped(min, max);
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.length")
   @Override
   public double length() {
      return super.length();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec4.class), value = "vector_n.to_rad")
   public FiguraVec4 toRad() {
      return (FiguraVec4)super.toRad();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec4.class), value = "vector_n.to_deg")
   public FiguraVec4 toDeg() {
      return (FiguraVec4)super.toDeg();
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.floor")
   public FiguraVec4 floor() {
      return of(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z), Math.floor(this.w));
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.ceil")
   public FiguraVec4 ceil() {
      return of(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.z), Math.ceil(this.w));
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "func"), value = "vector_n.apply_func")
   public FiguraVec4 applyFunc(@LuaNotNil LuaFunction function) {
      this.x = function.call(LuaValue.valueOf(this.x), LuaValue.valueOf(1)).todouble();
      this.y = function.call(LuaValue.valueOf(this.y), LuaValue.valueOf(2)).todouble();
      this.z = function.call(LuaValue.valueOf(this.z), LuaValue.valueOf(3)).todouble();
      this.w = function.call(LuaValue.valueOf(this.w), LuaValue.valueOf(4)).todouble();
      return this;
   }

   @Override
   public int size() {
      return 4;
   }

   @Override
   public double x() {
      return this.x;
   }

   @Override
   public double y() {
      return this.y;
   }

   @Override
   public double z() {
      return this.z;
   }

   @Override
   public double w() {
      return this.w;
   }

   @Override
   public double index(int i) {
      return switch (i) {
         case 0 -> this.x;
         case 1 -> this.y;
         case 2 -> this.z;
         case 3 -> this.w;
         default -> throw new IndexOutOfBoundsException(i);
      };
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof FiguraVec4 vec && this.x == vec.x && this.y == vec.y && this.z == vec.z && this.w == vec.w;
   }

   @LuaWhitelist
   @Override
   public String toString() {
      return getString(this.x, this.y, this.z, this.w);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, Double.class, FiguraVec4.class})
      }
   )
   public static FiguraVec4 __add(@LuaNotNil Object a, @LuaNotNil Object b) {
      if (a instanceof FiguraVec4 vec) {
         if (b instanceof FiguraVec4 vec2) {
            return vec.plus(vec2);
         }

         if (b instanceof Number d) {
            return vec.offseted(d.doubleValue());
         }
      } else if (a instanceof Number d && b instanceof FiguraVec4 vec) {
         return vec.offseted(d.doubleValue());
      }

      throw new LuaError("Invalid types to __add: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, Double.class, FiguraVec4.class})
      }
   )
   public static FiguraVec4 __sub(@LuaNotNil Object a, @LuaNotNil Object b) {
      if (a instanceof FiguraVec4 vec) {
         if (b instanceof FiguraVec4 vec2) {
            return vec.minus(vec2);
         }

         if (b instanceof Number d) {
            return vec.offseted(-d.doubleValue());
         }
      } else if (a instanceof Number d && b instanceof FiguraVec4 vec) {
         return vec.scaled(-1.0).offset(d.doubleValue());
      }

      throw new LuaError("Invalid types to __sub: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, FiguraMat4.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, Double.class, FiguraVec4.class})
      }
   )
   public static FiguraVec4 __mul(@LuaNotNil Object a, @LuaNotNil Object b) {
      if (a instanceof FiguraVec4 vec) {
         if (b instanceof FiguraVec4 vec2) {
            return vec.times(vec2);
         }

         if (b instanceof Number d) {
            return vec.scaled(d.doubleValue());
         }

         if (b instanceof FiguraMat4 mat) {
            return vec.copy().transform(mat);
         }
      } else if (a instanceof Number d && b instanceof FiguraVec4 vec) {
         return vec.scaled(d.doubleValue());
      }

      throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, Double.class})
      }
   )
   public FiguraVec4 __div(@LuaNotNil Object rhs) {
      if (rhs instanceof Number n) {
         double d = n.doubleValue();
         if (d == 0.0) {
            throw new LuaError("Attempt to divide vector by 0");
         } else {
            return this.scaled(1.0 / d);
         }
      } else if (rhs instanceof FiguraVec4 vec) {
         return this.dividedBy(vec);
      } else {
         throw new LuaError("Invalid types to __div: " + this.getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class, Double.class})
      }
   )
   public FiguraVec4 __mod(@LuaNotNil Object rhs) {
      if (rhs instanceof Number n) {
         double d = n.doubleValue();
         if (d == 0.0) {
            throw new LuaError("Attempt to reduce vector by 0");
         }

         FiguraVec4 modulus = of(d, d, d, d);
         return this.mod(modulus);
      } else if (rhs instanceof FiguraVec4 vec) {
         return this.mod(vec);
      } else {
         throw new LuaError("Invalid types to __mod: " + this.getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Boolean.class, FiguraVec4.class, FiguraVec4.class}))
   public boolean __eq(FiguraVec4 other) {
      return this.equals(other);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec4.class, FiguraVec4.class}))
   public FiguraVec4 __unm() {
      return this.scaled(-1.0);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Integer.class, FiguraVec4.class}))
   public int __len() {
      return this.size();
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Boolean.class, FiguraVec4.class, FiguraVec4.class}))
   public boolean __lt(@LuaNotNil FiguraVec4 r) {
      return this.x < r.x && this.y < r.y && this.z < r.z && this.w < r.w;
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Boolean.class, FiguraVec4.class, FiguraVec4.class}))
   public boolean __le(@LuaNotNil FiguraVec4 r) {
      return this.x <= r.x && this.y <= r.y && this.z <= r.z && this.w <= r.w;
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {String.class, FiguraVec4.class}))
   public String __tostring() {
      return this.toString();
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {Double.class, FiguraVec4.class, Integer.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {Double.class, FiguraVec4.class, String.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVector.class, FiguraVec4.class, String.class}, comment = "vector_n.comments.swizzle")
      }
   )
   public Object __index(Object arg) {
      if (arg == null) {
         return null;
      }

      String str = arg.toString();
      int len = str.length();
      if (len == 1) {
         return switch (str.charAt(0)) {
            case '1', 'r', 'x' -> this.x;
            case '2', 'g', 'y' -> this.y;
            case '3', 'b', 'z' -> this.z;
            case '4', 'a', 'w' -> this.w;
            case '_' -> 0;
            default -> null;
         };
      } else {
         if (len > 4) {
            return null;
         }

         double[] vals = new double[len];
         boolean fail = false;

         for (int i = 0; i < len; i++) {
            vals[i] = switch (str.charAt(i)) {
               case '1', 'r', 'x' -> this.x;
               case '2', 'g', 'y' -> this.y;
               case '3', 'b', 'z' -> this.z;
               case '4', 'a', 'w' -> this.w;
               case '_' -> 0.0;
               default -> {
                  fail = true;
                  yield 0.0;
               }
            };
         }

         return fail ? null : MathUtils.sizedVector(vals);
      }
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {void.class, FiguraVec4.class, Integer.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {void.class, FiguraVec4.class, String.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {void.class, FiguraVec4.class, String.class, FiguraVector.class})
      }
   )
   public void __newindex(@LuaNotNil String key, Object value) {
      int len = key.length();
      if (len == 1) {
         if (value instanceof Number n) {
            double d = n.doubleValue();
            switch (key) {
               case "1":
               case "x":
               case "r":
                  this.x = d;
                  break;
               case "2":
               case "y":
               case "g":
                  this.y = d;
                  break;
               case "3":
               case "z":
               case "b":
                  this.z = d;
                  break;
               case "4":
               case "w":
               case "a":
                  this.w = d;
               case "_":
                  break;
               default:
                  throw new LuaError("Invalid key to vector __newindex: " + key);
            }
         } else {
            throw new LuaError("Invalid call to __newindex - value assigned to key " + key + " must be number.");
         }
      } else if (value instanceof FiguraVector<?, ?> vecVal && len == vecVal.size()) {
         double[] vals = new double[]{vecVal.x(), vecVal.y(), vecVal.z(), vecVal.w()};

         for (int i = 0; i < len; i++) {
            switch (key.charAt(i)) {
               case '1':
               case 'r':
               case 'x':
                  this.x = vals[i];
                  break;
               case '2':
               case 'g':
               case 'y':
                  this.y = vals[i];
                  break;
               case '3':
               case 'b':
               case 'z':
                  this.z = vals[i];
                  break;
               case '4':
               case 'a':
               case 'w':
                  this.w = vals[i];
               case '_':
                  break;
               default:
                  throw new LuaError("Invalid key to __newindex: invalid swizzle character: " + key.charAt(i));
            }
         }
      } else {
         throw new LuaError("Invalid call to __newindex - vector swizzles must be the same size.");
      }
   }
}
