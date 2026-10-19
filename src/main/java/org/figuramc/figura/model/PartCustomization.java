package org.figuramc.figura.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Stack;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.texture.FiguraRenderTypes;

public class PartCustomization {
   public PartCustomization.PartType partType = PartCustomization.PartType.GROUP;
   public final FiguraMat4 positionMatrix = FiguraMat4.of();
   public final FiguraMat3 uvMatrix = FiguraMat3.of();
   public final FiguraMat3 normalMatrix = FiguraMat3.of();
   public boolean needsMatrixRecalculation = false;
   public boolean visible = true;
   public Boolean vanillaVisible = null;
   private final FiguraVec3 position = FiguraVec3.of();
   private final FiguraVec3 rotation = FiguraVec3.of();
   private final FiguraVec3 scale = FiguraVec3.of(1.0, 1.0, 1.0);
   private final FiguraVec3 pivot = FiguraVec3.of();
   private final FiguraVec3 offsetPivot = FiguraVec3.of();
   private final FiguraVec3 offsetPos = FiguraVec3.of();
   private final FiguraVec3 offsetRot = FiguraVec3.of();
   private final FiguraVec3 offsetScale = FiguraVec3.of(1.0, 1.0, 1.0);
   private final FiguraVec3 animPos = FiguraVec3.of();
   private final FiguraVec3 animRot = FiguraVec3.of();
   private final FiguraVec3 animScale = FiguraVec3.of(1.0, 1.0, 1.0);
   public final FiguraVec3 stackScale = FiguraVec3.of(1.0, 1.0, 1.0);
   public final FiguraVec3 color = FiguraVec3.of(1.0, 1.0, 1.0);
   public final FiguraVec3 color2 = FiguraVec3.of(1.0, 1.0, 1.0);
   public Float alpha = null;
   public Integer light = null;
   public Integer overlay = null;
   private FiguraRenderTypes primaryRenderType;
   private FiguraRenderTypes secondaryRenderType;
   public TextureCustomization primaryTexture;
   public TextureCustomization secondaryTexture;
   public static final PoseStack GLOBAL_CUSTOMIZATION_POSE_STACK = new PoseStack();

   public void applyToStack(PoseStack stack) {
      stack.mulPose(this.positionMatrix.toMatrix4f());
      stack.last().normal().mul(this.normalMatrix.toMatrix3f());
   }

   public void recalculate() {
      if (this.needsMatrixRecalculation) {
         this.positionMatrix.reset();
         this.positionMatrix
            .translate(
               this.offsetPos.x - this.pivot.x - this.offsetPivot.x,
               this.offsetPos.y - this.pivot.y - this.offsetPivot.y,
               this.offsetPos.z - this.pivot.z - this.offsetPivot.z
            );
         this.stackScale
            .set(
               this.offsetScale.x * this.scale.x * this.animScale.x,
               this.offsetScale.y * this.scale.y * this.animScale.y,
               this.offsetScale.z * this.scale.z * this.animScale.z
            );
         this.positionMatrix.scale(this.stackScale);
         if (this.partType == PartCustomization.PartType.MESH) {
            this.positionMatrix.rotateZ(this.rotation.z + this.offsetRot.z + this.animRot.z);
            this.positionMatrix.rotateY(this.rotation.y + this.offsetRot.y + this.animRot.y);
            this.positionMatrix.rotateX(this.rotation.x + this.offsetRot.x + this.animRot.x);
         } else {
            this.positionMatrix
               .rotateZYX(
                  this.rotation.x + this.offsetRot.x + this.animRot.x,
                  this.rotation.y + this.offsetRot.y + this.animRot.y,
                  this.rotation.z + this.offsetRot.z + this.animRot.z
               );
         }

         this.positionMatrix
            .translate(
               this.position.x + this.animPos.x + this.pivot.x + this.offsetPivot.x,
               this.position.y + this.animPos.y + this.pivot.y + this.offsetPivot.y,
               this.position.z + this.animPos.z + this.pivot.z + this.offsetPivot.z
            );
         this.normalMatrix.reset();
         double x = this.scale.x * this.animScale.x;
         double y = this.scale.y * this.animScale.y;
         double z = this.scale.z * this.animScale.z;
         double c = Math.cbrt(x * y * z);
         this.normalMatrix.scale(c == 0.0 && x == 0.0 ? 1.0 : c / x, c == 0.0 && y == 0.0 ? 1.0 : c / y, c == 0.0 && z == 0.0 ? 1.0 : c / z);
         if (this.partType == PartCustomization.PartType.MESH) {
            this.normalMatrix.rotateZ(this.rotation.z + this.offsetRot.z + this.animRot.z);
            this.normalMatrix.rotateY(this.rotation.y + this.offsetRot.y + this.animRot.y);
            this.normalMatrix.rotateX(this.rotation.x + this.offsetRot.x + this.animRot.x);
         } else {
            this.normalMatrix
               .rotateZYX(
                  this.rotation.x + this.offsetRot.x + this.animRot.x,
                  this.rotation.y + this.offsetRot.y + this.animRot.y,
                  this.rotation.z + this.offsetRot.z + this.animRot.z
               );
         }

         this.needsMatrixRecalculation = false;
      }
   }

   public void setPos(FiguraVec3 pos) {
      this.setPos(pos.x, pos.y, pos.z);
   }

   public void setPos(double x, double y, double z) {
      this.position.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getPos() {
      return this.position.copy();
   }

   public void setRot(FiguraVec3 rot) {
      this.setRot(rot.x, rot.y, rot.z);
   }

   public void setRot(double x, double y, double z) {
      this.rotation.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getRot() {
      return this.rotation.copy();
   }

   public void setScale(FiguraVec3 scale) {
      this.setScale(scale.x, scale.y, scale.z);
   }

   public void setScale(double x, double y, double z) {
      this.scale.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getScale() {
      return this.scale.copy();
   }

   public void setPivot(FiguraVec3 pivot) {
      this.setPivot(pivot.x, pivot.y, pivot.z);
   }

   public void setPivot(double x, double y, double z) {
      this.pivot.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getPivot() {
      return this.pivot.copy();
   }

   public void offsetPivot(FiguraVec3 pivot) {
      this.offsetPivot(pivot.x, pivot.y, pivot.z);
   }

   public void offsetPivot(double x, double y, double z) {
      this.offsetPivot.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public void addOffsetPivot(FiguraVec3 pivot) {
      this.addOffsetPivot(pivot.x, pivot.y, pivot.z);
   }

   public void addOffsetPivot(double x, double y, double z) {
      this.offsetPivot.add(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getOffsetPivot() {
      return this.offsetPivot.copy();
   }

   public void offsetPos(FiguraVec3 pos) {
      this.offsetPos(pos.x, pos.y, pos.z);
   }

   public void offsetPos(double x, double y, double z) {
      this.offsetPos.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public void addOffsetPos(FiguraVec3 pos) {
      this.addOffsetPos(pos.x, pos.y, pos.z);
   }

   public void addOffsetPos(double x, double y, double z) {
      this.offsetPos.add(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getOffsetPos() {
      return this.offsetPos.copy();
   }

   public void offsetRot(FiguraVec3 rot) {
      this.offsetRot(rot.x, rot.y, rot.z);
   }

   public void offsetRot(double x, double y, double z) {
      this.offsetRot.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public void addOffsetRot(FiguraVec3 rot) {
      this.addOffsetRot(rot.x, rot.y, rot.z);
   }

   public void addOffsetRot(double x, double y, double z) {
      this.offsetRot.add(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getOffsetRot() {
      return this.offsetRot.copy();
   }

   public void offsetScale(FiguraVec3 scale) {
      this.offsetScale(scale.x, scale.y, scale.z);
   }

   public void offsetScale(double x, double y, double z) {
      this.offsetScale.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public void addOffsetScale(FiguraVec3 scale) {
      this.addOffsetScale(scale.x, scale.y, scale.z);
   }

   public void addOffsetScale(double x, double y, double z) {
      this.offsetScale.add(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getOffsetScale() {
      return this.offsetScale.copy();
   }

   public void setAnimPos(double x, double y, double z) {
      this.animPos.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getAnimPos() {
      return this.animPos.copy();
   }

   public void setAnimRot(double x, double y, double z) {
      this.animRot.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getAnimRot() {
      return this.animRot.copy();
   }

   public void setAnimScale(double x, double y, double z) {
      this.animScale.set(x, y, z);
      this.needsMatrixRecalculation = true;
   }

   public FiguraVec3 getAnimScale() {
      return this.animScale.copy();
   }

   public void setMatrix(FiguraMat4 matrix) {
      this.positionMatrix.set(matrix);
      FiguraMat3 temp = matrix.deaugmented();
      temp.invert();
      temp.transpose();
      this.normalMatrix.set(temp);
      this.needsMatrixRecalculation = false;
   }

   public void setPositionMatrix(FiguraMat4 matrix) {
      this.positionMatrix.set(matrix);
   }

   public void setNormalMatrix(FiguraMat3 matrix) {
      this.normalMatrix.set(matrix);
   }

   public FiguraMat4 getPositionMatrix() {
      FiguraMat4 result = FiguraMat4.of();
      result.set(this.positionMatrix);
      return result;
   }

   public FiguraMat3 getNormalMatrix() {
      FiguraMat3 result = new FiguraMat3();
      result.set(this.normalMatrix);
      return result;
   }

   public void setPrimaryRenderType(FiguraRenderTypes type) {
      this.primaryRenderType = type;
   }

   public FiguraRenderTypes getPrimaryRenderType() {
      return this.primaryRenderType;
   }

   public void setSecondaryRenderType(FiguraRenderTypes type) {
      this.secondaryRenderType = type;
   }

   public FiguraRenderTypes getSecondaryRenderType() {
      return this.secondaryRenderType;
   }

   public void copyTo(PartCustomization target) {
      target.partType = this.partType;
      target.positionMatrix.set(this.positionMatrix);
      target.uvMatrix.set(this.uvMatrix);
      target.normalMatrix.set(this.normalMatrix);
      target.setPos(this.position);
      target.setRot(this.rotation);
      target.setScale(this.scale);
      target.setPivot(this.pivot);
      target.offsetPivot(this.offsetPivot);
      target.offsetPos(this.offsetPos);
      target.offsetRot(this.offsetRot);
      target.offsetScale(this.offsetScale);
      target.stackScale.set(this.stackScale);
      target.color.set(this.color);
      target.color2.set(this.color2);
      target.alpha = this.alpha;
      target.light = this.light;
      target.overlay = this.overlay;
      target.needsMatrixRecalculation = this.needsMatrixRecalculation;
      target.visible = this.visible;
      target.vanillaVisible = this.vanillaVisible;
      target.setPrimaryRenderType(this.primaryRenderType);
      target.setSecondaryRenderType(this.secondaryRenderType);
      target.primaryTexture = this.primaryTexture;
      target.secondaryTexture = this.secondaryTexture;
   }

   public void modify(PartCustomization other) {
      this.positionMatrix.rightMultiply(other.positionMatrix);
      this.uvMatrix.rightMultiply(other.uvMatrix);
      this.normalMatrix.rightMultiply(other.normalMatrix);
      if (other.primaryRenderType != null) {
         this.setPrimaryRenderType(other.primaryRenderType);
      }

      if (other.secondaryRenderType != null) {
         this.setSecondaryRenderType(other.secondaryRenderType);
      }

      this.visible = other.visible;
      if (other.vanillaVisible != null) {
         this.vanillaVisible = other.vanillaVisible;
      }

      if (other.light != null) {
         this.light = other.light;
      }

      if (other.overlay != null) {
         this.overlay = other.overlay;
      }

      if (other.alpha != null) {
         if (this.alpha != null) {
            this.alpha = this.alpha * other.alpha;
         } else {
            this.alpha = other.alpha;
         }
      }

      this.stackScale.multiply(other.stackScale);
      this.color.multiply(other.color);
      this.color2.multiply(other.color2);
      if (other.primaryTexture != null) {
         this.primaryTexture = other.primaryTexture;
      }

      if (other.secondaryTexture != null) {
         this.secondaryTexture = other.secondaryTexture;
      }

      this.needsMatrixRecalculation = false;
   }

   public PoseStack copyIntoGlobalPoseStack() {
      this.recalculate();
      this.positionMatrix.copyDataTo(GLOBAL_CUSTOMIZATION_POSE_STACK.last().pose());
      this.normalMatrix.copyDataTo(GLOBAL_CUSTOMIZATION_POSE_STACK.last().normal());
      return GLOBAL_CUSTOMIZATION_POSE_STACK;
   }

   public static class PartCustomizationStack {
      private final Stack<PartCustomization> stack = new Stack<PartCustomization>() {
         {
            this.add(new PartCustomization());
         }
      };

      public void push(PartCustomization customization) {
         PartCustomization newCustomization = new PartCustomization();
         this.stack.peek().copyTo(newCustomization);
         newCustomization.modify(customization);
         this.stack.push(newCustomization);
      }

      public void pop() {
         this.stack.pop();
      }

      public PartCustomization peek() {
         return this.stack.peek();
      }

      public boolean isEmpty() {
         return this.stack.size() == 1;
      }
   }

   public enum PartType {
      MESH,
      CUBE,
      GROUP;
   }
}
