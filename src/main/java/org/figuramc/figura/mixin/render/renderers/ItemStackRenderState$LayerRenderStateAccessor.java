package org.figuramc.figura.mixin.render.renderers;

import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LayerRenderState.class)
public interface ItemStackRenderState$LayerRenderStateAccessor {
   @Accessor("quads")
   List<BakedQuad> figura$getQuads();

   @Accessor("itemTransform")
   ItemTransform figura$getTransform();
}
