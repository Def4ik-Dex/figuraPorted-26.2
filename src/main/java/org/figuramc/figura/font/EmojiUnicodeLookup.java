package org.figuramc.figura.font;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class EmojiUnicodeLookup {
   private final Map<String, String> unicodeLookup = new HashMap<>();
   private final Map<String, String[]> reverseUnicodeLookup = new HashMap<>();
   private final Map<Integer, EmojiMetadata> metadataLookup = new HashMap<>();
   private final Map<String, String> shortcutLookup = new HashMap<>();
   private final Map<String, String[]> reverseShortcutLookup = new HashMap<>();

   public void putAliases(String[] aliases, String unicode) {
      for (String alias : aliases) {
         this.unicodeLookup.put(alias, unicode);
      }

      this.reverseUnicodeLookup.put(unicode, aliases);
   }

   public void putShortcuts(String[] shortcuts, String unicode) {
      for (String alias : shortcuts) {
         this.shortcutLookup.put(alias, unicode);
      }

      this.reverseShortcutLookup.put(unicode, shortcuts);
   }

   public void putMetadata(int codepoint, EmojiMetadata metadata) {
      this.metadataLookup.put(codepoint, metadata);
   }

   public Collection<String> getNames() {
      return this.unicodeLookup.keySet();
   }

   @Nullable
   public String[] getNames(String unicode) {
      return this.reverseUnicodeLookup.getOrDefault(unicode, null);
   }

   public Collection<String> getShortcuts() {
      return this.shortcutLookup.keySet();
   }

   @Nullable
   public String[] getShortcuts(String unicode) {
      return this.reverseShortcutLookup.getOrDefault(unicode, null);
   }

   @Nullable
   public EmojiMetadata getMetadata(int codepoint) {
      return this.metadataLookup.getOrDefault(codepoint, null);
   }

   @Nullable
   public String getUnicode(String emojiAlias) {
      return this.unicodeLookup.getOrDefault(emojiAlias, null);
   }

   @Nullable
   public String getUnicodeForShortcut(String shortcut) {
      return this.shortcutLookup.getOrDefault(shortcut, null);
   }

   public Collection<EmojiMetadata> metadataValues() {
      return this.metadataLookup.values();
   }

   public Collection<String> unicodeValues() {
      return this.reverseUnicodeLookup.keySet();
   }
}
