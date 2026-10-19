package org.figuramc.figura.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.compat.GeckoLibCompat;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.mixin.render.layers.EquipmentLayerRendererAccessor;
import org.figuramc.figura.mixin.render.layers.HumanoidArmorLayerAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.FiguraArmorPartRenderer;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HumanoidArmorLayer.class, priority = 900)
public abstract class HumanoidArmorLayerMixinFabric<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>>
   extends RenderLayer<S, M>
   implements HumanoidArmorLayerAccessor<S, M, A> {
   @Shadow
   @Final
   private EquipmentLayerRenderer equipmentRenderer;
   @Unique
   private boolean figura$renderingVanillaArmor;
   @Unique
   private Avatar figura$avatar;

   @Shadow
   protected abstract A getArmorModel(S var1, EquipmentSlot var2);

   @Shadow
   protected abstract void renderArmorPiece(PoseStack var1, SubmitNodeCollector var2, ItemStack var3, EquipmentSlot var4, int var5, S var6);

   public HumanoidArmorLayerMixinFabric(RenderLayerParent<S, M> context) {
      super(context);
   }

   @Inject(
      at = @At("HEAD"),
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"
   )
   public void setAvatar(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
      this.figura$avatar = AvatarManager.getAvatar(humanoidRenderState);
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         shift = Shift.AFTER,
         ordinal = 3,
         target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"
      ),
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V"
   )
   public void onRenderEnd(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
      if (this.figura$avatar != null) {
         this.figura$tryRenderArmorPart(
            EquipmentSlot.HEAD, this::figura$helmetRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.HelmetPivot
         );
         this.figura$tryRenderArmorPart(
            EquipmentSlot.CHEST,
            this::figura$chestplateRenderer,
            poseStack,
            humanoidRenderState,
            submitNodeCollector,
            i,
            ParentType.LeftShoulderPivot,
            ParentType.ChestplatePivot,
            ParentType.RightShoulderPivot
         );
         this.figura$tryRenderArmorPart(
            EquipmentSlot.LEGS,
            this::figura$leggingsRenderer,
            poseStack,
            humanoidRenderState,
            submitNodeCollector,
            i,
            ParentType.LeftLeggingPivot,
            ParentType.RightLeggingPivot,
            ParentType.LeggingsPivot
         );
         this.figura$tryRenderArmorPart(
            EquipmentSlot.FEET,
            this::figura$bootsRenderer,
            poseStack,
            humanoidRenderState,
            submitNodeCollector,
            i,
            ParentType.LeftBootPivot,
            ParentType.RightBootPivot
         );
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"
      ),
      method = "renderArmorPiece"
   )
   public void addFiguraCallbacks(
      PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot equipmentSlot, int light, S state, CallbackInfo ci
   ) {
      if (this.figura$avatar != null) {
         Avatar localAvatar = this.figura$avatar;
         A humanoidModel = this.getArmorModel(state, equipmentSlot);
         FiguraSubmitCallBackExtension extension = (FiguraSubmitCallBackExtension)(Object)humanoidModel;
         extension.figura$addPreRenderingCallback(poseStack -> {
            VanillaPart part = RenderUtils.partFromSlot(localAvatar, equipmentSlot);
            if (part != null) {
               part.save(humanoidModel);
               part.preTransform(humanoidModel);
               part.posTransform(humanoidModel);
            }

            return true;
         });
         extension.figura$addPostRenderingCallback(() -> {
            VanillaPart part = RenderUtils.partFromSlot(localAvatar, equipmentSlot);
            if (part != null) {
               part.restore(humanoidModel);
            }
         });
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z",
         shift = Shift.AFTER
      ),
      method = "renderArmorPiece"
   )
   public void renderArmorPieceHijack(
      PoseStack matrices,
      SubmitNodeCollector submitNodeCollector,
      ItemStack stack,
      EquipmentSlot armorSlot,
      int light,
      S state,
      CallbackInfo ci,
      @Local A humanoidModel
   ) {
      if (this.figura$avatar == null || !this.figura$renderingVanillaArmor) {
         this.figura$setPartVisibility(humanoidModel, armorSlot);
      }
   }

   @Unique
   private void figura$tryRenderArmorPart(
      EquipmentSlot slot,
      FiguraArmorPartRenderer<S, A> renderer,
      PoseStack vanillaPoseStack,
      S state,
      SubmitNodeCollector submitNodeCollector,
      int light,
      ParentType... parentTypes
   ) {
      if (slot != null) {
         if (AvatarManager.getEntity(state) instanceof LivingEntity livingEntity) {
            ItemStack itemStack = livingEntity.getItemBySlot(slot);
            if (itemStack.getItem() instanceof Item armorItem
               && armorItem.components().has(DataComponents.EQUIPPABLE)
               && ((Equippable)armorItem.components().get(DataComponents.EQUIPPABLE)).slot() == slot
               && armorItem.components().has(DataComponents.ATTRIBUTE_MODIFIERS)
               && ((ItemAttributeModifiers)armorItem.components().get(DataComponents.ATTRIBUTE_MODIFIERS))
                  .modifiers()
                  .stream()
                  .anyMatch(attribute -> attribute.attribute() == Attributes.ARMOR)) {
               A armorModel = this.getArmorModel(state, slot);
               armorModel.body.xRot = 0.0F;
               armorModel.rightLeg.z = 0.0F;
               armorModel.leftLeg.z = 0.0F;
               armorModel.rightLeg.y = 12.0F;
               armorModel.leftLeg.y = 12.0F;
               armorModel.head.y = 0.0F;
               armorModel.body.y = 0.0F;
               armorModel.leftArm.y = 2.0F;
               armorModel.rightArm.y = 2.0F;
               armorModel.leftArm.x = 5.0F;
               armorModel.rightArm.x = -5.0F;
               armorModel.leftArm.z = 0.0F;
               armorModel.rightArm.z = 0.0F;
               boolean allFailed = true;
               VanillaPart mainPart = RenderUtils.partFromSlot(this.figura$avatar, slot);
               int armorEditPermission = this.figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT);
               if (armorEditPermission == 1 && mainPart != null && !mainPart.checkVisible()) {
                  return;
               }

               if (!GeckoLibCompat.armorHasCustomModel(itemStack, slot, slot == EquipmentSlot.LEGS ? LayerType.HUMANOID_LEGGINGS : LayerType.HUMANOID)) {
                  for (ParentType parentType : parentTypes) {
                     VanillaPart part = RenderUtils.pivotToPart(this.figura$avatar, parentType);
                     if (armorEditPermission != 1 || part == null || part.checkVisible()) {
                        boolean renderedPivot = false;
                        if (armorEditPermission == 1) {
                           renderedPivot = this.figura$avatar.pivotPartRender(parentType, stack -> {
                              stack.pushPose();
                              this.figura$prepareArmorRender(stack);
                              renderer.renderArmorPart(stack, submitNodeCollector, light, armorModel, itemStack, slot, parentType);
                              stack.popPose();
                           });
                        }

                        if (renderedPivot) {
                           allFailed = false;
                        }
                     }
                  }
               }

               if (allFailed) {
                  this.figura$renderingVanillaArmor = true;
                  this.renderArmorPiece(vanillaPoseStack, submitNodeCollector, itemStack, slot, light, state);
                  this.figura$renderingVanillaArmor = false;
               }
            }
         }
      }
   }

   @Unique
   private void figura$prepareArmorRender(PoseStack stack) {
      stack.scale(16.0F, 16.0F, 16.0F);
      stack.mulPose(Axis.XP.rotationDegrees(180.0F));
      stack.mulPose(Axis.YP.rotationDegrees(180.0F));
   }

   @Unique
   private void figura$helmetRenderer(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType
   ) {
      if (parentType == ParentType.HelmetPivot) {
         this.figura$renderArmorPart(model.head, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         this.figura$renderArmorPart(model.hat, poseStack, submitNodeCollector, light, itemStack, armorSlot);
      }
   }

   @Unique
   private void figura$chestplateRenderer(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType
   ) {
      if (parentType == ParentType.ChestplatePivot) {
         this.figura$renderArmorPart(model.body, poseStack, submitNodeCollector, light, itemStack, armorSlot);
      }

      if (parentType == ParentType.LeftShoulderPivot) {
         poseStack.pushPose();
         poseStack.translate(-0.375F, 0.0F, 0.0F);
         this.figura$renderArmorPart(model.leftArm, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }

      if (parentType == ParentType.RightShoulderPivot) {
         poseStack.pushPose();
         poseStack.translate(0.375F, 0.0F, 0.0F);
         this.figura$renderArmorPart(model.rightArm, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }
   }

   @Unique
   private void figura$leggingsRenderer(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType
   ) {
      if (parentType == ParentType.LeggingsPivot) {
         poseStack.pushPose();
         poseStack.translate(0.0F, -0.75F, 0.0F);
         this.figura$renderArmorPart(model.body, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }

      if (parentType == ParentType.LeftLeggingPivot) {
         poseStack.pushPose();
         poseStack.translate(-0.125F, -0.75F, 0.0F);
         this.figura$renderArmorPart(model.leftLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }

      if (parentType == ParentType.RightLeggingPivot) {
         poseStack.pushPose();
         poseStack.translate(0.125F, -0.75F, 0.0F);
         this.figura$renderArmorPart(model.rightLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }
   }

   @Unique
   private void figura$bootsRenderer(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType
   ) {
      if (parentType == ParentType.LeftBootPivot) {
         poseStack.pushPose();
         poseStack.translate(-0.125F, -1.5F, 0.0F);
         this.figura$renderArmorPart(model.leftLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }

      if (parentType == ParentType.RightBootPivot) {
         poseStack.pushPose();
         poseStack.translate(0.125F, -1.5F, 0.0F);
         this.figura$renderArmorPart(model.rightLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
         poseStack.popPose();
      }
   }

   @Unique
   private void figura$renderArmorPart(
      ModelPart modelPart, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, ItemStack itemStack, EquipmentSlot armorSlot
   ) {
      boolean hasGlint = itemStack.hasFoil();
      modelPart.visible = true;
      modelPart.xRot = 0.0F;
      modelPart.yRot = 0.0F;
      modelPart.zRot = 0.0F;
      LayerType layerType = this.figura$usesInnerModel(armorSlot) ? LayerType.HUMANOID_LEGGINGS : LayerType.HUMANOID;
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
                  Identifier normalArmorResource = ((EquipmentLayerRendererAccessor)this.equipmentRenderer)
                     .layerTextureLookup()
                     .apply(new LayerTextureKey(layerType, layer));
                  nodeCollector.order(order++)
                     .submitModelPart(modelPart, poseStack, RenderTypes.armorCutoutNoCull(normalArmorResource), light, OverlayTexture.NO_OVERLAY, null, 0, null);
                  if (hasGlint) {
                     nodeCollector.order(order++)
                        .submitModelPart(modelPart, poseStack, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null, 0, null);
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
               nodeCollector.order(order).submitModelPart(modelPart, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, textureAtlasSprite, -1, null);
            }
         }
      }
   }

   @Unique
   protected void figura$setPartVisibility(A bipedModel, EquipmentSlot slot) {
      bipedModel.head.visible = false;
      bipedModel.hat.visible = false;
      bipedModel.body.visible = false;
      bipedModel.rightArm.visible = false;
      bipedModel.leftArm.visible = false;
      bipedModel.rightLeg.visible = false;
      bipedModel.leftLeg.visible = false;
      switch (slot) {
         case HEAD:
            bipedModel.head.visible = true;
            bipedModel.hat.visible = true;
            break;
         case CHEST:
            bipedModel.body.visible = true;
            bipedModel.rightArm.visible = true;
            bipedModel.leftArm.visible = true;
            break;
         case LEGS:
            bipedModel.body.visible = true;
            bipedModel.rightLeg.visible = true;
            bipedModel.leftLeg.visible = true;
            break;
         case FEET:
            bipedModel.rightLeg.visible = true;
            bipedModel.leftLeg.visible = true;
      }
   }
}
