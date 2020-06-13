package com.hrznstudio.galacticraft.world.biome.source;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.function.LongFunction;

import com.hrznstudio.galacticraft.world.biome.GalacticraftBiomes;
import com.hrznstudio.galacticraft.world.biome.layer.MoonBiomeLayers;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.layer.ContinentLayer;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;

import static net.minecraft.world.biome.source.VanillaLayeredBiomeSource.CODEC;

public class MoonBiomeSource extends BiomeSource {
   private final BiomeLayerSampler sampler;
   private static final List<Biome> BIOMES = ImmutableList.of(GalacticraftBiomes.MOON_HIGHLANDS_PLAINS, GalacticraftBiomes.MOON_HIGHLANDS_CRATERS, GalacticraftBiomes.MOON_HIGHLANDS_ROCKS,
           GalacticraftBiomes.MOON_MARE_PLAINS, GalacticraftBiomes.MOON_MARE_CRATERS, GalacticraftBiomes.MOON_MARE_ROCKS, GalacticraftBiomes.MOON_CHEESE_FOREST);
   private final int biomeSize;

   public MoonBiomeSource(long seed, int biomeSize) {
      super(BIOMES);
      this.biomeSize = biomeSize;

      this.sampler = MoonBiomeLayers.build(seed, biomeSize, 0);
   }

   @Override
   protected Codec<? extends BiomeSource> method_28442() {
      return CODEC;
   }

   @Environment(EnvType.CLIENT)
   public BiomeSource withSeed(long seed) {
      return new MoonBiomeSource(seed, this.biomeSize);
   }

   public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      return this.sampler.sample(biomeX, biomeZ);
   }
}
