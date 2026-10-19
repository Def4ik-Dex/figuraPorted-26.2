package org.figuramc.figura.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Vector3f;

public class PaperDoll {
   private static Long lastActivityTime = 0L;

   public static void render(GuiGraphicsExtractor gui, boolean force) {
      Minecraft minecraft = Minecraft.getInstance();
      LivingEntity entity = minecraft.getCameraEntity() instanceof LivingEntity e ? e : null;
      if ((Configs.HAS_PAPERDOLL.value || force)
         && entity != null
         && true
         && !minecraft.getDebugOverlay().showDebugScreen()
         && (!Configs.FIRST_PERSON_PAPERDOLL.value || minecraft.options.getCameraType().isFirstPerson() || force)
         && !entity.isSleeping()) {
         Avatar avatar;
         if (!Configs.PAPERDOLL_ALWAYS_ON.value
            && !force
            && (avatar = AvatarManager.getAvatar(entity)) != null
            && avatar.luaRuntime != null
            && !avatar.luaRuntime.renderer.forcePaperdoll) {
            if (!entity.isSprinting()
               && !entity.isCrouching()
               && !entity.isAutoSpinAttack()
               && !entity.isVisuallySwimming()
               && !entity.isFallFlying()
               && !entity.isBlocking()
               && !entity.onClimbable()
               && !(entity instanceof Player p && p.getAbilities().flying)) {
               if (System.currentTimeMillis() - lastActivityTime > 1000L) {
                  return;
               }
            } else {
               lastActivityTime = System.currentTimeMillis();
            }
         }

         Window window = minecraft.getWindow();
         float screenWidth = window.getWidth();
         float screenHeight = window.getHeight();
         float guiScale = window.getGuiScale();
         float scale = Configs.PAPERDOLL_SCALE.tempValue;
         float x = scale * 25.0F;
         float y = scale * 45.0F;
         x += Configs.PAPERDOLL_X.tempValue / 100.0F * screenWidth / guiScale;
         y += Configs.PAPERDOLL_Y.tempValue / 100.0F * screenHeight / guiScale;
         float x2 = x * 2.0F;
         float y2 = y * 2.0F;
         UIHelper.drawEntity(
            x,
            y,
            scale * 30.0F,
            Configs.PAPERDOLL_PITCH.tempValue,
            Configs.PAPERDOLL_YAW.tempValue,
            entity,
            gui,
            new Vector3f(),
            EntityRenderMode.PAPERDOLL,
            0,
            0,
            (int)x2,
            (int)y2
         );
      }
   }
}
