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
import net.neoforged.neoforge.items.IItemHandler;

public final class VoidContainerBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    private final int[] insertionRoutes = {-1, 0, -1, -1, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, () -> 1, () -> 0);
    private final IItemHandler unsidedHandler = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
    };
    public VoidContainerBlockEntity(BlockPos pos, BlockState state) {
        super(StorageRegistration.VOID_CONTAINER_BLOCK_ENTITY.get(), pos, state);
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
        saveIoState(tag, registries);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
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
