/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import javax.annotation.Nullable;
import net.claustra01.clayium.machine.ControllerLinkedDevice;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Redstone modes are isolated here and never expose item transport. */
public final class RedstoneInterfaceBlockEntity extends BlockEntity implements ControllerLinkedDevice {
    @Nullable private BlockPos linkedMachine;
    private int mode;
    private boolean lastSignal;

    public RedstoneInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.REDSTONE_INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }

    public void serverTick() {
        if (level == null) return;
        boolean signal = level.hasNeighborSignal(worldPosition);
        MachineBlockEntity machine = linkedMachine();
        if (machine != null) {
            switch (mode) {
                case 4 -> machine.setExternalWorkEnabled(signal);
                case 5 -> machine.setExternalWorkEnabled(!signal);
                case 6 -> { if (!lastSignal && signal) machine.setExternalWorkEnabled(true); }
                case 7 -> { if (!lastSignal && signal) machine.setExternalWorkEnabled(false); }
                case 8 -> { if (!lastSignal && signal) machine.runOnce(); }
                default -> { }
            }
        }
        if (signal != lastSignal || mode >= 1 && mode <= 3) {
            lastSignal = signal;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public int redstoneSignal() {
        MachineBlockEntity machine = linkedMachine();
        if (machine == null) return 0;
        return switch (mode) {
            case 1 -> machine.isDoingWork() ? 0 : 15;
            case 2 -> machine.isWorkScheduled() ? 15 : 0;
            case 3 -> machine.isDoingWork() ? 15 : 0;
            default -> 0;
        };
    }

    public Component cycleMode(boolean inspectOnly) {
        if (!inspectOnly) {
            mode = (mode + 1) % 9;
            lastSignal = false;
            setChanged();
            if (level != null) level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        return Component.translatable("gui.clayium_neoforged.redstone_interface.mode." + mode);
    }

    @Override public void linkMachine(BlockPos controller) {
        if (!controller.equals(linkedMachine)) { linkedMachine = controller.immutable(); setChanged(); }
    }
    @Override public void unlinkMachine(BlockPos controller) {
        if (controller.equals(linkedMachine)) { linkedMachine = null; setChanged(); }
    }

    @Nullable private MachineBlockEntity linkedMachine() {
        return linkedMachine != null && level != null
                && level.getBlockEntity(linkedMachine) instanceof MachineBlockEntity machine ? machine : null;
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        linkedMachine = tag.contains("LinkedMachine") ? BlockPos.of(tag.getLong("LinkedMachine")) : null;
        mode = Math.floorMod(tag.getInt("Mode"), 9);
        lastSignal = tag.getBoolean("LastSignal");
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (linkedMachine != null) tag.putLong("LinkedMachine", linkedMachine.asLong());
        tag.putInt("Mode", mode);
        tag.putBoolean("LastSignal", lastSignal);
    }
}
