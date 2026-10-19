package org.figuramc.figura.mixin.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.Library.Pool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEngineExecutor;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.ChannelAccess.ChannelHandle;
import net.minecraft.client.sounds.SoundEngine.PlayResult;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.ChannelHandleAccessor;
import org.figuramc.figura.ducks.SoundEngineAccessor;
import org.figuramc.figura.ducks.SubtitleOverlayAccessor;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements SoundEngineAccessor {
   @Shadow
   @Final
   private Library library;
   @Shadow
   @Final
   private SoundEngineExecutor executor;
   @Shadow
   @Final
   private SoundBufferLibrary soundBuffers;
   @Shadow
   private boolean loaded;
   @Shadow
   @Final
   private Listener listener;
   @Shadow
   @Final
   private List<SoundEventListener> listeners;
   @Shadow
   @Final
   private Options options;
   @Unique
   private ChannelAccess figuraChannel;
   @Unique
   private final List<LuaSound> figuraHandlers = Collections.synchronizedList(new ArrayList<>());

   @Shadow
   public abstract void addEventListener(SoundEventListener var1);

   @Inject(at = @At("RETURN"), method = "<init>")
   private void soundEngineInit(SoundManager soundManager, Options options, ResourceProvider resourceProvider, CallbackInfo ci) {
      this.figuraChannel = new ChannelAccess(this.library, this.executor);
   }

   @Inject(at = @At("RETURN"), method = "tick")
   private void tick(boolean bl, CallbackInfo ci) {
      this.figuraChannel.scheduleTick();
   }

   @Inject(at = @At("RETURN"), method = "tickInGameSound")
   private void tickNonPaused(CallbackInfo ci) {
      Iterator<LuaSound> iterator = this.figuraHandlers.iterator();

      while (iterator.hasNext()) {
         LuaSound sound = iterator.next();
         ChannelHandle handle = sound.getHandle();
         if (handle == null) {
            iterator.remove();
         } else if (this.figura$getVolume(SoundSource.PLAYERS) <= 0.0F) {
            handle.execute(Channel::stop);
            iterator.remove();
         } else if (handle.isStopped()) {
            iterator.remove();
         }
      }
   }

   @Inject(at = @At("RETURN"), method = "stopAll")
   private void stopAll(CallbackInfo ci) {
      this.figura$stopAllSounds();
   }

   @Inject(at = @At("RETURN"), method = "pauseAllExcept")
   private void pause(CallbackInfo ci) {
      if (this.loaded) {
         this.figuraChannel.executeOnChannels(stream -> stream.forEach(Channel::pause));
      }
   }

   @Inject(at = @At("RETURN"), method = "resume")
   private void resume(CallbackInfo ci) {
      if (this.loaded) {
         this.figuraChannel.executeOnChannels(stream -> stream.forEach(Channel::unpause));
      }
   }

   @Inject(at = @At("RETURN"), method = "updateCategoryVolume")
   private void updateCategoryVolume(SoundSource soundSource, float f, CallbackInfo ci) {
      if (this.loaded && soundSource == SoundSource.PLAYERS) {
         for (LuaSound sound : this.figuraHandlers) {
            sound.volume(sound.getVolume());
         }
      }
   }

   @Inject(at = @At("RETURN"), method = "stop(Lnet/minecraft/resources/Identifier;Lnet/minecraft/sounds/SoundSource;)V")
   private void stop(Identifier id, SoundSource category, CallbackInfo ci) {
      if (category == SoundSource.PLAYERS) {
         this.figura$stopAllSounds();
      }
   }

   @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"), method = "play", cancellable = true)
   public void play(SoundInstance sound, CallbackInfoReturnable<PlayResult> cir) {
      float g = Math.max(sound.getVolume(), 1.0F) * sound.getSound().getAttenuationDistance();
      Vec3 pos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
      if (sound.isRelative() || sound.getAttenuation() == Attenuation.NONE || this.listener.getTransform().position().distanceToSqr(pos) < g * g) {
         AvatarManager.executeAll(
            "playSoundEvent",
            avatar -> {
               boolean cancel = avatar.playSoundEvent(
                  sound.getIdentifier().toString(),
                  FiguraVec3.fromVec3(pos),
                  sound.getVolume(),
                  sound.getPitch(),
                  sound.isLooping(),
                  sound.getSource().name(),
                  sound.getSound().getLocation().toString()
               );
               if (avatar.permissions.get(Permissions.CANCEL_SOUNDS) >= 1) {
                  avatar.noPermissions.remove(Permissions.CANCEL_SOUNDS);
                  if (cancel) {
                     cir.setReturnValue(PlayResult.STARTED_SILENTLY);
                  }
               } else if (cancel) {
                  avatar.noPermissions.add(Permissions.CANCEL_SOUNDS);
               }
            }
         );
      }
   }

   @Override
   public void figura$addSound(LuaSound sound) {
      this.figuraHandlers.add(sound);

      for (SoundEventListener listener : this.listeners) {
         if (listener instanceof SubtitleOverlay overlay) {
            float g = sound.getVolume() * sound.calculateVolume() * sound.getAttenuation() * 16.0F;
            ((SubtitleOverlayAccessor)overlay).figura$PlaySound(sound, g);
         }
      }
   }

   @Override
   public void figura$stopSound(UUID owner, String name) {
      if (this.loaded) {
         Iterator<LuaSound> iterator = this.figuraHandlers.iterator();

         while (iterator.hasNext()) {
            LuaSound sound = iterator.next();
            ChannelHandleAccessor accessor = (ChannelHandleAccessor)sound.getHandle();
            if (accessor != null && (owner == null || accessor.getOwner().equals(owner) && (name == null || accessor.getName().equals(name)))) {
               sound.stop();
               iterator.remove();
            }
         }
      }
   }

   @Override
   public void figura$stopAllSounds() {
      if (this.loaded) {
         for (LuaSound sound : this.figuraHandlers) {
            sound.stop();
         }

         this.figuraHandlers.clear();
         this.figuraChannel.clear();
      }
   }

   @Override
   public ChannelHandle figura$createHandle(UUID owner, String name, Pool pool) {
      return this.figuraChannel.createHandle(pool).thenApply(channelHandle -> {
         if (channelHandle != null) {
            ((ChannelHandleAccessor)channelHandle).setOwner(owner);
            ((ChannelHandleAccessor)channelHandle).setName(name);
         }

         return (ChannelHandle)channelHandle;
      }).join();
   }

   @Override
   public float figura$getVolume(SoundSource soundSource) {
      return soundSource != null && soundSource != SoundSource.MASTER ? this.options.getSoundSourceVolume(soundSource) : 1.0F;
   }

   @Override
   public SoundBufferLibrary figura$getSoundBuffers() {
      return this.soundBuffers;
   }

   @Override
   public boolean figura$isPlaying(UUID owner) {
      if (!this.loaded) {
         return false;
      }

      for (LuaSound sound : List.copyOf(this.figuraHandlers)) {
         ChannelHandleAccessor accessor = (ChannelHandleAccessor)sound.getHandle();
         if (sound.isPlaying() && accessor != null && accessor.getOwner().equals(owner)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean figura$isEngineActive() {
      return this.loaded;
   }
}
