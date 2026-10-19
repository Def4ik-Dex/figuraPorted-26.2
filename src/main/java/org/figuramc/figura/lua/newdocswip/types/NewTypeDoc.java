package org.figuramc.figura.lua.newdocswip.types;

import java.util.ArrayList;
import java.util.List;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

public class NewTypeDoc extends NewFiguraDoc<LuaTypeDoc> {
   private final Class<?> documentedClass;
   private final List<NewMethodDoc> methods = new ArrayList<>();
   private final List<NewMetamethodDoc> metamethods = new ArrayList<>();
   private final List<NewFieldDoc> fields = new ArrayList<>();

   private NewTypeDoc(Class<?> clazz) {
      this.documentedClass = clazz;
   }

   public static NewTypeDoc createForClass(Class<?> clazz) {
      return new NewTypeDoc(clazz);
   }
}
