package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class MobBackpackAttachments {
    public static final AttachmentType<MobBackpackState> STATE = AttachmentRegistry.<MobBackpackState>builder()
            .initializer(() -> MobBackpackState.UNPROCESSED)
            .persistent(MobBackpackState.CODEC)
            .syncWith(MobBackpackState.STREAM_CODEC, AttachmentSyncPredicate.all())
            .buildAndRegister(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "mob_backpack"));

    private MobBackpackAttachments() {
    }

    public static void init() {
    }
}
