package net.claustra01.clayium.client.renderer.careactor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;

public final class CaReactorCoreRenderer implements BlockEntityRenderer<CaReactorCoreBlockEntity> {
    public CaReactorCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            CaReactorCoreBlockEntity core, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!ClayiumConfig.CA_REACTOR_GLITTERING.get()
                || !core.getBlockState().getValue(CaReactorCoreBlock.FORMED)) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ClayiumMod.id("block/careactorcore_1"));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        boolean active = core.state() == ClayMachineBlockEntity.MachineState.RUNNING;
        long gameTime = core.getLevel() == null ? 0L : core.getLevel().getGameTime();
        int alpha = active
                ? 180 + (int)(55.0D * Math.sin((gameTime + partialTick) * Math.PI / 6.0D))
                : 96;
        float low = -0.003F;
        float high = 1.003F;
        face(vertices, poseStack.last(), sprite, alpha, 0, 0, -1,
                low, low, low, high, low, low, high, high, low, low, high, low);
        face(vertices, poseStack.last(), sprite, alpha, 0, 0, 1,
                high, low, high, low, low, high, low, high, high, high, high, high);
        face(vertices, poseStack.last(), sprite, alpha, -1, 0, 0,
                low, low, high, low, low, low, low, high, low, low, high, high);
        face(vertices, poseStack.last(), sprite, alpha, 1, 0, 0,
                high, low, low, high, low, high, high, high, high, high, high, low);
        face(vertices, poseStack.last(), sprite, alpha, 0, -1, 0,
                low, low, high, high, low, high, high, low, low, low, low, low);
        face(vertices, poseStack.last(), sprite, alpha, 0, 1, 0,
                low, high, low, high, high, low, high, high, high, low, high, high);
    }

    @Override
    public AABB getRenderBoundingBox(CaReactorCoreBlockEntity core) {
        return new AABB(core.getBlockPos()).inflate(0.01D);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    private static void face(
            VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite,
            int alpha, float normalX, float normalY, float normalZ, float... points) {
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 3;
            vertices.addVertex(pose.pose(), points[offset], points[offset + 1], points[offset + 2])
                    .setColor(255, 255, 255, alpha)
                    .setUv(vertex == 0 || vertex == 3 ? sprite.getU0() : sprite.getU1(),
                            vertex < 2 ? sprite.getV1() : sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, normalX, normalY, normalZ);
        }
    }
}
