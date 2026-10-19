package org.figuramc.figura.math.vector;

import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.math.matrix.FiguraMatrix;

public abstract class FiguraVector<T extends FiguraVector<T, M>, M extends FiguraMatrix<M, T>> {
   public abstract double lengthSquared();

   public double length() {
      return Math.sqrt(this.lengthSquared());
   }

   public abstract T copy();

   public abstract double dot(T var1);

   public abstract T set(T var1);

   public abstract T add(T var1);

   public abstract T subtract(T var1);

   public abstract T offset(double var1);

   public abstract T multiply(T var1);

   public abstract T transform(M var1);

   public abstract T divide(T var1);

   public abstract T reduce(T var1);

   public abstract T scale(double var1);

   public abstract double[] unpack();

   public T normalize() {
      double len = this.length();
      if (len > 0.0) {
         this.scale(1.0 / len);
      }

      return (T)this;
   }

   public T clampLength(Double minLength, Double maxLength) {
      if (minLength == null) {
         minLength = 0.0;
      }

      if (maxLength == null) {
         maxLength = Double.POSITIVE_INFINITY;
      }

      double len = this.length();
      if (len == 0.0) {
         return (T)this;
      }

      if (len < minLength) {
         this.scale(minLength / len);
      } else if (len > maxLength) {
         this.scale(maxLength / len);
      }

      return (T)this;
   }

   public T clamped(Double minLength, Double maxLength) {
      return this.copy().clampLength(minLength, maxLength);
   }

   public T plus(T other) {
      return this.copy().add(other);
   }

   public T minus(T other) {
      return this.copy().subtract(other);
   }

   public T offseted(double factor) {
      return this.copy().offset(factor);
   }

   public T times(T other) {
      return this.copy().multiply(other);
   }

   public T dividedBy(T other) {
      return this.copy().divide(other);
   }

   public T mod(T other) {
      return this.copy().reduce(other);
   }

   public T scaled(double factor) {
      return this.copy().scale(factor);
   }

   public T normalized() {
      return this.copy().normalize();
   }

   public T toRad() {
      return this.copy().scale(Math.PI / 180.0);
   }

   public T toDeg() {
      return this.copy().scale(180.0 / Math.PI);
   }

   public abstract int size();

   public abstract double index(int var1);

   @Override
   public abstract boolean equals(Object var1);

   public double x() {
      return 0.0;
   }

   public double y() {
      return 0.0;
   }

   public double z() {
      return 0.0;
   }

   public double w() {
      return 0.0;
   }

   @Override
   public abstract String toString();

   protected static String getString(Double... d) {
      StringBuilder sb = new StringBuilder();
      sb.append("{");

      for (int i = 0; i < d.length; i++) {
         sb.append(FiguraLuaPrinter.df.format(d[i]));
         if (i < d.length - 1) {
            sb.append(", ");
         }
      }

      return sb.append("}").toString();
   }
}
