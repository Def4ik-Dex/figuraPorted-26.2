package org.figuramc.figura.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.figuramc.figura.utils.PlatformUtils;

public class ImmediatelyFastCompat {
   public static float getFontWidthIMF() {
      if (PlatformUtils.isModLoaded("immediatelyfast")) {
         String modVersion = PlatformUtils.getModVersion("immediatelyfast");
         if (PlatformUtils.compareVersionTo(modVersion, "1.2.0") >= 0) {
            try {
               Class<?> modClass = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
               Field configField = modClass.getDeclaredField("config");
               Object config = configField.get(null);
               Method getBoolean = config.getClass().getMethod("getBoolean", String.class, boolean.class);
               return (Boolean)getBoolean.invoke(config, "font_atlas_resizing", false) ? 2048.0F : 256.0F;
            } catch (ReflectiveOperationException var7) {
            }
         } else if (PlatformUtils.compareVersionTo(modVersion, "1.1.17") >= 0) {
            try {
               Class<?> modClass = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
               Field configField = modClass.getDeclaredField("runtimeConfig");
               Class<?> configClass = Class.forName("net.raphimc.immediatelyfast.feature.core.ImmediatelyFastRuntimeConfig");
               Field font_atlas_resizing = configClass.getDeclaredField("font_atlas_resizing");
               return font_atlas_resizing.getBoolean(configField.get(null)) ? 2048.0F : 256.0F;
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException var6) {
            }
         } else {
            try {
               Class<?> modClass = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
               Field configField = modClass.getDeclaredField("config");
               Class<?> configClass = Class.forName("net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig");
               Field font_atlas_resizing = configClass.getDeclaredField("font_atlas_resizing");
               return font_atlas_resizing.getBoolean(configField.get(null)) ? 2048.0F : 256.0F;
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException var5) {
            }
         }
      }

      return 256.0F;
   }
}
