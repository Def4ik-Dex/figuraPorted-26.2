package org.figuramc.figura.compat.wrappers;

public class ClassWrapper {
   private final Class<?> clazz;
   public final boolean isLoaded;

   public ClassWrapper(String classpath) {
      boolean exists = false;

      Class<?> temp;
      try {
         temp = Class.forName(classpath);
         exists = true;
      } catch (Exception e) {
         temp = null;
      }

      this.clazz = temp;
      this.isLoaded = exists;
   }

   public MethodWrapper getMethod(String name, Class<?>... args) {
      return new MethodWrapper(this.clazz, name, args);
   }

   public FieldWrapper getField(String name) {
      return new FieldWrapper(this.clazz, name);
   }
}
