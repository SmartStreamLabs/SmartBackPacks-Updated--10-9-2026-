package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public class BackpackLinkSavedData extends SavedData {
    public static final String ID = SmartBackpacks.MOD_ID + "_link_anchors";
    public static final Codec<BackpackLinkSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BackpackLinkAnchor.CODEC.listOf().optionalFieldOf("anchors", List.of()).forGetter(BackpackLinkSavedData::anchorList)
    ).apply(instance, BackpackLinkSavedData::new));

    private final Map<UUID, BackpackLinkAnchor> anchors = new LinkedHashMap<>();

    public BackpackLinkSavedData() {
    }

    public BackpackLinkSavedData(List<BackpackLinkAnchor> anchors) {
        for (BackpackLinkAnchor anchor : anchors) {
            if (!BackpackLinkData.EMPTY_UUID.equals(anchor.anchorId())) {
                this.anchors.put(anchor.anchorId(), anchor);
            }
        }
    }

    public Optional<BackpackLinkAnchor> get(UUID anchorId) {
        return Optional.ofNullable(this.anchors.get(anchorId));
    }

    public List<BackpackLinkAnchor> anchors() {
        return List.copyOf(this.anchors.values());
    }

    public void upsert(BackpackLinkAnchor anchor) {
        BackpackLinkAnchor previous = this.anchors.put(anchor.anchorId(), anchor);
        if (!anchor.equals(previous)) {
            this.setDirty();
        }
    }

    public void markOffline(UUID anchorId, long gameTime) {
        BackpackLinkAnchor anchor = this.anchors.get(anchorId);
        if (anchor == null || (!anchor.active() && !anchor.placed())) {
            return;
        }

        this.anchors.put(anchorId, anchor.offline(gameTime));
        this.setDirty();
    }

    public boolean addLink(UUID sourceId, UUID destinationId, int maxLinks) {
        if (sourceId.equals(destinationId)) {
            return false;
        }

        BackpackLinkAnchor source = this.anchors.get(sourceId);
        BackpackLinkAnchor destination = this.anchors.get(destinationId);
        if (source == null || destination == null) {
            return false;
        }

        boolean changed = false;
        List<UUID> sourceLinks = new ArrayList<>(source.links());
        if (!sourceLinks.contains(destinationId)) {
            if (sourceLinks.size() >= maxLinks) {
                return false;
            }
            sourceLinks.add(destinationId);
            this.anchors.put(sourceId, source.withLinks(sourceLinks));
            changed = true;
        }

        List<UUID> destinationLinks = new ArrayList<>(destination.links());
        if (!destinationLinks.contains(sourceId)) {
            if (destinationLinks.size() >= maxLinks) {
                return changed;
            }
            destinationLinks.add(sourceId);
            this.anchors.put(destinationId, destination.withLinks(destinationLinks));
            changed = true;
        }

        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    public void removeLink(UUID sourceId, UUID destinationId) {
        boolean changed = false;
        BackpackLinkAnchor source = this.anchors.get(sourceId);
        if (source != null && source.links().contains(destinationId)) {
            ArrayList<UUID> links = new ArrayList<>(source.links());
            links.remove(destinationId);
            this.anchors.put(sourceId, source.withLinks(links));
            changed = true;
        }

        BackpackLinkAnchor destination = this.anchors.get(destinationId);
        if (destination != null && destination.links().contains(sourceId)) {
            ArrayList<UUID> links = new ArrayList<>(destination.links());
            links.remove(sourceId);
            this.anchors.put(destinationId, destination.withLinks(links));
            changed = true;
        }

        if (changed) {
            this.setDirty();
        }
    }

    private List<BackpackLinkAnchor> anchorList() {
        return List.copyOf(this.anchors.values());
    }

    public static BackpackLinkSavedData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(BackpackLinkSavedData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(null);
        if (encoded instanceof CompoundTag compoundTag) {
            tag.merge(compoundTag);
        }
        return tag;
    }
}
