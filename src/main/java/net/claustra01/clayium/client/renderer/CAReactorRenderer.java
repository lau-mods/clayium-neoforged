package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.blockentity.CAReactorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

/**
 * Renders CA Reactor IO overlays plus layered golden energy glow over hull coordinates when active.
 */
public class CAReactorRenderer extends ClayContainerRenderer<CAReactorBlockEntity> {
    public CAReactorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            CAReactorBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        super.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        if (!ClayiumConfig.CA_REACTOR_GLITTERING.get() || !be.isDoingWork() || be.hullCoords == null) {
            return;
        }
        VertexConsumer boxConsumer = bufferSource.getBuffer(RenderType.debugFilledBox());
        for (int j = 1; (double) j < be.reactorRank + 1.01; j++) {
            for (int[] coord : be.hullCoords) {
                if (coord.length < 3) {
                    continue;
                }
                BlockPos rel = be.getRelativePos(coord[0], coord[1], coord[2]).subtract(be.getBlockPos());
                float i = (float) be.reactorRank + 1.0F - (float) j;
                float r = (float) j / ((float) be.reactorRank + 1.0F);
                float f = 0.01F * ((float) Math.pow(i, 1.6) + 1.0F);
                float blue = 0.3F + 0.05F * (2.0F * r - r * r) * (float) j;
                LevelRenderer.addChainedFilledBoxVertices(
                        poseStack,
                        boxConsumer,
                        (double) rel.getX() - f,
                        (double) rel.getY() - f,
                        (double) rel.getZ() - f,
                        (double) rel.getX() + 1.0 + f,
                        (double) rel.getY() + 1.0 + f,
                        (double) rel.getZ() + 1.0 + f,
                        1.0F,
                        1.0F,
                        blue,
                        0.11F
                );
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(CAReactorBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
