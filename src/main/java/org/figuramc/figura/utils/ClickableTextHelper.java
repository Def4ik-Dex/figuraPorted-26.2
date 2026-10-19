package org.figuramc.figura.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4i;

public class ClickableTextHelper {
   protected ClickableTextHelper.TextLine[] lines;
   protected HashMap<Vector4i, String> clickUrls = new HashMap<>();
   protected HashMap<Vector4i, Component> hoverText = new HashMap<>();
   protected boolean dirty = true;
   protected Component message;

   public void setMessage(@Nullable Component message) {
      if (this.message != message) {
         this.message = message;
         if (message == null) {
            this.clear();
         } else {
            this.dirty = true;
         }
      }
   }

   public void renderDebug(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
      for (Vector4i area : this.hoverText.keySet()) {
         graphics.outline(x + area.x, y + area.y, area.z - area.x, area.w - area.y, isPointWithinBounds(area, x, y, mouseX, mouseY) ? -16711936 : -65281);
      }

      for (Vector4i area : this.clickUrls.keySet()) {
         graphics.outline(x + area.x, y + area.y, area.z - area.x, area.w - area.y, isPointWithinBounds(area, x, y, mouseX, mouseY) ? -16711936 : -65281);
      }

      graphics.outline(mouseX - 1, mouseY - 1, 3, 3, -16711681);
   }

   public void update(Font font, int lineWidth) {
      if (this.dirty && this.message != null) {
         this.dirty = false;
         this.clear();
         List<ClickableTextHelper.TextLine> lines = new ArrayList<>();

         for (FormattedText curLine : font.getSplitter().splitLines(this.message, lineWidth, Style.EMPTY)) {
            List<ClickableTextHelper.TextNode> nodes = new ArrayList<>();
            TextUtils.formattedTextToText(curLine).visit((style, string) -> {
               nodes.add(new ClickableTextHelper.TextNode(string, style));
               return Optional.empty();
            }, Style.EMPTY);
            lines.add(new ClickableTextHelper.TextLine(nodes.toArray(new ClickableTextHelper.TextNode[0])));
         }

         this.lines = lines.toArray(new ClickableTextHelper.TextLine[0]);
         this.visit((a, style, x, y, w, h) -> {
            ClickEvent clickEvent = style.getClickEvent();
            HoverEvent hoverEvent = style.getHoverEvent();
            if (clickEvent != null || hoverEvent != null) {
               Vector4i rect = new Vector4i(x, y, x + w, y + h);
               if (clickEvent != null && clickEvent.action() == Action.OPEN_URL) {
                  this.clickUrls.put(rect, ((OpenUrl)clickEvent).uri().toString());
               }

               if (hoverEvent instanceof ShowText && ((ShowText)hoverEvent).value() instanceof Component component) {
                  this.hoverText.put(rect, component);
               }
            }
         });
      }
   }

   @Nullable
   public Component getHoverTooltip(int cx, int cy, int mouseX, int mouseY) {
      for (Vector4i area : this.hoverText.keySet()) {
         if (isPointWithinBounds(area, cx, cy, mouseX, mouseY)) {
            return this.hoverText.get(area);
         }
      }

      return null;
   }

   @Nullable
   public String getClickLink(int cx, int cy, int mouseX, int mouseY) {
      for (Vector4i area : this.clickUrls.keySet()) {
         if (isPointWithinBounds(area, cx, cy, mouseX, mouseY)) {
            return this.clickUrls.get(area);
         }
      }

      return null;
   }

   public int lineCount() {
      return this.lines == null ? 1 : this.lines.length;
   }

   private static boolean isPointWithinBounds(Vector4i area, int xOffset, int yOffset, int x, int y) {
      int x1 = area.x + xOffset;
      int y1 = area.y + yOffset;
      int x2 = area.z + xOffset;
      int y2 = area.w + yOffset;
      return x > x1 && x < x2 && y > y1 && y < y2;
   }

   public void visit(ClickableTextHelper.MultilineTextVisitor visitor) {
      if (this.lines != null) {
         Font font = Minecraft.getInstance().font;
         int y = 0;

         for (ClickableTextHelper.TextLine line : this.lines) {
            int x = 0;

            for (ClickableTextHelper.TextNode node : line.nodes) {
               int width = node.getWidth(font);
               visitor.visit(node.text, node.style, x, y, width, 9);
               x += width;
            }

            y += 9;
         }
      }
   }

   public void clear() {
      this.lines = new ClickableTextHelper.TextLine[0];
      this.clickUrls.clear();
      this.hoverText.clear();
   }

   public void markDirty() {
      this.dirty = true;
   }

   @FunctionalInterface
   public interface MultilineTextVisitor {
      void visit(String var1, Style var2, int var3, int var4, int var5, int var6);
   }

   protected record TextLine(ClickableTextHelper.TextNode[] nodes) {
   }

   protected record TextNode(String text, Style style) {
      public int getWidth(Font font) {
         return font.width(this.asText());
      }

      public Component asText() {
         return Component.literal(this.text).withStyle(this.style);
      }
   }
}
