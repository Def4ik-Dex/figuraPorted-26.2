package org.figuramc.figura.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface FiguraClientCommandSource extends SharedSuggestionProvider {
   void figura$sendFeedback(Component var1);

   void figura$sendError(Component var1);

   Minecraft figura$getClient();

   LocalPlayer figura$getPlayer();

   default Entity figura$getEntity() {
      return this.figura$getPlayer();
   }

   default Vec3 figura$getPosition() {
      return this.figura$getPlayer().position();
   }

   default Vec2 figura$getRotation() {
      return this.figura$getPlayer().getRotationVector();
   }

   ClientLevel figura$getWorld();

   default Object figura$getMeta(String key) {
      return null;
   }
}
