/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.AutoClayCondenserMenu;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Original automatic decimal clay condenser. */
public final class AutoClayCondenserBlockEntity extends AbstractConfigurableMachineBlockEntity {
    public static final int STORAGE_END = 20;
    public static final int LIMIT_SLOT = 21;
    public static final int SLOT_COUNT = 22;

    public AutoClayCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.AUTO_CLAY_CONDENSER_BLOCK_ENTITY.get(), pos, state, SLOT_COUNT, false);
    }

    @Override
    protected void tickMachine() {
        int maximum = clayLevel(getItem(LIMIT_SLOT));
        if (maximum < 1) maximum = 13;
        for (int level = maximum - 1; level >= 0; level--) {
            if (countClay(level) >= 9 && canStore(0, STORAGE_END, clay(level + 1))) {
                consumeClay(level, 9);
                store(0, STORAGE_END, clay(level + 1));
                sortClay();
                setChanged();
                return;
            }
        }
    }

    @Override protected boolean acceptsClayEnergy() { return false; }
    @Override protected boolean isExternalInput(int slot, ItemStack stack) {
        return slot < 15 && clayLevel(stack) >= 0 && clayLevel(stack) >= clayLevel(getItem(LIMIT_SLOT));
    }
    @Override protected boolean isExternalOutput(int slot) { return slot < STORAGE_END; }
    @Override protected boolean isNormalInputSlot(int slot) { return slot < 15; }
    @Override protected boolean isEnergySlot(int slot) { return false; }
    @Override public int totalProgress() { return 1; }
    @Override public int tierIndex() {
        return getBlockState().getBlock() instanceof AbstractTieredIoMachineBlock block
                ? block.tier().progressionIndex() : 5;
    }
    @Override public long energyPerTick() { return 0; }

    @Override
    protected long displayedEnergy() {
        long total = 0;
        for (int level = 0; level <= 13; level++) total += pow10(level) * countClay(level);
        return total;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < STORAGE_END && clayLevel(stack) >= 0 || slot == LIMIT_SLOT && clayLevel(stack) >= 0;
    }

    @Override protected Component getDefaultName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new AutoClayCondenserMenu(id, inventory, this, menuData());
    }

    private int countClay(int level) {
        int result = 0;
        for (int slot = 0; slot < STORAGE_END; slot++) if (clayLevel(getItem(slot)) == level) result += getItem(slot).getCount();
        return result;
    }

    private void consumeClay(int level, int amount) {
        for (int slot = 0; slot < STORAGE_END && amount > 0; slot++) {
            if (clayLevel(getItem(slot)) != level) continue;
            int consumed = Math.min(amount, getItem(slot).getCount());
            getItem(slot).shrink(consumed);
            amount -= consumed;
        }
    }

    private void sortClay() {
        List<ItemStack> sorted = new ArrayList<>();
        for (int level = 0; level <= 13; level++) {
            int count = countClay(level);
            while (count > 0) {
                int stackSize = Math.min(64, count);
                sorted.add(clay(level).copyWithCount(stackSize));
                count -= stackSize;
            }
        }
        for (int slot = 0; slot < STORAGE_END; slot++) {
            inventory().set(slot, slot < sorted.size() ? sorted.get(slot) : ItemStack.EMPTY);
        }
    }

    private static long pow10(int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) result = Math.multiplyExact(result, 10L);
        return result;
    }

    private static ItemStack clay(int value) {
        if (value == 0) return new ItemStack(Items.CLAY);
        if (value == 1) return new ItemStack(ClayiumRegistries.DENSE_CLAY.get());
        if (value == 2) return new ItemStack(ClayiumRegistries.COMPRESSED_CLAY.get());
        String[] ids = {"industrial_clay", "advanced_industrial_clay", "energetic_clay",
                "compressed_energetic_clay", "double_compressed_energetic_clay",
                "triple_compressed_energetic_clay", "quadruple_compressed_energetic_clay",
                "quintuple_compressed_energetic_clay", "sextuple_compressed_energetic_clay",
                "septuple_compressed_energetic_clay", "octuple_compressed_energetic_clay"};
        return value - 3 < ids.length
                ? new ItemStack(ClayiumRegistries.COMPRESSED_CLAY_BLOCKS.get(ids[value - 3]).get()) : ItemStack.EMPTY;
    }

    public static int clayLevel(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int level = 0; level <= 13; level++) if (ItemStack.isSameItem(stack, clay(level))) return level;
        return -1;
    }
}
