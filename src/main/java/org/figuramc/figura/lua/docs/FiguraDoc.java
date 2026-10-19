package org.figuramc.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

public abstract class FiguraDoc {
   public static final MutableComponent HEADER = Component.empty()
      .withStyle(ColorUtils.Colors.AWESOME_BLUE.style)
      .append(Component.literal("\n•*+•* ").append(FiguraText.of()).append(" Docs *•+*•").withStyle(ChatFormatting.UNDERLINE));
   public final String name;
   public final String description;

   public FiguraDoc(String name, String description) {
      this.name = name;
      this.description = description;
   }

   public abstract int print();

   public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
      LiteralArgumentBuilder<FiguraClientCommandSource> command = LiteralArgumentBuilder.literal(this.name);
      command.executes(context -> this.print());
      return command;
   }

   public JsonObject toJson(boolean translate) {
      JsonObject json = new JsonObject();
      json.addProperty("name", this.name);
      json.addProperty(
         "description",
         translate ? Language.getInstance().getOrDefault(FiguraText.of("docs." + this.description).getString()) : "figura.docs." + this.description
      );
      return json;
   }

   public static int printRoot() {
      FiguraMod.sendChatMessage(HEADER.copy().append("\n\n").append(FiguraText.of("docs").withStyle(ColorUtils.Colors.FIGURA_BLUE.style)));
      return 1;
   }

   public static class ClassDoc extends FiguraDoc {
      public final ArrayList<FiguraDoc.MethodDoc> documentedMethods;
      public final ArrayList<FiguraDoc.FieldDoc> documentedFields;
      public final Class<?> thisClass;
      public final Class<?> superclass;

      public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc) {
         this(clazz, typeDoc, null);
      }

      public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc, Map<String, List<FiguraDoc>> children) {
         super(typeDoc.name(), typeDoc.value());
         this.thisClass = clazz;
         if (clazz.getSuperclass().isAnnotationPresent(LuaTypeDoc.class)) {
            this.superclass = clazz.getSuperclass();
         } else {
            this.superclass = null;
         }

         this.documentedMethods = new ArrayList<>();
         Set<String> foundIndices = new HashSet<>();

         for (Method method : clazz.getDeclaredMethods()) {
            this.parseMethodIfNeeded(foundIndices, children, typeDoc, method);
         }

         for (Method method : clazz.getMethods()) {
            this.parseMethodIfNeeded(foundIndices, children, typeDoc, method);
         }

         this.documentedFields = new ArrayList<>();

         for (Field field : clazz.getDeclaredFields()) {
            this.parseFieldIfNeeded(children, foundIndices, field);
         }

         for (Field field : clazz.getFields()) {
            this.parseFieldIfNeeded(children, foundIndices, field);
         }
      }

      private void parseMethodIfNeeded(Set<String> foundIndices, Map<String, List<FiguraDoc>> children, LuaTypeDoc typeDoc, Method method) {
         String name = method.getName();
         if (!foundIndices.contains(name) && method.isAnnotationPresent(LuaMethodDoc.class)) {
            foundIndices.add(name);
            LuaMethodDoc doc = method.getAnnotation(LuaMethodDoc.class);
            List<FiguraDoc> childList = children == null ? null : children.get(name);
            this.documentedMethods.add(new FiguraDoc.MethodDoc(method, doc, childList, typeDoc.name()));
         }
      }

      private void parseFieldIfNeeded(Map<String, List<FiguraDoc>> children, Set<String> foundIndices, Field field) {
         String name = field.getName();
         if (!foundIndices.contains(name) && field.isAnnotationPresent(LuaFieldDoc.class)) {
            foundIndices.add(name);
            List<FiguraDoc> childList = children == null ? null : children.get(name);
            this.documentedFields.add(new FiguraDoc.FieldDoc(field, field.getAnnotation(LuaFieldDoc.class), childList));
         }
      }

      @Override
      public int print() {
         MutableComponent message = HEADER.copy()
            .append("\n\n")
            .append(Component.literal("• ").append(FiguraText.of("docs.text.type")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));
         message.append("\n\t").append(Component.literal("• " + this.name).withStyle(ColorUtils.Colors.FIGURA_BLUE.style));
         if (this.superclass != null) {
            message.append(" (")
               .append(FiguraText.of("docs.text.extends"))
               .append(" ")
               .append(FiguraDocsManager.getClassText(this.superclass).withStyle(ChatFormatting.YELLOW))
               .append(")");
         }

         message.append("\n\n")
            .append(Component.literal("• ").append(FiguraText.of("docs.text.description")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));
         MutableComponent descText = Component.empty().withStyle(ColorUtils.Colors.FIGURA_BLUE.style);

         for (Component component : TextUtils.splitText(FiguraText.of("docs." + this.description), "\n")) {
            descText.append("\n\t").append("• ").append(component);
         }

         message.append(descText);
         FiguraMod.sendChatMessage(message);
         return 1;
      }

      @Override
      public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
         LiteralArgumentBuilder<FiguraClientCommandSource> command = super.getCommand();

         for (FiguraDoc.MethodDoc methodDoc : this.documentedMethods) {
            command.then(methodDoc.getCommand());
         }

         for (FiguraDoc.FieldDoc fieldDoc : this.documentedFields) {
            command.then(fieldDoc.getCommand());
         }

         return command;
      }

      @Override
      public JsonObject toJson(boolean translate) {
         JsonObject json = super.toJson(translate);
         if (this.superclass != null) {
            json.addProperty("parent", FiguraDocsManager.getNameFor(this.superclass));
         }

         JsonArray methods = new JsonArray();

         for (FiguraDoc.MethodDoc methodDoc : this.documentedMethods) {
            methods.add(methodDoc.toJson(translate));
         }

         json.add("methods", methods);
         JsonArray fields = new JsonArray();

         for (FiguraDoc.FieldDoc fieldDoc : this.documentedFields) {
            fields.add(fieldDoc.toJson(translate));
         }

         json.add("fields", fields);
         return json;
      }
   }

   public static class FieldDoc extends FiguraDoc {
      public final Class<?> type;
      public final boolean editable;
      public final List<FiguraDoc> children;

      public FieldDoc(Field field, LuaFieldDoc luaFieldDoc, List<FiguraDoc> children) {
         super(field.getName(), luaFieldDoc.value());
         this.type = field.getType();
         this.editable = !Modifier.isFinal(field.getModifiers());
         this.children = children;
      }

      @Override
      public int print() {
         MutableComponent message = HEADER.copy()
            .append("\n\n")
            .append(Component.literal("• ").append(FiguraText.of("docs.text.field")).append(":").withStyle(ColorUtils.Colors.PURPLE.style))
            .append("\n\t")
            .append(Component.literal("• ").withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
            .append(FiguraDocsManager.getClassText(this.type).withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" " + this.name).withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
            .append(
               Component.literal(" (")
                  .append(FiguraText.of(this.editable ? "docs.text.editable" : "docs.text.not_editable"))
                  .append(")")
                  .withStyle(this.editable ? ChatFormatting.GREEN : ChatFormatting.DARK_RED)
            );
         message.append("\n\n")
            .append(Component.literal("• ").append(FiguraText.of("docs.text.description")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));
         MutableComponent descText = Component.empty().withStyle(ColorUtils.Colors.FIGURA_BLUE.style);

         for (Component component : TextUtils.splitText(FiguraText.of("docs." + this.description), "\n")) {
            descText.append("\n\t").append("• ").append(component);
         }

         message.append(descText);
         FiguraMod.sendChatMessage(message);
         return 1;
      }

      @Override
      public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
         LiteralArgumentBuilder<FiguraClientCommandSource> command = super.getCommand();
         if (this.children != null) {
            for (FiguraDoc child : this.children) {
               command.then(child.getCommand());
            }
         }

         return command;
      }

      @Override
      public JsonObject toJson(boolean translate) {
         JsonObject json = super.toJson(translate);
         json.addProperty("type", FiguraDocsManager.getNameFor(this.type));
         json.addProperty("editable", this.editable);
         JsonArray children = new JsonArray();
         if (this.children != null) {
            for (FiguraDoc child : this.children) {
               children.add(child.toJson(translate));
            }
         }

         json.add("children", children);
         return json;
      }
   }

   public static class MethodDoc extends FiguraDoc {
      public final Class<?>[][] parameterTypes;
      public final String[][] parameterNames;
      public final Class<?>[] returnTypes;
      public final String typeName;
      public final String[] aliases;
      public final boolean isStatic;
      public final List<FiguraDoc> children;

      public MethodDoc(Method method, LuaMethodDoc methodDoc, List<FiguraDoc> children, String typeName) {
         super(method.getName(), methodDoc.value());
         LuaMethodOverload[] overloads = methodDoc.overloads();
         this.parameterTypes = new Class[overloads.length][];
         this.parameterNames = new String[overloads.length][];
         this.returnTypes = new Class[overloads.length];
         this.isStatic = Modifier.isStatic(method.getModifiers());
         this.aliases = methodDoc.aliases();
         this.typeName = typeName;
         this.children = children;

         for (int i = 0; i < overloads.length; i++) {
            this.parameterTypes[i] = overloads[i].argumentTypes();
            this.parameterNames[i] = overloads[i].argumentNames();
            if (overloads[i].returnType() == LuaMethodOverload.DEFAULT.class) {
               this.returnTypes[i] = method.getReturnType();
            } else {
               this.returnTypes[i] = overloads[i].returnType();
            }
         }
      }

      @Override
      public int print() {
         MutableComponent message = HEADER.copy();
         message.append("\n\n")
            .append(Component.literal("• ").append(FiguraText.of("docs.text.function")).append(":").withStyle(ColorUtils.Colors.PURPLE.style))
            .append("\n\t")
            .append(Component.literal("• " + this.name).withStyle(ColorUtils.Colors.FIGURA_BLUE.style));
         if (this.aliases.length > 0) {
            message.append("\n\n")
               .append(Component.literal("• ").append(FiguraText.of("docs.text.aliases")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));

            for (String alias : this.aliases) {
               message.append("\n\t").append(Component.literal("• ").append(alias).withStyle(ColorUtils.Colors.FIGURA_BLUE.style));
            }
         }

         message.append("\n\n").append(Component.literal("• ").append(FiguraText.of("docs.text.syntax")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));

         for (int i = 0; i < this.parameterTypes.length; i++) {
            message.append("\n\t")
               .append(Component.literal("• ").withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
               .append(Component.literal("<" + this.typeName + ">").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(this.isStatic ? "." : ":").withStyle(ChatFormatting.BOLD))
               .append(Component.literal(this.name).withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
               .append("(");

            for (int j = 0; j < this.parameterTypes[i].length; j++) {
               message.append(FiguraDocsManager.getClassText(this.parameterTypes[i][j]).withStyle(ChatFormatting.YELLOW))
                  .append(" ")
                  .append(Component.literal(this.parameterNames[i][j]).withStyle(ChatFormatting.WHITE));
               if (j != this.parameterTypes[i].length - 1) {
                  message.append(", ");
               }
            }

            message.append(") → ")
               .append(FiguraText.of("docs.text.returns").append(" ").withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
               .append(FiguraDocsManager.getClassText(this.returnTypes[i]).withStyle(ChatFormatting.YELLOW));
         }

         message.append("\n\n")
            .append(Component.literal("• ").append(FiguraText.of("docs.text.description")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));
         MutableComponent descText = Component.empty().withStyle(ColorUtils.Colors.FIGURA_BLUE.style);

         for (Component component : TextUtils.splitText(FiguraText.of("docs." + this.description), "\n")) {
            descText.append("\n\t").append("• ").append(component);
         }

         message.append(descText);
         FiguraMod.sendChatMessage(message);
         return 1;
      }

      @Override
      public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
         LiteralArgumentBuilder<FiguraClientCommandSource> command = super.getCommand();
         if (this.children != null) {
            for (FiguraDoc child : this.children) {
               command.then(child.getCommand());
            }
         }

         return command;
      }

      @Override
      public JsonObject toJson(boolean translate) {
         JsonObject json = super.toJson(translate);
         JsonArray params = new JsonArray();

         for (int i = 0; i < this.parameterNames.length; i++) {
            JsonArray param = new JsonArray();

            for (int j = 0; j < this.parameterNames[i].length; j++) {
               JsonObject paramObj = new JsonObject();
               paramObj.addProperty("name", this.parameterNames[i][j]);
               paramObj.addProperty("type", FiguraDocsManager.getNameFor(this.parameterTypes[i][j]));
               param.add(paramObj);
            }

            params.add(param);
         }

         json.add("parameters", params);
         JsonArray returns = new JsonArray();

         for (Class<?> returnType : this.returnTypes) {
            returns.add(FiguraDocsManager.getNameFor(returnType));
         }

         json.add("returns", returns);
         JsonArray aliases = new JsonArray();

         for (String alias : this.aliases) {
            aliases.add(alias);
         }

         json.add("aliases", aliases);
         JsonArray children = new JsonArray();
         if (this.children != null) {
            for (FiguraDoc child : this.children) {
               children.add(child.toJson(translate));
            }
         }

         json.add("children", children);
         json.addProperty("static", this.isStatic);
         return json;
      }
   }
}
