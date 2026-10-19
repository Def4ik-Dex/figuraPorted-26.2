package org.figuramc.figura.lua.api.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

@LuaWhitelist
@LuaTypeDoc(name = "ResourcesAPI", value = "resources")
public class ResourcesAPI {
   private final Avatar parent;

   public ResourcesAPI(Avatar parent) {
      this.parent = parent;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "resources.get",
      overloads = @LuaMethodOverload(returnType = FiguraInputStream.class, argumentTypes = String.class, argumentNames = "path")
   )
   public FiguraInputStream get(@LuaNotNil String path) {
      try {
         if (this.parent.resources.containsKey(path)) {
            ByteArrayInputStream bais = new ByteArrayInputStream(this.parent.resources.get(path));
            return new FiguraInputStream(this.parent, new GZIPInputStream(bais));
         } else {
            return null;
         }
      } catch (IOException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "resources.get_paths", overloads = @LuaMethodOverload(returnType = LuaTable.class))
   public List<String> getPaths() {
      return new ArrayList<>(this.parent.resources.keySet());
   }

   @LuaWhitelist
   public FiguraInputStream __index(@LuaNotNil String path) {
      return this.get(path);
   }

   @Override
   public String toString() {
      return "ResourcesAPI";
   }
}
