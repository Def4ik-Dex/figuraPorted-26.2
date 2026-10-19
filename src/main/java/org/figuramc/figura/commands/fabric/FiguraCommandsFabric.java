package org.figuramc.figura.commands.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.figuramc.figura.commands.FiguraCommands;
import org.figuramc.figura.utils.FiguraClientCommandSource;

public class FiguraCommandsFabric {
   public static void init() {
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
         @SuppressWarnings("unchecked")
         CommandDispatcher<FiguraClientCommandSource> casted = (CommandDispatcher<FiguraClientCommandSource>)(Object)dispatcher;
         casted.register(FiguraCommands.getCommandRoot());
      });
   }
}
