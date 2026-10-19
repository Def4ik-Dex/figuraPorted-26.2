package org.figuramc.figura.mixin.render.renderers;

import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements FiguraItemStackRenderStateExtension {
   @Shadow
   ItemDisplayContext displayContext;
   @Shadow
   private LayerRenderState[] layers;
   @Unique
   ItemStack figura$itemStack;

   @Override
   public void figura$setItemStack(@Nullable ItemStack itemStack) {
      this.figura$itemStack = itemStack;
   }

   @Override
   public ItemStack figura$getItemStack() {
      return this.figura$itemStack;
   }

   @Override
   public boolean figura$isLeftHanded() {
      return this.displayContext.leftHand();
   }

   @Override
   public ItemDisplayContext figura$getDisplayContext() {
      return this.displayContext;
   }

   @Override
   public ItemTransform figura$getItemTransform() {
      for (LayerRenderState layerRenderState : this.layers) {
         if (((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getTransform() != null) {
            return ((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getTransform();
         }
      }

      return ItemTransform.NO_TRANSFORM;
   }

   @Override
   public List<BakedQuad> figura$getQuads() {
      for (LayerRenderState layerRenderState : this.layers) {
         if (((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getQuads() != null) {
            return ((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getQuads();
         }
      }

      return List.of();
   }

}
