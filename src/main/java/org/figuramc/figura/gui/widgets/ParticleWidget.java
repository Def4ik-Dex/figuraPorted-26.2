package org.figuramc.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import org.figuramc.figura.ducks.ParticleEngineAccessor;
import org.figuramc.figura.mixin.particle.ParticleResourcesAccessor;
import org.figuramc.figura.utils.ui.UIHelper;

public class ParticleWidget implements FiguraWidget, FiguraTickable, FiguraRemovable {
   private final SpriteSet sprite;
   private final int x;
   private float lastY;
   private float y;
   private float lastSize;
   private float size;
   private final float initialSize;
   private boolean visible = true;
   private boolean removed;

   public ParticleWidget(int x, int y, ParticleType<?> particle) {
      this.x = x;
      this.lastY = this.y = y;
      this.sprite = getParticle(particle);
      this.initialSize = this.lastSize = this.size = (int)(8.0 * (Math.random() + 1.0));
      this.removed = this.sprite == null;
   }

   @Override
   public void tick() {
      if (this.visible && !this.removed) {
         this.lastY = this.y;
         this.lastSize = this.size;
         this.y--;
         this.size--;
         this.removed = this.size <= 0.0F;
      }
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
      if (this.visible && !this.removed) {
         float size = Mth.lerp(delta, this.lastSize, this.size);
         float y = Mth.lerp(delta, this.lastY, this.y);
         UIHelper.renderSprite(
            gui,
            (int)(this.x - size / 2.0F),
            (int)(y - size / 2.0F),
            0,
            (int)size,
            (int)size,
            this.sprite.get((int)(this.initialSize - size), (int)this.initialSize)
         );
      }
   }

   private static SpriteSet getParticle(ParticleType<?> particleType) {
      return (SpriteSet)((ParticleResourcesAccessor)((ParticleEngineAccessor)Minecraft.getInstance().particleEngine).figura$getParticleResources())
         .figura$getSpriteSets()
         .get(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType));
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   @Override
   public boolean isRemoved() {
      return this.removed;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public void setX(int x) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getY() {
      return (int)this.y;
   }

   @Override
   public void setY(int y) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getWidth() {
      return (int)this.size;
   }

   @Override
   public void setWidth(int width) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getHeight() {
      return (int)this.size;
   }

   @Override
   public void setHeight(int height) {
      throw new UnsupportedOperationException();
   }
}
