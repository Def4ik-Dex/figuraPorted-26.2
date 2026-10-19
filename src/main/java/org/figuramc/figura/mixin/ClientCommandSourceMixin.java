package org.figuramc.figura.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientSuggestionProvider.class)
abstract class ClientCommandSourceMixin implements FiguraClientCommandSource {
   @Shadow
   @Final
   private Minecraft minecraft;

   @Override
   public void figura$sendFeedback(Component message) {
      this.minecraft.gui.hud.getChat().addClientSystemMessage(message);
      this.minecraft.getNarrator().saySystemNow(message);
   }

   @Override
   public void figura$sendError(Component message) {
      this.figura$sendFeedback(Component.literal("").append(message).withStyle(ChatFormatting.RED));
   }

   @Override
   public Minecraft figura$getClient() {
      return this.minecraft;
   }

   @Override
   public LocalPlayer figura$getPlayer() {
      return this.minecraft.player;
   }

   @Override
   public ClientLevel figura$getWorld() {
      return this.minecraft.level;
   }
}
