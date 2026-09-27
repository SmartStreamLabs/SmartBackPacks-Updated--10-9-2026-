package com.teamsmartstreamlabs.smartbackpacks.worldgen;

import com.mojang.serialization.MapCodec;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class BackpackerCampStructure extends Structure {
    public static final MapCodec<BackpackerCampStructure> CODEC = simpleCodec(BackpackerCampStructure::new);

    public BackpackerCampStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!SmartBackpacksConfig.backpackerCampEnabled()) return Optional.empty();
        int x = context.chunkPos().getMinBlockX() + 2;
        int z = context.chunkPos().getMinBlockZ() + 2;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = 0; dx <= 10; dx++) {
            for (int dz = 0; dz <= 10; dz++) {
                int height = context.chunkGenerator().getFirstOccupiedHeight(x + dx, z + dz,
                        Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                min = Math.min(min, height);
                max = Math.max(max, height);
            }
        }
        if (max - min > 2 || min <= context.chunkGenerator().getSeaLevel() + 1) return Optional.empty();
        int y = min;
        return Optional.of(new GenerationStub(new BlockPos(x + 5, y, z + 5), pieces ->
                pieces.addPiece(new BackpackerCampPiece(x, y, z, context.random()))));
    }

    @Override
    public StructureType<?> type() {
        return ModCampStructures.CAMP.get();
    }
}
