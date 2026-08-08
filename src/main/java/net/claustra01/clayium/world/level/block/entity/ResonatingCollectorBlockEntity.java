/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.machine.ResonanceField;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ResonatingCollectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/** Passive 3x3 antimatter collector driven only by nearby resonance. */
public final class ResonatingCollectorBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 9;
    public static final double BASE_WORK = 10_000.0D;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private double progress;
    private final ContainerData menuData=new ContainerData(){
        @Override public int get(int index){return switch(index){
            case 0->(int)Math.min(10_000,Math.round(progress/BASE_WORK*10_000.0D));
            case 1->(int)Math.min(Integer.MAX_VALUE,Math.round((ResonanceField.at(level,worldPosition))*1_000.0D));
            default->0;};}
        @Override public void set(int index,int value){}
        @Override public int getCount(){return 2;}
    };
    private final IItemHandler extraction = new IItemHandler() {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public ItemStack getStackInSlot(int slot) { return getItem(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = getItem(slot);
            int count = Math.min(Math.max(0, amount), current.getCount());
            if (count == 0) return ItemStack.EMPTY;
            ItemStack result = current.copyWithCount(count);
            if (!simulate) removeItem(slot, count);
            return result;
        }
        @Override public int getSlotLimit(int slot) { return getMaxStackSize(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    };

    public ResonatingCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.RESONATING_COLLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonatingCollectorBlockEntity collector) {
        collector.progress += Math.max(0.0D, ResonanceField.at(level, pos) - 1.0D);
        boolean changed = false;
        while (collector.progress >= BASE_WORK && collector.insertAntimatter()) {
            collector.progress -= BASE_WORK;
            changed = true;
        }
        if (changed) collector.setChanged();
    }

    private boolean insertAntimatter() {
        ItemStack antimatter = ClayiumRegistries.MATERIAL_ITEMS.get("antimatter").get().getDefaultInstance();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack current = items.get(slot);
            if (current.isEmpty()) { items.set(slot, antimatter); return true; }
            if (ItemStack.isSameItemSameComponents(current, antimatter) && current.getCount() < current.getMaxStackSize()) {
                current.grow(1); return true;
            }
        }
        return false;
    }

    public IItemHandler extractionHandler() { return extraction; }
    @Override protected Component getDefaultName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ResonatingCollectorMenu(id, inventory, this,menuData);
    }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> values) { items = values; }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = Math.max(0.0D, tag.getDouble("Progress"));
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putDouble("Progress", progress);
    }
}
