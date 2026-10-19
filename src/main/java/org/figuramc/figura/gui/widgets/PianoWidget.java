package org.figuramc.figura.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.utils.ui.UIHelper;

public class PianoWidget extends AbstractContainerElement {
   private static final int TOTAL_KEYS = 29;
   private static final String[] NOTES = new String[]{"F", "G", "A", "B", "C", "D", "E"};
   private final List<PianoWidget.Key> keys = new ArrayList<>();
   private final Supplier<LuaSound> soundSupplier;
   private PianoWidget.Key hovered;
   public boolean pressed;

   public PianoWidget(int x, int y, int width, int height, Supplier<LuaSound> soundSupplier) {
      super(x, y, width, height);
      this.soundSupplier = soundSupplier;
      double keyWidth = (width - 2) / Math.ceil(14.5);
      float j = 0.0F;
      List<PianoWidget.Key> sharpKeys = new ArrayList<>();
      int i = 1;
      int note = 0;
      int count = 0;

      while (i <= 29) {
         if (i % 2 != 1 || i % 7 != 0 && (i + 1) % 7 != 0) {
            boolean isSharp = i % 2 == 1;
            int keyX = x + 1 + (int)(keyWidth * j);
            if (!isSharp) {
               note = (note + 1) % NOTES.length;
            }

            PianoWidget.Key key = new PianoWidget.Key(
               keyX,
               y + 1,
               (int)Math.round(keyWidth),
               isSharp ? height / 2 : height - 2,
               NOTES[note] + (isSharp ? "#" : ""),
               (float)Math.pow(2.0, (count - 12) / 12.0F),
               isSharp,
               this
            );
            count++;
            if (!isSharp) {
               this.children.add(key);
            } else {
               sharpKeys.add(key);
            }

            this.keys.add(key);
         }

         i++;
         j += 0.5F;
      }

      this.children.addAll(sharpKeys);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      this.setHovered(this.isMouseOver(mouseX, mouseY));
      UIHelper.blitSliced(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), UIHelper.OUTLINE_FILL);
      PianoWidget.Key lastHovered = this.hovered;

      for (PianoWidget.Key key : this.keys) {
         key.setHovered(key.isMouseOver(mouseX, mouseY));
      }

      super.extractRenderState(gui, mouseX, mouseY, delta);
      if (this.pressed && this.hovered != lastHovered && this.hovered != null) {
         this.hovered.run();
      }
   }

   private static class Key extends ParentedButton {
      private final PianoWidget parent;
      private final float pitch;
      private final boolean isSharp;

      public Key(int x, int y, int width, int height, String key, float pitch, boolean isSharp, PianoWidget parent) {
         super(x, y, width, height, Component.literal(key), parent, button -> {});
         this.parent = parent;
         this.pitch = pitch;
         this.isSharp = isSharp;
      }

      public void playDownSound(SoundManager soundManager) {
         LuaSound sound = this.parent.soundSupplier.get();
         if (sound != null) {
            Vec3 vec = Minecraft.getInstance().player == null ? new Vec3(0.0, 0.0, 0.0) : Minecraft.getInstance().player.position();
            sound.pos(vec.x, vec.y, vec.z).pitch(this.pitch).play();
         } else {
            soundManager.play(SimpleSoundInstance.forUI((SoundEvent)SoundEvents.NOTE_BLOCK_HARP.value(), this.pitch, 1.0F));
         }
      }

      @Override
      public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         if (this.isVisible()) {
            UIHelper.fillRounded(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), (this.isSharp ? 0 : 16777215) + -16777216);
            UIHelper.blitSliced(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), UIHelper.OUTLINE);
            if (this.isHovered()) {
               UIHelper.fillRounded(
                  gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), FiguraMod.getAccentColor().getColor().getValue() + -1610612736
               );
            }

            Font font = Minecraft.getInstance().font;
            Component message = this.getMessage();
            int x = this.getX() + this.getWidth() / 2 - font.width(message) / 2;
            int y = this.getY() + this.getHeight() / 2 - 9 / 2;
            if (!this.isSharp) {
               y += this.getHeight() / 4;
            }

            gui.text(font, this.getMessage(), x, y, UIHelper.adjustColor(this.isSharp ? 16777215 : 0), false);
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         boolean over = super.isMouseOver(mouseX, mouseY);
         if (this.parent.hovered == this) {
            this.parent.hovered = over ? this : null;
            return over;
         } else if (!over) {
            return false;
         } else if (this.parent.hovered == null) {
            this.parent.hovered = this;
            return true;
         } else if (this.parent.hovered.isSharp) {
            return false;
         } else if (this.isSharp) {
            this.parent.hovered.setHovered(false);
            this.parent.hovered = this;
            return true;
         } else {
            return false;
         }
      }
   }
}
