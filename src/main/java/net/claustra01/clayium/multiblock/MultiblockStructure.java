package net.claustra01.clayium.multiblock;

import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public record MultiblockStructure(boolean formed, int tier) {
    public static MultiblockStructure inspect(
            ServerLevel level,
            BlockPos controllerPos,
            Direction facing,
            MultiblockMachineType machineType) {
        BlockPos center = controllerPos.relative(facing.getOpposite());
        if (!level.getBlockState(center).isAir()) {
            return invalid(machineType);
        }

        BlockPos laserInterface = center.below();
        int structureTier = Integer.MAX_VALUE;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockPos componentPos = center.offset(x, y, z);
                    if (componentPos.equals(controllerPos)) {
                        if (!(level.getBlockState(componentPos).getBlock() instanceof MultiblockControllerBlock controller)
                                || controller.machineType() != machineType) {
                            return invalid(machineType);
                        }
                        continue;
                    }
                    int componentTier;
                    if (machineType.requiresLaserInterface() && componentPos.equals(laserInterface)) {
                        componentTier = interfaceTier(level, componentPos, controllerPos, InterfaceType.LASER);
                    } else if (level.getBlockEntity(componentPos) instanceof InterfaceBlockEntity blockEntity
                            && (blockEntity.interfaceType() == InterfaceType.ITEM
                                    || blockEntity.interfaceType() == InterfaceType.REDSTONE)) {
                        componentTier = interfaceTier(
                                level, componentPos, controllerPos, blockEntity.interfaceType());
                    } else {
                        componentTier = MultiblockRegistration.hullTier(level.getBlockState(componentPos).getBlock());
                    }
                    if (componentTier < machineType.minimumTier()) {
                        return invalid(machineType);
                    }
                    structureTier = Math.min(structureTier, componentTier);
                }
            }
        }
        return new MultiblockStructure(true, structureTier);
    }

    private static int interfaceTier(
            ServerLevel level,
            BlockPos interfacePos,
            BlockPos controllerPos,
            InterfaceType expectedType) {
        if (!(level.getBlockEntity(interfacePos) instanceof InterfaceBlockEntity blockEntity)
                || blockEntity.interfaceType() != expectedType) {
            return -1;
        }
        SynchronizedTarget expectedTarget = SynchronizedTarget.at(level, controllerPos);
        if (!expectedTarget.equals(blockEntity.target())) {
            blockEntity.synchronize(expectedTarget, null);
        }
        return blockEntity.tier();
    }

    private static MultiblockStructure invalid(MultiblockMachineType machineType) {
        return new MultiblockStructure(false, machineType.minimumTier());
    }
}
