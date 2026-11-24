package net.claustra01.clayium.client.renderer.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.laser.LaserVisual;
import net.claustra01.clayium.laser.LaserReflectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public final class LaserBlockEntityRenderer<T extends BlockEntity & LaserVisual> implements BlockEntityRenderer<T> {
    public LaserBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            T laser, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        if (laser instanceof LaserReflectorBlockEntity) {
            renderReflector(laser.direction(), poseStack, buffers, packedLight);
        }
        if (!laser.isIrradiating() || laser.visibleBeam().isEmpty() || laser.laserLength() <= 0) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ClayiumMod.id("block/laser"));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        LaserBeam beam = laser.visibleBeam();
        long maximum = Math.max(beam.blue(), Math.max(beam.green(), beam.red()));
        int red = (int)(255L * beam.red() / maximum);
        int green = (int)(255L * beam.green() / maximum);
        int blue = (int)(255L * beam.blue() / maximum);
        double sum = Math.min(beam.blue(), 1_000L) + Math.min(beam.green(), 1_000L) + Math.min(beam.red(), 1_000L);
        float halfWidth = (float)Math.max(0.03125D, (1.0D - 14.0625D / (sum + 14.0D)) * 0.5D)
                + laser.direction().getAxis().ordinal() * 0.0005F;
        long gameTime = laser.getLevel() == null ? 0L : laser.getLevel().getGameTime();
        int alpha = 208 + (int)(31.0D * Math.sin((gameTime + partialTick) * Math.PI / 4.0D));

        Direction direction = laser.direction();
        float startDistance = laser instanceof LaserReflectorBlockEntity ? 0.0F : 0.501F;
        float endDistance = Math.max(startDistance, laser.laserLength() - 0.502F);
        float[] centerStart = point(direction, startDistance);
        float[] centerEnd = point(direction, endDistance);
        Direction.Axis axis = direction.getAxis();
        float[] first = axis == Direction.Axis.Y ? new float[]{1, 0, 0} : new float[]{0, 1, 0};
        float[] second = cross(direction, first);
        PoseStack.Pose pose = poseStack.last();
        int quality = ClayiumConfig.laserQuality();
        int planeAlpha = Math.max(1, Math.min(255, alpha * 8 / quality));
        for (int plane = 0; plane < quality; plane++) {
            double angle = Math.PI * plane / quality;
            float[] radial = {
                    (float)(first[0] * Math.cos(angle) + second[0] * Math.sin(angle)),
                    (float)(first[1] * Math.cos(angle) + second[1] * Math.sin(angle)),
                    (float)(first[2] * Math.cos(angle) + second[2] * Math.sin(angle))};
            float[][] corners = {
                    offset(centerStart, radial, second, -halfWidth, 0.0F),
                    offset(centerStart, radial, second, halfWidth, 0.0F),
                    offset(centerEnd, radial, second, halfWidth, 0.0F),
                    offset(centerEnd, radial, second, -halfWidth, 0.0F)};
            face(vertices, pose, sprite, red, green, blue, planeAlpha, corners, 0, 1, 2, 3);
            face(vertices, pose, sprite, red, green, blue, planeAlpha, corners, 3, 2, 1, 0);
        }
    }

    @Override
    public AABB getRenderBoundingBox(T laser) {
        return new AABB(laser.getBlockPos()).expandTowards(
                laser.direction().getStepX() * laser.laserLength(),
                laser.direction().getStepY() * laser.laserLength(),
                laser.direction().getStepZ() * laser.laserLength()).inflate(0.51D);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    private static void renderReflector(
            Direction direction, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ClayiumMod.id("block/laserreflector"));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        float[] first = direction.getAxis() == Direction.Axis.Y
                ? new float[]{1, 0, 0} : new float[]{0, 1, 0};
        float[] second = cross(direction, first);
        float[] base = point(direction, -0.375F);
        float[][] corners = {
                offset(base, first, second, -0.25F, -0.25F),
                offset(base, first, second, 0.25F, -0.25F),
                offset(base, first, second, 0.25F, 0.25F),
                offset(base, first, second, -0.25F, 0.25F),
                point(direction, 0.375F)};
        PoseStack.Pose pose = poseStack.last();
        reflectorFace(vertices, pose, sprite, packedLight, corners, 0, 3, 2, 1);
        for (int side = 0; side < 4; side++) {
            reflectorFace(vertices, pose, sprite, packedLight, corners,
                    4, side, (side + 1) % 4, (side + 1) % 4);
        }
    }

    private static void reflectorFace(
            VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite,
            int packedLight, float[][] corners, int... indices) {
        float[] a = corners[indices[0]];
        float[] b = corners[indices[1]];
        float[] c = corners[indices[2]];
        float nx = (b[1] - a[1]) * (c[2] - a[2]) - (b[2] - a[2]) * (c[1] - a[1]);
        float ny = (b[2] - a[2]) * (c[0] - a[0]) - (b[0] - a[0]) * (c[2] - a[2]);
        float nz = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
        float length = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        for (int vertex = 0; vertex < indices.length; vertex++) {
            float[] point = corners[indices[vertex]];
            vertices.addVertex(pose.pose(), point[0], point[1], point[2])
                    .setColor(255, 255, 255, 255)
                    .setUv(indices[0] == 4 ? (vertex == 0 ? (sprite.getU0() + sprite.getU1()) * 0.5F
                            : vertex == 1 ? sprite.getU0() : sprite.getU1())
                            : vertex == 0 || vertex == 3 ? sprite.getU0() : sprite.getU1(),
                            vertex < 2 ? sprite.getV1() : sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(pose, nx / length, ny / length, nz / length);
        }
    }

    private static float[] point(Direction direction, float distance) {
        return new float[]{
                0.5F + direction.getStepX() * distance,
                0.5F + direction.getStepY() * distance,
                0.5F + direction.getStepZ() * distance};
    }

    private static float[] cross(Direction direction, float[] vector) {
        float x = direction.getStepY() * vector[2] - direction.getStepZ() * vector[1];
        float y = direction.getStepZ() * vector[0] - direction.getStepX() * vector[2];
        float z = direction.getStepX() * vector[1] - direction.getStepY() * vector[0];
        return new float[]{x, y, z};
    }

    private static float[] offset(
            float[] center, float[] first, float[] second, float firstScale, float secondScale) {
        return new float[]{
                center[0] + first[0] * firstScale + second[0] * secondScale,
                center[1] + first[1] * firstScale + second[1] * secondScale,
                center[2] + first[2] * firstScale + second[2] * secondScale};
    }

    private static void face(
            VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite,
            int red, int green, int blue, int alpha, float[][] corners, int... indices) {
        for (int vertex = 0; vertex < indices.length; vertex++) {
            float[] point = corners[indices[vertex]];
            vertices.addVertex(pose.pose(), point[0], point[1], point[2])
                    .setColor(red, green, blue, alpha)
                    .setUv(vertex == 0 || vertex == 3 ? sprite.getU0() : sprite.getU1(),
                            vertex < 2 ? sprite.getV1() : sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, 0.0F, 1.0F, 0.0F);
        }
    }
}
