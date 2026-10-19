package org.figuramc.figura.lua.api.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.Map.Entry;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "JsonSerializer", value = "json_serializer")
public class FiguraJsonSerializer {
   private final Gson serializer;

   public FiguraJsonSerializer(Gson serializer) {
      this.serializer = serializer;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "json_serializer.serialize",
      overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = LuaValue.class, returnType = String.class)
   )
   public String serialize(LuaValue val) {
      if (JsonAPI.isSerializable(val)) {
         JsonElement elem = LuaUtils.asJsonValue(val);
         return this.serializer.toJson(elem);
      } else {
         throw new IllegalArgumentException("Type of provided value (%s) is not serializable".formatted(val.typename()));
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "json_serializer.deserialize",
      overloads = @LuaMethodOverload(argumentNames = "str", argumentTypes = String.class, returnType = LuaValue.class)
   )
   public LuaValue deserialize(String s) {
      JsonElement e = JsonParser.parseString(s);
      return luaValueFromElement(e);
   }

   public static LuaValue luaValueFromElement(JsonElement element) {
      if (element.isJsonNull()) {
         return LuaValue.NIL;
      } else if (element.isJsonPrimitive()) {
         return luaValueFromPrimitive(element.getAsJsonPrimitive());
      } else {
         return element.isJsonObject() ? luaTableFromObject(element.getAsJsonObject()) : luaTableFromArray(element.getAsJsonArray());
      }
   }

   private static LuaTable luaTableFromArray(JsonArray array) {
      LuaTable tbl = new LuaTable();
      int i = 1;

      for (JsonElement elem : array) {
         tbl.set(i, luaValueFromElement(elem));
         i++;
      }

      return tbl;
   }

   private static LuaTable luaTableFromObject(JsonObject obj) {
      LuaTable tbl = new LuaTable();

      for (Entry<String, JsonElement> kv : obj.entrySet()) {
         tbl.set(kv.getKey(), luaValueFromElement(kv.getValue()));
      }

      return tbl;
   }

   private static LuaValue luaValueFromPrimitive(JsonPrimitive primitive) {
      if (primitive.isBoolean()) {
         return LuaValue.valueOf(primitive.getAsBoolean());
      } else {
         return (LuaValue)(primitive.isString() ? LuaValue.valueOf(primitive.getAsString()) : LuaValue.valueOf(primitive.getAsDouble()));
      }
   }

   @Override
   public String toString() {
      return "JsonSerializer";
   }

   public interface JsonValue {
      JsonElement getElement();
   }
}
