package org.figuramc.figura.mixin.render.layers.elytra;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.LayerTextureKey;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer.TrimSpriteKey;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo.Layer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.mixin.render.layers.EquipmentLayerRendererAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.PlatformUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity, S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
   @Shadow
   @Final
   private EquipmentLayerRenderer equipmentRenderer;
   @Unique
   private VanillaPart vanillaPart;
   @Unique
   private Avatar figura$avatar;
   @Unique
   private boolean renderedPivot;

   public ElytraLayerMixin(RenderLayerParent<S, M> context) {
      super(context);
   }

   @Inject(
      at = @At("HEAD"),
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"
   )
   public void setAvatar(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
      this.figura$avatar = AvatarManager.getAvatar(humanoidRenderState);
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
         shift = Shift.BEFORE
      ),
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"
   )
   public void onRender(
      PoseStack pose,
      SubmitNodeCollector submitNodeCollector,
      int light,
      S humanoidRenderState,
      float f,
      float g,
      CallbackInfo ci,
      @Local ElytraModel elytraModel
   ) {
      this.vanillaPart = null;
      if (this.figura$avatar != null) {
         FiguraSubmitCallBackExtension submitCallBackExtension = (FiguraSubmitCallBackExtension)elytraModel;
         NodeCollectorExtension nodeCollectorExtension = (NodeCollectorExtension)submitNodeCollector;
         nodeCollectorExtension.submitFiguraModel(
            this.figura$avatar,
            humanoidRenderState,
            (avatarx, renderState, multiBufferSource) -> {
               if (avatarx.luaRuntime != null) {
                  VanillaPart part = avatarx.luaRuntime.vanilla_model.ELYTRA;
                  part.save(elytraModel);
                  if (avatarx.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                     this.vanillaPart = part;
                     this.vanillaPart.preTransform(elytraModel);
                  }
               }

               Entity entity = AvatarManager.getEntity(humanoidRenderState);
               if (entity != null) {
                  avatarx.elytraRender(
                     entity, multiBufferSource, pose, light, ((FiguraEntityRenderStateExtension)humanoidRenderState).figura$getTickDelta(), elytraModel
                  );
               }

               if (this.vanillaPart != null) {
                  this.vanillaPart.restore(elytraModel);
               }

               return null;
            }
         );
         Avatar avatar = this.figura$avatar;
         submitCallBackExtension.figura$addPreRenderingCallback(poseStack -> {
            if (avatar.luaRuntime != null) {
               VanillaPart part = avatar.luaRuntime.vanilla_model.ELYTRA;
               part.save(elytraModel);
               if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                  this.vanillaPart = part;
                  this.vanillaPart.preTransform(elytraModel);
               }
            }

            if (this.vanillaPart != null) {
               this.vanillaPart.posTransform(elytraModel);
            }

            return true;
         });
         submitCallBackExtension.figura$addPostRenderingCallback(() -> {
            if (this.vanillaPart != null) {
               this.vanillaPart.restore(elytraModel);
            }
         });
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V"
      ),
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
      cancellable = true
   )
   public void cancelVanillaPart(
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int light,
      S humanoidRenderState,
      float f,
      float g,
      CallbackInfo ci,
      @Local ElytraModel elytraModel
   ) {
      this.renderedPivot = true;
      this.submitElytraPivot(humanoidRenderState, poseStack, submitNodeCollector, light, elytraModel);
      if (this.renderedPivot) {
         poseStack.popPose();
         ci.cancel();
      }
   }

   public void submitElytraPivot(S state, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, ElytraModel elytraModel) {
      ItemStack itemStack = state.chestEquipment;
      if (itemStack.is(Items.ELYTRA) || PlatformUtils.isModLoaded("origins")) {
         Avatar figura$Avatar = this.figura$avatar;
         if (figura$Avatar != null
            && figura$Avatar.luaRuntime != null
            && figura$Avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1
            && figura$Avatar.luaRuntime.vanilla_model.ELYTRA.checkVisible()) {
            Identifier playerTexture = RenderUtils.getPlayerSkinTexture((WingsLayer<?, ?>)(Object)this, state);
            VanillaPart part = RenderUtils.pivotToPart(figura$Avatar, ParentType.LeftElytraPivot);
            if (part != null && part.checkVisible()) {
               boolean leftWing = figura$Avatar.pivotPartRender(
                  ParentType.LeftElytraPivot,
                  stack -> {
                     stack.pushPose();
                     stack.scale(16.0F, 16.0F, 16.0F);
                     stack.mulPose(Axis.XP.rotationDegrees(180.0F));
                     stack.mulPose(Axis.YP.rotationDegrees(180.0F));
                     stack.translate(0.0F, 0.0F, 0.125F);
                     this.figura$submitElytraPart(
                        elytraModel,
                        state,
                        ((ElytraModelAccessor)elytraModel).getLeftWing(),
                        stack,
                        nodeCollector,
                        light,
                        state.outlineColor,
                        itemStack,
                        playerTexture
                     );
                     stack.popPose();
                  }
               );
               if (!leftWing) {
                  this.figura$submitElytraPart(
                     elytraModel,
                     state,
                     ((ElytraModelAccessor)elytraModel).getLeftWing(),
                     poseStack,
                     nodeCollector,
                     light,
                     state.outlineColor,
                     itemStack,
                     playerTexture
                  );
               }
            }

            part = RenderUtils.pivotToPart(figura$Avatar, ParentType.RightElytraPivot);
            if (part != null && part.checkVisible()) {
               boolean rightWing = figura$Avatar.pivotPartRender(
                  ParentType.RightElytraPivot,
                  stack -> {
                     stack.pushPose();
                     stack.scale(16.0F, 16.0F, 16.0F);
                     stack.mulPose(Axis.XP.rotationDegrees(180.0F));
                     stack.mulPose(Axis.YP.rotationDegrees(180.0F));
                     stack.translate(0.0F, 0.0F, 0.125F);
                     this.figura$submitElytraPart(
                        elytraModel,
                        state,
                        ((ElytraModelAccessor)elytraModel).getRightWing(),
                        stack,
                        nodeCollector,
                        light,
                        state.outlineColor,
                        itemStack,
                        playerTexture
                     );
                     stack.popPose();
                  }
               );
               if (!rightWing) {
                  this.figura$submitElytraPart(
                     elytraModel,
                     state,
                     ((ElytraModelAccessor)elytraModel).getRightWing(),
                     poseStack,
                     nodeCollector,
                     light,
                     state.outlineColor,
                     itemStack,
                     playerTexture
                  );
               }
            }
         } else {
            this.renderedPivot = figura$Avatar != null
               && figura$Avatar.luaRuntime != null
               && figura$Avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1
               && !figura$Avatar.luaRuntime.vanilla_model.ELYTRA.checkVisible();
         }
      }
   }

   @Unique
   private void figura$submitElytraPart(
      ElytraModel elytraModel,
      S state,
      ModelPart modelPart,
      PoseStack poseStack,
      SubmitNodeCollector nodeCollector,
      int light,
      int outlineColor,
      ItemStack itemStack,
      @Nullable Identifier playerLocation
   ) {
      boolean hasGlint = itemStack.hasFoil();
      LayerType layerType = LayerType.WINGS;
      Equippable equippable = (Equippable)itemStack.get(DataComponents.EQUIPPABLE);
      if (equippable != null) {
         Optional<ResourceKey<EquipmentAsset>> location = equippable.assetId();
         if (!location.isEmpty()) {
            List<Layer> list = ((EquipmentLayerRendererAccessor)this.equipmentRenderer).figura$getAssetsManager().get(location.get()).getLayers(layerType);
            int i = DyedItemColor.getOrDefault(itemStack, -6265536);
            int order = 0;

            for (Layer layer : list) {
               int k = EquipmentLayerRendererAccessor.getColorForLayer(layer, i);
               if (k != 0) {
                  Identifier normalArmorResource = layer.usePlayerTexture() && playerLocation != null
                     ? playerLocation
                     : ((EquipmentLayerRendererAccessor)this.equipmentRenderer).layerTextureLookup().apply(new LayerTextureKey(layerType, layer));
                  ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPreRenderingCallback(stack -> {
                     elytraModel.setupAnim(state);
                     return true;
                  });
                  nodeCollector.order(order++)
                     .submitModelPart(
                        modelPart, poseStack, RenderTypes.armorCutoutNoCull(normalArmorResource), light, OverlayTexture.NO_OVERLAY, null, -1, null
                     );
                  if (hasGlint) {
                     ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPreRenderingCallback(stack -> {
                        elytraModel.setupAnim(state);
                        return true;
                     });
                     nodeCollector.order(order++)
                        .submitModelPart(modelPart, poseStack, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null, -1, null);
                  }

                  hasGlint = false;
               }
            }

            ArmorTrim trim = (ArmorTrim)itemStack.get(DataComponents.TRIM);
            if (trim != null) {
               TextureAtlasSprite textureAtlasSprite = ((EquipmentLayerRendererAccessor)this.equipmentRenderer)
                  .trimSpriteLookup()
                  .apply(new TrimSpriteKey(trim, layerType, location.get()));
               RenderType renderType = Sheets.armorTrimsSheet(((TrimPattern)trim.pattern().value()).decal());
               ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPreRenderingCallback(stack -> {
                  elytraModel.setupAnim(state);
                  return true;
               });
               nodeCollector.order(order).submitModelPart(modelPart, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, textureAtlasSprite, -1, null);
            }
         }
      }
   }
}
