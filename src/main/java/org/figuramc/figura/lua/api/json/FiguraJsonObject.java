package org.figuramc.figura.lua.api.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaWhitelist
@LuaTypeDoc(name = "JsonObject", value = "json_object")
public class FiguraJsonObject implements FiguraJsonSerializer.JsonValue {
   private final HashMap<String, LuaValue> contents = new HashMap<>();

   @LuaWhitelist
   @LuaMethodDoc(value = "json_object.size", overloads = @LuaMethodOverload(returnType = Integer.class))
   public int size() {
      return this.contents.size();
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "json_object.get", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key", returnType = LuaValue.class))
   public LuaValue get(@LuaNotNil String key) {
      return this.contents.get(key);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "json_object.contains_key",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key", returnType = Boolean.class)
   )
   public boolean containsKey(@LuaNotNil String key) {
      return this.contents.containsKey(key);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "json_object.put",
      overloads = @LuaMethodOverload(argumentTypes = {String.class, LuaValue.class}, argumentNames = {"key", "value"}, returnType = LuaValue.class)
   )
   public LuaValue put(@LuaNotNil String key, LuaValue value) {
      if (!JsonAPI.isSerializable(value)) {
         throw new IllegalArgumentException("Type %s can't be serialized".formatted(value));
      } else {
         return this.contents.put(key, value);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "json_object.remove",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key", returnType = LuaValue.class)
   )
   public LuaValue remove(@LuaNotNil String key) {
      return this.contents.remove(key);
   }

   @LuaWhitelist
   @LuaMethodDoc("json_object.clear")
   public void clear() {
      this.contents.clear();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "json_object.contains_value",
      overloads = @LuaMethodOverload(argumentTypes = LuaValue.class, argumentNames = "value", returnType = boolean.class)
   )
   public boolean containsValue(LuaValue value) {
      return this.contents.containsValue(value);
   }

   @Override
   public JsonElement getElement() {
      JsonObject obj = new JsonObject();

      for (Entry<String, LuaValue> val : this.contents.entrySet()) {
         obj.add(val.getKey(), LuaUtils.asJsonValue(val.getValue()));
      }

      return obj;
   }

   @LuaWhitelist
   public LuaValue __index(LuaValue k) {
      if (k.type() != 4) {
         return null;
      }

      String key = k.checkjstring();
      return this.contents.get(key);
   }

   @LuaWhitelist
   public void __newindex(LuaValue k, LuaValue v) {
      if (k.type() == 4) {
         String key = k.checkjstring();
         this.put(key, v);
      }
   }

   @LuaWhitelist
   public Object[] __pairs() {
      return new Object[]{new FiguraJsonObject.JsonObjectIterator(this), this, LuaValue.NIL};
   }

   @LuaWhitelist
   public Object[] __ipairs() {
      return new Object[]{new FiguraJsonObject.EmptyIterator(), this, 0};
   }

   @Override
   public String toString() {
      return "JsonObject";
   }

   private static class EmptyIterator extends LuaFunction {
      public Varargs invoke(Varargs args) {
         return LuaValue.NIL;
      }
   }

   private static class JsonObjectIterator extends LuaFunction {
      private final ArrayList<String> keys = new ArrayList<>();

      public JsonObjectIterator(FiguraJsonObject obj) {
         this.keys.addAll(obj.contents.keySet());
      }

      public Varargs invoke(Varargs args) {
         if (this.keys.size() == 0) {
            return LuaValue.NIL;
         } else if (!(args.checkuserdata(1) instanceof FiguraJsonObject obj)) {
            return LuaValue.NIL;
         } else {
            LuaValue a = args.arg(2);
            String k;
            if (!a.isnil() && a.type() == 4) {
               String oldKey = a.checkjstring();
               int ind = this.keys.indexOf(oldKey);
               if (ind >= this.keys.size() - 1) {
                  return LuaValue.NIL;
               }

               k = this.keys.get(ind + 1);
            } else {
               k = this.keys.get(0);
            }

            LuaValue v = obj.get(k);
            return LuaValue.varargsOf(LuaValue.valueOf(k), v);
         }
      }
   }
}
