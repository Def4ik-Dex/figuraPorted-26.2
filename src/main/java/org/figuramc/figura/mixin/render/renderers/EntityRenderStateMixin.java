package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements FiguraEntityRenderStateExtension {
   @Unique
   Integer figura$entityId = null;
   @Unique
   float figura$tickDelta = 1.0F;
   @Unique
   float figura$previewAlpha = 1.0F;

   @Override
   public Integer figura$getEntityId() {
      return this.figura$entityId;
   }

   @Override
   public void figura$setEntityId(int id) {
      this.figura$entityId = id;
   }

   @Override
   public float figura$getTickDelta() {
      return this.figura$tickDelta;
   }

   @Override
   public void figura$setTickDelta(float tickDelta) {
      this.figura$tickDelta = tickDelta;
   }

   @Override
   public float figura$getPreviewAlpha() {
      return this.figura$previewAlpha;
   }

   @Override
   public void figura$setPreviewAlpha(float alpha) {
      this.figura$previewAlpha = alpha;
   }
}
