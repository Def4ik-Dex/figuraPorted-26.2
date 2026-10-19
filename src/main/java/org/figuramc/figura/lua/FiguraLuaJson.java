package org.figuramc.figura.lua;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.Objects;
import java.util.function.Function;
import org.figuramc.figura.utils.JsonUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class FiguraLuaJson {
   private static final Function<FiguraLuaRuntime, LuaValue> PARSE_JSON_FUNCTION = runtime -> new VarArgFunction() {
      public Varargs invoke(Varargs args) {
         return FiguraLuaJson.jsonStringToTable(args.checkjstring(1));
      }

      public String tojstring() {
         return "function: parseJson";
      }
   };
   private static final Function<FiguraLuaRuntime, LuaValue> TO_JSON_FUNCTION = runtime -> new VarArgFunction() {
      public Varargs invoke(Varargs args) {
         return (Varargs)(args.narg() == 0 ? LuaValue.NIL : LuaValue.valueOf(FiguraLuaJson.tableToJsonString(args.arg(1))));
      }

      public String tojstring() {
         return "function: toJson";
      }
   };

   public static void loadFunctions(FiguraLuaRuntime runtime) {
      LuaValue print = PARSE_JSON_FUNCTION.apply(runtime);
      runtime.setGlobal("parseJson", print);
      LuaValue printJson = TO_JSON_FUNCTION.apply(runtime);
      runtime.setGlobal("toJson", printJson);
   }

   public static LuaValue jsonStringToTable(String json) {
      JsonElement jsonElement;
      try {
         jsonElement = JsonParser.parseString(json);
      } catch (JsonSyntaxException e) {
         throw new LuaError("Failed to parse malformed Json: " + e.getCause());
      }

      return JsonUtils.asLuaValue(jsonElement);
   }

   public static String tableToJsonString(LuaValue value) {
      return Objects.requireNonNullElse(LuaUtils.asJsonValue(value), "null").toString();
   }
}
