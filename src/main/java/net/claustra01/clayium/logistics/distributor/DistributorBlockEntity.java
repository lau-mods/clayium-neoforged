package net.claustra01.clayium.logistics.distributor;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class DistributorBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    /** Original TileClayDistributor: each colony is a 2x2 inventory. */
    public static final int COLONY_COLUMNS = 2;
    public static final int COLONY_ROWS = 2;
    public static final int COLONY_SLOTS = COLONY_COLUMNS * COLONY_ROWS;

    private final ItemStackHandler inventory;
    private int inputColony;
    private int outputColony;
    private boolean wrapped;
    private final int[] insertionRoutes = {-1, -1, -1, 0, -1, -1};
    private final int[] extractionRoutes = {0, 0, 0, -1, 0, 0};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, () -> 1, () -> 1);
    private int nextOutputDirection;
    private int nextInputDirection;
    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches;

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(MultitrackRegistration.DISTRIBUTOR_BLOCK_ENTITY.get(), pos, state);
        int tier = ((DistributorBlock)state.getBlock()).tier();
        inventory = new ItemStackHandler(colonyColumns(tier) * colonyRows(tier) * COLONY_SLOTS) {
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    /** Original inventoryColonyX by tier. */
    public static int colonyColumns(int tier) {
        return switch (tier) { case 7 -> 2; case 8 -> 3; default -> 4; };
    }

    /** Original inventoryColonyY by tier. */
    public static int colonyRows(int tier) {
        return switch (tier) { case 7, 8 -> 2; default -> 3; };
    }

    public int colonyCount() {
        return inventory.getSlots() / COLONY_SLOTS;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistributorBlockEntity distributor) {
        distributor.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureCaches(serverLevel);
        advanceColonies();
        int maximum = switch (tier()) { case 7 -> 64; case 8 -> 128; default -> 512; };
        distribute(maximum);
        pull(maximum);
    }

    /**
     * Original TileClayDistributor.updateEntity: input fills one colony per tick and moves on once
     * it holds anything; output drains one colony until empty. When input laps output the
     * distributor is crowded and accepts nothing until output advances.
     */
    private void advanceColonies() {
        int colonies = colonyCount();
        int previousInput = inputColony;
        int previousOutput = outputColony;
        boolean previouslyWrapped = wrapped;
        if (inputColony != outputColony || !wrapped) {
            if (!colonyEmpty(inputColony) && ++inputColony >= colonies) {
                inputColony = 0;
                wrapped = true;
            }
        }
        if (inputColony != outputColony || wrapped) {
            if (colonyEmpty(outputColony) && ++outputColony >= colonies) {
                outputColony = 0;
                wrapped = false;
            }
        }
        if ((previousInput != inputColony || previousOutput != outputColony || previouslyWrapped != wrapped)
                && level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean colonyEmpty(int colony) {
        for (int slot = colony * COLONY_SLOTS; slot < (colony + 1) * COLONY_SLOTS; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    /** Original func_94041_b: only the current input colony accepts items. */
    public boolean acceptsSlot(int slot) {
        return !crowded() && slot / COLONY_SLOTS == inputColony;
    }

    public boolean crowded() {
        return wrapped && inputColony == outputColony;
    }

    private final IItemHandler inputView = new ColonyView(true);
    private final IItemHandler outputView = new ColonyView(false);

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
                    outputView, target, stack -> true, 1, this::recover);
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
            int moved = BufferBlockEntity.moveItems(source, inputView, stack -> true, remaining, this::recover);
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
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        ioNeighborsChanged();
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return new IItemHandler() {
            @Override public int getSlots() { return direction != null
                    && insertionRoute(direction) < 0 && extractionRoute(direction) < 0 ? 0 : COLONY_SLOTS * 2; }
            @Override public ItemStack getStackInSlot(int slot) {
                return slot < COLONY_SLOTS ? inputView.getStackInSlot(slot) : outputView.getStackInSlot(slot - COLONY_SLOTS);
            }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return slot < COLONY_SLOTS && (direction == null || insertionRoute(direction) >= 0)
                        ? inputView.insertItem(slot, stack, simulate) : stack;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return slot >= COLONY_SLOTS && (direction == null || extractionRoute(direction) >= 0)
                        ? outputView.extractItem(slot - COLONY_SLOTS, amount, simulate) : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(0); }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return slot < COLONY_SLOTS && !crowded();
            }
        };
    }

    /** Four-slot window onto the current input or output colony. */
    private final class ColonyView implements IItemHandler {
        private final boolean input;

        private ColonyView(boolean input) {
            this.input = input;
        }

        private int base() {
            return (input ? inputColony : outputColony) * COLONY_SLOTS;
        }

        @Override public int getSlots() { return COLONY_SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(base() + slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return input && !crowded() ? inventory.insertItem(base() + slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return input ? ItemStack.EMPTY : inventory.extractItem(base() + slot, amount, simulate);
        }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(base() + slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return input && !crowded(); }
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropped.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return dropped;
    }

    @Override public Component getDisplayName() { return getBlockState().getBlock().getName(); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DistributorMenu(id, inventory, this);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        saveIoState(tag, registries);
        tag.putInt("next_output_direction", nextOutputDirection);
        tag.putInt("next_input_direction", nextInputDirection);
        tag.putInt("input_colony", inputColony);
        tag.putInt("output_colony", outputColony);
        tag.putBoolean("wrapped", wrapped);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag saved = tag.getCompound("inventory").copy();
        saved.putInt("Size", inventory.getSlots());
        inventory.deserializeNBT(registries, saved);
        loadIoState(tag, registries);
        nextOutputDirection = tag.getInt("next_output_direction");
        nextInputDirection = tag.getInt("next_input_direction");
        inputColony = Math.floorMod(tag.getInt("input_colony"), colonyCount());
        outputColony = Math.floorMod(tag.getInt("output_colony"), colonyCount());
        wrapped = tag.getBoolean("wrapped");
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

}
