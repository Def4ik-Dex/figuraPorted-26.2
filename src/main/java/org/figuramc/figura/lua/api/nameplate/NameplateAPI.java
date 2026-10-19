package org.figuramc.figura.lua.api.nameplate;

import java.util.Locale;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(name = "NameplateAPI", value = "nameplate")
public class NameplateAPI {
   @LuaWhitelist
   @LuaFieldDoc("nameplate.chat")
   public final NameplateCustomization CHAT = new NameplateCustomization();
   @LuaWhitelist
   @LuaFieldDoc("nameplate.entity")
   public final EntityNameplateCustomization ENTITY = new EntityNameplateCustomization();
   @LuaWhitelist
   @LuaFieldDoc("nameplate.list")
   public final NameplateCustomization LIST = new NameplateCustomization();
   @LuaWhitelist
   @LuaFieldDoc("nameplate.all")
   public final NameplateCustomizationGroup ALL = new NameplateCustomizationGroup(this.CHAT, this.ENTITY, this.LIST);

   @LuaWhitelist
   public Object __index(String arg) {
      if (arg == null) {
         return null;
      }

      return switch (arg.toUpperCase(Locale.US)) {
         case "CHAT" -> this.CHAT;
         case "ENTITY" -> this.ENTITY;
         case "LIST" -> this.LIST;
         case "ALL" -> this.ALL;
         default -> null;
      };
   }

   @Override
   public String toString() {
      return "NameplateAPI";
   }
}
