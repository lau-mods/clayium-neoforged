package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer for {@link RedstoneInterfaceBlockEntity} displaying targeted link overlays.
 */
public class SynchronizedInterfaceRenderer implements BlockEntityRenderer<RedstoneInterfaceBlockEntity> {
    public SynchronizedInterfaceRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            RedstoneInterfaceBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        InterfaceOverlayHelper.renderIfTargeted(be, be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(RedstoneInterfaceBlockEntity blockEntity) {
        return blockEntity.isSynced();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
