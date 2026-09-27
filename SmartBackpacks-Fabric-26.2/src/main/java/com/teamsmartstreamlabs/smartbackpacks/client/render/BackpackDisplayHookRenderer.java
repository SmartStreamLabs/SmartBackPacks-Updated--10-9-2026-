package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackDisplayHookBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.BackpackDisplayHookBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class BackpackDisplayHookRenderer implements BlockEntityRenderer<BackpackDisplayHookBlockEntity, BackpackDisplayHookRenderer.State> {
    private final ItemModelResolver itemModels;

    public BackpackDisplayHookRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModels = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BackpackDisplayHookBlockEntity hook, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(hook, state, partialTick, camera, crumbling);
        state.facing = hook.getBlockState().getValue(BackpackDisplayHookBlock.FACING);
        state.backpack.clear();
        if (!hook.getStoredBackpack().isEmpty()) {
            this.itemModels.updateForTopItem(state.backpack, hook.getStoredBackpack(), ItemDisplayContext.FIXED,
                    hook.getLevel(), null, (int) hook.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.backpack.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5F, 0.22F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - state.facing.toYRot()));
        pose.translate(0.0F, 0.0F, 0.05F);
        pose.scale(0.47F, 0.47F, 0.47F);
        state.backpack.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        final ItemStackRenderState backpack = new ItemStackRenderState();
    }
}
