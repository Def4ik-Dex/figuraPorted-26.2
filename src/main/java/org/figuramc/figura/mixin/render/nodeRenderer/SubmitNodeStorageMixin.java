package org.figuramc.figura.mixin.render.nodeRenderer;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SubmitNodeStorage.class)
public class SubmitNodeStorageMixin implements NodeCollectorExtension {
   @Override
   public <S extends EntityRenderState> void submitFiguraModel(Avatar avatar, S renderState, TriFunction<Avatar, S, net.minecraft.client.renderer.SubmitNodeCollector, Void> renderer) {
      renderer.apply(avatar, renderState, (SubmitNodeCollector)(Object)this);
   }
}
