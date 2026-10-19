package org.figuramc.figura.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.screens.NetworkFilterScreen;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;

public abstract class ConfigType<T> {
   public final String id;
   public final boolean hidden;
   public Component name;
   public Component tooltip;
   public T value;
   public T tempValue;
   public final T defaultValue;
   public boolean disabled;

   public ConfigType(String name, T value) {
      this(name, value, false);
   }

   public ConfigType(String name, T value, boolean hidden) {
      this.id = name;
      this.hidden = hidden;
      name = "config." + name;
      this.name = FiguraText.of(name);
      this.tooltip = FiguraText.of(name + ".tooltip");
      this.value = this.defaultValue = this.tempValue = value;
   }

   public abstract T parseValue(String var1);

   public void setValue(String newVal) {
      boolean change = !this.value.equals(this.tempValue);

      try {
         this.value = this.parseValue(newVal);
      } catch (Exception e) {
         FiguraMod.LOGGER.warn("Failed to set this config (" + this.id + ") value \"" + this.value + "\", restoring it to default", e);
         this.value = this.defaultValue;
         change = true;
      }

      this.tempValue = this.value;
      if (change) {
         try {
            this.onChange();
         } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to run onChange for config \"" + this.id + "\"", e);
         }
      }
   }

   public void setTempValue(String newVal) {
      this.tempValue = this.parseValue(newVal);
   }

   public void discardConfig() {
      this.tempValue = this.value;
   }

   public void setDefault() {
      this.value = this.defaultValue;
   }

   public void resetTemp() {
      this.tempValue = this.defaultValue;
   }

   public boolean isDefault() {
      return this.defaultValue == null || this.defaultValue.equals(this.tempValue);
   }

   public void onChange() {
   }

   public static class BoolConfig extends ConfigType.ParentedConfig<Boolean> {
      public BoolConfig(String name, ConfigType.Category category, Boolean defaultValue, boolean hidden) {
         super(name, category, defaultValue, hidden);
      }

      public BoolConfig(String name, ConfigType.Category category, Boolean defaultValue) {
         this(name, category, defaultValue, false);
      }

      public Boolean parseValue(String newVal) {
         return Boolean.valueOf(newVal);
      }
   }

   public static class ButtonConfig extends ConfigType.ParentedConfig<Void> {
      public final Runnable toRun;

      public ButtonConfig(String name, ConfigType.Category category, Runnable toRun) {
         super(name, category, null);
         this.toRun = toRun;
      }

      public Void parseValue(String newVal) {
         return null;
      }

      @Override
      public void setValue(String newVal) {
      }
   }

   public static class Category extends ConfigType<Void> {
      public final List<ConfigType<?>> children = new ArrayList<>();

      public Category(String name) {
         super(name, null);
         ConfigManager.CATEGORIES_REGISTRY.put(name, this);
      }

      public Void parseValue(String newVal) {
         return null;
      }

      @Override
      public void setValue(String newVal) {
      }
   }

   public static class ColorConfig extends ConfigType.InputConfig<Integer> {
      public ColorConfig(String name, ConfigType.Category category, int defaultValue) {
         super(name, category, defaultValue, InputType.HEX_COLOR);
      }

      public Integer parseValue(String newVal) {
         return Integer.parseInt(newVal);
      }

      @Override
      public void setTempValue(String newVal) {
         this.tempValue = ColorUtils.rgbToInt(ColorUtils.userInputHex(newVal, FiguraVec3.of()));
      }
   }

   public static class EnumConfig extends ConfigType.ParentedConfig<Integer> {
      public List<Component> enumList;
      public List<Component> enumTooltip;

      public EnumConfig(String name, ConfigType.Category category, int defaultValue, int length) {
         super(name, category, defaultValue);
         name = "config." + name;
         ArrayList<Component> enumList = new ArrayList<>();
         ArrayList<Component> enumTooltip = new ArrayList<>();

         for (int i = 1; i <= length; i++) {
            enumList.add(FiguraText.of(name + "." + i));
            enumTooltip.add(FiguraText.of(name + "." + i + ".tooltip"));
         }

         this.enumList = enumList;
         this.enumTooltip = enumTooltip;
      }

      public Integer parseValue(String newVal) {
         return Math.floorMod(Integer.parseInt(newVal), this.enumList.size());
      }
   }

   public static class FloatConfig extends ConfigType.InputConfig<Float> {
      public FloatConfig(String name, ConfigType.Category category, float defaultValue) {
         super(name, category, defaultValue, InputType.FLOAT);
      }

      public Float parseValue(String newVal) {
         return Float.parseFloat(newVal);
      }
   }

   public static class FolderConfig extends ConfigType.InputConfig<String> {
      public FolderConfig(String name, ConfigType.Category category, String defaultValue) {
         super(name, category, defaultValue, InputType.FOLDER_PATH);
      }

      public String parseValue(String newVal) {
         return newVal;
      }
   }

   public static class IPConfig extends ConfigType.InputConfig<String> {
      public IPConfig(String name, ConfigType.Category category, String defaultValue) {
         super(name, category, defaultValue, InputType.IP);
      }

      public String parseValue(String newVal) {
         return newVal;
      }
   }

   public abstract static class InputConfig<T> extends ConfigType.ParentedConfig<T> {
      public final InputType inputType;

      public InputConfig(String name, ConfigType.Category category, T defaultValue, InputType inputType) {
         super(name, category, defaultValue);
         this.inputType = inputType;
      }
   }

   public static class IntConfig extends ConfigType.InputConfig<Integer> {
      public IntConfig(String name, ConfigType.Category category, int defaultValue) {
         super(name, category, defaultValue, InputType.INT);
      }

      public Integer parseValue(String newVal) {
         return Integer.parseInt(newVal);
      }
   }

   public static class KeybindConfig extends ConfigType.ParentedConfig<String> {
      public ConfigKeyBind keyBind;

      public KeybindConfig(String name, ConfigType.Category category, String defaultValue) {
         super(name, category, defaultValue);
         this.keyBind = new ConfigKeyBind(this.name.getString(), InputConstants.getKey(defaultValue), this);
      }

      public String parseValue(String newVal) {
         return newVal;
      }
   }

   public static class NetworkFilterConfig extends ConfigType.ButtonConfig implements ConfigType.SerializableConfig {
      private final ArrayList<NetworkingAPI.Filter> filters = new ArrayList<>();

      public NetworkFilterConfig(String name, ConfigType.Category category) {
         super(name, category, () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.setScreen(new NetworkFilterScreen(mc.gui.screen()));
         });
      }

      public ArrayList<NetworkingAPI.Filter> getFilters() {
         return this.filters;
      }

      @Override
      public JsonElement serialize() {
         JsonArray array = new JsonArray();

         for (NetworkingAPI.Filter filter : this.filters) {
            JsonObject o = new JsonObject();
            o.addProperty("source", filter.getSource());
            array.add(o);
         }

         return array;
      }

      @Override
      public void deserialize(JsonElement element) {
         this.filters.clear();
         if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
               if (e.isJsonObject()) {
                  JsonObject o = e.getAsJsonObject();
                  JsonElement s = o.get("source");
                  if (s != null && s.isJsonPrimitive()) {
                     JsonPrimitive source = s.getAsJsonPrimitive();
                     if (source.isString()) {
                        this.filters.add(new NetworkingAPI.Filter(source.getAsString()));
                     }
                  }
               }
            }
         }
      }

      @Override
      public void setDefault() {
         this.filters.clear();
      }

      @Override
      public boolean isDefault() {
         return this.filters.size() == 0;
      }
   }

   public abstract static class ParentedConfig<T> extends ConfigType<T> {
      public final ConfigType.Category parent;

      public ParentedConfig(String name, ConfigType.Category category, T value, boolean hidden) {
         super(name, value, hidden);
         this.parent = category;
         category.children.add(this);
         ConfigManager.REGISTRY.add(this);
      }

      public ParentedConfig(String name, ConfigType.Category category, T value) {
         this(name, category, value, false);
      }
   }

   public static class PositiveFloatConfig extends ConfigType.InputConfig<Float> {
      public PositiveFloatConfig(String name, ConfigType.Category category, float defaultValue) {
         super(name, category, defaultValue, InputType.POSITIVE_FLOAT);
      }

      public Float parseValue(String newVal) {
         return Float.parseFloat(newVal);
      }
   }

   public static class PositiveIntConfig extends ConfigType.InputConfig<Integer> {
      public PositiveIntConfig(String name, ConfigType.Category category, int defaultValue) {
         super(name, category, defaultValue, InputType.POSITIVE_INT);
      }

      public Integer parseValue(String newVal) {
         return Integer.parseInt(newVal);
      }
   }

   public interface SerializableConfig {
      JsonElement serialize();

      void deserialize(JsonElement var1);
   }

   public static class StringConfig extends ConfigType.InputConfig<String> {
      public StringConfig(String name, ConfigType.Category category, String defaultValue) {
         super(name, category, defaultValue, InputType.ANY);
      }

      public String parseValue(String newVal) {
         return newVal;
      }
   }
}
