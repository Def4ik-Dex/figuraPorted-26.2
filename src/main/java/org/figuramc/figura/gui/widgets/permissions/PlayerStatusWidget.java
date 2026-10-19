package org.figuramc.figura.gui.widgets.permissions;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.gui.widgets.StatusWidget;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;

public class PlayerStatusWidget extends StatusWidget {
   private static final List<Function<Avatar, MutableComponent>> HOVER_TEXT = List.of(
      avatar -> FiguraText.of("gui.permissions.size").append("\n• ").append(MathUtils.asFileSize(avatar.fileSize)),
      avatar -> FiguraText.of("gui.permissions.complexity").append("\n• ").append(String.valueOf(avatar.complexity.pre)),
      avatar -> FiguraText.of("gui.permissions.init")
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.init.root", avatar.init.pre))
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.init.entity", avatar.init.post)),
      avatar -> FiguraText.of("gui.permissions.tick")
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.tick.world", avatar.worldTick.pre))
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.tick.entity", avatar.tick.pre)),
      avatar -> FiguraText.of("gui.permissions.render")
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.render.world", avatar.worldRender.pre))
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.render.entity", avatar.render.pre))
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.render.post_entity", avatar.render.post))
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.render.post_world", avatar.worldRender.post))
         .append("\n• ")
         .append(FiguraText.of("gui.permissions.render.animations", avatar.animation.pre))
   );
   private final UUID owner;
   private Avatar avatar;

   public PlayerStatusWidget(int x, int y, int width, UUID owner) {
      super(x, y, width, HOVER_TEXT.size());
      this.setBackground(false);
      this.owner = owner;
   }

   @Override
   public void tick() {
      this.avatar = AvatarManager.getAvatarForPlayer(this.owner);
      if (this.avatar != null && this.avatar.nbt != null) {
         this.status = !FiguraMod.isLocal(this.owner)
            ? 3
            : (this.avatar.fileSize > NetworkStuff.getSizeLimit() ? 1 : (this.avatar.fileSize > NetworkStuff.getSizeLimit() * 0.75 ? 2 : 3));
         int complexity = this.avatar.renderer == null ? 0 : (this.avatar.complexity.pre >= this.avatar.permissions.get(Permissions.COMPLEXITY) ? 1 : 3);
         this.status += complexity << 2;
         int init = this.avatar.scriptError
            ? 1
            : (this.avatar.luaRuntime == null ? 0 : (this.avatar.init.getTotal() >= this.avatar.permissions.get(Permissions.INIT_INST) * 0.75 ? 2 : 3));
         this.status += init << 4;
         int tick = this.avatar.scriptError
            ? 1
            : (
               this.avatar.luaRuntime == null
                  ? 0
                  : (
                     !(this.avatar.tick.getTotal() >= this.avatar.permissions.get(Permissions.TICK_INST) * 0.75)
                           && !(this.avatar.worldTick.getTotal() >= this.avatar.permissions.get(Permissions.WORLD_TICK_INST) * 0.75)
                        ? 3
                        : 2
                  )
            );
         this.status += tick << 6;
         int render = this.avatar.scriptError
            ? 1
            : (
               this.avatar.luaRuntime == null
                  ? 0
                  : (
                     !(this.avatar.render.getTotal() >= this.avatar.permissions.get(Permissions.RENDER_INST) * 0.75)
                           && !(this.avatar.worldRender.getTotal() >= this.avatar.permissions.get(Permissions.WORLD_RENDER_INST) * 0.75)
                        ? 3
                        : 2
                  )
            );
         this.status += render << 8;
      } else {
         this.status = 0;
      }
   }

   @Override
   public Component getTooltipFor(int i) {
      return this.avatar == null ? null : HOVER_TEXT.get(i).apply(this.avatar).setStyle(TEXT_COLORS.get(this.status >> i * 2 & 3));
   }
}
