package org.figuramc.figura.avatar;

import com.mojang.datafixers.util.Pair;
import java.util.BitSet;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class Badges {
   public static final Identifier FONT = FiguraIdentifier.of("badges");

   public static Component fetchBadges(UUID id) {
      if (PermissionManager.get(id).getCategory() == Permissions.Category.BLOCKED) {
         return Component.empty();
      }

      Pair<BitSet, BitSet> pair = AvatarManager.getBadges(id);
      if (pair == null) {
         return Component.empty();
      }

      MutableComponent badges = Component.empty().withStyle(Style.EMPTY.withFont(new Resource(FONT)).withColor(ChatFormatting.WHITE).withObfuscated(false));
      Avatar avatar = AvatarManager.getAvatarForPlayer(id);
      if (avatar != null) {
         if (!avatar.loaded) {
            badges.append(Component.literal(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)));
         } else if (avatar.nbt != null) {
            BitSet prideSet = (BitSet)pair.getFirst();
            Badges.Pride[] pride = Badges.Pride.values();
            int i = pride.length - 1;

            while (true) {
               if (i < 0) {
                  badges.append(Badges.System.DEFAULT.badge.copy().withStyle(Style.EMPTY.withColor(ColorUtils.rgbToInt(ColorUtils.userInputHex(avatar.color)))));
                  break;
               }

               if (prideSet.get(i)) {
                  badges.append(pride[i].badge);
                  break;
               }

               i--;
            }

            if (avatar.scriptError) {
               if (avatar.errorText == null) {
                  badges.append(Badges.System.ERROR.badge);
               } else {
                  badges.append(
                     Badges.System.ERROR
                        .badge
                        .copy()
                        .withStyle(Style.EMPTY.withHoverEvent(new ShowText(Badges.System.ERROR.desc.copy().append("\n\n").append(avatar.errorText))))
                  );
               }
            }

            if (avatar.versionStatus > 0) {
               badges.append(Badges.System.WARNING.badge);
            }

            if (!avatar.noPermissions.isEmpty()) {
               MutableComponent badge = Badges.System.PERMISSIONS.badge.copy();
               MutableComponent desc = Badges.System.PERMISSIONS.desc.copy().append("\n");

               for (Permissions t : avatar.noPermissions) {
                  desc.append("\n• ").append(FiguraText.of("badges.no_permissions." + t.name.toLowerCase(Locale.US)));
               }

               badges.append(badge.withStyle(Style.EMPTY.withHoverEvent(new ShowText(desc))));
            }
         }
      }

      if (avatar != null) {
         BitSet specialSet = (BitSet)pair.getSecond();
         Badges.Special[] specialValues = Badges.Special.values();

         for (int i = specialValues.length - 1; i >= 0; i--) {
            if (specialSet.get(i)) {
               Badges.Special special = specialValues[i];
               Integer color = special.color;
               if (avatar.badgeToColor.containsKey(special.name().toLowerCase(Locale.US))) {
                  color = ColorUtils.rgbToInt(ColorUtils.userInputHex(avatar.badgeToColor.get(special.name().toLowerCase(Locale.US))));
               }

               Component badge = (Component)(color != null ? special.badge.copy().withStyle(Style.EMPTY.withColor(color)) : special.badge);
               badges.append(badge);
            }
         }
      }

      if (avatar != null && Configs.SOUND_BADGE.value) {
         if (avatar.lastPlayingSound > 0) {
            badges.append(Badges.System.SOUND.badge);
         } else if (SoundAPI.getSoundEngine().figura$isPlaying(id)) {
            avatar.lastPlayingSound = 20;
            badges.append(Badges.System.SOUND.badge);
         }
      }

      return badges;
   }

   public static Component noBadges4U(Component text) {
      return TextUtils.replaceInText(
         text,
         "[-*/+=❗❌\ud83d\udee1★☆❤文✒\ud83d\uddff0-9a-f]",
         TextUtils.UNKNOWN,
         (s, style) -> style.getFont().equals(FONT) || style.getFont().equals(UIHelper.UI_FONT),
         Integer.MAX_VALUE
      );
   }

   public static Pair<BitSet, BitSet> emptyBadges() {
      return Pair.of(new BitSet(Badges.Pride.values().length), new BitSet(Badges.Special.values().length));
   }

   public static boolean hasCustomBadges(Component text) {
      return text.visit(
            (style, string) -> !string.contains("${badges}") && !string.contains("${segdab}") ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY
         )
         .isPresent();
   }

   public static Component appendBadges(Component text, UUID id, boolean allow) {
      Component badges = (Component)(allow ? fetchBadges(id) : Component.empty());
      boolean custom = hasCustomBadges(text);
      if (!custom) {
         return (Component)(badges.getString().isBlank() ? text : text.copy().append(" ").append(badges));
      }

      text = TextUtils.replaceInText(text, "\\$\\{badges\\}(?s)", badges);
      return TextUtils.replaceInText(text, "\\$\\{segdab\\}(?s)", TextUtils.reverse(badges));
   }

   public enum Pride {
      AGENDER("ᚠ"),
      AROACE("ᚡ"),
      AROMANTIC("ᚢ"),
      ASEXUAL("ᚣ"),
      BIGENDER("ᚤ"),
      BISEXUAL("ᚥ"),
      DEMIBOY("ᚦ"),
      DEMIGENDER("ᚧ"),
      DEMIGIRL("ᚨ"),
      DEMIROMANTIC("ᚩ"),
      DEMISEXUAL("ᚪ"),
      DISABILITY("ᚫ"),
      FINSEXUAL("ᚬ"),
      GAYMEN("ᚭ"),
      GENDERFAE("ᚮ"),
      GENDERFLUID("ᚯ"),
      GENDERQUEER("ᚰ"),
      INTERSEX("ᚱ"),
      LESBIAN("ᚲ"),
      NONBINARY("ᚳ"),
      PANSEXUAL("ᚴ"),
      PLURAL("ᚵ"),
      POLYSEXUAL("ᚶ"),
      PRIDE("ᚷ"),
      TRANSGENDER("ᚸ");

      public final Component badge;
      public final Component desc = FiguraText.of("badges.pride." + this.name().toLowerCase(Locale.US));

      Pride(String unicode) {
         this.badge = Component.literal(unicode).withStyle(Style.EMPTY.withHoverEvent(new ShowText(this.desc)));
      }
   }

   public enum Special {
      DEV("★"),
      DISCORD_STAFF("☆", ColorUtils.Colors.DISCORD.hex),
      CONTEST("☆", ColorUtils.Colors.AWESOME_BLUE.hex),
      DONATOR("❤", ColorUtils.Colors.AWESOME_BLUE.hex),
      TRANSLATOR("文"),
      TEXTURE_ARTIST("✒"),
      IMMORTALIZED("\ud83d\uddff");

      public final Component badge;
      public final Component desc = FiguraText.of("badges.special." + this.name().toLowerCase(Locale.US));
      public final Integer color;

      Special(String unicode) {
         this(unicode, null);
      }

      Special(String unicode, Integer color) {
         Style style = Style.EMPTY.withHoverEvent(new ShowText(this.desc));
         if (color != null) {
            style = style.withColor(color);
         }

         this.color = color;
         this.badge = Component.literal(unicode).withStyle(style);
      }
   }

   public enum System {
      DEFAULT("△"),
      PERMISSIONS("\ud83d\udee1"),
      WARNING("❗"),
      ERROR("❌"),
      SOUND("\ud83d\udd0a");

      public final Component badge;
      public final Component desc = FiguraText.of("badges.system." + this.name().toLowerCase(Locale.US));

      System(String unicode) {
         this.badge = Component.literal(unicode).withStyle(Style.EMPTY.withHoverEvent(new ShowText(this.desc)));
      }
   }
}
