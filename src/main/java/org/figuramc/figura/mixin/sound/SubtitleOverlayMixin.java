package org.figuramc.figura.mixin.sound;

import java.util.List;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay.Subtitle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.ducks.SubtitleOverlayAccessor;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SubtitleOverlay.class)
public class SubtitleOverlayMixin implements SubtitleOverlayAccessor {
   @Shadow
   @Final
   private List<Subtitle> subtitles;

   @Override
   public void figura$PlaySound(LuaSound sound, float volume) {
      Component text = sound.getSubtitleText();
      if (text != null) {
         Vec3 pos = sound.getPos().asVec3();

         for (Subtitle subtitle : this.subtitles) {
            if (subtitle.getText().getString().equals(text.getString())) {
               subtitle.refresh(pos);
               return;
            }
         }

         this.subtitles.add(new Subtitle(text, volume, pos));
      }
   }
}
