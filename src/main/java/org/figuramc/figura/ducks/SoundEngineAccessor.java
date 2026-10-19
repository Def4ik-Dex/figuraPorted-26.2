package org.figuramc.figura.ducks;

import com.mojang.blaze3d.audio.Library.Pool;
import java.util.UUID;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.ChannelAccess.ChannelHandle;
import net.minecraft.sounds.SoundSource;
import org.figuramc.figura.lua.api.sound.LuaSound;

public interface SoundEngineAccessor {
   void figura$addSound(LuaSound var1);

   void figura$stopSound(UUID var1, String var2);

   void figura$stopAllSounds();

   ChannelHandle figura$createHandle(UUID var1, String var2, Pool var3);

   float figura$getVolume(SoundSource var1);

   SoundBufferLibrary figura$getSoundBuffers();

   boolean figura$isPlaying(UUID var1);

   boolean figura$isEngineActive();
}
