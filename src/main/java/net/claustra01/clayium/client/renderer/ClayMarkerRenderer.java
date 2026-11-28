package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.blockentity.ClayMarkerBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Renders 64-block axis guide lines (state 1) and linked blue/red area selection boxes (states 2..4) for Clay Markers.
 */
public class ClayMarkerRenderer implements BlockEntityRenderer<ClayMarkerBlockEntity> {
    public ClayMarkerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            ClayMarkerBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        BlockPos pos = be.getBlockPos();
        if (be.state == 1) {
            renderRangeGuides(poseStack, bufferSource);
            renderRedMarkerBox(poseStack, bufferSource);
        } else if (be.state >= 2 && be.axisAlignedBB != null) {
            int appearance = be.getBoxAppearance();
            if (appearance != 0) {
                AABB relBox = be.axisAlignedBB.move(-pos.getX(), -pos.getY(), -pos.getZ());
                renderBlueSelectionBox(poseStack, bufferSource, relBox);
            }
            renderRedMarkerBox(poseStack, bufferSource);
        }
    }

    static void renderBlueSelectionBox(PoseStack poseStack, MultiBufferSource bufferSource, AABB relBox) {
        VertexConsumer box = bufferSource.getBuffer(RenderType.debugFilledBox());
        LevelRenderer.addChainedFilledBoxVertices(
                poseStack,
                box,
                relBox.minX,
                relBox.minY,
                relBox.minZ,
                relBox.maxX,
                relBox.maxY,
                relBox.maxZ,
                0.1F,
                0.1F,
                0.7F,
                0.3F
        );
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lines, relBox.inflate(0.002), 0.1F, 0.1F, 0.7F, 1.0F);
    }

    private static void renderRedMarkerBox(PoseStack poseStack, MultiBufferSource bufferSource) {
        AABB marker = new AABB(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);
        VertexConsumer box = bufferSource.getBuffer(RenderType.debugFilledBox());
        LevelRenderer.addChainedFilledBoxVertices(
                poseStack, box, marker.minX, marker.minY, marker.minZ, marker.maxX, marker.maxY, marker.maxZ,
                1.0F, 0.0F, 0.0F, 0.3F
        );
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lines, marker.inflate(0.002), 1.0F, 0.0F, 0.0F, 1.0F);
    }

    private static void renderRangeGuides(PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        float[] offs = {-0.1875F, 0.1875F};
        for (float a : offs) {
            for (float b : offs) {
                addLine(lines, mat, pose, -63.0F, 0.5F + a, 0.5F + b, 64.0F, 0.5F + a, 0.5F + b);
                addLine(lines, mat, pose, 0.5F + a, -63.0F, 0.5F + b, 0.5F + a, 64.0F, 0.5F + b);
                addLine(lines, mat, pose, 0.5F + a, 0.5F + b, -63.0F, 0.5F + a, 0.5F + b, 64.0F);
            }
        }
    }

    private static void addLine(
            VertexConsumer vc,
            Matrix4f mat,
            PoseStack.Pose pose,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2) {
        vc.addVertex(mat, x1, y1, z1).setColor(0.2F, 0.7F, 0.3F, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, x2, y2, z2).setColor(0.2F, 0.7F, 0.3F, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(ClayMarkerBlockEntity blockEntity) {
        return blockEntity.state >= 1;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(ClayMarkerBlockEntity blockEntity) {
        if (blockEntity.axisAlignedBB != null) {
            return blockEntity.axisAlignedBB.minmax(new AABB(blockEntity.getBlockPos()));
        }
        return new AABB(blockEntity.getBlockPos()).inflate(64.0);
    }
}
