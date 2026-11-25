package net.claustra01.clayium.storage;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.items.ItemStackHandler;

public final class StorageContainerBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    public static final long BASIC_CAPACITY = 65_536L;
    public static final long EXPANDED_CAPACITY = Integer.MAX_VALUE;

    private ItemStack stored = ItemStack.EMPTY;
    private long amount;
    private boolean expanded;
    private final IItemHandler handler = new LongItemHandler();
    private final int[] insertionRoutes = {-1, 0, -1, -1, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, () -> 1, () -> 1);
    private final java.util.EnumMap<Direction, IItemHandler> sidedHandlers = new java.util.EnumMap<>(Direction.class);
    private final ItemStackHandler memoryInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            syncVisual();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private final ContainerData menuData = new StorageData();

    public StorageContainerBlockEntity(BlockPos pos, BlockState state) {
        super(StorageRegistration.STORAGE_CONTAINER_BLOCK_ENTITY.get(), pos, state);
        for (Direction direction : Direction.values()) sidedHandlers.put(direction, new SidedHandler(direction));
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

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? handler : sidedHandlers.get(direction);
    }

    public IItemHandler itemHandler() {
        return handler;
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() { return StorageContainerBlock.PIPE; }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return StorageContainerBlock.FACING; }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        syncVisual();
        ioNeighborsChanged();
    }

    public ItemStackHandler memoryInventory() {
        return memoryInventory;
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
        tag.put("memory", memoryInventory.serializeNBT(registries));
        tag.putIntArray("insertion_routes", insertionRoutes);
        tag.putIntArray("extraction_routes", extractionRoutes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = tag.contains("stored") ? ItemStack.parseOptional(registries, tag.getCompound("stored")) : ItemStack.EMPTY;
        expanded = tag.getBoolean("expanded");
        amount = tag.getLong("amount");
        memoryInventory.setStackInSlot(0, ItemStack.EMPTY);
        if (tag.contains("memory") && validHandlerSize(tag.getCompound("memory"), 1)) {
            memoryInventory.deserializeNBT(registries, tag.getCompound("memory"));
        }
        sanitizeMemory();
        if (tag.contains("insertion_routes") || tag.contains("extraction_routes")) {
            sideConfiguration.replaceRoutes(tag.getIntArray("insertion_routes"), tag.getIntArray("extraction_routes"));
        }
        if (amount == 0L) {
            stored = ItemStack.EMPTY;
        } else if (!stored.isEmpty()) {
            stored.setCount(1);
        }
    }

    private final class SidedHandler implements IItemHandler {
        private final Direction direction;
        private SidedHandler(Direction direction) { this.direction = direction; }
        @Override public int getSlots() { return insertionRoute(direction) >= 0 || extractionRoute(direction) >= 0 ? 1 : 0; }
        @Override public ItemStack getStackInSlot(int slot) { return handler.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return insertionRoute(direction) >= 0 ? handler.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return extractionRoute(direction) >= 0 ? handler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return handler.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return insertionRoute(direction) >= 0 && handler.isItemValid(slot, stack);
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
        tag.put("memory", memoryInventory.serializeNBT(registries));
        return tag;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        CompoundTag contents = new CompoundTag();
        if (!stored.isEmpty()) contents.put("stored", stored.copyWithCount(1).save(level.registryAccess()));
        contents.putLong("amount", amount);
        contents.putBoolean("expanded", expanded);
        contents.put("memory", memoryInventory.serializeNBT(level.registryAccess()));
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
        memoryInventory.setStackInSlot(0, ItemStack.EMPTY);
        if (contents.contains("memory") && validHandlerSize(contents.getCompound("memory"), 1)) {
            memoryInventory.deserializeNBT(level.registryAccess(), contents.getCompound("memory"));
        }
        sanitizeMemory();
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
            if (stack.isEmpty() || !memoryMatches(stack)
                    || !stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, stack)) {
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
            return memoryMatches(stack)
                    && (stored.isEmpty() || ItemStack.isSameItemSameComponents(stored, stack));
        }

    }

    private boolean memoryMatches(ItemStack stack) {
        ItemStack memory = memoryInventory.getStackInSlot(0);
        return memory.isEmpty() || ItemStack.isSameItemSameComponents(memory, stack);
    }

    private void sanitizeMemory() {
        ItemStack memory = memoryInventory.getStackInSlot(0);
        if (!memory.isEmpty() && memory.getCount() != 1) {
            memoryInventory.setStackInSlot(0, memory.copyWithCount(1));
        }
    }

    private static boolean validHandlerSize(CompoundTag tag, int expected) {
        return !tag.contains("Size") || tag.getInt("Size") == expected;
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
            long value = index < 4 ? amount : capacity();
            return (int)(value >>> ((index % 4) * 16)) & 0xFFFF;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 8;
        }
    }
}
