package net.claustra01.clayium.client.screen.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.storage.MetalChestBlock;
import net.claustra01.clayium.storage.MetalChestBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class MetalChestRenderer implements BlockEntityRenderer<MetalChestBlockEntity> {
    private static final RenderType[] LAYERS = {
            RenderType.entityCutoutNoCull(ClayiumMod.id("textures/entity/metalchest/base.png")),
            overlayType("metal_chest_dark", ClayiumMod.id("textures/entity/metalchest/dark.png"), -0.1F, -1.0F),
            overlayType("metal_chest_light", ClayiumMod.id("textures/entity/metalchest/light.png"), -0.2F, -2.0F)};
    private final ModelPart lid;
    private final ModelPart lock;
    private final ModelPart bottom;

    public MetalChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.CHEST);
        lid = root.getChild("lid");
        lock = root.getChild("lock");
        bottom = root.getChild("bottom");
    }

    @Override
    public void render(MetalChestBlockEntity chest, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-chest.getBlockState().getValue(MetalChestBlock.FACING).toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        float openness = chest.openness(partialTick);
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;
        lid.xRot = -(openness * ((float)Math.PI / 2.0F));
        lock.xRot = lid.xRot;
        for (int layer = 0; layer < LAYERS.length; layer++) {
            VertexConsumer consumer = buffers.getBuffer(LAYERS[layer]);
            int color = chest.type().material().color(layer);
            lid.render(poseStack, consumer, packedLight, packedOverlay, color);
            lock.render(poseStack, consumer, packedLight, packedOverlay, color);
            bottom.render(poseStack, consumer, packedLight, packedOverlay, color);
        }
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MetalChestBlockEntity chest) {
        return new AABB(chest.getBlockPos());
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
    /** Mirrors the two distinct polygon offsets used by the original Metal Chest renderer. */
    private static RenderType overlayType(String name, ResourceLocation texture, float factor, float units) {
        RenderStateShard.LayeringStateShard layering = new RenderStateShard.LayeringStateShard(
                name + "_polygon_offset",
                () -> {
                    RenderSystem.polygonOffset(factor, units);
                    RenderSystem.enablePolygonOffset();
                },
                () -> {
                    RenderSystem.polygonOffset(0.0F, 0.0F);
                    RenderSystem.disablePolygonOffset();
                });
        return RenderType.create(
                ClayiumMod.MOD_ID + ":" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setLayeringState(layering)
                        .createCompositeState(false));
    }
}
