package org.figuramc.figura.mixin.render;

import java.util.List;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AtlasManager.class)
public interface AtlasManagerAccessor {
   @Accessor("KNOWN_ATLASES")
   static List<AtlasConfig> getVanillaAtlases() {
      throw new AssertionError();
   }
}
