package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.blockentity.ISynchronizedInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

/**
 * Shared client highlight/overlay renderer matching 1.7.10 {@code InterfaceRenderer} when the player looks at a synced interface.
 */
public final class InterfaceOverlayHelper {
    private InterfaceOverlayHelper() {}

    public static void renderIfTargeted(
            BlockEntity be,
            ISynchronizedInterface iface,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (!iface.isSynced() || !iface.acceptsCoordChanger()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = be.getLevel();
        if (player == null || level == null) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        if (!pos.equals(bhr.getBlockPos())) {
            return;
        }

        BlockPos offset = iface.getCoreBlockOffset();
        BlockPos corePos = pos.offset(offset);
        ResourceKey<Level> coreDim = iface.getCoreDimension();
        boolean sameDim = coreDim == null || level.dimension().equals(coreDim);
        BlockState coreState = sameDim && level.isLoaded(corePos) ? level.getBlockState(corePos) : null;
        ItemStack coreStack = coreState != null ? new ItemStack(coreState.getBlock()) : ItemStack.EMPTY;
        String blockName = !coreStack.isEmpty()
                ? coreStack.getHoverName().getString()
                : BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).getPath();
        String dimLabel = (coreDim != null ? coreDim.location() : level.dimension().location()).toString();
        String coordsLine = corePos.getX() + "," + corePos.getY() + "," + corePos.getZ() + ";" + dimLabel;

        float tickTime = (float) level.getGameTime() + partialTick;
        Direction hitDir = bhr.getDirection();

        poseStack.pushPose();
        poseStack.translate(
                0.5 + hitDir.getStepX() * 0.8F,
                0.5 + hitDir.getStepY() * 0.8F,
                0.5 + hitDir.getStepZ() * 0.8F
        );

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.35F, 0.0F);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.015F, -0.015F, 0.015F);
        Font font = mc.font;
        Matrix4f textMat = poseStack.last().pose();
        float x1 = -font.width(blockName) / 2.0F;
        font.drawInBatch(Component.literal(blockName), x1, 0.0F, 0xFFFFFFFF, false, textMat, bufferSource, Font.DisplayMode.SEE_THROUGH, 0x80000000, packedLight);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        float x2 = -font.width(coordsLine) / 2.0F;
        font.drawInBatch(Component.literal(coordsLine), x2, 18.0F, 0xFFFFFFFF, false, textMat, bufferSource, Font.DisplayMode.SEE_THROUGH, 0x80000000, packedLight);
        poseStack.popPose();
        poseStack.popPose();

        if (sameDim) {
            float cr = (float) (Math.sin(tickTime * 0.1F) + 1.0) * 0.5F;
            float cg = (float) (Math.sin(tickTime * 0.1F + (Math.PI * 2.0 / 3.0)) + 1.0) * 0.5F;
            float cb = (float) (Math.sin(tickTime * 0.1F + (Math.PI * 4.0 / 3.0)) + 1.0) * 0.5F;
            float ca = (float) (Math.sin(tickTime * 0.12F) + 1.0) * 0.15F + 0.45F;

            VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
            AABB relBox = new AABB(0, 0, 0, 1, 1, 1);
            if (coreState != null) {
                VoxelShape shape = coreState.getShape(level, corePos);
                if (!shape.isEmpty()) {
                    relBox = shape.bounds();
                }
            }
            AABB offsetBox = relBox.move(offset.getX(), offset.getY(), offset.getZ()).inflate(0.002);
            LevelRenderer.renderLineBox(poseStack, lines, offsetBox, cr, cg, cb, ca);

            PoseStack.Pose linePose = poseStack.last();
            lines.addVertex(linePose.pose(), 0.5F, 0.5F, 0.5F)
                    .setColor(cr, cg, cb, ca)
                    .setNormal(linePose, 0.0F, 1.0F, 0.0F);
            lines.addVertex(linePose.pose(), offset.getX() + 0.5F, offset.getY() + 0.5F, offset.getZ() + 0.5F)
                    .setColor(cr, cg, cb, ca)
                    .setNormal(linePose, 0.0F, 1.0F, 0.0F);
        }
    }
}
