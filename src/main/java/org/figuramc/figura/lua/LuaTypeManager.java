package org.figuramc.figura.lua;

import com.google.gson.JsonElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class LuaTypeManager {
   private final Map<Class<?>, LuaTable> metatables = new HashMap<>();
   private final Map<Class<?>, String> namesCache = new HashMap<>();

   public void generateMetatableFor(final Class<?> clazz) {
      if (!this.metatables.containsKey(clazz)) {
         if (!clazz.isAnnotationPresent(LuaWhitelist.class)) {
            throw new IllegalArgumentException("Tried to generate metatable for un-whitelisted class " + clazz.getName() + "!");
         }

         try {
            this.generateMetatableFor(clazz.getSuperclass());
         } catch (IllegalArgumentException var10) {
         }

         LuaTable metatable = new LuaTable();
         final LuaTable indexTable = new LuaTable();

         for (Class<?> currentClass = clazz; currentClass.isAnnotationPresent(LuaWhitelist.class); currentClass = currentClass.getSuperclass()) {
            for (final Method method : currentClass.getDeclaredMethods()) {
               if (method.isAnnotationPresent(LuaWhitelist.class)) {
                  String name = method.getName();
                  if (name.startsWith("__")) {
                     if (metatable.rawget(name) == LuaValue.NIL) {
                        if (name.equals("__index")) {
                           metatable.set("__index", new TwoArgFunction() {
                              final LuaFunction wrappedIndexer = LuaTypeManager.this.getWrapper(method);

                              public LuaValue call(LuaValue arg1, LuaValue arg2) {
                                 LuaValue result = indexTable.get(arg2);
                                 if (result == LuaValue.NIL) {
                                    result = this.wrappedIndexer.call(arg1, arg2);
                                 }

                                 return result;
                              }
                           });
                        } else {
                           metatable.set(name, this.getWrapper(method));
                        }
                     }
                  } else {
                     indexTable.set(name, this.getWrapper(method));
                  }
               }
            }
         }

         if (metatable.rawget("__index") == LuaValue.NIL) {
            metatable.set("__index", indexTable);
         }

         if (metatable.rawget("__tostring") == LuaValue.NIL) {
            metatable.set("__tostring", new OneArgFunction() {
               private final LuaString val = LuaString.valueOf(clazz.getName());

               public LuaValue call(LuaValue arg) {
                  return this.val;
               }
            });
         }

         if (indexTable.rawget("__index") == LuaValue.NIL) {
            LuaTable superclassMetatable = this.metatables.get(clazz.getSuperclass());
            if (superclassMetatable != null) {
               LuaTable newMetatable = new LuaTable();
               newMetatable.set("__index", superclassMetatable.get("__index"));
               indexTable.setmetatable(newMetatable);
            }
         }

         this.metatables.put(clazz, metatable);
      }
   }

   public void dumpMetatables(LuaTable table) {
      for (Entry<Class<?>, LuaTable> entry : this.metatables.entrySet()) {
         if (entry.getKey().isAnnotationPresent(LuaTypeDoc.class)) {
            String name = entry.getKey().getAnnotation(LuaTypeDoc.class).name();
            if (table.get(name) != LuaValue.NIL) {
               throw new IllegalStateException("Two classes have the same type name: " + name);
            }

            table.set(name, (LuaValue)entry.getValue());
         }
      }
   }

   public String getTypeName(Class<?> clazz) {
      return this.namesCache
         .computeIfAbsent(
            clazz, someClass -> someClass.isAnnotationPresent(LuaTypeDoc.class) ? someClass.getAnnotation(LuaTypeDoc.class).name() : someClass.getSimpleName()
         );
   }

   private static boolean[] getRequiredNotNil(Method method) {
      Parameter[] params = method.getParameters();
      boolean[] result = new boolean[params.length];

      for (int i = 0; i < params.length; i++) {
         if (params[i].isAnnotationPresent(LuaNotNil.class)) {
            result[i] = true;
         }
      }

      return result;
   }

   public VarArgFunction getWrapper(final Method method) {
      return new VarArgFunction() {
         private final boolean isStatic = Modifier.isStatic(method.getModifiers());
         private Object caller;
         private final Class<?> clazz = method.getDeclaringClass();
         private final Class<?>[] argumentTypes = method.getParameterTypes();
         private final Object[] actualArgs = new Object[this.argumentTypes.length];
         private final boolean[] requiredNotNil = LuaTypeManager.getRequiredNotNil(method);

         public Varargs invoke(Varargs args) {
            if (!this.isStatic) {
               this.caller = args.checkuserdata(1, this.clazz);
            }

            int offset = this.isStatic
                  && this.argumentTypes.length > 0
                  && !this.argumentTypes[0].isAssignableFrom(this.clazz)
                  && args.isuserdata(1)
                  && this.clazz.isAssignableFrom(args.checkuserdata(1).getClass())
               ? 1
               : 0;

            for (int i = 0; i < this.argumentTypes.length; i++) {
               int argIndex = i + (this.isStatic ? 1 : 2) + offset;
               boolean nil = args.isnil(argIndex);
               if (nil && this.requiredNotNil[i]) {
                  throw new LuaError(
                     "bad argument: "
                        + method.getName()
                        + " "
                        + argIndex
                        + " do not allow nil values, expected "
                        + FiguraDocsManager.getNameFor(this.argumentTypes[i])
                  );
               }

               if (argIndex <= args.narg() && !nil) {
                  try {
                     Object[] var18 = this.actualArgs;

                     var18[i] = switch (this.argumentTypes[i].getName()) {
                        case "java.lang.Number", "java.lang.Double", "double" -> args.checkdouble(argIndex);
                        case "java.lang.String" -> args.checkjstring(argIndex);
                        case "java.lang.Boolean", "boolean" -> args.toboolean(argIndex);
                        case "java.lang.Float", "float" -> (float)args.checkdouble(argIndex);
                        case "java.lang.Integer", "int" -> args.checkint(argIndex);
                        case "java.lang.Long", "long" -> args.checklong(argIndex);
                        case "org.luaj.vm2.LuaTable" -> args.checktable(argIndex);
                        case "org.luaj.vm2.LuaFunction" -> args.checkfunction(argIndex);
                        case "org.luaj.vm2.LuaValue" -> args.arg(argIndex);
                        case "java.lang.Object" -> LuaTypeManager.this.luaToJava(args.arg(argIndex));
                        default -> this.argumentTypes[i].getName().startsWith("[")
                           ? LuaTypeManager.this.luaVarargToJava(args, argIndex, this.argumentTypes[i])
                           : args.checkuserdata(argIndex, this.argumentTypes[i]);
                     };
                  } catch (LuaError err) {
                     String expectedType = FiguraDocsManager.getNameFor(this.argumentTypes[i]);
                     String actualType;
                     if (args.arg(argIndex).type() == 7) {
                        actualType = FiguraDocsManager.getNameFor(args.arg(argIndex).checkuserdata().getClass());
                     } else {
                        actualType = args.arg(argIndex).typename();
                     }

                     throw new LuaError(
                        "Invalid argument " + argIndex + " to function " + method.getName() + ". Expected " + expectedType + ", but got " + actualType
                     );
                  }
               } else {
                  Object[] var10000 = this.actualArgs;

                  var10000[i] = switch (this.argumentTypes[i].getName()) {
                     case "double" -> 0.0;
                     case "int" -> 0;
                     case "long" -> 0L;
                     case "float" -> 0.0F;
                     case "boolean" -> false;
                     default -> null;
                  };
               }
            }

            Object result;
            try {
               result = method.invoke(this.caller, this.actualArgs);
            } catch (IllegalAccessException | InvocationTargetException e) {
               throw e.getCause() instanceof LuaError l ? l : new LuaError(e.getCause());
            }

            return result instanceof Varargs v ? v : LuaTypeManager.this.javaToLua(result);
         }

         public String tojstring() {
            return "function: " + method.getName();
         }
      };
   }

   private LuaValue wrap(Object instance) {
      Class<?> clazz = instance.getClass();

      LuaTable metatable;
      for (metatable = this.metatables.get(clazz); metatable == null; metatable = this.metatables.get(clazz)) {
         clazz = clazz.getSuperclass();
         if (clazz == Object.class) {
            throw new RuntimeException(
               "Attempt to wrap illegal type " + instance.getClass().getName() + " (not registered in LuaTypeManager's \"metatables\" map)!"
            );
         }
      }

      LuaUserdata result = new LuaUserdata(instance);
      result.setmetatable(metatable);
      return result;
   }

   private LuaValue wrapMap(Map<?, ?> map) {
      LuaTable table = new LuaTable();

      for (Entry<?, ?> entry : map.entrySet()) {
         LuaValue key = this.javaToLua(entry.getKey()).arg1();
         LuaValue val = this.javaToLua(entry.getValue()).arg1();
         table.set(key, val);
      }

      return table;
   }

   private LuaValue wrapCollection(Collection<?> collection) {
      LuaTable table = new LuaTable();
      int i = 1;

      for (Object o : collection) {
         table.set(i++, this.javaToLua(o).arg1());
      }

      return table;
   }

   private Varargs wrapArray(Object array) {
      int len = Array.getLength(array);
      LuaValue[] args = new LuaValue[len];

      for (int i = 0; i < len; i++) {
         args[i] = this.javaToLua(Array.get(array, i)).arg1();
      }

      return LuaValue.varargsOf(args);
   }

   public Object luaVarargToJava(Varargs args, int argIndex, Class<?> argumentType) {
      if (args.arg(argIndex).istable()) {
         return this.luaVarargToJava(args.checktable(argIndex).unpack(), 1, argumentType);
      }

      Object[] obj = new Object[args.narg() - argIndex + 1];
      int start = argIndex;

      while (argIndex <= args.narg()) {
         int var10001 = argIndex - start;

         obj[var10001] = switch (argumentType.getName()) {
            case "[Ljava.lang.Number;", "[Ljava.lang.Double;", "[D" -> args.checkdouble(argIndex);
            case "[Ljava.lang.String;" -> args.checkjstring(argIndex);
            case "[Ljava.lang.Boolean;", "[B" -> args.toboolean(argIndex);
            case "[Ljava.lang.Float;", "[F" -> (float)args.checkdouble(argIndex);
            case "[Ljava.lang.Integer;", "[I" -> args.checkint(argIndex);
            case "[Ljava.lang.Long;", "[J" -> args.checklong(argIndex);
            case "[Lorg.luaj.vm2.LuaTable;" -> args.checktable(argIndex);
            case "[Lorg.luaj.vm2.LuaFunction;" -> args.checkfunction(argIndex);
            case "[Lorg.luaj.vm2.LuaValue;" -> args.arg(argIndex);
            case "[Ljava.lang.Object;" -> this.luaToJava(args.arg(argIndex));
            default -> args.checkuserdata(argIndex, argumentType);
         };
         argIndex++;
      }

      Object array = java.lang.reflect.Array.newInstance(argumentType.getComponentType(), obj.length);
      System.arraycopy(obj, 0, array, 0, obj.length);
      return array;
   }

   public Object luaToJava(LuaValue val) {
      if (val.istable()) {
         return val.checktable();
      }

      if (val.isnumber()) {
         if (val instanceof LuaInteger i) {
            return i.checkint();
         } else {
            return val.isint() && val instanceof LuaString s ? s.checkint() : val.checkdouble();
         }
      } else if (val.isstring()) {
         return val.checkjstring();
      } else if (val.isboolean()) {
         return val.checkboolean();
      } else if (val.isfunction()) {
         return val.checkfunction();
      } else {
         return val.isuserdata() ? val.checkuserdata(Object.class) : null;
      }
   }

   public Varargs javaToLua(Object val) {
      if (val == null) {
         return LuaValue.NIL;
      } else if (val instanceof LuaValue l) {
         return l;
      } else if (val instanceof Double d) {
         return LuaValue.valueOf(d);
      } else if (val instanceof String s) {
         return LuaValue.valueOf(s);
      } else if (val instanceof Boolean b) {
         return LuaValue.valueOf(b);
      } else if (val instanceof Integer i) {
         return LuaValue.valueOf(i);
      } else if (val instanceof Float f) {
         return LuaValue.valueOf(f.floatValue());
      } else if (val instanceof Byte b) {
         return LuaValue.valueOf(b);
      } else if (val instanceof Long l) {
         return LuaValue.valueOf(l.longValue());
      } else if (val instanceof Character c) {
         return LuaValue.valueOf(c);
      } else if (val instanceof Short s) {
         return LuaValue.valueOf(s);
      } else if (val instanceof Map<?, ?> map) {
         return this.wrapMap(map);
      } else if (val instanceof Collection<?> collection) {
         return this.wrapCollection(collection);
      } else if (val.getClass().isArray()) {
         return this.wrapArray(val);
      } else if (val instanceof Component c) {
         TextUtils.allowScriptEvents = true;
         LuaValue ret = LuaValue.valueOf(((JsonElement)ComponentSerialization.CODEC.encodeStart(TextUtils.OPS, c).getOrThrow()).toString());
         TextUtils.allowScriptEvents = false;
         return ret;
      } else {
         return this.wrap(val);
      }
   }
}
