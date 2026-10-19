package org.figuramc.figura.mixin.render.layers;

import java.util.function.Function;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.LayerTextureKey;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.TrimSpriteKey;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo.Layer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EquipmentLayerRenderer.class)
public interface EquipmentLayerRendererAccessor {
   @Accessor("equipmentAssets")
   EquipmentAssetManager figura$getAssetsManager();

   @Accessor("layerTextureLookup")
   Function<LayerTextureKey, Identifier> layerTextureLookup();

   @Accessor("trimSpriteLookup")
   Function<TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup();

   @Invoker("getColorForLayer")
   static int getColorForLayer(Layer layer, int i) {
      throw new AssertionError();
   }
}
