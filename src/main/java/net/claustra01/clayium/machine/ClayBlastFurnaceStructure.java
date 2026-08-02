/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.machine;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.MachineInterfaceBlock;
import net.claustra01.clayium.world.level.block.RedstoneInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Validation and tier calculation for the original 3x2x3 Clay Blast Furnace. */
public final class ClayBlastFurnaceStructure {
    public static final int COMPONENT_COUNT = 17;

    public record Result(boolean formed, ClayTier recipeTier) {
        public static final Result INVALID = new Result(false, ClayTier.RAW);
    }

    private ClayBlastFurnaceStructure() {
    }

    public static Result validate(ServerLevel level, BlockPos controller, Direction front) {
        List<ControllerLinkedDevice> interfaces = new ArrayList<>();
        long tierWeight = 0;
        Direction back = front.getOpposite();
        Direction right = front.getClockWise();
        for (int y = 0; y < 2; y++) {
            for (int depth = 0; depth < 3; depth++) {
                for (int horizontal = -1; horizontal <= 1; horizontal++) {
                    if (y == 0 && depth == 0 && horizontal == 0) {
                        continue;
                    }
                    BlockPos position = controller.above(y).relative(back, depth).relative(right, horizontal);
                    if (!level.hasChunkAt(position)) {
                        unlinkExpectedInterfaces(level, controller, front);
                        return Result.INVALID;
                    }
                    BlockState state = level.getBlockState(position);
                    ClayTier componentTier = componentTier(state);
                    if (componentTier.progressionIndex() < ClayTier.ADVANCED.progressionIndex()) {
                        unlinkExpectedInterfaces(level, controller, front);
                        return Result.INVALID;
                    }
                    tierWeight += 1L << (16 - componentTier.progressionIndex());
                    if (level.getBlockEntity(position) instanceof ControllerLinkedDevice device
                            && isInterface(state)) {
                        interfaces.add(device);
                    }
                }
            }
        }

        double averageWeight = tierWeight / COMPONENT_COUNT;
        int tier = Math.max(0, 16 - (int) Math.floor(Math.log(averageWeight) / Math.log(2.0D) + 0.5D));
        ClayTier recipeTier = ClayTier.byLegacyIndex(tier);
        interfaces.forEach(blockEntity -> blockEntity.linkMachine(controller));
        return new Result(true, recipeTier);
    }

    public static void unlinkExpectedInterfaces(ServerLevel level, BlockPos controller, Direction front) {
        Direction back = front.getOpposite();
        Direction right = front.getClockWise();
        for (int y = 0; y < 2; y++) {
            for (int depth = 0; depth < 3; depth++) {
                for (int horizontal = -1; horizontal <= 1; horizontal++) {
                    BlockPos position = controller.above(y).relative(back, depth).relative(right, horizontal);
                    if (level.hasChunkAt(position)
                            && level.getBlockEntity(position) instanceof ControllerLinkedDevice device) {
                        device.unlinkMachine(controller);
                    }
                }
            }
        }
    }

    private static ClayTier componentTier(BlockState state) {
        Block block = state.getBlock();
        for (var entry : ClayiumRegistries.MACHINE_HULL_BLOCKS.entrySet()) {
            if (entry.getValue().get() == block) {
                String tierId = entry.getKey().substring(0, entry.getKey().length() - "_machine_hull".length());
                return ClayTier.byIdOrRaw(tierId);
            }
        }
        if (block instanceof MachineInterfaceBlock machineInterface) {
            return machineInterface.tier();
        }
        if (block instanceof RedstoneInterfaceBlock redstoneInterface) {
            return redstoneInterface.tier();
        }
        return ClayTier.RAW;
    }

    private static boolean isInterface(BlockState state) {
        return state.getBlock() instanceof MachineInterfaceBlock
                || state.getBlock() instanceof RedstoneInterfaceBlock;
    }

}
