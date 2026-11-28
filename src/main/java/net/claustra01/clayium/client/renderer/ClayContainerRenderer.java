package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.blockentity.ClayContainerIoIcons;
import net.claustra01.clayium.blockentity.ISynchronizedInterface;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * Renders 6-face IO route & filter overlays on {@link ClayContainerBlockEntity} blocks
 * (switching to pipe-mode {@code _p} overlays when holding Clayium configuration tools),
 * as well as targeted {@link ISynchronizedInterface} highlights.
 */
public class ClayContainerRenderer<T extends ClayContainerBlockEntity> implements BlockEntityRenderer<T> {
    private static final float OFFSET = 0.002F;

    public ClayContainerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            T be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) {
            return;
        }
        BlockState state = be.getBlockState();
        boolean isPipe = state.hasProperty(ClayContainerBlock.PIPE_MODE) && state.getValue(ClayContainerBlock.PIPE_MODE);
        boolean holdingConfigTool = isLocalPlayerHoldingConfigTool(be);

        if (!isPipe || holdingConfigTool) {
            renderIoOverlays(be, level, isPipe && holdingConfigTool, poseStack, bufferSource, packedOverlay);
        }

        if (be instanceof ISynchronizedInterface iface) {
            InterfaceOverlayHelper.renderIfTargeted(be, iface, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    protected static boolean isLocalPlayerHoldingConfigTool(ClayContainerBlockEntity be) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        ItemStack main = player.getMainHandItem();
        if (be.getItemUseMode(main, player) >= 0) {
            return true;
        }
        return be.getItemUseMode(player.getOffhandItem(), player) >= 0;
    }

    protected void renderIoOverlays(
            ClayContainerBlockEntity be,
            Level level,
            boolean pipeMode,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedOverlay) {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());

        for (Direction face : Direction.values()) {
            RelativeSide rel = be.toRelativeSide(face);
            String insertIcon = ClayContainerIoIcons.getInsertIconName(be, rel, pipeMode);
            String extractIcon = ClayContainerIoIcons.getExtractIconName(be, rel, pipeMode);
            String filterIcon = ClayContainerIoIcons.getFilterIconName(be, rel);
            if (insertIcon == null && extractIcon == null && filterIcon == null) {
                continue;
            }
            int faceLight = LevelRenderer.getLightColor(level, be.getBlockPos().relative(face));
            if (insertIcon != null) {
                TextureAtlasSprite sprite = atlas.apply(blockSprite(insertIcon));
                drawFaceQuad(poseStack, consumer, face, sprite, OFFSET, faceLight, packedOverlay);
            }
            if (extractIcon != null) {
                TextureAtlasSprite sprite = atlas.apply(blockSprite(extractIcon));
                drawFaceQuad(poseStack, consumer, face, sprite, OFFSET * 1.5F, faceLight, packedOverlay);
            }
            if (filterIcon != null) {
                TextureAtlasSprite sprite = atlas.apply(blockSprite(filterIcon));
                drawFaceQuad(poseStack, consumer, face, sprite, OFFSET * 2.0F, faceLight, packedOverlay);
            }
        }
    }

    @Nullable
    private static ResourceLocation blockSprite(String stem) {
        return ClayiumMod.id("block/" + stem);
    }

    protected static void drawFaceQuad(
            PoseStack poseStack,
            VertexConsumer consumer,
            Direction face,
            TextureAtlasSprite sprite,
            float push,
            int light,
            int overlay) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();

        switch (face) {
            case NORTH -> {
                float z = -push;
                vertex(consumer, mat, pose, 1.0F, 1.0F, z, u0, v0, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, 0.0F, z, u0, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 0.0F, 0.0F, z, u1, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 0.0F, 1.0F, z, u1, v0, light, overlay, nx, ny, nz);
            }
            case SOUTH -> {
                float z = 1.0F + push;
                vertex(consumer, mat, pose, 0.0F, 1.0F, z, u0, v0, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 0.0F, 0.0F, z, u0, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, 0.0F, z, u1, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, 1.0F, z, u1, v0, light, overlay, nx, ny, nz);
            }
            case WEST -> {
                float x = -push;
                vertex(consumer, mat, pose, x, 1.0F, 0.0F, u0, v0, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, x, 0.0F, 0.0F, u0, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, x, 0.0F, 1.0F, u1, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, x, 1.0F, 1.0F, u1, v0, light, overlay, nx, ny, nz);
            }
            case EAST -> {
                float x = 1.0F + push;
                vertex(consumer, mat, pose, x, 1.0F, 1.0F, u0, v0, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, x, 0.0F, 1.0F, u0, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, x, 0.0F, 0.0F, u1, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, x, 1.0F, 0.0F, u1, v0, light, overlay, nx, ny, nz);
            }
            case UP -> {
                float y = 1.0F + push;
                vertex(consumer, mat, pose, 0.0F, y, 0.0F, u0, v0, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 0.0F, y, 1.0F, u0, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, y, 1.0F, u1, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, y, 0.0F, u1, v0, light, overlay, nx, ny, nz);
            }
            case DOWN -> {
                float y = -push;
                vertex(consumer, mat, pose, 0.0F, y, 1.0F, u0, v0, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 0.0F, y, 0.0F, u0, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, y, 0.0F, u1, v1, light, overlay, nx, ny, nz);
                vertex(consumer, mat, pose, 1.0F, y, 1.0F, u1, v0, light, overlay, nx, ny, nz);
            }
        }
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f mat,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            int overlay,
            float nx,
            float ny,
            float nz) {
        consumer.addVertex(mat, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return blockEntity instanceof ISynchronizedInterface iface && iface.isSynced();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
