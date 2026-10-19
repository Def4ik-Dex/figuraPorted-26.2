package org.figuramc.figura.mixin.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.figuramc.figura.config.Configs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "de.maxhenkel.voicechat.voice.client.RenderEvents")
public class SimpleVCMixin {
   @Redirect(
      method = "onRenderName",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", opcode = 180, remap = true),
      remap = false
   )
   private LocalPlayer renderSelfNameplate(Minecraft minecraft) {
      return Configs.SELF_NAMEPLATE.value ? null : minecraft.player;
   }

   @Inject(method = "shouldShowIcons", at = @At("HEAD"), remap = false, cancellable = true)
   private void renderSelfNameplate(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      callbackInfoReturnable.setReturnValue(true);
   }
}
