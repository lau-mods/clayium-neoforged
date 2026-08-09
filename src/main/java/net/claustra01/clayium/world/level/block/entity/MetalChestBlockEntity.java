/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.storage.MetalStorageCatalog;
import net.claustra01.clayium.world.inventory.MetalChestMenu;
import net.claustra01.clayium.world.level.block.MetalChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class MetalChestBlockEntity extends BaseContainerBlockEntity implements LidBlockEntity {
    public static final int MAX_SLOTS = 13 * 8 * 8;
    private NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private final IItemHandler itemHandler = new InvWrapper(this);
    private final ChestLidController lid = new ChestLidController();
    private int openCount;

    public MetalChestBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.METAL_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    public MetalStorageCatalog.Chest definition() {
        return getBlockState().getBlock() instanceof MetalChestBlock chest
                ? chest.definition() : MetalStorageCatalog.CHESTS.getFirst();
    }

    public IItemHandler itemHandler() { return itemHandler; }
    public static void clientTick(MetalChestBlockEntity chest) { chest.lid.tickLid(); }
    @Override public float getOpenNess(float partialTick) { return lid.getOpenness(partialTick); }
    @Override protected Component getDefaultName() { return getBlockState().getBlock().getName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new MetalChestMenu(id, inventory, this); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> stacks) { items = stacks; }
    @Override public int getContainerSize() { return definition().slots(); }

    @Override
    public void startOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator() || isRemoved()) return;
        if (openCount++ == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                    0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
    }

    @Override
    public void stopOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator() || isRemoved()) return;
        openCount = Math.max(0, openCount - 1);
        if (openCount == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
                    0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
    }

    @Override
    public boolean triggerEvent(int id, int parameter) {
        if (id == 1) {
            lid.shouldBeOpen(parameter > 0);
            return true;
        }
        return super.triggerEvent(id, parameter);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }
}
