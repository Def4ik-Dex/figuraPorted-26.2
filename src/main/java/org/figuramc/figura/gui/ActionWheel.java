package org.figuramc.figura.gui;

import com.mojang.blaze3d.platform.Window;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.api.action_wheel.Action;
import org.figuramc.figura.lua.api.action_wheel.Page;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class ActionWheel {
   private static final Identifier TEXTURE = FiguraIdentifier.of("textures/gui/action_wheel.png");
   private static final Identifier ICONS = FiguraIdentifier.of("textures/gui/action_wheel_icons.png");
   private static final double DISTANCE = 41.0;
   private static final double DEADZONE = 19.0;
   private static boolean enabled = false;
   private static int selected = -1;
   private static Minecraft minecraft;
   private static int slots;
   private static int leftSlots;
   private static int rightSlots;
   private static float scale;
   private static int x;
   private static int y;
   private static double mouseX;
   private static double mouseY;

   public static void render(GuiGraphicsExtractor gui) {
      if (isEnabled()) {
         minecraft = Minecraft.getInstance();
         Window window = minecraft.getWindow();
         x = (int)(window.getGuiScaledWidth() / 2.0);
         y = (int)(window.getGuiScaledHeight() / 2.0);
         Matrix3x2fStack pose = gui.pose();
         pose.pushMatrix();
         pose.translate(x, y);
         scale = Configs.ACTION_WHEEL_SCALE.value;
         pose.scale(scale, scale);
         Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
         if (avatar != null && avatar.luaRuntime != null) {
            Page currentPage = avatar.luaRuntime.action_wheel.currentPage;
            if (avatar.luaRuntime.action_wheel.currentPage != null) {
               slots = currentPage.getSize();
               leftSlots = (int)Math.floor(slots / 2.0);
               rightSlots = (int)Math.ceil(slots / 2.0);
               mouseX = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
               mouseY = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
               FiguraMod.pushProfiler("selectedSlot");
               calculateSelected();
               FiguraMod.popPushProfiler("wheel");
               renderTextures(gui, currentPage);
               FiguraMod.popPushProfiler("items");
               renderItemsAndIcons(gui, currentPage);
               pose.popMatrix();
               FiguraMod.popPushProfiler("texts");
               renderTexts(gui, currentPage);
               FiguraMod.popProfiler();
               return;
            }
         }

         renderEmpty(gui, avatar == null);
      }
   }

   private static double getAngle(int i) {
      double angle;
      if (i < rightSlots) {
         angle = 180.0 / rightSlots * (i - (rightSlots - 1) * 0.5);
      } else {
         angle = 180.0 / leftSlots * (i - rightSlots - (leftSlots - 1) * 0.5F + leftSlots);
      }

      return Math.toRadians(angle);
   }

   private static void renderEmpty(GuiGraphicsExtractor gui, boolean avatar) {
      ActionWheel.TextureData data = ActionWheel.OverlayTexture.values()[0].data[0];
      data.render(gui, null, false);
      data.render(gui, null, true);
      gui.pose().popMatrix();
      Component component = FiguraText.of("gui.error." + (avatar ? "no_avatar" : "no_wheel_page")).withStyle(ChatFormatting.YELLOW);
      Font font = minecraft.font;
      UIHelper.renderOutlineText(gui, font, component, x - font.width(component) / 2, y - 9 / 2, 16777215, 0);
   }

   private static void calculateSelected() {
      double mouseDistance = Math.sqrt(Math.pow(x - mouseX, 2.0) + Math.pow(y - mouseY, 2.0));
      if (rightSlots != 0 && !(mouseDistance < 19.0 * scale)) {
         double angle = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x)) + 90.0;
         if (angle < 0.0) {
            angle += 360.0;
         }

         if (angle < 180.0) {
            selected = (int)Math.floor(rightSlots / 180.0 * angle);
         } else {
            selected = (int)Math.floor(leftSlots / 180.0 * (angle - 180.0)) + rightSlots;
         }
      } else {
         selected = -1;
      }
   }

   private static void renderTextures(GuiGraphicsExtractor gui, Page page) {
      for (int i = 0; i < slots; i++) {
         Action action = page.slots()[i];
         boolean left = i >= rightSlots;
         int type = left ? leftSlots : rightSlots;
         int relativeIndex = left ? i - rightSlots : i;
         FiguraVec3 color = action == null ? null : action.getColor(selected == i);
         ActionWheel.OverlayTexture.values()[type - 1].data[relativeIndex].render(gui, color, left);
         if (action != null) {
            double angle = getAngle(i);
            double x = Math.cos(angle) * 15.0 - 4.0;
            double y = Math.sin(angle) * 15.0 - 4.0;
            UIHelper.enableBlend();
            int tintColor = color != null ? ColorUtils.rgbaToIntARGB(FiguraVec4.of(color.x, color.y, color.z, 1.0)) : -1;
            gui.blit(
               RenderPipelines.GUI_TEXTURED,
               ICONS,
               (int)Math.round(x),
               (int)Math.round(y),
               action.scroll != null ? 24.0F : (action.toggle != null ? (action.isToggled() ? 16.0F : 8.0F) : 0.0F),
               color == null ? 0.0F : 8.0F,
               8,
               8,
               8,
               8,
               32,
               16,
               tintColor
            );
         }
      }
   }

   private static void renderItemsAndIcons(GuiGraphicsExtractor gui, Page page) {
      for (int i = 0; i < slots; i++) {
         Action action = page.slots()[i];
         if (action != null) {
            boolean isSelected = selected == i;
            double angle = getAngle(i);
            double xOff = Math.cos(angle) * 41.0;
            double yOff = Math.sin(angle) * 41.0;
            Action.TextureData texture = action.getTexture(isSelected);
            if (texture != null) {
               UIHelper.enableBlend();
               gui.blit(
                  RenderPipelines.GUI_TEXTURED,
                  texture.texture.getLocation(),
                  (int)Math.round(xOff - texture.width * texture.scale / 2.0),
                  (int)Math.round(yOff - texture.height * texture.scale / 2.0),
                  (float)texture.u,
                  (float)texture.v,
                  (int)Math.round(texture.width * texture.scale),
                  (int)Math.round(texture.height * texture.scale),
                  texture.width,
                  texture.height,
                  texture.texture.getWidth(),
                  texture.texture.getHeight()
               );
            }

            ItemStack item = action.getItem(isSelected);
            if (item != null && !item.isEmpty()) {
               gui.item(item, (int)Math.round(xOff - 8.0), (int)Math.round(yOff - 8.0));
               if (Configs.ACTION_WHEEL_DECORATIONS.value) {
                  gui.itemDecorations(minecraft.font, item, (int)Math.round(xOff - 8.0), (int)Math.round(yOff - 8.0));
               }
            }
         }
      }
   }

   private static void renderTexts(GuiGraphicsExtractor gui, Page page) {
      Font font = minecraft.font;
      int titlePosition = Configs.ACTION_WHEEL_TITLE.value;
      int indicatorPosition = Configs.ACTION_WHEEL_SLOTS_INDICATOR.value;
      Matrix3x2fStack pose = gui.pose();
      Action selectedTitleAction = selected == -1 ? null : page.slots()[selected];
      String selectedTitle = selectedTitleAction == null ? null : selectedTitleAction.getTitle();
      int groupCount = page.getGroupCount();
      if (groupCount > 1 && (selectedTitle == null || indicatorPosition != titlePosition - 2)) {
         pose.pushMatrix();
         int index = page.getSlotsShift();
         int greatest = page.getGreatestSlot() + 1;
         MutableComponent indicator = Component.empty();
         int extraWidth = 0;
         if (index > 1) {
            Component arrow = UIHelper.UP_ARROW.copy().append(" ");
            indicator.append(arrow);
            extraWidth -= font.width(arrow);
         }

         indicator.append(
            FiguraText.of(
               "gui.action_wheel.slots_indicator",
               Component.literal(String.valueOf((index - 1) * 8 + 1)).withStyle(FiguraMod.getAccentColor()),
               Component.literal(String.valueOf(Math.min(index * 8, greatest))).withStyle(FiguraMod.getAccentColor()),
               Component.literal(String.valueOf(greatest)).withStyle(FiguraMod.getAccentColor())
            )
         );
         if (index < groupCount) {
            Component arrow = Component.literal(" ").append(UIHelper.DOWN_ARROW);
            indicator.append(arrow);
            extraWidth += font.width(arrow);
         }

         gui.text(
            font,
            indicator,
            x - (int)((font.width(indicator) - extraWidth) / 2.0F),
            (int)ActionWheel.Position.index(indicatorPosition).apply(9.0),
            UIHelper.adjustColor(16777215)
         );
         pose.popMatrix();
      }

      if (titlePosition >= 5) {
         boolean internal = titlePosition == 5;
         double distance = (internal ? 41.0 : 66.0) * scale;
         pose.pushMatrix();

         for (int i = 0; i < slots; i++) {
            Action action = page.slots()[i];
            if (action != null) {
               String title = action.getTitle();
               if (title != null) {
                  double angle = getAngle(i);
                  double xOff = Math.cos(angle) * distance;
                  double yOff = Math.sin(angle) * distance;
                  int textX = x + (int)Math.round(xOff);
                  int textY = ActionWheel.y + (int)Math.round(yOff + (internal ? 9.0F * scale : -9 / 2.0F));
                  Component text = TextUtils.replaceInText(Emojis.applyEmojis(TextUtils.tryParseJson(title)), "\n|\\\\n", " ");
                  int textWidth = font.width(text);
                  if (internal) {
                     textX = (int)(textX - textWidth / 2.0F);
                     if (i >= rightSlots) {
                        textX = Math.min(textX, x - textWidth - 1);
                     } else {
                        textX = Math.max(textX, x + 1);
                     }
                  } else if (i >= rightSlots) {
                     textX -= textWidth;
                  }

                  gui.text(font, text, textX, textY, UIHelper.adjustColor(16777215));
               }
            }
         }

         pose.popMatrix();
      } else if (selectedTitle != null) {
         Component text = Emojis.applyEmojis(TextUtils.tryParseJson(selectedTitle));
         List<Component> list = TextUtils.splitText(text, "\n");
         int height = 9 * list.size();
         if (titlePosition < 2) {
            UIHelper.renderTooltip(gui, text, (int)mouseX, (int)mouseY, titlePosition == 0);
         } else {
            pose.pushMatrix();
            int y = (int)ActionWheel.Position.index(titlePosition - 2).apply(height);

            for (int i = 0; i < list.size(); i++) {
               Component component = list.get(i);
               gui.text(font, component, x - (int)(font.width(component) / 2.0F), y + 9 * i, UIHelper.adjustColor(16777215));
            }

            pose.popMatrix();
         }
      }
   }

   public static void execute(int index, boolean left) {
      Avatar avatar;
      if (!isEnabled() || (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) == null || avatar.luaRuntime == null) {
         selected = -1;
      } else if (!avatar.luaRuntime.action_wheel.execute(avatar, left)) {
         if (index >= 0 && index <= 7 && avatar.luaRuntime != null) {
            Page currentPage = avatar.luaRuntime.action_wheel.currentPage;
            if (avatar.luaRuntime.action_wheel.currentPage != null) {
               Action action = currentPage.slots()[index];
               if (action != null) {
                  action.execute(avatar, left);
               }

               selected = -1;
               return;
            }
         }

         selected = -1;
      }
   }

   public static void hotbarKeyPressed(int i) {
      execute(i, true);
   }

   public static void scroll(double delta) {
      Avatar avatar;
      if (isEnabled() && (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) != null && avatar.luaRuntime != null) {
         if (!avatar.luaRuntime.action_wheel.mouseScroll(avatar, delta)) {
            if (avatar.luaRuntime != null) {
               Page currentPage = avatar.luaRuntime.action_wheel.currentPage;
               if (avatar.luaRuntime.action_wheel.currentPage != null) {
                  if (selected >= 0 && selected <= 7) {
                     Action action = currentPage.slots()[selected];
                     if (action != null && action.scroll != null) {
                        action.mouseScroll(avatar, delta);
                        return;
                     }
                  }

                  currentPage.setSlotsShift(currentPage.getSlotsShift() - (int)Math.signum(delta));
                  return;
               }
            }
         }
      }
   }

   public static void setEnabled(boolean enabled) {
      ActionWheel.enabled = enabled;
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static int getSelected() {
      return selected;
   }

   private enum OverlayTexture {
      ONE(new ActionWheel.TextureData(true, -64, 0.0F, 0.0F, 0)),
      TWO(new ActionWheel.TextureData(64.0F, 0.0F), new ActionWheel.TextureData(64.0F, 0.0F, 90)),
      THREE(new ActionWheel.TextureData(128.0F, 0.0F), new ActionWheel.TextureData(-32, 128.0F, 64.0F, 0), new ActionWheel.TextureData(0, 64.0F, 64.0F, 0)),
      FOUR(
         new ActionWheel.TextureData(192.0F, 0.0F),
         new ActionWheel.TextureData(192.0F, 64.0F),
         new ActionWheel.TextureData(192.0F, 0.0F, 90),
         new ActionWheel.TextureData(192.0F, 64.0F, 90)
      );

      private final ActionWheel.TextureData[] data;

      OverlayTexture(ActionWheel.TextureData... data) {
         this.data = data;
      }
   }

   private enum Position {
      TOP(height -> Math.max(ActionWheel.y - 64.0F * ActionWheel.scale - 4.0F - height, 4.0)),
      MID(height -> ActionWheel.y - height / 2.0),
      BOT(height -> Math.min(ActionWheel.y + 64.0F * ActionWheel.scale + 4.0F + height, ActionWheel.y * 2 - 4) - height);

      private final Function<Double, Double> function;

      Position(Function<Double, Double> function) {
         this.function = function;
      }

      public static ActionWheel.Position index(int i) {
         return values()[i];
      }

      public double apply(double d) {
         return this.function.apply(d);
      }
   }

   private static class TextureData {
      private final int y;
      private final int h;
      private final int rh;
      private final float u;
      private final float v;
      private final int rotation;

      public TextureData(boolean large, int y, float u, float v, int rotation) {
         this.y = y;
         this.h = large ? 128 : 64;
         this.u = u;
         this.v = v;
         this.rh = large ? 128 : 64;
         this.rotation = rotation;
      }

      public TextureData(int y, float u, float v, int rotation) {
         this(false, y, u, v, rotation);
      }

      public TextureData(float u, float v, int rotation) {
         this(-64, u, v, rotation);
      }

      public TextureData(float u, float v) {
         this(u, v, 0);
      }

      public void render(GuiGraphicsExtractor gui, FiguraVec3 color, boolean left) {
         Matrix3x2fStack pose = gui.pose();
         pose.pushMatrix();
         pose.rotate((this.rotation + (left ? 180 : 0)) * (float) (Math.PI / 180.0));
         UIHelper.enableBlend();
         int tintColor = color != null ? ColorUtils.rgbaToIntARGB(FiguraVec4.of(color.x, color.y, color.z, 1.0)) : -1;
         gui.blit(
            RenderPipelines.GUI_TEXTURED,
            ActionWheel.TEXTURE,
            0,
            this.y,
            this.u,
            color == null ? this.v : this.v + 128.0F,
            64,
            this.h,
            64,
            this.rh,
            256,
            256,
            tintColor
         );
         pose.popMatrix();
      }
   }
}
