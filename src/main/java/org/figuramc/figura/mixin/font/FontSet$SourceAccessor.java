package org.figuramc.figura.mixin.font;

import net.minecraft.client.gui.font.FontSet.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Source.class)
public interface FontSet$SourceAccessor {
   @Accessor("filterFishyGlyphs")
   boolean figura$shouldFilterFishyGlyphs();
}
