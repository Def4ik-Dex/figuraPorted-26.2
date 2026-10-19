package org.figuramc.figura.lua.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaMethodDoc {
   LuaMethodOverload[] overloads() default {@LuaMethodOverload};

   String[] aliases() default {};

   String value();
}
