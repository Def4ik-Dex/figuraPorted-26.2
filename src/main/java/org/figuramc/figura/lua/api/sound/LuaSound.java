package org.figuramc.figura.lua.api.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.audio.Library.Pool;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.ChannelAccess.ChannelHandle;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "Sound", value = "sound")
public class LuaSound {
   private final Avatar owner;
   private final String id;
   private final SoundBuffer buffer;
   private final Sound sound;
   private ChannelHandle handle;
   private boolean playing = false;
   private FiguraVec3 pos = FiguraVec3.of();
   private float pitch = 1.0F;
   private float volume = 1.0F;
   private float attenuation = 1.0F;
   private boolean loop = false;
   private Component subtitleText;
   private String subtitle;

   public LuaSound(SoundBuffer buffer, String id, Avatar owner) {
      this(null, buffer, id, Component.literal(id), owner);
   }

   public LuaSound(Sound sound, String id, Component subtitle, Avatar owner) {
      this(sound, null, id, subtitle, owner);
   }

   private LuaSound(Sound sound, SoundBuffer buffer, String id, Component subtitle, Avatar owner) {
      this.owner = owner;
      this.id = id;
      this.buffer = buffer;
      this.sound = sound;
      this.subtitleText = subtitle;
      this.subtitle = subtitle == null ? null : subtitle.getString();
   }

   public ChannelHandle getHandle() {
      return this.handle;
   }

   public Component getSubtitleText() {
      return this.subtitleText;
   }

   public String getId() {
      return this.id;
   }

   public float calculateVolume() {
      return SoundAPI.getSoundEngine().figura$getVolume(SoundSource.PLAYERS) * (this.owner.permissions.get(Permissions.VOLUME) / 100.0F);
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.play")
   public LuaSound play() {
      if (this.playing && this.handle != null && !this.handle.isStopped()) {
         return this;
      }

      if (!this.owner.soundsRemaining.use()) {
         this.owner.noPermissions.add(Permissions.SOUNDS);
         return this;
      }

      this.owner.noPermissions.remove(Permissions.SOUNDS);
      if (this.handle != null && this.handle.isStopped()) {
         this.handle = null;
      }

      if (this.handle != null) {
         this.handle.execute(Channel::unpause);
         this.playing = true;
         return this;
      }

      if (this.buffer == null && this.sound == null) {
         return this;
      }

      float vol = this.calculateVolume();
      if (vol <= 0.0F) {
         return this;
      }

      this.playing = true;
      AvatarManager.executeAll(
         "playSoundEvent",
         avatar -> {
            boolean cancel = avatar.playSoundEvent(
               this.getId(), this.getPos(), this.getVolume(), this.getPitch(), this.isLooping(), SoundSource.PLAYERS.name(), null
            );
            if (avatar.permissions.get(Permissions.CANCEL_SOUNDS) >= 1) {
               avatar.noPermissions.remove(Permissions.CANCEL_SOUNDS);
               if (cancel) {
                  this.playing = false;
               }
            } else {
               avatar.noPermissions.add(Permissions.CANCEL_SOUNDS);
            }
         }
      );
      if (!this.playing) {
         return this;
      }

      if (this.buffer != null) {
         this.handle = SoundAPI.getSoundEngine().figura$createHandle(this.owner.owner, this.id, Pool.STATIC);
      } else {
         this.handle = SoundAPI.getSoundEngine().figura$createHandle(this.owner.owner, this.id, this.sound.shouldStream() ? Pool.STREAMING : Pool.STATIC);
      }

      if (this.handle == null) {
         this.playing = false;
         return this;
      }

      SoundAPI.getSoundEngine().figura$addSound(this);
      if (this.buffer != null) {
         this.handle.execute(channel -> {
            channel.setPitch(this.pitch);
            channel.setVolume(this.volume * vol);
            channel.linearAttenuation(this.attenuation * 16.0F);
            channel.setLooping(this.loop);
            channel.setSelfPosition(this.pos.asVec3());
            channel.setRelative(false);
            channel.attachStaticBuffer(this.buffer);
            channel.play();
         });
      } else {
         this.handle.execute(channel -> {
            channel.setPitch(this.pitch);
            channel.setVolume(this.volume * vol);
            channel.linearAttenuation(this.attenuation * 16.0F);
            channel.setLooping(this.loop && !this.sound.shouldStream());
            channel.setSelfPosition(this.pos.asVec3());
            channel.setRelative(false);
         });
         SoundBufferLibrary lib = SoundAPI.getSoundEngine().figura$getSoundBuffers();
         if (!this.sound.shouldStream()) {
            lib.getCompleteBuffer(this.sound.getPath()).thenAccept(buffer -> this.handle.execute(channel -> {
               channel.attachStaticBuffer(buffer);
               channel.play();
            }));
         } else {
            lib.getStream(this.sound.getPath(), this.loop).thenAccept(stream -> this.handle.execute(channel -> {
               channel.attachBufferStream(stream);
               channel.play();
            }));
         }
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.is_playing")
   public boolean isPlaying() {
      if (this.handle != null) {
         if (this.handle.isStopped()) {
            this.playing = false;
         } else {
            this.handle.execute(channel -> this.playing = channel.playing());
         }
      } else {
         this.playing = false;
      }

      return this.playing;
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.pause")
   public LuaSound pause() {
      this.playing = false;
      if (this.handle != null) {
         this.handle.execute(Channel::pause);
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.stop")
   public LuaSound stop() {
      this.playing = false;
      if (this.handle != null) {
         this.handle.execute(Channel::stop);
      }

      this.handle = null;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.get_pos")
   public FiguraVec3 getPos() {
      return this.pos;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "pos"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"x", "y", "z"})
      },
      aliases = "pos",
      value = "sound.set_pos"
   )
   public LuaSound setPos(Object x, Double y, Double z) {
      this.pos = LuaUtils.parseVec3("setPos", x, y, z);
      if (this.handle != null) {
         this.handle.execute(channel -> channel.setSelfPosition(this.pos.asVec3()));
      }

      return this;
   }

   @LuaWhitelist
   public LuaSound pos(Object x, Double y, Double z) {
      return this.setPos(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.get_volume")
   public float getVolume() {
      return this.volume;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "volume"), aliases = "volume", value = "sound.set_volume")
   public LuaSound setVolume(float volume) {
      this.volume = Math.min(volume, 1.0F);
      if (this.handle != null) {
         this.handle.execute(channel -> {
            float f = this.calculateVolume();
            if (f <= 0.0F) {
               channel.stop();
            } else {
               channel.setVolume(this.volume * f);
            }
         });
      }

      return this;
   }

   @LuaWhitelist
   public LuaSound volume(float volume) {
      return this.setVolume(volume);
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.get_attenuation")
   public float getAttenuation() {
      return this.attenuation;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "attenuation"),
      aliases = "attenuation",
      value = "sound.set_attenuation"
   )
   public LuaSound setAttenuation(float attenuation) {
      this.attenuation = Math.max(attenuation, 1.0F);
      if (this.handle != null) {
         this.handle.execute(channel -> channel.linearAttenuation(this.attenuation * 16.0F));
      }

      return this;
   }

   @LuaWhitelist
   public LuaSound attenuation(float attenuation) {
      return this.setAttenuation(attenuation);
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.get_pitch")
   public float getPitch() {
      return this.pitch;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "pitch"), aliases = "pitch", value = "sound.set_pitch")
   public LuaSound setPitch(float pitch) {
      this.pitch = Math.max(pitch, 0.0F);
      if (this.handle != null) {
         this.handle.execute(channel -> channel.setPitch(this.pitch));
      }

      return this;
   }

   @LuaWhitelist
   public LuaSound pitch(float pitch) {
      return this.setPitch(pitch);
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.is_looping")
   public boolean isLooping() {
      return this.loop;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "loop"), aliases = "loop", value = "sound.set_loop")
   public LuaSound setLoop(boolean loop) {
      this.loop = loop;
      if (this.handle != null) {
         this.handle.execute(channel -> channel.setLooping(this.loop));
      }

      return this;
   }

   @LuaWhitelist
   public LuaSound loop(boolean loop) {
      return this.setLoop(loop);
   }

   @LuaWhitelist
   @LuaMethodDoc("sound.get_subtitle")
   public String getSubtitle() {
      return this.subtitle;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "subtitle"), aliases = "subtitle", value = "sound.set_subtitle")
   public LuaSound setSubtitle(String subtitle) {
      this.subtitle = subtitle;
      if (subtitle == null) {
         this.subtitleText = null;
      } else {
         this.subtitleText = TextUtils.tryParseJson(subtitle);
         if (this.subtitleText.getString().length() > 48) {
            throw new LuaError("Text length exceeded limit of 48 characters");
         }
      }

      return this;
   }

   @LuaWhitelist
   public LuaSound subtitle(String subtitle) {
      return this.setSubtitle(subtitle);
   }

   @Override
   public String toString() {
      return this.id + " (Sound)";
   }
}
