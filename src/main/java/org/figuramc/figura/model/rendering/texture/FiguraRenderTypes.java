package org.figuramc.figura.model.rendering.texture;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.FiguraRenderTypeFactory;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public enum FiguraRenderTypes {
   NONE(null),
   CUTOUT(RenderTypes::entityCutout),
   CUTOUT_CULL(RenderTypes::entityCutoutCull),
   CUTOUT_EMISSIVE_SOLID(resourceLocation -> FiguraRenderTypes.FiguraRenderType.CUTOUT_EMISSIVE_SOLID.apply(resourceLocation, true)),
   TRANSLUCENT(RenderTypes::entityTranslucent),
   TRANSLUCENT_CULL(RenderTypes::entityTranslucentCullItemTarget),
   EMISSIVE(RenderTypes::eyes),
   EMISSIVE_SOLID(resourceLocation -> RenderTypes.beaconBeam(resourceLocation, false)),
   EYES(RenderTypes::eyes),
   END_PORTAL(t -> RenderTypes.endPortal(), false),
   END_GATEWAY(t -> RenderTypes.endGateway(), false),
   TEXTURED_PORTAL(FiguraRenderTypes.FiguraRenderType.TEXTURED_PORTAL),
   GLINT(t -> RenderTypes.entityGlint(), false, false),
   GLINT2(t -> RenderTypes.glint(), false, false),
   TEXTURED_GLINT(FiguraRenderTypes.FiguraRenderType.TEXTURED_GLINT, true, false),
   LINES(t -> RenderTypes.lines(), false),
   LINES_STRIP(t -> RenderTypes.lines(), false),
   SOLID(RenderTypes::entitySolid),
   BLURRY(FiguraRenderTypes.FiguraRenderType.BLURRY);

   private final Function<Identifier, RenderType> func;
   private final boolean texture;
   private final boolean offset;

   FiguraRenderTypes(Function<Identifier, RenderType> func) {
      this(func, true);
   }

   FiguraRenderTypes(Function<Identifier, RenderType> func, boolean texture) {
      this(func, texture, true);
   }

   FiguraRenderTypes(Function<Identifier, RenderType> func, boolean texture, boolean offset) {
      this.func = func;
      this.texture = texture;
      this.offset = offset;
   }

   public boolean isOffset() {
      return this.offset;
   }

   public RenderType get(Identifier id) {
      if (!this.texture) {
         return this.func.apply(id);
      } else {
         return id != null && this.func != null ? this.func.apply(id) : null;
      }
   }

   public static class FiguraRenderPipelines {
      public static final RenderPipeline FIGURA_SOLID = RenderPipelines.ENTITY_SOLID;
   }

   private static class FiguraRenderType {
      private static final BiFunction<Identifier, Boolean, RenderType> CUTOUT_EMISSIVE_SOLID = Util.memoize(
         (texture, affectsOutline) -> FiguraRenderTypeFactory.create(
            "figura_cutout_emissive_solid",
            RenderSetup.builder(RenderPipelines.BEACON_BEAM_TRANSLUCENT)               .withTexture("Sampler0", texture)
               .affectsCrumbling()
               .sortOnUpload()
               .useOverlay()
               .setOutline(affectsOutline ? OutlineProperty.AFFECTS_OUTLINE : OutlineProperty.NONE)
               .createRenderSetup()
         )
      );
      public static final Function<Identifier, RenderType> TEXTURED_PORTAL = Util.memoize(
         texture -> FiguraRenderTypeFactory.create(
            "figura_textured_portal",
            RenderSetup.builder(RenderPipelines.END_GATEWAY)               .withTexture("Sampler0", texture)
               .withTexture("Sampler1", texture)
               .setOutline(OutlineProperty.NONE)
               .createRenderSetup()
         )
      );
      public static final Function<Identifier, RenderType> BLURRY = Util.memoize(
         texture -> FiguraRenderTypeFactory.create(
            "figura_blurry",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)               .affectsCrumbling()
               .sortOnUpload()
               .useLightmap()
               .useOverlay()
               .withTexture(
                  "Sampler0",
                  texture,
                  () -> {
                     GpuDevice device = RenderSystem.getDevice();
                     AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
                     return device.createSampler(
                        abstractTexture.getSampler().getAddressModeU(),
                        abstractTexture.getSampler().getAddressModeV(),
                        FilterMode.LINEAR,
                        FilterMode.LINEAR,
                        abstractTexture.getSampler().getMaxAnisotropy(),
                        abstractTexture.getSampler().getMaxLod()
                     );
                  }
               )
               .setOutline(OutlineProperty.AFFECTS_OUTLINE)
               .createRenderSetup()
         )
      );
      public static final Function<Identifier, RenderType> TEXTURED_GLINT = Util.memoize(
         texture -> FiguraRenderTypeFactory.create(
            "figura_textured_glint_direct",
            RenderSetup.builder(RenderPipelines.GLINT)               .withTexture("Sampler0", texture)
               .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
               .createRenderSetup()
         )
      );
   }
}
