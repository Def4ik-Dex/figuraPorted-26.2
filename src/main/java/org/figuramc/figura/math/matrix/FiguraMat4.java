package org.figuramc.figura.math.matrix;

import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.utils.LuaUtils;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "Matrix4", value = "matrix4")
public class FiguraMat4 extends FiguraMatrix<FiguraMat4, FiguraVec4> {
   public double v11 = 1.0;
   public double v12;
   public double v13;
   public double v14;
   public double v21;
   public double v22 = 1.0;
   public double v23;
   public double v24;
   public double v31;
   public double v32;
   public double v33 = 1.0;
   public double v34;
   public double v41;
   public double v42;
   public double v43;
   public double v44 = 1.0;

   public FiguraMat4 set(Matrix4f mat) {
      return this.set(
         mat.m00(),
         mat.m01(),
         mat.m02(),
         mat.m03(),
         mat.m10(),
         mat.m11(),
         mat.m12(),
         mat.m13(),
         mat.m20(),
         mat.m21(),
         mat.m22(),
         mat.m23(),
         mat.m30(),
         mat.m31(),
         mat.m32(),
         mat.m33()
      );
   }

   public FiguraMat4 set(Matrix4d mat) {
      return this.set(
         mat.m00(),
         mat.m01(),
         mat.m02(),
         mat.m03(),
         mat.m10(),
         mat.m11(),
         mat.m12(),
         mat.m13(),
         mat.m20(),
         mat.m21(),
         mat.m22(),
         mat.m23(),
         mat.m30(),
         mat.m31(),
         mat.m32(),
         mat.m33()
      );
   }

   public Matrix4f toMatrix4f() {
      return new Matrix4f(
         (float)this.v11,
         (float)this.v21,
         (float)this.v31,
         (float)this.v41,
         (float)this.v12,
         (float)this.v22,
         (float)this.v32,
         (float)this.v42,
         (float)this.v13,
         (float)this.v23,
         (float)this.v33,
         (float)this.v43,
         (float)this.v14,
         (float)this.v24,
         (float)this.v34,
         (float)this.v44
      );
   }

   public void copyDataTo(Matrix4f vanillaMatrix) {
      vanillaMatrix.set(
         (float)this.v11,
         (float)this.v21,
         (float)this.v31,
         (float)this.v41,
         (float)this.v12,
         (float)this.v22,
         (float)this.v32,
         (float)this.v42,
         (float)this.v13,
         (float)this.v23,
         (float)this.v33,
         (float)this.v43,
         (float)this.v14,
         (float)this.v24,
         (float)this.v34,
         (float)this.v44
      );
   }

   public static FiguraMat4 of() {
      return new FiguraMat4();
   }

   public static FiguraMat4 of(
      double n11,
      double n21,
      double n31,
      double n41,
      double n12,
      double n22,
      double n32,
      double n42,
      double n13,
      double n23,
      double n33,
      double n43,
      double n14,
      double n24,
      double n34,
      double n44
   ) {
      return of().set(n11, n21, n31, n41, n12, n22, n32, n42, n13, n23, n33, n43, n14, n24, n34, n44);
   }

   @Override
   public void resetIdentity() {
      this.v12 = this.v13 = this.v14 = this.v21 = this.v23 = this.v24 = this.v31 = this.v32 = this.v34 = this.v41 = this.v42 = this.v43 = 0.0;
      this.v11 = this.v22 = this.v33 = this.v44 = 1.0;
   }

   @Override
   protected double calculateDeterminant() {
      double A2323 = this.v33 * this.v44 - this.v34 * this.v43;
      double A1323 = this.v32 * this.v44 - this.v34 * this.v42;
      double A1223 = this.v32 * this.v43 - this.v33 * this.v42;
      double A0323 = this.v31 * this.v44 - this.v34 * this.v41;
      double A0223 = this.v31 * this.v43 - this.v33 * this.v41;
      double A0123 = this.v31 * this.v42 - this.v32 * this.v41;
      return this.v11 * (this.v22 * A2323 - this.v23 * A1323 + this.v24 * A1223)
         - this.v12 * (this.v21 * A2323 - this.v23 * A0323 + this.v24 * A0223)
         + this.v13 * (this.v21 * A1323 - this.v22 * A0323 + this.v24 * A0123)
         - this.v14 * (this.v21 * A1223 - this.v22 * A0223 + this.v23 * A0123);
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat4.class), value = "matrix_n.copy")
   public FiguraMat4 copy() {
      return of(
         this.v11,
         this.v21,
         this.v31,
         this.v41,
         this.v12,
         this.v22,
         this.v32,
         this.v42,
         this.v13,
         this.v23,
         this.v33,
         this.v43,
         this.v14,
         this.v24,
         this.v34,
         this.v44
      );
   }

   public boolean equals(FiguraMat4 o) {
      return this.v11 == o.v11
         && this.v12 == o.v12
         && this.v13 == o.v13
         && this.v14 == o.v14
         && this.v21 == o.v21
         && this.v22 == o.v22
         && this.v23 == o.v23
         && this.v24 == o.v24
         && this.v31 == o.v31
         && this.v32 == o.v32
         && this.v33 == o.v33
         && this.v34 == o.v34
         && this.v41 == o.v41
         && this.v42 == o.v42
         && this.v43 == o.v43
         && this.v44 == o.v44;
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof FiguraMat4 o && this.equals(o);
   }

   @Override
   public String toString() {
      return this.getString(
         this.v11,
         this.v12,
         this.v13,
         this.v14,
         this.v21,
         this.v22,
         this.v23,
         this.v24,
         this.v31,
         this.v32,
         this.v33,
         this.v34,
         this.v41,
         this.v42,
         this.v43,
         this.v44
      );
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "col", returnType = FiguraVec4.class),
      value = "matrix_n.get_column"
   )
   public FiguraVec4 getColumn(int col) {
      return switch (col) {
         case 1 -> FiguraVec4.of(this.v11, this.v21, this.v31, this.v41);
         case 2 -> FiguraVec4.of(this.v12, this.v22, this.v32, this.v42);
         case 3 -> FiguraVec4.of(this.v13, this.v23, this.v33, this.v43);
         case 4 -> FiguraVec4.of(this.v14, this.v24, this.v34, this.v44);
         default -> throw new LuaError("Column must be 1 to " + this.cols());
      };
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "row", returnType = FiguraVec4.class),
      value = "matrix_n.get_row"
   )
   public FiguraVec4 getRow(int row) {
      return switch (row) {
         case 1 -> FiguraVec4.of(this.v11, this.v12, this.v13, this.v14);
         case 2 -> FiguraVec4.of(this.v21, this.v22, this.v23, this.v24);
         case 3 -> FiguraVec4.of(this.v31, this.v32, this.v33, this.v34);
         case 4 -> FiguraVec4.of(this.v41, this.v42, this.v43, this.v44);
         default -> throw new LuaError("Row must be 1 to " + this.rows());
      };
   }

   @Override
   public int rows() {
      return 4;
   }

   @Override
   public int cols() {
      return 4;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat4.class, argumentNames = "other", returnType = FiguraMat4.class),
      value = "matrix_n.set"
   )
   public FiguraMat4 set(@LuaNotNil FiguraMat4 o) {
      return this.set(o.v11, o.v21, o.v31, o.v41, o.v12, o.v22, o.v32, o.v42, o.v13, o.v23, o.v33, o.v43, o.v14, o.v24, o.v34, o.v44);
   }

   public FiguraMat4 set(
      double n11,
      double n21,
      double n31,
      double n41,
      double n12,
      double n22,
      double n32,
      double n42,
      double n13,
      double n23,
      double n33,
      double n43,
      double n14,
      double n24,
      double n34,
      double n44
   ) {
      this.v11 = n11;
      this.v12 = n12;
      this.v13 = n13;
      this.v14 = n14;
      this.v21 = n21;
      this.v22 = n22;
      this.v23 = n23;
      this.v24 = n24;
      this.v31 = n31;
      this.v32 = n32;
      this.v33 = n33;
      this.v34 = n34;
      this.v41 = n41;
      this.v42 = n42;
      this.v43 = n43;
      this.v44 = n44;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat4.class, argumentNames = "other", returnType = FiguraMat4.class),
      value = "matrix_n.multiply"
   )
   public FiguraMat4 multiply(@LuaNotNil FiguraMat4 o) {
      double nv11 = o.v11 * this.v11 + o.v12 * this.v21 + o.v13 * this.v31 + o.v14 * this.v41;
      double nv12 = o.v11 * this.v12 + o.v12 * this.v22 + o.v13 * this.v32 + o.v14 * this.v42;
      double nv13 = o.v11 * this.v13 + o.v12 * this.v23 + o.v13 * this.v33 + o.v14 * this.v43;
      double nv14 = o.v11 * this.v14 + o.v12 * this.v24 + o.v13 * this.v34 + o.v14 * this.v44;
      double nv21 = o.v21 * this.v11 + o.v22 * this.v21 + o.v23 * this.v31 + o.v24 * this.v41;
      double nv22 = o.v21 * this.v12 + o.v22 * this.v22 + o.v23 * this.v32 + o.v24 * this.v42;
      double nv23 = o.v21 * this.v13 + o.v22 * this.v23 + o.v23 * this.v33 + o.v24 * this.v43;
      double nv24 = o.v21 * this.v14 + o.v22 * this.v24 + o.v23 * this.v34 + o.v24 * this.v44;
      double nv31 = o.v31 * this.v11 + o.v32 * this.v21 + o.v33 * this.v31 + o.v34 * this.v41;
      double nv32 = o.v31 * this.v12 + o.v32 * this.v22 + o.v33 * this.v32 + o.v34 * this.v42;
      double nv33 = o.v31 * this.v13 + o.v32 * this.v23 + o.v33 * this.v33 + o.v34 * this.v43;
      double nv34 = o.v31 * this.v14 + o.v32 * this.v24 + o.v33 * this.v34 + o.v34 * this.v44;
      double nv41 = o.v41 * this.v11 + o.v42 * this.v21 + o.v43 * this.v31 + o.v44 * this.v41;
      double nv42 = o.v41 * this.v12 + o.v42 * this.v22 + o.v43 * this.v32 + o.v44 * this.v42;
      double nv43 = o.v41 * this.v13 + o.v42 * this.v23 + o.v43 * this.v33 + o.v44 * this.v43;
      this.v44 = o.v41 * this.v14 + o.v42 * this.v24 + o.v43 * this.v34 + o.v44 * this.v44;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.v14 = nv14;
      this.v21 = nv21;
      this.v22 = nv22;
      this.v23 = nv23;
      this.v24 = nv24;
      this.v31 = nv31;
      this.v32 = nv32;
      this.v33 = nv33;
      this.v34 = nv34;
      this.v41 = nv41;
      this.v42 = nv42;
      this.v43 = nv43;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat4.class, argumentNames = "other", returnType = FiguraMat4.class),
      value = "matrix_n.right_multiply"
   )
   public FiguraMat4 rightMultiply(@LuaNotNil FiguraMat4 o) {
      double nv11 = this.v11 * o.v11 + this.v12 * o.v21 + this.v13 * o.v31 + this.v14 * o.v41;
      double nv12 = this.v11 * o.v12 + this.v12 * o.v22 + this.v13 * o.v32 + this.v14 * o.v42;
      double nv13 = this.v11 * o.v13 + this.v12 * o.v23 + this.v13 * o.v33 + this.v14 * o.v43;
      double nv14 = this.v11 * o.v14 + this.v12 * o.v24 + this.v13 * o.v34 + this.v14 * o.v44;
      double nv21 = this.v21 * o.v11 + this.v22 * o.v21 + this.v23 * o.v31 + this.v24 * o.v41;
      double nv22 = this.v21 * o.v12 + this.v22 * o.v22 + this.v23 * o.v32 + this.v24 * o.v42;
      double nv23 = this.v21 * o.v13 + this.v22 * o.v23 + this.v23 * o.v33 + this.v24 * o.v43;
      double nv24 = this.v21 * o.v14 + this.v22 * o.v24 + this.v23 * o.v34 + this.v24 * o.v44;
      double nv31 = this.v31 * o.v11 + this.v32 * o.v21 + this.v33 * o.v31 + this.v34 * o.v41;
      double nv32 = this.v31 * o.v12 + this.v32 * o.v22 + this.v33 * o.v32 + this.v34 * o.v42;
      double nv33 = this.v31 * o.v13 + this.v32 * o.v23 + this.v33 * o.v33 + this.v34 * o.v43;
      double nv34 = this.v31 * o.v14 + this.v32 * o.v24 + this.v33 * o.v34 + this.v34 * o.v44;
      double nv41 = this.v41 * o.v11 + this.v42 * o.v21 + this.v43 * o.v31 + this.v44 * o.v41;
      double nv42 = this.v41 * o.v12 + this.v42 * o.v22 + this.v43 * o.v32 + this.v44 * o.v42;
      double nv43 = this.v41 * o.v13 + this.v42 * o.v23 + this.v43 * o.v33 + this.v44 * o.v43;
      this.v44 = this.v41 * o.v14 + this.v42 * o.v24 + this.v43 * o.v34 + this.v44 * o.v44;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.v14 = nv14;
      this.v21 = nv21;
      this.v22 = nv22;
      this.v23 = nv23;
      this.v24 = nv24;
      this.v31 = nv31;
      this.v32 = nv32;
      this.v33 = nv33;
      this.v34 = nv34;
      this.v41 = nv41;
      this.v42 = nv42;
      this.v43 = nv43;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat4.class), value = "matrix_n.transpose")
   public FiguraMat4 transpose() {
      double temp = this.v12;
      this.v12 = this.v21;
      this.v21 = temp;
      temp = this.v13;
      this.v13 = this.v31;
      this.v31 = temp;
      temp = this.v14;
      this.v14 = this.v41;
      this.v41 = temp;
      temp = this.v23;
      this.v23 = this.v32;
      this.v32 = temp;
      temp = this.v24;
      this.v24 = this.v42;
      this.v42 = temp;
      temp = this.v34;
      this.v34 = this.v43;
      this.v43 = temp;
      this.cachedInverse = null;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat4.class), value = "matrix_n.transposed")
   public FiguraMat4 transposed() {
      return (FiguraMat4)super.transposed();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat4.class), value = "matrix_n.invert")
   public FiguraMat4 invert() {
      FiguraMat4 capture = this.copy();
      if (this.cachedInverse != null) {
         this.set(this.cachedInverse);
         this.cachedDeterminant = 1.0 / this.cachedDeterminant;
      } else {
         double A2323 = this.v33 * this.v44 - this.v34 * this.v43;
         double A1323 = this.v32 * this.v44 - this.v34 * this.v42;
         double A1223 = this.v32 * this.v43 - this.v33 * this.v42;
         double A0323 = this.v31 * this.v44 - this.v34 * this.v41;
         double A0223 = this.v31 * this.v43 - this.v33 * this.v41;
         double A0123 = this.v31 * this.v42 - this.v32 * this.v41;
         double A2313 = this.v23 * this.v44 - this.v24 * this.v43;
         double A1313 = this.v22 * this.v44 - this.v24 * this.v42;
         double A1213 = this.v22 * this.v43 - this.v23 * this.v42;
         double A2312 = this.v23 * this.v34 - this.v24 * this.v33;
         double A1312 = this.v22 * this.v34 - this.v24 * this.v32;
         double A1212 = this.v22 * this.v33 - this.v23 * this.v32;
         double A0313 = this.v21 * this.v44 - this.v24 * this.v41;
         double A0213 = this.v21 * this.v43 - this.v23 * this.v41;
         double A0312 = this.v21 * this.v34 - this.v24 * this.v31;
         double A0212 = this.v21 * this.v33 - this.v23 * this.v31;
         double A0113 = this.v21 * this.v42 - this.v22 * this.v41;
         double A0112 = this.v21 * this.v32 - this.v22 * this.v31;
         double det = this.v11 * (this.v22 * A2323 - this.v23 * A1323 + this.v24 * A1223)
            - this.v12 * (this.v21 * A2323 - this.v23 * A0323 + this.v24 * A0223)
            + this.v13 * (this.v21 * A1323 - this.v22 * A0323 + this.v24 * A0123)
            - this.v14 * (this.v21 * A1223 - this.v22 * A0223 + this.v23 * A0123);
         if (det == 0.0) {
            det = Double.MIN_VALUE;
         }

         det = 1.0 / det;
         this.cachedDeterminant = det;
         this.set(
            det * (this.v22 * A2323 - this.v23 * A1323 + this.v24 * A1223),
            det * -(this.v12 * A2323 - this.v13 * A1323 + this.v14 * A1223),
            det * (this.v12 * A2313 - this.v13 * A1313 + this.v14 * A1213),
            det * -(this.v12 * A2312 - this.v13 * A1312 + this.v14 * A1212),
            det * -(this.v21 * A2323 - this.v23 * A0323 + this.v24 * A0223),
            det * (this.v11 * A2323 - this.v13 * A0323 + this.v14 * A0223),
            det * -(this.v11 * A2313 - this.v13 * A0313 + this.v14 * A0213),
            det * (this.v11 * A2312 - this.v13 * A0312 + this.v14 * A0212),
            det * (this.v21 * A1323 - this.v22 * A0323 + this.v24 * A0123),
            det * -(this.v11 * A1323 - this.v12 * A0323 + this.v14 * A0123),
            det * (this.v11 * A1313 - this.v12 * A0313 + this.v14 * A0113),
            det * -(this.v11 * A1312 - this.v12 * A0312 + this.v14 * A0112),
            det * -(this.v21 * A1223 - this.v22 * A0223 + this.v23 * A0123),
            det * (this.v11 * A1223 - this.v12 * A0223 + this.v13 * A0123),
            det * -(this.v11 * A1213 - this.v12 * A0213 + this.v13 * A0113),
            det * (this.v11 * A1212 - this.v12 * A0212 + this.v13 * A0112)
         );
         this.transpose();
      }

      this.cachedInverse = capture;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat4.class), value = "matrix_n.inverted")
   public FiguraMat4 inverted() {
      return (FiguraMat4)super.inverted();
   }

   @LuaWhitelist
   @LuaMethodDoc("matrix_n.det")
   @Override
   public double det() {
      return super.det();
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(returnType = FiguraMat4.class), value = "matrix_n.reset")
   public FiguraMat4 reset() {
      return (FiguraMat4)super.reset();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat4.class, argumentNames = "other", returnType = FiguraMat4.class),
      value = "matrix_n.add"
   )
   public FiguraMat4 add(@LuaNotNil FiguraMat4 o) {
      this.v11 = this.v11 + o.v11;
      this.v12 = this.v12 + o.v12;
      this.v13 = this.v13 + o.v13;
      this.v14 = this.v14 + o.v14;
      this.v21 = this.v21 + o.v21;
      this.v22 = this.v22 + o.v22;
      this.v23 = this.v23 + o.v23;
      this.v24 = this.v24 + o.v24;
      this.v31 = this.v31 + o.v31;
      this.v32 = this.v32 + o.v32;
      this.v33 = this.v33 + o.v33;
      this.v34 = this.v34 + o.v34;
      this.v41 = this.v41 + o.v41;
      this.v42 = this.v42 + o.v42;
      this.v43 = this.v43 + o.v43;
      this.v44 = this.v44 + o.v44;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = FiguraMat4.class, argumentNames = "other", returnType = FiguraMat4.class),
      value = "matrix_n.sub"
   )
   public FiguraMat4 sub(@LuaNotNil FiguraMat4 o) {
      this.v11 = this.v11 - o.v11;
      this.v12 = this.v12 - o.v12;
      this.v13 = this.v13 - o.v13;
      this.v14 = this.v14 - o.v14;
      this.v21 = this.v21 - o.v21;
      this.v22 = this.v22 - o.v22;
      this.v23 = this.v23 - o.v23;
      this.v24 = this.v24 - o.v24;
      this.v31 = this.v31 - o.v31;
      this.v32 = this.v32 - o.v32;
      this.v33 = this.v33 - o.v33;
      this.v34 = this.v34 - o.v34;
      this.v41 = this.v41 - o.v41;
      this.v42 = this.v42 - o.v42;
      this.v43 = this.v43 - o.v43;
      this.v44 = this.v44 - o.v44;
      this.invalidate();
      return this;
   }

   public FiguraMat4 scale(double x, double y, double z) {
      this.v11 *= x;
      this.v12 *= x;
      this.v13 *= x;
      this.v14 *= x;
      this.v21 *= y;
      this.v22 *= y;
      this.v23 *= y;
      this.v24 *= y;
      this.v31 *= z;
      this.v32 *= z;
      this.v33 *= z;
      this.v34 *= z;
      this.invalidate();
      return this;
   }

   public FiguraMat4 scale(FiguraVec3 vec) {
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
   public FiguraMat4 scale(Object x, Double y, Double z) {
      return this.scale(LuaUtils.parseOneArgVec("scale", x, y, z, 1.0));
   }

   public FiguraMat4 translate(double x, double y, double z) {
      this.v11 = this.v11 + x * this.v41;
      this.v12 = this.v12 + x * this.v42;
      this.v13 = this.v13 + x * this.v43;
      this.v14 = this.v14 + x * this.v44;
      this.v21 = this.v21 + y * this.v41;
      this.v22 = this.v22 + y * this.v42;
      this.v23 = this.v23 + y * this.v43;
      this.v24 = this.v24 + y * this.v44;
      this.v31 = this.v31 + z * this.v41;
      this.v32 = this.v32 + z * this.v42;
      this.v33 = this.v33 + z * this.v43;
      this.v34 = this.v34 + z * this.v44;
      this.invalidate();
      return this;
   }

   public FiguraMat4 translate(FiguraVec3 amount) {
      return this.translate(amount.x, amount.y, amount.z);
   }

   public FiguraMat4 translate(Vec3 amount) {
      return this.translate(amount.x, amount.y, amount.z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      value = "matrix_n.translate"
   )
   public FiguraMat4 translate(Object x, Double y, Double z) {
      return this.translate(LuaUtils.parseVec3("translate", x, y, z));
   }

   public FiguraMat4 translateFirst(double x, double y, double z) {
      this.v14 = this.v14 + (this.v11 * x + this.v12 * y + this.v13 * z);
      this.v24 = this.v24 + (this.v21 * x + this.v22 * y + this.v23 * z);
      this.v34 = this.v34 + (this.v31 * x + this.v32 * y + this.v33 * z);
      this.v44 = this.v44 + (this.v41 * x + this.v42 * y + this.v43 * z);
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "degrees"), value = "matrix_n.rotate_x")
   public FiguraMat4 rotateX(double degrees) {
      degrees = Math.toRadians(degrees);
      double c = Math.cos(degrees);
      double s = Math.sin(degrees);
      double nv21 = c * this.v21 - s * this.v31;
      double nv22 = c * this.v22 - s * this.v32;
      double nv23 = c * this.v23 - s * this.v33;
      double nv24 = c * this.v24 - s * this.v34;
      this.v31 = s * this.v21 + c * this.v31;
      this.v32 = s * this.v22 + c * this.v32;
      this.v33 = s * this.v23 + c * this.v33;
      this.v34 = s * this.v24 + c * this.v34;
      this.v21 = nv21;
      this.v22 = nv22;
      this.v23 = nv23;
      this.v24 = nv24;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "degrees"), value = "matrix_n.rotate_y")
   public FiguraMat4 rotateY(double degrees) {
      degrees = Math.toRadians(degrees);
      double c = Math.cos(degrees);
      double s = Math.sin(degrees);
      double nv11 = c * this.v11 + s * this.v31;
      double nv12 = c * this.v12 + s * this.v32;
      double nv13 = c * this.v13 + s * this.v33;
      double nv14 = c * this.v14 + s * this.v34;
      this.v31 = c * this.v31 - s * this.v11;
      this.v32 = c * this.v32 - s * this.v12;
      this.v33 = c * this.v33 - s * this.v13;
      this.v34 = c * this.v34 - s * this.v14;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.v14 = nv14;
      this.invalidate();
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "degrees"), value = "matrix_n.rotate_z")
   public FiguraMat4 rotateZ(double degrees) {
      degrees = Math.toRadians(degrees);
      double c = Math.cos(degrees);
      double s = Math.sin(degrees);
      double nv11 = c * this.v11 - s * this.v21;
      double nv12 = c * this.v12 - s * this.v22;
      double nv13 = c * this.v13 - s * this.v23;
      double nv14 = c * this.v14 - s * this.v24;
      this.v21 = c * this.v21 + s * this.v11;
      this.v22 = c * this.v22 + s * this.v12;
      this.v23 = c * this.v23 + s * this.v13;
      this.v24 = c * this.v24 + s * this.v14;
      this.v11 = nv11;
      this.v12 = nv12;
      this.v13 = nv13;
      this.v14 = nv14;
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
      double nv14 = ce * this.v14 + p1 * this.v24 + p2 * this.v34;
      double nv24 = cf * this.v14 + p3 * this.v24 + p4 * this.v34;
      this.v34 = -d * this.v14 + bc * this.v24 + ac * this.v34;
      this.v11 = nv11;
      this.v21 = nv21;
      this.v31 = nv31;
      this.v12 = nv12;
      this.v22 = nv22;
      this.v32 = nv32;
      this.v13 = nv13;
      this.v23 = nv23;
      this.v33 = nv33;
      this.v14 = nv14;
      this.v24 = nv24;
      this.invalidate();
   }

   public void rotateZYX(FiguraVec3 vec) {
      this.rotateZYX(vec.x, vec.y, vec.z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      value = "matrix_n.rotate"
   )
   public FiguraMat4 rotate(Object x, Double y, Double z) {
      this.rotateZYX(LuaUtils.parseVec3("rotate", x, y, z));
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("matrix_n.deaugmented")
   public FiguraMat3 deaugmented() {
      FiguraMat3 result = FiguraMat3.of();
      result.set(this.v11, this.v21, this.v31, this.v12, this.v22, this.v32, this.v13, this.v23, this.v33);
      return result;
   }

   private FiguraMat4 rawScale(double f) {
      this.v11 *= f;
      this.v12 *= f;
      this.v13 *= f;
      this.v14 *= f;
      this.v21 *= f;
      this.v22 *= f;
      this.v23 *= f;
      this.v24 *= f;
      this.v31 *= f;
      this.v32 *= f;
      this.v33 *= f;
      this.v34 *= f;
      this.v41 *= f;
      this.v42 *= f;
      this.v43 *= f;
      this.v44 *= f;
      this.invalidate();
      return this;
   }

   public FiguraVec3 apply(FiguraVec4 vec) {
      FiguraVec4 result = this.times(vec);
      return FiguraVec3.of(result.x, result.y, result.z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      value = "matrix_n.apply"
   )
   public FiguraVec3 apply(Object x, Double y, Double z) {
      FiguraVec3 vec = LuaUtils.parseVec3("apply", x, y, z);
      return this.apply(FiguraVec4.of(vec.x, vec.y, vec.z, 1.0));
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      value = "matrix_n.apply_dir"
   )
   public FiguraVec3 applyDir(Object x, Double y, Double z) {
      FiguraVec3 vec = LuaUtils.parseVec3("applyDir", x, y, z);
      return this.apply(FiguraVec4.of(vec.x, vec.y, vec.z, 0.0));
   }

   @LuaWhitelist
   public FiguraMat4 __add(@LuaNotNil FiguraMat4 mat) {
      return this.plus(mat);
   }

   @LuaWhitelist
   public FiguraMat4 __sub(@LuaNotNil FiguraMat4 mat) {
      return this.minus(mat);
   }

   @LuaWhitelist
   public Object __mul(@LuaNotNil Object o) {
      if (o instanceof FiguraMat4 mat) {
         return mat.times(this);
      } else if (o instanceof FiguraVec4 vec) {
         return this.times(vec);
      } else if (o instanceof Number n) {
         return this.copy().rawScale(n.doubleValue());
      } else {
         throw new LuaError("Invalid types to Matrix4 __mul: " + o.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   public boolean __eq(Object o) {
      return this.equals(o);
   }

   @LuaWhitelist
   public int __len() {
      return 4;
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
         case "4", "c4" -> this.getColumn(4);
         case "r1" -> this.getRow(1);
         case "r2" -> this.getRow(2);
         case "r3" -> this.getRow(3);
         case "r4" -> this.getRow(4);
         case "v11" -> this.v11;
         case "v12" -> this.v12;
         case "v13" -> this.v13;
         case "v14" -> this.v14;
         case "v21" -> this.v21;
         case "v22" -> this.v22;
         case "v23" -> this.v23;
         case "v24" -> this.v24;
         case "v31" -> this.v31;
         case "v32" -> this.v32;
         case "v33" -> this.v33;
         case "v34" -> this.v34;
         case "v41" -> this.v41;
         case "v42" -> this.v42;
         case "v43" -> this.v43;
         case "v44" -> this.v44;
         default -> null;
      };
   }

   @LuaWhitelist
   public void __newindex(@LuaNotNil String string, Object value) {
      if (value instanceof FiguraVec4 vec4) {
         switch (string) {
            case "1":
            case "c1":
               this.v11 = vec4.x;
               this.v21 = vec4.y;
               this.v31 = vec4.z;
               this.v41 = vec4.w;
               break;
            case "2":
            case "c2":
               this.v12 = vec4.x;
               this.v22 = vec4.y;
               this.v32 = vec4.z;
               this.v42 = vec4.w;
               break;
            case "3":
            case "c3":
               this.v13 = vec4.x;
               this.v23 = vec4.y;
               this.v33 = vec4.z;
               this.v43 = vec4.w;
               break;
            case "4":
            case "c4":
               this.v14 = vec4.x;
               this.v24 = vec4.y;
               this.v34 = vec4.z;
               this.v44 = vec4.w;
               break;
            case "r1":
               this.v11 = vec4.x;
               this.v12 = vec4.y;
               this.v13 = vec4.z;
               this.v14 = vec4.w;
               break;
            case "r2":
               this.v21 = vec4.x;
               this.v22 = vec4.y;
               this.v23 = vec4.z;
               this.v24 = vec4.w;
               break;
            case "r3":
               this.v31 = vec4.x;
               this.v32 = vec4.y;
               this.v33 = vec4.z;
               this.v34 = vec4.w;
               break;
            case "r4":
               this.v41 = vec4.x;
               this.v42 = vec4.y;
               this.v43 = vec4.z;
               this.v44 = vec4.w;
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
            case "v14":
               this.v14 = num.doubleValue();
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
            case "v24":
               this.v24 = num.doubleValue();
               break;
            case "v31":
               this.v31 = num.doubleValue();
               break;
            case "v32":
               this.v32 = num.doubleValue();
               break;
            case "v33":
               this.v33 = num.doubleValue();
               break;
            case "v34":
               this.v34 = num.doubleValue();
               break;
            case "v41":
               this.v41 = num.doubleValue();
               break;
            case "v42":
               this.v42 = num.doubleValue();
               break;
            case "v43":
               this.v43 = num.doubleValue();
               break;
            case "v44":
               this.v44 = num.doubleValue();
         }
      } else {
         throw new LuaError("Illegal arguments to Matrix4 __newindex: " + string + ", " + value);
      }
   }
}
