/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.claustra01.clayium.world.level.block.entity.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.laser.ClayLaserPath;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
        double radius = 0.035D;
        double sx = .5D + direction.getStepX() * .5D;
        double sy = .5D + direction.getStepY() * .5D;
        double sz = .5D + direction.getStepZ() * .5D;
        double ex = .5D + direction.getStepX() * laser.beamLength();
        double ey = .5D + direction.getStepY() * laser.beamLength();
        double ez = .5D + direction.getStepZ() * laser.beamLength();
        AABB box = new AABB(Math.min(sx, ex) - radius, Math.min(sy, ey) - radius, Math.min(sz, ez) - radius,
                Math.max(sx, ex) + radius, Math.max(sy, ey) + radius, Math.max(sz, ez) + radius);
        int tier = laser.tierIndex();
        float red = tier == 9 || tier >= 10 ? 1F : .15F;
        float green = tier == 8 || tier >= 10 ? 1F : .2F;
        float blue = tier == 7 || tier >= 10 ? 1F : .2F;
        LevelRenderer.renderLineBox(poses, buffers.getBuffer(RenderType.lines()), box, red, green, blue, .9F);
    }
    @Override public boolean shouldRenderOffScreen(ClayEnergyLaserBlockEntity laser) { return true; }
    @Override public AABB getRenderBoundingBox(ClayEnergyLaserBlockEntity laser) {
        return new AABB(laser.getBlockPos()).inflate(ClayLaserPath.MAX_LENGTH);
    }
}
