/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.world.level.block.LogisticsBlock;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.claustra01.clayium.world.level.block.entity.MachineBlockEntity;
import net.claustra01.clayium.world.level.block.FluidBufferBlock;
import net.claustra01.clayium.world.level.block.entity.FluidBufferBlockEntity;
import net.claustra01.clayium.world.level.block.SaltExtractorBlock;
import net.claustra01.clayium.world.level.block.entity.SaltExtractorBlockEntity;
import net.claustra01.clayium.world.level.block.CobblestoneGeneratorBlock;
import net.claustra01.clayium.world.level.block.entity.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.world.item.ClayConfiguratorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Draws the original per-face import, export, and filter decals. */
public final class IoOverlayRenderer<T extends BlockEntity & ConfigurableItemDevice>
        implements BlockEntityRenderer<T> {
    @Override
    public void render(
            T blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        boolean pipe = blockEntity.getBlockState().getBlock() instanceof MachineBlock
                ? blockEntity.getBlockState().getValue(MachineBlock.PIPE)
                : blockEntity.getBlockState().getBlock() instanceof LogisticsBlock
                        ? blockEntity.getBlockState().getValue(LogisticsBlock.PIPE)
                        : blockEntity.getBlockState().getBlock() instanceof FluidBufferBlock
                                ? blockEntity.getBlockState().getValue(FluidBufferBlock.PIPE)
                                : blockEntity.getBlockState().getBlock() instanceof SaltExtractorBlock
                                        ? blockEntity.getBlockState().getValue(SaltExtractorBlock.PIPE)
                                        : blockEntity.getBlockState().getValue(CobblestoneGeneratorBlock.PIPE);
        if (pipe) {
            ResourceLocation hull = hullTexture(blockEntity);
            for (Direction side : Direction.values()) {
                if (pipeConnects(blockEntity, side)) {
                    drawArm(poseStack, buffers, packedLight, side, hull);
                }
            }
        }
        if (pipe && !showsPipeOverlay()) {
            return;
        }
        for (Direction side : Direction.values()) {
            String insert = insertionIcon(blockEntity, side);
            String extract = extractionIcon(blockEntity, side);
            if (!insert.isEmpty()) {
                drawFace(poseStack, buffers, packedLight, side, insert + (pipe ? "_p" : ""), pipe);
            }
            if (!extract.isEmpty()) {
                drawFace(poseStack, buffers, packedLight, side, extract + (pipe ? "_p" : ""), pipe);
            }
            if (blockEntity.hasFilter(side) && !pipe) {
                drawFace(poseStack, buffers, packedLight, side, "filter", false);
            }
        }
    }

    private static boolean pipeConnects(ConfigurableItemDevice device, Direction side) {
        return device instanceof MachineBlockEntity machine
                ? machine.pipeConnects(side)
                : device instanceof LogisticsBlockEntity logistics
                        ? logistics.pipeConnects(side)
                        : device instanceof FluidBufferBlockEntity fluid
                                ? fluid.pipeConnects(side)
                                : device instanceof SaltExtractorBlockEntity salt
                                        ? salt.pipeConnects(side)
                                        : device instanceof CobblestoneGeneratorBlockEntity generator && generator.pipeConnects(side);
    }

    private static ResourceLocation hullTexture(BlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof MachineBlock machine) {
            return machine.tier().progressionIndex() == 1
                    ? Clayium.id("block/clay_machine_hull")
                    : Clayium.id("block/machine_hull_" + machine.tier().id());
        }
        if (blockEntity.getBlockState().getBlock() instanceof LogisticsBlock logistics) {
            if (logistics.kind() == net.claustra01.clayium.logistics.LogisticsKind.STORAGE_CONTAINER
                    || logistics.kind() == net.claustra01.clayium.logistics.LogisticsKind.VOID_CONTAINER) {
                return Clayium.id("block/az91d_hull");
            }
            return Clayium.id("block/machine_hull_"
                    + net.claustra01.clayium.tier.ClayTier.byLegacyIndex(logistics.tier()).id());
        }
        if (blockEntity.getBlockState().getBlock() instanceof FluidBufferBlock fluid) {
            return Clayium.id("block/machine_hull_" + fluid.tier().id());
        }
        if (blockEntity.getBlockState().getBlock() instanceof SaltExtractorBlock salt) {
            return Clayium.id("block/machine_hull_" + salt.tier().id());
        }
        if (blockEntity.getBlockState().getBlock() instanceof CobblestoneGeneratorBlock generator) {
            return generator.tier().progressionIndex() == 1
                    ? Clayium.id("block/clay_machine_hull")
                    : Clayium.id("block/machine_hull_" + generator.tier().id());
        }
        return Clayium.id("block/clay_machine_hull");
    }

    private static void drawArm(
            PoseStack poses,
            MultiBufferSource buffers,
            int light,
            Direction side,
            ResourceLocation hull) {
        float min = 5.0F / 16.0F;
        float max = 11.0F / 16.0F;
        switch (side) {
            case DOWN -> cuboid(poses, buffers, light, hull, min, 0, min, max, min, max);
            case UP -> cuboid(poses, buffers, light, hull, min, max, min, max, 1, max);
            case NORTH -> cuboid(poses, buffers, light, hull, min, min, 0, max, max, min);
            case SOUTH -> cuboid(poses, buffers, light, hull, min, min, max, max, max, 1);
            case WEST -> cuboid(poses, buffers, light, hull, 0, min, min, min, max, max);
            case EAST -> cuboid(poses, buffers, light, hull, max, min, min, 1, max, max);
        }
    }

    private static void cuboid(
            PoseStack poses,
            MultiBufferSource buffers,
            int light,
            ResourceLocation texture,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        VertexConsumer consumer = sprite.wrap(
                buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)));
        quad(poses, consumer, light, Direction.NORTH,
                minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ,
                minX, maxX, 1 - minY, 1 - maxY);
        quad(poses, consumer, light, Direction.SOUTH,
                maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ,
                1 - maxX, 1 - minX, 1 - minY, 1 - maxY);
        quad(poses, consumer, light, Direction.WEST,
                minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ,
                1 - maxZ, 1 - minZ, 1 - minY, 1 - maxY);
        quad(poses, consumer, light, Direction.EAST,
                maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
                minZ, maxZ, 1 - minY, 1 - maxY);
        quad(poses, consumer, light, Direction.DOWN,
                minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ,
                minX, maxX, minZ, maxZ);
        quad(poses, consumer, light, Direction.UP,
                minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ,
                minX, maxX, 1 - maxZ, 1 - minZ);
    }

    private static boolean showsPipeOverlay() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        return player.getMainHandItem().getItem() instanceof ClayConfiguratorItem main && main.showsPipingOverlay()
                || player.getOffhandItem().getItem() instanceof ClayConfiguratorItem off && off.showsPipingOverlay();
    }

    private static String insertionIcon(ConfigurableItemDevice device, Direction side) {
        if (device instanceof MachineBlockEntity machine) {
            return machine.insertionIcon(side);
        }
        if (device instanceof LogisticsBlockEntity logistics) {
            return logistics.insertionIcon(side);
        }
        if (device instanceof FluidBufferBlockEntity fluid) return fluid.insertionRoute(side) >= 0 ? "import" : "";
        return device instanceof SaltExtractorBlockEntity salt && salt.insertionRoute(side) >= 0 ? "import_energy" : "";
    }

    private static String extractionIcon(ConfigurableItemDevice device, Direction side) {
        if (device instanceof MachineBlockEntity machine) {
            return machine.extractionIcon(side);
        }
        if (device instanceof LogisticsBlockEntity logistics) {
            return logistics.extractionIcon(side);
        }
        if (device instanceof FluidBufferBlockEntity fluid) return fluid.extractionRoute(side) >= 0 ? "export" : "";
        if (device instanceof SaltExtractorBlockEntity salt) return salt.extractionRoute(side) >= 0 ? "export" : "";
        return device instanceof CobblestoneGeneratorBlockEntity generator && generator.extractionRoute(side) >= 0 ? "export" : "";
    }

    private static void drawFace(
            PoseStack poses,
            MultiBufferSource buffers,
            int light,
            Direction side,
            String textureName,
            boolean pipe) {
        ResourceLocation texture = Clayium.id("block/" + textureName);
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        VertexConsumer consumer = sprite.wrap(
                buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)));
        float min = pipe ? 5.0F / 16.0F : 0.001F;
        float max = pipe ? 11.0F / 16.0F : 0.999F;
        float low = -0.02F;
        float high = 1.02F;
        switch (side) {
            case NORTH -> quadOverlay(poses, consumer,
                    min, min, low, max, min, low, max, max, low, min, max, low);
            case SOUTH -> quadOverlay(poses, consumer,
                    max, min, high, min, min, high, min, max, high, max, max, high);
            case WEST -> quadOverlay(poses, consumer,
                    low, min, max, low, min, min, low, max, min, low, max, max);
            case EAST -> quadOverlay(poses, consumer,
                    high, min, min, high, min, max, high, max, max, high, max, min);
            case DOWN -> quadOverlay(poses, consumer,
                    min, low, min, max, low, min, max, low, max, min, low, max);
            case UP -> quadOverlay(poses, consumer,
                    min, high, max, max, high, max, max, high, min, min, high, min);
        }
    }

    private static void quadOverlay(
            PoseStack poses,
            VertexConsumer consumer,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3) {
        vertexOverlay(poses, consumer, x0, y0, z0, 0, 1);
        vertexOverlay(poses, consumer, x1, y1, z1, 1, 1);
        vertexOverlay(poses, consumer, x2, y2, z2, 1, 0);
        vertexOverlay(poses, consumer, x3, y3, z3, 0, 0);
    }

    private static void quad(
            PoseStack poses,
            VertexConsumer consumer,
            int light,
            Direction normal,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3) {
        vertex(poses, consumer, light, normal, x0, y0, z0, 0, 1);
        vertex(poses, consumer, light, normal, x1, y1, z1, 1, 1);
        vertex(poses, consumer, light, normal, x2, y2, z2, 1, 0);
        vertex(poses, consumer, light, normal, x3, y3, z3, 0, 0);
    }

    private static void quad(
            PoseStack poses,
            VertexConsumer consumer,
            int light,
            Direction normal,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float u0, float u1, float v0, float v1) {
        vertex(poses, consumer, light, normal, x0, y0, z0, u0, v0);
        vertex(poses, consumer, light, normal, x1, y1, z1, u1, v0);
        vertex(poses, consumer, light, normal, x2, y2, z2, u1, v1);
        vertex(poses, consumer, light, normal, x3, y3, z3, u0, v1);
    }

    private static void vertex(
            PoseStack poses,
            VertexConsumer consumer,
            int light,
            Direction normal,
            float x, float y, float z,
            float u, float v) {
        consumer.addVertex(poses.last().pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(poses.last(), normal.getStepX(), normal.getStepY(), normal.getStepZ());
    }

    private static void vertexOverlay(
            PoseStack poses,
            VertexConsumer consumer,
            float x, float y, float z,
            float u, float v) {
        consumer.addVertex(poses.last().pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(poses.last(), 0.0F, 1.0F, 0.0F);
    }
}
