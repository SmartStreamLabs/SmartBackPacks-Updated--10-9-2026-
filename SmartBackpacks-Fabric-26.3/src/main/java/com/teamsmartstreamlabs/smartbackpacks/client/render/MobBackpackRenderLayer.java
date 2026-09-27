package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class MobBackpackRenderLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {
    private final boolean creeper;
    private final ItemStackRenderState backpackRenderState = new ItemStackRenderState();

    public MobBackpackRenderLayer(RenderLayerParent<S, M> parent, boolean creeper) {
        super(parent);
        this.creeper = creeper;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
            S renderState, float yRot, float xRot) {
        ItemStack backpack = ((MobBackpackRenderStateAccess) renderState).smartbackpacks$getMobBackpack();
        if (backpack.isEmpty()) return;

        poseStack.pushPose();
        if (this.getParentModel() instanceof HumanoidModel<?> humanoidModel) {
            humanoidModel.body.translateAndRotate(poseStack);
        } else {
            this.getParentModel().root().getChild("body").translateAndRotate(poseStack);
        }
        poseStack.translate(0.0F, this.creeper ? 0.18F : 0.22F, this.creeper ? 0.30F : 0.20F);
        poseStack.mulPose(new Matrix4f().rotate(Axis.YP.rotationDegrees(180.0F)));
        poseStack.mulPose(new Matrix4f().rotate(Axis.ZP.rotationDegrees(180.0F)));
        float scale = renderState.isBaby ? 0.31F : this.creeper ? 0.38F : 0.46F;
        poseStack.scale(scale, scale, scale);
        this.backpackRenderState.clear();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(this.backpackRenderState, backpack,
                ItemDisplayContext.FIXED, minecraft.level, null, 0);
        this.backpackRenderState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
