package org.figuramc.figura.ducks;

import java.util.List;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface FiguraItemStackRenderStateExtension {
   void figura$setItemStack(@Nullable ItemStack var1);

   ItemStack figura$getItemStack();

   boolean figura$isLeftHanded();

   ItemDisplayContext figura$getDisplayContext();

   ItemTransform figura$getItemTransform();

   List<BakedQuad> figura$getQuads();
}
