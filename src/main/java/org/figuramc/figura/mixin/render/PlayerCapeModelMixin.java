package org.figuramc.figura.mixin.render;

import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.figuramc.figura.ducks.PlayerModelCapeAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerCapeModel.class)
public abstract class PlayerCapeModelMixin<T extends HumanoidRenderState> extends HumanoidModel<T> implements PlayerModelCapeAccessor {
   @Unique
   public ModelPart fakeCloak = new ModelPart(List.of(), Map.of());
   @Final
   @Shadow
   private ModelPart cape;

   public PlayerCapeModelMixin(ModelPart modelPart) {
      super(modelPart);
   }

   @Override
   public ModelPart figura$getCloak() {
      return this.cape;
   }

   @Override
   public ModelPart figura$getFakeCloak() {
      return this.fakeCloak;
   }
}
