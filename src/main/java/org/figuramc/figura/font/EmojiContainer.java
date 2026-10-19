package org.figuramc.figura.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.JsonUtils;
import org.figuramc.figura.utils.TextUtils;

public class EmojiContainer {
   public static final String JSON_KEY_FRAMES = "frames";
   public static final String JSON_KEY_FRAME_TIME = "frametime";
   public static final String JSON_KEY_WIDTH = "width";
   public static final String JSON_KEY_NAMES = "names";
   public static final String JSON_KEY_SHORTCUTS = "shortcuts";
   private static final String ERROR_MSG = "Invalid emoji metadata \"{}\" @ \"{}\", Reason: Field '{}' {}";
   private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE);
   public final String name;
   private final Identifier font;
   private final EmojiUnicodeLookup lookup = new EmojiUnicodeLookup();
   private final String blacklist;

   public EmojiContainer(String containerName, JsonObject data) {
      this.name = containerName;
      this.font = FiguraIdentifier.of("emoji_" + containerName);
      this.blacklist = data.get("blacklist").getAsString();

      for (Entry<String, JsonElement> emoji : data.get("emojis").getAsJsonObject().entrySet()) {
         String curUnicode = emoji.getKey();
         JsonElement curValue = emoji.getValue();
         JsonArray namesArray = null;
         JsonArray shortcutsArray = null;
         if (curValue.isJsonArray()) {
            namesArray = curValue.getAsJsonArray();
         } else {
            JsonObject obj = curValue.getAsJsonObject();
            if (JsonUtils.validate(
               obj,
               "names",
               JsonElement::isJsonArray,
               "Invalid emoji metadata \"{}\" @ \"{}\", Reason: Field '{}' {}",
               curUnicode.codePointAt(0),
               containerName,
               "names",
               "must be an array"
            )) {
               namesArray = obj.getAsJsonArray("names");
            }

            if ((obj.has("frametime") || obj.has("frametime"))
               && JsonUtils.validate(
                  obj,
                  "frames",
                  JsonElement::isJsonPrimitive,
                  "Invalid emoji metadata \"{}\" @ \"{}\", Reason: Field '{}' {}",
                  curUnicode.codePointAt(0),
                  containerName,
                  "frames",
                  "field must be an int"
               )
               && JsonUtils.validate(
                  obj,
                  "frametime",
                  JsonElement::isJsonPrimitive,
                  "Invalid emoji metadata \"{}\" @ \"{}\", Reason: Field '{}' {}",
                  curUnicode.codePointAt(0),
                  containerName,
                  "frametime",
                  "field must be an int"
               )) {
               this.lookup.putMetadata(curUnicode.codePointAt(0), new EmojiMetadata(obj));
            }

            if (obj.has("shortcuts")
               && JsonUtils.validate(
                  obj,
                  "shortcuts",
                  JsonElement::isJsonArray,
                  "Invalid emoji metadata \"{}\" @ \"{}\", Reason: Field '{}' {}",
                  curUnicode.codePointAt(0),
                  containerName,
                  "shortcuts",
                  "field must be an array"
               )) {
               shortcutsArray = obj.getAsJsonArray("shortcuts");
            }
         }

         if (namesArray != null) {
            List<String> validAliases = new ArrayList<>();
            if (validateAliases(containerName, namesArray, validAliases::add)) {
               String[] arr = new String[validAliases.size()];
               this.lookup.putAliases(validAliases.toArray(arr), curUnicode);
            }
         }

         if (shortcutsArray != null) {
            List<String> validAliases = new ArrayList<>();
            if (validateAliases(containerName, shortcutsArray, validAliases::add)) {
               String[] arr = new String[validAliases.size()];
               this.lookup.putShortcuts(validAliases.toArray(arr), curUnicode);
            }
         }
      }
   }

   public EmojiUnicodeLookup getLookup() {
      return this.lookup;
   }

   private static boolean validateAliases(String containerName, JsonArray aliasArray, Consumer<String> consumer) {
      boolean atLeastOne = false;

      for (JsonElement element : aliasArray) {
         String alias = element.getAsString();
         if (!alias.isBlank() && alias.indexOf(32) == -1 && alias.indexOf(58) == -1) {
            consumer.accept(alias);
            atLeastOne = true;
         } else {
            FiguraMod.LOGGER.warn("Invalid emoji name \"{}\" in container: {}", alias, containerName);
         }
      }

      return atLeastOne;
   }

   public void tickAnimations() {
      for (EmojiMetadata metadata : this.lookup.metadataValues()) {
         metadata.tickAnimation();
      }
   }

   public Component getEmojiComponent(String key) {
      return this.getEmojiComponent(key, Component.literal(":" + key + ":"));
   }

   public Component getEmojiComponent(String key, MutableComponent hover) {
      String unicode = this.lookup.getUnicode(key);
      return unicode == null ? null : this.makeComponent(unicode, hover);
   }

   public Component getShortcutComponent(String shortcut) {
      String unicode = this.lookup.getUnicodeForShortcut(shortcut);
      return unicode == null ? null : this.makeComponent(unicode, Component.literal(shortcut));
   }

   private Component makeComponent(String unicode, MutableComponent hover) {
      return Component.literal(unicode)
         .withStyle(
            STYLE.withFont(new Resource(this.font))
               .withHoverEvent(new ShowText(hover.append("\n").append(FiguraText.of("emoji." + this.name).withStyle(ChatFormatting.DARK_GRAY))))
         );
   }

   public Component blacklist(Component text) {
      return this.blacklist.isBlank()
         ? text
         : TextUtils.replaceInText(text, "[" + this.blacklist + "]", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(this.font), Integer.MAX_VALUE);
   }

   public Identifier getFont() {
      return this.font;
   }
}
