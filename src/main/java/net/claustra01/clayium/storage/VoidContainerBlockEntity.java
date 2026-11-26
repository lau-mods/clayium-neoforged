package net.claustra01.clayium.storage;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.IoTransportKind;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class VoidContainerBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    private final int[] insertionRoutes = {-1, 0, -1, -1, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, () -> 1, () -> 0);
    /** Original TileVacuumContainer filter SlotMemory: a remembered one-item copy. */
    private final ItemStackHandler memory = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public int getSlotLimit(int slot) { return 1; }
    };
    private final IItemHandler unsidedHandler = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return accepts(stack) ? ItemStack.EMPTY : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return accepts(stack); }
    };
    public VoidContainerBlockEntity(BlockPos pos, BlockState state) {
        super(StorageRegistration.VOID_CONTAINER_BLOCK_ENTITY.get(), pos, state);
    }

    /** TileVacuumContainer.checkFilterSlot: same item type or a matching remembered filter. */
    public boolean accepts(ItemStack stack) {
        return ItemFilter.matchesMemory(memory.getStackInSlot(0), stack);
    }

    public ItemStackHandler memoryInventory() {
        return memory;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return unsidedHandler;
    }

    public IItemHandler itemHandler() {
        return unsidedHandler;
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() { return VoidContainerBlock.PIPE; }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return VoidContainerBlock.FACING; }
    @Override public boolean allowsPassiveInsertion(Direction side, IoTransportKind kind) {
        return kind == IoTransportKind.ITEM;
    }
    @Override public boolean allowsPassiveExtraction(Direction side, IoTransportKind kind) {
        return kind == IoTransportKind.ITEM;
    }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        ioNeighborsChanged();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("filter", memory.serializeNBT(registries));
        saveIoState(tag, registries);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("filter")) {
            memory.deserializeNBT(registries, tag.getCompound("filter"));
        }
        loadIoState(tag, registries);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new VoidContainerMenu(containerId, playerInventory, this);
    }
}
