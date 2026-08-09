/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.client.renderer.blockentity.MetalChestRenderer;
import net.claustra01.clayium.world.level.block.MetalChestBlock;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MetalChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private MetalChestRenderer renderer;

    public MetalChestItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof MetalChestBlock chest) {
            if (renderer == null) {
                renderer = new MetalChestRenderer(net.minecraft.client.Minecraft.getInstance().getEntityModels());
            }
            renderer.renderItem(chest.definition(), poseStack, buffers, packedLight, packedOverlay);
        }
    }
}
