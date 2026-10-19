package org.figuramc.figura.permissions;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;

public class Permissions {
   public static final Permissions INIT_INST = new Permissions("INIT_INST", 0, 524287, 0, 32768, 65536, 262144, Integer.MAX_VALUE);
   public static final Permissions WORLD_TICK_INST = new Permissions("WORLD_TICK_INST", 0, 32767, 0, 64, 128, 256, Integer.MAX_VALUE);
   public static final Permissions TICK_INST = new Permissions("TICK_INST", 0, 65535, 0, 4096, 8192, 32768, Integer.MAX_VALUE);
   public static final Permissions WORLD_RENDER_INST = new Permissions("WORLD_RENDER_INST", 0, 32767, 0, 32, 64, 256, Integer.MAX_VALUE);
   public static final Permissions RENDER_INST = new Permissions("RENDER_INST", 0, 65535, 0, 4096, 8192, 32768, Integer.MAX_VALUE);
   public static final Permissions COMPLEXITY = new Permissions("COMPLEXITY", 0, 8191, 0, 512, 2048, 4096, Integer.MAX_VALUE);
   public static final Permissions PARTICLES = new Permissions("PARTICLES", 0, 127, 0, 20, 50, 100, Integer.MAX_VALUE);
   public static final Permissions SOUNDS = new Permissions("SOUNDS", 0, 127, 0, 5, 20, 100, Integer.MAX_VALUE);
   public static final Permissions VOLUME = new Permissions("VOLUME", 0, 99, 0, 100, 100, 100, 100) {
      @Override
      public boolean checkInfinity(int value) {
         return false;
      }
   };
   public static final Permissions BB_ANIMATIONS = new Permissions("BB_ANIMATIONS", 0, 511, 0, 32, 128, 256, Integer.MAX_VALUE);
   public static final Permissions ANIMATION_INST = new Permissions("ANIMATION_INST", 0, 32767, 0, 2048, 4096, 8192, Integer.MAX_VALUE);
   public static final Permissions TEXTURE_SIZE = new Permissions("TEXTURE_SIZE", 0, 2048, 64, 0, 128, 512, 2048, 2048);
   public static final Permissions VANILLA_MODEL_EDIT = new Permissions("VANILLA_MODEL_EDIT", 0, 0, 1, 1, 1);
   public static final Permissions NAMEPLATE_EDIT = new Permissions("NAMEPLATE_EDIT", 0, 0, 0, 1, 1);
   public static final Permissions OFFSCREEN_RENDERING = new Permissions("OFFSCREEN_RENDERING", 0, 0, 0, 1, 1);
   public static final Permissions CUSTOM_SOUNDS = new Permissions("CUSTOM_SOUNDS", 0, 0, 1, 1, 1);
   public static final Permissions CANCEL_SOUNDS = new Permissions("CANCEL_SOUNDS", 0, 0, 0, 1, 1);
   public static final Permissions CUSTOM_SKULL = new Permissions("CUSTOM_SKULL", 0, 0, 1, 1, 1);
   public static final Permissions BUFFER_SIZE = new Permissions("BUFFER_SIZE", 0, 3072000, 0, 128000, 1024000, 2048000, Integer.MAX_VALUE);
   public static final Permissions BUFFERS_COUNT = new Permissions("BUFFERS_COUNT", 0, 32, 0, 2, 4, 16, 32);
   public static final Permissions NETWORKING = new Permissions("NETWORKING", 0, 0, 0, 1, 1);
   public static final List<Permissions> DEFAULT = List.of(
      INIT_INST,
      WORLD_TICK_INST,
      TICK_INST,
      WORLD_RENDER_INST,
      RENDER_INST,
      COMPLEXITY,
      PARTICLES,
      SOUNDS,
      VOLUME,
      BB_ANIMATIONS,
      ANIMATION_INST,
      TEXTURE_SIZE,
      VANILLA_MODEL_EDIT,
      NAMEPLATE_EDIT,
      OFFSCREEN_RENDERING,
      CUSTOM_SOUNDS,
      CANCEL_SOUNDS,
      CUSTOM_SKULL,
      BUFFER_SIZE,
      BUFFERS_COUNT,
      NETWORKING
   );
   public final String name;
   private final List<Integer> defaults;
   public final boolean isToggle;
   public final Integer min;
   public final Integer max;
   public final int stepSize;

   public Permissions(String name, int blocked, int low, int def, int high, int max) {
      this(name, null, null, blocked, low, def, high, max);
   }

   public Permissions(String name, Integer sliderMin, Integer sliderMax, int blocked, int low, int def, int high, int max) {
      this(name, sliderMin, sliderMax, 1, blocked, low, def, high, max);
   }

   public Permissions(String name, Integer sliderMin, Integer sliderMax, int stepSize, int blocked, int low, int def, int high, int max) {
      this.name = name;
      this.isToggle = sliderMin == null || sliderMax == null;
      this.min = sliderMin;
      this.max = sliderMax;
      this.stepSize = stepSize;
      this.defaults = List.of(blocked, low, def, high, max);
   }

   public boolean checkInfinity(int value) {
      return this.max != null && value > this.max;
   }

   public boolean showSteps() {
      return this.stepSize > 1;
   }

   public boolean asBoolean(int value) {
      return value >= 1;
   }

   public int getDefault(Permissions.Category category) {
      return category.index >= 0 && category.index < this.defaults.size() ? this.defaults.get(category.index) : -1;
   }

   public enum Category {
      BLOCKED(0, ChatFormatting.RED),
      LOW(1, ChatFormatting.YELLOW),
      DEFAULT(2, ChatFormatting.WHITE),
      HIGH(3, ChatFormatting.GREEN),
      MAX(4, ColorUtils.Colors.LUA_PING);

      public final int index;
      public final int color;
      public final MutableComponent text;
      public final MutableComponent info;

      Category(int index, ColorUtils.Colors color) {
         this(index, color.hex, color.style);
      }

      Category(int index, ChatFormatting formatting) {
         this(index, getFormattingColor(formatting), Style.EMPTY.applyFormat(formatting));
      }

      Category(int index, int color, Style style) {
         this.index = index;
         this.color = color;
         String name = "permissions.category." + this.name().toLowerCase(Locale.US);
         this.text = FiguraText.of(name).withStyle(style);
         this.info = FiguraText.of(name + ".info");
      }

      private static int getFormattingColor(ChatFormatting formatting) {
         TextColor textColor = TextColor.fromLegacyFormat(formatting);
         return textColor == null ? 0xFFFFFF : textColor.getValue();
      }

      public static Permissions.Category indexOf(int i) {
         for (Permissions.Category value : values()) {
            if (value.index == i) {
               return value;
            }
         }

         return null;
      }
   }
}
