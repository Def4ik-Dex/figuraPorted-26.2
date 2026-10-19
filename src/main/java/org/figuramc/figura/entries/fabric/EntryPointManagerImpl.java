package org.figuramc.figura.entries.fabric;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.figuramc.figura.FiguraMod;

public class EntryPointManagerImpl {
   public static <T> Set<T> load(String name, Class<T> clazz) {
      Set<T> ret = new HashSet<>();

      for (EntrypointContainer<T> entrypoint : FabricLoader.getInstance().getEntrypointContainers(name, clazz)) {
         ModMetadata metadata = entrypoint.getProvider().getMetadata();
         String modId = metadata.getId();

         try {
            ret.add((T)entrypoint.getEntrypoint());
         } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load entrypoint of mod {}", modId, e);
         }
      }

      return ret;
   }
}
