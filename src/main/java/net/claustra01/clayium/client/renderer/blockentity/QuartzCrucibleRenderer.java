/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.world.level.block.entity.QuartzCrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.Blocks;

/** Recreates the original visible silicon level inside the open quartz vessel. */
public final class QuartzCrucibleRenderer implements BlockEntityRenderer<QuartzCrucibleBlockEntity> {
    @Override public void render(QuartzCrucibleBlockEntity crucible,float partialTick,PoseStack poses,
                                 MultiBufferSource buffers,int light,int overlay) {
        int count=crucible.ingotCount();if(count<=0)return;
        poses.pushPose();
        poses.translate(1.0D/16.0D,1.0D/16.0D,1.0D/16.0D);
        poses.scale(14.0F/16.0F,count/16.0F,14.0F/16.0F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.IRON_BLOCK.defaultBlockState(),poses,buffers,light,overlay);
        poses.popPose();
    }
}
