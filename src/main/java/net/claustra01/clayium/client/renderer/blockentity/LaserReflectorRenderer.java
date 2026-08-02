/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserPath;
import net.claustra01.clayium.world.level.block.LaserReflectorBlock;
import net.claustra01.clayium.world.level.block.entity.LaserReflectorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
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
        ClayLaser laser = reflector.outputLaser();
        ClayLaserBeamRenderer.render(poses, buffers, direction, reflector.beamLength(), laser);
    }

    @Override public boolean shouldRenderOffScreen(LaserReflectorBlockEntity reflector) { return true; }
    @Override public AABB getRenderBoundingBox(LaserReflectorBlockEntity reflector) {
        return new AABB(reflector.getBlockPos()).inflate(ClayLaserPath.MAX_LENGTH);
    }
}
