/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.laser.ClayLaser;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/** Original-style translucent Clay Laser beam with bundle-dependent thickness. */
final class ClayLaserBeamRenderer {
    private static final ResourceLocation TEXTURE = Clayium.id("textures/block/laser.png");
    private static final int PLANES = 8;

    private ClayLaserBeamRenderer() {}

    static void render(
            PoseStack poses,
            MultiBufferSource buffers,
            Direction direction,
            int length,
            ClayLaser laser) {
        int strands = laser.blue() + laser.green() + laser.red();
        if (length <= 0 || strands <= 0) return;

        float diameter = 1.0F - 14.0625F / (strands + 14.0F);
        float radius = diameter * 0.5F;
        Vector3f axis = new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        Vector3f first = direction.getAxis() == Direction.Axis.Y
                ? new Vector3f(1, 0, 0)
                : new Vector3f(0, 1, 0);
        Vector3f second = new Vector3f(axis).cross(first).normalize();

        float startX = .5F + direction.getStepX() * .5F;
        float startY = .5F + direction.getStepY() * .5F;
        float startZ = .5F + direction.getStepZ() * .5F;
        float endX = .5F + direction.getStepX() * length;
        float endY = .5F + direction.getStepY() * length;
        float endZ = .5F + direction.getStepZ() * length;

        int maximum = Math.max(laser.blue(), Math.max(laser.green(), laser.red()));
        int red = 255 * laser.red() / maximum;
        int green = 255 * laser.green() / maximum;
        int blue = 255 * laser.blue() / maximum;
        int alpha = Math.round(26.0F + diameter * 26.0F);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));

        for (int index = 0; index < PLANES; index++) {
            float angle = (float)Math.PI * index / PLANES;
            Vector3f transverse = new Vector3f(first).mul((float)Math.cos(angle))
                    .add(new Vector3f(second).mul((float)Math.sin(angle)))
                    .mul(radius);
            quad(poses, consumer, red, green, blue, alpha,
                    startX + transverse.x, startY + transverse.y, startZ + transverse.z,
                    startX - transverse.x, startY - transverse.y, startZ - transverse.z,
                    endX - transverse.x, endY - transverse.y, endZ - transverse.z,
                    endX + transverse.x, endY + transverse.y, endZ + transverse.z);
        }
    }

    private static void quad(
            PoseStack poses, VertexConsumer consumer,
            int red, int green, int blue, int alpha,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3) {
        vertex(poses, consumer, red, green, blue, alpha, x0, y0, z0, 0, 1);
        vertex(poses, consumer, red, green, blue, alpha, x1, y1, z1, 1, 1);
        vertex(poses, consumer, red, green, blue, alpha, x2, y2, z2, 1, 0);
        vertex(poses, consumer, red, green, blue, alpha, x3, y3, z3, 0, 0);
    }

    private static void vertex(
            PoseStack poses, VertexConsumer consumer,
            int red, int green, int blue, int alpha,
            float x, float y, float z, float u, float v) {
        consumer.addVertex(poses.last().pose(), x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(poses.last(), 0, 1, 0);
    }
}
