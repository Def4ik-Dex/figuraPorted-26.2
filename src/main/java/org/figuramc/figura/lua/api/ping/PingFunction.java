package org.figuramc.figura.lua.api.ping;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaWhitelist
@LuaTypeDoc(name = "PingFunction", value = "ping_function")
public class PingFunction extends LuaFunction {
   private final int id;
   private final Avatar owner;
   private final boolean isHost;
   public final LuaFunction func;

   public PingFunction(int id, Avatar owner, LuaFunction func) {
      this.id = id;
      this.owner = owner;
      this.isHost = owner.isHost;
      this.func = func;
   }

   public LuaValue call() {
      this.invoke(NONE);
      return NIL;
   }

   public LuaValue call(LuaValue arg) {
      this.invoke(arg);
      return NIL;
   }

   public LuaValue call(LuaValue arg1, LuaValue arg2) {
      this.invoke(arg1, arg2);
      return NIL;
   }

   public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
      this.invoke(arg1, arg2, arg3);
      return NIL;
   }

   public Varargs invoke(Varargs args) {
      if (!this.isHost) {
         return NIL;
      }

      boolean sync = Configs.SYNC_PINGS.value;
      byte[] data = new PingArg(args).toByteArray();
      NetworkStuff.sendPing(this.id, sync, data);
      if (!sync) {
         this.owner.runPing(this.id, data);
      }

      return NIL;
   }

   public String toString() {
      return "PingFunction";
   }
}
