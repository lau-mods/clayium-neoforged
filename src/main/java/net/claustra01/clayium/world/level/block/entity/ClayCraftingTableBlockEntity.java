/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.List;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ClayCraftingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent 3x3 crafting grid used by the original Clay Crafting Table. */
public final class ClayCraftingTableBlockEntity extends BaseContainerBlockEntity implements CraftingContainer {
    public static final int SLOT_COUNT = 9;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public ClayCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.CLAY_CRAFTING_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.clayium_neoforged.clay_crafting_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ClayCraftingTableMenu(containerId, inventory, this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack stack : items) {
            contents.accountSimpleStack(stack);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }
}
