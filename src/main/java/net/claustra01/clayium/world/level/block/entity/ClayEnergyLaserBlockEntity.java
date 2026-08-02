/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserPath;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ClayEnergyLaserMenu;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayEnergyLaserBlockEntity extends AbstractConfigurableMachineBlockEntity {
    public static final int ENERGY_SLOT = 0;
    private int beamLength;
    private boolean irradiating;

    public ClayEnergyLaserBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.CLAY_ENERGY_LASER_BLOCK_ENTITY.get(), pos, state, 1, true);
    }

    @Override protected void tickMachine() {
        long cost = energyPerTick();
        if (energy.extract(cost, true) != cost) consumeFuel(ENERGY_SLOT);
        if (!(level instanceof ServerLevel server)) return;
        boolean enabled = !level.hasNeighborSignal(worldPosition) && energy.extract(cost, true) == cost;
        if (!enabled) {
            updateBeam(0, false);
            return;
        }
        energy.extract(cost, false);
        ClayLaserPath.Result result = ClayLaserPath.irradiate(server, worldPosition,
                getBlockState().getValue(AbstractTieredIoMachineBlock.FACING), spectrum());
        updateBeam(result.length(), true);
        setChanged();
    }

    private void updateBeam(int length, boolean active) {
        if (beamLength == length && irradiating == active) return;
        beamLength = length;
        irradiating = active;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private ClayLaser spectrum() {
        return switch (tierIndex()) {
            case 7 -> new ClayLaser(0, 1, 0, 0);
            case 8 -> new ClayLaser(0, 0, 1, 0);
            case 9 -> new ClayLaser(0, 0, 0, 1);
            default -> new ClayLaser(0, 3, 3, 3);
        };
    }

    public ClayLaser outputLaser() { return spectrum(); }

    public int beamLength() { return beamLength; }
    public boolean irradiating() { return irradiating; }
    @Override protected boolean acceptsClayEnergy() { return true; }
    @Override protected boolean isExternalInput(int slot, ItemStack stack) { return slot == ENERGY_SLOT && EnergeticClayFuel.isFuel(stack); }
    @Override protected boolean isExternalOutput(int slot) { return false; }
    @Override protected boolean isNormalInputSlot(int slot) { return false; }
    @Override protected boolean isEnergySlot(int slot) { return slot == ENERGY_SLOT; }
    @Override public int totalProgress() { return 1; }
    @Override public int tierIndex() { return getBlockState().getBlock() instanceof AbstractTieredIoMachineBlock block ? block.tier().progressionIndex() : 7; }
    @Override public long energyPerTick() { return switch (tierIndex()) { case 7 -> 40_000L; case 8 -> 400_000L; case 9 -> 4_000_000L; default -> 40_000_000L; }; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == ENERGY_SLOT && EnergeticClayFuel.isFuel(stack); }
    @Override protected Component getDefaultName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new ClayEnergyLaserMenu(id, inventory, this, menuData()); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); tag.putInt("BeamLength", beamLength); tag.putBoolean("Irradiating", irradiating); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); beamLength = Math.max(0, Math.min(ClayLaserPath.MAX_LENGTH, tag.getInt("BeamLength"))); irradiating = tag.getBoolean("Irradiating"); }
}
