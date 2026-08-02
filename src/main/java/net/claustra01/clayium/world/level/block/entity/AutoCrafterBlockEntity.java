/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.AutoCrafterMenu;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/** Pattern-driven automatic crafting machine. */
public final class AutoCrafterBlockEntity extends AbstractConfigurableMachineBlockEntity {
    public static final int INPUT_START = 0;
    public static final int INPUT_END = 9;
    public static final int OUTPUT_START = 9;
    public static final int OUTPUT_END = 15;
    public static final int PATTERN_START = 15;
    public static final int PATTERN_END = 24;
    public static final int ENERGY_SLOT = 33;
    public static final int RETURN_START = 34;
    public static final int RETURN_END = 43;
    public static final int SLOT_COUNT = 43;

    public AutoCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.AUTO_CRAFTER_BLOCK_ENTITY.get(), pos, state, SLOT_COUNT, tier(state) >= 6);
    }

    private static int tier(BlockState state) {
        return state.getBlock() instanceof AbstractTieredIoMachineBlock block ? block.tier().progressionIndex() : 5;
    }

    @Override
    protected void tickMachine() {
        CraftingInput input = craftingInput();
        if (input.isEmpty() || level == null) { progress = 0; return; }
        Optional<RecipeHolder<CraftingRecipe>> match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) { progress = 0; return; }
        ItemStack result = match.get().value().assemble(input, level.registryAccess());
        if (result.isEmpty() || !canStore(OUTPUT_START, OUTPUT_END, result)) return;
        long cost = energyPerTick();
        if (cost > 0 && energy.extract(cost, true) != cost && !consumeFuel(ENERGY_SLOT)) return;
        energy.extract(cost, false);
        if (++progress < totalProgress()) { setChanged(); return; }
        progress = 0;
        NonNullList<ItemStack> remains = level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, input, level);
        for (int slot = 0; slot < INPUT_END; slot++) {
            if (!getItem(slot).isEmpty()) getItem(slot).shrink(1);
            if (!remains.get(slot).isEmpty()) store(RETURN_START, RETURN_END, remains.get(slot).copy());
        }
        store(OUTPUT_START, OUTPUT_END, result);
        moveReturnsToOutputs();
        setChanged();
    }

    private CraftingInput craftingInput() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int slot = 0; slot < INPUT_END; slot++) {
            ItemStack pattern = getItem(PATTERN_START + slot);
            ItemStack actual = getItem(slot);
            if (pattern.isEmpty() != actual.isEmpty()) return CraftingInput.EMPTY;
            if (!pattern.isEmpty() && !matchesPattern(pattern, actual)) return CraftingInput.EMPTY;
            grid.add(actual.isEmpty() ? ItemStack.EMPTY : actual.copyWithCount(1));
        }
        return CraftingInput.of(3, 3, grid);
    }

    public static boolean matchesPattern(ItemStack pattern, ItemStack actual) {
        return pattern.getItem() instanceof ClayFilterItem
                ? ClayFilterItem.matches(pattern, actual) : ItemStack.isSameItemSameComponents(pattern, actual);
    }

    private void moveReturnsToOutputs() {
        for (int slot = RETURN_START; slot < RETURN_END; slot++) {
            if (getItem(slot).isEmpty() || !canStore(OUTPUT_START, OUTPUT_END, getItem(slot))) continue;
            ItemStack value = getItem(slot).copy();
            inventory().set(slot, ItemStack.EMPTY);
            store(OUTPUT_START, OUTPUT_END, value);
        }
    }

    @Override protected boolean acceptsClayEnergy() { return tierIndex() >= 6; }
    @Override protected boolean isExternalInput(int slot, ItemStack stack) {
        return slot < INPUT_END && matchesPattern(getItem(PATTERN_START + slot), stack)
                || slot == ENERGY_SLOT && acceptsClayEnergy() && EnergeticClayFuel.isFuel(stack);
    }
    @Override protected boolean isExternalOutput(int slot) { return slot >= OUTPUT_START && slot < OUTPUT_END; }
    @Override protected boolean isNormalInputSlot(int slot) { return slot < INPUT_END; }
    @Override protected boolean isEnergySlot(int slot) { return slot == ENERGY_SLOT; }
    @Override public int totalProgress() { return tierIndex() >= 6 ? 1 : 20; }
    @Override public int tierIndex() { return tier(getBlockState()); }
    @Override public long energyPerTick() { return tierIndex() >= 6 ? 10 : 0; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < INPUT_END && matchesPattern(getItem(PATTERN_START + slot), stack)
                || slot >= PATTERN_START && slot < PATTERN_END
                || slot == ENERGY_SLOT && acceptsClayEnergy() && EnergeticClayFuel.isFuel(stack);
    }

    @Override protected Component getDefaultName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new AutoCrafterMenu(id, inventory, this, menuData(), tierIndex());
    }
}
