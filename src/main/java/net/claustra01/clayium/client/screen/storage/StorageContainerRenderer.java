package net.claustra01.clayium.client.screen.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.claustra01.clayium.storage.StorageContainerBlock;
import net.claustra01.clayium.storage.StorageContainerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class StorageContainerRenderer implements BlockEntityRenderer<StorageContainerBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final Font font;

    public StorageContainerRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
        font = context.getFont();
    }

    @Override
    public void render(
            StorageContainerBlockEntity storage, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack stored = storage.storedItem();
        if (stored.isEmpty() || storage.amount() == 0L) {
            return;
        }
        float rotation = 180.0F - storage.getBlockState().getValue(StorageContainerBlock.FACING).toYRot();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.62F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0.0F, 0.0F, -0.506F);
        poseStack.scale(0.42F, 0.42F, 0.42F);
        itemRenderer.renderStatic(
                stored, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffers, storage.getLevel(), 0);
        poseStack.popPose();

        String count = storage.amount() + "/" + storage.capacity();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.25F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0.0F, 0.0F, -0.508F);
        poseStack.scale(-0.010F, -0.010F, 0.010F);
        font.drawInBatch(
                count, -font.width(count) / 2.0F, 0.0F, 0xFFF0F0F0, false,
                poseStack.last().pose(), buffers, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(StorageContainerBlockEntity storage) {
        return new AABB(storage.getBlockPos());
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
