package org.figuramc.figura.lua.api;

import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "AvatarAPI", value = "avatar")
public class AvatarAPI {
   private final Avatar avatar;
   public final LuaTable storedStuff = new LuaTable();

   public AvatarAPI(Avatar avatar) {
      this.avatar = avatar;
   }

   private boolean bool(Permissions permissions) {
      return permissions.asBoolean(this.avatar.permissions.get(permissions));
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_nbt")
   public LuaTable getNBT() {
      return (LuaTable)NbtToLua.convert(this.avatar.nbt);
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = {String.class, Object.class}, argumentNames = {"key", "value"}), value = "avatar.store")
   public AvatarAPI store(@LuaNotNil String key, LuaValue value) {
      this.storedStuff.set(key, value == null ? LuaValue.NIL : value);
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_uuid")
   public String getUUID() {
      return this.avatar.owner.toString();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_current_instructions")
   public int getCurrentInstructions() {
      return this.avatar.luaRuntime == null ? 0 : this.avatar.luaRuntime.getInstructions();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_color")
   public String getColor() {
      return this.avatar.color;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "color"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b"}),
            @LuaMethodOverload(argumentTypes = {FiguraVec3.class, String.class}, argumentNames = {"color", "badgeName"}),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, String.class}, argumentNames = {"r", "g", "b", "badgeName"})
      },
      aliases = "color",
      value = "avatar.set_color"
   )
   public AvatarAPI setColor(Object r, Object g, Double b, String badge) {
      if (!(g instanceof Number) && g != null || badge != null && !badge.isEmpty()) {
         if (g instanceof String && r instanceof FiguraVec3) {
            FiguraVec3 vec = ((FiguraVec3)r).copy();
            this.avatar.badgeToColor.put((String)g, ColorUtils.rgbToHex(vec));
         } else {
            Number h;
            if (g instanceof Number) {
               h = ((Number)g).doubleValue();
            } else {
               h = 1.0;
            }

            FiguraVec3 vec = LuaUtils.parseOneArgVec("setColor", r, h, b, 1.0);
            this.avatar.badgeToColor.put(badge, ColorUtils.rgbToHex(vec));
         }
      } else {
         FiguraVec3 vec = LuaUtils.parseOneArgVec("setColor", r, (Number)g, b, 1.0);
         this.avatar.color = ColorUtils.rgbToHex(vec);
      }

      return this;
   }

   @LuaWhitelist
   public AvatarAPI color(Object r, Object g, Double b, String badge) {
      return this.setColor(r, g, b, badge);
   }

   @LuaWhitelist
   @LuaMethodDoc(aliases = "badges", value = "avatar.get_badges")
   public String getBadges() {
      Component component = Badges.fetchBadges(this.avatar.owner);
      return component.getString();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_version")
   public String getVersion() {
      return this.avatar.version.toString();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_authors")
   public String getAuthors() {
      return this.avatar.authors;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_name")
   public String getName() {
      return this.avatar.name;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_entity_name")
   public String getEntityName() {
      return this.avatar.entityName;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_size")
   public double getSize() {
      return this.avatar.fileSize;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.has_texture")
   public boolean hasTexture() {
      return this.avatar.hasTexture;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.has_script_error")
   public boolean hasScriptError() {
      return this.avatar.scriptError;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_permission_level")
   public String getPermissionLevel() {
      return this.avatar.permissions.getCategory().name();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_init_count")
   public int getInitCount() {
      return this.avatar.init.pre;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_entity_init_count")
   public int getEntityInitCount() {
      return this.avatar.init.post;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_init_count")
   public int getMaxInitCount() {
      return this.avatar.permissions.get(Permissions.INIT_INST);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_tick_count")
   public int getTickCount() {
      return this.avatar.tick.getTotal();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_tick_count")
   public int getMaxTickCount() {
      return this.avatar.permissions.get(Permissions.TICK_INST);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_render_count")
   public int getRenderCount() {
      return this.avatar.render.getTotal();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_render_count")
   public int getMaxRenderCount() {
      return this.avatar.permissions.get(Permissions.RENDER_INST);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_world_tick_count")
   public int getWorldTickCount() {
      return this.avatar.worldTick.getTotal();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_world_tick_count")
   public int getMaxWorldTickCount() {
      return this.avatar.permissions.get(Permissions.WORLD_TICK_INST);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_world_render_count")
   public int getWorldRenderCount() {
      return this.avatar.worldRender.getTotal();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_world_render_count")
   public int getMaxWorldRenderCount() {
      return this.avatar.permissions.get(Permissions.WORLD_RENDER_INST);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_complexity")
   public int getComplexity() {
      return this.avatar.complexity.pre;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_complexity")
   public int getMaxComplexity() {
      return this.avatar.permissions.get(Permissions.COMPLEXITY);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_remaining_particles")
   public int getRemainingParticles() {
      return this.avatar.particlesRemaining.peek();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_particles")
   public int getMaxParticles() {
      return this.avatar.permissions.get(Permissions.PARTICLES);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_remaining_sounds")
   public int getRemainingSounds() {
      return this.avatar.soundsRemaining.peek();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_sounds")
   public int getMaxSounds() {
      return this.avatar.permissions.get(Permissions.SOUNDS);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_volume")
   public int getVolume() {
      return this.avatar.permissions.get(Permissions.VOLUME);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_animation_complexity")
   public int getAnimationComplexity() {
      return this.avatar.animationComplexity;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_animation_complexity")
   public int getMaxAnimationComplexity() {
      return this.avatar.permissions.get(Permissions.BB_ANIMATIONS);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_animation_count")
   public int getAnimationCount() {
      return this.avatar.animation.pre;
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_animation_count")
   public int getMaxAnimationCount() {
      return this.avatar.permissions.get(Permissions.ANIMATION_INST);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_buffers_count")
   public int getBuffersCount() {
      return this.avatar.openBuffers.size();
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_buffers_count")
   public int getMaxBuffersCount() {
      return this.avatar.permissions.get(Permissions.BUFFERS_COUNT);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_texture_size")
   public int getMaxTextureSize() {
      return this.avatar.permissions.get(Permissions.TEXTURE_SIZE);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.get_max_buffer_size")
   public int getMaxBufferSize() {
      return this.avatar.permissions.get(Permissions.BUFFER_SIZE);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.can_edit_vanilla_model")
   public boolean canEditVanillaModel() {
      return this.bool(Permissions.VANILLA_MODEL_EDIT);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.can_edit_nameplate")
   public boolean canEditNameplate() {
      return this.bool(Permissions.NAMEPLATE_EDIT);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.can_render_offscreen")
   public boolean canRenderOffscreen() {
      return this.bool(Permissions.OFFSCREEN_RENDERING);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.can_use_custom_sounds")
   public boolean canUseCustomSounds() {
      return this.bool(Permissions.CUSTOM_SOUNDS);
   }

   @LuaWhitelist
   @LuaMethodDoc("avatar.can_have_custom_skull")
   public boolean canHaveCustomSkull() {
      return this.bool(Permissions.CUSTOM_SKULL);
   }

   @Override
   public String toString() {
      return "AvatarAPI";
   }
}
