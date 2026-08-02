/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserPath;
import net.claustra01.clayium.world.level.block.LaserReflectorBlock;
import net.claustra01.clayium.world.level.block.entity.LaserReflectorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** Renders the merged beam emitted by the original-style laser reflector. */
public final class LaserReflectorRenderer implements BlockEntityRenderer<LaserReflectorBlockEntity> {
    @Override
    public void render(
            LaserReflectorBlockEntity reflector,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (!reflector.irradiating() || reflector.beamLength() <= 0) return;
        Direction direction = reflector.getBlockState().getValue(LaserReflectorBlock.FACING);
        double radius = 0.035D;
        double sx = .5D + direction.getStepX() * .5D;
        double sy = .5D + direction.getStepY() * .5D;
        double sz = .5D + direction.getStepZ() * .5D;
        double ex = .5D + direction.getStepX() * reflector.beamLength();
        double ey = .5D + direction.getStepY() * reflector.beamLength();
        double ez = .5D + direction.getStepZ() * reflector.beamLength();
        AABB box = new AABB(
                Math.min(sx, ex) - radius,
                Math.min(sy, ey) - radius,
                Math.min(sz, ez) - radius,
                Math.max(sx, ex) + radius,
                Math.max(sy, ey) + radius,
                Math.max(sz, ez) + radius);
        ClayLaser laser = reflector.outputLaser();
        float red = laser.red() > 0 ? 1.0F : 0.15F;
        float green = laser.green() > 0 ? 1.0F : 0.15F;
        float blue = laser.blue() > 0 ? 1.0F : 0.15F;
        LevelRenderer.renderLineBox(
                poses, buffers.getBuffer(RenderType.lines()), box, red, green, blue, .9F);
    }

    @Override public boolean shouldRenderOffScreen(LaserReflectorBlockEntity reflector) { return true; }
    @Override public AABB getRenderBoundingBox(LaserReflectorBlockEntity reflector) {
        return new AABB(reflector.getBlockPos()).inflate(ClayLaserPath.MAX_LENGTH);
    }
}
