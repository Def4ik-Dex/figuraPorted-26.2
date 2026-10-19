package org.figuramc.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

class LinkCommand {
   private static final List<FiguraMod.Links> LINKS = new ArrayList<FiguraMod.Links>() {
      {
         this.add(FiguraMod.Links.Wiki);
         this.add(FiguraMod.Links.Kofi);
         this.add(FiguraMod.Links.OpenCollective);
         this.add(null);
         this.add(FiguraMod.Links.Discord);
         this.add(FiguraMod.Links.Github);
         this.add(null);
         this.add(FiguraMod.Links.Modrinth);
         this.add(FiguraMod.Links.Curseforge);
      }
   };

   public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
      LiteralArgumentBuilder<FiguraClientCommandSource> links = LiteralArgumentBuilder.literal("links");
      links.executes(
         context -> {
            MutableComponent message = Component.empty()
               .withStyle(ColorUtils.Colors.AWESOME_BLUE.style)
               .append(Component.literal("•*+•* ").append(FiguraText.of()).append(" Links *•+*•").withStyle(ChatFormatting.UNDERLINE))
               .append("\n");

            for (FiguraMod.Links link : LINKS) {
               message.append("\n");
               if (link != null) {
                  message.append(
                     Component.literal("• [" + link.name() + "]")
                        .withStyle(link.style)
                        .withStyle(Style.EMPTY.withClickEvent(new OpenUrl(URI.create(link.url))))
                        .withStyle(Style.EMPTY.withHoverEvent(new ShowText(Component.literal(link.url))))
                  );
               }
            }

            FiguraMod.sendChatMessage(message);
            return 1;
         }
      );
      return links;
   }
}
