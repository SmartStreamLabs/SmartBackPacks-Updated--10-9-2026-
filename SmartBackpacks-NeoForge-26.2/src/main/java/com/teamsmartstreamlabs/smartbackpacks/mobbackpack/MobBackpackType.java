package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.DyeColor;

public enum MobBackpackType {
    ZOMBIE("zombie", DyeColor.GREEN),
    HUSK("husk", DyeColor.YELLOW),
    DROWNED("drowned", DyeColor.CYAN),
    SKELETON("skeleton", DyeColor.LIGHT_GRAY),
    STRAY("stray", DyeColor.LIGHT_BLUE),
    CREEPER("creeper", DyeColor.LIME),
    PILLAGER("pillager", DyeColor.GRAY),
    VINDICATOR("vindicator", DyeColor.GRAY),
    WITCH("witch", DyeColor.PURPLE),
    PIGLIN("piglin", DyeColor.ORANGE),
    ZOMBIFIED_PIGLIN("zombified_piglin", DyeColor.PINK);

    private final String id;
    private final DyeColor defaultColor;

    MobBackpackType(String id, DyeColor defaultColor) {
        this.id = id;
        this.defaultColor = defaultColor;
    }

    public String id() { return this.id; }
    public DyeColor defaultColor() { return this.defaultColor; }

    public static MobBackpackType from(Mob mob) {
        if (mob instanceof Husk) return HUSK;
        if (mob instanceof Drowned) return DROWNED;
        if (mob instanceof Zombie && !(mob instanceof ZombifiedPiglin)) return ZOMBIE;
        if (mob instanceof Stray) return STRAY;
        if (mob instanceof Skeleton) return SKELETON;
        if (mob instanceof Creeper) return CREEPER;
        if (mob instanceof Pillager) return PILLAGER;
        if (mob instanceof Vindicator) return VINDICATOR;
        if (mob instanceof Witch) return WITCH;
        if (mob instanceof Piglin) return PIGLIN;
        if (mob instanceof ZombifiedPiglin) return ZOMBIFIED_PIGLIN;
        return null;
    }
}
