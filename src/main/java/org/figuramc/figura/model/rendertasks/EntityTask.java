package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.LivingEntityRendererAccessor;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.mixin.LivingEntityAccessor;
import org.figuramc.figura.model.FiguraModelPart;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "EntityTask", value = "entity_task")
public class EntityTask extends RenderTask {
   @Nullable
   Entity entity;
   long ticksSinceEntity;

   public EntityTask(String name, Avatar owner, FiguraModelPart parent) {
      super(name, owner, parent);
   }

   @Override
   public void render(PoseStack stack, SubmitNodeCollector buffer, int light, int overlay) {
      stack.scale(16.0F, 16.0F, 16.0F);
      label47:
      if (this.entity != null) {
         assert Minecraft.getInstance().level != null;
         this.entity.tickCount = (int)(Minecraft.getInstance().level.getGameTime() - this.ticksSinceEntity);
         EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
         Minecraft minecraft = Minecraft.getInstance();
         DebugScreenEntryStatus h = minecraft.debugEntries.getStatus(DebugScreenEntries.ENTITY_HITBOXES);
         minecraft.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.NEVER);
         OptionalInt prev = LivingEntityRendererAccessor.overrideOverlay;
         LivingEntityRendererAccessor.overrideOverlay = OptionalInt.of(this.customization.overlay != null ? this.customization.overlay : overlay);
         float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

         try {
            CameraRenderState cameraRenderState = new CameraRenderState();
            cameraRenderState.initialized = minecraft.gameRenderer.mainCamera().isInitialized();
            cameraRenderState.pos = minecraft.gameRenderer.mainCamera().position(); 
            cameraRenderState.blockPos = minecraft.gameRenderer.mainCamera().blockPosition();
            cameraRenderState.orientation = new Quaternionf(minecraft.gameRenderer.mainCamera().rotation());
            EntityRenderState state = dispatcher.extractEntity(this.entity, tickDelta);
            state.lightCoords = this.customization.light != null ? this.customization.light : light;
            dispatcher.submit(state, cameraRenderState, 0.0, 0.0, 0.0, stack, buffer);
         } finally {
            LivingEntityRendererAccessor.overrideOverlay = prev;
            minecraft.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, h);
         }
      }
   }

   @Override
   public int getComplexity() {
      return 20;
   }

   @Override
   public boolean shouldRender() {
      return super.shouldRender() && this.entity != null;
   }

   @LuaWhitelist
   @LuaMethodDoc("entity_task.as_entity")
   public EntityAPI<?> asEntity() {
      return this.entity == null ? null : new EntityAPI<>(this.entity);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "nbt"),
            @LuaMethodOverload(argumentTypes = {String.class, String.class}, argumentNames = {"id", "nbt"})
      },
      value = "entity_task.set_nbt"
   )
   @Contract("_,_->this")
   public EntityTask setNbt(String nbtOrId, String nullOrNbt) {
      try {
         CompoundTag finalNbt;
         if (nullOrNbt == null) {
            finalNbt = TagParser.parseCompoundFully(nbtOrId);
            if (!finalNbt.contains("id")) {
               throw new LuaError("Nbt must contain id");
            }
         } else {
            finalNbt = TagParser.parseCompoundFully(nullOrNbt);
            finalNbt.put("id", StringTag.valueOf(nbtOrId));
         }

         assert Minecraft.getInstance().level != null;
         this.entity = EntityType.loadEntityRecursive(
            TagValueInput.create(ProblemReporter.DISCARDING, Minecraft.getInstance().level.registryAccess(), finalNbt),
            Minecraft.getInstance().level,
            EntitySpawnReason.SPAWN_ITEM_USE,
            EntityProcessor.NOP
         );
         if (this.entity == null) {
            throw new LuaError("Could not create entity");
         }

         this.ticksSinceEntity = Minecraft.getInstance().level.getGameTime() - this.entity.tickCount;
         return this;
      } catch (CommandSyntaxException e) {
         throw new LuaError(e.getMessage());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Double.class, argumentNames = "distance"), value = "entity_task.update_walking_distance")
   @Contract("_->this")
   public EntityTask updateWalkingDistance(float distance) {
      if (this.entity != null && this.entity instanceof LivingEntity living) {
         ((LivingEntityAccessor)living).invokeUpdateWalkAnimation(distance);
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "rotation"), value = "entity_task.set_head_rotation")
   @Contract("_->this")
   public EntityTask setHeadRotation(FiguraVec2 vec2) {
      if (this.entity != null && this.entity instanceof LivingEntity living) {
         living.yHeadRot = (float)vec2.y;
         living.yHeadRotO = (float)vec2.y;
         living.setXRot((float)vec2.x);
         living.xRotO = (float)vec2.x;
      }

      return this;
   }
}
