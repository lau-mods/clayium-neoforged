/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.laser.ClayLaserPath;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class ClayEnergyLaserRenderer implements BlockEntityRenderer<ClayEnergyLaserBlockEntity> {
    private final IoOverlayRenderer<ClayEnergyLaserBlockEntity> overlays = new IoOverlayRenderer<>();
    @Override public void render(ClayEnergyLaserBlockEntity laser, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        overlays.render(laser, partialTick, poses, buffers, light, overlay);
        if (!laser.irradiating() || laser.beamLength() <= 0) return;
        Direction direction = laser.getBlockState().getValue(AbstractTieredIoMachineBlock.FACING);
        ClayLaserBeamRenderer.render(poses, buffers, direction, laser.beamLength(), laser.outputLaser());
    }
    @Override public boolean shouldRenderOffScreen(ClayEnergyLaserBlockEntity laser) { return true; }
    @Override public AABB getRenderBoundingBox(ClayEnergyLaserBlockEntity laser) {
        return new AABB(laser.getBlockPos()).inflate(ClayLaserPath.MAX_LENGTH);
    }
}
