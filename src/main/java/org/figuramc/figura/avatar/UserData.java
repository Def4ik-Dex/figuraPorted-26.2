package org.figuramc.figura.avatar;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.nbt.CompoundTag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;

public class UserData {
   public final UUID id;
   private final Queue<Avatar> avatars = new ConcurrentLinkedQueue<>();
   private Pair<BitSet, BitSet> badges;

   public UserData(UUID id) {
      this.id = id;
   }

   public void loadData(ArrayList<Pair<String, Pair<String, UUID>>> avatars, Pair<BitSet, BitSet> badges) {
      this.loadBadges(badges);
      this.clear();

      for (Pair<String, Pair<String, UUID>> avatar : avatars) {
         if (!CacheAvatarLoader.checkAndLoad((String)avatar.getFirst(), this)) {
            Pair<String, UUID> pair = (Pair<String, UUID>)avatar.getSecond();
            NetworkStuff.getAvatar(this, (UUID)pair.getSecond(), (String)pair.getFirst(), (String)avatar.getFirst());
         }
      }
   }

   public void loadAvatar(CompoundTag nbt) {
      Avatar avatar = new Avatar(this.id);
      this.avatars.add(avatar);
      avatar.load(nbt);
      FiguraMod.debug("Loaded avatar for " + this.id);
   }

   public void loadBadges(Pair<BitSet, BitSet> pair) {
      this.badges = pair;
   }

   public Pair<BitSet, BitSet> getBadges() {
      return this.badges;
   }

   public Queue<Avatar> getAvatars() {
      return this.avatars;
   }

   public Avatar getMainAvatar() {
      return this.avatars.peek();
   }

   public void clear() {
      for (Avatar avatar : this.avatars) {
         avatar.clean();
      }

      this.avatars.clear();
   }
}
