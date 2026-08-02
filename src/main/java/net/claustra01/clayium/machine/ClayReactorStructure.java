/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.ClayLaserInterfaceBlock;
import net.claustra01.clayium.world.level.block.MachineInterfaceBlock;
import net.claustra01.clayium.world.level.block.MachineModifierBlock;
import net.claustra01.clayium.world.level.block.RedstoneInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/** Original 3x3x3 Clay Reactor shell, with the controller occupying front centre. */
public final class ClayReactorStructure {
    private ClayReactorStructure() {}
    public static Result validate(ServerLevel level, BlockPos controller, Direction facing) {
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        long sum = 0L;
        int count = 0;
        boolean formed = true;
        for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = 0; z <= 2; z++) {
            if (x == 0 && y == 0 && z == 0) continue;
            BlockPos pos = controller.relative(right, x).relative(Direction.UP, y).relative(back, z);
            int tier = tierAt(level, pos);
            boolean laserPosition = x == 0 && y == 1 && z == 1;
            if (laserPosition != (level.getBlockState(pos).getBlock() instanceof ClayLaserInterfaceBlock)) formed = false;
            if (tier <= 6) formed = false;
            if (tier > 0) sum += 1L << Math.max(0, 16 - tier);
            count++;
        }
        // Linking is deliberately a second pass. A valid-looking interface encountered before a
        // broken shell block must never retain a stale controller link.
        setInterfaceLinks(level, controller, facing, formed);
        long average = count == 0 ? 0 : sum / count;
        int tier = average <= 0 ? 0
                : Math.max(0, 16 - (int)Math.floor(Math.log(average) / Math.log(2D) + .5D));
        return new Result(formed, ClayTier.byLegacyIndex(tier));
    }

    public static void unlinkExpectedInterfaces(ServerLevel level, BlockPos controller, Direction facing) {
        setInterfaceLinks(level, controller, facing, false);
    }

    private static void setInterfaceLinks(
            ServerLevel level, BlockPos controller, Direction facing, boolean linkedToController) {
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = 0; z <= 2; z++) {
            if (x == 0 && y == 0 && z == 0) continue;
            BlockPos pos = controller.relative(right, x).relative(Direction.UP, y).relative(back, z);
            if (level.getBlockEntity(pos) instanceof ControllerLinkedDevice device) {
                if (linkedToController) device.linkMachine(controller);
                else device.unlinkMachine(controller);
            }
        }
    }
    private static int tierAt(ServerLevel level, BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        if (block instanceof ClayLaserInterfaceBlock value) return value.tier().progressionIndex();
        if (block instanceof MachineInterfaceBlock value) return value.tier().progressionIndex();
        if (block instanceof RedstoneInterfaceBlock value) return value.tier().progressionIndex();
        if (block instanceof MachineModifierBlock value
                && value.kind() == MachineModifierBlock.Kind.OVERCLOCKER) {
            return value.tier().progressionIndex();
        }
        for (var entry : ClayiumRegistries.MACHINE_HULL_BLOCKS.entrySet()) if (entry.getValue().get() == block) {
            String id = entry.getKey().replace("_machine_hull", "");
            return ClayTier.byIdOrRaw(id).progressionIndex();
        }
        return -1;
    }
    public record Result(boolean formed, ClayTier tier) {}
}
