package net.claustra01.clayium.logistics.distributor;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class DistributorBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    public static final int SLOTS = 9;
    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final int[] insertionRoutes = {-1, -1, -1, 0, -1, -1};
    private final int[] extractionRoutes = {0, 0, 0, -1, 0, 0};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, () -> 1, () -> 1);
    private final ContainerData menuData = new DistributorData();
    private int nextOutputDirection;
    private int nextInputDirection;
    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches;

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(MultitrackRegistration.DISTRIBUTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistributorBlockEntity distributor) {
        distributor.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureCaches(serverLevel);
        int maximum = switch (tier()) { case 7 -> 64; case 8 -> 128; default -> 512; };
        distribute(maximum);
        pull(maximum);
    }

    private void distribute(int maximum) {
        int moved = 0;
        int failures = 0;
        while (moved < maximum && failures < Direction.values().length) {
            Direction direction = Direction.values()[nextOutputDirection];
            nextOutputDirection = (nextOutputDirection + 1) % Direction.values().length;
            if (extractionRoute(direction) < 0) {
                failures++;
                continue;
            }
            IItemHandler target = neighborCaches.get(direction).getCapability();
            int sent = target == null ? 0 : BufferBlockEntity.moveItems(
                    inventory, target, stack -> true, 1, this::recover);
            if (sent == 0) failures++; else { moved += sent; failures = 0; }
        }
    }

    private void pull(int maximum) {
        int remaining = maximum;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int index = (nextInputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (insertionRoute(direction) < 0) continue;
            IItemHandler source = neighborCaches.get(direction).getCapability();
            if (source == null) continue;
            int moved = BufferBlockEntity.moveItems(source, inventory, stack -> true, remaining, this::recover);
            if (moved > 0) {
                remaining -= moved;
                nextInputDirection = (index + 1) % Direction.values().length;
            }
        }
    }

    private void ensureCaches(ServerLevel serverLevel) {
        if (neighborCaches != null) return;
        neighborCaches = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            neighborCaches.put(direction, BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK,
                    serverLevel, worldPosition.relative(direction), direction.getOpposite(),
                    () -> !isRemoved(), () -> {}));
        }
    }

    private void recover(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < inventory.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = inventory.insertItem(slot, remaining, false);
        }
        if (!remaining.isEmpty() && level != null) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, remaining);
        }
    }

    public int tier() { return ((DistributorBlock)getBlockState().getBlock()).tier(); }
    public ItemStackHandler inventory() { return inventory; }
    public BufferBlockEntity.FaceMode faceMode(Direction direction) {
        boolean input = insertionRoute(direction) >= 0;
        boolean output = extractionRoute(direction) >= 0;
        if (input && output) return BufferBlockEntity.FaceMode.INPUT_OUTPUT;
        if (input) return BufferBlockEntity.FaceMode.INPUT;
        if (output) return BufferBlockEntity.FaceMode.OUTPUT;
        return BufferBlockEntity.FaceMode.DISABLED;
    }
    public void setFaceMode(Direction direction, BufferBlockEntity.FaceMode mode) {
        sideConfiguration.setRoutes(ioFacing(), direction,
                mode.canInput() ? 0 : -1, mode.canOutput() ? 0 : -1);
        ioConfigurationChanged();
    }
    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() { return DistributorBlock.PIPE; }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return DistributorBlock.FACING; }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChanged();
        ioNeighborsChanged();
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return new IItemHandler() {
            @Override public int getSlots() { return direction != null
                    && insertionRoute(direction) < 0 && extractionRoute(direction) < 0 ? 0 : SLOTS; }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return direction == null || insertionRoute(direction) >= 0
                        ? inventory.insertItem(slot, stack, simulate) : stack;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return direction == null || extractionRoute(direction) >= 0
                        ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++) {
            dropped.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return dropped;
    }

    @Override public Component getDisplayName() { return getBlockState().getBlock().getName(); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DistributorMenu(id, inventory, this, menuData);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putIntArray("insertion_routes", insertionRoutes);
        tag.putIntArray("extraction_routes", extractionRoutes);
        tag.putInt("next_output_direction", nextOutputDirection);
        tag.putInt("next_input_direction", nextInputDirection);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.contains("insertion_routes") || tag.contains("extraction_routes")) {
            sideConfiguration.replaceRoutes(tag.getIntArray("insertion_routes"), tag.getIntArray("extraction_routes"));
        } else {
            CompoundTag savedFaces = tag.getCompound("faces");
            for (Direction direction : Direction.values()) {
                if (!savedFaces.contains(direction.getName())) continue;
                setFaceMode(direction, BufferBlockEntity.FaceMode.valueOf(savedFaces.getString(direction.getName())));
            }
        }
        nextOutputDirection = tag.getInt("next_output_direction");
        nextInputDirection = tag.getInt("next_input_direction");
    }

    private final class DistributorData implements ContainerData {
        @Override public int get(int index) {
            return index == 0 ? tier() : index >= 1 && index <= 6
                    ? faceMode(Direction.values()[index - 1]).ordinal() : 0;
        }
        @Override public void set(int index, int value) { }
        @Override public int getCount() { return 7; }
    }
}
