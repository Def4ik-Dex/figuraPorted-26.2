package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "TextTask", value = "text_task")
public class TextTask extends RenderTask {
   private String textCached;
   private List<Component> text;
   private TextUtils.Alignment alignment = TextUtils.Alignment.LEFT;
   private boolean shadow = false;
   private boolean outline = false;
   private boolean background = false;
   private boolean seeThrough = false;
   private Integer outlineColor;
   private Integer backgroundColor;
   private int opacity = 255;
   private int width = 0;
   private boolean wrap = true;
   private int cachedComplexity;
   private int cacheWidth;
   private int cacheHeight;

   public TextTask(String name, Avatar owner, FiguraModelPart parent) {
      super(name, owner, parent);
   }

   @Override
   public void render(PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
      if (this.opacity != 0) {
         poseStack.last().pose().scale(-1.0F, -1.0F, -1.0F);
         Font font = Minecraft.getInstance().font;
         int l = this.customization.light != null ? this.customization.light : light;
         int bg = this.backgroundColor != null
            ? ColorUtils.intRGBAToIntARGB(this.backgroundColor)
            : (this.background ? (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24 : 0);
         int out = this.outlineColor != null ? this.outlineColor : 2105376;
         int op = this.opacity << 24 | 16777215;
         DisplayMode displayMode = this.seeThrough ? DisplayMode.SEE_THROUGH : DisplayMode.POLYGON_OFFSET;
         float vertexOffset = this.outline ? -5.0E-4F : 0.0F;
         if (bg != 0) {
            int offset = this.alignment.apply(this.cacheWidth);
            float x1 = -1 - offset;
            float x2 = this.cacheWidth - offset;
            buffer.submitCustomGeometry(
               poseStack,
               this.seeThrough ? RenderTypes.textBackgroundSeeThrough() : RenderTypes.textBackground(),
               (pose, vertexConsumer) -> {
                  vertexConsumer.addVertex(pose, x1, -1.0F, vertexOffset).setColor(bg).setLight(l);
                  vertexConsumer.addVertex(pose, x1, this.cacheHeight, vertexOffset).setColor(bg).setLight(l);
                  vertexConsumer.addVertex(pose, x2, this.cacheHeight, vertexOffset).setColor(bg).setLight(l);
                  vertexConsumer.addVertex(pose, x2, -1.0F, vertexOffset).setColor(bg).setLight(l);
               }
            );
         }

         int i = 0;

         for (int j = 0; i < this.text.size(); j += 9 + 1) {
            Component text = this.text.get(i);
            int x = -this.alignment.apply(font, text);
            buffer.submitText(poseStack, x, j, text.getVisualOrderText(), this.shadow, displayMode, op, 0, this.outline ? out : 0, l);

            i++;
         }
      }
   }

   @Override
   public int getComplexity() {
      return this.cachedComplexity;
   }

   @Override
   public boolean shouldRender() {
      return super.shouldRender() && this.text != null && this.text.size() != 0;
   }

   private void updateText() {
      if (this.textCached == null) {
         this.text = null;
      } else {
         Component component = TextUtils.tryParseJson(this.textCached);
         component = Badges.noBadges4U(component);
         component = Badges.appendBadges(component, this.owner.owner, this.textCached.contains("${badges}") || this.textCached.contains("${segdab}"));
         Component var5 = Emojis.applyEmojis(component);
         component = Emojis.removeBlacklistedEmojis(var5);
         this.text = TextUtils.formatInBounds(component, Minecraft.getInstance().font, this.width, this.wrap);
         Font font = Minecraft.getInstance().font;
         this.cacheWidth = TextUtils.getWidth(this.text, font);
         this.cacheHeight = TextUtils.getHeight(this.text, font);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.get_text")
   public String getText() {
      return this.textCached;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "text"), aliases = "text", value = "text_task.set_text")
   public TextTask setText(String text) {
      this.textCached = text;
      this.updateText();
      if (text != null) {
         this.cachedComplexity = text.length() + 1;
      }

      return this;
   }

   @LuaWhitelist
   public TextTask text(String text) {
      return this.setText(text);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.get_alignment")
   public String getAlignment() {
      return this.alignment.name();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "alignment"),
      aliases = "alignment",
      value = "text_task.set_alignment"
   )
   public TextTask setAlignment(@LuaNotNil String alignment) {
      try {
         this.alignment = TextUtils.Alignment.valueOf(alignment.toUpperCase(Locale.US));
         return this;
      } catch (Exception ignored) {
         throw new LuaError("Invalid alignment type \"" + alignment + "\"");
      }
   }

   @LuaWhitelist
   public TextTask alignment(@LuaNotNil String alignment) {
      return this.setAlignment(alignment);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.has_shadow")
   public boolean hasShadow() {
      return this.shadow;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "shadow"), aliases = "shadow", value = "text_task.set_shadow")
   public TextTask setShadow(boolean shadow) {
      this.shadow = shadow;
      return this;
   }

   @LuaWhitelist
   public TextTask shadow(boolean shadow) {
      return this.setShadow(shadow);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.has_outline")
   public boolean hasOutline() {
      return this.outline;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "outline"),
      aliases = "outline",
      value = "text_task.set_outline"
   )
   public TextTask setOutline(boolean outline) {
      this.outline = outline;
      return this;
   }

   @LuaWhitelist
   public TextTask outline(boolean outline) {
      return this.setOutline(outline);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.get_outline_color")
   public FiguraVec3 getOutlineColor() {
      return this.outlineColor != null ? ColorUtils.intToRGB(this.outlineColor) : FiguraVec3.of();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "color"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b"})
      },
      aliases = "outlineColor",
      value = "text_task.set_outline_color"
   )
   public TextTask setOutlineColor(Object x, Double y, Double z) {
      FiguraVec3 vec = LuaUtils.parseVec3("setOutlineColor", x, y, z);
      this.outlineColor = ColorUtils.rgbToInt(vec);
      return this;
   }

   @LuaWhitelist
   public TextTask outlineColor(Object x, Double y, Double z) {
      return this.setOutlineColor(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.get_width")
   public int getWidth() {
      return this.width;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "width"), aliases = "width", value = "text_task.set_width")
   public TextTask setWidth(int width) {
      this.width = width;
      this.updateText();
      return this;
   }

   @LuaWhitelist
   public TextTask width(int width) {
      return this.setWidth(width);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.has_wrap")
   public boolean hasWrap() {
      return this.wrap;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "wrap"), aliases = "wrap", value = "text_task.set_wrap")
   public TextTask setWrap(boolean wrap) {
      this.wrap = wrap;
      this.updateText();
      return this;
   }

   @LuaWhitelist
   public TextTask wrap(boolean wrap) {
      return this.setWrap(wrap);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.is_see_through")
   public boolean isSeeThrough() {
      return this.seeThrough;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "seeThrough"),
      aliases = "seeThrough",
      value = "text_task.set_see_through"
   )
   public TextTask setSeeThrough(boolean seeThrough) {
      this.seeThrough = seeThrough;
      return this;
   }

   @LuaWhitelist
   public TextTask seeThrough(boolean seeThrough) {
      return this.setSeeThrough(seeThrough);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.has_background")
   public boolean hasBackground() {
      return this.background;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "background"),
      aliases = "background",
      value = "text_task.set_background"
   )
   public TextTask setBackground(boolean background) {
      this.background = background;
      return this;
   }

   @LuaWhitelist
   public TextTask background(boolean background) {
      return this.setBackground(background);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.get_background_color")
   public FiguraVec4 getBackgroundColor() {
      return this.backgroundColor == null ? null : ColorUtils.intToRGBA(this.backgroundColor);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec4.class, argumentNames = "rgba"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b", "a"})
      },
      aliases = "backgroundColor",
      value = "text_task.set_background_color"
   )
   public TextTask setBackgroundColor(Object r, Double g, Double b, Double a) {
      FiguraVec4 vec = LuaUtils.parseVec4("setBackgroundColor", r, g, b, a, 0.0, 0.0, 0.0, Minecraft.getInstance().options.getBackgroundOpacity(0.25F));
      this.backgroundColor = ColorUtils.rgbaToInt(vec);
      return this;
   }

   @LuaWhitelist
   public TextTask backgroundColor(Object r, Double g, Double b, Double a) {
      return this.setBackgroundColor(r, g, b, a);
   }

   @LuaWhitelist
   @LuaMethodDoc("text_task.get_opacity")
   public float getOpacity() {
      return this.opacity / 255.0F;
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "opacity"), aliases = "opacity", value = "text_task.set_opacity")
   public TextTask setOpacity(float opacity) {
      this.opacity = (int)(opacity * 255.0F);
      return this;
   }

   @LuaWhitelist
   public TextTask opacity(float opacity) {
      return this.setOpacity(opacity);
   }

   @Override
   public String toString() {
      return this.name + " (Text Render Task)";
   }
}
