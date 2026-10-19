package org.figuramc.figura.model.rendering.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;

import com.mojang.blaze3d.GpuFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.NativeImageExtension;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.mixin.render.TextureManagerAccessor;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.LuaUtils;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.lwjgl.BufferUtils;

@LuaWhitelist
@LuaTypeDoc(name = "Texture", value = "texture")
public class FiguraTexture extends SimpleTexture {
   private boolean registered = false;
   private boolean dirty = true;
   private boolean modified = false;
   private final String name;
   private final Avatar owner;
   private final NativeImage nativeImageTexture;
   private NativeImage backup;
   private boolean isClosed = false;

   public FiguraTexture(Avatar owner, String name, byte[] data) {
      super(FiguraIdentifier.of("avatar_tex/" + owner.owner + "/" + UUID.randomUUID()));

      NativeImage image;
      try {
         ByteBuffer wrapper = BufferUtils.createByteBuffer(data.length);
         wrapper.put(data);
         wrapper.rewind();
         image = NativeImage.read(wrapper);
      } catch (IOException e) {
         FiguraMod.LOGGER.error("", e);
         image = new NativeImage(1, 1, true);
      }

      this.nativeImageTexture = image;
      this.name = name;
      this.owner = owner;
   }

   public FiguraTexture(Avatar owner, String name, int width, int height) {
      super(FiguraIdentifier.of("avatar_tex/" + owner.owner + "/" + UUID.randomUUID()));
      this.nativeImageTexture = new NativeImage(width, height, true);
      this.name = name;
      this.owner = owner;
   }

   public FiguraTexture(Avatar owner, String name, NativeImage image) {
      super(FiguraIdentifier.of("avatar_tex/" + owner.owner + "/custom/" + UUID.randomUUID()));
      this.nativeImageTexture = image;
      this.name = name;
      this.owner = owner;
   }

   @NotNull
   public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
      return new TextureContents(this.copy(), new TextureMetadataSection(false, false, MipmapStrategy.AUTO, 0.0F));
   }

   public void apply(TextureContents textureContents) {
      this.uploadIfDirty(false, false);
   }

   public void closeFromRenderThread() {
      Minecraft.getInstance().execute(this::close);
   }

   public void close() {
      if (!this.isClosed) {
         this.isClosed = true;
         if (this.nativeImageTexture != null) {
            this.nativeImageTexture.close();
         }

         if (this.backup != null) {
            this.backup.close();
         }

         super.close();
         ((TextureManagerAccessor)Minecraft.getInstance().getTextureManager()).getByPath().remove(this.getLocation());
      }
   }

   public void uploadIfDirty(boolean clamp, boolean blur) {
      if (!this.registered) {
         Minecraft.getInstance().getTextureManager().register(this.getLocation(), this);
         this.registered = true;
      }

      if (this.dirty && !this.isClosed && this.nativeImageTexture != null) {
         this.dirty = false;
         AddressMode addressMode = clamp ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
         FilterMode filterMode = blur ? FilterMode.LINEAR : FilterMode.NEAREST;
         this.sampler = RenderSystem.getSamplerCache().getSampler(addressMode, addressMode, filterMode, filterMode, false);
         this.doLoad(this.nativeImageTexture);
      }
   }

   protected void doLoad(NativeImage nativeImage) {
      GpuDevice gpuDevice = RenderSystem.getDevice();
      this.texture = gpuDevice.createTexture(this.resourceId()::toString, 5,  GpuFormat.RGBA8_UNORM, nativeImage.getWidth(), nativeImage.getHeight(), 1, 1);
      this.textureView = gpuDevice.createTextureView(this.texture);
      gpuDevice.createCommandEncoder().writeToTexture(this.texture, nativeImage);
   }

   public void writeTexture(Path dest) throws IOException {
      this.nativeImageTexture.writeToFile(dest);
   }

   private void backupImage() {
      this.modified = true;
      if (this.backup == null) {
         this.backup = this.copy();
      }
   }

   public NativeImage copy() {
      NativeImage image = new NativeImage(this.nativeImageTexture.format(), this.nativeImageTexture.getWidth(), this.nativeImageTexture.getHeight(), true);
      image.copyFrom(this.nativeImageTexture);
      return image;
   }

   public int getWidth() {
      return this.nativeImageTexture.getWidth();
   }

   public int getHeight() {
      return this.nativeImageTexture.getHeight();
   }

   public Identifier getLocation() {
      return this.resourceId();
   }

   private FiguraVec4 parseColor(String method, Object r, Double g, Double b, Double a) {
      return LuaUtils.parseVec4(method, r, g, b, a, 0.0, 0.0, 0.0, 1.0);
   }

   @LuaWhitelist
   @LuaMethodDoc("texture.get_name")
   public String getName() {
      return this.name;
   }

   @LuaWhitelist
   @LuaMethodDoc("texture.get_path")
   public String getPath() {
      return this.getLocation().toString();
   }

   @LuaWhitelist
   @LuaMethodDoc("texture.get_dimensions")
   public FiguraVec2 getDimensions() {
      return FiguraVec2.of(this.getWidth(), this.getHeight());
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = {Integer.class, Integer.class}, argumentNames = {"x", "y"}), value = "texture.get_pixel")
   public FiguraVec4 getPixel(int x, int y) {
      try {
         return ColorUtils.abgrToRGBA(((NativeImageExtension)(Object)this.nativeImageTexture).figura$getPixelABGR(x, y));
      } catch (Exception e) {
         throw new LuaError(e.getMessage());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = {Integer.class, Integer.class, FiguraVec3.class}, argumentNames = {"x", "y", "rgb"}),
            @LuaMethodOverload(argumentTypes = {Integer.class, Integer.class, FiguraVec4.class}, argumentNames = {"x", "y", "rgba"}),
            @LuaMethodOverload(
               argumentTypes = {Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class},
               argumentNames = {"x", "y", "r", "g", "b", "a"}
            )
      },
      aliases = "pixel",
      value = "texture.set_pixel"
   )
   public FiguraTexture setPixel(int x, int y, Object r, Double g, Double b, Double a) {
      try {
         this.backupImage();
         ((NativeImageExtension)(Object)this.nativeImageTexture).figura$setPixelABGR(x, y, ColorUtils.rgbaToIntABGR(this.parseColor("setPixel", r, g, b, a)));
         return this;
      } catch (Exception e) {
         throw new LuaError(e.getMessage());
      }
   }

   @LuaWhitelist
   public FiguraTexture pixel(int x, int y, Object r, Double g, Double b, Double a) {
      return this.setPixel(x, y, r, g, b, a);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(
               argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, FiguraVec3.class},
               argumentNames = {"x", "y", "width", "height", "rgb"}
            ),
            @LuaMethodOverload(
               argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, FiguraVec4.class},
               argumentNames = {"x", "y", "width", "height", "rgba"}
            ),
            @LuaMethodOverload(
               argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class},
               argumentNames = {"x", "y", "width", "height", "r", "g", "b", "a"}
            )
      },
      value = "texture.fill"
   )
   public FiguraTexture fill(int x, int y, int width, int height, Object r, Double g, Double b, Double a) {
      try {
         this.backupImage();
         this.nativeImageTexture.fillRect(x, y, width, height, ColorUtils.rgbaToIntABGR(this.parseColor("fill", r, g, b, a)));
         return this;
      } catch (Exception e) {
         throw new LuaError(e.getMessage());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("texture.update")
   public FiguraTexture update() {
      this.dirty = true;
      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("texture.restore")
   public FiguraTexture restore() {
      if (this.modified) {
         this.nativeImageTexture.copyFrom(this.backup);
         this.modified = false;
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("texture.save")
   public String save() {
      try {
         return Base64.getEncoder().encodeToString(((NativeImageExtension)(Object)this.nativeImageTexture).figura$asByteArray());
      } catch (Exception e) {
         throw new LuaError(e.getMessage());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(
         argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, LuaFunction.class},
         argumentNames = {"x", "y", "width", "height", "func"}
      ),
      value = "texture.apply_func"
   )
   public FiguraTexture applyFunc(int x, int y, int width, int height, @LuaNotNil LuaFunction function) {
      for (int i = y; i < y + height; i++) {
         for (int j = x; j < x + width; j++) {
            FiguraVec4 color = this.getPixel(j, i);
            LuaValue result = function.call(this.owner.luaRuntime.typeManager.javaToLua(color).arg1(), LuaValue.valueOf(j), LuaValue.valueOf(i));
            if (!result.isnil() && result.isuserdata(FiguraVec4.class)) {
               this.setPixel(j, i, result.checkuserdata(FiguraVec4.class), null, null, null);
            }
         }
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(
         argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, FiguraMat4.class, Boolean.class},
         argumentNames = {"x", "y", "width", "height", "matrix", "clip"}
      ),
      value = "texture.apply_matrix"
   )
   public FiguraTexture applyMatrix(int x, int y, int width, int height, @LuaNotNil FiguraMat4 matrix, boolean clip) {
      for (int i = y; i < y + height; i++) {
         for (int j = x; j < x + width; j++) {
            FiguraVec4 color = this.getPixel(j, i);
            color.transform(matrix);
            if (clip) {
               color.x = Math.max(0.0, Math.min(color.x, 1.0));
               color.y = Math.max(0.0, Math.min(color.y, 1.0));
               color.z = Math.max(0.0, Math.min(color.z, 1.0));
               color.w = Math.max(0.0, Math.min(color.w, 1.0));
            }

            this.setPixel(j, i, color, null, null, null);
         }
      }

      return this;
   }

   @LuaWhitelist
   public Object __index(String arg) {
      return "name".equals(arg) ? this.name : null;
   }

   public String toString() {
      return this.name + " (" + this.getWidth() + "x" + this.getHeight() + ") (Texture)";
   }
}
