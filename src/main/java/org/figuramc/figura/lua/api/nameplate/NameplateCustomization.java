package org.figuramc.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "NameplateCustomization", value = "nameplate_customization")
public class NameplateCustomization {
   private Component json;
   private String text;

   private Component parseJsonText(String text) {
      Component component = TextUtils.tryParseJson(text);
      component = Badges.noBadges4U(component);
      component = TextUtils.removeClickableObjects(component);
      Component var5 = Emojis.applyEmojis(component);
      return Emojis.removeBlacklistedEmojis(var5);
   }

   public Component getJson() {
      return this.json;
   }

   @LuaWhitelist
   @LuaMethodDoc("nameplate_customization.get_text")
   public String getText() {
      return this.text;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "text"), value = "nameplate_customization.set_text")
   public NameplateCustomization setText(String text) {
      this.text = text;
      if (text != null) {
         Component component = this.parseJsonText(text);
         if (component.getString().length() > 64) {
            throw new LuaError("Text length exceeded limit of 64 characters");
         }

         this.json = component;
      } else {
         this.json = null;
      }

      return this;
   }

   @Override
   public String toString() {
      return "NameplateCustomization";
   }
}
