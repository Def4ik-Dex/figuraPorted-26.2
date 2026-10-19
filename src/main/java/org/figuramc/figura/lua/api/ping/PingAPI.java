package org.figuramc.figura.lua.api.ping;

import java.util.HashMap;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaFunction;

@LuaWhitelist
@LuaTypeDoc(name = "PingAPI", value = "pings")
public class PingAPI {
   private final HashMap<String, PingFunction> map = new HashMap<>();
   private final HashMap<Integer, String> idMap = new HashMap<>();
   private final Avatar owner;

   public PingAPI(Avatar owner) {
      this.owner = owner;
   }

   public PingFunction get(String arg) {
      return this.map.get(arg);
   }

   public String getName(int id) {
      return this.idMap.get(id);
   }

   @LuaWhitelist
   public Object __index(String arg) {
      return this.get(arg);
   }

   @LuaWhitelist
   public void __newindex(@LuaNotNil String key, LuaFunction value) {
      int id = (key.hashCode() + 1) * 31;
      if (value == null) {
         this.map.remove(key);
         this.idMap.remove(id);
      } else {
         PingFunction func = new PingFunction(id, this.owner, value);
         this.map.put(key, func);
         this.idMap.put(id, key);
      }
   }

   @Override
   public String toString() {
      return "PingAPI";
   }
}
