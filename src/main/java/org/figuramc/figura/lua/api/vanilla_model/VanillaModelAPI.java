package org.figuramc.figura.lua.api.vanilla_model;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.entries.FiguraVanillaPart;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.VanillaModelProvider;

@LuaWhitelist
@LuaTypeDoc(name = "VanillaModelAPI", value = "vanilla_model")
public class VanillaModelAPI {
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.head")
   public final VanillaModelPart HEAD;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.body")
   public final VanillaModelPart BODY;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_arm")
   public final VanillaModelPart LEFT_ARM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_arm")
   public final VanillaModelPart RIGHT_ARM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_leg")
   public final VanillaModelPart LEFT_LEG;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_leg")
   public final VanillaModelPart RIGHT_LEG;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.hat")
   public final VanillaModelPart HAT;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.jacket")
   public final VanillaModelPart JACKET;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_sleeve")
   public final VanillaModelPart LEFT_SLEEVE;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_sleeve")
   public final VanillaModelPart RIGHT_SLEEVE;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_pants")
   public final VanillaModelPart LEFT_PANTS;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_pants")
   public final VanillaModelPart RIGHT_PANTS;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.cape_model")
   public final VanillaModelPart CAPE_MODEL;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.fake_cape")
   public final VanillaModelPart FAKE_CAPE;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.helmet_item")
   public final VanillaModelPart HELMET_ITEM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.helmet_head")
   public final VanillaModelPart HELMET_HEAD;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.helmet_hat")
   public final VanillaModelPart HELMET_HAT;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.chestplate_body")
   public final VanillaModelPart CHESTPLATE_BODY;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.chestplate_left_arm")
   public final VanillaModelPart CHESTPLATE_LEFT_ARM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.chestplate_right_arm")
   public final VanillaModelPart CHESTPLATE_RIGHT_ARM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.leggings_body")
   public final VanillaModelPart LEGGINGS_BODY;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.leggings_left_leg")
   public final VanillaModelPart LEGGINGS_LEFT_LEG;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.leggings_right_leg")
   public final VanillaModelPart LEGGINGS_RIGHT_LEG;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.boots_left_leg")
   public final VanillaModelPart BOOTS_LEFT_LEG;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.boots_right_leg")
   public final VanillaModelPart BOOTS_RIGHT_LEG;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_elytra")
   public final VanillaModelPart LEFT_ELYTRA;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_elytra")
   public final VanillaModelPart RIGHT_ELYTRA;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_item")
   public final VanillaModelPart LEFT_ITEM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_item")
   public final VanillaModelPart RIGHT_ITEM;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.left_parrot")
   public final VanillaModelPart LEFT_PARROT;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.right_parrot")
   public final VanillaModelPart RIGHT_PARROT;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.all")
   public final VanillaGroupPart ALL;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.player")
   public final VanillaGroupPart PLAYER;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.outer_layer")
   public final VanillaGroupPart OUTER_LAYER;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.inner_layer")
   public final VanillaGroupPart INNER_LAYER;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.cape")
   public final VanillaGroupPart CAPE;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.armor")
   public final VanillaGroupPart ARMOR;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.helmet")
   public final VanillaGroupPart HELMET;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.chestplate")
   public final VanillaGroupPart CHESTPLATE;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.leggings")
   public final VanillaGroupPart LEGGINGS;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.boots")
   public final VanillaGroupPart BOOTS;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.elytra")
   public final VanillaGroupPart ELYTRA;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.held_items")
   public final VanillaGroupPart HELD_ITEMS;
   @LuaWhitelist
   @LuaFieldDoc("vanilla_model.parrots")
   public final VanillaGroupPart PARROTS;
   private static final List<FiguraVanillaPart> ENTRYPOINTS = new ArrayList<>();
   private final Map<String, VanillaPart> allParts = new HashMap<>();

   private <T extends VanillaPart> T addPart(T part) {
      this.allParts.put(part.name, part);
      return part;
   }

   public VanillaModelAPI(Avatar owner) {
      this.HEAD = this.addPart(new VanillaModelPart(owner, "HEAD", ParentType.Head, VanillaModelProvider.HEAD.func));
      this.BODY = this.addPart(new VanillaModelPart(owner, "BODY", ParentType.Body, VanillaModelProvider.BODY.func));
      this.LEFT_ARM = this.addPart(new VanillaModelPart(owner, "LEFT_ARM", ParentType.LeftArm, VanillaModelProvider.LEFT_ARM.func));
      this.RIGHT_ARM = this.addPart(new VanillaModelPart(owner, "RIGHT_ARM", ParentType.RightArm, VanillaModelProvider.RIGHT_ARM.func));
      this.LEFT_LEG = this.addPart(new VanillaModelPart(owner, "LEFT_LEG", ParentType.LeftLeg, VanillaModelProvider.LEFT_LEG.func));
      this.RIGHT_LEG = this.addPart(new VanillaModelPart(owner, "RIGHT_LEG", ParentType.RightLeg, VanillaModelProvider.RIGHT_LEG.func));
      this.HAT = this.addPart(new VanillaModelPart(owner, "HAT", ParentType.Head, VanillaModelProvider.HAT.func));
      this.JACKET = this.addPart(new VanillaModelPart(owner, "JACKET", ParentType.Body, VanillaModelProvider.JACKET.func));
      this.LEFT_SLEEVE = this.addPart(new VanillaModelPart(owner, "LEFT_SLEEVE", ParentType.LeftArm, VanillaModelProvider.LEFT_SLEEVE.func));
      this.RIGHT_SLEEVE = this.addPart(new VanillaModelPart(owner, "RIGHT_SLEEVE", ParentType.RightArm, VanillaModelProvider.RIGHT_SLEEVE.func));
      this.LEFT_PANTS = this.addPart(new VanillaModelPart(owner, "LEFT_PANTS", ParentType.LeftLeg, VanillaModelProvider.LEFT_PANTS.func));
      this.RIGHT_PANTS = this.addPart(new VanillaModelPart(owner, "RIGHT_PANTS", ParentType.RightLeg, VanillaModelProvider.RIGHT_PANTS.func));
      this.CAPE_MODEL = this.addPart(new VanillaModelPart(owner, "CAPE_MODEL", ParentType.Cape, VanillaModelProvider.CAPE.func));
      this.FAKE_CAPE = this.addPart(new VanillaModelPart(owner, "FAKE_CAPE", ParentType.Cape, VanillaModelProvider.FAKE_CAPE.func));
      this.HELMET_ITEM = this.addPart(new VanillaModelPart(owner, "HELMET_ITEM", ParentType.Head, null));
      this.HELMET_HEAD = this.addPart(new VanillaModelPart(owner, "HELMET_HEAD", ParentType.Head, VanillaModelProvider.HEAD.func));
      this.HELMET_HAT = this.addPart(new VanillaModelPart(owner, "HELMET_HAT", ParentType.Head, VanillaModelProvider.HAT.func));
      this.CHESTPLATE_BODY = this.addPart(new VanillaModelPart(owner, "CHESTPLATE_BODY", ParentType.Body, VanillaModelProvider.BODY.func));
      this.CHESTPLATE_LEFT_ARM = this.addPart(new VanillaModelPart(owner, "CHESTPLATE_LEFT_ARM", ParentType.LeftArm, VanillaModelProvider.LEFT_ARM.func));
      this.CHESTPLATE_RIGHT_ARM = this.addPart(new VanillaModelPart(owner, "CHESTPLATE_RIGHT_ARM", ParentType.RightArm, VanillaModelProvider.RIGHT_ARM.func));
      this.LEGGINGS_BODY = this.addPart(new VanillaModelPart(owner, "LEGGINGS_BODY", ParentType.Body, VanillaModelProvider.BODY.func));
      this.LEGGINGS_LEFT_LEG = this.addPart(new VanillaModelPart(owner, "LEGGINGS_LEFT_LEG", ParentType.LeftLeg, VanillaModelProvider.LEFT_LEG.func));
      this.LEGGINGS_RIGHT_LEG = this.addPart(new VanillaModelPart(owner, "LEGGINGS_RIGHT_LEG", ParentType.RightLeg, VanillaModelProvider.RIGHT_LEG.func));
      this.BOOTS_LEFT_LEG = this.addPart(new VanillaModelPart(owner, "BOOTS_LEFT_LEG", ParentType.LeftLeg, VanillaModelProvider.LEFT_LEG.func));
      this.BOOTS_RIGHT_LEG = this.addPart(new VanillaModelPart(owner, "BOOTS_RIGHT_LEG", ParentType.RightLeg, VanillaModelProvider.RIGHT_LEG.func));
      this.LEFT_ELYTRA = this.addPart(new VanillaModelPart(owner, "LEFT_ELYTRA", ParentType.LeftElytra, VanillaModelProvider.LEFT_ELYTRON.func));
      this.RIGHT_ELYTRA = this.addPart(new VanillaModelPart(owner, "RIGHT_ELYTRA", ParentType.RightElytra, VanillaModelProvider.RIGHT_ELYTRON.func));
      this.LEFT_ITEM = this.addPart(new VanillaModelPart(owner, "LEFT_ITEM", ParentType.LeftArm, null));
      this.RIGHT_ITEM = this.addPart(new VanillaModelPart(owner, "RIGHT_ITEM", ParentType.RightArm, null));
      this.LEFT_PARROT = this.addPart(new VanillaModelPart(owner, "LEFT_PARROT", ParentType.Body, null));
      this.RIGHT_PARROT = this.addPart(new VanillaModelPart(owner, "RIGHT_PARROT", ParentType.Body, null));
      this.INNER_LAYER = this.addPart(
         new VanillaGroupPart(owner, "INNER_LAYER", this.HEAD, this.BODY, this.LEFT_ARM, this.RIGHT_ARM, this.LEFT_LEG, this.RIGHT_LEG)
      );
      this.OUTER_LAYER = this.addPart(
         new VanillaGroupPart(owner, "OUTER_LAYER", this.HAT, this.JACKET, this.LEFT_SLEEVE, this.RIGHT_SLEEVE, this.LEFT_PANTS, this.RIGHT_PANTS)
      );
      this.PLAYER = this.addPart(new VanillaGroupPart(owner, "PLAYER", this.INNER_LAYER, this.OUTER_LAYER));
      this.CAPE = this.addPart(new VanillaGroupPart(owner, "CAPE", this.CAPE_MODEL, this.FAKE_CAPE));
      this.HELMET = this.addPart(new VanillaGroupPart(owner, "HELMET", this.HELMET_ITEM, this.HELMET_HEAD, this.HELMET_HAT));
      this.CHESTPLATE = this.addPart(new VanillaGroupPart(owner, "CHESTPLATE", this.CHESTPLATE_BODY, this.CHESTPLATE_LEFT_ARM, this.CHESTPLATE_RIGHT_ARM));
      this.LEGGINGS = this.addPart(new VanillaGroupPart(owner, "LEGGINGS", this.LEGGINGS_BODY, this.LEGGINGS_LEFT_LEG, this.LEGGINGS_RIGHT_LEG));
      this.BOOTS = this.addPart(new VanillaGroupPart(owner, "BOOTS", this.BOOTS_LEFT_LEG, this.BOOTS_RIGHT_LEG));
      this.ARMOR = this.addPart(new VanillaGroupPart(owner, "ARMOR", this.HELMET, this.CHESTPLATE, this.LEGGINGS, this.BOOTS));
      this.ELYTRA = this.addPart(new VanillaGroupPart(owner, "ELYTRA", this.LEFT_ELYTRA, this.RIGHT_ELYTRA));
      this.HELD_ITEMS = this.addPart(new VanillaGroupPart(owner, "HELD_ITEMS", this.LEFT_ITEM, this.RIGHT_ITEM));
      this.PARROTS = this.addPart(new VanillaGroupPart(owner, "PARROTS", this.LEFT_PARROT, this.RIGHT_PARROT));
      List<VanillaGroupPart> groups = new ArrayList<VanillaGroupPart>() {
         {
            this.add(VanillaModelAPI.this.PLAYER);
            this.add(VanillaModelAPI.this.CAPE);
            this.add(VanillaModelAPI.this.ARMOR);
            this.add(VanillaModelAPI.this.ELYTRA);
            this.add(VanillaModelAPI.this.HELD_ITEMS);
            this.add(VanillaModelAPI.this.PARROTS);
         }
      };

      for (FiguraVanillaPart entrypoint : ENTRYPOINTS) {
         List<VanillaModelPart> parts = new ArrayList<>();
         String ID = entrypoint.getID().toUpperCase(Locale.US);

         for (Pair<String, Function<EntityModel<?>, ModelPart>> part : entrypoint.getParts()) {
            String name = ID + "_" + ((String)part.getFirst()).toUpperCase(Locale.US);
            VanillaModelPart model = new VanillaModelPart(owner, name, ParentType.None, (Function<EntityModel<?>, ModelPart>)part.getSecond());
            this.allParts.put(name, model);
            parts.add(model);
         }

         for (Pair<String, Pair<Function<EntityModel<?>, ModelPart>, ParentType>> part : entrypoint.getPartsWithParent()) {
            String name = ID + "_" + ((String)part.getFirst()).toUpperCase(Locale.US);
            VanillaModelPart model = new VanillaModelPart(
               owner, name, (ParentType)((Pair)part.getSecond()).getSecond(), (Function<EntityModel<?>, ModelPart>)((Pair)part.getSecond()).getFirst()
            );
            this.allParts.put(name, model);
            parts.add(model);
         }

         VanillaGroupPart group = new VanillaGroupPart(owner, ID, parts.toArray(new VanillaModelPart[0]));
         this.allParts.put(ID, group);
         groups.add(group);
      }

      this.ALL = this.addPart(new VanillaGroupPart(owner, "ALL", groups.toArray(new VanillaGroupPart[0])));
   }

   public static void initEntryPoints(Set<FiguraVanillaPart> set) {
      ENTRYPOINTS.addAll(set);
   }

   @LuaWhitelist
   public Object __index(String key) {
      return key == null ? null : this.allParts.getOrDefault(key.toUpperCase(Locale.US), null);
   }

   @Override
   public String toString() {
      return "VanillaModelAPI";
   }
}
