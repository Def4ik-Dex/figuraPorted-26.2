package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {
   @Accessor("title")
   Component getTitle();

   @Accessor("subtitle")
   Component getSubtitle();

   @Accessor("overlayMessageString")
   Component getActionbar();

   @Accessor("titleTime")
   int getTime();

   @Accessor("overlayMessageTime")
   int getActionbarTime();
}
