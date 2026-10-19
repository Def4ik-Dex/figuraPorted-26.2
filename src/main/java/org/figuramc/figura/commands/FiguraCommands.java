package org.figuramc.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.BackendCommands;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.figuramc.figura.model.rendering.FiguraRenderer;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

public class FiguraCommands {
   public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommandRoot() {
      LiteralArgumentBuilder<FiguraClientCommandSource> root = LiteralArgumentBuilder.literal("figura");
      root.then(FiguraDocsManager.getCommand());
      root.then(LinkCommand.getCommand());
      root.then(RunCommand.getCommand());
      root.then(LoadCommand.getCommand());
      root.then(ReloadCommand.getCommand());
      root.then(DebugCommand.getCommand());
      root.then(ExportCommand.getCommand());
      if (FiguraMod.debugModeEnabled()) {
         root.then(BackendCommands.getCommand());
         root.then(AvatarManager.getCommand());
      }

      root.then(EmojiListCommand.getCommand());
      return root;
   }

   protected static Avatar checkAvatar(CommandContext<FiguraClientCommandSource> context) {
      Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
      if (avatar == null) {
         ((FiguraClientCommandSource)context.getSource()).figura$sendError(FiguraText.of("command.no_avatar_error"));
         return null;
      } else {
         return avatar;
      }
   }

   protected static FiguraLuaRuntime getRuntime(CommandContext<FiguraClientCommandSource> context) {
      Avatar avatar = checkAvatar(context);
      if (avatar == null) {
         return null;
      }

      if (avatar.luaRuntime != null && !avatar.scriptError) {
         return avatar.luaRuntime;
      }

      ((FiguraClientCommandSource)context.getSource()).figura$sendError(FiguraText.of("command.no_script_error"));
      return null;
   }

   protected static FiguraRenderer getRenderer(CommandContext<FiguraClientCommandSource> context) {
      Avatar avatar = checkAvatar(context);
      if (avatar == null) {
         return null;
      } else if (avatar.renderer == null) {
         ((FiguraClientCommandSource)context.getSource()).figura$sendError(FiguraText.of("command.no_renderer_error"));
         return null;
      } else {
         return avatar.renderer;
      }
   }
}
