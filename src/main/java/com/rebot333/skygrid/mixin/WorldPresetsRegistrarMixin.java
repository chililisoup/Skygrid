package com.rebot333.skygrid.mixin;

import com.rebot333.skygrid.SkygridChunkGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

// if you can't mixin here, make sure your access widener is set up correctly!
@Mixin(WorldPresets.Registrar.class)
public abstract class WorldPresetsRegistrarMixin {
    // defining our registry key. this key provides an Identifier for our preset, that we can use for our lang files and data elements.
    private static final RegistryKey<WorldPreset> SKYGRID = RegistryKey.of(Registry.WORLD_PRESET_KEY, new Identifier("skygrid", "skygrid"));
    @Shadow protected abstract RegistryEntry<WorldPreset> register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);
    @Shadow protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);

    @Inject(method = "initAndGetDefault", at = @At("RETURN"))
    private void addPresets(CallbackInfoReturnable<RegistryEntry<WorldPreset>> cir) {
        // the register() method is shadowed from the target class
        this.register(SKYGRID, this.createOverworldOptions(
                // a FlatChunkGenerator is the easiest way to get a void world, but you can replace this FlatChunkGenerator constructor with a NoiseChunkGenerator, or your own custom ChunkGenerator.
                //new SkygridChunkGenerator(null, new SkygridChunkGeneratorConfig())
                new SkygridChunkGenerator(BuiltinRegistries.STRUCTURE_SET, new FlatChunkGeneratorConfig(Optional.empty(), BuiltinRegistries.BIOME))
            )
        );
    }
}