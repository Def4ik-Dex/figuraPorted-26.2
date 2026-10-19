package org.figuramc.figura.wizards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import org.apache.commons.codec.binary.Base64;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.exporters.BlockBenchModel;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraResourceListener;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.ResourceUtils;

public class AvatarWizard {
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
   private static String playerTexture = "";
   private static String playerTextureSlim = "";
   private static String capeTexture = "";
   private static byte[] iconTexture;
   private static final BiFunction<ResourceManager, String, String> GET_TEXTURE_DATA = (manager, path) -> {
      byte[] bytes = ResourceUtils.getResource(manager, FiguraIdentifier.of(path));
      return bytes != null ? Base64.encodeBase64String(bytes) : "";
   };
   public static final FiguraResourceListener RESOURCE_LISTENER = FiguraResourceListener.createResourceListener("avatar_wizard", manager -> {
      playerTexture = GET_TEXTURE_DATA.apply(manager, "textures/avatar_wizard/texture.png");
      playerTextureSlim = GET_TEXTURE_DATA.apply(manager, "textures/avatar_wizard/texture_slim.png");
      capeTexture = GET_TEXTURE_DATA.apply(manager, "textures/avatar_wizard/cape.png");
      iconTexture = ResourceUtils.getResource(manager, FiguraIdentifier.of("textures/avatar_wizard/icon.png"));
   });
   private final HashMap<WizardEntry, Object> map = new HashMap<>();

   public void changeEntry(WizardEntry entry, Object value) {
      this.map.put(entry, value);
   }

   public Object getEntry(WizardEntry entry, Object fallback) {
      return this.map.getOrDefault(entry, fallback);
   }

   public boolean canBuild() {
      String name = (String)this.map.get(WizardEntry.NAME);
      return name != null && !name.isBlank();
   }

   public boolean checkDependency(WizardEntry entry) {
      if (entry.dependencies == null) {
         return true;
      }

      for (WizardEntry dependency : entry.dependencies) {
         if (!this.checkDependency(dependency)) {
            return false;
         }

         if (dependency.type != WizardEntry.Type.CATEGORY) {
            Object obj = this.map.get(dependency);
            if (obj == null || !dependency.validate(obj)) {
               return false;
            }
            boolean bl = switch (dependency.type) {
               case TOGGLE -> (Boolean)obj;
               case TEXT -> !((String)obj).isBlank();
               default -> true;
            };
            if (!bl) {
               return false;
            }
         }
      }

      return true;
   }

   public void build() throws IOException {
      Path root = LocalAvatarFetcher.getLocalAvatarDirectory();
      String name = (String)this.map.get(WizardEntry.NAME);
      String filename = name.replaceAll("CON|PRN|AUX|NUL|COM\\d|LPT\\d|[\\\\/:*?\"<>|\u0000]|\\.$", "_");
      Path folder = root.resolve(filename);

      for (int i = 1; Files.exists(folder); i++) {
         folder = root.resolve(filename + "_" + i);
      }

      byte[] metadata = this.buildMetadata(name);
      byte[] script = null;
      if (WizardEntry.DUMMY_SCRIPT.asBool(this.map)) {
         script = this.buildScript();
      }

      byte[] model = null;
      if (WizardEntry.DUMMY_MODEL.asBool(this.map)) {
         model = this.buildModel();
      }

      new IOUtils.DirWrapper(folder)
         .create()
         .write("avatar.json", metadata)
         .write("script.lua", script)
         .write("model.bbmodel", model)
         .write("avatar.png", iconTexture);
      Util.getPlatform().openUri(folder.toUri());
   }

   private byte[] buildMetadata(String name) {
      JsonObject root = new JsonObject();
      root.addProperty("name", name);
      String description = (String)this.map.get(WizardEntry.DESCRIPTION);
      root.addProperty("description", description == null ? "" : description);
      String authorStr = (String)this.map.get(WizardEntry.AUTHORS);
      String playerName = Minecraft.getInstance().player.getName().getString();
      String[] authors = authorStr == null ? new String[]{playerName} : authorStr.split(",");
      if (authors.length == 0) {
         authors = new String[]{playerName};
      }

      JsonArray authorsJson = new JsonArray();

      for (String author : authors) {
         authorsJson.add(author.trim());
      }

      root.add("authors", authorsJson);
      root.addProperty("color", "#" + ColorUtils.rgbToHex(ColorUtils.Colors.random().vec));
      return GSON.toJson(root).getBytes();
   }

   private byte[] buildScript() {
      String script = "-- Auto generated script file --\n";
      boolean hasPlayerModel = WizardEntry.PLAYER_MODEL.asBool(this.map);
      if (hasPlayerModel && WizardEntry.HIDE_PLAYER.asBool(this.map)) {
         script = script + "\n--hide vanilla model\nvanilla_model.PLAYER:setVisible(false)\n";
      }

      boolean hideArmor = WizardEntry.HIDE_ARMOR.asBool(this.map);
      if (hideArmor) {
         script = script + "\n--hide vanilla armor model\nvanilla_model.ARMOR:setVisible(false)\n";
      }

      if (hasPlayerModel && hideArmor && WizardEntry.HELMET_ITEM_PIVOT.asBool(this.map)) {
         script = script + "--re-enable the helmet item\nvanilla_model.HELMET_ITEM:setVisible(true)\n";
      }

      if (WizardEntry.HIDE_CAPE.asBool(this.map)) {
         script = script + "\n--hide vanilla cape model\nvanilla_model.CAPE:setVisible(false)\n";
      }

      if (WizardEntry.HIDE_ELYTRA.asBool(this.map)) {
         script = script + "\n--hide vanilla elytra model\nvanilla_model.ELYTRA:setVisible(false)\n";
      }

      if (WizardEntry.EMPTY_EVENTS.asBool(this.map)) {
         script = script
            + "\n--entity init event, used for when the avatar entity is loaded for the first time\nfunction events.entity_init()\n  --player functions goes here\nend\n\n--tick event, called 20 times per second\nfunction events.tick()\n  --code goes here\nend\n\n--render event, called every time your avatar is rendered\n--it have two arguments, \"delta\" and \"context\"\n--\"delta\" is the percentage between the last and the next tick (as a decimal value, 0.0 to 1.0)\n--\"context\" is a string that tells from where this render event was called (the paperdoll, gui, player render, first person)\nfunction events.render(delta, context)\n  --code goes here\nend\n";
      }

      return script.getBytes();
   }

   private byte[] buildModel() {
      boolean hasPlayer = WizardEntry.PLAYER_MODEL.asBool(this.map);
      boolean hasElytra = WizardEntry.ELYTRA.asBool(this.map);
      boolean hasCape = WizardEntry.CAPE.asBool(this.map);
      boolean hasCapeOrElytra = hasCape || hasElytra;
      boolean slim = WizardEntry.SLIM.asBool(this.map);
      boolean hasArmor = WizardEntry.ARMOR_PIVOTS.asBool(this.map);
      BlockBenchModel model = new BlockBenchModel("free");
      int playerTex = hasPlayer ? model.addImage("Skin", slim ? playerTextureSlim : playerTexture, 64, 64) : -1;
      int capeTex = hasCapeOrElytra ? model.addImage("Cape", capeTexture, 64, 32) : -1;
      if (hasPlayer) {
         model.setResolution(64, 64);
      } else if (hasCapeOrElytra) {
         model.setResolution(64, 32);
      }

      BlockBenchModel.Group root = model.addGroup("root", FiguraVec3.of());
      BlockBenchModel.Group head = model.addGroup(ParentType.Head, FiguraVec3.of(0.0, 24.0, 0.0), root);
      BlockBenchModel.Group body = model.addGroup(ParentType.Body, FiguraVec3.of(0.0, 24.0, 0.0), root);
      BlockBenchModel.Group leftArm = model.addGroup(ParentType.LeftArm, FiguraVec3.of(-5.0, 22.0, 0.0), root);
      BlockBenchModel.Group rightArm = model.addGroup(ParentType.RightArm, FiguraVec3.of(5.0, 22.0, 0.0), root);
      BlockBenchModel.Group leftLeg = model.addGroup(ParentType.LeftLeg, FiguraVec3.of(-1.9, 12.0, 0.0), root);
      BlockBenchModel.Group rightLeg = model.addGroup(ParentType.RightLeg, FiguraVec3.of(1.9, 12.0, 0.0), root);
      if (hasPlayer) {
         generateCubeAndLayer(model, "Hat", FiguraVec3.of(-4.0, 24.0, -4.0), FiguraVec3.of(8.0, 8.0, 8.0), 0.5, head, 0, 0, 32, 0, playerTex);
         generateCubeAndLayer(model, "Jacket", FiguraVec3.of(-4.0, 12.0, -2.0), FiguraVec3.of(8.0, 12.0, 4.0), 0.25, body, 16, 16, 16, 32, playerTex);
         FiguraVec3 armSize = FiguraVec3.of(slim ? 3.0 : 4.0, 12.0, 4.0);
         generateCubeAndLayer(model, "Left Sleeve", FiguraVec3.of(slim ? -7.0 : -8.0, 12.0, -2.0), armSize, 0.25, leftArm, 32, 48, 48, 48, playerTex);
         generateCubeAndLayer(model, "Right Sleeve", FiguraVec3.of(4.0, 12.0, -2.0), armSize, 0.25, rightArm, 40, 16, 40, 32, playerTex);
         generateCubeAndLayer(model, "Left Pants", FiguraVec3.of(-3.9, 0.0, -2.0), FiguraVec3.of(4.0, 12.0, 4.0), 0.25, leftLeg, 16, 48, 0, 48, playerTex);
         generateCubeAndLayer(model, "Right Pants", FiguraVec3.of(-0.1, 0.0, -2.0), FiguraVec3.of(4.0, 12.0, 4.0), 0.25, rightLeg, 0, 16, 0, 32, playerTex);
      }

      if (hasCape) {
         BlockBenchModel.Group cape = model.addGroup(ParentType.Cape, FiguraVec3.of(0.0, 24.0, 2.0), root);
         BlockBenchModel.Cube cube = model.addCube("Cape", FiguraVec3.of(-5.0, 8.0, 2.0), FiguraVec3.of(10.0, 16.0, 1.0), cape);
         cube.generateBoxFaces(0.0, 0.0, capeTex, 1.0, 1.0);
      }

      if (hasElytra) {
         BlockBenchModel.Group elytra = model.addGroup("Elytra", FiguraVec3.of(0.0, 24.0, 2.0), root);
         BlockBenchModel.Group leftElytra = model.addGroup(ParentType.LeftElytra, FiguraVec3.of(-5.0, 24.0, 2.0), elytra);
         BlockBenchModel.Cube cube = model.addCube(FiguraVec3.of(-5.0, 4.0, 2.0), FiguraVec3.of(10.0, 20.0, 2.0), leftElytra);
         cube.inflate = 1.0;
         cube.generateBoxFaces(22.0, 0.0, capeTex, 1.0, 1.0);
         BlockBenchModel.Group rightElytra = model.addGroup(ParentType.RightElytra, FiguraVec3.of(5.0, 24.0, 2.0), elytra);
         cube = model.addCube(FiguraVec3.of(-5.0, 4.0, 2.0), FiguraVec3.of(10.0, 20.0, 2.0), rightElytra);
         cube.inflate = 1.0;
         cube.generateBoxFaces(22.0, 0.0, capeTex, -1.0, 1.0);
      }

      if (WizardEntry.ITEMS_PIVOT.asBool(this.map)) {
         model.addGroup(ParentType.LeftItemPivot, FiguraVec3.of(slim ? -5.5 : -6.0, 12.0, -2.0), leftArm);
         model.addGroup(ParentType.RightItemPivot, FiguraVec3.of(slim ? 5.5 : 6.0, 12.0, -2.0), rightArm);
      }

      if (WizardEntry.SPYGLASS_PIVOT.asBool(this.map)) {
         model.addGroup(ParentType.LeftSpyglassPivot, FiguraVec3.of(-2.0, 28.0, -4.0), head);
         model.addGroup(ParentType.RightSpyglassPivot, FiguraVec3.of(2.0, 28.0, -4.0), head);
      }

      if (WizardEntry.HELMET_ITEM_PIVOT.asBool(this.map)) {
         model.addGroup(ParentType.HelmetItemPivot, FiguraVec3.of(0.0, 24.0, 0.0), head);
      }

      if (WizardEntry.PARROTS_PIVOT.asBool(this.map)) {
         model.addGroup(ParentType.LeftParrotPivot, FiguraVec3.of(-6.0, 24.0, 0.0), body);
         model.addGroup(ParentType.RightParrotPivot, FiguraVec3.of(6.0, 24.0, 0.0), body);
      }

      if (hasArmor) {
         model.addGroup(ParentType.HelmetPivot, FiguraVec3.of(0.0, 24.0, 0.0), head);
         model.addGroup(ParentType.ChestplatePivot, FiguraVec3.of(0.0, 24.0, 0.0), body);
         model.addGroup(ParentType.LeftElytraPivot, FiguraVec3.of(0.0, 24.0, 0.0), body);
         model.addGroup(ParentType.RightElytraPivot, FiguraVec3.of(0.0, 24.0, 0.0), body);
         model.addGroup(ParentType.LeftShoulderPivot, FiguraVec3.of(-6.0, 24.0, 0.0), leftArm);
         model.addGroup(ParentType.RightShoulderPivot, FiguraVec3.of(6.0, 24.0, 0.0), rightArm);
         model.addGroup(ParentType.LeggingsPivot, FiguraVec3.of(0.0, 12.0, 0.0), body);
         model.addGroup(ParentType.LeftLeggingPivot, FiguraVec3.of(-2.0, 12.0, 0.0), leftLeg);
         model.addGroup(ParentType.RightLeggingPivot, FiguraVec3.of(2.0, 12.0, 0.0), rightLeg);
         model.addGroup(ParentType.LeftBootPivot, FiguraVec3.of(-2.0, 0.0, 0.0), leftLeg);
         model.addGroup(ParentType.RightBootPivot, FiguraVec3.of(2.0, 0.0, 0.0), rightLeg);
      }

      return GSON.toJson(model.build()).getBytes();
   }

   private static void generateCubeAndLayer(
      BlockBenchModel model,
      String layerName,
      FiguraVec3 position,
      FiguraVec3 size,
      double inflation,
      BlockBenchModel.Group parent,
      int x1,
      int y1,
      int x2,
      int y2,
      int texture
   ) {
      BlockBenchModel.Cube c = model.addCube(position, size, parent);
      c.generateBoxFaces(x1, y1, texture);
      BlockBenchModel.Cube l = model.addCube(layerName, position, size, parent);
      l.inflate = inflation;
      l.generateBoxFaces(x2, y2, texture);
   }
}
