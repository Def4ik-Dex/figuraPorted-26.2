package org.figuramc.figura.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.fabric.RenderUtilsImpl;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderUtils {
   static final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
   static PoseStack dummyPoseStack = new PoseStack();

   public static boolean vanillaModel(Avatar avatar) {
      return avatar != null && avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) >= 1;
   }

   public static boolean vanillaModelAndScript(Avatar avatar) {
      return avatar != null && avatar.luaRuntime != null && avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) >= 1;
   }

   public static TextureAtlasSprite firstFireLayer(Avatar avatar) {
      if (!vanillaModelAndScript(avatar)) {
         return null;
      }

      Identifier layer = avatar.luaRuntime.renderer.fireLayer1;
      return layer != null ? Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(layer) : null;
   }

   public static TextureAtlasSprite secondFireLayer(Avatar avatar) {
      if (!vanillaModelAndScript(avatar)) {
         return null;
      } else {
         Identifier layer1 = avatar.luaRuntime.renderer.fireLayer1;
         Identifier layer2 = avatar.luaRuntime.renderer.fireLayer2;
         if (layer2 != null) {
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(layer2);
         } else {
            return layer1 != null ? Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(layer1) : null;
         }
      }
   }

   public static VanillaPart partFromSlot(Avatar avatar, EquipmentSlot equipmentSlot) {
      if (!vanillaModelAndScript(avatar)) {
         return null;
      }

      return switch (equipmentSlot) {
         case HEAD -> avatar.luaRuntime.vanilla_model.HELMET;
         case CHEST -> avatar.luaRuntime.vanilla_model.CHESTPLATE;
         case LEGS -> avatar.luaRuntime.vanilla_model.LEGGINGS;
         case FEET -> avatar.luaRuntime.vanilla_model.BOOTS;
         default -> null;
      };
   }

   public static VanillaPart pivotToPart(Avatar avatar, ParentType type) {
      if (!vanillaModelAndScript(avatar)) {
         return null;
      }

      return switch (type) {
         case HelmetPivot -> avatar.luaRuntime.vanilla_model.HELMET;
         case ChestplatePivot -> avatar.luaRuntime.vanilla_model.CHESTPLATE;
         case LeftShoulderPivot -> avatar.luaRuntime.vanilla_model.CHESTPLATE_LEFT_ARM;
         case RightShoulderPivot -> avatar.luaRuntime.vanilla_model.CHESTPLATE_RIGHT_ARM;
         case LeggingsPivot -> avatar.luaRuntime.vanilla_model.LEGGINGS;
         case LeftLeggingPivot -> avatar.luaRuntime.vanilla_model.LEGGINGS_LEFT_LEG;
         case RightLeggingPivot -> avatar.luaRuntime.vanilla_model.LEGGINGS_RIGHT_LEG;
         case LeftBootPivot -> avatar.luaRuntime.vanilla_model.BOOTS_LEFT_LEG;
         case RightBootPivot -> avatar.luaRuntime.vanilla_model.BOOTS_RIGHT_LEG;
         case LeftElytraPivot -> avatar.luaRuntime.vanilla_model.LEFT_ELYTRA;
         case RightElytraPivot -> avatar.luaRuntime.vanilla_model.RIGHT_ELYTRA;
         default -> null;
      };
   }

   public static EquipmentSlot slotFromPart(ParentType type) {
      switch (type) {
         case HelmetPivot:
         case Head:
         case HelmetItemPivot:
         case Skull:
            return EquipmentSlot.HEAD;
         case ChestplatePivot:
         case LeftShoulderPivot:
         case RightShoulderPivot:
         case LeftElytraPivot:
         case RightElytraPivot:
         case Body:
         case LeftElytra:
         case RightElytra:
            return EquipmentSlot.CHEST;
         case LeggingsPivot:
         case LeftLeggingPivot:
         case RightLeggingPivot:
         case LeftLeg:
         case RightLeg:
            return EquipmentSlot.LEGS;
         case LeftBootPivot:
         case RightBootPivot:
            return EquipmentSlot.FEET;
         case LeftArm:
         case LeftItemPivot:
         case LeftSpyglassPivot:
            return EquipmentSlot.OFFHAND;
         case RightArm:
         case RightItemPivot:
         case RightSpyglassPivot:
            return EquipmentSlot.MAINHAND;
         default:
            return null;
      }
   }

   public static boolean renderArmItem(Avatar avatar, boolean lefty, CallbackInfo ci) {
      if (!vanillaModel(avatar)) {
         return false;
      }

      if (avatar.luaRuntime == null
         || (!lefty || avatar.luaRuntime.vanilla_model.LEFT_ITEM.checkVisible()) && (lefty || avatar.luaRuntime.vanilla_model.RIGHT_ITEM.checkVisible())) {
         return true;
      }

      ci.cancel();
      return false;
   }

   public static Identifier getPlayerSkinTexture(WingsLayer<?, ?> wingsLayer, HumanoidRenderState renderState) {
      return RenderUtilsImpl.getPlayerSkinTexture(wingsLayer, renderState);
   }

   public static void renderStatic(LivingEntity entity, ItemStack item, ItemDisplayContext displayMode, PoseStack poseStack, SubmitNodeCollector buffer, int newLight, int newOverlay) {
      Minecraft client = Minecraft.getInstance();
      if (entity != null) {
         client.getItemModelResolver().updateForLiving(itemStackRenderState, item, displayMode, entity);
      } else {
         client.getItemModelResolver().updateForTopItem(itemStackRenderState, item, displayMode, client.level, null, 0);
      }

      itemStackRenderState.submit(poseStack, buffer, newLight, newOverlay, 0);
   }

   public static void createDummySubmission(
      BitSet selection, SubmitNodeCollector submitNodeStorage, Function<PoseStack, Boolean> preRender, Runnable postRender
   ) {
      assert selection.size() >= 3;
      AvatarRenderer<AbstractClientPlayer> avatarRenderer = Minecraft.getInstance()
         .getEntityRenderDispatcher()
         .getPlayerRenderer(Minecraft.getInstance().player);
      if (selection.get(1)) {
         PlayerModel playerModel = (PlayerModel)avatarRenderer.getModel();
         ((FiguraSubmitCallBackExtension)(Object)playerModel).figura$addPreRenderingCallback(preRender);
         ((FiguraSubmitCallBackExtension)(Object)playerModel).figura$addPostRenderingCallback(postRender);
         submitNodeStorage.submitModel(playerModel, null, dummyPoseStack, RenderTypes.LINES, 0, 0, 0, null);
      }

      if (selection.get(2)) {
         ModelPart modelPart = ((PlayerModel)avatarRenderer.getModel()).leftArm;
         ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPreRenderingCallback(preRender);
         ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPostRenderingCallback(postRender);
         submitNodeStorage.submitModelPart(modelPart, dummyPoseStack, RenderTypes.LINES, 0, 0, null);
      }

      if (selection.get(3)) {
      }
   }

   public static <M extends Model<?>> Map<ModelPart, PartPose> captureModelState(M model) {
      HashMap<ModelPart, PartPose> map = new HashMap<>();

      for (ModelPart part : model.allParts()) {
         PartPose pose = part.storePose();
         map.put(part, pose);
      }

      return map;
   }

   public static <M extends Model<?>> void restoreModelPoseState(M model, Map<ModelPart, PartPose> map) {
      for (ModelPart part : model.allParts()) {
         PartPose pose = map.get(part);
         if (pose != null) {
            part.loadPose(pose);
         }
      }
   }

   public static boolean isEntityUpsideDown(LivingEntity livingEntity) {
      if (livingEntity instanceof Player || livingEntity.hasCustomName()) {
         String string = ChatFormatting.stripFormatting(livingEntity.getName().getString());
         if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
            return !(livingEntity instanceof Player player && !player.isModelPartShown(PlayerModelPart.CAPE));
         }
      }

      return false;
   }
}
