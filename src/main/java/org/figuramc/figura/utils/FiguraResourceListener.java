package org.figuramc.figura.utils;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.figuramc.figura.utils.fabric.FiguraResourceListenerImpl;

public class FiguraResourceListener {
   private final String id;
   private final Consumer<ResourceManager> reloadConsumer;

   protected FiguraResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
      this.id = id;
      this.reloadConsumer = reloadConsumer;
   }

   public static FiguraResourceListener createResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
      return FiguraResourceListenerImpl.createResourceListener(id, reloadConsumer);
   }

   public String id() {
      return this.id;
   }

   public Consumer<ResourceManager> reloadConsumer() {
      return this.reloadConsumer;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         FiguraResourceListener that = (FiguraResourceListener)obj;
         return Objects.equals(this.id, that.id) && Objects.equals(this.reloadConsumer, that.reloadConsumer);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.reloadConsumer);
   }

   @Override
   public String toString() {
      return "FiguraResourceListener[id=" + this.id + ", reloadConsumer=" + this.reloadConsumer + "]";
   }
}
