package net.claustra01.clayium.storage;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public final class StorageContainerBlockEntity extends BlockEntity implements MenuProvider {
    public static final long BASIC_CAPACITY = 65_536L;
    public static final long EXPANDED_CAPACITY = Integer.MAX_VALUE;

    private ItemStack stored = ItemStack.EMPTY;
    private long amount;
    private boolean expanded;
    private final IItemHandler handler = new LongItemHandler();
    private final ContainerData menuData = new StorageData();

    public StorageContainerBlockEntity(BlockPos pos, BlockState state) {
        super(StorageRegistration.STORAGE_CONTAINER_BLOCK_ENTITY.get(), pos, state);
    }

    public long amount() {
        return amount;
    }

    public long capacity() {
        return expanded ? EXPANDED_CAPACITY : BASIC_CAPACITY;
    }

    public boolean expanded() {
        return expanded;
    }

    public ItemStack storedItem() {
        return stored.copy();
    }

    public IItemHandler itemHandler() {
        return handler;
    }

    public boolean applyClayCore() {
        if (expanded) {
            return false;
        }
        expanded = true;
        syncVisual();
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(StorageContainerBlock.EXPANDED, true), 3);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StorageContainerMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!stored.isEmpty()) {
            tag.put("stored", stored.copyWithCount(1).save(registries));
        }
        tag.putLong("amount", amount);
        tag.putBoolean("expanded", expanded);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = tag.contains("stored") ? ItemStack.parseOptional(registries, tag.getCompound("stored")) : ItemStack.EMPTY;
        expanded = tag.getBoolean("expanded");
        amount = tag.getLong("amount");
        if (amount == 0L) {
            stored = ItemStack.EMPTY;
        } else if (!stored.isEmpty()) {
            stored.setCount(1);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (!stored.isEmpty()) {
            tag.put("stored", stored.copyWithCount(1).save(registries));
        }
        tag.putLong("amount", amount);
        tag.putBoolean("expanded", expanded);
        return tag;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        CompoundTag contents = new CompoundTag();
        if (!stored.isEmpty()) contents.put("stored", stored.copyWithCount(1).save(level.registryAccess()));
        contents.putLong("amount", amount);
        contents.putBoolean("expanded", expanded);
        components.set(DataComponents.CUSTOM_DATA, CustomData.of(contents));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        CustomData data = input.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag contents = data.copyTag();
        stored = contents.contains("stored") ? ItemStack.parseOptional(level.registryAccess(), contents.getCompound("stored")) : ItemStack.EMPTY;
        amount = contents.getLong("amount");
        expanded = contents.getBoolean("expanded");
    }

    private final class LongItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stored.isEmpty() ? ItemStack.EMPTY
                    : stored.copyWithCount((int)Math.min(amount, stored.getMaxStackSize()));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, stack)) {
                return stack;
            }
            int accepted = (int)Math.min((long)stack.getCount(), capacity() - amount);
            if (accepted <= 0) {
                return stack;
            }
            if (!simulate) {
                if (stored.isEmpty()) {
                    stored = stack.copyWithCount(1);
                }
                amount += accepted;
                syncVisual();
            }
            return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int requested, boolean simulate) {
            if (requested <= 0 || stored.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = (int)Math.min(Math.min((long)requested, amount), stored.getMaxStackSize());
            ItemStack result = stored.copyWithCount(extracted);
            if (!simulate) {
                amount -= extracted;
                if (amount == 0L) {
                    stored = ItemStack.EMPTY;
                }
                syncVisual();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return stored.isEmpty() ? 64 : stored.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stored.isEmpty() || ItemStack.isSameItemSameComponents(stored, stack);
        }

    }

    private void syncVisual() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private final class StorageData implements ContainerData {
        @Override
        public int get(int index) {
            if (index == 8) return expanded ? 1 : 0;
            long value = index < 4 ? amount : capacity();
            return (int)(value >>> ((index % 4) * 16)) & 0xFFFF;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 9;
        }
    }
}
