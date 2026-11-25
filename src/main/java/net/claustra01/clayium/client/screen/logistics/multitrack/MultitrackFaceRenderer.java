package net.claustra01.clayium.client.screen.logistics.multitrack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.claustra01.clayium.client.renderer.io.IoDeviceRenderer;

public final class MultitrackFaceRenderer implements BlockEntityRenderer<MultitrackBufferBlockEntity> {
    public MultitrackFaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MultitrackBufferBlockEntity buffer, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        if (buffer.pipeEnabled()) {
            IoDeviceRenderer.renderDevice(buffer, poseStack, buffers, light);
            return;
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        for (Direction direction : Direction.values()) {
            int track = buffer.faceTrack(direction);
            if (track >= buffer.trackCount()) continue;
            BufferBlockEntity.FaceMode mode = buffer.track(track).faceMode(direction);
            if (mode == BufferBlockEntity.FaceMode.DISABLED) continue;
            String texture = (mode == BufferBlockEntity.FaceMode.INPUT ? "import_m" : "export_m") + track;
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(ClayiumMod.id("block/" + texture));
            float low = -0.002F;
            float high = 1.002F;
            switch (direction) {
                case NORTH -> face(vertices, pose, sprite, light, direction,
                        low, low, low, high, low, low, high, high, low, low, high, low);
                case SOUTH -> face(vertices, pose, sprite, light, direction,
                        high, low, high, low, low, high, low, high, high, high, high, high);
                case WEST -> face(vertices, pose, sprite, light, direction,
                        low, low, high, low, low, low, low, high, low, low, high, high);
                case EAST -> face(vertices, pose, sprite, light, direction,
                        high, low, low, high, low, high, high, high, high, high, high, low);
                case UP -> face(vertices, pose, sprite, light, direction,
                        low, high, low, high, high, low, high, high, high, low, high, high);
                case DOWN -> face(vertices, pose, sprite, light, direction,
                        low, low, high, high, low, high, high, low, low, low, low, low);
            }
        }
    }

    private static void face(VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite, int light,
                             Direction direction, float... points) {
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 3;
            vertices.addVertex(pose.pose(), points[offset], points[offset + 1], points[offset + 2])
                    .setColor(255, 255, 255, 255)
                    .setUv(sprite.getU(vertex == 0 || vertex == 3 ? 0F : 1F),
                            sprite.getV(vertex < 2 ? 1F : 0F))
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
        }
    }
}
