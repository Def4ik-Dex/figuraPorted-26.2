package org.figuramc.figura.utils;

public class RefilledNumber {
   private double max;
   private double add;
   private double current;

   public RefilledNumber() {
      this(0.0);
   }

   public RefilledNumber(double max) {
      this(max, max);
   }

   public RefilledNumber(double max, double initial) {
      this.set(max);
      this.current = initial;
   }

   public void tick() {
      this.current = Math.min(this.current + this.add, Math.ceil(this.max));
   }

   public boolean use() {
      boolean check = this.check();
      if (check) {
         this.current--;
      }

      return check;
   }

   public boolean check() {
      return this.current >= 1.0;
   }

   public int peek() {
      return (int)Math.floor(this.current);
   }

   public void set(double max) {
      this.max = max;
      this.add = max / 20.0;
      this.current = Math.min(this.current, Math.ceil(max));
   }
}
