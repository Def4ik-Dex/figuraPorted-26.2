package org.figuramc.figura.utils;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;

public class ColorUtils {
   public static int[] split(int color, int len) {
      int[] array = new int[len];

      for (int i = 0; i < len; i++) {
         int shift = len * 8 - (i + 1) * 8;
         array[i] = color >> shift & 0xFF;
      }

      return array;
   }

   public static int rgbToInt(FiguraVec3 rgb) {
      int hex = (int)(rgb.x * 255.0);
      hex = (hex << 8) + (int)(rgb.y * 255.0);
      return (hex << 8) + (int)(rgb.z * 255.0);
   }

   public static FiguraVec3 intToRGB(int color) {
      int[] rgb = split(color, 3);
      return FiguraVec3.of(rgb[0] / 255.0F, rgb[1] / 255.0F, rgb[2] / 255.0F);
   }

    public static FiguraVec3 userInputHex(String hex, FiguraVec3 fallback) {
        ColorUtils.Colors color = ColorUtils.Colors.getColor(hex);
        if (color != null) {
            return color.vec;
        }

        try {
            ChatFormatting formatting = ChatFormatting.valueOf(hex.toUpperCase(Locale.US));
            TextColor textColor = TextColor.fromLegacyFormat(formatting);
            if (textColor != null) {
                return intToRGB(textColor.getValue());
            }
        } catch (Exception ignored) {
        }

        return hexStringToRGB(hex, fallback);
    }



















   public static FiguraVec3 userInputHex(String hex) {
      return userInputHex(hex, ColorUtils.Colors.DEFAULT.vec);
   }

   public static FiguraVec3 hexStringToRGB(String string, FiguraVec3 fallback) {
      if (string != null && !string.isBlank()) {
         StringBuilder hex = new StringBuilder(string);
         if (hex.toString().startsWith("#")) {
            hex = new StringBuilder(hex.substring(1));
         }

         if (hex.length() == 3) {
            char[] bgChar = hex.toString().toCharArray();
            hex = new StringBuilder("" + bgChar[0] + bgChar[0] + bgChar[1] + bgChar[1] + bgChar[2] + bgChar[2]);
         } else {
            hex.append("0".repeat(Math.max(6 - hex.length(), 0)));
         }

         try {
            return intToRGB(Integer.parseInt(hex.substring(0, 6), 16));
         } catch (Exception ignored) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   public static FiguraVec3 hsvToRGB(FiguraVec3 hsv) {
      int hex = Color.HSBtoRGB((float)hsv.x, (float)hsv.y, (float)hsv.z);
      return intToRGB(hex);
   }

   public static FiguraVec3 rgbToHSV(FiguraVec3 rgb) {
      float[] hsv = Color.RGBtoHSB((int)(rgb.x * 255.0), (int)(rgb.y * 255.0), (int)(rgb.z * 255.0), null);
      return FiguraVec3.of(hsv[0], hsv[1], hsv[2]);
   }

   public static String rgbToHex(FiguraVec3 rgb) {
      String color = Integer.toHexString(rgbToInt(rgb));
      return "0".repeat(Math.max(6 - color.length(), 0)) + color;
   }

   public static int rgbaToIntABGR(FiguraVec4 rgba) {
      int hex = (int)(rgba.w * 255.0);
      hex = (hex << 8) + (int)(rgba.z * 255.0);
      hex = (hex << 8) + (int)(rgba.y * 255.0);
      return (hex << 8) + (int)(rgba.x * 255.0);
   }

   public static int rgbaToIntARGB(FiguraVec4 rgba) {
      int hex = (int)(rgba.w * 255.0);
      hex = (hex << 8) + (int)(rgba.x * 255.0);
      hex = (hex << 8) + (int)(rgba.y * 255.0);
      return (hex << 8) + (int)(rgba.z * 255.0);
   }

   public static int intRGBAToIntARGB(int hexRGBA) {
      int green = hexRGBA >> 16 & 0xFF;
      int blue = hexRGBA >> 8 & 0xFF;
      int alpha = hexRGBA & 0xFF;
      int red = hexRGBA >> 24 & 0xFF;
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   public static int rgbaToInt(FiguraVec4 rgba) {
      int hex = (int)(rgba.x * 255.0);
      hex = (hex << 8) + (int)(rgba.y * 255.0);
      hex = (hex << 8) + (int)(rgba.z * 255.0);
      return (hex << 8) + (int)(rgba.w * 255.0);
   }

   public static FiguraVec4 abgrToRGBA(int color) {
      int[] rgb = split(color, 4);
      return FiguraVec4.of(rgb[3] / 255.0F, rgb[2] / 255.0F, rgb[1] / 255.0F, rgb[0] / 255.0F);
   }

   public static FiguraVec4 intToRGBA(int color) {
      int[] rgb = split(color, 4);
      return FiguraVec4.of(rgb[0] / 255.0F, rgb[1] / 255.0F, rgb[2] / 255.0F, rgb[3] / 255.0F);
   }

   public static FiguraVec4 intToARGB(int color) {
      int[] rgb = split(color, 4);
      return FiguraVec4.of(rgb[3] / 255.0F, rgb[0] / 255.0F, rgb[1] / 255.0F, rgb[2] / 255.0F);
   }

   public enum Colors {
      AWESOME_BLUE(6202879),
      PURPLE(10908399),
      FIGURA_BLUE(61695),
      SOFT_BLUE(10075118),
      FIGURA_RED(16720896),
      ORANGE(16761856),
      CHEESE(16303418),
      LUA_LOG(5592575),
      LUA_ERROR(16733525),
      LUA_PING(10573274),
      DEFAULT(5942015),
      DISCORD(5793266),
      KOFI(2599648),
      GITHUB(16777215),
      MODRINTH(1825130),
      CURSEFORGE(15819830);

      public final int hex;
      public final FiguraVec3 vec;
      public final Style style;

      Colors(int hex) {
         this.hex = hex;
         this.vec = ColorUtils.intToRGB(hex);
         this.style = Style.EMPTY.withColor(hex);
      }

      public static ColorUtils.Colors getColor(String s) {
         if (s == null) {
            return null;
         }

         for (ColorUtils.Colors value : values()) {
            if (s.equalsIgnoreCase(value.name())) {
               return value;
            }
         }

         return null;
      }

      public static ColorUtils.Colors random() {
         ColorUtils.Colors[] colors = values();
         return colors[(int)(Math.random() * colors.length)];
      }
   }
}
