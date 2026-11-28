package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Renders Storage Container & Vacuum Container front item display, stack count, hover name, and IO overlays.
 */
public class StorageContainerRenderer extends ClayContainerRenderer<StorageContainerBlockEntity> {
    private final ItemRenderer itemRenderer;

    public StorageContainerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            StorageContainerBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        super.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        ItemStack displayStack = be.isVacuumContainer() ? be.getFilterTemplate() : be.getStoredTemplate();
        if (displayStack.isEmpty() || be.getLevel() == null) {
            return;
        }
        Direction front = be.getFrontDirection();
        if (!front.getAxis().isHorizontal()) {
            front = Direction.NORTH;
        }
        int frontLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(front));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        float yRot = switch (front) {
            case NORTH -> 180.0F;
            case SOUTH -> 0.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        poseStack.pushPose();
        poseStack.translate(0.0F, -0.02F, 0.51F);
        poseStack.scale(0.42F, 0.42F, 0.02F);
        this.itemRenderer.renderStatic(
                displayStack.copyWithCount(1),
                ItemDisplayContext.FIXED,
                frontLight,
                packedOverlay,
                poseStack,
                bufferSource,
                be.getLevel(),
                0
        );
        poseStack.popPose();

        Font font = Minecraft.getInstance().font;
        if (!be.isVacuumContainer()) {
            String countText = ClayiumFormatting.formatStackSize(be.getStoredCount());
            poseStack.pushPose();
            poseStack.translate(0.0F, -0.25F, 0.52F);
            poseStack.scale(-0.022F, -0.022F, 0.022F);
            float cx = -font.width(countText) / 2.0F;
            font.drawInBatch(
                    countText,
                    cx,
                    0.0F,
                    0xDCFFFFFF,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    frontLight
            );
            poseStack.popPose();
        }

        HitResult hit = Minecraft.getInstance().hitResult;
        boolean targeted = hit instanceof BlockHitResult bhr
                && bhr.getType() == HitResult.Type.BLOCK
                && be.getBlockPos().equals(bhr.getBlockPos());
        if (targeted || isLocalPlayerHoldingConfigTool(be)) {
            String name = displayStack.getHoverName().getString();
            int width = font.width(name);
            float scaleFit = width > 64 ? 64.0F / (float) width : 1.0F;
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.34F, 0.52F);
            poseStack.scale(-0.014F * scaleFit, -0.014F, 0.014F);
            font.drawInBatch(
                    name,
                    -width / 2.0F,
                    0.0F,
                    0xFFFFFFFF,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0x80000000,
                    frontLight
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
