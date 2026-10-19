package org.figuramc.figura.gui.widgets.lists;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.SwitchButton;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;
import org.figuramc.figura.wizards.AvatarWizard;
import org.figuramc.figura.wizards.WizardEntry;
import org.joml.Matrix3x2fStack;

public class AvatarWizardList extends AbstractList {
   private final AvatarWizard wizard;
   private final Map<Component, List<GuiEventListener>> map = new LinkedHashMap<>();

   public AvatarWizardList(int x, int y, int width, int height, AvatarWizard wizard) {
      super(x, y, width, height);
      this.wizard = wizard;
      this.generate();
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
      int lineHeight = 9 + 8;
      int entryHeight = 24;
      int size = 0;

      for (List<GuiEventListener> list : this.map.values()) {
         for (GuiEventListener widget : list) {
            if (widget instanceof AvatarWizardList.WizardInputBox ib) {
               ib.setVisible(this.wizard.checkDependency(ib.entry));
               if (ib.isVisible()) {
                  size++;
               }
            } else if (widget instanceof AvatarWizardList.WizardToggleButton tb) {
               tb.setVisible(this.wizard.checkDependency(tb.entry));
               if (tb.isVisible()) {
                  size++;
               }
            }
         }
      }

      int totalHeight = entryHeight * size + lineHeight * this.map.size();
      this.scrollBar.setVisible(totalHeight > height);
      this.scrollBar.setScrollRatio(entryHeight, totalHeight - height);
      int yOffset = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -4.0, totalHeight - height)) : 4;

      for (Entry<Component, List<GuiEventListener>> entry : this.map.entrySet()) {
         List<GuiEventListener> value = entry.getValue();
         if (!value.isEmpty()) {
            int newY = yOffset + lineHeight;

            for (GuiEventListener w : entry.getValue()) {
               FiguraWidget widget = (FiguraWidget)w;
               if (widget.isVisible()) {
                  widget.setY(y + newY);
                  newY += entryHeight;
               }
            }

            if (newY != yOffset + lineHeight) {
               gui.centeredText(font, entry.getKey(), x + width / 2, y + yOffset + 4, UIHelper.adjustColor(16777215));
               yOffset = newY;
            }
         }
      }

      super.extractRenderState(gui, mouseX, mouseY, delta);
      gui.disableScissor();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
      for (GuiEventListener widget : this.children()) {
         if (widget instanceof TextField field) {
            field.getField().setFocused(field.isEnabled() && field.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y()));
         }
      }

      return super.mouseClicked(mouseButtonEvent, bl);
   }

   private void generate() {
      for (List<GuiEventListener> value : this.map.values()) {
         this.children.removeAll(value);
      }

      this.map.clear();
      int x = this.getX() + this.getWidth() / 2 + 4;
      int width = this.getWidth() / 2 - 20;
      Component lastName = Component.empty();
      List<GuiEventListener> lastList = new ArrayList<>();

      for (WizardEntry value : WizardEntry.all()) {
         switch (value.type) {
            case CATEGORY:
               if (!lastList.isEmpty()) {
                  this.map.put(lastName, lastList);
                  this.children.addAll(lastList);
               }

               lastName = FiguraText.of("gui.avatar_wizard." + value.name.toLowerCase(Locale.US));
               lastList = new ArrayList<>();
               break;
            case TEXT:
               lastList.add(new AvatarWizardList.WizardInputBox(x, width, this, value));
               break;
            case TOGGLE:
               lastList.add(new AvatarWizardList.WizardToggleButton(x, width, this, value));
         }
      }

      this.map.put(lastName, lastList);
      this.children.addAll(lastList);
   }

   private static class WizardInputBox extends TextField {
      private final AvatarWizardList parent;
      private final WizardEntry entry;
      private final Component name;

      public WizardInputBox(int x, int width, AvatarWizardList parent, WizardEntry entry) {
         super(x, 0, width, 20, TextField.HintType.ANY, s -> parent.wizard.changeEntry(entry, s));
         this.parent = parent;
         this.entry = entry;
         this.name = FiguraText.of("gui.avatar_wizard." + entry.name.toLowerCase(Locale.US));
         this.getField().setValue(String.valueOf(parent.wizard.getEntry(entry, "")));
      }

      @Override
      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         if (this.isVisible()) {
            super.extractRenderState(gui, mouseX, mouseY, delta);
            Font font = Minecraft.getInstance().font;
            MutableComponent name = this.name.copy();
            if (!this.getField().getValue().isBlank()) {
               name.setStyle(FiguraMod.getAccentColor());
            }

            gui.text(font, name, this.getX() - this.getWidth() - 8, (int)(this.getY() + (this.getHeight() - 9) / 2.0F), UIHelper.adjustColor(16777215));
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }
   }

   private static class WizardToggleButton extends SwitchButton {
      private final AvatarWizardList parent;
      private final WizardEntry entry;

      public WizardToggleButton(int x, int width, AvatarWizardList parent, WizardEntry entry) {
         super(x, 0, width, 20, FiguraText.of("gui.avatar_wizard." + entry.name.toLowerCase(Locale.US)), false);
         this.parent = parent;
         this.entry = entry;
         this.setToggled((Boolean)parent.wizard.getEntry(entry, false));
      }

      @Override
      public void onPress(InputWithModifiers inputWithModifiers) {
         super.onPress(inputWithModifiers);
         this.parent.wizard.changeEntry(this.entry, this.isToggled());
      }

      @Override
      protected void renderDefaultTexture(GuiGraphicsExtractor gui, float delta) {
         Matrix3x2fStack pose = gui.pose();
         pose.pushMatrix();
         pose.translate(this.getWidth() - 30, 0.0F);
         super.renderDefaultTexture(gui, delta);
         pose.popMatrix();
      }

      @Override
      protected void renderText(GuiGraphicsExtractor gui, float delta) {
         Font font = Minecraft.getInstance().font;
         MutableComponent name = this.getMessage().copy();
         if (this.isToggled()) {
            name.withStyle(FiguraMod.getAccentColor());
         }

         gui.text(font, name, this.getX() - this.getWidth() - 8, (int)(this.getY() + (this.getHeight() - 9) / 2.0F), UIHelper.adjustColor(16777215));
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }
   }
}
