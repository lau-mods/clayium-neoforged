package net.claustra01.clayium.storage;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MetalChestBlockEntity extends BlockEntity implements MenuProvider {
    private final MetalChestType type;
    private final ItemStackHandler inventory;
    private final ChestLidController lid = new ChestLidController();
    private int openers;

    public MetalChestBlockEntity(BlockPos pos, BlockState state) {
        super(StorageRegistration.METAL_CHEST_BLOCK_ENTITY.get(), pos, state);
        type = ((MetalChestBlock)state.getBlock()).type();
        inventory = new ItemStackHandler(type.slots()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public MetalChestType type() {
        return type;
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public static void lidAnimateTick(
            Level level, BlockPos pos, BlockState state,
            MetalChestBlockEntity chest) {
        chest.lid.tickLid();
    }

    public float openness(float partialTick) {
        return lid.getOpenness(partialTick);
    }

    public void startOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator()) {
            return;
        }
        if (openers++ == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openers);
    }

    public void stopOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator() || openers == 0) {
            return;
        }
        if (--openers == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openers);
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id == 1) {
            lid.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(id, value);
    }

    public Container removeInventory() {
        SimpleContainer contents = new SimpleContainer(type.slots());
        for (int slot = 0; slot < type.slots(); slot++) {
            contents.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return contents;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MetalChestMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
    }
}
