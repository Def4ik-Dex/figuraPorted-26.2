package org.figuramc.figura.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public interface BiomeAccessor {
   @Invoker("getTemperature")
   float getTheTemperature(BlockPos var1, int var2);

   @Accessor("climateSettings")
   ClimateSettings getClimateSettings();
}
