package net.claustra01.clayium.client.screen.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.storage.MetalChestBlock;
import net.claustra01.clayium.storage.MetalChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MetalChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final MetalChestItemRenderer INSTANCE = new MetalChestItemRenderer();

    private MetalChestItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffers, int light, int overlay) {
        MetalChestBlock block = (MetalChestBlock)((BlockItem)stack.getItem()).getBlock();
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(
                new MetalChestBlockEntity(BlockPos.ZERO, block.defaultBlockState()), poseStack, buffers, light, overlay);
    }
}
