package net.claustra01.clayium.client.renderer.area;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.world.area.AreaBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

final class AreaOutlineRenderer {
    static void render(
            AreaBounds area, BlockPos origin, PoseStack poseStack, MultiBufferSource buffers,
            float red, float green, float blue) {
        double x0 = area.minimum().getX() - origin.getX();
        double y0 = area.minimum().getY() - origin.getY();
        double z0 = area.minimum().getZ() - origin.getZ();
        double x1 = area.maximum().getX() + 1.0D - origin.getX();
        double y1 = area.maximum().getY() + 1.0D - origin.getY();
        double z1 = area.maximum().getZ() + 1.0D - origin.getZ();
        VertexConsumer vertices = buffers.getBuffer(RenderType.lines());
        edge(vertices, poseStack, x0, y0, z0, x1, y0, z0, red, green, blue);
        edge(vertices, poseStack, x1, y0, z0, x1, y0, z1, red, green, blue);
        edge(vertices, poseStack, x1, y0, z1, x0, y0, z1, red, green, blue);
        edge(vertices, poseStack, x0, y0, z1, x0, y0, z0, red, green, blue);
        edge(vertices, poseStack, x0, y1, z0, x1, y1, z0, red, green, blue);
        edge(vertices, poseStack, x1, y1, z0, x1, y1, z1, red, green, blue);
        edge(vertices, poseStack, x1, y1, z1, x0, y1, z1, red, green, blue);
        edge(vertices, poseStack, x0, y1, z1, x0, y1, z0, red, green, blue);
        edge(vertices, poseStack, x0, y0, z0, x0, y1, z0, red, green, blue);
        edge(vertices, poseStack, x1, y0, z0, x1, y1, z0, red, green, blue);
        edge(vertices, poseStack, x1, y0, z1, x1, y1, z1, red, green, blue);
        edge(vertices, poseStack, x0, y0, z1, x0, y1, z1, red, green, blue);
    }

    private static void edge(
            VertexConsumer vertices, PoseStack poseStack,
            double x0, double y0, double z0, double x1, double y1, double z1,
            float red, float green, float blue) {
        double length = Math.sqrt((x1 - x0) * (x1 - x0)
                + (y1 - y0) * (y1 - y0) + (z1 - z0) * (z1 - z0));
        int segments = Math.max(1, (int)Math.ceil(length / 16.0D));
        float normalX = (float)((x1 - x0) / length);
        float normalY = (float)((y1 - y0) / length);
        float normalZ = (float)((z1 - z0) / length);
        for (int segment = 0; segment < segments; segment++) {
            double start = (double)segment / segments;
            double end = (double)(segment + 1) / segments;
            vertices.addVertex(poseStack.last().pose(), (float)(x0 + (x1 - x0) * start),
                            (float)(y0 + (y1 - y0) * start), (float)(z0 + (z1 - z0) * start))
                    .setColor(red, green, blue, 0.9F)
                    .setNormal(poseStack.last(), normalX, normalY, normalZ);
            vertices.addVertex(poseStack.last().pose(), (float)(x0 + (x1 - x0) * end),
                            (float)(y0 + (y1 - y0) * end), (float)(z0 + (z1 - z0) * end))
                    .setColor(red, green, blue, 0.9F)
                    .setNormal(poseStack.last(), normalX, normalY, normalZ);
        }
    }

    private AreaOutlineRenderer() {
    }
}
