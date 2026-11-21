package net.claustra01.clayium.laser;

import javax.annotation.Nullable;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record LaserPath(int length, @Nullable BlockPos target, BlockState targetState) {
    public static final int MAX_LENGTH = 32;

    public static LaserPath find(ServerLevel level, BlockPos source, Direction direction) {
        for (int distance = 1; distance < MAX_LENGTH; distance++) {
            BlockPos position = source.relative(direction, distance);
            BlockState state = level.getBlockState(position);
            if (!state.isAir() && !state.is(Blocks.FIRE)) {
                return new LaserPath(distance, position.immutable(), state);
            }
        }
        return new LaserPath(MAX_LENGTH, null, Blocks.AIR.defaultBlockState());
    }

    public boolean remainsValid(ServerLevel level) {
        return target == null || level.getBlockState(target) == targetState;
    }

    public boolean deliver(ServerLevel level, LaserBeam beam, Direction direction) {
        if (target == null) {
            return false;
        }
        LaserReceiver receiver = level.getCapability(
                InterfaceCapabilities.LASER_RECEIVER, target, direction.getOpposite());
        return receiver != null && receiver.receiveLaser(beam);
    }
}
