package org.figuramc.figura.avatar;

import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.animation.AnimationPlayer;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.gui.FiguraPortraitRenderState;
import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.api.data.FiguraOutputStream;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.particle.ParticleAPI;
import org.figuramc.figura.lua.api.ping.PingArg;
import org.figuramc.figura.lua.api.ping.PingFunction;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.gui.GuiGraphicsExtractorAccessor;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.PartCustomization;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.model.rendering.FiguraRenderer;
import org.figuramc.figura.model.rendering.ImmediateFiguraRenderer;
import org.figuramc.figura.model.rendering.PartFilterScheme;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.PathUtils;
import org.figuramc.figura.utils.RefilledNumber;
import org.figuramc.figura.utils.Version;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class Avatar {
   private static CompletableFuture<Void> tasks;
   public static boolean firstPerson;
   public final UUID owner;
   public final EntityType<?> entityType;
   public CompoundTag nbt;
   public boolean loaded = true;
   public final boolean isHost;
   public String name;
   public String entityName;
   public String authors;
   public Version version;
   public String id;
   public int fileSize;
   public String color;
   public Map<String, String> badgeToColor = new HashMap<>();
   public Map<String, byte[]> resources = new HashMap<>();
   public boolean minify;
   private final Queue<Runnable> events = new ConcurrentLinkedQueue<>();
   public final ArrayList<FiguraBuffer> openBuffers = new ArrayList<>();
   public final ArrayList<FiguraInputStream> openInputStreams = new ArrayList<>();
   public final ArrayList<FiguraOutputStream> openOutputStreams = new ArrayList<>();
   public FiguraRenderer renderer;
   public FiguraLuaRuntime luaRuntime;
   public EntityRenderMode renderMode = EntityRenderMode.OTHER;
   public final PermissionPack.PlayerPermissionPack permissions;
   public final Map<String, SoundBuffer> customSounds = new HashMap<>();
   public final Map<Integer, Animation> animations = new HashMap<>();
   public boolean hasTexture;
   public boolean scriptError;
   public Component errorText;
   public Set<Permissions> noPermissions = new HashSet<>();
   public Set<Permissions> permissionsToTick = new HashSet<>();
   public int lastPlayingSound = 0;
   public int versionStatus = 0;
   public int animationComplexity;
   public final Avatar.Instructions complexity;
   public final Avatar.Instructions init;
   public final Avatar.Instructions render;
   public final Avatar.Instructions worldRender;
   public final Avatar.Instructions tick;
   public final Avatar.Instructions worldTick;
   public final Avatar.Instructions animation;
   public final Map<String, Avatar.Instructions> customInstructions = new HashMap<>();
   public final RefilledNumber particlesRemaining;
   public final RefilledNumber soundsRemaining;
   private static final PartCustomization PIVOT_PART_RENDERING_CUSTOMIZATION = new PartCustomization();

   private Avatar(UUID owner, EntityType<?> type, String name) {
      this.owner = owner;
      this.entityType = type;
      this.isHost = type == null && FiguraMod.isLocal(owner);
      this.permissions = type == null ? PermissionManager.get(owner) : PermissionManager.getMobPermissions(owner);
      this.complexity = new Avatar.Instructions(this.permissions.get(Permissions.COMPLEXITY));
      this.init = new Avatar.Instructions(this.permissions.get(Permissions.INIT_INST));
      this.render = new Avatar.Instructions(this.permissions.get(Permissions.RENDER_INST));
      this.worldRender = new Avatar.Instructions(this.permissions.get(Permissions.WORLD_RENDER_INST));
      this.tick = new Avatar.Instructions(this.permissions.get(Permissions.TICK_INST));
      this.worldTick = new Avatar.Instructions(this.permissions.get(Permissions.WORLD_TICK_INST));
      this.animation = new Avatar.Instructions(this.permissions.get(Permissions.ANIMATION_INST));
      this.particlesRemaining = new RefilledNumber(this.permissions.get(Permissions.PARTICLES));
      this.soundsRemaining = new RefilledNumber(this.permissions.get(Permissions.SOUNDS));
      this.entityName = name == null ? "" : name;

      for (Collection<Permissions> pluginPermissions : PermissionManager.CUSTOM_PERMISSIONS.values()) {
         for (Permissions customPermission : pluginPermissions) {
            this.customInstructions.putIfAbsent(customPermission.name, new Avatar.Instructions(this.permissions.get(customPermission)));
         }
      }
   }

   public Avatar(UUID owner) {
      this(owner, null, EntityUtils.getNameForUUID(owner));
   }

   public Avatar(Entity entity) {
      this(entity.getUUID(), entity.getType(), entity.getName().getString());
   }

   public Avatar(EntityRenderState entity) {
      this(getEntityForRenderState(entity));
   }

   private static Entity getEntityForRenderState(EntityRenderState entity) {
      int entityId = entity instanceof AvatarRenderState playerRenderState ? playerRenderState.id : ((FiguraEntityRenderStateExtension)entity).figura$getEntityId();
      return (Entity)AvatarManager.ENTITY_CACHE.computeIfAbsent(entityId, id2 -> WorldAPI.getCurrentWorld().getEntity(id2));
   }

   public void load(CompoundTag nbt) {
      Runnable toRun = () -> {
         this.nbt = nbt;
         this.loaded = false;
      };
      if (tasks != null && !tasks.isDone()) {
         tasks.thenRun(toRun);
      } else {
         tasks = CompletableFuture.runAsync(toRun);
      }

      tasks.join();
      if (nbt == null) {
         this.loaded = true;
      } else {
         tasks.thenRun(() -> {
            try {
               CompoundTag metadata = nbt.getCompoundOrEmpty("metadata");
               this.name = metadata.getStringOr("name", "");
               this.authors = metadata.getStringOr("authors", "");
               this.version = new Version(metadata.getStringOr("ver", ""));
               if (metadata.contains("id")) {
                  this.id = metadata.getStringOr("id", "");
               }

               if (metadata.contains("color")) {
                  this.color = metadata.getStringOr("color", "");
               }

               if (metadata.contains("minify")) {
                  this.minify = metadata.getBooleanOr("minify", false);
               }

               if (nbt.contains("resources")) {
                  CompoundTag res = nbt.getCompoundOrEmpty("resources");

                  for (String k : res.keySet()) {
                     this.resources.put(k, res.getByteArray(k).orElse(new byte[0]));
                  }
               }

               for (String key : metadata.keySet()) {
                  if (key.contains("badge_color_")) {
                     this.badgeToColor.put(key.replace("badge_color_", ""), metadata.getStringOr(key, ""));
                  }
               }

               this.fileSize = this.getFileSize();
               this.versionStatus = this.getVersionStatus();
               if (this.entityName.isBlank()) {
                  this.entityName = this.name;
               }

               this.loadAnimations();
               this.renderer = new ImmediateFiguraRenderer(this);
               this.loadCustomSounds();
               this.createLuaRuntime();
            } catch (Exception e) {
               FiguraMod.LOGGER.error("", e);
               this.clean();
               this.nbt = null;
               this.renderer = null;
               this.luaRuntime = null;
            }

            this.loaded = true;
         });
      }
   }

   public void tick() {
      if (!this.scriptError && this.luaRuntime != null && this.loaded) {
         if (this.luaRuntime.getUser() == null) {
            Entity entity = EntityUtils.getEntityByUUID(this.owner);
            if (entity != null) {
               this.luaRuntime.setUser(entity);
               this.run("ENTITY_INIT", this.init.post());
            }
         }

         for (Permissions t : this.permissionsToTick) {
            if (this.permissions.get(t) > 0) {
               this.noPermissions.remove(t);
            } else {
               this.noPermissions.add(t);
            }
         }

         if (this.lastPlayingSound > 0) {
            this.lastPlayingSound--;
         }

         this.particlesRemaining.set(this.permissions.get(Permissions.PARTICLES));
         this.particlesRemaining.tick();
         this.soundsRemaining.set(this.permissions.get(Permissions.SOUNDS));
         this.soundsRemaining.tick();
         FiguraMod.pushProfiler("worldTick");
         this.worldTick.reset(this.permissions.get(Permissions.WORLD_TICK_INST));
         this.run("WORLD_TICK", this.worldTick);
         FiguraMod.popPushProfiler("tick");
         this.tick.reset(this.permissions.get(Permissions.TICK_INST));
         this.tickEvent();
         FiguraMod.popProfiler();
      }
   }

   public void render(float delta) {
      if (this.complexity.remaining <= 0) {
         this.noPermissions.add(Permissions.COMPLEXITY);
      } else {
         this.noPermissions.remove(Permissions.COMPLEXITY);
      }

      this.complexity.reset(this.permissions.get(Permissions.COMPLEXITY));
      if (!this.scriptError && this.luaRuntime != null && this.loaded) {
         this.render.reset(this.permissions.get(Permissions.RENDER_INST));
         this.worldRender.reset(this.permissions.get(Permissions.WORLD_RENDER_INST));
         this.run("WORLD_RENDER", this.worldRender, delta);
      }
   }

   public void runPing(int id, byte[] data) {
      this.events.offer(() -> {
         if (!this.scriptError && this.luaRuntime != null && this.loaded) {
            LuaValue[] args = PingArg.fromByteArray(data, this);
            String name = this.luaRuntime.ping.getName(id);
            PingFunction function = this.luaRuntime.ping.get(name);
            if (args != null && function != null) {
               FiguraLuaPrinter.sendPingMessage(this, name, data.length, args);
               this.luaRuntime.run(function.func, this.tick, args);
            }
         }
      });
   }

   public LuaValue loadScript(String name, String chunk) {
      return !this.scriptError && this.luaRuntime != null && this.loaded ? this.luaRuntime.load(name, chunk) : null;
   }

   private void flushQueuedEvents() {
      Runnable e;
      while ((e = this.events.poll()) != null) {
         try {
            e.run();
         } catch (Exception | StackOverflowError ex) {
            if (this.luaRuntime != null) {
               this.luaRuntime.error(ex);
            }
         }
      }
   }

   @Nullable
   public Varargs run(Object toRun, Avatar.Instructions limit, Object... args) {
      this.flushQueuedEvents();
      if (!this.scriptError && this.luaRuntime != null && this.loaded) {
         Varargs ret = this.luaRuntime.run(toRun, limit, args);
         this.flushQueuedEvents();
         return ret;
      } else {
         return null;
      }
   }

   public void punish(int amount) {
      if (this.luaRuntime != null) {
         this.luaRuntime.takeInstructions(amount);
      }
   }

   private boolean isCancelled(Varargs args) {
      if (args == null) {
         return false;
      }

      for (int i = 1; i <= args.narg(); i++) {
         if (args.arg(i).isboolean() && args.arg(i).checkboolean()) {
            return true;
         }
      }

      return false;
   }

   public void tickEvent() {
      if (this.loaded && this.luaRuntime != null && this.luaRuntime.getUser() != null) {
         this.run("TICK", this.tick);
      }
   }

   public void renderEvent(float delta, FiguraMat4 poseMatrix) {
      if (this.loaded && this.luaRuntime != null && this.luaRuntime.getUser() != null) {
         this.run("RENDER", this.render, delta, this.renderMode.name(), poseMatrix);
      }
   }

   public void postRenderEvent(float delta, FiguraMat4 poseMatrix) {
      if (this.loaded && this.luaRuntime != null && this.luaRuntime.getUser() != null) {
         this.run("POST_RENDER", this.render.post(), delta, this.renderMode.name(), poseMatrix);
      }

      this.renderMode = EntityRenderMode.OTHER;
   }

   public void postWorldRenderEvent(float delta) {
      if (this.loaded) {
         if (this.renderer != null) {
            this.renderer.allowMatrixUpdate = false;
         }

         this.run("POST_WORLD_RENDER", this.worldRender.post(), delta);
      }
   }

   public boolean skullRenderEvent(float delta, BlockStateAPI block, ItemStackAPI item, EntityAPI<?> entity, String mode) {
      Varargs result = null;
      if (this.loaded && this.renderer != null && this.renderer.interceptRendersIntoFigura) {
         result = this.run("SKULL_RENDER", this.render, delta, block, item, entity, mode);
      }

      return this.isCancelled(result);
   }

   public boolean useItemEvent(ItemStackAPI stack, String type, int particleCount) {
      Varargs result = this.loaded ? this.run("USE_ITEM", this.tick, stack, type, particleCount) : null;
      return this.isCancelled(result);
   }

   public boolean arrowRenderEvent(float delta, EntityAPI<?> arrow) {
      Varargs result = null;
      if (this.loaded) {
         result = this.run("ARROW_RENDER", this.render, delta, arrow);
      }

      return this.isCancelled(result);
   }

   public boolean tridentRenderEvent(float delta, EntityAPI<?> trident) {
      Varargs result = null;
      if (this.loaded) {
         result = this.run("TRIDENT_RENDER", this.render, delta, trident);
      }

      return this.isCancelled(result);
   }

   public boolean itemRenderEvent(
      ItemStackAPI item,
      String mode,
      FiguraVec3 pos,
      FiguraVec3 rot,
      FiguraVec3 scale,
      boolean leftHanded,
      PoseStack stack,
      SubmitNodeCollector nodeCollector,
      int light,
      int overlay
   ) {
      if (this.loaded && this.renderer != null && this.renderer.interceptRendersIntoFigura) {
         Varargs result = this.run("ITEM_RENDER", this.render, item, mode, pos, rot, scale, leftHanded);
         NodeCollectorExtension extension = (NodeCollectorExtension)nodeCollector;
         if (result == null) {
            return false;
         }

         PoseStack copy = new PoseStack();
         copy.pushPose();
         copy.last().set(stack.last());
         boolean rendered = false;

         for (int i = 1; i <= result.narg(); i++) {
            if (result.arg(i).isuserdata(FiguraModelPart.class)) {
               FiguraModelPart modelPart = (FiguraModelPart)result.arg(i).checkuserdata(FiguraModelPart.class);
               boolean renderedPart = this.figuraItemRendered(modelPart);
               rendered |= renderedPart;
               if (renderedPart) {
                  extension.submitFiguraModel(this, null, (avatar, entity, bufferSource) -> {
                     this.renderItem(copy, bufferSource, modelPart, light, overlay);
                     return null;
                  });
               }
            }
         }

         return rendered;
      } else {
         return false;
      }
   }

   public boolean playSoundEvent(String id, FiguraVec3 pos, float vol, float pitch, boolean loop, String category, String file) {
      Varargs result = null;
      if (this.loaded) {
         result = this.run("ON_PLAY_SOUND", this.tick, id, pos, vol, pitch, loop, category, file);
      }

      return this.isCancelled(result);
   }

   public void resourceReloadEvent() {
      if (this.loaded) {
         this.run("RESOURCE_RELOAD", this.tick);
      }
   }

   public void damageEvent(String sourceType, EntityAPI<?> sourceCause, EntityAPI<?> sourceDirect, FiguraVec3 sourcePosition) {
      if (this.loaded) {
         this.run("DAMAGE", this.tick, sourceType, sourceCause, sourceDirect, sourcePosition);
      }
   }

   public String chatSendMessageEvent(String message) {
      Varargs val = this.loaded ? this.run("CHAT_SEND_MESSAGE", this.tick, message) : null;
      return val != null && (val.isnil(1) || Configs.CHAT_MESSAGES.value) ? (val.isnil(1) ? "" : val.arg(1).tojstring()) : message;
   }

   public Pair<String, Integer> chatReceivedMessageEvent(String message, String json) {
      Varargs val = this.loaded ? this.run("CHAT_RECEIVE_MESSAGE", this.tick, message, json) : null;
      if (val == null) {
         return null;
      }

      if (val.arg(1).isboolean() && !val.arg(1).checkboolean()) {
         return Pair.of(null, null);
      }

      String msg = val.isnil(1) ? json : val.arg(1).tojstring();
      Integer color = null;
      if (val.arg(2).isuserdata(FiguraVec3.class)) {
         color = ColorUtils.rgbToInt((FiguraVec3)val.arg(2).checkuserdata(FiguraVec3.class));
      }

      return Pair.of(msg, color);
   }

   public boolean mouseScrollEvent(double delta) {
      Varargs result = this.loaded ? this.run("MOUSE_SCROLL", this.tick, delta) : null;
      return this.isCancelled(result);
   }

   public boolean mouseMoveEvent(double x, double y) {
      Varargs result = this.loaded ? this.run("MOUSE_MOVE", this.tick, x, y) : null;
      return this.isCancelled(result);
   }

   public boolean mousePressEvent(int button, int action, int modifiers) {
      Varargs result = this.loaded ? this.run("MOUSE_PRESS", this.tick, button, action, modifiers) : null;
      return this.isCancelled(result);
   }

   public boolean keyPressEvent(int key, int action, int modifiers) {
      Varargs result = this.loaded ? this.run("KEY_PRESS", this.tick, key, action, modifiers) : null;
      return this.isCancelled(result);
   }

   public void charTypedEvent(String chars, int modifiers, int codePoint) {
      if (this.loaded) {
         this.run("CHAR_TYPED", this.tick, chars, modifiers, codePoint);
      }
   }

   public boolean totemEvent() {
      return this.isCancelled(this.loaded ? this.run("TOTEM", this.tick) : null);
   }

   private void render() {
      if (this.renderMode != EntityRenderMode.RENDER && this.renderMode != EntityRenderMode.FIRST_PERSON) {
         int prev = this.complexity.remaining;
         this.complexity.remaining = this.permissions.get(Permissions.COMPLEXITY);
         this.renderer.render();
         this.complexity.remaining = prev;
      } else {
         this.complexity.use(this.renderer.render());
      }
   }

   public void render(
      Entity entity,
      float yaw,
      float delta,
      float alpha,
      PoseStack stack,
      SubmitNodeCollector bufferSource,
      int light,
      int overlay,
      EntityModel<?> entityModel,
      PartFilterScheme filter,
      boolean translucent,
      boolean glowing
   ) {
      if (this.renderer != null && this.loaded) {
         this.renderer.vanillaModelData.update(entityModel);
         this.renderer.yaw = yaw;
         this.renderer.entity = entity;
         this.renderer.setupRenderer(filter, bufferSource, stack, delta, light, alpha, overlay, translucent, glowing);
         this.render();
      }
   }

   public synchronized void worldRender(
      Entity entity,
      double camX,
      double camY,
      double camZ,
      PoseStack stack,
      SubmitNodeCollector bufferSource,
      int lightFallback,
      float tickDelta,
      EntityRenderMode mode
   ) {
      if (this.renderer != null && this.loaded) {
         EntityRenderMode prevRenderMode = this.renderMode;
         this.renderMode = mode;
         boolean update = prevRenderMode != EntityRenderMode.OTHER || this.renderMode == EntityRenderMode.FIRST_PERSON_WORLD;
         this.renderer.pivotCustomizations.values().clear();
         this.renderer.allowMatrixUpdate = this.renderer.updateLight = update;
         this.renderer.entity = entity;
         this.renderer
            .setupRenderer(
               PartFilterScheme.WORLD, bufferSource, stack, tickDelta, lightFallback, 1.0F, OverlayTexture.NO_OVERLAY, false, false, camX, camY, camZ
            );
         this.complexity.use(this.renderer.renderSpecialParts());
         this.renderMode = prevRenderMode;
         this.renderer.updateLight = false;
      }
   }

   public void capeRender(Entity entity, SubmitNodeCollector bufferSource, PoseStack stack, int light, float tickDelta, ModelPart cloak) {
      if (this.renderer != null && this.loaded) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this);
         FiguraMod.pushProfiler("capeRender");
         this.renderer.vanillaModelData.update(ParentType.Cape, cloak);
         this.renderer.entity = entity;
         this.renderer
            .setupRenderer(
               PartFilterScheme.CAPE, bufferSource, stack, tickDelta, light, 1.0F, OverlayTexture.NO_OVERLAY, this.renderer.translucent, this.renderer.glowing
            );
         this.render();
         FiguraMod.popProfiler(3);
      }
   }

   public void elytraRender(Entity entity, SubmitNodeCollector bufferSource, PoseStack stack, int light, float tickDelta, EntityModel<?> model) {
      if (this.renderer != null && this.loaded) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this);
         FiguraMod.pushProfiler("elytraRender");
         this.renderer.entity = entity;
         this.renderer
            .setupRenderer(
               PartFilterScheme.LEFT_ELYTRA,
               bufferSource,
               stack,
               tickDelta,
               light,
               1.0F,
               OverlayTexture.NO_OVERLAY,
               this.renderer.translucent,
               this.renderer.glowing
            );
         FiguraMod.pushProfiler("leftWing");
         this.renderer.vanillaModelData.update(ParentType.LeftElytra, model);
         this.renderer.renderSpecialParts();
         FiguraMod.popPushProfiler("rightWing");
         this.renderer.vanillaModelData.update(ParentType.RightElytra, model);
         this.renderer.currentFilterScheme = PartFilterScheme.RIGHT_ELYTRA;
         this.renderer.renderSpecialParts();
         FiguraMod.popProfiler(4);
      }
   }

   public void firstPersonWorldRender(Entity watcher, SubmitNodeCollector bufferSource, PoseStack matrices, Camera camera, float tickDelta) {
      if (this.renderer != null && this.loaded) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this);
         FiguraMod.pushProfiler("firstPersonWorldRender");
         int light = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(watcher, tickDelta);
         Vec3 camPos = camera.position();
         this.worldRender(watcher, camPos.x, camPos.y, camPos.z, matrices, bufferSource, light, tickDelta, EntityRenderMode.FIRST_PERSON_WORLD);
         FiguraMod.popProfiler(3);
      }
   }

   public void firstPersonRender(
      PoseStack stack, SubmitNodeCollector bufferSource, Player player, PlayerModel playerModel, ModelPart arm, int light, float tickDelta
   ) {
      if (this.renderer != null && this.loaded) {
         boolean lefty = arm == playerModel.leftArm;
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this);
         FiguraMod.pushProfiler("firstPersonRender");
         FiguraMod.pushProfiler(lefty ? "leftArm" : "rightArm");
         PartFilterScheme filter = lefty ? PartFilterScheme.LEFT_ARM : PartFilterScheme.RIGHT_ARM;
         boolean config = Configs.ALLOW_FP_HANDS.value;
         this.renderer.allowHiddenTransforms = config;
         this.renderer.allowMatrixUpdate = false;
         this.renderer.ignoreVanillaVisibility = true;
         stack.pushPose();
         if (!config) {
            stack.mulPose(Axis.ZP.rotation(arm.zRot));
            stack.mulPose(Axis.YP.rotation(arm.yRot));
            stack.mulPose(Axis.XP.rotation(arm.xRot));
         }

         this.render(player, 0.0F, tickDelta, 1.0F, stack, bufferSource, light, OverlayTexture.NO_OVERLAY, playerModel, filter, false, false);
         stack.popPose();
         this.renderer.allowHiddenTransforms = true;
         this.renderer.ignoreVanillaVisibility = false;
         FiguraMod.popProfiler(4);
      }
   }

   public void hudRender(PoseStack stack, SubmitNodeCollector bufferSource, Entity entity, float tickDelta) {
      if (this.renderer != null && this.loaded) {
         FiguraMod.pushProfiler(this);
         FiguraMod.pushProfiler("hudRender");
         stack.pushPose();
         stack.last().pose().scale(16.0F, 16.0F, -16.0F);
         stack.last().normal().scale(1.0F, 1.0F, -1.0F);
         Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ITEMS_FLAT);
         GlStateManager._disableDepthTest();
         this.renderer.entity = entity;
         this.renderer.setupRenderer(PartFilterScheme.HUD, bufferSource, stack, tickDelta, 15728880, 1.0F, OverlayTexture.NO_OVERLAY, false, false);
         if (this.renderer.renderSpecialParts() > 0) {

         }

         GlStateManager._enableDepthTest();
         Minecraft.getInstance().gameRenderer.lighting().setupFor(Entry.ITEMS_3D);
         stack.popPose();
         FiguraMod.popProfiler(2);
      }
   }

   public boolean skullRender(PoseStack stack, SubmitNodeCollector bufferSource, int light, Direction direction, float yaw) {
      if (this.renderer != null && this.loaded && this.renderer.interceptRendersIntoFigura) {
         stack.pushPose();
         if (direction == null) {
            stack.translate(0.5, 0.0, 0.5);
         } else {
            stack.translate(0.5 - direction.getStepX() * 0.25, 0.25, 0.5 - direction.getStepZ() * 0.25);
         }

         stack.scale(-1.0F, -1.0F, 1.0F);
         stack.mulPose(Axis.YP.rotationDegrees(yaw));
         this.renderer.allowPivotParts = false;
         this.renderer.setupRenderer(PartFilterScheme.SKULL, bufferSource, stack, 1.0F, light, 1.0F, OverlayTexture.NO_OVERLAY, false, false);
         int comp = this.renderer.renderSpecialParts();
         this.complexity.use(comp);
         boolean bool = comp > 0 || this.headRender(stack, bufferSource, light, true);
         this.renderer.allowPivotParts = true;
         stack.popPose();
         return bool;
      } else {
         return false;
      }
   }

   public boolean headRender(PoseStack stack, SubmitNodeCollector bufferSource, int light, boolean useComplexity) {
      if (this.renderer != null && this.loaded) {
         boolean oldMat = this.renderer.allowMatrixUpdate;
         this.renderer.setupRenderer(PartFilterScheme.HEAD, bufferSource, stack, 1.0F, light, 1.0F, OverlayTexture.NO_OVERLAY, false, false);
         this.renderer.allowHiddenTransforms = false;
         this.renderer.allowMatrixUpdate = false;
         this.renderer.ignoreVanillaVisibility = true;
         int comp = this.renderer.render();
         if (useComplexity) {
            this.complexity.use(comp);
         }

         this.renderer.allowMatrixUpdate = oldMat;
         this.renderer.allowHiddenTransforms = true;
         this.renderer.ignoreVanillaVisibility = false;
         return comp > 0 && this.luaRuntime != null && !this.luaRuntime.vanilla_model.HEAD.checkVisible();
      } else {
         return false;
      }
   }

   public boolean submitPortraitDraw(GuiGraphicsExtractor gui, Identifier fallback, int x, int y, int size, float modelScale, boolean upsideDown) {
      if (Configs.AVATAR_PORTRAIT.value && this.renderer != null && this.loaded) {
         Matrix3x2fStack pose = gui.pose();
         pose.pushMatrix();
         pose.translate(x, y);
         pose.rotate((float) Math.PI);
         Vector2f pos = pose.transformPosition(new Vector2f());
         int x1 = (int)pos.x;
         int y1 = (int)pos.y;
         int x2 = (int)pos.x + size;
         int y2 = (int)pos.y + size;
         gui.pose().pushMatrix();
         gui.pose().identity();
         gui.enableScissor(x1, y1, x2, y2);
         gui.pose().popMatrix();
         pose.translate(0.25F, (float)(upsideDown ? 0.0 : 0.5));
         FiguraPortraitRenderState state = new FiguraPortraitRenderState(
            this, fallback, modelScale, upsideDown, x1, y1, x2, y2, size, ((GuiGraphicsExtractorAccessor)gui).figura$getScissorStack().peek()
         );
         gui.fill(x1, y1, x2, y2, -1);
         ((GuiGraphicsExtractorAccessor)gui).figura$getRenderState().addPicturesInPictureState(state);
         gui.pose().popMatrix();
         gui.disableScissor();
         return true;
      } else {
         return false;
      }
   }

   public boolean renderHeadForPortrait(SubmitNodeCollector buffer, PoseStack stack, int light, float modelScale, boolean upsideDown) {
      stack.pushPose();
      stack.scale(2.0F, 2.0F, 2.0F);
      this.renderer.allowPivotParts = false;
      UIHelper.paperdoll = true;
      UIHelper.dollScale = 16.0F;
      this.renderer.setupRenderer(PartFilterScheme.PORTRAIT, buffer, stack, 1.0F, light, 1.0F, OverlayTexture.NO_OVERLAY, false, false);
      int comp = this.renderer.renderSpecialParts();
      boolean ret = comp > 0 || this.headRender(stack, buffer, light, false);
      stack.popPose();

      UIHelper.paperdoll = false;
      this.renderer.allowPivotParts = true;
      return ret;
   }

   public boolean renderArrow(PoseStack stack, SubmitNodeCollector bufferSource, float delta, int light) {
      if (this.renderer != null && this.loaded) {
         stack.pushPose();
         Quaternionf quaternionf = Axis.XP.rotationDegrees(135.0F);
         Quaternionf quaternionf2 = Axis.YP.rotationDegrees(-90.0F);
         quaternionf.mul(quaternionf2);
         stack.mulPose(quaternionf);
         this.renderer.setupRenderer(PartFilterScheme.ARROW, bufferSource, stack, delta, light, 1.0F, OverlayTexture.NO_OVERLAY, false, false);
         int comp = this.renderer.renderSpecialParts();
         stack.popPose();
         return comp > 0;
      } else {
         return false;
      }
   }

   public boolean renderTrident(PoseStack stack, SubmitNodeCollector bufferSource, float delta, int light) {
      if (this.renderer != null && this.loaded) {
         stack.pushPose();
         Quaternionf quaternionf = Axis.ZP.rotationDegrees(90.0F);
         Quaternionf quaternionf2 = Axis.YP.rotationDegrees(90.0F);
         quaternionf.mul(quaternionf2);
         stack.mulPose(quaternionf);
         this.renderer.setupRenderer(PartFilterScheme.TRIDENT, bufferSource, stack, delta, light, 1.0F, OverlayTexture.NO_OVERLAY, false, false);
         int comp = this.renderer.renderSpecialParts();
         stack.popPose();
         return comp > 0;
      } else {
         return false;
      }
   }

   public boolean isItemPart(FiguraModelPart modelPart) {
      return this.renderer != null && this.loaded && modelPart.parentType == ParentType.Item;
   }

   public boolean renderItem(PoseStack stack, SubmitNodeCollector bufferSource, FiguraModelPart part, int light, int overlay) {
      if (!this.isItemPart(part)) {
         return false;
      }

      stack.pushPose();
      stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      this.renderer.setupRenderer(PartFilterScheme.ITEM, bufferSource, stack, 1.0F, light, 1.0F, overlay, false, false);
      this.renderer.itemToRender = part;
      int ret = this.renderer.renderSpecialParts();
      stack.popPose();
      return ret > 0;
   }

   public boolean figuraItemRendered(FiguraModelPart part) {
      if (!this.isItemPart(part)) {
         return false;
      }

      PartFilterScheme partFilterScheme = this.renderer.currentFilterScheme;
      this.renderer.currentFilterScheme = PartFilterScheme.ITEM;
      this.renderer.itemToRender = part;
      int ret = this.renderer.getComplexity();
      this.renderer.currentFilterScheme = partFilterScheme;
      return ret > 0;
   }

   public synchronized boolean pivotPartRender(ParentType parent, Consumer<PoseStack> consumer) {
      if (this.renderer != null && this.loaded && parent.isPivot) {
         Queue<Pair<FiguraMat4, FiguraMat3>> queue = this.renderer.pivotCustomizations.computeIfAbsent(parent, p -> new ConcurrentLinkedQueue<>());
         if (queue.isEmpty()) {
            return false;
         }

         int i = 0;

         while (!queue.isEmpty() && i++ < 1000) {
            Pair<FiguraMat4, FiguraMat3> matrixPair = queue.poll();
            PIVOT_PART_RENDERING_CUSTOMIZATION.setPositionMatrix((FiguraMat4)matrixPair.getFirst());
            PIVOT_PART_RENDERING_CUSTOMIZATION.setNormalMatrix((FiguraMat3)matrixPair.getSecond());
            PIVOT_PART_RENDERING_CUSTOMIZATION.needsMatrixRecalculation = false;
            PoseStack stack = PIVOT_PART_RENDERING_CUSTOMIZATION.copyIntoGlobalPoseStack();
            consumer.accept(stack);
         }

         queue.clear();
         return true;
      } else {
         return false;
      }
   }

   public void updateMatrices(EntityModel<?> entityModel, PoseStack stack) {
      if (this.renderer != null && this.loaded) {
         FiguraMod.pushProfiler("figura");
         FiguraMod.pushProfiler(this);
         FiguraMod.pushProfiler("updateMatrices");
         this.renderer.vanillaModelData.update(entityModel);
         this.renderer.currentFilterScheme = PartFilterScheme.MODEL;
         this.renderer.setMatrices(stack);
         this.renderer.updateMatrices();
         FiguraMod.popProfiler(3);
      }
   }

   public void applyAnimations() {
      if (this.loaded && !this.scriptError) {
         this.animation.reset(this.permissions.get(Permissions.ANIMATION_INST));
         int animationsLimit = this.permissions.get(Permissions.BB_ANIMATIONS);
         int limit = animationsLimit;

         for (Animation animation : this.animations.values()) {
            limit = AnimationPlayer.tick(animation, limit);
         }

         this.animationComplexity = animationsLimit - limit;
         if (limit <= 0) {
            this.noPermissions.add(Permissions.BB_ANIMATIONS);
         } else {
            this.noPermissions.remove(Permissions.BB_ANIMATIONS);
         }
      }
   }

   public void clearAnimations() {
      if (this.loaded && !this.scriptError) {
         for (Animation animation : this.animations.values()) {
            AnimationPlayer.clear(animation);
         }
      }
   }

   public void clean() {
      if (this.renderer != null) {
         this.renderer.invalidate();
      }

      this.clearSounds();
      this.clearParticles();
      this.closeBuffers();
      this.closeStreams();
      this.events.clear();
   }

   public void clearSounds() {
      SoundAPI.getSoundEngine().figura$stopSound(this.owner, null);
      if (SoundAPI.getSoundEngine().figura$isEngineActive()) {
         for (SoundBuffer value : this.customSounds.values()) {
            value.releaseAlBuffer();
         }
      }
   }

   public void closeBuffers() {
      for (FiguraBuffer buffer : this.openBuffers) {
         if (!buffer.isClosed()) {
            try {
               buffer.baseClose();
            } catch (Exception var4) {
            }
         }
      }

      this.openBuffers.clear();
   }

   public void closeStreams() {
      for (FiguraInputStream stream : new ArrayList<>(this.openInputStreams)) {
         try {
            stream.close();
         } catch (IOException var5) {
         }
      }

      this.openInputStreams.clear();

      for (FiguraOutputStream stream : new ArrayList<>(this.openOutputStreams)) {
         try {
            stream.close();
         } catch (IOException var4) {
         }
      }

      this.openOutputStreams.clear();
   }

   public void clearParticles() {
      ParticleAPI.getParticleEngine().figura$clearParticles(this.owner);
   }

   private int getFileSize() {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         NbtIo.writeCompressed(this.nbt, baos);
         return baos.size();
      } catch (Exception e) {
         FiguraMod.LOGGER.warn("Failed to generate file size for model " + this.name, e);
         return 0;
      }
   }

   private int getVersionStatus() {
      return this.version != null && (NetworkStuff.latestVersion == null || this.version.compareTo(NetworkStuff.latestVersion) <= 0)
         ? this.version.compareTo(FiguraMod.VERSION)
         : 0;
   }

   private void createLuaRuntime() {
      if (this.nbt.contains("scripts")) {
         Map<String, String> scripts = new HashMap<>();
         CompoundTag scriptsNbt = this.nbt.getCompoundOrEmpty("scripts");

         for (String s : scriptsNbt.keySet()) {
            scripts.put(PathUtils.computeSafeString(s), new String(scriptsNbt.getByteArray(s).orElse(new byte[0]), StandardCharsets.UTF_8));
         }

         CompoundTag metadata = this.nbt.getCompoundOrEmpty("metadata");
         ListTag autoScripts;
         if (metadata.contains("autoScripts")) {
            autoScripts = metadata.getListOrEmpty("autoScripts");
         } else {
            autoScripts = null;
         }

         FiguraLuaRuntime runtime = new FiguraLuaRuntime(this, scripts);
         if (this.renderer != null && this.renderer.root != null) {
            runtime.setGlobal("models", this.renderer.root);
         }

         this.init.reset(this.permissions.get(Permissions.INIT_INST));
         runtime.setInstructionLimit(this.init.remaining);
         this.events.offer(() -> {
            if (runtime.init(autoScripts)) {
               this.init.use(runtime.getInstructions());
            }
         });
      }
   }

   private void loadAnimations() {
      if (this.nbt.contains("animations")) {
         ArrayList<String> autoAnims = new ArrayList<>();
         CompoundTag metadata = this.nbt.getCompoundOrEmpty("metadata");
         if (metadata.contains("autoAnims")) {
            for (Tag name : metadata.getListOrEmpty("autoAnims")) {
               autoAnims.add(name.asString().orElse(""));
            }
         }

         ListTag root = this.nbt.getListOrEmpty("animations");

         for (int i = 0; i < root.size(); i++) {
            try {
               CompoundTag animNbt = root.getCompoundOrEmpty(i);
               if (animNbt.contains("mdl") && animNbt.contains("name")) {
                  String mdl = animNbt.getStringOr("mdl", "");
                  String name = animNbt.getStringOr("name", "");
                  Animation.LoopMode loop = Animation.LoopMode.ONCE;
                  if (animNbt.contains("loop")) {
                     try {
                        loop = Animation.LoopMode.valueOf(animNbt.getStringOr("loop", "").toUpperCase(Locale.US));
                     } catch (Exception var13) {
                     }
                  }

                  Animation animation = new Animation(
                     this,
                     mdl,
                     name,
                     loop,
                     animNbt.contains("ovr") && animNbt.getBooleanOr("ovr", false),
                     animNbt.contains("len") ? animNbt.getFloatOr("len", 0.0F) : 0.0F,
                     animNbt.contains("off") ? animNbt.getFloatOr("off", 0.0F) : 0.0F,
                     animNbt.contains("bld") ? animNbt.getFloatOr("bld", 0.0F) : 1.0F,
                     animNbt.contains("sdel") ? animNbt.getFloatOr("sdel", 0.0F) : 0.0F,
                     animNbt.contains("ldel") ? animNbt.getFloatOr("ldel", 0.0F) : 0.0F
                  );
                  if (animNbt.contains("code")) {
                     for (Tag code : animNbt.getListOrEmpty("code")) {
                        CompoundTag compound = (CompoundTag)code;
                        animation.newCode(compound.getFloatOr("time", 0.0F), compound.getStringOr("src", ""));
                     }
                  }

                  this.animations.put(i, animation);
                  if (autoAnims.contains(mdl + "." + name)) {
                     animation.play();
                  }
               }
            } catch (Exception var14) {
            }
         }
      }
   }

   private void loadCustomSounds() {
      if (this.nbt.contains("sounds")) {
         CompoundTag root = this.nbt.getCompoundOrEmpty("sounds");

         for (String key : root.keySet()) {
            try {
               this.loadSound(key, root.getByteArray(key).orElse(new byte[0]));
            } catch (Exception e) {
               FiguraMod.LOGGER.warn("Failed to load custom sound \"" + key + "\"", e);
            }
         }
      }
   }

   public void loadSound(String name, byte[] data) throws Exception {
      if (SoundAPI.getSoundEngine().figura$isEngineActive()) {
         try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            JOrbisAudioStream oggAudioStream = new JOrbisAudioStream(inputStream);

            try {
               SoundBuffer sound = new SoundBuffer(oggAudioStream.readAll(), oggAudioStream.getFormat());
               this.customSounds.put(name, sound);
            } catch (Throwable var9) {
               try {
                  oggAudioStream.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            oggAudioStream.close();
         }
      } else {
         FiguraMod.LOGGER.error("Sound is not supported or enabled on this system but a custom sound tried to load anyway, scripts may break.");
      }
   }

   public FiguraTexture registerTexture(String name, NativeImage image, boolean ignoreSize) {
      int max = this.permissions.get(Permissions.TEXTURE_SIZE);
      if (ignoreSize || image.getWidth() <= max && image.getHeight() <= max) {
         FiguraTexture oldText = this.renderer.customTextures.get(name);
         if (oldText != null) {
            oldText.close();
         }

         if (this.renderer.customTextures.size() > 128) {
            throw new LuaError("Maximum amount of textures reached!");
         }

         FiguraTexture texture = new FiguraTexture(this, name, image);
         this.renderer.customTextures.put(name, texture);
         return texture;
      } else {
         this.noPermissions.add(Permissions.TEXTURE_SIZE);
         throw new LuaError("Texture exceeded max size of " + max + " x " + max + " resolution, got " + image.getWidth() + " x " + image.getHeight());
      }
   }

   public static class Instructions {
      public int max;
      public int remaining;
      private int currPre;
      private int currPost;
      public int pre;
      public int post;
      private boolean inverted;

      public Instructions(int remaining) {
         this.reset(remaining);
      }

      public Avatar.Instructions post() {
         this.inverted = true;
         return this;
      }

      public int getTotal() {
         return this.pre + this.post;
      }

      public void reset(int remaining) {
         this.max = this.remaining = remaining;
         this.currPre = this.currPost = 0;
      }

      public void use(int amount) {
         this.remaining -= amount;
         if (!this.inverted) {
            this.currPre += amount;
            this.pre = this.currPre;
         } else {
            this.currPost += amount;
            this.post = this.currPost;
            this.inverted = false;
         }
      }
   }
}
