package net.claustra01.clayium.client.renderer.environment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlock;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;

public final class QuartzCrucibleRenderer implements BlockEntityRenderer<QuartzCrucibleBlockEntity> {
    public QuartzCrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            QuartzCrucibleBlockEntity crucible, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int amount = crucible.impureSilicon();
        if (amount == 0) {
            return;
        }
        boolean ready = crucible.getBlockState().getValue(
                QuartzCrucibleBlock.STAGE)
                == QuartzCrucibleStage.READY;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ClayiumMod.id("block/" + (ready ? "silicon" : "impuresilicon")));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        float low = 3.01F / 16.0F;
        float high = 12.99F / 16.0F;
        float bottom = 4.01F / 16.0F;
        float top = bottom + amount / (float)QuartzCrucibleBlockEntity.CAPACITY * 10.5F / 16.0F;
        PoseStack.Pose pose = poseStack.last();
        face(vertices, pose, sprite, packedLight, 0, 1, 0,
                low, top, low, low, top, high, high, top, high, high, top, low);
        face(vertices, pose, sprite, packedLight, 0, 0, -1,
                high, bottom, low, low, bottom, low, low, top, low, high, top, low);
        face(vertices, pose, sprite, packedLight, 0, 0, 1,
                low, bottom, high, high, bottom, high, high, top, high, low, top, high);
        face(vertices, pose, sprite, packedLight, -1, 0, 0,
                low, bottom, low, low, bottom, high, low, top, high, low, top, low);
        face(vertices, pose, sprite, packedLight, 1, 0, 0,
                high, bottom, high, high, bottom, low, high, top, low, high, top, high);
    }

    @Override
    public AABB getRenderBoundingBox(QuartzCrucibleBlockEntity crucible) {
        return new AABB(crucible.getBlockPos());
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    private static void face(
            VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite, int packedLight,
            float normalX, float normalY, float normalZ, float... points) {
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 3;
            vertices.addVertex(pose.pose(), points[offset], points[offset + 1], points[offset + 2])
                    .setColor(255, 255, 255, 255)
                    .setUv(sprite.getU(16.0F * (normalX != 0 ? points[offset + 2] : points[offset])),
                            sprite.getV(16.0F * (normalY != 0 ? points[offset + 2]
                                    : 1.0F - points[offset + 1])))
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(pose, normalX, normalY, normalZ);
        }
    }
}
