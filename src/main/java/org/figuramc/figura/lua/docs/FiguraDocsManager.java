package org.figuramc.figura.lua.docs;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.OpenFile;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.entries.FiguraAPI;
import org.figuramc.figura.lua.api.AnimationAPI;
import org.figuramc.figura.lua.api.AvatarAPI;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.lua.api.ConfigAPI;
import org.figuramc.figura.lua.api.FileAPI;
import org.figuramc.figura.lua.api.HostAPI;
import org.figuramc.figura.lua.api.RaycastAPI;
import org.figuramc.figura.lua.api.RendererAPI;
import org.figuramc.figura.lua.api.TextureAPI;
import org.figuramc.figura.lua.api.TextureAtlasAPI;
import org.figuramc.figura.lua.api.action_wheel.Action;
import org.figuramc.figura.lua.api.action_wheel.ActionWheelAPI;
import org.figuramc.figura.lua.api.action_wheel.Page;
import org.figuramc.figura.lua.api.data.DataAPI;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.figuramc.figura.lua.api.data.FiguraFuture;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.api.data.FiguraOutputStream;
import org.figuramc.figura.lua.api.data.ResourcesAPI;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.entity.LivingEntityAPI;
import org.figuramc.figura.lua.api.entity.PlayerAPI;
import org.figuramc.figura.lua.api.entity.ViewerAPI;
import org.figuramc.figura.lua.api.event.EventsAPI;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.figuramc.figura.lua.api.json.FiguraJsonArray;
import org.figuramc.figura.lua.api.json.FiguraJsonBuilder;
import org.figuramc.figura.lua.api.json.FiguraJsonObject;
import org.figuramc.figura.lua.api.json.FiguraJsonSerializer;
import org.figuramc.figura.lua.api.json.JsonAPI;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.lua.api.keybind.KeybindAPI;
import org.figuramc.figura.lua.api.math.MatricesAPI;
import org.figuramc.figura.lua.api.math.VectorsAPI;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.lua.api.nameplate.NameplateAPI;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomizationGroup;
import org.figuramc.figura.lua.api.net.HttpRequestsAPI;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.lua.api.particle.LuaParticle;
import org.figuramc.figura.lua.api.particle.ParticleAPI;
import org.figuramc.figura.lua.api.ping.PingAPI;
import org.figuramc.figura.lua.api.ping.PingFunction;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaGroupPart;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelPart;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.lua.api.world.BiomeAPI;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.matrix.FiguraMat2;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.matrix.FiguraMatrix;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.math.vector.FiguraVector;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendertasks.BlockTask;
import org.figuramc.figura.model.rendertasks.EntityTask;
import org.figuramc.figura.model.rendertasks.ItemTask;
import org.figuramc.figura.model.rendertasks.RenderTask;
import org.figuramc.figura.model.rendertasks.SpriteTask;
import org.figuramc.figura.model.rendertasks.TextTask;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class FiguraDocsManager {
   private static final Map<Class<?>, String> NAME_MAP = new HashMap<Class<?>, String>() {
      {
         this.put(Double.class, "Number");
         this.put(double.class, "Number");
         this.put(Float.class, "Number");
         this.put(float.class, "Number");
         this.put(Number.class, "Number");
         this.put(Integer.class, "Integer");
         this.put(int.class, "Integer");
         this.put(Long.class, "Integer");
         this.put(long.class, "Integer");
         this.put(void.class, "nil");
         this.put(String.class, "String");
         this.put(Object.class, "AnyType");
         this.put(LuaUserdata.class, "Userdata");
         this.put(Boolean.class, "Boolean");
         this.put(boolean.class, "Boolean");
         this.put(LuaFunction.class, "Function");
         this.put(LuaTable.class, "Table");
         this.put(LuaValue.class, "AnyType");
         this.put(Varargs.class, "Varargs");
         this.put(Map.class, "Table");
         this.put(HashMap.class, "Table");
         this.put(List.class, "Table");
         this.put(ArrayList.class, "Table");
         this.put(FiguraVector.class, "Vector");
         this.put(FiguraMatrix.class, "Matrix");
      }
   };
   private static final Map<Class<?>, String> CLASS_COMMAND_MAP = new HashMap<>();
   private static final Map<String, Collection<Class<?>>> GLOBAL_CHILDREN = new HashMap<String, Collection<Class<?>>>() {
      {
         this.put("action_wheel", List.of(ActionWheelAPI.class, Page.class, Action.class));
         this.put("animations", List.of(AnimationAPI.class, Animation.class));
         this.put("nameplate", List.of(NameplateAPI.class, NameplateCustomization.class, EntityNameplateCustomization.class, NameplateCustomizationGroup.class));
         this.put("world", List.of(WorldAPI.class, BiomeAPI.class, BlockStateAPI.class, ItemStackAPI.class));
         this.put("vanilla_model", List.of(VanillaModelAPI.class, VanillaPart.class, VanillaModelPart.class, VanillaGroupPart.class));
         this.put(
            "models",
            List.of(Vertex.class, FiguraModelPart.class, RenderTask.class, BlockTask.class, ItemTask.class, TextTask.class, SpriteTask.class, EntityTask.class)
         );
         this.put("player", List.of(EntityAPI.class, LivingEntityAPI.class, PlayerAPI.class, ViewerAPI.class));
         this.put("events", List.of(EventsAPI.class, LuaEvent.class));
         this.put("keybinds", List.of(KeybindAPI.class, FiguraKeybind.class));
         this.put("vectors", List.of(VectorsAPI.class, FiguraVec2.class, FiguraVec3.class, FiguraVec4.class));
         this.put("matrices", List.of(MatricesAPI.class, FiguraMat2.class, FiguraMat3.class, FiguraMat4.class));
         this.put("client", List.of(ClientAPI.class));
         this.put("host", List.of(HostAPI.class));
         this.put("avatar", List.of(AvatarAPI.class));
         this.put("particles", List.of(ParticleAPI.class, LuaParticle.class));
         this.put("sounds", List.of(SoundAPI.class, LuaSound.class));
         this.put("renderer", List.of(RendererAPI.class));
         this.put("pings", List.of(PingAPI.class, PingFunction.class));
         this.put("textures", List.of(TextureAPI.class, FiguraTexture.class, TextureAtlasAPI.class));
         this.put("config", List.of(ConfigAPI.class));
         this.put("data", List.of(DataAPI.class, FiguraInputStream.class, FiguraOutputStream.class, FiguraBuffer.class, FiguraFuture.class));
         this.put("net", List.of(NetworkingAPI.class, HttpRequestsAPI.class, HttpRequestsAPI.HttpResponse.class, HttpRequestsAPI.HttpRequestBuilder.class));
         this.put("file", List.of(FileAPI.class));
         this.put("json", List.of(JsonAPI.class, FiguraJsonBuilder.class, FiguraJsonSerializer.class, FiguraJsonObject.class, FiguraJsonArray.class));
         this.put("resources", List.of(ResourcesAPI.class));
         this.put("raycast", List.of(RaycastAPI.class));
      }
   };
   private static final Map<String, List<FiguraDoc>> GENERATED_CHILDREN = new HashMap<>();
   private static FiguraDoc.ClassDoc global;
   private static final List<Class<?>> LUA_LIB_OVERRIDES = List.of(FiguraMathDocs.class);
   private static final List<FiguraDoc> GENERATED_LIB_OVERRIDES = new ArrayList<>();

   public static void init() {
      for (Entry<String, Collection<Class<?>>> packageEntry : GLOBAL_CHILDREN.entrySet()) {
         for (Class<?> documentedClass : packageEntry.getValue()) {
            FiguraDoc.ClassDoc doc = generateDocFor(documentedClass, "globals " + packageEntry.getKey());
            if (doc != null) {
               GENERATED_CHILDREN.computeIfAbsent(packageEntry.getKey(), s -> new ArrayList<>()).add(doc);
            }
         }
      }

      for (Class<?> lib : LUA_LIB_OVERRIDES) {
         FiguraDoc.ClassDoc libDoc = generateDocFor(lib, null);
         if (libDoc != null) {
            GENERATED_LIB_OVERRIDES.add(libDoc);
         }
      }

      Class<?> globalClass = FiguraGlobalsDocs.class;
      global = new FiguraDoc.ClassDoc(globalClass, globalClass.getAnnotation(LuaTypeDoc.class), GENERATED_CHILDREN);
   }

   public static void initEntryPoints(Set<FiguraAPI> set) {
      for (FiguraAPI api : set) {
         GLOBAL_CHILDREN.put(api.getName(), api.getDocsClasses());
      }
   }

   private static FiguraDoc.ClassDoc generateDocFor(Class<?> documentedClass, String pack) {
      if (!documentedClass.isAnnotationPresent(LuaTypeDoc.class)) {
         return null;
      }

      FiguraDoc.ClassDoc doc = new FiguraDoc.ClassDoc(documentedClass, documentedClass.getAnnotation(LuaTypeDoc.class));
      NAME_MAP.put(documentedClass, doc.name);
      CLASS_COMMAND_MAP.put(documentedClass, "/figura docs " + (pack == null ? "" : pack) + " " + doc.name);
      return doc;
   }

   public static String getNameFor(Class<?> clazz) {
      return NAME_MAP.computeIfAbsent(clazz, aClass -> {
         if (clazz.isAnnotationPresent(LuaTypeDoc.class)) {
            return clazz.getAnnotation(LuaTypeDoc.class).name();
         } else {
            return clazz.getName().startsWith("[") ? "Varargs" : clazz.getName();
         }
      });
   }

   public static MutableComponent getClassText(Class<?> clazz) {
      String name = getNameFor(clazz);
      String doc = CLASS_COMMAND_MAP.get(clazz);
      MutableComponent text = Component.literal(name);
      if (doc == null) {
         return text;
      }

      text.setStyle(
         Style.EMPTY
            .withClickEvent(new RunCommand(doc))
            .withHoverEvent(new ShowText(FiguraText.of("command.docs_type_hover", Component.literal(name).withStyle(ChatFormatting.DARK_PURPLE))))
            .withUnderlined(true)
      );
      return text;
   }

   public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
      LiteralArgumentBuilder<FiguraClientCommandSource> root = LiteralArgumentBuilder.literal("docs");
      root.executes(context -> FiguraDoc.printRoot());
      LiteralArgumentBuilder<FiguraClientCommandSource> globals = global == null ? LiteralArgumentBuilder.literal("globals") : global.getCommand();
      root.then(globals);

      for (FiguraDoc figuraDoc : GENERATED_LIB_OVERRIDES) {
         root.then(figuraDoc.getCommand());
      }

      root.then(FiguraListDocs.getCommand());
      return root;
   }

   public static LiteralArgumentBuilder<FiguraClientCommandSource> getExportCommand() {
      LiteralArgumentBuilder<FiguraClientCommandSource> root = LiteralArgumentBuilder.literal("docs");
      root.executes(context -> exportDocsFunction(context, true));
      RequiredArgumentBuilder<FiguraClientCommandSource, Boolean> e = RequiredArgumentBuilder.argument("translate", BoolArgumentType.bool());
      e.executes(context -> exportDocsFunction(context, BoolArgumentType.getBool(context, "translate")));
      root.then(e);
      return root;
   }

   private static int exportDocsFunction(CommandContext<FiguraClientCommandSource> context, boolean translate) {
      try {
         Path targetPath = FiguraMod.getFiguraDirectory().resolve("exported_docs.json");
         if (!Files.exists(targetPath)) {
            Files.createFile(targetPath);
         }

         OutputStream fs = Files.newOutputStream(targetPath);
         fs.write(exportAsJsonString(translate).getBytes());
         fs.close();
         ((FiguraClientCommandSource)context.getSource())
            .figura$sendFeedback(
               FiguraText.of("command.docs_export.success")
                  .append(" ")
                  .append(FiguraText.of("command.click_to_open").setStyle(Style.EMPTY.withClickEvent(new OpenFile(targetPath.toString())).withUnderlined(true)))
            );
         return 1;
      } catch (Exception e) {
         ((FiguraClientCommandSource)context.getSource()).figura$sendError(FiguraText.of("command.docs_export.error"));
         FiguraMod.LOGGER.error("Failed to export docs!", e);
         return 0;
      }
   }

   public static String exportAsJsonString(boolean translate) {
      JsonObject root = new JsonObject();
      JsonObject globals = global == null ? new JsonObject() : global.toJson(translate);
      root.add("globals", globals);

      for (FiguraDoc figuraDoc : GENERATED_LIB_OVERRIDES) {
         root.add(figuraDoc.name, figuraDoc.toJson(translate));
      }

      root.add("lists", FiguraListDocs.toJson(translate));
      return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(root);
   }
}
