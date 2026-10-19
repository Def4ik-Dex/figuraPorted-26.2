package org.figuramc.figura.ducks;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface FiguraItemModelResolverExtension {
   int figura$getModelComplexity(ItemStack var1, RandomSource var2);
}
