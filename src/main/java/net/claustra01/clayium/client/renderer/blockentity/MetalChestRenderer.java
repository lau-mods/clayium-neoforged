/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.storage.MetalStorageCatalog;
import net.claustra01.clayium.world.level.block.MetalChestBlock;
import net.claustra01.clayium.world.level.block.entity.MetalChestBlockEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Original three-layer Metal Chest texture rendered on the vanilla single-chest model. */
public final class MetalChestRenderer implements BlockEntityRenderer<MetalChestBlockEntity> {
    private static final ResourceLocation BASE = Clayium.id("textures/entity/metalchest/base.png");
    private static final ResourceLocation DARK = Clayium.id("textures/entity/metalchest/dark.png");
    private static final ResourceLocation LIGHT = Clayium.id("textures/entity/metalchest/light.png");

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public MetalChestRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getModelSet());
    }

    public MetalChestRenderer(EntityModelSet models) {
        ModelPart root = models.bakeLayer(ModelLayers.CHEST);
        lid = root.getChild("lid");
        bottom = root.getChild("bottom");
        lock = root.getChild("lock");
    }

    @Override
    public void render(MetalChestBlockEntity chest, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Direction facing = chest.getBlockState().getValue(MetalChestBlock.FACING);
        renderChest(chest.definition(), facing, chest.getOpenNess(partialTick), poseStack,
                buffers, packedLight, packedOverlay);
    }

    public void renderItem(MetalStorageCatalog.Chest definition, PoseStack poseStack,
                           MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderChest(definition, Direction.SOUTH, 0.0F, poseStack, buffers, packedLight, packedOverlay);
    }

    private void renderChest(MetalStorageCatalog.Chest definition, Direction facing, float openness,
                             PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        float eased = 1.0F - openness;
        eased = 1.0F - eased * eased * eased;
        lid.xRot = -(eased * (float)Math.PI / 2.0F);
        lock.xRot = lid.xRot;

        MetalStorageCatalog.ChestColors colors = definition.colors();
        renderLayer(poseStack, buffers, BASE, colors.base(), packedLight, packedOverlay);
        renderLayer(poseStack, buffers, DARK, colors.dark(), packedLight, packedOverlay);
        renderLayer(poseStack, buffers, LIGHT, colors.light(), packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderLayer(PoseStack poseStack, MultiBufferSource buffers, ResourceLocation texture,
                             int color, int packedLight, int packedOverlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(texture));
        lid.render(poseStack, consumer, packedLight, packedOverlay, color);
        lock.render(poseStack, consumer, packedLight, packedOverlay, color);
        bottom.render(poseStack, consumer, packedLight, packedOverlay, color);
    }
}
