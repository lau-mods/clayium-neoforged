/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.machine.ChemicalMetalSeparatorProcess;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ChemicalMetalSeparatorMenu;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Weighted industrial-clay dust separator. */
public final class ChemicalMetalSeparatorBlockEntity extends AbstractConfigurableMachineBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_START = 1;
    public static final int OUTPUT_END = 17;
    public static final int INTERNAL_SLOT = 17;
    public static final int ENERGY_SLOT = 18;
    public static final int SLOT_COUNT = 19;
    private static final long CE_PER_TICK = 5_000;
    private static final int PROCESS_TICKS = 40;

    public ChemicalMetalSeparatorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.CHEMICAL_METAL_SEPARATOR_BLOCK_ENTITY.get(), pos, state, SLOT_COUNT, true);
    }

    @Override
    protected void tickMachine() {
        if (getItem(INTERNAL_SLOT).isEmpty()) {
            if (!isIndustrialClayDust(getItem(INPUT_SLOT)) || !allOutputsFit()) { progress = 0; return; }
            getItem(INPUT_SLOT).shrink(1);
            inventory().set(INTERNAL_SLOT, component("industrial_clay_dust"));
        }
        if (!allOutputsFit()) return;
        if (energy.extract(CE_PER_TICK, true) != CE_PER_TICK && !consumeFuel(ENERGY_SLOT)) return;
        energy.extract(CE_PER_TICK, false);
        if (++progress < PROCESS_TICKS) { setChanged(); return; }
        progress = 0;
        inventory().set(INTERNAL_SLOT, ItemStack.EMPTY);
        store(OUTPUT_START, OUTPUT_END, ChemicalMetalSeparatorProcess.select(level.random));
        setChanged();
    }

    private boolean allOutputsFit() {
        for (var product : ChemicalMetalSeparatorProcess.PRODUCTS) {
            if (!canStore(OUTPUT_START, OUTPUT_END, product.stack())) return false;
        }
        return true;
    }

    @Override protected boolean acceptsClayEnergy() { return true; }
    @Override protected boolean isExternalInput(int slot, ItemStack stack) {
        return slot == INPUT_SLOT && isIndustrialClayDust(stack)
                || slot == ENERGY_SLOT && EnergeticClayFuel.isFuel(stack);
    }
    @Override protected boolean isExternalOutput(int slot) { return slot >= OUTPUT_START && slot < OUTPUT_END; }
    @Override protected boolean isNormalInputSlot(int slot) { return slot == INPUT_SLOT; }
    @Override protected boolean isEnergySlot(int slot) { return slot == ENERGY_SLOT; }
    @Override public int totalProgress() { return PROCESS_TICKS; }
    @Override public int tierIndex() {
        return getBlockState().getBlock() instanceof AbstractTieredIoMachineBlock block
                ? block.tier().progressionIndex() : 6;
    }
    @Override public long energyPerTick() { return CE_PER_TICK; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT && isIndustrialClayDust(stack)
                || slot == ENERGY_SLOT && EnergeticClayFuel.isFuel(stack);
    }

    @Override protected Component getDefaultName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChemicalMetalSeparatorMenu(id, inventory, this, menuData());
    }

    private static ItemStack component(String id) {
        return new ItemStack(ClayiumRegistries.COMPONENT_ITEMS.get(id).get());
    }

    private static boolean isIndustrialClayDust(ItemStack stack) {
        return ItemStack.isSameItem(stack, component("industrial_clay_dust"));
    }
}
