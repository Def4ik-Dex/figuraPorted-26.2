package org.figuramc.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.ducks.GameEffects;
import org.figuramc.figura.mixin.input.KeyMappingAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.model.rendering.texture.FiguraRenderTypes;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

public class FiguraListDocs {
   public static final LinkedHashSet<String> KEYBINDS = new LinkedHashSet<>();
   private static final LinkedHashMap<String, List<String>> PARENT_TYPES = new LinkedHashMap<String, List<String>>() {
      {
         for (ParentType value : ParentType.values()) {
            this.put(value.name(), Arrays.asList(value.aliases));
         }
      }
   };
   private static final LinkedHashMap<String, List<String>> STRING_ENCODINGS = new LinkedHashMap<String, List<String>>() {
      {
         this.put("utf8", List.of("utf_8"));
         this.put("utf16", List.of("utf_16"));
         this.put("utf16be", List.of("utf_16_be"));
         this.put("utf16le", List.of("utf_16_le"));
         this.put("ascii", List.of());
         this.put("iso88591", List.of("iso_8859_1"));
      }
   };
   private static final LinkedHashSet<String> RENDER_TYPES = new LinkedHashSet<String>() {
      {
         for (FiguraRenderTypes value : FiguraRenderTypes.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> TEXTURE_TYPES = new LinkedHashSet<String>() {
      {
         for (FiguraTextureSet.OverrideType value : FiguraTextureSet.OverrideType.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> ENTITY_POSES = new LinkedHashSet<String>() {
      {
         for (Pose value : Pose.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> ITEM_DISPLAY_MODES = new LinkedHashSet<String>() {
      {
         for (ItemDisplayContext value : ItemDisplayContext.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> POST_EFFECTS = new LinkedHashSet<String>() {
      {
         for (Identifier effect : GameEffects.getEffects()) {
            String[] split = effect.getPath().split("/");
            String name = split[split.length - 1];
            this.add(name.split("\\.")[0]);
         }
      }
   };
   private static final LinkedHashSet<String> PLAY_STATES = new LinkedHashSet<String>() {
      {
         for (Animation.PlayState value : Animation.PlayState.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> LOOP_MODES = new LinkedHashSet<String>() {
      {
         for (Animation.LoopMode value : Animation.LoopMode.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashMap<String, List<String>> COLORS = new LinkedHashMap<String, List<String>>() {
      {
         for (ColorUtils.Colors value : ColorUtils.Colors.values()) {
            this.put(value.name(), List.of(value.name()));
         }
      }
   };
   private static final LinkedHashSet<String> PLAYER_MODEL_PARTS = new LinkedHashSet<String>() {
      {
         for (PlayerModelPart value : PlayerModelPart.values()) {
            String name = value.name();
            this.add(name.endsWith("_LEG") ? name.substring(0, name.length() - 4) : name);
         }
      }
   };
   private static final LinkedHashSet<String> USE_ACTIONS = new LinkedHashSet<String>() {
      {
         for (ItemUseAnimation value : ItemUseAnimation.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> RENDER_MODES = new LinkedHashSet<String>() {
      {
         for (EntityRenderMode value : EntityRenderMode.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> BLOCK_RAYCAST_TYPE = new LinkedHashSet<String>() {
      {
         for (Block value : Block.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> FLUID_RAYCAST_TYPE = new LinkedHashSet<String>() {
      {
         for (Fluid value : Fluid.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> HEIGHTMAP_TYPE = new LinkedHashSet<String>() {
      {
         for (Types value : Types.values()) {
            this.add(value.name());
         }
      }
   };
   private static final LinkedHashSet<String> REGISTRIES = new LinkedHashSet<String>() {
      {
         for (Identifier resourceLocation : BuiltInRegistries.REGISTRY.keySet()) {
            this.add(resourceLocation.getPath());
         }
      }
   };

   public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
      LiteralArgumentBuilder<FiguraClientCommandSource> root = LiteralArgumentBuilder.literal("enums");
      root.executes(
         context -> {
            FiguraMod.sendChatMessage(
               FiguraDoc.HEADER
                  .copy()
                  .append("\n\n")
                  .append(Component.literal("• ").append(FiguraText.of("docs.text.type")).append(":").withStyle(ColorUtils.Colors.PURPLE.style))
                  .append("\n\t")
                  .append(Component.literal("• ").append(Component.literal("enumerators")).withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
                  .append("\n\n")
                  .append(Component.literal("• ").append(FiguraText.of("docs.text.description")).append(":").withStyle(ColorUtils.Colors.PURPLE.style))
                  .append("\n\t")
                  .append(Component.literal("• ").append(FiguraText.of("docs.enum")).withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
            );
            return 1;
         }
      );

      for (FiguraListDocs.ListDoc value : FiguraListDocs.ListDoc.values()) {
         root.then(value.generateCommand());
      }

      return root;
   }

   public static List<String> getEnumValues(String enumName) {
      try {
         FiguraListDocs.ListDoc enumListDoc = FiguraListDocs.ListDoc.valueOf(enumName.toUpperCase(Locale.US));
         Collection<?> enumValues = enumListDoc.get();
         List<String> enumValueList = new ArrayList<>();

         for (Object value : enumValues) {
            if (value instanceof Entry<?, ?> entry) {
               enumValueList.add(entry.getKey().toString());
               if (entry.getValue() instanceof Collection) {
                  for (Object alias : (Collection)entry.getValue()) {
                     enumValueList.add(alias.toString());
                  }
               }
            } else {
               enumValueList.add(value.toString());
            }
         }

         return enumValueList;
      } catch (IllegalArgumentException e) {
         throw new RuntimeException("Enum " + enumName + " does not exist");
      }
   }

   public static JsonElement toJson(boolean translate) {
      JsonArray array = new JsonArray();

      for (FiguraListDocs.ListDoc value : FiguraListDocs.ListDoc.values()) {
         array.add(value.generateJson(translate));
      }

      return array;
   }

   private enum ListDoc {
      KEYBINDS(() -> FiguraListDocs.KEYBINDS, "Keybinds", "keybinds", 2),
      PARENT_TYPES(() -> FiguraListDocs.PARENT_TYPES, "ParentTypes", "parent_types", 1),
      RENDER_TYPES(() -> FiguraListDocs.RENDER_TYPES, "FiguraRenderTypes", "render_types", 1),
      TEXTURE_TYPES(() -> FiguraListDocs.TEXTURE_TYPES, "TextureTypes", "texture_types", 1),
      KEY_IDS(() -> new LinkedHashSet<Object>() {
         {
            this.addAll(KeyMappingAccessor.getAll().keySet());
         }
      }, "KeyIDs", "key_ids", 2),
      ENTITY_POSES(() -> FiguraListDocs.ENTITY_POSES, "EntityPoses", "entity_poses", 2),
      ITEM_RENDER_TYPES(() -> FiguraListDocs.ITEM_DISPLAY_MODES, "ItemDisplayModes", "item_display_modes", 1),
      POST_EFFECTS(() -> FiguraListDocs.POST_EFFECTS, "PostEffects", "post_effects", 2),
      PLAY_STATES(() -> FiguraListDocs.PLAY_STATES, "PlayStates", "play_states", 1),
      LOOP_MODES(() -> FiguraListDocs.LOOP_MODES, "LoopModes", "loop_modes", 1),
      COLORS(() -> FiguraListDocs.COLORS, "Colors", "colors", 1),
      PLAYER_MODEL_PARTS(() -> FiguraListDocs.PLAYER_MODEL_PARTS, "PlayerModelParts", "player_model_parts", 1),
      USE_ACTIONS(() -> FiguraListDocs.USE_ACTIONS, "UseActions", "use_actions", 1),
      RENDER_MODES(() -> FiguraListDocs.RENDER_MODES, "RenderModes", "render_modes", 1),
      STRING_ENCODINGS(() -> FiguraListDocs.STRING_ENCODINGS, "StringEncodings", "string_encodings", 1),
      BLOCK_RAYCAST_TYPE(() -> FiguraListDocs.BLOCK_RAYCAST_TYPE, "BlockRaycastTypes", "block_raycast_types", 1),
      FLUID_RAYCAST_TYPE(() -> FiguraListDocs.FLUID_RAYCAST_TYPE, "FluidRaycastTypes", "fluid_raycast_types", 1),
      HEIGHTMAP_TYPE(() -> FiguraListDocs.HEIGHTMAP_TYPE, "HeightmapTypes", "heightmap_types", 1),
      REGISTRIES(() -> FiguraListDocs.REGISTRIES, "Registries", "registries", 1);

      private final Supplier<Object> supplier;
      private final String name;
      private final String id;
      private final int split;

      ListDoc(Supplier<Object> supplier, String name, String id, int split) {
         this.supplier = supplier;
         this.name = name;
         this.id = id;
         this.split = split;
      }

      private Collection<?> get() {
         Object obj = this.supplier.get();
         if (obj instanceof LinkedHashSet<?> set) {
            return set;
         } else if (obj instanceof Map<?, ?> map) {
            return map.entrySet();
         } else {
            throw new UnsupportedOperationException("Invalid object " + obj);
         }
      }

      private JsonElement generateJson(boolean translate) {
         JsonObject object = new JsonObject();
         object.addProperty("name", this.name);
         object.addProperty(
            "description", translate ? Language.getInstance().getOrDefault(FiguraText.of("docs.enum." + this.id).getString()) : "figura.docs.enum." + this.id
         );
         Collection<?> coll = this.get();
         if (coll.size() == 0) {
            return object;
         }

         JsonArray entries = new JsonArray();

         for (Object o : coll) {
            if (o instanceof Entry e) {
               entries.add(e.getKey().toString());

               for (Object s : (List<?>)e.getValue()) {
                  entries.add(s.toString());
               }
            } else {
               entries.add(o.toString());
            }
         }

         object.add("entries", entries);
         return object;
      }

      private LiteralArgumentBuilder<FiguraClientCommandSource> generateCommand() {
         LiteralArgumentBuilder<FiguraClientCommandSource> command = LiteralArgumentBuilder.literal(this.id);
         command.executes(
            context -> {
               Collection<?> coll = this.get();
               if (coll.size() == 0) {
                  FiguraMod.sendChatMessage(FiguraText.of("docs.enum.empty"));
                  return 0;
               }

               MutableComponent textx = FiguraDoc.HEADER
                  .copy()
                  .append("\n\n")
                  .append(Component.literal("• ").append(FiguraText.of("docs.text.description")).append(":").withStyle(ColorUtils.Colors.PURPLE.style))
                  .append("\n\t")
                  .append(Component.literal("• ").append(FiguraText.of("docs.enum." + this.id)).withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
                  .append("\n\n")
                  .append(Component.literal("• ").append(FiguraText.of("docs.text.entries")).append(":").withStyle(ColorUtils.Colors.PURPLE.style));
               int i = 0;

               for (Object ox : coll) {
                  MutableComponent component;
                  if (ox instanceof Entry ex) {
                     component = Component.literal(ex.getKey().toString()).withStyle(ChatFormatting.WHITE);

                     for (Object sx : (List<?>)ex.getValue()) {
                        component.append(Component.literal(" | ").withStyle(ChatFormatting.YELLOW))
                           .append(Component.literal(sx.toString()).withStyle(ChatFormatting.GRAY));
                     }
                  } else {
                     component = Component.literal(ox.toString()).withStyle(ChatFormatting.WHITE);
                  }

                  textx.append(i % this.split == 0 ? "\n\t" : "\t");
                  textx.append(Component.literal("• ").withStyle(ChatFormatting.YELLOW)).append(component);
                  i++;
               }

               FiguraMod.sendChatMessage(textx);
               return 1;
            }
         );

         for (Object o : this.get()) {
            String text = o instanceof Entry e ? e.getKey().toString() : o.toString();
            LiteralArgumentBuilder<FiguraClientCommandSource> entry = LiteralArgumentBuilder.literal(text);
            entry.executes(context -> {
               FiguraMod.sendChatMessage(Component.literal(text).withStyle(ColorUtils.Colors.AWESOME_BLUE.style));
               return 1;
            });
            if (o instanceof Entry e) {
               for (Object s : (List<?>)e.getValue()) {
                  String str = s.toString();
                  LiteralArgumentBuilder<FiguraClientCommandSource> child = LiteralArgumentBuilder.literal(str);
                  child.executes(context -> {
                     FiguraMod.sendChatMessage(Component.literal(str).withStyle(ColorUtils.Colors.AWESOME_BLUE.style));
                     return 1;
                  });
                  entry.then(child);
               }
            }

            command.then(entry);
         }

         return command;
      }
   }
}
