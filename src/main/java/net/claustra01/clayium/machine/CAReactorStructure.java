/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.CAReactorCoilBlock;
import net.claustra01.clayium.world.level.block.CAReactorHullBlock;
import net.claustra01.clayium.world.level.block.MachineInterfaceBlock;
import net.claustra01.clayium.world.level.block.RedstoneInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/** Deterministic, bounded validation of the original ring-and-shell CA Reactor. */
public final class CAReactorStructure {
    public static final int MAX_COILS = 128;
    public static final int MIN_HULLS = 50;
    private static final int[][] COIL_NEIGHBORS = {
        {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1},
        {1,1,0},{1,-1,0},{-1,1,0},{-1,-1,0},
        {1,0,1},{1,0,-1},{-1,0,1},{-1,0,-1},
        {0,1,1},{0,1,-1},{0,-1,1},{0,-1,-1}
    };

    public record Result(boolean formed, ClayTier tier, int hullCount, double averageRank,
                         double efficiency, double energyMultiplier, int productRank) {
        public static Result invalid(ClayTier tier) {
            return new Result(false, tier, 0, 0.0D, 0.0D, 1.0D, 0);
        }
    }

    private CAReactorStructure() {}

    public static Result validate(ServerLevel level, BlockPos controller, Direction front, ClayTier reactorTier) {
        BlockPos start = controller.relative(front.getOpposite());
        if (!level.hasChunkAt(start) || !(level.getBlockState(start).getBlock() instanceof CAReactorCoilBlock)) {
            unlink(level, controller, Set.of());
            return Result.invalid(reactorTier);
        }

        Set<BlockPos> coils = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos position = queue.removeFirst();
            if (!coils.add(position)) continue;
            if (coils.size() > MAX_COILS) return Result.invalid(reactorTier);
            CAReactorCoilBlock coil = coil(level, position);
            if (coil == null || !coil.tier().isAtLeast(reactorTier)) return Result.invalid(reactorTier);
            for (BlockPos neighbor : coilNeighbors(position)) {
                if (!level.hasChunkAt(neighbor)) return Result.invalid(reactorTier);
                if (coil(level, neighbor) != null && !coils.contains(neighbor)) queue.addLast(neighbor);
            }
        }
        if (coils.size() < 4) return Result.invalid(reactorTier);
        for (BlockPos coil : coils) {
            long degree = coilNeighbors(coil).stream().filter(coils::contains).count();
            if (degree != 2) return Result.invalid(reactorTier);
        }

        Set<BlockPos> shell = new LinkedHashSet<>();
        Set<ControllerLinkedDevice> interfaces = new HashSet<>();
        int hullCount = 0;
        int rankSum = 0;
        int maxRankIndex = reactorTier == ClayTier.ANTIMATTER ? 1
                : reactorTier == ClayTier.PURE_ANTIMATTER ? 5 : 9;
        for (BlockPos coil : coils) {
            for (Direction direction : Direction.values()) {
                BlockPos position = coil.relative(direction);
                if (position.equals(controller) || coils.contains(position) || !shell.add(position)) continue;
                if (!level.hasChunkAt(position)) return Result.invalid(reactorTier);
                Block block = level.getBlockState(position).getBlock();
                if (block instanceof CAReactorHullBlock hull) {
                    if (hull.rankIndex() > maxRankIndex) return Result.invalid(reactorTier);
                    hullCount++;
                    rankSum += hull.rankIndex();
                } else if (block instanceof MachineInterfaceBlock machineInterface) {
                    if (!machineInterface.tier().isAtLeast(reactorTier)) return Result.invalid(reactorTier);
                    if (level.getBlockEntity(position) instanceof ControllerLinkedDevice linked) interfaces.add(linked);
                } else if (block instanceof RedstoneInterfaceBlock redstoneInterface) {
                    if (!redstoneInterface.tier().isAtLeast(reactorTier)) return Result.invalid(reactorTier);
                    if (level.getBlockEntity(position) instanceof ControllerLinkedDevice linked) interfaces.add(linked);
                } else {
                    return Result.invalid(reactorTier);
                }
            }
        }
        if (hullCount < MIN_HULLS) {
            unlink(level, controller, interfaces);
            return Result.invalid(reactorTier);
        }
        interfaces.forEach(device -> device.linkMachine(controller));
        double averageRank = (double)rankSum / hullCount;
        double efficiency = 0.2D * Math.pow(1.02D, hullCount) * Math.pow(7.5D, averageRank);
        double energyMultiplier = Math.pow(1.01D, hullCount * averageRank);
        return new Result(true, reactorTier, hullCount, averageRank, efficiency, energyMultiplier,
                Math.min(8, (int)averageRank));
    }

    private static CAReactorCoilBlock coil(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).getBlock() instanceof CAReactorCoilBlock value ? value : null;
    }

    private static List<BlockPos> coilNeighbors(BlockPos position) {
        List<BlockPos> result = new ArrayList<>(COIL_NEIGHBORS.length);
        for (int[] offset : COIL_NEIGHBORS) result.add(position.offset(offset[0], offset[1], offset[2]));
        return result;
    }

    private static void unlink(ServerLevel level, BlockPos controller, Set<ControllerLinkedDevice> devices) {
        devices.forEach(device -> device.unlinkMachine(controller));
    }

    public static long processingTime(long baseTicks, Result result) {
        if (!result.formed()) return Long.MAX_VALUE;
        double value = baseTicks * Math.pow(9.0D, result.productRank()) / result.efficiency();
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(1L, Math.round(value));
    }
}
