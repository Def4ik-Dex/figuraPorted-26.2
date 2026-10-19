package org.figuramc.figura.mixin.gui;

import java.util.List;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccessor {
   @Accessor("header")
   Component getHeader();

   @Accessor("footer")
   Component getFooter();

   @Invoker("getPlayerInfos")
   List<PlayerInfo> getThePlayerInfos();
}
