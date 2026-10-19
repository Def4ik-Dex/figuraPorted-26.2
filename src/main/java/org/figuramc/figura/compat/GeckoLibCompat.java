package org.figuramc.figura.compat;

import java.util.Map;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.compat.wrappers.ClassWrapper;
import org.figuramc.figura.compat.wrappers.FieldWrapper;
import org.figuramc.figura.compat.wrappers.MethodWrapper;

public class GeckoLibCompat {
   private static ClassWrapper GLRenderUtil;
   private static MethodWrapper getGeoModelForArmor;
   private static ClassWrapper GLGeoArmorRenderer;
   private static FieldWrapper renderers;

   public static void init() {
      GLRenderUtil = new ClassWrapper("com.geckolib.util.RenderUtil");
      getGeoModelForArmor = GLRenderUtil.getMethod("getGeoModelForArmor", ItemStack.class, EquipmentSlot.class, LayerType.class);
      GLGeoArmorRenderer = new ClassWrapper("com.geckolib3.renderers.geo.GeoArmorRenderer");
      renderers = GLGeoArmorRenderer.getField("renderers");
   }

   public static boolean armorHasCustomModel(ItemStack stack, EquipmentSlot slot, LayerType layerType) {
      if (GLRenderUtil.isLoaded) {
         return getGeoModelForArmor.exists() ? getGeoModelForArmor.invoke(null, stack, slot, layerType) != null : false;
      }

      if (GLGeoArmorRenderer.isLoaded) {
         if (renderers.exists()) {
            if (renderers.getValue(null) instanceof Map<?, ?> map) {
               return map.containsKey(stack.getItem().getClass());
            }

            renderers.markErrored();
         }

         return false;
      } else {
         return false;
      }
   }
}
