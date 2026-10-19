package org.figuramc.figura.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

public class BlockbenchModel {
   BlockbenchModel.Meta meta;
   BlockbenchModel.Resolution resolution;
   BlockbenchModel.Texture[] textures;
   BlockbenchModel.Element[] elements;
   BlockbenchModel.Animation[] animations;
   JsonArray outliner;

   public static class Animation {
      String name;
      String loop;
      Boolean override;
      float length;
      String anim_time_update;
      String blend_weight;
      String start_delay;
      String loop_delay;
      JsonObject animators;
   }

   public static class CubeFace {
      static final List<String> FACES = List.of("north", "south", "west", "east", "up", "down");
      float[] uv;
      float rotation;
      Integer texture;
   }

   public static class Element {
      String name;
      String type;
      String uuid;
      float[] from;
      float[] to;
      float[] rotation;
      float[] origin;
      float inflate;
      Boolean visibility;
      Boolean export;
      JsonObject faces;
      JsonObject vertices;
   }

   public static class GroupElement {
      String name;
      String uuid;
      Boolean visibility;
      Boolean export;
      float[] origin;
      float[] rotation;
      JsonArray children;
   }

   public static class KeyFrame {
      String channel;
      String interpolation;
      float time;
      JsonArray data_points;
      float[] bezier_left_value;
      float[] bezier_right_value;
      float[] bezier_left_time;
      float[] bezier_right_time;
   }

   public static class KeyFrameData {
      String x;
      String y;
      String z;
   }

   public static class MeshFace {
      JsonObject uv;
      String[] vertices;
      Integer texture;
   }

   public static class Meta {
      String format_version;
      String model_format;
   }

   public static class Resolution {
      int width;
      int height;
   }

   public static class Texture {
      String name;
      String relative_path;
      String source;
      Float width;
      Float height;
      Float uv_width;
      Float uv_height;
   }
}
