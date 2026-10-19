package org.figuramc.figura.exporters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.UUID;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.ParentType;

public class BlockBenchModel {
   private static final String VERSION = "4.9";
   private final JsonObject root = new JsonObject();
   private final ArrayList<BlockBenchModel.Cube> elements = new ArrayList<>();
   private final ArrayList<BlockBenchModel.Element> outliner = new ArrayList<>();
   private final JsonArray textures = new JsonArray();

   public BlockBenchModel(String format) {
      JsonObject meta = new JsonObject();
      meta.addProperty("format_version", "4.9");
      meta.addProperty("model_format", format);
      this.root.add("meta", meta);
   }

   public void setResolution(int w, int h) {
      JsonObject resolution = new JsonObject();
      resolution.addProperty("width", w);
      resolution.addProperty("height", h);
      this.root.add("resolution", resolution);
   }

   public int addImage(String name, String source, int width, int height) {
      int id = this.textures.size();
      JsonObject texture = new JsonObject();
      texture.addProperty("name", name);
      texture.addProperty("id", String.valueOf(id));
      texture.addProperty("source", "data:image/png;base64," + source);
      texture.addProperty("width", width);
      texture.addProperty("height", height);
      texture.addProperty("uv_width", width);
      texture.addProperty("uv_height", height);
      this.textures.add(texture);
      return id;
   }

   public BlockBenchModel.Group addGroup(String name, FiguraVec3 pivot) {
      return this.addGroup(name, pivot, null);
   }

   public BlockBenchModel.Group addGroup(String name, FiguraVec3 pivot, BlockBenchModel.Group parent) {
      BlockBenchModel.Group g = new BlockBenchModel.Group(name, pivot);
      this.addElement(g, parent);
      return g;
   }

   public BlockBenchModel.Group addGroup(ParentType type, FiguraVec3 pivot, BlockBenchModel.Group parent) {
      return this.addGroup(type.name(), pivot, parent);
   }

   public BlockBenchModel.Cube addCube(FiguraVec3 position, FiguraVec3 size, BlockBenchModel.Group parent) {
      return this.addCube(parent == null ? "cube" : parent.getName(), position, size, parent);
   }

   public BlockBenchModel.Cube addCube(String name, FiguraVec3 position, FiguraVec3 size, BlockBenchModel.Group parent) {
      return this.addCube(name, parent == null ? position.plus(size.scaled(0.5)) : parent.getPivot(), position, size, parent);
   }

   public BlockBenchModel.Cube addCube(String name, FiguraVec3 pivot, FiguraVec3 position, FiguraVec3 size, BlockBenchModel.Group parent) {
      BlockBenchModel.Cube c = new BlockBenchModel.Cube(name, pivot, position, size);
      this.addElement(c, parent);
      return c;
   }

   public void addElement(BlockBenchModel.Element element, BlockBenchModel.Group parent) {
      if (element instanceof BlockBenchModel.Cube cube) {
         this.elements.add(cube);
      }

      if (parent == null) {
         this.outliner.add(element);
      } else {
         parent.addChild(element);
      }
   }

   public JsonObject build() {
      JsonArray elements = new JsonArray();

      for (BlockBenchModel.Cube cube : this.elements) {
         elements.add(cube.build());
      }

      JsonArray outliner = new JsonArray();

      for (BlockBenchModel.Element element : this.outliner) {
         outliner.add(element.outliner());
      }

      this.root.add("elements", elements);
      this.root.add("outliner", outliner);
      this.root.add("textures", this.textures);
      return this.root;
   }

   private static JsonArray vec3ToJson(FiguraVec3 vec) {
      JsonArray json = new JsonArray();
      json.add(vec.x);
      json.add(vec.y);
      json.add(vec.z);
      return json;
   }

   private static JsonArray vec4ToJson(FiguraVec4 vec) {
      JsonArray json = new JsonArray();
      json.add(vec.x);
      json.add(vec.y);
      json.add(vec.z);
      json.add(vec.w);
      return json;
   }

   public static class Cube extends BlockBenchModel.Element {
      private final JsonObject faces = new JsonObject();
      private final FiguraVec3 position;
      private final FiguraVec3 size;
      public double inflate;

      public Cube(String name, FiguraVec3 pivot, FiguraVec3 position, FiguraVec3 size) {
         super(name, pivot);
         this.position = position;
         this.size = size;
      }

      @Override
      public JsonObject build() {
         JsonObject cube = super.build();
         cube.add("from", BlockBenchModel.vec3ToJson(this.position));
         cube.add("to", BlockBenchModel.vec3ToJson(this.position.plus(this.size)));
         cube.addProperty("inflate", this.inflate);
         cube.add("faces", this.faces);
         return cube;
      }

      public void addFace(String direction, FiguraVec4 uv, int texture, double scaleW, double scaleH) {
         this.addFace(direction, uv, texture, false, false, scaleW, scaleH);
      }

      public void addFace(String direction, FiguraVec4 uv, int texture, boolean mirrorW, boolean mirrorH, double scaleW, double scaleH) {
         if (Math.signum(scaleW) < 0.0) {
            mirrorW = !mirrorW;
         }

         if (Math.signum(scaleH) < 0.0) {
            mirrorH = !mirrorH;
         }

         if (mirrorW) {
            uv.set(uv.z, uv.y, uv.x, uv.w);
         }

         if (mirrorH) {
            uv.set(uv.x, uv.w, uv.z, uv.y);
         }

         scaleW = Math.abs(scaleW);
         scaleH = Math.abs(scaleH);
         uv.multiply(scaleW, scaleH, scaleW, scaleH);
         JsonObject face = new JsonObject();
         face.add("uv", BlockBenchModel.vec4ToJson(uv));
         face.addProperty("texture", texture);
         this.faces.add(direction, face);
      }

      public void generateBoxFaces(double x, double y, int texture) {
         this.generateBoxFaces(x, y, texture, 1.0, 1.0);
      }

      public void generateBoxFaces(double x, double y, int texture, double scaleW, double scaleH) {
         String[] faces = new String[]{"up", "down", "east", "north", "west", "south"};
         if (Math.signum(scaleW) < 0.0) {
            String t = faces[2];
            faces[2] = faces[4];
            faces[4] = t;
         }

         if (Math.signum(scaleH) < 0.0) {
            String t = faces[0];
            faces[0] = faces[1];
            faces[1] = t;
         }

         this.addFace(faces[0], FiguraVec4.of(x + this.size.z, y, x + this.size.z + this.size.x, y + this.size.z), texture, true, true, scaleW, scaleH);
         this.addFace(
            faces[1],
            FiguraVec4.of(x + this.size.z + this.size.x, y, x + this.size.z + this.size.x + this.size.x, y + this.size.z),
            texture,
            true,
            false,
            scaleW,
            scaleH
         );
         this.addFace(faces[2], FiguraVec4.of(x, y + this.size.z, x + this.size.z, y + this.size.z + this.size.y), texture, scaleW, scaleH);
         this.addFace(
            faces[3], FiguraVec4.of(x + this.size.z, y + this.size.z, x + this.size.z + this.size.x, y + this.size.z + this.size.y), texture, scaleW, scaleH
         );
         this.addFace(
            faces[4],
            FiguraVec4.of(x + this.size.z + this.size.x, y + this.size.z, x + this.size.z + this.size.x + this.size.z, y + this.size.z + this.size.y),
            texture,
            scaleW,
            scaleH
         );
         this.addFace(
            faces[5],
            FiguraVec4.of(
               x + this.size.z + this.size.x + this.size.z,
               y + this.size.z,
               x + this.size.z + this.size.x + this.size.z + this.size.x,
               y + this.size.z + this.size.y
            ),
            texture,
            scaleW,
            scaleH
         );
      }
   }

   private abstract static class Element {
      protected final UUID uuid;
      protected final String name;
      protected final FiguraVec3 pivot;

      public Element(String name, FiguraVec3 pivot) {
         this.name = name;
         this.uuid = UUID.randomUUID();
         this.pivot = pivot;
      }

      public JsonObject build() {
         JsonObject element = new JsonObject();
         element.addProperty("name", this.name);
         element.addProperty("uuid", this.uuid.toString());
         element.add("origin", BlockBenchModel.vec3ToJson(this.pivot));
         return element;
      }

      public JsonElement outliner() {
         return new JsonPrimitive(this.uuid.toString());
      }

      public String getName() {
         return this.name;
      }

      public FiguraVec3 getPivot() {
         return this.pivot;
      }
   }

   public static class Group extends BlockBenchModel.Element {
      private final ArrayList<BlockBenchModel.Element> children = new ArrayList<>();

      public Group(String name, FiguraVec3 pivot) {
         super(name, pivot);
      }

      @Override
      public JsonObject build() {
         JsonArray children = new JsonArray();

         for (BlockBenchModel.Element element : this.children) {
            children.add(element.outliner());
         }

         JsonObject group = super.build();
         group.addProperty("isOpen", true);
         group.add("children", children);
         return group;
      }

      @Override
      public JsonElement outliner() {
         return this.build();
      }

      public void addChild(BlockBenchModel.Element element) {
         this.children.add(element);
      }
   }
}
