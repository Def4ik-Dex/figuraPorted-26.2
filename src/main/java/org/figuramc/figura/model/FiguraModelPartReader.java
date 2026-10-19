package org.figuramc.figura.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.animation.Interpolation;
import org.figuramc.figura.animation.Keyframe;
import org.figuramc.figura.animation.TransformType;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraRenderTypes;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.figuramc.figura.utils.MathUtils;

public class FiguraModelPartReader {
   private static final Map<String, FiguraVec3[]> faceData = ImmutableMap.of(
      "n",
      new FiguraVec3[]{
         FiguraVec3.of(1.0, 0.0, 0.0), FiguraVec3.of(0.0, 0.0, 0.0), FiguraVec3.of(0.0, 1.0, 0.0), FiguraVec3.of(1.0, 1.0, 0.0), FiguraVec3.of(0.0, 0.0, -1.0)
      },
      "s",
      new FiguraVec3[]{
         FiguraVec3.of(0.0, 0.0, 1.0), FiguraVec3.of(1.0, 0.0, 1.0), FiguraVec3.of(1.0, 1.0, 1.0), FiguraVec3.of(0.0, 1.0, 1.0), FiguraVec3.of(0.0, 0.0, 1.0)
      },
      "e",
      new FiguraVec3[]{
         FiguraVec3.of(1.0, 0.0, 1.0), FiguraVec3.of(1.0, 0.0, 0.0), FiguraVec3.of(1.0, 1.0, 0.0), FiguraVec3.of(1.0, 1.0, 1.0), FiguraVec3.of(1.0, 0.0, 0.0)
      },
      "w",
      new FiguraVec3[]{
         FiguraVec3.of(0.0, 0.0, 0.0), FiguraVec3.of(0.0, 0.0, 1.0), FiguraVec3.of(0.0, 1.0, 1.0), FiguraVec3.of(0.0, 1.0, 0.0), FiguraVec3.of(-1.0, 0.0, 0.0)
      },
      "u",
      new FiguraVec3[]{
         FiguraVec3.of(0.0, 1.0, 1.0), FiguraVec3.of(1.0, 1.0, 1.0), FiguraVec3.of(1.0, 1.0, 0.0), FiguraVec3.of(0.0, 1.0, 0.0), FiguraVec3.of(0.0, 1.0, 0.0)
      },
      "d",
      new FiguraVec3[]{
         FiguraVec3.of(0.0, 0.0, 0.0), FiguraVec3.of(1.0, 0.0, 0.0), FiguraVec3.of(1.0, 0.0, 1.0), FiguraVec3.of(0.0, 0.0, 1.0), FiguraVec3.of(0.0, -1.0, 0.0)
      }
   );
   private static final FiguraVec2[] uvValues = new FiguraVec2[]{
      FiguraVec2.of(0.0, 1.0), FiguraVec2.of(1.0, 1.0), FiguraVec2.of(1.0, 0.0), FiguraVec2.of(0.0, 0.0)
   };

   public static FiguraModelPart read(Avatar owner, CompoundTag partCompound, List<FiguraTextureSet> textureSets, boolean smoothNormals) {
      String name = partCompound.getStringOr("name", "");
      PartCustomization customization = new PartCustomization();
      customization.needsMatrixRecalculation = true;
      FiguraVec3 rot = FiguraVec3.of();
      readVec3(rot, partCompound, "rot");
      customization.setRot(rot);
      FiguraVec3 piv = FiguraVec3.of();
      readVec3(piv, partCompound, "piv");
      customization.setPivot(piv);
      if (partCompound.contains("primary")) {
         try {
            customization.setPrimaryRenderType(FiguraRenderTypes.valueOf(partCompound.getStringOr("primary", "")));
         } catch (Exception var36) {
         }
      }

      if (partCompound.contains("secondary")) {
         try {
            customization.setSecondaryRenderType(FiguraRenderTypes.valueOf(partCompound.getStringOr("secondary", "")));
         } catch (Exception var35) {
         }
      }

      if (partCompound.contains("vsb")) {
         customization.visible = partCompound.getBooleanOr("vsb", false);
      }

      List<Integer> facesByTexture = new ArrayList<>(0);

      while (textureSets.size() > facesByTexture.size()) {
         facesByTexture.add(0);
      }

      Map<Integer, List<Vertex>> vertices = new HashMap<>();
      if (hasCubeData(partCompound)) {
         readCuboid(facesByTexture, partCompound, vertices);
         customization.partType = PartCustomization.PartType.CUBE;
      } else if (hasMeshData(partCompound)) {
         readMesh(facesByTexture, partCompound, vertices);
         customization.partType = PartCustomization.PartType.MESH;
      }

      if (partCompound.contains("smo")) {
         smoothNormals = partCompound.getBooleanOr("smo", false);
      }

      if (Configs.FORCE_SMOOTH_AVATAR.value || smoothNormals && !vertices.isEmpty()) {
         smoothfy(vertices);
      }

      ArrayList<FiguraModelPart> children = new ArrayList<>(0);
      if (partCompound.contains("chld")) {
         for (Tag tag : partCompound.getListOrEmpty("chld")) {
            children.add(read(owner, (CompoundTag)tag, textureSets, smoothNormals));
         }
      }

      FiguraModelPart result = new FiguraModelPart(owner, name, customization, vertices, children);

      for (FiguraModelPart child : children) {
         child.parent = result;
      }

      result.facesByTexture = facesByTexture;
      storeTextures(result, textureSets);
      if (partCompound.contains("pt")) {
         try {
            result.parentType = ParentType.valueOf(partCompound.getStringOr("pt", ""));
         } catch (Exception var34) {
         }
      }

      if (partCompound.contains("anim")) {
         for (Tag tag : partCompound.getListOrEmpty("anim")) {
            CompoundTag compound = (CompoundTag)tag;
            Animation animation;
            if (compound.contains("id") && compound.contains("data") && (animation = owner.animations.get(compound.getIntOr("id", 0))) != null) {
               CompoundTag animNbt = compound.getCompoundOrEmpty("data");

               for (String channelString : animNbt.keySet()) {
                  TransformType type = switch (channelString) {
                     case "pos" -> TransformType.POSITION;
                     case "rot" -> TransformType.ROTATION;
                     case "grot" -> TransformType.GLOBAL_ROT;
                     case "scl" -> TransformType.SCALE;
                     default -> null;
                  };
                  if (type != null) {
                     List<Keyframe> keyframes = new ArrayList<>();

                     for (Tag keyframeTag : animNbt.getListOrEmpty(channelString)) {
                        CompoundTag keyframeNbt = (CompoundTag)keyframeTag;
                        float time = keyframeNbt.getFloatOr("time", 0.0F);

                        Interpolation interpolation;
                        try {
                           interpolation = Interpolation.valueOf(keyframeNbt.getStringOr("int", "").toUpperCase(Locale.US));
                        } catch (Exception e) {
                           FiguraMod.LOGGER
                              .error("Invalid interpolation type in the model {}, something is wrong with this model!", keyframeNbt.getString("int"));
                           FiguraMod.LOGGER.error("", e);
                           continue;
                        }

                        Pair<FiguraVec3, String[]> pre = parseKeyframeData(keyframeNbt, "pre");
                        if (pre == null) {
                           pre = Pair.of(FiguraVec3.of(), null);
                        }

                        Pair<FiguraVec3, String[]> end = parseKeyframeData(keyframeNbt, "end");
                        if (end == null) {
                           end = pre;
                        }

                        FiguraVec3 bezierLeft = FiguraVec3.of();
                        FiguraVec3 bezierRight = FiguraVec3.of();
                        readVec3(bezierLeft, keyframeNbt, "bl");
                        readVec3(bezierRight, keyframeNbt, "br");
                        FiguraVec3 bezierLeftTime = FiguraVec3.of(-0.1, -0.1, -0.1);
                        FiguraVec3 bezierRightTime = FiguraVec3.of(0.1, 0.1, 0.1);
                        readVec3(bezierLeftTime, keyframeNbt, "blt");
                        readVec3(bezierRightTime, keyframeNbt, "brt");
                        bezierLeftTime.add(1.0, 1.0, 1.0);
                        bezierLeftTime = MathUtils.clamp(bezierLeftTime, 0.0, 1.0);
                        bezierRightTime = MathUtils.clamp(bezierRightTime, 0.0, 1.0);
                        keyframes.add(new Keyframe(owner, animation, time, interpolation, pre, end, bezierLeft, bezierRight, bezierLeftTime, bezierRightTime));
                     }

                     keyframes.sort(Keyframe::compareTo);
                     animation.addAnimation(result, new Animation.AnimationChannel(type, keyframes.toArray(new Keyframe[0])));
                  }
               }
            }
         }
      }

      return result;
   }

   private static Pair<FiguraVec3, String[]> parseKeyframeData(CompoundTag keyframeNbt, String tag) {
      if (!keyframeNbt.contains(tag)) {
         return null;
      } else {
         ListTag keyframeVec = keyframeNbt.getListOrEmpty(tag);
         if (!keyframeVec.isEmpty() && isNumberList(keyframeVec)) {
            FiguraVec3 ret = FiguraVec3.of();
            readVec3(ret, keyframeVec);
            return Pair.of(ret, null);
         } else {
            return Pair.of(null, new String[]{keyframeVec.getStringOr(0, ""), keyframeVec.getStringOr(1, ""), keyframeVec.getStringOr(2, "")});
         }
      }
   }

   private static boolean isNumberList(ListTag list) {
      for (Tag tag : list) {
         if (tag.asNumber().isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private static void storeTextures(FiguraModelPart modelPart, List<FiguraTextureSet> textureSets) {
      List<FiguraTextureSet> list = new ArrayList<>(0);

      for (int j = 0; j < modelPart.facesByTexture.size(); j++) {
         list.add(textureSets.get(j));
      }

      modelPart.textures = list;
      int w = -1;
      int h = -1;

      for (FiguraModelPart child : modelPart.children) {
         if (child.textureWidth == -1) {
            modelPart.textureWidth = -1;
            modelPart.textureHeight = -1;
            return;
         }

         if (child.textureWidth != w || child.textureHeight != h) {
            if (w != -1) {
               modelPart.textureWidth = -1;
               modelPart.textureHeight = -1;
               return;
            }

            w = child.textureWidth;
            h = child.textureHeight;
         }
      }

      if (modelPart.customization.partType != PartCustomization.PartType.GROUP) {
         int i = -1;

         for (int j = 0; j < modelPart.facesByTexture.size(); j++) {
            if (modelPart.facesByTexture.get(j) > 0) {
               int realTexWidth = textureSets.get(j).getWidth();
               int realTexHeight = textureSets.get(j).getHeight();
               if (w != -1 && w != realTexWidth || h != -1 && h != realTexHeight) {
                  modelPart.textureWidth = -1;
                  modelPart.textureHeight = -1;
                  return;
               }

               if (i != -1) {
                  modelPart.textureWidth = -1;
                  modelPart.textureHeight = -1;
                  return;
               }

               i = j;
               w = realTexWidth;
               h = realTexHeight;
            }
         }
      }

      modelPart.textureWidth = w;
      modelPart.textureHeight = h;
   }

   private static void readVec3(FiguraVec3 target, CompoundTag tag, String name) {
      if (tag.contains(name)) {
         readVec3(target, (ListTag)tag.get(name));
      }
   }

   private static void readVec3(FiguraVec3 target, ListTag list) {
      switch (((Tag)list.getFirst()).getId()) {
         case 1:
            target.set(((ByteTag)list.get(0)).byteValue(), ((ByteTag)list.get(1)).byteValue(), ((ByteTag)list.get(2)).byteValue());
            break;
         case 2:
            target.set(list.getShortOr(0, (short)0), list.getShortOr(1, (short)0), list.getShortOr(2, (short)0));
            break;
         case 3:
            target.set(list.getIntOr(0, 0), list.getIntOr(1, 0), list.getIntOr(2, 0));
         case 4:
         default:
            break;
         case 5:
            target.set(list.getFloatOr(0, 0.0F), list.getFloatOr(1, 0.0F), list.getFloatOr(2, 0.0F));
      }
   }

   private static void readVec4(FiguraVec4 target, CompoundTag tag, String name) {
      if (tag.contains(name)) {
         ListTag list = (ListTag)tag.get(name);
         switch (((Tag)list.getFirst()).getId()) {
            case 1:
               target.set(
                  ((ByteTag)list.get(0)).byteValue(),
                  ((ByteTag)list.get(1)).byteValue(),
                  ((ByteTag)list.get(2)).byteValue(),
                  ((ByteTag)list.get(3)).byteValue()
               );
               break;
            case 2:
               target.set(list.getShortOr(0, (short)0), list.getShortOr(1, (short)0), list.getShortOr(2, (short)0), list.getShortOr(3, (short)0));
               break;
            case 3:
               target.set(list.getIntOr(0, 0), list.getIntOr(1, 0), list.getIntOr(2, 0), list.getIntOr(3, 0));
            case 4:
            default:
               break;
            case 5:
               target.set(list.getFloatOr(0, 0.0F), list.getFloatOr(1, 0.0F), list.getFloatOr(2, 0.0F), list.getFloatOr(3, 0.0F));
         }
      } else {
         target.set(0.0, 0.0, 0.0, 0.0);
      }
   }

   private static boolean hasCubeData(CompoundTag partCompound) {
      return partCompound.contains("cube_data") && partCompound.getCompound("cube_data").isPresent()
         ? !((CompoundTag)partCompound.getCompound("cube_data").get()).isEmpty()
         : false;
   }

   private static boolean hasMeshData(CompoundTag partCompound) {
      return partCompound.contains("mesh_data") && partCompound.getCompound("mesh_data").isPresent()
         ? !((CompoundTag)partCompound.getCompound("mesh_data").get()).isEmpty()
         : false;
   }

   private static void readCuboid(List<Integer> facesByTexture, CompoundTag data, Map<Integer, List<Vertex>> vertices) {
      FiguraVec3 from = FiguraVec3.of();
      readVec3(from, data, "f");
      FiguraVec3 to = FiguraVec3.of();
      readVec3(to, data, "t");
      double inflate = 0.0;
      if (data.contains("inf")) {
         inflate = data.getFloatOr("inf", 0.0F);
      }

      from.add(-inflate, -inflate, -inflate);
      to.add(inflate, inflate, inflate);
      FiguraVec3 ftDiff = to.copy();
      ftDiff.subtract(from);

      for (String direction : faceData.keySet()) {
         readFace(data.getCompoundOrEmpty("cube_data"), facesByTexture, direction, vertices, from, ftDiff);
      }
   }

   private static void readFace(
      CompoundTag faces, List<Integer> facesByTexture, String direction, Map<Integer, List<Vertex>> vertices, FiguraVec3 from, FiguraVec3 ftDiff
   ) {
      if (faces.contains(direction)) {
         CompoundTag face = faces.getCompoundOrEmpty(direction);
         short texId = face.getShortOr("tex", (short)0);
         facesByTexture.set(texId, facesByTexture.get(texId) + 1);
         FiguraVec3 normal = faceData.get(direction)[4];
         int rotation = (int)(face.getFloatOr("rot", 0.0F) / 90.0F);
         FiguraVec4 uv = FiguraVec4.of();
         readVec4(uv, face, "uv");

         for (int i = 0; i < 4; i++) {
            FiguraVec3 tempPos = ftDiff.copy();
            tempPos.multiply(faceData.get(direction)[i]);
            tempPos.add(from);
            FiguraVec2 normalizedUv = uvValues[(i + rotation) % 4];
            List<Vertex> list = vertices.getOrDefault(Integer.valueOf(texId), new ArrayList<>());
            list.add(
               new Vertex(
                  (float)tempPos.x,
                  (float)tempPos.y,
                  (float)tempPos.z,
                  (float)Mth.lerp(normalizedUv.x, uv.x, uv.z),
                  (float)Mth.lerp(normalizedUv.y, uv.y, uv.w),
                  (float)normal.x,
                  (float)normal.y,
                  (float)normal.z
               )
            );
            vertices.put(Integer.valueOf(texId), list);
         }
      }
   }

   private static void readMesh(List<Integer> facesByTexture, CompoundTag data, Map<Integer, List<Vertex>> vertices) {
      CompoundTag meshData = data.getCompoundOrEmpty("mesh_data");
      ListTag verts = meshData.getListOrEmpty("vtx");
      ListTag uvs = meshData.getListOrEmpty("uvs");
      ListTag tex = meshData.getListOrEmpty("tex");
      int bestType = 0;
      if (verts.size() > 765) {
         bestType = 1;
      }

      if (verts.size() > 98301) {
         bestType = 2;
      }

      ListTag fac = meshData.getListOrEmpty("fac");
      int vi = 0;
      int uvi = 0;
      float[] posArr = new float[12];
      float[] uvArr = new float[8];

      for (int ti = 0; ti < tex.size(); ti++) {
         short packed = tex.getShortOr(ti, (short)0);
         int texId = packed >> 4;
         int numVerts = packed & 15;
         facesByTexture.set(texId, facesByTexture.get(texId) + 1);

         for (int j = 0; j < numVerts; j++) {
            int vid = switch (bestType) {
               case 0 -> ((ByteTag)fac.get(vi + j)).byteValue() & 0xFF;
               case 1 -> fac.getShortOr(vi + j, (short)0) & 65535;
               default -> fac.getIntOr(vi + j, 0);
            };
            posArr[3 * j] = verts.getFloatOr(3 * vid, 0.0F);
            posArr[3 * j + 1] = verts.getFloatOr(3 * vid + 1, 0.0F);
            posArr[3 * j + 2] = verts.getFloatOr(3 * vid + 2, 0.0F);
            uvArr[2 * j] = uvs.getFloatOr(uvi + 2 * j, 0.0F);
            uvArr[2 * j + 1] = uvs.getFloatOr(uvi + 2 * j + 1, 0.0F);
         }

         FiguraVec3 p1 = FiguraVec3.of(posArr[0], posArr[1], posArr[2]);
         FiguraVec3 p2 = FiguraVec3.of(posArr[3], posArr[4], posArr[5]);
         FiguraVec3 p3 = FiguraVec3.of(posArr[6], posArr[7], posArr[8]);
         p3.subtract(p2);
         p1.subtract(p2);
         p3.cross(p1);
         p3.normalize();

         for (int j = 0; j < numVerts; j++) {
            List<Vertex> list = vertices.getOrDefault(texId, new ArrayList<>());
            list.add(new Vertex(posArr[3 * j], posArr[3 * j + 1], posArr[3 * j + 2], uvArr[2 * j], uvArr[2 * j + 1], (float)p3.x, (float)p3.y, (float)p3.z));
            vertices.put(texId, list);
         }

         if (numVerts == 3) {
            List<Vertex> list = vertices.getOrDefault(texId, new ArrayList<>());
            list.add(new Vertex(posArr[6], posArr[7], posArr[8], uvArr[4], uvArr[5], (float)p3.x, (float)p3.y, (float)p3.z));
            vertices.put(texId, list);
         }

         vi += numVerts;
         uvi += 2 * numVerts;
      }
   }

   private static void smoothfy(Map<Integer, List<Vertex>> verticesByTextuers) {
      Map<String, List<Vertex>> verticesByPos = new HashMap<>();

      for (List<Vertex> vertices : verticesByTextuers.values()) {
         for (Vertex vertex : vertices) {
            String id = String.valueOf(vertex.getPos());
            List<Vertex> list = verticesByPos.computeIfAbsent(id, str -> new ArrayList<>(4));
            list.add(vertex);
         }
      }

      for (List<Vertex> vertices : verticesByPos.values()) {
         FiguraVec3 result = FiguraVec3.of();

         for (Vertex vertex : vertices) {
            result.add(vertex.getNormal());
         }

         result.normalize();

         for (Vertex vertex : vertices) {
            vertex.setNormal(result);
         }
      }
   }
}
