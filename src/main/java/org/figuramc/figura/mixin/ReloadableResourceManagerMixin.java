package org.figuramc.figura.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.figuramc.figura.resources.FiguraRuntimeResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
   @ModifyVariable(at = @At("HEAD"), method = "createReload", argsOnly = true)
   private List<PackResources> createReload(List<PackResources> packs) {
      List<PackResources> list = new ArrayList<>(packs);
      int index = 0;

      for (int i = 0; i < list.size(); i++) {
         String id = list.get(i).packId();
         if ("Fabric Mods".equals(id) || "vanilla".equals(id)) {
            index = i + 1;
         }
      }

      FiguraRuntimeResources.joinFuture();
      list.add(index, FiguraRuntimeResources.PACK);
      return list;
   }
}
