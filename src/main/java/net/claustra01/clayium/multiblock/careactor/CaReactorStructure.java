package net.claustra01.clayium.multiblock.careactor;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.claustra01.clayium.machine.iface.InterfaceBlock;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public record CaReactorStructure(
        boolean formed,
        int coilCount,
        int hullCount,
        double averageRank,
        Set<BlockPos> components,
        Optional<CaReactorFormula> formula,
        String failure) {
    public static final int MAX_COILS = 128;
    public static final int SEARCH_RADIUS = 8;

    public static CaReactorStructure inspect(ServerLevel level, BlockPos corePos, int coreTier) {
        Set<BlockPos> observed = new HashSet<>();
        observed.add(corePos.immutable());
        BlockPos firstCoil = findFirstCoil(level, corePos);
        if (firstCoil == null) {
            return invalid(observed, "no coil was found within the search radius");
        }

        Set<BlockPos> coils = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(firstCoil);
        while (!pending.isEmpty()) {
            BlockPos coilPos = pending.removeFirst();
            if (!coils.add(coilPos.immutable())) {
                continue;
            }
            if (coils.size() > MAX_COILS) {
                observed.addAll(coils);
                return invalid(observed, "the coil path exceeded " + MAX_COILS + " blocks");
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = coilPos.relative(direction);
                if (level.getBlockState(neighbor).getBlock() instanceof CaReactorCoilBlock
                        && !coils.contains(neighbor)) {
                    pending.add(neighbor);
                }
            }
        }
        observed.addAll(coils);
        if (coils.size() < 8) {
            return invalid(observed, "the coil path from " + firstCoil + " contained " + coils.size() + " blocks");
        }

        Set<BlockPos> hulls = new HashSet<>();
        EnumSet<InterfaceType> interfaces = EnumSet.noneOf(InterfaceType.class);
        long totalRank = 0L;
        int coreFaces = 0;
        int maximumRank = coreTier - 3;
        for (BlockPos coilPos : coils) {
            BlockState coilState = level.getBlockState(coilPos);
            if (!(coilState.getBlock() instanceof CaReactorCoilBlock coil) || coil.tier() != coreTier) {
                return invalid(observed, "a coil tier did not match the core tier at " + coilPos);
            }
            int coilNeighbors = 0;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = coilPos.relative(direction);
                BlockState state = level.getBlockState(neighbor);
                if (state.getBlock() instanceof CaReactorCoilBlock) {
                    coilNeighbors++;
                    continue;
                }
                observed.add(neighbor.immutable());
                if (state.getBlock() instanceof CaReactorHullBlock hull) {
                    if (hull.rank() > maximumRank) {
                        return invalid(observed, "a hull rank exceeded the core limit at " + neighbor);
                    }
                    if (hulls.add(neighbor.immutable())) {
                        totalRank += hull.rank();
                    }
                } else if (state.getBlock() instanceof InterfaceBlock reactorInterface) {
                    if (reactorInterface.tier() < coreTier) {
                        return invalid(observed, "an interface tier was below the core tier at " + neighbor);
                    }
                    if (!(level.getBlockEntity(neighbor) instanceof InterfaceBlockEntity blockEntity)
                            || !SynchronizedTarget.at(level, corePos).equals(blockEntity.target())) {
                        return invalid(observed, "an interface was not synchronized to the core at " + neighbor);
                    }
                    interfaces.add(reactorInterface.interfaceType());
                } else if (state.getBlock() instanceof CaReactorCoreBlock) {
                    if (!neighbor.equals(corePos)) {
                        return invalid(observed, "a second core touched the coil path at " + neighbor);
                    }
                    coreFaces++;
                } else {
                    return invalid(observed, "a coil face was uncovered at " + neighbor
                            + " (" + state.getBlock() + ")");
                }
            }
            if (coilNeighbors != 2) {
                return invalid(observed, "a coil had " + coilNeighbors + " coil neighbors at " + coilPos);
            }
        }
        if (coreFaces != 1 || hulls.size() < 50
                || !interfaces.contains(InterfaceType.ITEM)
                || !interfaces.contains(InterfaceType.LASER)) {
            return invalid(observed, "the shell had " + hulls.size() + " hulls and interfaces " + interfaces);
        }
        double averageRank = (double)totalRank / hulls.size();
        Optional<CaReactorFormula> formula = CaReactorFormula.calculate(hulls.size(), averageRank);
        return new CaReactorStructure(
                formula.isPresent(), coils.size(), hulls.size(), averageRank, Set.copyOf(observed), formula,
                formula.isPresent() ? "" : "the reactor formula was outside its safe numeric range");
    }

    private static BlockPos findFirstCoil(ServerLevel level, BlockPos corePos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = corePos.relative(direction);
            if (level.getBlockState(adjacent).getBlock() instanceof CaReactorCoilBlock) {
                return adjacent;
            }
        }
        return null;
    }

    private static CaReactorStructure invalid(Set<BlockPos> observed, String failure) {
        return new CaReactorStructure(false, 0, 0, 0.0D, Set.copyOf(observed), Optional.empty(), failure);
    }
}
