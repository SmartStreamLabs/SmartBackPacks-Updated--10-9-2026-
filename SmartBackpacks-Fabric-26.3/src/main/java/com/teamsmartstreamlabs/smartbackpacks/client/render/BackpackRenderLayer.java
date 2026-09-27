package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class BackpackRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final ItemStackRenderState backpackRenderState = new ItemStackRenderState();

    public BackpackRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AvatarRenderState playerRenderState,
            float yRot, float xRot) {
        AbstractClientPlayer player = this.resolvePlayer(playerRenderState);
        if (player == null) {
            return;
        }

        ItemStack backpack = this.getVisibleBackpack(player);
        if (backpack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.06F, 0.16F);
            poseStack.mulPose(new Matrix4f().rotate(Axis.XP.rotationDegrees(8.0F)));
        } else {
            poseStack.translate(0.0F, 0.0F, 0.15F);
        }
        poseStack.translate(0.0F, 0.22F, 0.18F);
        poseStack.mulPose(new Matrix4f().rotate(Axis.YP.rotationDegrees(180.0F)));
        poseStack.mulPose(new Matrix4f().rotate(Axis.ZP.rotationDegrees(180.0F)));
        poseStack.scale(0.52F, 0.52F, 0.52F);
        this.backpackRenderState.clear();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(this.backpackRenderState, backpack, ItemDisplayContext.FIXED,
                player.level(), player, player.getId());
        this.backpackRenderState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private AbstractClientPlayer resolvePlayer(AvatarRenderState playerRenderState) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        return minecraft.level.getEntity(playerRenderState.id) instanceof AbstractClientPlayer player ? player : null;
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
