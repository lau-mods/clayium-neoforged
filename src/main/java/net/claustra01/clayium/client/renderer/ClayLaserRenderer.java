package net.claustra01.clayium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.IClayLaserManager;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders emitted Clay Laser beams with exact 1.7.10 {@code ClayLaserRenderer} channel color mixing,
 * beam thickness scaling, quality-plane rotation, and fullbright translucency, plus container IO overlays.
 */
public class ClayLaserRenderer<T extends ClayContainerBlockEntity & IClayLaserManager> extends ClayContainerRenderer<T> {
    private static final ResourceLocation LASER_TEXTURE = ClayiumMod.id("textures/block/laser.png");

    public ClayLaserRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            T be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        super.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        ClayLaser laser = be.getIrradiatingClayLaser();
        if (!be.isIrradiating() || laser == null) {
            return;
        }
        int blue = laser.numbers[0];
        int green = laser.numbers[1];
        int red = laser.numbers[2];
        int str = blue + green + red;
        int max = Math.max(blue, Math.max(green, red));
        if (str <= 0 || max <= 0) {
            return;
        }

        float scale = 1.0F - 14.0625F / (float) (str + 14);
        int length = Math.max(1, be.getLaserLength());
        Direction dir = be.getLaserDirection();
        int quality = Math.max(1, ClayiumConfig.LASER_QUALITY.get());
        int rCol = 255 * red / max;
        int gCol = 255 * green / max;
        int bCol = 255 * blue / max;
        int alpha = Math.max(0, Math.min(255, (int) (26.0F + scale * 26.0F) * 8 / quality));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(rotationFromUp(dir));
        poseStack.scale(scale, 1.0F, scale);
        poseStack.translate(0.0F, -scale / 6.0F, 0.0F);
        poseStack.scale(1.0F, (float) length + scale / 3.0F, 1.0F);

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(LASER_TEXTURE));
        for (int k = 0; k < quality; k++) {
            PoseStack.Pose pose = poseStack.last();
            Matrix4f mat = pose.pose();
            addLaserVertex(vc, mat, pose, 0.0F, 1.0F, 0.5F, rCol, gCol, bCol, alpha, 0.0F, 0.0F);
            addLaserVertex(vc, mat, pose, 0.0F, 1.0F, -0.5F, rCol, gCol, bCol, alpha, 1.0F, 0.0F);
            addLaserVertex(vc, mat, pose, 0.0F, 0.0F, -0.5F, rCol, gCol, bCol, alpha, 1.0F, 1.0F);
            addLaserVertex(vc, mat, pose, 0.0F, 0.0F, 0.5F, rCol, gCol, bCol, alpha, 0.0F, 1.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F / (float) quality));
        }

        poseStack.popPose();
    }

    private static Quaternionf rotationFromUp(Direction dir) {
        return switch (dir) {
            case UP -> new Quaternionf();
            case DOWN -> Axis.XP.rotationDegrees(180.0F);
            case NORTH -> Axis.XP.rotationDegrees(-90.0F);
            case SOUTH -> Axis.XP.rotationDegrees(90.0F);
            case WEST -> Axis.ZP.rotationDegrees(90.0F);
            case EAST -> Axis.ZP.rotationDegrees(-90.0F);
        };
    }

    private static void addLaserVertex(
            VertexConsumer vc,
            Matrix4f mat,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            int r,
            int g,
            int b,
            int a,
            float u,
            float v) {
        vc.addVertex(mat, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 1.0F, 0.0F, 0.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        BlockPos start = blockEntity.getBlockPos();
        BlockPos end = start.relative(blockEntity.getLaserDirection(), Math.max(1, blockEntity.getLaserLength()));
        return new AABB(
                Math.min(start.getX(), end.getX()),
                Math.min(start.getY(), end.getY()),
                Math.min(start.getZ(), end.getZ()),
                Math.max(start.getX(), end.getX()) + 1.0,
                Math.max(start.getY(), end.getY()) + 1.0,
                Math.max(start.getZ(), end.getZ()) + 1.0
        );
    }
}
