package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.ducks.BakedGlyphAccessor;
import org.figuramc.figura.ducks.GlyphStitcherExtension;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlyphStitcher.class)
public class GlyphStitcherMixin implements GlyphStitcherExtension {
   @Unique
   final Map<GlyphInfo, Integer> figura$codePoints = new HashMap<>();
   @Shadow
   @Final
   private Identifier texturePrefix;

   @Redirect(
      method = "stitch",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/font/FontTexture;add(Lcom/mojang/blaze3d/font/GlyphInfo;Lcom/mojang/blaze3d/font/GlyphBitmap;)Lnet/minecraft/client/gui/font/glyphs/BakedSheetGlyph;"
      )
   )
   public BakedSheetGlyph insertDataIntoBakedGlyph(FontTexture instance, GlyphInfo glyphInfo, GlyphBitmap glyphBitmap) {
      BakedSheetGlyph glyph = instance.add(glyphInfo, glyphBitmap);
      if (this.figura$isEmojiFont() && glyph != null && this.figura$codePoints.containsKey(glyphInfo)) {
         ((BakedGlyphAccessor)glyph).figura$setupEmoji(Emojis.getCategoryByFont(this.texturePrefix), this.figura$codePoints.get(glyphInfo));
      }

      return glyph;
   }

   @Unique
   private boolean figura$isEmojiFont() {
      return this.texturePrefix.getNamespace().equals("figura");
   }

   @Override
   public void addCodePoint(GlyphInfo info, int codePoint) {
      this.figura$codePoints.put(info, codePoint);
   }
}
