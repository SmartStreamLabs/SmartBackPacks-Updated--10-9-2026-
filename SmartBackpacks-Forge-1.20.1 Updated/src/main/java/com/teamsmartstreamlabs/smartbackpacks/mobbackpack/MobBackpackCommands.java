package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class MobBackpackCommands {
    private MobBackpackCommands() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> summon = Commands.literal("summon");
        for (MobBackpackType type : MobBackpackType.values()) {
            summon.then(Commands.literal(type.id()).executes(context -> summon(context.getSource(), type)));
        }
        dispatcher.register(Commands.literal("smartbackpacks")
                .requires(source -> source.hasPermission(2))
                .then(summon));
    }

    private static int summon(CommandSourceStack source, MobBackpackType type) {
        ServerLevel level = source.getLevel();
        Mob mob = entityType(type).create(level);
        if (mob == null) {
            source.sendFailure(Component.literal("Could not create " + type.id()));
            return 0;
        }

        Vec3 position = source.getPosition();
        mob.moveTo(position.x + 2.0D, position.y, position.z, source.getRotation().y, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.COMMAND, null, null);
        MobBackpackHandler.assignGuaranteed(level, mob);
        if (!level.addFreshEntity(mob)) {
            source.sendFailure(Component.literal("Could not add " + type.id() + " to the world"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Summoned " + type.id() + " with a Smart Backpack"), true);
        return 1;
    }

    private static EntityType<? extends Mob> entityType(MobBackpackType type) {
        return switch (type) {
            case ZOMBIE -> EntityType.ZOMBIE;
            case HUSK -> EntityType.HUSK;
            case DROWNED -> EntityType.DROWNED;
            case SKELETON -> EntityType.SKELETON;
            case STRAY -> EntityType.STRAY;
            case CREEPER -> EntityType.CREEPER;
            case PILLAGER -> EntityType.PILLAGER;
            case VINDICATOR -> EntityType.VINDICATOR;
            case WITCH -> EntityType.WITCH;
            case PIGLIN -> EntityType.PIGLIN;
            case ZOMBIFIED_PIGLIN -> EntityType.ZOMBIFIED_PIGLIN;
        };
    }
}
