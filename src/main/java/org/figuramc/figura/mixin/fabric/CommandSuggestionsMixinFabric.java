package org.figuramc.figura.mixin.fabric;

import com.mojang.brigadier.StringReader;
import java.util.Collection;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.SuggestionsListAccessor;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixinFabric {
   @Shadow
   private SuggestionsList suggestions;
   @Unique
   private boolean emojiSuggestions;

   @Shadow
   public abstract void showSuggestions(boolean var1);

   @Inject(
      method = "updateCommandInfo",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"
      ),
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void addFiguraSuggestions(
      CallbackInfo ci, String string, StringReader stringReader, boolean bl2, boolean bl3, int i, String string2, int j, Collection<String> collection
   ) {
      this.emojiSuggestions = false;
      if (Configs.EMOJIS.value != 0) {
         String lastWord = string2.substring(j);
         Collection<String> emojis = Emojis.getMatchingEmojis(lastWord);
         this.emojiSuggestions = !emojis.isEmpty();
         if (this.emojiSuggestions) {
            collection.addAll(emojis);
         }
      }
   }

   @Inject(
      method = "updateCommandInfo",
      at = @At(
         value = "INVOKE_ASSIGN",
         target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
         shift = Shift.AFTER
      )
   )
   private void afterSuggesting(CallbackInfo ci) {
      if (this.emojiSuggestions && Configs.EMOJIS.value >= 2) {
         this.showSuggestions(false);
      }
   }

   @Inject(method = "showSuggestions", at = @At("RETURN"))
   private void showSuggestions(CallbackInfo ci) {
      if (this.emojiSuggestions && this.suggestions != null) {
         ((SuggestionsListAccessor)this.suggestions).figura$setFiguraList(true);
         this.emojiSuggestions = false;
      }
   }
}
