package org.figuramc.figura.lua.api.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.animal.parrot.Parrot.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.mixin.FoodDataMixin;
import org.figuramc.figura.mixin.PlayerModelTypeAccessor;
import org.figuramc.figura.utils.EntityUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "PlayerAPI", value = "player")
public class PlayerAPI extends LivingEntityAPI<Player> {
   private PlayerInfo playerInfo;

   public PlayerAPI(Player entity) {
      super(entity);
   }

   private boolean checkPlayerInfo() {
      if (this.playerInfo != null) {
         return true;
      }

      PlayerInfo info = EntityUtils.getPlayerInfo(this.entity.getUUID());
      if (info == null) {
         return false;
      }

      this.playerInfo = info;
      return true;
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_food")
   public int getFood() {
      this.checkEntity();
      return this.entity.getFoodData().getFoodLevel();
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_saturation")
   public float getSaturation() {
      this.checkEntity();
      return this.entity.getFoodData().getSaturationLevel();
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_exhaustion")
   public float getExhaustion() {
      this.checkEntity();
      return ((FoodDataMixin)this.entity.getFoodData()).getExhaustionLevel();
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_experience_progress")
   public float getExperienceProgress() {
      this.checkEntity();
      return this.entity.experienceProgress;
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_experience_level")
   public int getExperienceLevel() {
      this.checkEntity();
      return this.entity.experienceLevel;
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_model_type")
   public String getModelType() {
      this.checkEntity();
      return (this.checkPlayerInfo()
            ? this.playerInfo.getSkin().model().name().toUpperCase()
            : ((PlayerModelTypeAccessor)(Object)DefaultPlayerSkin.get(this.entity.getUUID()).model()).figura$getLegacyServicesId())
         .toUpperCase(Locale.US);
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_gamemode")
   public String getGamemode() {
      this.checkEntity();
      if (!this.checkPlayerInfo()) {
         return null;
      }

      GameType gamemode = this.playerInfo.getGameMode();
      return gamemode == null ? null : gamemode.getName().toUpperCase(Locale.US);
   }

   @LuaWhitelist
   @LuaMethodDoc("player.has_cape")
   public boolean hasCape() {
      this.checkEntity();
      return this.checkPlayerInfo() && this.playerInfo.getSkin().cape() != null;
   }

   @LuaWhitelist
   @LuaMethodDoc("player.has_skin")
   public boolean hasSkin() {
      this.checkEntity();
      return this.checkPlayerInfo() && this.playerInfo.getSkin().body() != null;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "part"), value = "player.is_skin_layer_visible")
   public boolean isSkinLayerVisible(@LuaNotNil String part) {
      this.checkEntity();

      try {
         if (part.equalsIgnoreCase("left_pants") || part.equalsIgnoreCase("right_pants")) {
            part = part + "_leg";
         }

         return this.entity.isModelPartShown(PlayerModelPart.valueOf(part.toUpperCase(Locale.US)));
      } catch (Exception ignored) {
         throw new LuaError("Invalid player model part: " + part);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("player.is_fishing")
   public boolean isFishing() {
      this.checkEntity();
      return this.entity.fishing != null;
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_charged_attack_delay")
   public float getChargedAttackDelay() {
      this.checkEntity();
      return this.entity.getCurrentItemAttackStrengthDelay();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "right")},
      value = "player.get_shoulder_entity"
   )
   public LuaTable getShoulderEntity(boolean right) {
      this.checkEntity();
      LuaTable table = new LuaTable();
      if (right ? !this.entity.getShoulderParrotRight().isEmpty() : !this.entity.getShoulderParrotLeft().isEmpty()) {
         table.set(
            "ParrotVariant", LuaValue.valueOf(((Variant)(right ? this.entity.getShoulderParrotRight() : this.entity.getShoulderParrotLeft()).get()).toString())
         );
         return new ReadOnlyLuaTable(table);
      } else {
         table.set("ParrotVariant", LuaValue.NIL);
         return new ReadOnlyLuaTable(table);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("player.get_team_info")
   public Map<String, Object> getTeamInfo() {
      this.checkEntity();
      if (!this.checkPlayerInfo()) {
         return null;
      }

      PlayerTeam team = this.playerInfo.getTeam();
      if (team == null) {
         return null;
      }

      Map<String, Object> map = new HashMap<>();
      map.put("name", team.getName());
      map.put("display_name", team.getDisplayName().getString());
      map.put("color", team.getColor().map(color -> color.getSerializedName()).orElse("reset"));
      map.put("prefix", team.getPlayerPrefix().getString());
      map.put("suffix", team.getPlayerSuffix().getString());
      map.put("friendly_fire", team.isAllowFriendlyFire());
      map.put("see_friendly_invisibles", team.canSeeFriendlyInvisibles());
      map.put("nametag_visibility", team.getNameTagVisibility().name);
      map.put("death_message_visibility", team.getDeathMessageVisibility().name);
      map.put("collision_rule", team.getCollisionRule().name);
      return map;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = {ItemStackAPI.class, Float.class}, argumentNames = {"stack", "delta"}),
      value = "player.get_cooldown_percent"
   )
   public float getCooldownPercent(@LuaNotNil ItemStackAPI stack, Float delta) {
      this.checkEntity();
      if (delta == null) {
         delta = 0.0F;
      }

      return this.entity.getCooldowns().getCooldownPercent(stack.itemStack, delta);
   }

   @Override
   public String toString() {
      this.checkEntity();
      return this.entity.getName().getString() + " (Player)";
   }
}
