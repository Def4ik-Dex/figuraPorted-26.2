package org.figuramc.figura.mixin.compat;

import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(value = GeoRenderer.class, remap = false)
public interface GeckolibGeoRendererMixin<R extends GeoRenderState> {
}
