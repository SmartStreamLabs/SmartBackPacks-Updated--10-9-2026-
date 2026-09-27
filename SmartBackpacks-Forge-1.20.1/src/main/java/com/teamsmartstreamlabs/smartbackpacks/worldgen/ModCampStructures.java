package com.teamsmartstreamlabs.smartbackpacks.worldgen;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCampStructures {
    private static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, SmartBackpacks.MOD_ID);
    private static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, SmartBackpacks.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<StructureType<?>, StructureType<BackpackerCampStructure>> CAMP =
            STRUCTURES.register("backpacker_camp", () -> () -> BackpackerCampStructure.CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<StructurePieceType, StructurePieceType> CAMP_PIECE =
            PIECES.register("backpacker_camp", () -> (StructurePieceType.ContextlessType) BackpackerCampPiece::new);

    private ModCampStructures() {
    }

    public static void register(IEventBus bus) {
        STRUCTURES.register(bus);
        PIECES.register(bus);
    }
}
