package com.rebot333.skygrid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class SkygridChunkGeneratorConfig {
    public static final Codec<SkygridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance ->
                    instance.group(
                            Codecs.NONNEGATIVE_INT.fieldOf("generatorType").forGetter(SkygridChunkGeneratorConfig::getGeneratorType)
                    ).apply(instance, SkygridChunkGeneratorConfig::new)
    );

    private final RegistryEntryList<StructureSet> structureOverrides;
    private final RegistryEntry<Biome> biome;
    private final int generatorType;

    public SkygridChunkGeneratorConfig() { this(0); }
    public SkygridChunkGeneratorConfig(int generatorType) {
        this.structureOverrides = RegistryEntryList.of(BuiltinRegistries.STRUCTURE_SET.entryOf(StructureSetKeys.STRONGHOLDS));
        Registry<Biome> biomeRegistry = BuiltinRegistries.BIOME;
        this.biome = biomeRegistry.getOrCreateEntry(BiomeKeys.PLAINS);
        this.generatorType = generatorType;
    }


    public RegistryEntryList<StructureSet> getStructureOverrides() { return this.structureOverrides; }
    public RegistryEntry<Biome> getBiome() { return this.biome; }
    public int getGeneratorType() { return this.generatorType; }
}
