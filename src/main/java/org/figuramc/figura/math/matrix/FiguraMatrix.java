package org.figuramc.figura.math.matrix;

import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.math.vector.FiguraVector;

public abstract class FiguraMatrix<T extends FiguraMatrix<T, V>, V extends FiguraVector<V, T>> {
   protected T cachedInverse = (T)null;
   protected double cachedDeterminant = Double.NaN;

   protected FiguraMatrix() {
   }

   protected abstract double calculateDeterminant();

   protected abstract void resetIdentity();

   public abstract T copy();

   public abstract boolean equals(T var1);

   public abstract V getColumn(int var1);

   public abstract V getRow(int var1);

   public abstract int rows();

   public abstract int cols();

   public abstract T set(T var1);

   public abstract T multiply(T var1);

   public abstract T rightMultiply(T var1);

   public abstract T transpose();

   public abstract T invert();

   public abstract T add(T var1);

   public abstract T sub(T var1);

   public T transposed() {
      return this.copy().transpose();
   }

   public T inverted() {
      return this.copy().invert();
   }

   public T times(T o) {
      return this.copy().multiply(o);
   }

   public V times(V vec) {
      return (V)vec.copy().transform((T) this);
   }

   public T plus(T o) {
      return this.copy().add(o);
   }

   public T minus(T o) {
      return this.copy().sub(o);
   }

   public T reset() {
      this.cachedDeterminant = Double.NaN;
      this.cachedInverse = null;
      this.resetIdentity();
      return (T)this;
   }

   public double det() {
      if (!Double.isNaN(this.cachedDeterminant)) {
         return this.cachedDeterminant;
      }

      this.cachedDeterminant = this.calculateDeterminant();
      if (this.cachedDeterminant == 0.0) {
         this.cachedDeterminant = Double.MIN_VALUE;
      }

      return this.cachedDeterminant;
   }

   protected final void invalidate() {
      this.cachedInverse = null;
      this.cachedDeterminant = Double.NaN;
   }

   protected String getString(Double... d) {
      StringBuilder sb = new StringBuilder();
      sb.append("\n[");
      int mod = this.cols();

      for (int i = 0; i < d.length; i++) {
         if (i % mod == 0) {
            sb.append("\n\t");
         }

         sb.append(FiguraLuaPrinter.df.format(d[i]));
         if (i < d.length - 1) {
            sb.append(", ");
         }
      }

      return sb.append("\n]").toString();
   }

   public abstract static class DummyMatrix<V extends FiguraVector<V, FiguraMatrix.DummyMatrix<V>>> extends FiguraMatrix<FiguraMatrix.DummyMatrix<V>, V> {
   }
}
