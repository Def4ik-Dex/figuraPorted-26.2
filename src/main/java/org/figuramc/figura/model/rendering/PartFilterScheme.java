package org.figuramc.figura.model.rendering;

import org.figuramc.figura.model.ParentType;

public enum PartFilterScheme {
   MODEL(true, PartFilterScheme.SchemeFunction.cancelOnSeparate(), ParentType.None),
   HEAD(false, PartFilterScheme.SchemeFunction.onlyThis(ParentType.Head), ParentType.Head),
   LEFT_ARM(false, PartFilterScheme.SchemeFunction.onlyThis(ParentType.LeftArm), ParentType.LeftArm),
   RIGHT_ARM(false, PartFilterScheme.SchemeFunction.onlyThis(ParentType.RightArm), ParentType.RightArm),
   CAPE(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Cape), ParentType.Cape),
   LEFT_ELYTRA(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.LeftElytra), ParentType.LeftElytra),
   RIGHT_ELYTRA(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.RightElytra), ParentType.RightElytra),
   WORLD(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.World), ParentType.World),
   HUD(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Hud), ParentType.Hud),
   SKULL(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Skull), ParentType.Skull),
   PORTRAIT(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Portrait), ParentType.Portrait),
   ARROW(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Arrow), ParentType.Arrow),
   TRIDENT(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Trident), ParentType.Trident),
   ITEM(false, PartFilterScheme.SchemeFunction.onlyThisSeparate(ParentType.Item), ParentType.Item),
   PIVOTS(false, PartFilterScheme.SchemeFunction.onlyPivotsAndCancelOnSeparate(), ParentType.HelmetItemPivot);

   public final boolean initialValue;
   public final PartFilterScheme.SchemeFunction predicate;
   public final ParentType parentType;

   PartFilterScheme(boolean initialValue, PartFilterScheme.SchemeFunction predicate, ParentType parentType) {
      this.initialValue = initialValue;
      this.predicate = predicate;
      this.parentType = parentType;
   }

   public Boolean test(ParentType toTest, boolean prevResult) {
      return this.predicate.test(toTest, prevResult);
   }

   @FunctionalInterface
   private interface SchemeFunction {
      Boolean test(ParentType var1, boolean var2);

      static PartFilterScheme.SchemeFunction onlyThisSeparate(ParentType typeToAllow) {
         return (parent, prev) -> {
            if (parent == typeToAllow) {
               return !prev;
            } else {
               return parent.isSeparate ? false : prev;
            }
         };
      }

      static PartFilterScheme.SchemeFunction cancelOnSeparate() {
         return (parent, prev) -> parent.isSeparate ? null : true;
      }

      static PartFilterScheme.SchemeFunction onlyThis(ParentType typeToAllow) {
         return (parent, prev) -> {
            if (parent == typeToAllow) {
               return true;
            } else {
               return parent == ParentType.None ? prev : null;
            }
         };
      }

      static PartFilterScheme.SchemeFunction onlyPivotsAndCancelOnSeparate() {
         return (parent, prev) -> {
            if (parent.isPivot) {
               return true;
            } else {
               return parent.isSeparate ? null : false;
            }
         };
      }
   }
}
