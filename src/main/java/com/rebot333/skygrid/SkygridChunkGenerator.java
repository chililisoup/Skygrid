package com.rebot333.skygrid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureSet;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.*;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SkygridChunkGenerator extends ChunkGenerator {

    /*
    private final SkygridChunkGeneratorConfig config;
    public SkygridChunkGeneratorConfig getConfig() { return this.config; }
    public SkygridChunkGenerator(Registry<StructureSet> structureSetRegistry, SkygridChunkGeneratorConfig config) {
        super(structureSetRegistry, Optional.of(config.getStructureOverrides()), new FixedBiomeSource(config.getBiome()));
        this.config = config;
    }
    public static final Codec<SkygridChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance ->
                    createStructureSetRegistryGetter(instance).and(
                            SkygridChunkGeneratorConfig.CODEC.fieldOf("config").forGetter(SkygridChunkGenerator::getConfig)
                    ).apply(instance, SkygridChunkGenerator::new));
*/

    //====================================
    private final FlatChunkGeneratorConfig config;
    public FlatChunkGeneratorConfig getConfig() { return this.config; }

    public SkygridChunkGenerator(Registry<StructureSet> structureSetRegistry, FlatChunkGeneratorConfig config) {
        super(structureSetRegistry, config.getStructureOverrides(), new FixedBiomeSource(config.getBiome()));
        this.config = config;
    }
    public static final Codec<SkygridChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance ->
                    createStructureSetRegistryGetter(instance).and(
                            FlatChunkGeneratorConfig.CODEC.fieldOf("config").forGetter(SkygridChunkGenerator::getConfig)
                    ).apply(instance, SkygridChunkGenerator::new));
    ///====================================

    public static List<BlockState> getBlockStates() {
        RegistryEntryList.Named<Block> blocks = Registry.BLOCK.getOrCreateEntryList(TagKey.of(Registry.BLOCK_KEY, new Identifier("skygrid", "overworld_generates")));
        List<BlockState> blockStates = new ArrayList<>();
        for (RegistryEntry<Block> block : blocks) {
            if (block.isIn(BlockTags.LEAVES)) blockStates.add(block.value().getDefaultState().with(Properties.PERSISTENT, true));
            else if (block.value() == Blocks.SCULK_SHRIEKER) blockStates.add(block.value().getDefaultState().with(Properties.CAN_SUMMON, true));
            else blockStates.add(block.value().getDefaultState());
        }
        return blockStates;
    }

    public static final RegistryEntryList.Named<Block> rarityIncreased = Registry.BLOCK.getOrCreateEntryList(TagKey.of(Registry.BLOCK_KEY, new Identifier("skygrid", "increase_rarity")));

    public static final EntityType[] spawnerMobs = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.CREEPER,
            EntityType.SILVERFISH,
            EntityType.GUARDIAN,
            EntityType.COW,
            EntityType.PIG,
            EntityType.CHICKEN
    };

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        BlockState blockState;
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        List<BlockState> blockStates = getBlockStates();

        // Chance to replace special block with a new random block (which could still end up being special hehe)
        final float specialChanceModifier = 0.85f;

        for(int i = 0; i <= 128; i+=4) {
            int j = chunk.getBottomY() + i;

            for(int k = 0; k < 16; k+=4) {
                for(int l = 0; l < 16; l+=4) {
                    blockState = blockStates.get(chunkRandom.nextBetween(0, blockStates.size() - 1));

                    if (rarityIncreased.contains(blockState.getBlock().getRegistryEntry()) && chunkRandom.nextFloat() < specialChanceModifier)
                        blockState = blockStates.get(chunkRandom.nextBetweenExclusive(0, blockStates.size()));

                    if (blockState.getBlock() == Blocks.CHEST) {
                        BlockPos chestPos = new BlockPos(chunk.getPos().x * 16 + k, j, chunk.getPos().z * 16 + l);
                        ChestBlockEntity chestEntity = new ChestBlockEntity(chestPos, blockState);
                        chestEntity.setLootTable(new Identifier("skygrid", "chest_loot"), 0L);
                        chunk.setBlockEntity(chestEntity);

                    } else if (blockState.getBlock() == Blocks.SPAWNER) {
                        BlockPos spawnerPos = new BlockPos(chunk.getPos().x * 16 + k, j, chunk.getPos().z * 16 + l);
                        MobSpawnerBlockEntity spawnerEntity = new MobSpawnerBlockEntity(spawnerPos, blockState);
                        spawnerEntity.getLogic().setEntityId(spawnerMobs[chunkRandom.nextBetweenExclusive(0, spawnerMobs.length)]);
                        chunk.setBlockEntity(spawnerEntity);
                    }

                    chunk.setBlockState(mutable.set(k, j, l), blockState, false);
                    heightmap.trackUpdate(k, j, l, blockState);
                    heightmap2.trackUpdate(k, j, l, blockState);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return world.getBottomY() + 128;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(world.getBottomY(), new BlockState[0]);
    }

    //public static final Codec<SkygridChunkGenerator> CODEC = MapCodec.of(Encoder.empty(), Decoder.unit(SkygridChunkGenerator::new)).codec();
    @Override protected Codec<? extends ChunkGenerator> getCodec() { return CODEC; }

    @Override public int getSeaLevel() { return 0; }
    @Override public int getMinimumY() { return 0; }
    @Override public int getWorldHeight() { return 0; }

    @Override public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {}
    @Override public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {}
    @Override public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {}
    @Override public void populateEntities(ChunkRegion region) {}
    @Override public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}
}
