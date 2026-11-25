package net.claustra01.clayium.storage;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
    private final java.util.EnumMap<Direction, IItemHandler> sidedHandlers = new java.util.EnumMap<>(Direction.class);

    public VoidContainerBlockEntity(BlockPos pos, BlockState state) {
        super(StorageRegistration.VOID_CONTAINER_BLOCK_ENTITY.get(), pos, state);
        for (Direction direction : Direction.values()) sidedHandlers.put(direction, new SidedHandler(direction));
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    public IItemHandler itemHandler() {
        return unsidedHandler;
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() { return VoidContainerBlock.PIPE; }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return VoidContainerBlock.FACING; }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChanged();
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

    private final class SidedHandler implements IItemHandler {
        private final Direction direction;
        private SidedHandler(Direction direction) { this.direction = direction; }
        @Override public int getSlots() { return insertionRoute(direction) >= 0 ? 1 : 0; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return insertionRoute(direction) >= 0 ? ItemStack.EMPTY : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return insertionRoute(direction) >= 0; }
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
