package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BackpackRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public BackpackRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack backpack = this.getVisibleBackpack(player);
        if (backpack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.06F, 0.16F);
            poseStack.mulPose(Axis.XP.rotationDegrees(8.0F));
        } else {
            poseStack.translate(0.0F, 0.0F, 0.15F);
        }
        poseStack.translate(0.0F, 0.22F, 0.18F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.42F, 0.42F, 0.42F);
        Minecraft.getInstance().getItemRenderer().renderStatic(backpack, ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), 0);
        poseStack.popPose();
    }

    private ItemStack getVisibleBackpack(AbstractClientPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (BackpackItem.isBackpack(chest)) {
            return chest;
        }

        return CuriosCompat.findFirstMatchingBackStack(player, BackpackItem::isBackpack)
                .map(CuriosCompat.BackSlotMatch::stack)
                .filter(BackpackItem::isBackpack)
                .orElse(ItemStack.EMPTY);
    }
}
