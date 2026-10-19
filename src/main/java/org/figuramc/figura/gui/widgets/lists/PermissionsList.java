package org.figuramc.figura.gui.widgets.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.SliderWidget;
import org.figuramc.figura.gui.widgets.SwitchButton;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class PermissionsList extends AbstractList {
   public boolean precise = false;
   private final Map<Component, List<GuiEventListener>> permissions = new LinkedHashMap<>();

   public PermissionsList(int x, int y, int width, int height) {
      super(x, y, width, height);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
      this.enableScissors(gui);
      Font font = Minecraft.getInstance().font;
      int lineHeight = 9;
      int entryHeight = 27 + lineHeight;
      int titleHeight = 16 + lineHeight;
      int size = 0;

      for (List<GuiEventListener> value : this.permissions.values()) {
         size += value.size();
      }

      int totalHeight = size * entryHeight;
      boolean titles = this.permissions.size() > 1;
      if (titles) {
         totalHeight += this.permissions.size() * titleHeight;
      }

      this.scrollBar.setY(y + 4);
      this.scrollBar.setVisible(totalHeight > height);
      this.scrollBar.setScrollRatio(entryHeight, totalHeight - height);
      int xOffset = this.scrollBar.isVisible() ? 8 : 15;
      int yOffset = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -16.0, totalHeight - height)) : 16;

      for (Entry<Component, List<GuiEventListener>> entry : this.permissions.entrySet()) {
         if (titles) {
            gui.centeredText(font, entry.getKey(), x + (width - xOffset) / 2, y + yOffset, UIHelper.adjustColor(16777215));
            yOffset += titleHeight;
         }

         for (GuiEventListener widget : entry.getValue()) {
            ((FiguraWidget)widget).setX(x + xOffset);
            ((FiguraWidget)widget).setY(y + yOffset);
            yOffset += entryHeight;
         }
      }

      super.extractRenderState(gui, mouseX, mouseY, delta);
      gui.disableScissor();
   }

   public void updateList(PermissionPack container) {
      for (List<GuiEventListener> list : this.permissions.values()) {
         list.forEach(this.children::remove);
      }

      this.permissions.clear();
      this.permissions.put(FiguraText.of(), this.generateWidgets(container, Permissions.DEFAULT, "figura"));

      for (Entry<String, Collection<Permissions>> entry : PermissionManager.CUSTOM_PERMISSIONS.entrySet()) {
         this.permissions.put(Component.translatable(entry.getKey()), this.generateWidgets(container, entry.getValue(), entry.getKey()));
      }
   }

   private List<GuiEventListener> generateWidgets(PermissionPack container, Collection<Permissions> coll, String id) {
      List<GuiEventListener> list = new ArrayList<>();
      int x = this.getX();
      int y = this.getWidth();
      int width = this.getWidth();

      for (Permissions permissions : coll) {
         int lineHeight = 9;
         String text = id + ".permissions.value." + permissions.name.toLowerCase(Locale.US);
         GuiEventListener widget;
         if (!permissions.isToggle) {
            if (!this.precise) {
               widget = new PermissionsList.PermissionSlider(x + 8, y, width - 30, 11 + lineHeight, container, permissions, this, id, text);
            } else {
               widget = new PermissionsList.PermissionField(x + 8, y, width - 30, 11 + lineHeight, container, permissions, this, id, text);
            }
         } else {
            widget = new PermissionsList.PermissionSwitch(x + 8, y, width - 30, 20 + lineHeight, container, permissions, this, id, text);
         }

         list.add(widget);
         this.children.add(widget);
      }

      return list;
   }

   private static class PermissionField extends TextField {
      private static final Predicate<String> validator = s -> {
         try {
            Integer i = Integer.parseInt(s);
            return i >= 0;
         } catch (Exception ignored) {
            return false;
         }
      };
      private final PermissionPack container;
      private final Permissions permissions;
      private final PermissionsList parent;
      private final String text;
      private Component value;
      private boolean changed;

      public PermissionField(
         int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id, String text
      ) {
         super(x, y, width, height, null, null);
         this.container = container;
         this.permissions = permissions;
         this.parent = parent;
         this.text = text;
         String val = String.valueOf(container.get(permissions));
         this.value = Component.literal(val);
         this.changed = container.isChanged(permissions);
         this.getField().setValue(val);
         this.getField().setResponder(txt -> {
            if (validator.test(txt)) {
               int value = Integer.parseInt(txt);
               container.insert(permissions, value, id);
               this.changed = container.isChanged(permissions);
               this.value = Component.literal(String.valueOf(value));
            }
         });
      }

      @Override
      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         Font font = Minecraft.getInstance().font;
         int color = 16777215;
         String text = this.getField().getValue();
         if (!validator.test(text)) {
            color = 16733525;
         } else if (this.changed) {
            TextColor textColor = FiguraMod.getAccentColor().getColor();
            color = textColor == null ? ColorUtils.Colors.AWESOME_BLUE.hex : textColor.getValue();
         }

         this.setColor(color);
         this.setBorderColour(-16777216 + color);
         super.extractRenderState(gui, mouseX, mouseY, delta);
         MutableComponent name = Component.translatable(this.text);
         if (this.changed) {
            name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");
         }

         int valueX = this.getX() + this.getWidth() - font.width(this.value) - 1;
         int x = this.getX() + 1;
         int y = this.getY() + 1 - 9;
         int width = valueX - this.getX() - 2;
         UIHelper.renderScrollingText(gui, name, x, y, width, 16777215);
         gui.text(font, this.value.copy().setStyle(FiguraMod.getAccentColor()), valueX, this.getY() + 1 - 9, UIHelper.adjustColor(16777215));
         if (this.parent.isInsideScissors(mouseX, mouseY) && UIHelper.isMouseOver(x, y, width, 9, mouseX, mouseY)) {
            UIHelper.setTooltip(Component.translatable(this.text + ".tooltip"));
         }
      }

      @Override
      public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
         if (!this.isEnabled() || !this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            return false;
         } else if (mouseButtonEvent.button() == 1) {
            this.container.reset(this.permissions);
            this.parent.updateList(this.container);
            this.getField().playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
         } else {
            return super.mouseClicked(mouseButtonEvent, bl);
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }
   }

   private static class PermissionSlider extends SliderWidget {
      private static final Component INFINITY = FiguraText.of("permissions.infinity");
      private final PermissionPack container;
      private final Permissions permissions;
      private final PermissionsList parent;
      private final String text;
      private Component value;
      private boolean changed;

      public PermissionSlider(
         int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id, String text
      ) {
         super(
            x,
            y,
            width,
            height,
            Mth.clamp(container.get(permissions) / (permissions.max.intValue() + 1.0), 0.0, 1.0),
            permissions.max / permissions.stepSize + 1,
            permissions.showSteps()
         );
         this.container = container;
         this.permissions = permissions;
         this.parent = parent;
         this.text = text;
         this.value = (Component)(container.get(permissions) == Integer.MAX_VALUE ? INFINITY : Component.literal(String.valueOf(container.get(permissions))));
         this.changed = container.isChanged(permissions);
         this.setAction(
            slider -> {
               int value = this.showSteps
                  ? ((SliderWidget)slider).getIntValue() * permissions.stepSize
                  : (int)((permissions.max.intValue() + 1.0) * slider.getScrollProgress());
               boolean infinity = permissions.checkInfinity(value);
               container.insert(permissions, infinity ? Integer.MAX_VALUE : value, id);
               this.changed = container.isChanged(permissions);
               this.value = (Component)(infinity ? INFINITY : Component.literal(String.valueOf(value)));
            }
         );
      }

      @Override
      public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         Matrix3x2fStack pose = gui.pose();
         Font font = Minecraft.getInstance().font;
         pose.pushMatrix();
         pose.translate(0.0F, 9.0F);
         super.extractWidgetRenderState(gui, mouseX, mouseY, delta);
         pose.popMatrix();
         MutableComponent name = Component.translatable(this.text);
         if (this.changed) {
            name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");
         }

         int valueX = this.getX() + this.getWidth() - font.width(this.value) - 1;
         int x = this.getX() + 1;
         int y = this.getY() + 1;
         int width = valueX - this.getX() - 2;
         UIHelper.renderScrollingText(gui, name, x, y, width, 16777215);
         gui.text(font, this.value.copy().setStyle(FiguraMod.getAccentColor()), valueX, this.getY() + 1, UIHelper.adjustColor(16777215));
         if (this.parent.isInsideScissors(mouseX, mouseY) && UIHelper.isMouseOver(x, y, width, 9, mouseX, mouseY)) {
            UIHelper.setTooltip(Component.translatable(this.text + ".tooltip"));
         }
      }

      @Override
      public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
         if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            return false;
         } else if (mouseButtonEvent.button() == 1) {
            this.container.reset(this.permissions);
            this.parent.updateList(this.container);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
         } else {
            return super.mouseClicked(mouseButtonEvent, bl);
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }
   }

   private static class PermissionSwitch extends SwitchButton {
      private final PermissionPack container;
      private final Permissions permissions;
      private final PermissionsList parent;
      private final String id;
      private final String text;
      private Component value;
      private boolean changed;

      public PermissionSwitch(
         int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id, String text
      ) {
         super(x, y, width, height, Component.translatable(text), permissions.asBoolean(container.get(permissions)));
         this.container = container;
         this.permissions = permissions;
         this.parent = parent;
         this.id = id;
         this.text = text;
         this.changed = container.isChanged(permissions);
         this.value = FiguraText.of("permissions." + (this.toggled ? "enabled" : "disabled"));
      }

      @Override
      public void onPress(InputWithModifiers inputWithModifiers) {
         boolean value = !this.isToggled();
         this.container.insert(this.permissions, value ? 1 : 0, this.id);
         this.changed = this.container.isChanged(this.permissions);
         this.value = FiguraText.of("permissions." + (value ? "enabled" : "disabled"));
         super.onPress(inputWithModifiers);
      }

      @Override
      public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         super.extractContents(gui, mouseX, mouseY, delta);
         if (this.parent.isInsideScissors(mouseX, mouseY) && UIHelper.isMouseOver(this.getX() + 1, this.getY() + 1, this.getWidth() - 2, 9, mouseX, mouseY)) {
            UIHelper.setTooltip(Component.translatable(this.text + ".tooltip"));
         }
      }

      @Override
      protected void renderDefaultTexture(GuiGraphicsExtractor gui, float delta) {
         Matrix3x2fStack pose = gui.pose();
         Font font = Minecraft.getInstance().font;
         pose.pushMatrix();
         pose.translate(0.0F, 9.0F);
         super.renderDefaultTexture(gui, delta);
         pose.popMatrix();
      }

      @Override
      protected void renderText(GuiGraphicsExtractor gui, float delta) {
         Font font = Minecraft.getInstance().font;
         MutableComponent name = this.getMessage().copy();
         if (this.changed) {
            name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");
         }

         int valueX = this.getX() + this.getWidth() - font.width(this.value) - 1;
         int valueY = this.getY() + 9 + 11 - 9 / 2;
         UIHelper.renderScrollingText(gui, name, this.getX() + 1, this.getY() + 1, this.getWidth() - 2, 16777215);
         gui.text(font, this.value.copy().setStyle(FiguraMod.getAccentColor()), valueX, valueY, UIHelper.adjustColor(16777215));
      }

      @Override
      public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
         if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            return false;
         } else if (mouseButtonEvent.button() == 1) {
            this.container.reset(this.permissions);
            this.parent.updateList(this.container);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
         } else {
            return super.mouseClicked(mouseButtonEvent, bl);
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }
   }
}
