package org.figuramc.figura.gui.widgets.lists;

import com.mojang.blaze3d.audio.SoundBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class SoundsList extends AbstractList {
   private final List<SoundsList.SoundElement> sounds = new ArrayList<>();
   private final Avatar owner;
   private SoundsList.SoundElement selected;

   public SoundsList(int x, int y, int width, int height, Avatar owner) {
      super(x, y, width, height);
      this.owner = owner;
      this.updateList();
      Label noOwner;
      this.children
         .add(
            noOwner = new Label(
               FiguraText.of("gui.error.no_avatar").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0
            )
         );
      Label noSounds;
      this.children
         .add(
            noSounds = new Label(
               FiguraText.of("gui.error.no_sounds").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0
            )
         );
      noOwner.centerVertically = noSounds.centerVertically = true;
      noOwner.setVisible(owner == null);
      noSounds.setVisible(!noOwner.isVisible() && this.sounds.isEmpty());
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      UIHelper.blitSliced(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), UIHelper.OUTLINE_FILL);
      this.enableScissors(gui);
      if (!this.sounds.isEmpty()) {
         this.updateEntries();
      }

      super.extractRenderState(gui, mouseX, mouseY, delta);
      gui.disableScissor();
   }

   private void updateEntries() {
      int totalHeight = -4;

      for (SoundsList.SoundElement sound : this.sounds) {
         totalHeight += sound.getHeight() + 8;
      }

      int entryHeight = this.sounds.isEmpty() ? 0 : totalHeight / this.sounds.size();
      this.scrollBar.setVisible(totalHeight > this.getHeight());
      this.scrollBar.setScrollRatio(entryHeight, totalHeight - this.getHeight());
      int xOffset = this.scrollBar.isVisible() ? 4 : 11;
      int yOffset = this.scrollBar.isVisible() ? (int)(-Mth.lerp(this.scrollBar.getScrollProgress(), -4.0, totalHeight - this.getHeight())) : 4;

      for (SoundsList.SoundElement sound : this.sounds) {
         sound.setX(this.getX() + xOffset);
         sound.setY(this.getY() + yOffset);
         yOffset += sound.getHeight() + 8;
      }
   }

   private void updateList() {
      this.sounds.forEach(this.children::remove);
      if (this.owner != null) {
         for (Entry<String, SoundBuffer> entry : this.owner.customSounds.entrySet()) {
            SoundsList.SoundElement sound = new SoundsList.SoundElement(this.getWidth() - 22, entry.getKey(), entry.getValue(), this, this.owner);
            this.sounds.add(sound);
            this.children.add(sound);
         }

         this.sounds.sort(SoundsList.SoundElement::compareTo);
         if (!this.sounds.isEmpty()) {
            this.selected = this.sounds.get(0);
         }
      }
   }

   public LuaSound getSound() {
      return this.selected != null ? this.selected.getSound() : null;
   }

   private static class SoundElement extends AbstractContainerElement implements Comparable<SoundsList.SoundElement> {
      private final Component size;
      private final String name;
      private final SoundBuffer sound;
      private final Avatar owner;
      private final SoundsList parent;
      private final ParentedButton play;
      private final ParentedButton stop;

      public SoundElement(int width, String name, SoundBuffer sound, SoundsList parent, Avatar owner) {
         super(0, 0, width, 20);
         this.name = name;
         this.sound = sound;
         this.owner = owner;
         this.parent = parent;
         int len = owner.nbt.getCompoundOrEmpty("sounds").getByteArray(name).orElse(new byte[0]).length;
         this.size = Component.literal("(" + MathUtils.asFileSize(len) + ")").withStyle(ChatFormatting.GRAY);
         this.children
            .add(
               0,
               this.play = new ParentedButton(
                  0, 0, 20, 20, 0, 0, 20, FiguraIdentifier.of("textures/gui/play.png"), 60, 20, FiguraText.of("gui.sound.play"), this, button -> {}
               ) {
                  public void playDownSound(SoundManager soundManager) {
                     Vec3 vec = Minecraft.getInstance().player == null ? new Vec3(0.0, 0.0, 0.0) : Minecraft.getInstance().player.position();
                     SoundElement.this.getSound().pos(vec.x, vec.y, vec.z).play();
                  }
               }
            );
         this.children
            .add(
               this.stop = new ParentedButton(
                  0,
                  0,
                  20,
                  20,
                  0,
                  0,
                  20,
                  FiguraIdentifier.of("textures/gui/stop.png"),
                  60,
                  20,
                  FiguraText.of("gui.sound.stop"),
                  this,
                  button -> SoundAPI.getSoundEngine().figura$stopSound(owner.owner, name)
               )
            );
      }

      @Override
      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         if (this.isVisible()) {
            int x = this.getX();
            int y = this.getY();
            int width = this.getWidth();
            int height = this.getHeight();
            if (this.parent.selected == this) {
               UIHelper.fillOutline(gui, x - 1, y - 1, width + 2, height + 2, -1);
            }

            Font font = Minecraft.getInstance().font;
            int textY = y + height / 2 - 9 / 2;
            this.setHovered(this.isMouseOver(mouseX, mouseY));
            if (this.isHovered()) {
               gui.text(font, HOVERED_ARROW, x + 4, textY, UIHelper.adjustColor(16777215));
            }

            gui.text(font, this.name, x + 16, textY, UIHelper.adjustColor(16777215));
            gui.text(font, this.size, x + width - 96 - font.width(this.size), textY, UIHelper.adjustColor(16777215));
            super.extractRenderState(gui, mouseX, mouseY, delta);
         }
      }

      @Override
      public boolean isMouseOver(double mouseX, double mouseY) {
         return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
      }

      @Override
      public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
         boolean clicked = super.mouseClicked(mouseButtonEvent, bl);
         double mouseX = mouseButtonEvent.x();
         double mouseY = mouseButtonEvent.y();
         if (!clicked && this.isMouseOver(mouseX, mouseY)) {
            this.parent.selected = this;
            return true;
         } else {
            return clicked;
         }
      }

      @Override
      public void setX(int x) {
         super.setX(x);
         this.play.setX(x + this.getWidth() - 64);
         this.stop.setX(x + this.getWidth() - 40);
      }

      @Override
      public void setY(int y) {
         super.setY(y);
         this.play.setY(y);
         this.stop.setY(y);
      }

      public LuaSound getSound() {
         return new LuaSound(this.sound, this.name, this.owner);
      }

      public int compareTo(SoundsList.SoundElement o) {
         return this.name.compareTo(o.name);
      }
   }
}
