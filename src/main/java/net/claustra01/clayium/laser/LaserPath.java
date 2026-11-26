package net.claustra01.clayium.laser;

import javax.annotation.Nullable;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.world.claytree.ClayTreeLaserConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public record LaserPath(int length, @Nullable BlockPos target, BlockState targetState) {
    public static final int MAX_LENGTH = 32;

    public static LaserPath find(ServerLevel level, BlockPos source, Direction direction) {
        for (int distance = 1; distance < MAX_LENGTH; distance++) {
            BlockPos position = source.relative(direction, distance);
            BlockState state = level.getBlockState(position);
            if (!state.isAir() && !state.is(Blocks.FIRE) && !state.is(Tags.Blocks.GLASS_BLOCKS)) {
                return new LaserPath(distance, position.immutable(), state);
            }
        }
        return new LaserPath(MAX_LENGTH, null, Blocks.AIR.defaultBlockState());
    }

    public boolean remainsValid(ServerLevel level) {
        return target == null || level.getBlockState(target).equals(targetState);
    }

    public boolean deliver(
            ServerLevel level,
            LaserBeam beam,
            Direction direction,
            LaserIrradiation irradiation,
            BlockPos source) {
        if (target == null) {
            irradiation.clearTarget();
            return false;
        }
        LaserReceiver receiver = level.getCapability(
                InterfaceCapabilities.LASER_RECEIVER, target, direction.getOpposite());
        BlockState before = level.getBlockState(target);
        if (receiver != null && receiver.receiveLaser(beam)) {
            if (before.is(ClayTreeLaserConversion.LASER_TREE_SOURCES)
                    && before.equals(level.getBlockState(target))) {
                return irradiation.irradiate(level, source, this, beam);
            }
            irradiation.clearTarget();
            return true;
        }
        return irradiation.irradiate(level, source, this, beam);
    }
}
