package net.claustra01.clayium.client.renderer.area;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public final class AreaMachineRenderer implements BlockEntityRenderer<AreaMachineBlockEntity> {
    public AreaMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            AreaMachineBlockEntity machine, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        // Original UtilRender.renderAxisAlignedBB: box appearance 0 hides the area.
        if (machine.boxState() == 0) {
            return;
        }
        AreaOutlineRenderer.render(
                machine.workArea(), machine.getBlockPos(), poseStack, buffers, 1.0F, 0.85F, 0.1F);
    }

    @Override
    public AABB getRenderBoundingBox(AreaMachineBlockEntity machine) {
        return machine.workArea().box().inflate(0.25D);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
