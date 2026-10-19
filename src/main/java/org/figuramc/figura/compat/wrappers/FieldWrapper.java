package org.figuramc.figura.compat.wrappers;

import java.lang.reflect.Field;
import org.jetbrains.annotations.Nullable;

public class FieldWrapper {
   @Nullable
   private final Field field;
   private boolean isErrored;

   public FieldWrapper(Class<?> clazz, String fieldName) {
      Field temp = null;

      try {
         temp = clazz.getDeclaredField(fieldName);
         temp.setAccessible(true);
      } catch (Exception var5) {
      }

      this.field = temp;
   }

   public void markErrored() {
      this.isErrored = true;
   }

   public boolean exists() {
      return this.field != null && !this.isErrored;
   }

   @Nullable
   public Object getValue(Object holder) {
      if (!this.exists()) {
         return null;
      }

      try {
         return this.field.get(holder);
      } catch (Exception e) {
         this.isErrored = true;
         return null;
      }
   }
}
