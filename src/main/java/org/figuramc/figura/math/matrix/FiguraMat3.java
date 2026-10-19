package org.figuramc.figura.math.matrix;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;
import org.joml.Matrix3f;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "Matrix3", value = "matrix3")
public class FiguraMat3 extends FiguraMatrix<FiguraMat3, FiguraVec3> {
   public double v11 = 1.0;
   public double v12;
   public double v13;
   public double v21;
   public double v22 = 1.0;
   public double v23;
   public double v31;
   public double v32;
   public double v33 = 1.0;

   public FiguraMat3 set(Matrix3f mat) {
      return this.set(mat.m00(), mat.m01(), mat.m02(), mat.m10(), mat.m11(), mat.m12(), mat.m20(), mat.m21(), mat.m22());
   }

   public Matrix3f toMatrix3f() {
      return new Matrix3f(
         (float)this.v11,
         (float)this.v21,
         (float)this.v31,
         (float)this.v12,
         (float)this.v22,
         (float)this.v32,
         (float)this.v13,
         (float)this.v23,
         (float)this.v33
      );
   }

   public void copyDataTo(Matrix3f vanillaMatrix) {
      vanillaMatrix.set(
         (float)this.v11,
         (float)this.v21,
         (float)this.v31,
         (float)this.v12,
         (float)this.v22,
         (float)this.v32,
         (float)this.v13,
         (float)this.v23,
         (float)this.v33
      );
   }

   public static FiguraMat3 of() {
      return new FiguraMat3();
   }

   public static FiguraMat3 of(double n11, double n21, double n31, double n12, double n22, double n32, double n13, double n23, double n33) {
      return of().set(n11, n21, n31, n12, n22, n32, n13, n23, n33);
   }

   @Override
   public void resetIdentity() {
      this.v12 = this.v13 = this.v21 = this.v23 = this.v31 = this.v32 = 0.0;
      this.v11 = this.v22 = this.v33 = 1.0;
   }

   @Override
   protected double calculateDeterminant() {
      double sub11 = this.v22 * this.v33 - this.v23 * this.v32;
      double sub12 = this.v21 * this.v33 - this.v23 * this.v31;
      double sub13 = this.v21 * this.v32 - this.v22 * this.v31;
      return this.v11 * sub11 - this.v12 * sub12 + this.v13 * sub13;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat3.class), value = "matrix_n.copy")
   public FiguraMat3 copy() {
      return of(this.v11, this.v21, this.v31, this.v12, this.v22, this.v32, this.v13, this.v23, this.v33);
   }

   public boolean equals(FiguraMat3 o) {
      return this.v11 == o.v11
         && this.v12 == o.v12
         && this.v13 == o.v13
         && this.v21 == o.v21
         && this.v22 == o.v22
         && this.v23 == o.v23
         && this.v31 == o.v31
         && this.v32 == o.v32
         && this.v33 == o.v33;
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof FiguraMat3 o ? this.equals(o) : false;
   }

   @Override
   public String toString() {
      return this.getString(this.v11, this.v12, this.v13, this.v21, this.v22, this.v23, this.v31, this.v32, this.v33);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "col", returnType = FiguraVec3.class),
      value = "matrix_n.get_column"
   )
   public FiguraVec3 getColumn(int col) {
      return switch (col) {
         case 1 -> FiguraVec3.of(this.v11, this.v21, this.v31);
         case 2 -> FiguraVec3.of(this.v12, this.v22, this.v32);
         case 3 -> FiguraVec3.of(this.v13, this.v23, this.v33);
         default -> throw new LuaError("Column must be 1 to " + this.cols());
      };
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "row", returnType = FiguraVec3.class),
      value = "matrix_n.get_row"
   )
   public FiguraVec3 getRow(int row) {
      return switch (row) {
         case 1 -> FiguraVec3.of(this.v11, this.v12, this.v13);
         case 2 -> FiguraVec3.of(this.v21, this.v22, this.v23);
         case 3 -> FiguraVec3.of(this.v31, this.v32, this.v33);
         default -> throw new LuaError("Row must be 1 to " + this.rows());
      };
   }

   @Override
   public int rows() {
      return 3;
   }

   @Override
   public int cols() {
      return 3;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat3.class, argumentNames = "other", returnType = FiguraMat3.class),
      value = "matrix_n.set"
   )
   public FiguraMat3 set(@LuaNotNil FiguraMat3 o) {
      return this.set(o.v11, o.v21, o.v31, o.v12, o.v22, o.v32, o.v13, o.v23, o.v33);
   }

   public FiguraMat3 set(double n11, double n21, double n31, double n12, double n22, double n32, double n13, double n23, double n33) {
      this.v11 = n11;
      this.v12 = n12;
      this.v13 = n13;
      this.v21 = n21;
      this.v22 = n22;
      this.v23 = n23;
      this.v31 = n31;
      this.v32 = n32;
      this.v33 = n33;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat3.class, argumentNames = "other", returnType = FiguraMat3.class),
      value = "matrix_n.multiply"
   )
   public FiguraMat3 multiply(@LuaNotNil FiguraMat3 o) {
      double nv11 = o.v11 * this.v11 + o.v12 * this.v21 + o.v13 * this.v31;
      double nv12 = o.v11 * this.v12 + o.v12 * this.v22 + o.v13 * this.v32;
      double nv13 = o.v11 * this.v13 + o.v12 * this.v23 + o.v13 * this.v33;
      double nv21 = o.v21 * this.v11 + o.v22 * this.v21 + o.v23 * this.v31;
      double nv22 = o.v21 * this.v12 + o.v22 * this.v22 + o.v23 * this.v32;
      double nv23 = o.v21 * this.v13 + o.v22 * this.v23 + o.v23 * this.v33;
      double nv31 = o.v31 * this.v11 + o.v32 * this.v21 + o.v33 * this.v31;
      double nv32 = o.v31 * this.v12 + o.v32 * this.v22 + o.v33 * this.v32;
      double nv33 = o.v31 * this.v13 + o.v32 * this.v23 + o.v33 * this.v33;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.v21 = nv21;
      this.v22 = nv22;
      this.v23 = nv23;
      this.v31 = nv31;
      this.v32 = nv32;
      this.v33 = nv33;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat3.class, argumentNames = "other", returnType = FiguraMat3.class),
      value = "matrix_n.right_multiply"
   )
   public FiguraMat3 rightMultiply(@LuaNotNil FiguraMat3 o) {
      double nv11 = this.v11 * o.v11 + this.v12 * o.v21 + this.v13 * o.v31;
      double nv12 = this.v11 * o.v12 + this.v12 * o.v22 + this.v13 * o.v32;
      double nv13 = this.v11 * o.v13 + this.v12 * o.v23 + this.v13 * o.v33;
      double nv21 = this.v21 * o.v11 + this.v22 * o.v21 + this.v23 * o.v31;
      double nv22 = this.v21 * o.v12 + this.v22 * o.v22 + this.v23 * o.v32;
      double nv23 = this.v21 * o.v13 + this.v22 * o.v23 + this.v23 * o.v33;
      double nv31 = this.v31 * o.v11 + this.v32 * o.v21 + this.v33 * o.v31;
      double nv32 = this.v31 * o.v12 + this.v32 * o.v22 + this.v33 * o.v32;
      double nv33 = this.v31 * o.v13 + this.v32 * o.v23 + this.v33 * o.v33;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.v21 = nv21;
      this.v22 = nv22;
      this.v23 = nv23;
      this.v31 = nv31;
      this.v32 = nv32;
      this.v33 = nv33;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat3.class), value = "matrix_n.transpose")
   public FiguraMat3 transpose() {
      double temp = this.v12;
      this.v12 = this.v21;
      this.v21 = temp;
      temp = this.v13;
      this.v13 = this.v31;
      this.v31 = temp;
      temp = this.v23;
      this.v23 = this.v32;
      this.v32 = temp;
      this.cachedInverse = null;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat3.class), value = "matrix_n.transposed")
   public FiguraMat3 transposed() {
      return (FiguraMat3)super.transposed();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat3.class), value = "matrix_n.invert")
   public FiguraMat3 invert() {
      FiguraMat3 capture = this.copy();
      if (this.cachedInverse != null) {
         this.set(this.cachedInverse);
         this.cachedDeterminant = 1.0 / this.cachedDeterminant;
      } else {
         double sub11 = this.v22 * this.v33 - this.v23 * this.v32;
         double sub12 = this.v21 * this.v33 - this.v23 * this.v31;
         double sub13 = this.v21 * this.v32 - this.v22 * this.v31;
         double sub21 = this.v12 * this.v33 - this.v13 * this.v32;
         double sub22 = this.v11 * this.v33 - this.v13 * this.v31;
         double sub23 = this.v11 * this.v32 - this.v12 * this.v31;
         double sub31 = this.v12 * this.v23 - this.v13 * this.v22;
         double sub32 = this.v11 * this.v23 - this.v13 * this.v21;
         double sub33 = this.v11 * this.v22 - this.v12 * this.v21;
         double det = this.v11 * sub11 - this.v12 * sub12 + this.v13 * sub13;
         if (det == 0.0) {
            det = Double.MIN_VALUE;
         }

         det = 1.0 / det;
         this.cachedDeterminant = det;
         this.set(det * sub11, -det * sub12, det * sub13, -det * sub21, det * sub22, -det * sub23, det * sub31, -det * sub32, det * sub33);
      }

      this.cachedInverse = capture;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat3.class), value = "matrix_n.inverted")
   public FiguraMat3 inverted() {
      return (FiguraMat3)super.inverted();
   }

   @LuaWhitelist
   @LuaMethodDoc("matrix_n.det")
   @Override
   public double det() {
      return super.det();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat3.class), value = "matrix_n.reset")
   public FiguraMat3 reset() {
      return (FiguraMat3)super.reset();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat3.class, argumentNames = "other", returnType = FiguraMat3.class),
      value = "matrix_n.add"
   )
   public FiguraMat3 add(@LuaNotNil FiguraMat3 o) {
      this.v11 = this.v11 + o.v11;
      this.v12 = this.v12 + o.v12;
      this.v13 = this.v13 + o.v13;
      this.v21 = this.v21 + o.v21;
      this.v22 = this.v22 + o.v22;
      this.v23 = this.v23 + o.v23;
      this.v31 = this.v31 + o.v31;
      this.v32 = this.v32 + o.v32;
      this.v33 = this.v33 + o.v33;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat3.class, argumentNames = "other", returnType = FiguraMat3.class),
      value = "matrix_n.sub"
   )
   public FiguraMat3 sub(@LuaNotNil FiguraMat3 o) {
      this.v11 = this.v11 - o.v11;
      this.v12 = this.v12 - o.v12;
      this.v13 = this.v13 - o.v13;
      this.v21 = this.v21 - o.v21;
      this.v22 = this.v22 - o.v22;
      this.v23 = this.v23 - o.v23;
      this.v31 = this.v31 - o.v31;
      this.v32 = this.v32 - o.v32;
      this.v33 = this.v33 - o.v33;
      this.invalidate();
      return this;
   }

   public FiguraMat3 scale(double x, double y, double z) {
      this.v11 *= x;
      this.v12 *= x;
      this.v13 *= x;
      this.v21 *= y;
      this.v22 *= y;
      this.v23 *= y;
      this.v31 *= z;
      this.v32 *= z;
      this.v33 *= z;
      this.invalidate();
      return this;
   }

   public FiguraMat3 scale(FiguraVec3 vec) {
      return this.scale(vec.x, vec.y, vec.z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      value = "matrix_n.scale"
   )
   public FiguraMat3 scale(Object x, Double y, Double z) {
      return this.scale(LuaUtils.parseOneArgVec("scale", x, y, z, 1.0));
   }

   public FiguraMat3 translate(double x, double y) {
      this.v11 = this.v11 + x * this.v31;
      this.v12 = this.v12 + x * this.v32;
      this.v13 = this.v13 + x * this.v33;
      this.v21 = this.v21 + y * this.v31;
      this.v22 = this.v22 + y * this.v32;
      this.v23 = this.v23 + y * this.v33;
      this.invalidate();
      return this;
   }

   public FiguraMat3 translate(FiguraVec2 amount) {
      return this.translate(amount.x, amount.y);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "matrix_n.translate"
   )
   public FiguraMat3 translate(Object x, Double y) {
      return this.translate(LuaUtils.parseVec2("translate", x, y));
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "degrees"), value = "matrix_n.rotate_x")
   public FiguraMat3 rotateX(double degrees) {
      degrees = Math.toRadians(degrees);
      double c = Math.cos(degrees);
      double s = Math.sin(degrees);
      double nv21 = c * this.v21 - s * this.v31;
      double nv22 = c * this.v22 - s * this.v32;
      double nv23 = c * this.v23 - s * this.v33;
      this.v31 = s * this.v21 + c * this.v31;
      this.v32 = s * this.v22 + c * this.v32;
      this.v33 = s * this.v23 + c * this.v33;
      this.v21 = nv21;
      this.v22 = nv22;
      this.v23 = nv23;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "degrees"), value = "matrix_n.rotate_y")
   public FiguraMat3 rotateY(double degrees) {
      degrees = Math.toRadians(degrees);
      double c = Math.cos(degrees);
      double s = Math.sin(degrees);
      double nv11 = c * this.v11 + s * this.v31;
      double nv12 = c * this.v12 + s * this.v32;
      double nv13 = c * this.v13 + s * this.v33;
      this.v31 = c * this.v31 - s * this.v11;
      this.v32 = c * this.v32 - s * this.v12;
      this.v33 = c * this.v33 - s * this.v13;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "degrees"), value = "matrix_n.rotate_z")
   public FiguraMat3 rotateZ(double degrees) {
      degrees = Math.toRadians(degrees);
      double c = Math.cos(degrees);
      double s = Math.sin(degrees);
      double nv11 = c * this.v11 - s * this.v21;
      double nv12 = c * this.v12 - s * this.v22;
      double nv13 = c * this.v13 - s * this.v23;
      this.v21 = c * this.v21 + s * this.v11;
      this.v22 = c * this.v22 + s * this.v12;
      this.v23 = c * this.v23 + s * this.v13;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.invalidate();
      return this;
   }

   public void rotateZYX(double x, double y, double z) {
      x = Math.toRadians(x);
      y = Math.toRadians(y);
      z = Math.toRadians(z);
      double a = Math.cos(x);
      double b = Math.sin(x);
      double c = Math.cos(y);
      double d = Math.sin(y);
      double e = Math.cos(z);
      double f = Math.sin(z);
      double bc = b * c;
      double ac = a * c;
      double ce = c * e;
      double cf = c * f;
      double p1 = b * d * e - a * f;
      double p2 = a * d * e + b * f;
      double p3 = a * e + b * d * f;
      double p4 = a * d * f - b * e;
      double nv11 = ce * this.v11 + p1 * this.v21 + p2 * this.v31;
      double nv21 = cf * this.v11 + p3 * this.v21 + p4 * this.v31;
      double nv31 = -d * this.v11 + bc * this.v21 + ac * this.v31;
      double nv12 = ce * this.v12 + p1 * this.v22 + p2 * this.v32;
      double nv22 = cf * this.v12 + p3 * this.v22 + p4 * this.v32;
      double nv32 = -d * this.v12 + bc * this.v22 + ac * this.v32;
      double nv13 = ce * this.v13 + p1 * this.v23 + p2 * this.v33;
      double nv23 = cf * this.v13 + p3 * this.v23 + p4 * this.v33;
      double nv33 = -d * this.v13 + bc * this.v23 + ac * this.v33;
      this.v11 = nv11;
      this.v21 = nv21;
      this.v31 = nv31;
      this.v12 = nv12;
      this.v22 = nv22;
      this.v32 = nv32;
      this.v13 = nv13;
      this.v23 = nv23;
      this.v33 = nv33;
      this.invalidate();
   }

   public void rotateZYX(FiguraVec3 vec) {
      this.rotateZYX(vec.x, vec.y, vec.z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "vec", returnType = FiguraMat3.class),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"}, returnType = FiguraMat3.class)
      },
      value = "matrix_n.rotate"
   )
   public FiguraMat3 rotate(Object x, Double y, Double z) {
      this.rotateZYX(LuaUtils.parseVec3("rotate", x, y, z));
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("matrix_n.deaugmented")
   public FiguraMat2 deaugmented() {
      FiguraMat2 result = FiguraMat2.of();
      result.set(this.v11, this.v21, this.v12, this.v22);
      return result;
   }

   @LuaWhitelist
   @LuaMethodDoc("matrix_n.augmented")
   public FiguraMat4 augmented() {
      FiguraMat4 result = FiguraMat4.of();
      result.set(this.v11, this.v21, this.v31, 0.0, this.v12, this.v22, this.v32, 0.0, this.v13, this.v23, this.v33, 0.0, 0.0, 0.0, 0.0, 1.0);
      return result;
   }

   public FiguraVec2 apply(FiguraVec3 vec) {
      FiguraVec3 result = this.times(vec);
      return FiguraVec2.of(result.x, result.y);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "matrix_n.apply"
   )
   public FiguraVec2 apply(Object x, Double y) {
      FiguraVec2 vec = LuaUtils.parseVec2("apply", x, y);
      return this.apply(FiguraVec3.of(vec.x, vec.y, 1.0));
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"x", "y"})
      },
      value = "matrix_n.apply_dir"
   )
   public FiguraVec2 applyDir(Object x, Double y, Double z) {
      FiguraVec2 vec = LuaUtils.parseVec2("applyDir", x, y);
      return this.apply(FiguraVec3.of(vec.x, vec.y, 0.0));
   }

   @LuaWhitelist
   public FiguraMat3 __add(@LuaNotNil FiguraMat3 mat) {
      return this.plus(mat);
   }

   @LuaWhitelist
   public FiguraMat3 __sub(@LuaNotNil FiguraMat3 mat) {
      return this.minus(mat);
   }

   @LuaWhitelist
   public Object __mul(@LuaNotNil Object o) {
      if (o instanceof FiguraMat3 mat) {
         return mat.times(this);
      } else if (o instanceof FiguraVec3 vec) {
         return this.times(vec);
      } else if (o instanceof Number n) {
         return this.copy().scale(n.doubleValue(), n.doubleValue(), n.doubleValue());
      } else {
         throw new LuaError("Invalid types to Matrix3 __mul: " + o.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   public boolean __eq(Object o) {
      return this.equals(o);
   }

   @LuaWhitelist
   public int __len() {
      return 3;
   }

   @LuaWhitelist
   public String __tostring() {
      return this.toString();
   }

   @LuaWhitelist
   public Object __index(String string) {
      if (string == null) {
         return null;
      }

      return switch (string) {
         case "1", "c1" -> this.getColumn(1);
         case "2", "c2" -> this.getColumn(2);
         case "3", "c3" -> this.getColumn(3);
         case "r1" -> this.getRow(1);
         case "r2" -> this.getRow(2);
         case "r3" -> this.getRow(3);
         case "v11" -> this.v11;
         case "v12" -> this.v12;
         case "v13" -> this.v13;
         case "v21" -> this.v21;
         case "v22" -> this.v22;
         case "v23" -> this.v23;
         case "v31" -> this.v31;
         case "v32" -> this.v32;
         case "v33" -> this.v33;
         default -> null;
      };
   }

   @LuaWhitelist
   public void __newindex(@LuaNotNil String string, Object value) {
      if (value instanceof FiguraVec3 vec3) {
         switch (string) {
            case "1":
            case "c1":
               this.v11 = vec3.x;
               this.v21 = vec3.y;
               this.v31 = vec3.z;
               break;
            case "2":
            case "c2":
               this.v12 = vec3.x;
               this.v22 = vec3.y;
               this.v32 = vec3.z;
               break;
            case "3":
            case "c3":
               this.v13 = vec3.x;
               this.v23 = vec3.y;
               this.v33 = vec3.z;
               break;
            case "r1":
               this.v11 = vec3.x;
               this.v12 = vec3.y;
               this.v13 = vec3.z;
               break;
            case "r2":
               this.v21 = vec3.x;
               this.v22 = vec3.y;
               this.v23 = vec3.z;
               break;
            case "r3":
               this.v31 = vec3.x;
               this.v32 = vec3.y;
               this.v33 = vec3.z;
         }
      } else if (value instanceof Number num) {
         switch (string) {
            case "v11":
               this.v11 = num.doubleValue();
               break;
            case "v12":
               this.v12 = num.doubleValue();
               break;
            case "v13":
               this.v13 = num.doubleValue();
               break;
            case "v21":
               this.v21 = num.doubleValue();
               break;
            case "v22":
               this.v22 = num.doubleValue();
               break;
            case "v23":
               this.v23 = num.doubleValue();
               break;
            case "v31":
               this.v31 = num.doubleValue();
               break;
            case "v32":
               this.v32 = num.doubleValue();
               break;
            case "v33":
               this.v33 = num.doubleValue();
         }
      } else {
         throw new LuaError("Illegal arguments to Matrix3 __newindex: " + string + ", " + value);
      }
   }
}
