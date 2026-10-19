package org.figuramc.figura.font;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.FiguraResourceListener;
import org.figuramc.figura.utils.TextUtils;

public class Emojis {
   private static final Map<String, EmojiContainer> EMOJIS = new HashMap<>();
   private static final Map<String, EmojiContainer> SHORTCUT_LOOKUP = new HashMap<>();
   public static final char DELIMITER = ':';
   public static final char ESCAPE = '\\';
   public static final FiguraResourceListener RESOURCE_LISTENER = FiguraResourceListener.createResourceListener(
      "emojis",
      manager -> {
         EMOJIS.clear();

         for (Entry<Identifier, Resource> emojis : manager.listResources(
               "emojis", location -> location.getNamespace().equals("figura") && location.getPath().endsWith(".json")
            )
            .entrySet()) {
            Identifier location = emojis.getKey();
            String[] split = location.getPath().split("/", 2);
            if (split.length > 1) {
               String name = split[1].substring(0, split[1].length() - 5);

               try (InputStream stream = emojis.getValue().open()) {
                  JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                  EmojiContainer container = new EmojiContainer(name, json);
                  EMOJIS.put(name, container);
                  container.getLookup().getShortcuts().forEach(shortcut -> SHORTCUT_LOOKUP.put(shortcut, container));
               } catch (Exception e) {
                  FiguraMod.LOGGER.error("Failed to load {} emojis", name, e);
               }
            }
         }

         HashMap<String, List<String>> duplicates = new HashMap<>();
         HashMap<String, String> map = new HashMap<>();

         for (EmojiContainer curContainer : EMOJIS.values()) {
            for (String curName : curContainer.getLookup().getNames()) {
               String prevValue = map.put(curName, curContainer.name);
               if (prevValue != null) {
                  List<String> list;
                  if (!duplicates.containsKey(curName)) {
                     list = new ArrayList<>();
                     list.add(curContainer.name);
                     duplicates.put(curName, list);
                  } else {
                     list = duplicates.get(curName);
                  }

                  list.add(prevValue);
               }
            }
         }

         for (String curName : duplicates.keySet()) {
            FiguraMod.LOGGER.warn("Duplicate emoji \"{}\" found in containers: {}", curName, String.join(", ", duplicates.get(curName)));
         }
      }
   );

   public static Collection<String> getCategoryNames() {
      return EMOJIS.keySet();
   }

   public static EmojiContainer getCategory(String key) {
      return EMOJIS.get(key);
   }

   public static boolean hasCategory(String key) {
      return EMOJIS.containsKey(key);
   }

   public static MutableComponent applyEmojis(Component text) {
      Component newText = TextUtils.parseLegacyFormatting(text);
      MutableComponent ret = Component.empty();
      newText.visit((style, string) -> {
         ret.append(convertEmoji(string, style));
         return Optional.empty();
      }, Style.EMPTY);
      return ret;
   }

   private static MutableComponent convertEmoji(String string, Style style) {
      List<String> strings = new ArrayList<>();
      StringBuilder current = new StringBuilder();
      boolean escaped = false;
      boolean inside = false;

      for (int i = 0; i < string.length(); i++) {
         char c = string.charAt(i);
         if (c == '\\') {
            escaped = !escaped;
            if (i + 1 == string.length() || string.charAt(i + 1) != ':') {
               current.append(c);
            }
         } else if (c == ':' && !escaped) {
            inside = !inside;
            strings.add(current.toString());
            current = new StringBuilder();
         } else {
            escaped = false;
            current.append(c);
            if (c == ' ' && inside) {
               inside = false;
               String removed = strings.remove(strings.size() - 1);
               current = new StringBuilder(removed).append(':').append(current);
            }
         }
      }

      if (current.length() > 0 || inside) {
         String toAdd = current.toString();
         if (inside) {
            int index = strings.size() - 1;
            String s = strings.get(index) + ":" + toAdd;
            strings.set(index, s);
         } else {
            strings.add(toAdd);
         }
      }

      MutableComponent result = Component.empty().withStyle(style);

      for (int i = 0; i < strings.size(); i++) {
         String s = strings.get(i);
         if (i % 2 != 0) {
            appendEmoji(result, s, Emojis::getEmoji);
         } else {
            List<String> shortcuts = SHORTCUT_LOOKUP.keySet().stream().filter(s::contains).toList();
            if (shortcuts.size() <= 0) {
               result.append(s);
            } else {
               while (s.length() > 0) {
                  boolean anyFound = false;

                  for (String shortcut : shortcuts) {
                     if (s.startsWith(shortcut)) {
                        s = s.substring(shortcut.length());
                        result.append(SHORTCUT_LOOKUP.get(shortcut).getShortcutComponent(shortcut));
                        anyFound = true;
                        break;
                     }
                  }

                  if (!anyFound) {
                     result.append(String.valueOf(s.charAt(0)));
                     s = s.substring(1);
                  }
               }
            }
         }
      }

      return result;
   }

   private static void appendEmoji(MutableComponent result, String s, Function<String, Component> converter) {
      Component emoji = converter.apply(s);
      if (emoji != null) {
         result.append(emoji);
      } else {
         result.append(":" + s + ":");
      }
   }

   public static Component getEmoji(String emojiAlias) {
      for (EmojiContainer container : EMOJIS.values()) {
         Component emoji = container.getEmojiComponent(emojiAlias);
         if (emoji != null) {
            return emoji;
         }
      }

      return null;
   }

   public static Component getEmoji(String emojiAlias, MutableComponent hover) {
      for (EmojiContainer container : EMOJIS.values()) {
         Component emoji = container.getEmojiComponent(emojiAlias, hover);
         if (emoji != null) {
            return emoji;
         }
      }

      return null;
   }

   public static EmojiContainer getCategoryByFont(Identifier location) {
      for (EmojiContainer container : EMOJIS.values()) {
         if (location.equals(container.getFont())) {
            return container;
         }
      }

      return null;
   }

   public static Component removeBlacklistedEmojis(Component text) {
      for (EmojiContainer container : EMOJIS.values()) {
         text = container.blacklist(text);
      }

      return text;
   }

   public static List<String> getMatchingEmojis(String query) {
      if (query.length() != 0 && query.charAt(0) == ':') {
         String name = query.substring(1);
         List<String> emojis = new ArrayList<>();

         for (EmojiContainer container : EMOJIS.values()) {
            for (String s : container.getLookup().getNames()) {
               if (s.startsWith(name)) {
                  emojis.add(":" + s + ":");
               }
            }
         }

         return emojis;
      } else {
         return List.of();
      }
   }

   public static void tickAnimations() {
      for (EmojiContainer container : EMOJIS.values()) {
         container.tickAnimations();
      }
   }
}
