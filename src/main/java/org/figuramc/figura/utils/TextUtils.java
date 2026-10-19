package org.figuramc.figura.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.StringSplitter.WidthLimitedCharSink;
 
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;

public class TextUtils {
   public static final Component TAB = FiguraText.of("tab");
   public static final Component ELLIPSIS = FiguraText.of("ellipsis");
   public static final Component UNKNOWN = Component.literal("�").withStyle(Style.EMPTY);
   public static boolean allowScriptEvents;
   public static final RegistryOps<JsonElement> OPS = RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE);

   public static List<Component> splitText(FormattedText text, String regex) {
      ArrayList<Component> textList = new ArrayList<>();
      MutableComponent[] currentText = new MutableComponent[]{Component.empty()};
      text.visit((style, string) -> {
         String[] lines = string.split(regex, -1);

         for (int i = 0; i < lines.length; i++) {
            if (i != 0) {
               textList.add(currentText[0].copy());
               currentText[0] = Component.empty();
            }

            currentText[0].append(Component.literal(lines[i]).withStyle(style));
         }

         return Optional.empty();
      }, Style.EMPTY);
      textList.add(currentText[0]);
      return textList;
   }

   public static Component removeClickableObjects(FormattedText text) {
      return removeClickableObjects(text, p -> true);
   }

   public static Component removeClickableObjects(FormattedText text, Predicate<ClickEvent> pred) {
      MutableComponent ret = Component.empty();
      text.visit(
         (style, string) -> {
            ret.append(
               Component.literal(string).withStyle(style.getClickEvent() != null && pred.test(style.getClickEvent()) ? style.withClickEvent(null) : style)
            );
            return Optional.empty();
         },
         Style.EMPTY
      );
      return ret;
   }

   public static Component tryParseJson(String text) {
      if (text == null) {
         return Component.empty();
      }

      Component finalText;
      try {
         JsonElement object = JsonParser.parseString(text);
         if (object.isJsonObject()) {
            JsonObject obj = object.getAsJsonObject();
            if (obj.has("clickEvent")) {
               JsonElement clickEvent = obj.get("clickEvent");
               JsonObject replacement = convertClickEvent(clickEvent);
               obj.remove("clickEvent");
               obj.add("click_event", replacement);
            }

            if (obj.has("hoverEvent")) {
               JsonElement hoverEvent = obj.get("hoverEvent");
               JsonObject replacement = convertHoverEvent(hoverEvent);
               obj.remove("hoverEvent");
               obj.add("hover_event", replacement);
            }
         }

         finalText = (Component)((Pair)ComponentSerialization.CODEC.decode(OPS, object).getOrThrow()).getFirst();
         if (finalText == null) {
            throw new Exception("Error parsing JSON string");
         }
      } catch (Exception ignored) {
         finalText = Component.literal(text);
      }

      return finalText;
   }

   @NotNull
   private static JsonObject convertHoverEvent(JsonElement hoverEvent) {
      JsonObject replacement = new JsonObject();
      if (hoverEvent.isJsonObject()) {
         JsonObject event = hoverEvent.getAsJsonObject();
         if (event.has("action")) {
            JsonElement action = event.get("action");
            switch (action.getAsString()) {
               case "show_text":
                  replacement.addProperty("action", "show_text");
                  if (event.has("value")) {
                     String value = event.get("value").getAsString();
                     replacement.addProperty("value", value);
                  } else if (event.has("contents")) {
                     String content = event.get("contents").getAsString();
                     replacement.addProperty("value", content);
                  }
                  break;
               case "show_item":
                  replacement.addProperty("action", "show_item");
                  if (event.has("contents") && event.get("contents").isJsonObject()) {
                     JsonObject content = event.get("contents").getAsJsonObject();

                     for (Entry<String, JsonElement> entry : content.entrySet()) {
                        replacement.addProperty(entry.getKey(), entry.getValue().getAsString());
                     }
                  } else if (event.has("contents")) {
                     String id = event.get("contents").getAsString();
                     replacement.addProperty("id", id);
                  }
                  break;
               case "show_entity":
                  replacement.addProperty("action", "show_entity");
                  if (event.has("contents") && event.get("contents").isJsonObject()) {
                     JsonObject content = event.get("contents").getAsJsonObject();

                     for (Entry<String, JsonElement> entry : content.entrySet()) {
                        String key = entry.getKey();
                        if (key.equals("id")) {
                           key = "uuid";
                        } else if (key.equals("type")) {
                           key = "id";
                        }

                        replacement.addProperty(key, entry.getValue().getAsString());
                     }
                  }
            }
         }
      }

      return replacement;
   }

   @NotNull
   private static JsonObject convertClickEvent(JsonElement clickEvent) {
      JsonObject replacement = new JsonObject();
      if (clickEvent.isJsonObject()) {
         JsonObject event = clickEvent.getAsJsonObject();
         if (event.has("action") && event.has("value")) {
            JsonElement action = event.get("action");
            switch (action.getAsString()) {
               case "open_url":
                  replacement.addProperty("action", "open_url");
                  String url = event.get("value").getAsString();
                  if (!url.startsWith("http")) {
                     url = "http://" + url;
                  }

                  replacement.addProperty("url", url);
                  break;
               case "run_command": {
                  replacement.addProperty("action", "run_command");
                  String command = event.get("value").getAsString();
                  replacement.addProperty("command", command);
                  break;
               }
               case "suggest_command": {
                  replacement.addProperty("action", "suggest_command");
                  String command = event.get("value").getAsString();
                  replacement.addProperty("suggest_command", command);
                  break;
               }
               case "change_page":
                  replacement.addProperty("action", "change_page");
                  String page = event.get("value").getAsString();
                  Integer pageVal = Integer.parseInt(page);
                  replacement.addProperty("page", pageVal);
                  break;
               case "copy_to_clipboard":
                  replacement.addProperty("action", "copy_to_clipboard");
                  String value = event.get("value").getAsString();
                  replacement.addProperty("value", value);
            }
         }
      }

      return replacement;
   }

   public static Component replaceInText(FormattedText text, String regex, Object replacement) {
      return replaceInText(text, regex, replacement, (s, style) -> true, Integer.MAX_VALUE);
   }

   public static Component replaceInText(FormattedText text, String regex, Object replacement, BiPredicate<String, Style> predicate, int times) {
      return replaceInText(text, regex, replacement, predicate, 0, times);
   }

   public static Component replaceInText(FormattedText text, String regex, Object replacement, BiPredicate<String, Style> predicate, int beginIndex, int times) {
      Component replace = (Component)(replacement instanceof Component c ? c : Component.literal(replacement.toString()));
      MutableComponent ret = Component.empty();
      int[] ints = new int[]{beginIndex, times};
      text.visit((style, string) -> {
         if (!predicate.test(string, style)) {
            ret.append(Component.literal(string).withStyle(style));
            return Optional.empty();
         }

         String[] split = string.split("((?<=" + regex + ")|(?=" + regex + "))");

         for (String s : split) {
            if (!s.matches(regex)) {
               ret.append(Component.literal(s).withStyle(style));
            } else {
               if (ints[0] <= 0 && ints[1] > 0) {
                  ret.append(Component.empty().withStyle(style).append(replace));
               } else {
                  ret.append(Component.literal(s).withStyle(style));
               }

               ints[0]--;
               ints[1]--;
            }
         }

         return Optional.empty();
      }, Style.EMPTY);
      return ret;
   }

   public static Component trimToWidthEllipsis(Font font, Component text, int width, Component ellipsis) {
      return font.width(text.getVisualOrderText()) <= width ? text : addEllipsis(font, text, width, ellipsis);
   }

   public static Component addEllipsis(Font font, FormattedText text, int width, Component ellipsis) {
      FormattedText trimmed = font.substrByWidth(text, width - font.width(ellipsis));
      return formattedTextToText(trimmed).copy().append(ellipsis);
   }

   public static Component replaceTabs(FormattedText text) {
      return replaceInText(text, "\\t", TAB);
   }

   public static List<FormattedCharSequence> wrapTooltip(FormattedText text, Font font, int mousePos, int screenWidth, int offset) {
      List<Component> splitText = splitText(text, "\n");
      int left = mousePos - offset;
      int right = screenWidth - mousePos - offset;
      int largest = getWidth(splitText, font);
      int side = largest <= right ? right : (largest <= left ? left : Math.max(left, right));
      return wrapText(text, side, font);
   }

   public static int getWidth(List<?> text, Font font) {
      int width = 0;

      for (Object object : text) {
         int w;
         if (object instanceof Component component) {
            w = font.width(component);
         } else if (object instanceof FormattedCharSequence charSequence) {
            w = font.width(charSequence);
         } else if (object instanceof String s) {
            w = font.width(s);
         } else {
            w = 0;
         }

         width = Math.max(width, w);
      }

      return width;
   }

   public static int getHeight(List<?> text, Font font, int lineSpaceing) {
      int lines = text.size();
      return lines * 9 + Math.max((lines - 1) * lineSpaceing, 0);
   }

   public static int getHeight(List<?> text, Font font) {
      return getHeight(text, font, 1);
   }

   public static Component replaceStyle(FormattedText text, Style newStyle, Predicate<Style> predicate) {
      MutableComponent ret = Component.empty();
      text.visit((style, string) -> {
         ret.append(Component.literal(string).withStyle(predicate.test(style) ? newStyle.applyTo(style) : style));
         return Optional.empty();
      }, Style.EMPTY);
      return ret;
   }

   public static Component setStyleAtWidth(FormattedText text, int width, Font font, Style newStyle) {
      MutableComponent ret = Component.empty();
      text.visit((style, string) -> {
         MutableComponent current = Component.literal(string).withStyle(style);
         int prevWidth = font.width(ret);
         int currentWidth = font.width(current);
         if (prevWidth <= width && prevWidth + currentWidth > width) {
            current.withStyle(newStyle);
         }

         ret.append(current);
         return Optional.empty();
      }, Style.EMPTY);
      return ret;
   }

   public static List<FormattedCharSequence> wrapText(FormattedText text, int width, Font font) {
      List<FormattedCharSequence> warp = new ArrayList<>();
      font.getSplitter().splitLines(text, width, Style.EMPTY, (formattedText, aBoolean) -> warp.add(Language.getInstance().getVisualOrder(formattedText)));
      return warp;
   }

   public static Component charSequenceToText(FormattedCharSequence charSequence) {
      MutableComponent builder = Component.empty();
      StringBuilder buffer = new StringBuilder();
      Style[] lastStyle = new Style[1];
      charSequence.accept((index, style, codePoint) -> {
         if (!style.equals(lastStyle[0])) {
            if (buffer.length() > 0) {
               builder.append(Component.literal(buffer.toString()).withStyle(lastStyle[0]));
               buffer.setLength(0);
            }

            lastStyle[0] = style;
         }

         buffer.append(Character.toChars(codePoint));
         return true;
      });
      if (buffer.length() > 0) {
         builder.append(Component.literal(buffer.toString()).withStyle(lastStyle[0]));
      }

      return builder;
   }

   public static Component formattedTextToText(FormattedText formattedText) {
      if (formattedText instanceof Component c) {
         return c;
      } else {
         MutableComponent builder = Component.empty();
         formattedText.visit((style, string) -> {
            builder.append(Component.literal(string).withStyle(style));
            return Optional.empty();
         }, Style.EMPTY);
         return builder;
      }
   }

   public static Component substring(FormattedText text, int beginIndex, int endIndex) {
      StringBuilder counter = new StringBuilder();
      MutableComponent builder = Component.empty();
      text.visit((style, string) -> {
         int index = counter.length();
         int len = string.length();
         if (index <= endIndex && index + len >= beginIndex) {
            int sub = Math.max(beginIndex - index, 0);
            int top = Math.min(endIndex - index, len);
            builder.append(Component.literal(string.substring(sub, top)).withStyle(style));
         }

         counter.append(string);
         return counter.length() > endIndex ? FormattedText.STOP_ITERATION : Optional.empty();
      }, Style.EMPTY);
      return builder;
   }

   public static Component parseLegacyFormatting(FormattedText text) {
      MutableComponent builder = Component.empty();
      text.visit((style, string) -> {
         if (string.contains("§")) {
            String[] split = string.split("§");
            if (split.length >= 2) {
               MutableComponent newText = Component.literal(split[0]).withStyle(style);

               for (int i = 1; i < split.length; i++) {
                  String s = split[i];
                  if (s.length() != 0) {
                     ChatFormatting formatting = ChatFormatting.getByCode(s.charAt(0));
                     if (formatting != null) {
                        style = style.applyLegacyFormat(formatting);
                     }

                     newText.append(Component.literal(s.substring(1)).withStyle(style));
                  }
               }

               builder.append(newText);
               return Optional.empty();
            }
         }

         builder.append(Component.literal(string).withStyle(style));
         return Optional.empty();
      }, Style.EMPTY);
      return builder;
   }

   public static Component reverse(FormattedText text) {
      MutableComponent[] builder = new MutableComponent[]{Component.empty()};
      text.visit((style, string) -> {
         StringBuilder str = new StringBuilder(string).reverse();
         builder[0] = Component.literal(str.toString()).withStyle(style).append(builder[0]);
         return Optional.empty();
      }, Style.EMPTY);
      return builder[0];
   }

   public static Component trim(FormattedText text) {
      String string = text.getString();
      int start = 0;
      int end = string.length();

      while (start < end && string.charAt(start) <= ' ') {
         start++;
      }

      while (start < end && string.charAt(end - 1) <= ' ') {
         end--;
      }

      return substring(text, start, end);
   }

   public static List<Component> formatInBounds(FormattedText text, Font font, int maxWidth, boolean wrap) {
      if (maxWidth <= 0) {
         return splitText(text, "\n");
      }

      if (wrap) {
         List<FormattedCharSequence> warped = wrapText(text, maxWidth, font);
         List<Component> newList = new ArrayList<>();

         for (FormattedCharSequence charSequence : warped) {
            newList.add(charSequenceToText(charSequence));
         }

         return newList;
      } else {
         List<Component> list = splitText(text, "\n");
         List<Component> newList = new ArrayList<>();

         for (Component component : list) {
            newList.add(formattedTextToText(font.substrByWidth(component, maxWidth)));
         }

         return newList;
      }
   }

   public static Style componentStyleAtWidth(Font font, Component text, int pos) {







       return text.getStyle();
   }

   public enum Alignment {
      LEFT((font, component) -> 0, i -> 0),
      RIGHT((font, component) -> font.width(component), i -> i),
      CENTER((font, component) -> font.width(component) / 2, i -> i / 2);

      private final BiFunction<Font, FormattedText, Integer> textFunction;
      private final Function<Integer, Integer> integerFunction;

      Alignment(BiFunction<Font, FormattedText, Integer> textFunction, Function<Integer, Integer> integerFunction) {
         this.textFunction = textFunction;
         this.integerFunction = integerFunction;
      }

      public int apply(Font font, FormattedText component) {
         return this.textFunction.apply(font, component);
      }

      public int apply(int width) {
         return this.integerFunction.apply(width);
      }

      public TextAlignment toVanilla() {
         return switch (this) {
            case LEFT -> TextAlignment.LEFT;
            case RIGHT -> TextAlignment.RIGHT;
            case CENTER -> TextAlignment.CENTER;
         };
      }
   }

   public record FiguraClickEvent(Runnable onClick) implements ClickEvent {
      @NotNull
      public Action action() {
         return Action.SUGGEST_COMMAND;
      }
   }
}
