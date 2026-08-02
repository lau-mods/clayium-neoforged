/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import javax.annotation.Nullable;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserReceiver;
import net.claustra01.clayium.machine.ControllerLinkedDevice;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayLaserInterfaceBlockEntity extends BlockEntity implements ClayLaserReceiver, ControllerLinkedDevice {
    @Nullable private BlockPos linkedMachine;
    public ClayLaserInterfaceBlockEntity(BlockPos pos, BlockState state) { super(ClayiumRegistries.CLAY_LASER_INTERFACE_BLOCK_ENTITY.get(), pos, state); }
    @Override public boolean receiveClayLaser(ClayLaser laser, Direction incomingSide) {
        if (level == null || linkedMachine == null) return false;
        return level.getBlockEntity(linkedMachine) instanceof ClayLaserReceiver receiver
                && receiver.receiveClayLaser(laser, incomingSide);
    }
    @Override public void linkMachine(BlockPos controller) { linkedMachine = controller.immutable(); setChanged(); }
    @Override public void unlinkMachine(BlockPos controller) { if (controller.equals(linkedMachine)) { linkedMachine = null; setChanged(); } }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); if (linkedMachine != null) tag.putLong("LinkedMachine", linkedMachine.asLong()); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); linkedMachine = tag.contains("LinkedMachine") ? BlockPos.of(tag.getLong("LinkedMachine")) : null; }
}
