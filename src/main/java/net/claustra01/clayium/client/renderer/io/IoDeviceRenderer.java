package net.claustra01.clayium.client.renderer.io;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Shared pipe body and per-face I/O decals for every configurable device. */
public final class IoDeviceRenderer<T extends BlockEntity & ConfigurableIoDevice> implements BlockEntityRenderer<T> {
    @Override
    public void render(T device, float partialTick, PoseStack poses, MultiBufferSource buffers,
                       int light, int overlay) {
        renderDevice(device, poses, buffers, light);
    }

    public static void renderDevice(ConfigurableIoDevice device, PoseStack poses,
                                    MultiBufferSource buffers, int light) {
        if (device.pipeEnabled()) renderPipe(device, poses, buffers, light);
        renderFaces(device, poses, buffers, device.pipeEnabled());
    }

    private static void renderPipe(ConfigurableIoDevice device, PoseStack poses,
                                   MultiBufferSource buffers, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(device.ioOwner().getBlockState());
        cuboid(poses, buffers, light, sprite, 5 / 16F, 5 / 16F, 5 / 16F, 11 / 16F, 11 / 16F, 11 / 16F);
        for (Direction side : Direction.values()) {
            if (!device.pipeConnects(side)) continue;
            switch (side) {
                case DOWN -> cuboid(poses, buffers, light, sprite, 5 / 16F, 0, 5 / 16F, 11 / 16F, 5 / 16F, 11 / 16F);
                case UP -> cuboid(poses, buffers, light, sprite, 5 / 16F, 11 / 16F, 5 / 16F, 11 / 16F, 1, 11 / 16F);
                case NORTH -> cuboid(poses, buffers, light, sprite, 5 / 16F, 5 / 16F, 0, 11 / 16F, 11 / 16F, 5 / 16F);
                case SOUTH -> cuboid(poses, buffers, light, sprite, 5 / 16F, 5 / 16F, 11 / 16F, 11 / 16F, 11 / 16F, 1);
                case WEST -> cuboid(poses, buffers, light, sprite, 0, 5 / 16F, 5 / 16F, 5 / 16F, 11 / 16F, 11 / 16F);
                case EAST -> cuboid(poses, buffers, light, sprite, 11 / 16F, 5 / 16F, 5 / 16F, 1, 11 / 16F, 11 / 16F);
            }
        }
    }

    private static void renderFaces(ConfigurableIoDevice device, PoseStack poses,
                                    MultiBufferSource buffers, boolean pipe) {
        for (Direction side : Direction.values()) {
            SideMode mode = device.sideMode(side);
            if (mode.allowsInsert()) drawFace(poses, buffers, side, "import" + (pipe ? "_p" : ""), pipe);
            if (mode.allowsExtract()) drawFace(poses, buffers, side, "export" + (pipe ? "_p" : ""), pipe);
            if (device.hasFilter(side) && !pipe) drawFace(poses, buffers, side, "filter", false);
        }
    }

    private static void drawFace(PoseStack poses, MultiBufferSource buffers, Direction side,
                                 String texture, boolean pipe) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ClayiumMod.id("block/" + texture));
        VertexConsumer vertices = sprite.wrap(buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)));
        float min = pipe ? 5 / 16F : 0.001F;
        float max = pipe ? 11 / 16F : 0.999F;
        float low = -0.01F;
        float high = 1.01F;
        switch (side) {
            case NORTH -> brightQuad(poses, vertices, side, min,min,low, max,min,low, max,max,low, min,max,low);
            case SOUTH -> brightQuad(poses, vertices, side, max,min,high, min,min,high, min,max,high, max,max,high);
            case WEST -> brightQuad(poses, vertices, side, low,min,max, low,min,min, low,max,min, low,max,max);
            case EAST -> brightQuad(poses, vertices, side, high,min,min, high,min,max, high,max,max, high,max,min);
            case DOWN -> brightQuad(poses, vertices, side, min,low,min, max,low,min, max,low,max, min,low,max);
            case UP -> brightQuad(poses, vertices, side, min,high,max, max,high,max, max,high,min, min,high,min);
        }
    }

    private static void cuboid(PoseStack poses, MultiBufferSource buffers, int light, TextureAtlasSprite sprite,
                               float x0, float y0, float z0, float x1, float y1, float z1) {
        VertexConsumer vertices = sprite.wrap(buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)));
        quad(poses,vertices,Direction.NORTH,x0,y0,z0,x1,y0,z0,x1,y1,z0,x0,y1,z0,light);
        quad(poses,vertices,Direction.SOUTH,x1,y0,z1,x0,y0,z1,x0,y1,z1,x1,y1,z1,light);
        quad(poses,vertices,Direction.WEST,x0,y0,z1,x0,y0,z0,x0,y1,z0,x0,y1,z1,light);
        quad(poses,vertices,Direction.EAST,x1,y0,z0,x1,y0,z1,x1,y1,z1,x1,y1,z0,light);
        quad(poses,vertices,Direction.DOWN,x0,y0,z0,x1,y0,z0,x1,y0,z1,x0,y0,z1,light);
        quad(poses,vertices,Direction.UP,x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,y1,z0,light);
    }

    private static void brightQuad(PoseStack poses, VertexConsumer vertices, Direction normal,
                                   float x0,float y0,float z0,float x1,float y1,float z1,
                                   float x2,float y2,float z2,float x3,float y3,float z3) {
        quad(poses, vertices, normal, x0,y0,z0,x1,y1,z1,x2,y2,z2,x3,y3,z3,
                LightTexture.FULL_BRIGHT);
    }

    private static void quad(PoseStack poses, VertexConsumer vertices, Direction normal,
                             float x0,float y0,float z0,float x1,float y1,float z1,
                             float x2,float y2,float z2,float x3,float y3,float z3,
                             int light) {
        vertex(poses,vertices,normal,x0,y0,z0,0,1,light);
        vertex(poses,vertices,normal,x1,y1,z1,1,1,light);
        vertex(poses,vertices,normal,x2,y2,z2,1,0,light);
        vertex(poses,vertices,normal,x3,y3,z3,0,0,light);
    }

    private static void vertex(PoseStack poses, VertexConsumer vertices, Direction normal,
                               float x,float y,float z,float u,float v,int light) {
        vertices.addVertex(poses.last().pose(),x,y,z).setColor(255,255,255,255).setUv(u,v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(poses.last(),normal.getStepX(),normal.getStepY(),normal.getStepZ());
    }
}
