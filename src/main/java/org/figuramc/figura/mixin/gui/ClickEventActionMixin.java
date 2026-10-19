package org.figuramc.figura.mixin.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.util.StringRepresentable;
import org.figuramc.figura.gui.FiguraFunctionClickEvent;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Action.class)
public class ClickEventActionMixin {
   @Shadow
   @Final
   @Mutable
   private static Action[] $VALUES;
   @Shadow
   @Final
   private String name;
   @Shadow
   @Final
   @Mutable
   public static Codec<Action> UNSAFE_CODEC;
   @Shadow
   @Final
   @Mutable
   public static Codec<Action> CODEC;

   @Invoker("<init>")
   public static Action figura$invokeInit(String internalName, int internalId, String name, boolean user, MapCodec<? extends ClickEvent> codec) {
      throw new AssertionError();
   }

   private static Action figura$addVariant(String internalName, String name, boolean user, MapCodec<? extends ClickEvent> codec) {
      ArrayList<Action> variants = new ArrayList<>(Arrays.asList($VALUES));
      Action action = figura$invokeInit(internalName, variants.getLast().ordinal() + 1, name, user, codec);
      variants.add(action);
      $VALUES = variants.toArray(new Action[0]);
      UNSAFE_CODEC = StringRepresentable.fromEnum(Action::values);
      CODEC = UNSAFE_CODEC.validate(Action::filterForSerialization);
      return action;
   }

   @Inject(at = @At("HEAD"), method = "isAllowedFromServer", cancellable = true)
   private void isAllowedFromServer(CallbackInfoReturnable<Boolean> cir) {
      if (TextUtils.allowScriptEvents) {
         cir.setReturnValue(true);
      }
   }

   @Inject(at = @At("HEAD"), method = "filterForSerialization", cancellable = true)
   private static void filterForSerialization(Action action, CallbackInfoReturnable<DataResult<Action>> cir) {
      if (TextUtils.allowScriptEvents) {
         cir.setReturnValue(DataResult.success(action, Lifecycle.stable()));
      }
   }

   static {
      figura$addVariant("FIGURA_FUNCTION", "figura_function", false, FiguraFunctionClickEvent.CODEC);
   }
}
