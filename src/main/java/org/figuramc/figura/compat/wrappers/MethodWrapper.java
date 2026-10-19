package org.figuramc.figura.compat.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

public class MethodWrapper {
   @Nullable
   private final Method method;
   private boolean isErrored;

   public MethodWrapper(Class<?> clazz, String methodName, Class<?>... args) {
      Method temp = null;

      try {
         temp = clazz.getDeclaredMethod(methodName, args);
         temp.setAccessible(true);
      } catch (Exception var6) {
      }

      this.method = temp;
   }

   public void markErrored() {
      this.isErrored = true;
   }

   public boolean exists() {
      return this.method != null && !this.isErrored;
   }

   @Nullable
   public Object invoke(Object caller, Object... args) {
      if (!this.exists()) {
         return null;
      }

      try {
         return this.method.invoke(caller, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
         this.isErrored = true;
         return null;
      }
   }
}
