package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraRenderTypes;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "SpriteTask", value = "sprite_task")
public class SpriteTask extends RenderTask {
   private Identifier texture;
   private int textureW = -1;
   private int textureH = -1;
   private int width;
   private int height;
   private int regionW;
   private int regionH;
   private float u = 0.0F;
   private float v = 0.0F;
   private int r = 255;
   private int g = 255;
   private int b = 255;
   private int a = 255;
   private FiguraRenderTypes renderType = FiguraRenderTypes.TRANSLUCENT;
   private final List<Vertex> vertices = new ArrayList<>(4);

   public SpriteTask(String name, Avatar owner, FiguraModelPart parent) {
      super(name, owner, parent);
   }

   @Override
   public void render(PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
      if (this.a != 0) {
         poseStack.scale(-1.0F, -1.0F, 1.0F);
         int newLight = this.customization.light != null ? this.customization.light : light;
         int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;
         buffer.submitCustomGeometry(poseStack, this.renderType.get(this.texture), (pose, consumer) -> {
            for (Vertex v : this.vertices) {
               consumer.addVertex(pose, v.x, v.y, v.z)
                  .setColor(this.r, this.g, this.b, this.a)
                  .setUv(v.u, v.v)
                  .setOverlay(newOverlay)
                  .setLight(newLight)
                  .setNormal(pose, v.nx, v.ny, v.nz);
            }
         });
      }
   }

   @Override
   public int getComplexity() {
      return 1;
   }

   @Override
   public boolean shouldRender() {
      return super.shouldRender() && this.texture != null && this.renderType != FiguraRenderTypes.NONE;
   }

   private void recalculateVertices() {
      float u2 = this.u + (float)this.regionW / this.textureW;
      float v2 = this.v + (float)this.regionH / this.textureH;
      this.vertices.clear();
      this.vertices.add(new Vertex(0.0F, this.height, 0.0F, this.u, v2, 0.0F, 0.0F, -1.0F));
      this.vertices.add(new Vertex(this.width, this.height, 0.0F, u2, v2, 0.0F, 0.0F, -1.0F));
      this.vertices.add(new Vertex(this.width, 0.0F, 0.0F, u2, this.v, 0.0F, 0.0F, -1.0F));
      this.vertices.add(new Vertex(0.0F, 0.0F, 0.0F, this.u, this.v, 0.0F, 0.0F, -1.0F));
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_texture")
   public String getTexture() {
      return this.texture == null ? null : this.texture.toString();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = {String.class, Integer.class, Integer.class}, argumentNames = {"textureLocation", "width", "height"}),
            @LuaMethodOverload(argumentTypes = FiguraTexture.class, argumentNames = "texture"),
            @LuaMethodOverload(argumentTypes = {FiguraTexture.class, Integer.class, Integer.class}, argumentNames = {"texture", "width", "height"}),
            @LuaMethodOverload(argumentTypes = {String.class, FiguraVec2.class}, argumentNames = {"textureLocation", "dimensions"}),
            @LuaMethodOverload(argumentTypes = {FiguraTexture.class, FiguraVec2.class}, argumentNames = {"texture", "dimensions"})
      },
      aliases = "texture",
      value = "sprite_task.set_texture"
   )
   public SpriteTask setTexture(Object texture, Object width, Integer height) {
      if (texture == null) {
         this.texture = null;
         return this;
      }

      FiguraVec2 dimensions = LuaUtils.parseVec2("setTexture", width, height);
      if (width == null || height == null) {
         dimensions = null;
      }

      if (texture instanceof String s) {
         try {
            this.texture = Identifier.parse(s);
         } catch (Exception e) {
            this.texture = MissingTextureAtlasSprite.getLocation();
         }

         if (dimensions == null) {
            throw new LuaError("Texture dimensions cannot be null");
         }
      } else {
         if (!(texture instanceof FiguraTexture tex)) {
            throw new LuaError("Illegal argument to setTexture(): " + texture.getClass().getSimpleName());
         }

         this.texture = tex.getLocation();
         if (dimensions == null) {
            width = tex.getWidth();
            height = tex.getHeight();
         }
      }

      if (dimensions == null || !(dimensions.x <= 0.0) && !(dimensions.y <= 0.0)) {
         if (dimensions != null) {
            this.textureW = this.regionW = this.width = (int)Math.round(dimensions.x);
            this.textureH = this.regionH = this.height = (int)Math.round(dimensions.y);
         }

         this.recalculateVertices();
         return this;
      } else {
         throw new LuaError("Invalid texture size: " + width + "x" + height);
      }
   }

   @LuaWhitelist
   public SpriteTask texture(Object texture, Integer width, Integer height) {
      return this.setTexture(texture, width, height);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_dimensions")
   public FiguraVec2 getDimensions() {
      return FiguraVec2.of(this.textureW, this.textureH);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "dimensions"),
            @LuaMethodOverload(argumentTypes = {Integer.class, Integer.class}, argumentNames = {"width", "height"})
      },
      aliases = "dimensions",
      value = "sprite_task.set_dimensions"
   )
   public SpriteTask setDimensions(Object w, Double h) {
      FiguraVec2 vec = LuaUtils.parseVec2("setDimensions", w, h);
      if (!(vec.x <= 0.0) && !(vec.y <= 0.0)) {
         this.textureW = (int)Math.round(vec.x);
         this.textureH = (int)Math.round(vec.y);
         this.recalculateVertices();
         return this;
      } else {
         throw new LuaError("Invalid dimensions: " + vec.x + "x" + vec.y);
      }
   }

   @LuaWhitelist
   public SpriteTask dimensions(Object w, Double h) {
      return this.setDimensions(w, h);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_size")
   public FiguraVec2 getSize() {
      return FiguraVec2.of(this.width, this.height);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "size"),
            @LuaMethodOverload(argumentTypes = {Integer.class, Integer.class}, argumentNames = {"width", "height"})
      },
      aliases = "size",
      value = "sprite_task.set_size"
   )
   public SpriteTask setSize(Object w, Double h) {
      FiguraVec2 vec = LuaUtils.parseVec2("setSize", w, h);
      this.width = (int)Math.round(vec.x);
      this.height = (int)Math.round(vec.y);
      this.recalculateVertices();
      return this;
   }

   @LuaWhitelist
   public SpriteTask size(Object w, Double h) {
      return this.setSize(w, h);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_region")
   public FiguraVec2 getRegion() {
      return FiguraVec2.of(this.regionW, this.regionH);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "region"),
            @LuaMethodOverload(argumentTypes = {Integer.class, Integer.class}, argumentNames = {"width", "height"})
      },
      aliases = "region",
      value = "sprite_task.set_region"
   )
   public SpriteTask setRegion(Object w, Double h) {
      FiguraVec2 vec = LuaUtils.parseVec2("setRegion", w, h);
      this.regionW = (int)Math.round(vec.x);
      this.regionH = (int)Math.round(vec.y);
      this.recalculateVertices();
      return this;
   }

   @LuaWhitelist
   public SpriteTask region(Object w, Double h) {
      return this.setRegion(w, h);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_uv")
   public FiguraVec2 getUV() {
      return FiguraVec2.of(this.u, this.v);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "uv"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"u", "v"})
      },
      aliases = "uv",
      value = "sprite_task.set_uv"
   )
   public SpriteTask setUV(Object u, Double v) {
      FiguraVec2 vec = LuaUtils.parseVec2("setUV", u, v);
      this.u = (float)vec.x;
      this.v = (float)vec.y;
      this.recalculateVertices();
      return this;
   }

   @LuaWhitelist
   public SpriteTask uv(Object u, Double v) {
      return this.setUV(u, v);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_uv_pixels")
   public FiguraVec2 getUVPixels() {
      if (this.textureW != -1 && this.textureH != -1) {
         return this.getUV().multiply(this.textureW, this.textureH);
      } else {
         throw new LuaError("Cannot call getUVPixels before defining the texture dimensions!");
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "uv"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"u", "v"})
      },
      aliases = "uvPixels",
      value = "sprite_task.set_uv_pixels"
   )
   public SpriteTask setUVPixels(Object u, Double v) {
      if (this.textureW != -1 && this.textureH != -1) {
         FiguraVec2 uv = LuaUtils.parseVec2("setUVPixels", u, v);
         uv.divide(this.textureW, this.textureH);
         this.setUV(uv.x, uv.y);
         return this;
      } else {
         throw new LuaError("Cannot call setUVPixels before defining the texture dimensions!");
      }
   }

   @LuaWhitelist
   public SpriteTask uvPixels(Object u, Double v) {
      return this.setUVPixels(u, v);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_color")
   public FiguraVec4 getColor() {
      return FiguraVec4.of(this.r, this.g, this.b, this.a).scale(0.003921569F);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "rgb"),
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "rgba"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b", "a"})
      },
      aliases = "color",
      value = "sprite_task.set_color"
   )
   public SpriteTask setColor(Object r, Double g, Double b, Double a) {
      FiguraVec4 vec = LuaUtils.parseVec4("setColor", r, g, b, a, 0.0, 0.0, 0.0, 1.0);
      int i = ColorUtils.rgbaToInt(vec);
      this.r = i >> 24 & 0xFF;
      this.g = i >> 16 & 0xFF;
      this.b = i >> 8 & 0xFF;
      this.a = i & 0xFF;
      return this;
   }

   @LuaWhitelist
   public SpriteTask color(Object r, Double g, Double b, Double a) {
      return this.setColor(r, g, b, a);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_render_type")
   public String getRenderType() {
      return this.renderType.name();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "renderType"),
      aliases = "renderType",
      value = "sprite_task.set_render_type"
   )
   public SpriteTask setRenderType(@LuaNotNil String renderType) {
      try {
         this.renderType = FiguraRenderTypes.valueOf(renderType.toUpperCase(Locale.US));
         return this;
      } catch (Exception ignored) {
         throw new LuaError("Illegal RenderType: \"" + renderType + "\".");
      }
   }

   @LuaWhitelist
   public SpriteTask renderType(@LuaNotNil String renderType) {
      return this.setRenderType(renderType);
   }

   @LuaWhitelist
   @LuaMethodDoc("sprite_task.get_vertices")
   public List<Vertex> getVertices() {
      return this.vertices;
   }

   @Override
   public String toString() {
      return this.name + " (Sprite Render Task)";
   }
}
