package net.claustra01.clayium.client.renderer.area;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.world.area.AreaBounds;
import net.claustra01.clayium.world.area.AreaMarkerBlockEntity;
import net.claustra01.clayium.world.area.AreaMarkerType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public final class AreaMarkerRenderer implements BlockEntityRenderer<AreaMarkerBlockEntity> {
    public AreaMarkerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            AreaMarkerBlockEntity marker, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        AreaMarkerType type = marker.markerType();
        AreaOutlineRenderer.render(
                marker.selectedArea(), marker.getBlockPos(), poseStack, buffers,
                type.red(), type.green(), type.blue());
    }

    @Override
    public AABB getRenderBoundingBox(AreaMarkerBlockEntity marker) {
        AreaBounds area = marker.selectedArea();
        return area.box().inflate(0.25D);
    }

    @Override
    public boolean shouldRenderOffScreen(AreaMarkerBlockEntity marker) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
