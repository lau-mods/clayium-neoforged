package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.HorizontalClayContainerBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 3-pass tinted chest model renderer with smooth lid open/close animation matching 1.7.10 {@code MetalChestRenderer}.
 */
public class MetalChestRenderer implements BlockEntityRenderer<MetalChestBlockEntity> {
    private static final ResourceLocation TEX_BASE = ClayiumMod.id("textures/entity/metalchest/base.png");
    private static final ResourceLocation TEX_DARK = ClayiumMod.id("textures/entity/metalchest/dark.png");
    private static final ResourceLocation TEX_LIGHT = ClayiumMod.id("textures/entity/metalchest/light.png");

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public MetalChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart chestRoot = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = chestRoot.getChild("bottom");
        this.lid = chestRoot.getChild("lid");
        this.lock = chestRoot.getChild("lock");
    }

    @Override
    public void render(
            MetalChestBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        BlockState state = be.getBlockState();
        CMaterial material = state.getBlock() instanceof MetalChestBlock chest ? chest.getMaterial() : CMaterials.ALUMINIUM;
        Direction facing = state.hasProperty(HorizontalClayContainerBlock.FACING)
                ? state.getValue(HorizontalClayContainerBlock.FACING)
                : Direction.SOUTH;

        float openness = be.prevLidAngle + (be.lidAngle - be.prevLidAngle) * partialTick;
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;
        float lidXRot = -(openness * ((float) Math.PI / 2.0F));
        this.lid.xRot = lidXRot;
        this.lock.xRot = lidXRot;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        renderChestParts(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEX_BASE)), packedLight, packedOverlay, material.opaqueArgbForLayer(0));
        renderChestParts(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEX_DARK)), packedLight, packedOverlay, material.opaqueArgbForLayer(1));
        renderChestParts(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEX_LIGHT)), packedLight, packedOverlay, material.opaqueArgbForLayer(2));

        poseStack.popPose();
    }

    private void renderChestParts(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int argb) {
        this.lid.render(poseStack, consumer, light, overlay, argb);
        this.lock.render(poseStack, consumer, light, overlay, argb);
        this.bottom.render(poseStack, consumer, light, overlay, argb);
    }
}
