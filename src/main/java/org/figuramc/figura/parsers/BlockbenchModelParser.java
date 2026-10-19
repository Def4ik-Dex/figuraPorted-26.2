package org.figuramc.figura.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.IOUtils;

public class BlockbenchModelParser {
   private static final Gson GSON = new GsonBuilder().create();
   private int textureOffset = 0;
   private int animationOffset = 0;
   private final HashMap<String, CompoundTag> elementMap = new HashMap<>();
   private final HashMap<String, ListTag> animationMap = new HashMap<>();
   private final HashMap<String, BlockbenchModelParser.TextureData> textureMap = new HashMap<>();
   private final HashMap<Integer, String> textureIdMap = new HashMap<>();
   private static final FiguraVec3 v1 = FiguraVec3.of();
   private static final FiguraVec3 v2 = FiguraVec3.of();
   private static final FiguraVec3 v3 = FiguraVec3.of();
   private static final FiguraVec3 v4 = FiguraVec3.of();
   private static final FiguraVec3 t1 = FiguraVec3.of();
   private static final FiguraVec3 t2 = FiguraVec3.of();
   private static final FiguraVec3 t3 = FiguraVec3.of();
   private static final FiguraVec3 t4 = FiguraVec3.of();

   public BlockbenchModelParser.ModelData parseModel(Path avatarFolder, Path sourceFile, String json, String modelName, String folders) throws Exception {
      BlockbenchModel model = (BlockbenchModel)GSON.fromJson(json, BlockbenchModel.class);
      if (!model.meta.model_format.equals("free") && !model.meta.model_format.contains("figura")) {
         throw new Exception(
            "Model \""
               + modelName
               + "\" have an incompatible model format. Compatibility is limited to \"Generic Model\" format and third-party Figura specific formats"
         );
      }

      if (Integer.parseInt(model.meta.format_version.split("\\.")[0]) < 4) {
         throw new Exception(
            "Model \""
               + modelName
               + "\" was created using a version too old ("
               + model.meta.format_version
               + ") of Blockbench. Minimum compatible version is 4.0"
         );
      }

      CompoundTag textures = new CompoundTag();
      List<CompoundTag> animationList = new ArrayList<>();
      CompoundTag nbt = new CompoundTag();
      nbt.putString("name", modelName);
      parseParent(modelName, nbt);
      this.parseTextures(avatarFolder, sourceFile, folders, modelName, textures, model.textures, model.resolution);
      this.parseElements(model.elements);
      this.parseAnimations(animationList, model.animations, modelName, folders);
      nbt.put("chld", this.parseOutliner(model.outliner, true));
      this.elementMap.clear();
      this.animationMap.clear();
      this.textureMap.clear();
      this.textureIdMap.clear();
      return new BlockbenchModelParser.ModelData(textures, animationList, nbt);
   }

   public static void parseParent(String name, CompoundTag nbt) {
      ParentType parentType = ParentType.get(name);
      if (parentType != ParentType.None) {
         nbt.putString("pt", parentType.name());
      }
   }

   private void parseTextures(
      Path avatar,
      Path sourceFile,
      String folders,
      String modelName,
      CompoundTag texturesNbt,
      BlockbenchModel.Texture[] textures,
      BlockbenchModel.Resolution resolution
   ) throws Exception {
      if (textures != null) {
         LinkedHashMap<String, CompoundTag> texturesTemp = new LinkedHashMap<>();
         List<String> textureIndex = new ArrayList<>();
         CompoundTag src = new CompoundTag();
         ListTag data = new ListTag();

         for (int i = 0; i < textures.length; i++) {
            BlockbenchModel.Texture texture = textures[i];
            String name = texture.name;
            if (name.endsWith(".png")) {
               name = name.substring(0, name.length() - 4);
            }

            String textureType;
            if (name.endsWith("_e")) {
               textureType = "e";
            } else if (name.endsWith("_n")) {
               textureType = "n";
            } else if (name.endsWith("_s")) {
               textureType = "s";
            } else {
               textureType = "d";
            }

            String path;
            byte[] source;
            try {
               Path p = sourceFile.getParent().resolve(texture.relative_path);
               if (p.getFileSystem() == FileSystems.getDefault()) {
                  p = p.toFile().getCanonicalFile().toPath();
               }

               p = p.normalize();
               if (!Files.exists(p) || avatar.getNameCount() > 1 && !p.startsWith(avatar)) {
                  if (!texture.relative_path.startsWith("../")) {
                     throw new IllegalStateException("File does not exist!");
                  }

                  p = sourceFile.resolve(texture.relative_path);
                  if (p.getFileSystem() == FileSystems.getDefault()) {
                     p = p.toFile().getCanonicalFile().toPath();
                  }

                  p = p.normalize();
                  if (!Files.exists(p)) {
                     throw new IllegalStateException("File does not exist!");
                  }
               }

               if (avatar.getNameCount() > 1 && !p.startsWith(avatar) || p.getFileSystem() != avatar.getFileSystem()) {
                  throw new IllegalStateException("File from outside the avatar folder!");
               }

               FiguraMod.debug("path is {}", p.toString());
               source = IOUtils.readFileBytes(p);
               path = avatar.relativize(p).toString().replace(p.getFileSystem().getSeparator(), ".");
               path = path.substring(0, path.length() - 4);
               name = folders + name;
               FiguraMod.debug("Loaded {} Texture \"{}\" from {}", textureType.toUpperCase(Locale.US), name, p);
            } catch (Exception e) {
               if (e instanceof IOException) {
                  FiguraMod.LOGGER.error("", e);
               }

               source = Base64.getDecoder().decode(texture.source.substring("data:image/png;base64,".length()));
               path = folders + modelName + "." + name;
               FiguraMod.debug("Loaded {} Texture \"{}\" from {}", textureType.toUpperCase(Locale.US), name, path);
            }

            src.putByteArray(path, source);
            if (!textureType.equals("d")) {
               name = name.substring(0, name.length() - 2);
            }

            if (texturesTemp.containsKey(name)) {
               CompoundTag textureContainer = texturesTemp.get(name);
               if (textureContainer.contains(textureType)) {
                  throw new Exception("Model \"" + modelName + "\" contains texture with duplicate name \"" + name + "\"");
               }

               textureContainer.putString(textureType, path);
            } else {
               CompoundTag compound = new CompoundTag();
               compound.putString(textureType, path);
               texturesTemp.put(name, compound);
               textureIndex.add(name);
            }

            this.textureIdMap.put(i, name);
            if (!this.textureMap.containsKey(name)) {
               int id = textureIndex.indexOf(name) + this.textureOffset;
               float[] fixedSize;
               if (texture.width != null) {
                  fixedSize = new float[]{texture.width / texture.uv_width, texture.height / texture.uv_height};
               } else {
                  int[] imageSize = getTextureSize(source);
                  fixedSize = new float[]{(float)imageSize[0] / resolution.width, (float)imageSize[1] / resolution.height};
               }

               this.textureMap.put(name, new BlockbenchModelParser.TextureData(id, fixedSize));
            }
         }

         for (Entry<String, CompoundTag> entry : texturesTemp.entrySet()) {
            data.add((Tag)entry.getValue());
         }

         this.textureOffset = this.textureOffset + data.size();
         texturesNbt.put("src", src);
         texturesNbt.put("data", data);
      }
   }

   private void parseElements(BlockbenchModel.Element[] elements) {
      for (BlockbenchModel.Element element : elements) {
         if (element.type == null) {
            element.type = "cube";
         }

         if ((element.type.equalsIgnoreCase("cube") || element.type.equalsIgnoreCase("mesh")) && (element.export == null || element.export)) {
            String id = element.uuid;
            CompoundTag nbt = new CompoundTag();
            nbt.putString("name", orEmpty(element.name));
            if (notZero(element.from)) {
               nbt.put("f", toNbtList(element.from));
            }

            if (notZero(element.to)) {
               nbt.put("t", toNbtList(element.to));
            }

            if (notZero(element.rotation)) {
               nbt.put("rot", toNbtList(element.rotation));
            }

            if (notZero(element.origin)) {
               nbt.put("piv", toNbtList(element.origin));
            }

            if (element.inflate != 0.0F) {
               nbt.putFloat("inf", element.inflate);
            }

            nbt.putBoolean("vsb", element.visibility == null || element.visibility);
            if (element.type.equalsIgnoreCase("cube")) {
               CompoundTag data = this.parseCubeFaces(element.faces);
               nbt.put("cube_data", data);
            } else {
               CompoundTag data = this.parseMesh(element.faces, element.vertices, element.origin);
               nbt.put("mesh_data", data);
            }

            this.elementMap.put(id, nbt);
         }
      }
   }

   private CompoundTag parseCubeFaces(JsonObject faces) {
      CompoundTag nbt = new CompoundTag();

      for (String cubeFace : BlockbenchModel.CubeFace.FACES) {
         if (faces.has(cubeFace)) {
            JsonObject faceObj = faces.getAsJsonObject(cubeFace);
            if (faceObj.has("texture")) {
               try {
                  faceObj.get("texture").getAsNumber();
               } catch (Exception e) {
                  continue;
               }

               BlockbenchModel.CubeFace face = (BlockbenchModel.CubeFace)GSON.fromJson(faceObj, BlockbenchModel.CubeFace.class);
               BlockbenchModelParser.TextureData texture = this.textureMap.get(this.textureIdMap.get(face.texture));
               if (texture != null) {
                  CompoundTag faceNbt = new CompoundTag();
                  faceNbt.putInt("tex", texture.id);
                  if (face.rotation != 0.0F) {
                     faceNbt.putFloat("rot", face.rotation);
                  }

                  if (notZero(face.uv)) {
                     float[] uv = new float[]{
                        face.uv[0] * texture.fixedSize[0],
                        face.uv[1] * texture.fixedSize[1],
                        face.uv[2] * texture.fixedSize[0],
                        face.uv[3] * texture.fixedSize[1]
                     };
                     faceNbt.put("uv", toNbtList(uv));
                  }

                  nbt.put(String.valueOf(cubeFace.charAt(0)), faceNbt);
               }
            }
         }
      }

      return nbt;
   }

   private CompoundTag parseMesh(JsonObject faces, JsonObject vertices, float[] offset) {
      CompoundTag nbt = new CompoundTag();
      HashMap<String, Integer> verticesMap = new HashMap<>();
      ListTag verticesList = new ListTag();
      int index = 0;

      for (Entry<String, JsonElement> entry : vertices.entrySet()) {
         verticesMap.put(entry.getKey(), index);
         float[] arr = jsonToFloat(entry.getValue().getAsJsonArray());
         verticesList.add(FloatTag.valueOf(arr[0] + offset[0]));
         verticesList.add(FloatTag.valueOf(arr[1] + offset[1]));
         verticesList.add(FloatTag.valueOf(arr[2] + offset[2]));
         index++;
      }

      ListTag texesList = new ListTag();
      ListTag uvsList = new ListTag();
      ListTag facesList = new ListTag();
      int bestType = 0;
      if (index > 255) {
         bestType = 1;
      }

      if (index > 32767) {
         bestType = 2;
      }

      for (Entry<String, JsonElement> entry : faces.entrySet()) {
         BlockbenchModel.MeshFace face = (BlockbenchModel.MeshFace)GSON.fromJson(entry.getValue(), BlockbenchModel.MeshFace.class);
         if (face.texture != null && face.vertices != null && face.uv != null && face.vertices.length >= 3 && face.vertices.length <= 4) {
            BlockbenchModelParser.TextureData texture = this.textureMap.get(this.textureIdMap.get(face.texture));
            if (texture != null) {
               short k = (short)((texture.id << 4) + face.vertices.length);
               texesList.add(ShortTag.valueOf(k));
               if (face.vertices.length > 3) {
                  reorderVertices(face.vertices, verticesMap, verticesList);
               }

               for (String vertex : face.vertices) {
                  Tag bestVal = (Tag)(switch (bestType) {
                     case 0 -> ByteTag.valueOf(verticesMap.get(vertex).byteValue());
                     case 1 -> ShortTag.valueOf(verticesMap.get(vertex).shortValue());
                     case 2 -> IntTag.valueOf(verticesMap.get(vertex));
                     default -> throw new IllegalStateException("Unexpected value: " + bestType);
                  });
                  facesList.add(bestVal);
                  float[] uv = jsonToFloat(face.uv.getAsJsonArray(vertex));
                  float u = uv[0] * texture.fixedSize[0];
                  float v = uv[1] * texture.fixedSize[1];
                  uvsList.add(FloatTag.valueOf(u));
                  uvsList.add(FloatTag.valueOf(v));
               }
            }
         }
      }

      nbt.put("vtx", verticesList);
      nbt.put("tex", texesList);
      nbt.put("fac", facesList);
      nbt.put("uvs", uvsList);
      return nbt;
   }

   private static void reorderVertices(String[] vertexNames, Map<String, Integer> nameToIndex, ListTag vertices) {
      readVectors(vertexNames, nameToIndex, vertices);
      if (testOppositeSides(v2, v3, v1, v4)) {
         String temp = vertexNames[2];
         vertexNames[2] = vertexNames[1];
         vertexNames[1] = vertexNames[0];
         vertexNames[0] = temp;
      } else if (testOppositeSides(v1, v2, v3, v4)) {
         String temp = vertexNames[2];
         vertexNames[2] = vertexNames[1];
         vertexNames[1] = temp;
      }
   }

   private static void readVectors(String[] vertexNames, Map<String, Integer> nameToIndex, ListTag vertices) {
      int i = nameToIndex.get(vertexNames[0]);
      float f0 = (Float)vertices.getFloat(3 * i).get();
      float f1 = (Float)vertices.getFloat(3 * i + 1).get();
      float f2 = (Float)vertices.getFloat(3 * i + 2).get();
      v1.set(f0, f1, f2);
      i = nameToIndex.get(vertexNames[1]);
      f0 = (Float)vertices.getFloat(3 * i).get();
      f1 = (Float)vertices.getFloat(3 * i + 1).get();
      f2 = (Float)vertices.getFloat(3 * i + 2).get();
      v2.set(f0, f1, f2);
      i = nameToIndex.get(vertexNames[2]);
      f0 = (Float)vertices.getFloat(3 * i).get();
      f1 = (Float)vertices.getFloat(3 * i + 1).get();
      f2 = (Float)vertices.getFloat(3 * i + 2).get();
      v3.set(f0, f1, f2);
      i = nameToIndex.get(vertexNames[3]);
      f0 = (Float)vertices.getFloat(3 * i).get();
      f1 = (Float)vertices.getFloat(3 * i + 1).get();
      f2 = (Float)vertices.getFloat(3 * i + 2).get();
      v4.set(f0, f1, f2);
   }

   private static boolean testOppositeSides(FiguraVec3 linePoint1, FiguraVec3 linePoint2, FiguraVec3 point1, FiguraVec3 point2) {
      t1.set(linePoint1);
      t2.set(linePoint2);
      t3.set(point1);
      t4.set(point2);
      t2.subtract(t1);
      t3.subtract(t1);
      t4.subtract(t1);
      t1.set(t2);
      t1.cross(t3);
      t2.cross(t4);
      return t1.dot(t2) < 0.0;
   }

   private void parseAnimations(List<CompoundTag> list, BlockbenchModel.Animation[] animations, String modelName, String folders) {
      if (animations != null) {
         int i = 0;

         for (BlockbenchModel.Animation animation : animations) {
            CompoundTag animNbt = new CompoundTag();
            animNbt.putString("mdl", folders.isBlank() ? modelName : folders + modelName);
            animNbt.putString("name", orEmpty(animation.name));
            if (animation.loop != null && !animation.loop.equals("once")) {
               animNbt.putString("loop", animation.loop);
            }

            if (animation.override != null && animation.override) {
               animNbt.putBoolean("ovr", true);
            }

            if (animation.length != 0.0F) {
               animNbt.putFloat("len", animation.length);
            }

            float offset = toFloat(animation.anim_time_update, 0.0F);
            if (offset != 0.0F) {
               animNbt.putFloat("off", offset);
            }

            float blend = toFloat(animation.blend_weight, 1.0F);
            if (blend != 1.0F) {
               animNbt.putFloat("bld", blend);
            }

            float startDelay = toFloat(animation.start_delay, 0.0F);
            if (startDelay != 0.0F) {
               animNbt.putFloat("sdel", startDelay);
            }

            float loopDelay = toFloat(animation.loop_delay, 0.0F);
            if (loopDelay != 0.0F) {
               animNbt.putFloat("ldel", loopDelay);
            }

            if (animation.animators == null) {
               animation.animators = new JsonObject();
            }

            for (Entry<String, JsonElement> entry : animation.animators.entrySet()) {
               String id = entry.getKey();
               boolean effect = id.equalsIgnoreCase("effects");
               ListTag effectData = new ListTag();
               ListTag rotData = new ListTag();
               ListTag posData = new ListTag();
               ListTag scaleData = new ListTag();
               JsonObject animationData = entry.getValue().getAsJsonObject();

               for (JsonElement keyframeJson : animationData.get("keyframes").getAsJsonArray()) {
                  BlockbenchModel.KeyFrame keyFrame = (BlockbenchModel.KeyFrame)GSON.fromJson(keyframeJson, BlockbenchModel.KeyFrame.class);
                  CompoundTag keyframeNbt = new CompoundTag();
                  keyframeNbt.putFloat("time", keyFrame.time);
                  if (effect) {
                     if (keyFrame.channel.equalsIgnoreCase("timeline")) {
                        keyframeNbt.putString("src", keyFrame.data_points.get(0).getAsJsonObject().get("script").getAsString());
                        effectData.add(keyframeNbt);
                     }
                  } else {
                     keyframeNbt.putString("int", orDefault(keyFrame.interpolation, "linear"));
                     JsonObject dataPoints = keyFrame.data_points.get(0).getAsJsonObject();
                     keyframeNbt.put("pre", this.parseKeyFrameData(dataPoints, keyFrame.channel));
                     if (keyFrame.data_points.size() > 1) {
                        JsonObject endDataPoints = keyFrame.data_points.get(1).getAsJsonObject();
                        keyframeNbt.put("end", this.parseKeyFrameData(endDataPoints, keyFrame.channel));
                     }

                     if (notZero(keyFrame.bezier_left_value)) {
                        keyframeNbt.put("bl", toNbtList(keyFrame.bezier_left_value));
                     }

                     if (notZero(keyFrame.bezier_right_value)) {
                        keyframeNbt.put("br", toNbtList(keyFrame.bezier_right_value));
                     }

                     if (isDifferent(keyFrame.bezier_left_time, -0.1F)) {
                        keyframeNbt.put("blt", toNbtList(keyFrame.bezier_left_time));
                     }

                     if (isDifferent(keyFrame.bezier_right_time, 0.1F)) {
                        keyframeNbt.put("brt", toNbtList(keyFrame.bezier_right_time));
                     }

                     switch (keyFrame.channel) {
                        case "position":
                           posData.add(keyframeNbt);
                           break;
                        case "rotation":
                           rotData.add(keyframeNbt);
                           break;
                        case "scale":
                           scaleData.add(keyframeNbt);
                     }
                  }
               }

               if (effect) {
                  animNbt.put("code", effectData);
               } else {
                  ListTag partAnimations = this.animationMap.containsKey(id) ? this.animationMap.get(id) : new ListTag();
                  CompoundTag nbt = new CompoundTag();
                  CompoundTag channels = new CompoundTag();
                  if (!rotData.isEmpty()) {
                     JsonElement globalRotJson = animationData.get("rotation_global");
                     if (globalRotJson != null && globalRotJson.getAsBoolean()) {
                        channels.put("grot", rotData);
                     } else {
                        channels.put("rot", rotData);
                     }
                  }

                  if (!posData.isEmpty()) {
                     channels.put("pos", posData);
                  }

                  if (!scaleData.isEmpty()) {
                     channels.put("scl", scaleData);
                  }

                  if (!channels.isEmpty()) {
                     nbt.putInt("id", i + this.animationOffset);
                     nbt.put("data", channels);
                  }

                  if (!nbt.isEmpty()) {
                     partAnimations.add(nbt);
                  }

                  if (!partAnimations.isEmpty()) {
                     this.animationMap.put(id, partAnimations);
                  }
               }
            }

            list.add(animNbt);
            i++;
         }

         this.animationOffset = this.animationOffset + list.size();
      }
   }

   private ListTag parseKeyFrameData(JsonObject object, String channel) {
      BlockbenchModel.KeyFrameData frameData = (BlockbenchModel.KeyFrameData)GSON.fromJson(object, BlockbenchModel.KeyFrameData.class);
      float fallback = channel.equals("scale") ? 1.0F : 0.0F;
      Object x = keyFrameData(frameData.x, fallback);
      Object y = keyFrameData(frameData.y, fallback);
      Object z = keyFrameData(frameData.z, fallback);
      ListTag nbt = new ListTag();
      if (x instanceof Float xx && y instanceof Float yy && z instanceof Float zz) {
         nbt.add(FloatTag.valueOf(xx));
         nbt.add(FloatTag.valueOf(yy));
         nbt.add(FloatTag.valueOf(zz));
      } else {
         nbt.add(StringTag.valueOf(String.valueOf(x)));
         nbt.add(StringTag.valueOf(String.valueOf(y)));
         nbt.add(StringTag.valueOf(String.valueOf(z)));
      }

      return nbt;
   }

   private ListTag parseOutliner(JsonArray outliner, boolean parentVsb) {
      ListTag children = new ListTag();
      if (outliner == null) {
         return children;
      }

      for (JsonElement element : outliner) {
         if (element instanceof JsonPrimitive) {
            String key = element.getAsString();
            if (this.elementMap.containsKey(key)) {
               CompoundTag elementNbt = this.elementMap.get(key);
               if (elementNbt.contains("vsb") && elementNbt.getBooleanOr("vsb", false) == parentVsb) {
                  elementNbt.remove("vsb");
               }

               children.add(elementNbt);
            }
         } else {
            CompoundTag groupNbt = new CompoundTag();
            BlockbenchModel.GroupElement group = (BlockbenchModel.GroupElement)GSON.fromJson(element, BlockbenchModel.GroupElement.class);
            if (group.export == null || group.export) {
               groupNbt.putString("name", orEmpty(group.name));
               boolean thisVisibility = group.visibility == null || group.visibility;
               if (thisVisibility != parentVsb) {
                  groupNbt.putBoolean("vsb", thisVisibility);
               }

               if (notZero(group.origin)) {
                  groupNbt.put("piv", toNbtList(group.origin));
               }

               if (notZero(group.rotation)) {
                  groupNbt.put("rot", toNbtList(group.rotation));
               }

               parseParent(group.name, groupNbt);
               if (group.children != null && !group.children.isEmpty()) {
                  groupNbt.put("chld", this.parseOutliner(group.children, thisVisibility));
               }

               if (this.animationMap.containsKey(group.uuid)) {
                  groupNbt.put("anim", (Tag)this.animationMap.get(group.uuid));
               }

               children.add(groupNbt);
            }
         }
      }

      return children;
   }

   public static ListTag toNbtList(float[] floats) {
      ListTag list = new ListTag();
      int bestType = 0;

      for (float f : floats) {
         if (Math.rint(f) - f != 0.0) {
            bestType = 2;
            break;
         }

         if (f < -127.0F || f >= 128.0F) {
            bestType = 1;
         }

         if (f < -16383.0F || f >= 16384.0F) {
            bestType = 2;
            break;
         }
      }

      for (float f : floats) {
         switch (bestType) {
            case 0:
               list.add(ByteTag.valueOf((byte)f));
               break;
            case 1:
               list.add(ShortTag.valueOf((short)f));
               break;
            case 2:
               list.add(FloatTag.valueOf(f));
         }
      }

      return list;
   }

   public static boolean notZero(float[] floats) {
      return isDifferent(floats, 0.0F);
   }

   public static boolean isDifferent(float[] floats, float value) {
      if (floats == null) {
         return false;
      }

      for (float f : floats) {
         if (f != value) {
            return true;
         }
      }

      return false;
   }

   public static float toFloat(String input, float fallback) {
      try {
         return Float.parseFloat(input);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static String orEmpty(String input) {
      return input == null ? "" : input;
   }

   private static String orDefault(String input, String fallback) {
      return input == null ? fallback : input;
   }

   public static Object keyFrameData(String input, float fallback) {
      try {
         return Float.parseFloat(input);
      } catch (Exception ignored) {
         return input != null && !input.isBlank() ? input : fallback;
      }
   }

   public static int[] getTextureSize(byte[] texture) {
      int w = texture[16] & 255;
      w = (w << 8) + (texture[17] & 255);
      w = (w << 8) + (texture[18] & 255);
      w = (w << 8) + (texture[19] & 255);
      int h = texture[20] & 255;
      h = (h << 8) + (texture[21] & 255);
      h = (h << 8) + (texture[22] & 255);
      h = (h << 8) + (texture[23] & 255);
      return new int[]{w, h};
   }

   public static float[] jsonToFloat(JsonArray array) {
      float[] f = new float[array.size()];
      int i = 0;

      for (JsonElement element : array) {
         f[i] = element.isJsonNull() ? 0.0F : element.getAsFloat();
         i++;
      }

      return f;
   }

   public record ModelData(CompoundTag textures, List<CompoundTag> animationList, CompoundTag modelNbt) {
   }

   private record TextureData(int id, float[] fixedSize) {
   }
}
