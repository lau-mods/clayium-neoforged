package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.blockentity.AreaActivatorBlockEntity;
import net.claustra01.clayium.blockentity.AreaMinerBlockEntity;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Renders IO overlays plus area selection boxes for {@link AreaMinerBlockEntity},
 * {@link net.claustra01.clayium.blockentity.AreaCollectorBlockEntity}, and {@link AreaActivatorBlockEntity}.
 */
public class AreaMachineRenderer<T extends ClayContainerBlockEntity> extends ClayContainerRenderer<T> {
    public AreaMachineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            T be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        super.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        int boxState = 0;
        AABB worldBox = null;
        if (be instanceof AreaMinerBlockEntity miner && miner.boxState != 0 && miner.hasAxisAlignedBB()) {
            boxState = miner.boxState;
            worldBox = miner.getAxisAlignedBB();
        } else if (be instanceof AreaActivatorBlockEntity activator && activator.boxState != 0 && activator.hasAxisAlignedBB()) {
            boxState = activator.boxState;
            worldBox = activator.getAxisAlignedBB();
        }
        if (boxState == 0 || worldBox == null) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        AABB relBox = worldBox.move(-pos.getX(), -pos.getY(), -pos.getZ());
        ClayMarkerRenderer.renderBlueSelectionBox(poseStack, bufferSource, relBox);

        VertexConsumer box = bufferSource.getBuffer(RenderType.debugFilledBox());
        LevelRenderer.addChainedFilledBoxVertices(
                poseStack, box, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0F, 0.0F, 0.0F, 0.3F
        );
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lines, new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).inflate(0.002), 1.0F, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        AABB worldBox = null;
        if (blockEntity instanceof AreaMinerBlockEntity miner && miner.hasAxisAlignedBB()) {
            worldBox = miner.getAxisAlignedBB();
        } else if (blockEntity instanceof AreaActivatorBlockEntity activator && activator.hasAxisAlignedBB()) {
            worldBox = activator.getAxisAlignedBB();
        }
        if (worldBox != null) {
            return worldBox.minmax(new AABB(blockEntity.getBlockPos()));
        }
        return super.getRenderBoundingBox(blockEntity);
    }
}
