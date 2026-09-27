package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackDisplayHookBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.BackpackDisplayHookBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class BackpackDisplayHookRenderer implements BlockEntityRenderer<BackpackDisplayHookBlockEntity> {
    public BackpackDisplayHookRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BackpackDisplayHookBlockEntity hook, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        ItemStack backpack = hook.getStoredBackpack();
        if (backpack.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5F, 0.22F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - hook.getBlockState().getValue(BackpackDisplayHookBlock.FACING).toYRot()));
        pose.translate(0.0F, 0.0F, 0.05F);
        pose.scale(0.47F, 0.47F, 0.47F);
        Minecraft.getInstance().getItemRenderer().renderStatic(backpack, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, pose, buffer, hook.getLevel(), (int) hook.getBlockPos().asLong());
        pose.popPose();
    }
}
