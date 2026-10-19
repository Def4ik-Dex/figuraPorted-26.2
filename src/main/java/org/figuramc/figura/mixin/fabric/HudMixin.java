package org.figuramc.figura.mixin.fabric;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.ActionWheel;
import org.figuramc.figura.gui.FiguraGui;
import org.figuramc.figura.lua.api.RendererAPI;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Unique
   private FiguraVec2 crosshairOffset;

   @Inject(at = @At("HEAD"), method = "extractRenderState", cancellable = true)
   private void onRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      FiguraGui.onRender(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false), ci);
   }

   @Inject(at = @At("RETURN"), method = "extractRenderState")
   private void afterRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (!AvatarManager.panic) {
         FiguraGui.renderOverlays(guiGraphics);
      }
   }

   @Inject(at = @At("HEAD"), method = "extractCrosshair", cancellable = true)
   private void renderCrosshair(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      this.crosshairOffset = null;
      if (ActionWheel.isEnabled()) {
         ci.cancel();
      } else {
         Entity entity = this.minecraft.getCameraEntity();
         Avatar avatar;
         if (entity != null && (avatar = AvatarManager.getAvatar(entity)) != null && avatar.luaRuntime != null) {
            RendererAPI renderer = avatar.luaRuntime.renderer;
            if (!renderer.renderCrosshair) {
               ci.cancel();
            } else {
               this.crosshairOffset = renderer.crosshairOffset;
            }
         }
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
      ),
      method = "extractCrosshair"
   )
   private void blitRenderCrosshair(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (this.crosshairOffset != null) {
         guiGraphics.pose().pushMatrix();
         guiGraphics.pose().translate((float)this.crosshairOffset.x, (float)this.crosshairOffset.y);
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
         shift = Shift.AFTER
      ),
      method = "extractCrosshair"
   )
   private void afterBlitRenderCrosshair(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (this.crosshairOffset != null) {
         guiGraphics.pose().popMatrix();
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"
      ),
      method = "extractCrosshair"
   )
   private void blitRenderCrosshairSliced(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (this.crosshairOffset != null) {
         guiGraphics.pose().pushMatrix();
         guiGraphics.pose().translate((float)this.crosshairOffset.x, (float)this.crosshairOffset.y);
      }
   }

   @Inject(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V",
         shift = Shift.AFTER
      ),
      method = "extractCrosshair"
   )
   private void afterBlitRenderCrosshairSliced(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (this.crosshairOffset != null) {
         guiGraphics.pose().popMatrix();
      }
   }
}
