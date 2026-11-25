package net.claustra01.clayium.logistics;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PipeShapes {
    private static final VoxelShape CENTER = Block.box(5, 5, 5, 11, 11, 11);

    private PipeShapes() {
    }

    public static VoxelShape forDevice(ConfigurableIoDevice device) {
        VoxelShape shape = CENTER;
        for (Direction direction : Direction.values()) {
            if (device.pipeConnects(direction)) {
                shape = Shapes.or(shape, arm(direction));
            }
        }
        return shape;
    }

    private static VoxelShape arm(Direction direction) {
        return switch (direction) {
            case DOWN -> Block.box(5, 0, 5, 11, 5, 11);
            case UP -> Block.box(5, 11, 5, 11, 16, 11);
            case NORTH -> Block.box(5, 5, 0, 11, 11, 5);
            case SOUTH -> Block.box(5, 5, 11, 11, 11, 16);
            case WEST -> Block.box(0, 5, 5, 5, 11, 11);
            case EAST -> Block.box(11, 5, 5, 16, 11, 11);
        };
    }
}
