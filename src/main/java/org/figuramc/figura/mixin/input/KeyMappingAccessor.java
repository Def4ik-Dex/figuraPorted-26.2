package org.figuramc.figura.mixin.input;

import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
   @Accessor("ALL")
   static Map<String, KeyMapping> getAll() {
      throw new AssertionError();
   }
}
