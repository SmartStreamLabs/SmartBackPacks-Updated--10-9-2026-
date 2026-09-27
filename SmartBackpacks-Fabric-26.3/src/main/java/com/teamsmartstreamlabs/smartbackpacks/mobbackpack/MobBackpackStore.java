package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.world.entity.Mob;

public final class MobBackpackStore {
    private MobBackpackStore() {
    }

    public static MobBackpackState get(Mob mob) {
        return ((AttachmentTarget) mob).getAttachedOrElse(MobBackpackAttachments.STATE, MobBackpackState.UNPROCESSED);
    }

    public static void set(Mob mob, MobBackpackState state) {
        ((AttachmentTarget) mob).setAttached(MobBackpackAttachments.STATE, state);
    }
}
