package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MobBackpackRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final boolean creeper;

    public MobBackpackRenderLayer(RenderLayerParent<T, M> parent, boolean creeper) {
        super(parent);
        this.creeper = creeper;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        ItemStack backpack = MobBackpackClientData.get(entity.getId());
        if (backpack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        if (this.getParentModel() instanceof HumanoidModel<?> humanoidModel) {
            humanoidModel.body.translateAndRotate(poseStack);
        } else if (this.getParentModel() instanceof HierarchicalModel<?> hierarchicalModel) {
            ModelPart body = hierarchicalModel.root().getChild("body");
            body.translateAndRotate(poseStack);
        }

        if (this.creeper) {
            poseStack.translate(0.0F, 0.18F, 0.30F);
        } else {
            poseStack.translate(0.0F, 0.22F, 0.20F);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        float scale = entity.isBaby() ? 0.31F : this.creeper ? 0.38F : 0.46F;
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(backpack, ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }
}
