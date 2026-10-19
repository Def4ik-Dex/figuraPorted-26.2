package org.figuramc.figura.config;

import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.fabric.ConfigKeyBindImpl;

public class ConfigKeyBind extends KeyMapping {
   private final ConfigType.KeybindConfig config;

   public ConfigKeyBind(String translationKey, Key key, ConfigType.KeybindConfig config) {
      super(translationKey, key.getType(), key.getValue(), FiguraMod.FIGURA_KEY_CATEGORY);
      this.config = config;
      if (FiguraMod.debugModeEnabled() || !config.disabled) {
         addKeyBind(this);
      }
   }

   public void setKey(Key boundKey) {
      super.setKey(boundKey);
      this.config.value = this.config.tempValue = this.saveString();
      ConfigManager.saveConfig();
   }

   public void saveConfigChanges() {
      Options options = Minecraft.getInstance().options;
      if (options != null) {
         options.save();
      }

      KeyMapping.resetMapping();
   }

   public static void addKeyBind(ConfigKeyBind keyBind) {
      ConfigKeyBindImpl.addKeyBind(keyBind);
   }
}
