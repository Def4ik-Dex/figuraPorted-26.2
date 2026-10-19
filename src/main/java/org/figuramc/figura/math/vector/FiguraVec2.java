package org.figuramc.figura.math.vector;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat2;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.MathUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "Vector2", value = "vector2")
public class FiguraVec2 extends FiguraVector<FiguraVec2, FiguraMat2> {
   @LuaWhitelist
   @LuaFieldDoc("vector_n.x")
   public double x;
   @LuaWhitelist
   @LuaFieldDoc("vector_n.y")
   public double y;

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec2.class), value = "vector_n.reset")
   public FiguraVec2 reset() {
      this.x = this.y = 0.0;
      return this;
   }

   public static FiguraVec2 of() {
      return new FiguraVec2();
   }

   public static FiguraVec2 of(double x, double y) {
      return of().set(x, y);
   }

   public FiguraVec2 set(FiguraVec2 other) {
      return this.set(other.x, other.y);
   }

   public FiguraVec2 set(double x, double y) {
      this.x = x;
      this.y = y;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "vector_n.set"
   )
   public FiguraVec2 set(Object x, double y) {
      return this.set(LuaUtils.parseVec2("set", x, y));
   }

   public FiguraVec2 add(FiguraVec2 other) {
      return this.add(other.x, other.y);
   }

   public FiguraVec2 add(double x, double y) {
      this.x += x;
      this.y += y;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "vector_n.add"
   )
   public FiguraVec2 add(Object x, double y) {
      return this.add(LuaUtils.parseVec2("add", x, y));
   }

   public FiguraVec2 subtract(FiguraVec2 other) {
      return this.subtract(other.x, other.y);
   }

   public FiguraVec2 subtract(double x, double y) {
      this.x -= x;
      this.y -= y;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "vector_n.sub"
   )
   public FiguraVec2 sub(Object x, double y) {
      return this.subtract(LuaUtils.parseVec2("sub", x, y));
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "factor", returnType = FiguraVec2.class),
      value = "vector_n.offset"
   )
   public FiguraVec2 offset(double factor) {
      this.x += factor;
      this.y += factor;
      return this;
   }

   public FiguraVec2 multiply(FiguraVec2 other) {
      return this.multiply(other.x, other.y);
   }

   public FiguraVec2 multiply(double x, double y) {
      this.x *= x;
      this.y *= y;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "vector_n.mul"
   )
   public FiguraVec2 mul(Object x, double y) {
      return this.multiply(LuaUtils.parseVec2("mul", x, y));
   }

   public FiguraVec2 divide(FiguraVec2 other) {
      return this.divide(other.x, other.y);
   }

   public FiguraVec2 divide(double x, double y) {
      this.x /= x;
      this.y /= y;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "vector_n.div"
   )
   public FiguraVec2 div(Object x, double y) {
      return this.divide(LuaUtils.parseVec2("div", x, y));
   }

   public FiguraVec2 reduce(FiguraVec2 other) {
      return this.reduce(other.x, other.y);
   }

   public FiguraVec2 reduce(double x, double y) {
      this.x = (this.x % x + x) % x;
      this.y = (this.y % y + y) % y;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "vector_n.reduce"
   )
   public FiguraVec2 reduce(Object x, double y) {
      return this.reduce(LuaUtils.parseVec2("reduce", x, y));
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "factor", returnType = FiguraVec2.class),
      value = "vector_n.scale"
   )
   public FiguraVec2 scale(double factor) {
      this.x *= factor;
      this.y *= factor;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.unpack")
   @Override
   public double[] unpack() {
      return new double[]{this.x, this.y};
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat2.class, argumentNames = "mat", returnType = FiguraVec2.class),
      value = "vector_n.transform"
   )
   public FiguraVec2 transform(FiguraMat2 mat) {
      return mat != null ? this.set(mat.v11 * this.x + mat.v12 * this.y, mat.v21 * this.x + mat.v22 * this.y) : this;
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.length_squared")
   @Override
   public double lengthSquared() {
      return this.x * this.x + this.y * this.y;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec2.class), value = "vector_n.copy")
   public FiguraVec2 copy() {
      return of(this.x, this.y);
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"), value = "vector_n.dot")
   public double dot(@LuaNotNil FiguraVec2 other) {
      return this.x * other.x + this.y * other.y;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec2.class), value = "vector_n.normalize")
   public FiguraVec2 normalize() {
      return (FiguraVec2)super.normalize();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec2.class), value = "vector_n.normalized")
   public FiguraVec2 normalized() {
      return (FiguraVec2)super.normalized();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"minLength", "maxLength"}, returnType = FiguraVec2.class),
      value = "vector_n.clamp_length"
   )
   public FiguraVec2 clampLength(Double min, Double max) {
      return (FiguraVec2)super.clampLength(min, max);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"minLength", "maxLength"}, returnType = FiguraVec2.class),
      value = "vector_n.clamped"
   )
   public FiguraVec2 clamped(Double min, Double max) {
      return (FiguraVec2)super.clamped(min, max);
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.length")
   @Override
   public double length() {
      return super.length();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec2.class), value = "vector_n.to_rad")
   public FiguraVec2 toRad() {
      return (FiguraVec2)super.toRad();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraVec2.class), value = "vector_n.to_deg")
   public FiguraVec2 toDeg() {
      return (FiguraVec2)super.toDeg();
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.floor")
   public FiguraVec2 floor() {
      return of(Math.floor(this.x), Math.floor(this.y));
   }

   @LuaWhitelist
   @LuaMethodDoc("vector_n.ceil")
   public FiguraVec2 ceil() {
      return of(Math.ceil(this.x), Math.ceil(this.y));
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "func"), value = "vector_n.apply_func")
   public FiguraVec2 applyFunc(@LuaNotNil LuaFunction function) {
      this.x = function.call(LuaValue.valueOf(this.x), LuaValue.valueOf(1)).todouble();
      this.y = function.call(LuaValue.valueOf(this.y), LuaValue.valueOf(2)).todouble();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "value")}, value = "vector_n.augmented")
   public FiguraVec3 augmented(Double d) {
      return FiguraVec3.of(this.x, this.y, d == null ? 1.0 : d);
   }

   @Override
   public int size() {
      return 2;
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
   public double index(int i) {
      return switch (i) {
         case 0 -> this.x;
         case 1 -> this.y;
         default -> throw new IndexOutOfBoundsException(i);
      };
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof FiguraVec2 vec && this.x == vec.x && this.y == vec.y;
   }

   @LuaWhitelist
   @Override
   public String toString() {
      return getString(this.x, this.y);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, Double.class, FiguraVec2.class})
      }
   )
   public static FiguraVec2 __add(@LuaNotNil Object a, @LuaNotNil Object b) {
      if (a instanceof FiguraVec2 vec) {
         if (b instanceof FiguraVec2 vec2) {
            return vec.plus(vec2);
         }

         if (b instanceof Number d) {
            return vec.offseted(d.doubleValue());
         }
      } else if (a instanceof Number d && b instanceof FiguraVec2 vec) {
         return vec.offseted(d.doubleValue());
      }

      throw new LuaError("Invalid types to __add: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, Double.class, FiguraVec2.class})
      }
   )
   public static FiguraVec2 __sub(@LuaNotNil Object a, @LuaNotNil Object b) {
      if (a instanceof FiguraVec2 vec) {
         if (b instanceof FiguraVec2 vec2) {
            return vec.minus(vec2);
         }

         if (b instanceof Number d) {
            return vec.offseted(-d.doubleValue());
         }
      } else if (a instanceof Number d && b instanceof FiguraVec2 vec) {
         return vec.scaled(-1.0).offset(d.doubleValue());
      }

      throw new LuaError("Invalid types to __sub: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, FiguraMat2.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, Double.class, FiguraVec2.class})
      }
   )
   public static FiguraVec2 __mul(@LuaNotNil Object a, @LuaNotNil Object b) {
      if (a instanceof FiguraVec2 vec) {
         if (b instanceof FiguraVec2 vec2) {
            return vec.times(vec2);
         }

         if (b instanceof Number d) {
            return vec.scaled(d.doubleValue());
         }

         if (b instanceof FiguraMat2 mat) {
            return vec.copy().transform(mat);
         }
      } else if (a instanceof Number d && b instanceof FiguraVec2 vec) {
         return vec.scaled(d.doubleValue());
      }

      throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, Double.class})
      }
   )
   public FiguraVec2 __div(@LuaNotNil Object rhs) {
      if (rhs instanceof Number n) {
         double d = n.doubleValue();
         if (d == 0.0) {
            throw new LuaError("Attempt to divide vector by 0");
         } else {
            return this.scaled(1.0 / d);
         }
      } else if (rhs instanceof FiguraVec2 vec) {
         return this.dividedBy(vec);
      } else {
         throw new LuaError("Invalid types to __div: " + this.getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class, Double.class})
      }
   )
   public FiguraVec2 __mod(@LuaNotNil Object rhs) {
      if (rhs instanceof Number n) {
         double d = n.doubleValue();
         if (d == 0.0) {
            throw new LuaError("Attempt to reduce vector by 0");
         }

         FiguraVec2 modulus = of(d, d);
         return this.mod(modulus);
      } else if (rhs instanceof FiguraVec2 vec) {
         return this.mod(vec);
      } else {
         throw new LuaError("Invalid types to __mod: " + this.getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Boolean.class, FiguraVec2.class, FiguraVec2.class}))
   public boolean __eq(FiguraVec2 other) {
      return this.equals(other);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVec2.class, FiguraVec2.class}))
   public FiguraVec2 __unm() {
      return this.scaled(-1.0);
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Integer.class, FiguraVec2.class}))
   public int __len() {
      return this.size();
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Boolean.class, FiguraVec2.class, FiguraVec2.class}))
   public boolean __lt(@LuaNotNil FiguraVec2 r) {
      return this.x < r.x && this.y < r.y;
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {Boolean.class, FiguraVec2.class, FiguraVec2.class}))
   public boolean __le(@LuaNotNil FiguraVec2 r) {
      return this.x <= r.x && this.y <= r.y;
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {String.class, FiguraVec2.class}))
   public String __tostring() {
      return this.toString();
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {Double.class, FiguraVec2.class, Integer.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {Double.class, FiguraVec2.class, String.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {FiguraVector.class, FiguraVec2.class, String.class}, comment = "vector_n.comments.swizzle")
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
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {void.class, FiguraVec2.class, Integer.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {void.class, FiguraVec2.class, String.class, Double.class}),
            @LuaMetamethodDoc.LuaMetamethodOverload(types = {void.class, FiguraVec2.class, String.class, FiguraVector.class})
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
