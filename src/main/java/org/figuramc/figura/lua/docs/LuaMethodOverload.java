package org.figuramc.figura.lua.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LuaMethodOverload {
   Class<?>[] argumentTypes() default {};

   String[] argumentNames() default {};

   Class<?> returnType() default LuaMethodOverload.DEFAULT.class;

   final class DEFAULT {
   }
}
