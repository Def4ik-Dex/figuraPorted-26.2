package org.figuramc.figura.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.function.Function;
import org.figuramc.figura.FiguraMod;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class JsonUtils {
   public static boolean validate(JsonObject object, String fieldName, Function<JsonElement, Boolean> validator) {
      return object.has(fieldName) && validator.apply(object.get(fieldName));
   }

   public static boolean validate(JsonObject object, String fieldName, Function<JsonElement, Boolean> validator, String warningMessage, Object... params) {
      if (!validate(object, fieldName, validator)) {
         FiguraMod.LOGGER.warn(warningMessage, params);
         return false;
      } else {
         return true;
      }
   }

   public static int getIntOrDefault(JsonObject object, String fieldName, int fallback) {
      return validate(object, fieldName, JsonElement::isJsonPrimitive) ? object.get(fieldName).getAsInt() : fallback;
   }

   public static LuaValue asLuaValue(JsonElement value) {
      if (value.isJsonPrimitive()) {
         JsonPrimitive p = value.getAsJsonPrimitive();
         if (p.isBoolean()) {
            return LuaValue.valueOf(p.getAsBoolean());
         } else if (p.isNumber()) {
            return LuaValue.valueOf(p.getAsNumber().doubleValue());
         } else {
            return p.isString() ? LuaValue.valueOf(p.getAsString()) : LuaValue.valueOf(value.getAsString());
         }
      } else if (value.isJsonArray()) {
         JsonArray arr = value.getAsJsonArray();
         LuaTable table = new LuaTable();
         int i = 1;

         for (JsonElement element : arr) {
            table.insert(i, asLuaValue(element));
            i++;
         }

         return table;
      } else {
         if (value.isJsonNull()) {
            return LuaValue.NIL;
         }

         if (!value.isJsonObject()) {
            return LuaValue.NIL;
         }

         JsonObject obj = value.getAsJsonObject();
         LuaTable table = new LuaTable();

         for (String key : obj.keySet()) {
            table.set(key, asLuaValue(obj.get(key)));
         }

         return table;
      }
   }
}
