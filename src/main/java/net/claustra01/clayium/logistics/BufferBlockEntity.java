package net.claustra01.clayium.logistics;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Original TileClayBuffer: a tier-sized inventory moved through configured routes by
 * {@link TierManager#bufferTransport(int)}. Faces and per-face filters are set with tools.
 */
public class BufferBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    private final ItemStackHandler inventory;
    private final int[] insertionRoutes = {-1, -1, -1, 0, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final ItemStack[] sideFilters = new ItemStack[RelativeFace.COUNT];
    private final SideConfiguration sideConfiguration;
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final IItemHandler unsidedHandler = new BufferItemHandler(null);

    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches;
    private int insertTick;
    private int extractTick;
    private int nextOutputDirection;
    private int nextInputDirection;

    public BufferBlockEntity(BlockPos pos, BlockState state) {
        this(LogisticsRegistration.BUFFER_BLOCK_ENTITY.get(), pos, state);
    }

    protected BufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        BufferBlock block = (BufferBlock)state.getBlock();
        inventory = new ItemStackHandler(block.inventoryColumns() * block.inventoryRows()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return acceptsInput();
            }
        };
        java.util.Arrays.fill(sideFilters, ItemStack.EMPTY);
        sideConfiguration = new SideConfiguration(
                insertionRoutes, extractionRoutes, sideFilters, () -> 1, () -> 1, ItemFilter::accepts);
        for (Direction direction : Direction.values()) {
            sidedHandlers.put(direction, new BufferItemHandler(direction));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BufferBlockEntity buffer) {
        buffer.tickServer();
    }

    protected final void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        TierManager.TransportParameters parameters = TierManager.bufferTransport(tier());
        insertTick++;
        extractTick++;
        boolean insertDue = insertTick >= parameters.insertIntervalTicks();
        boolean extractDue = extractTick >= parameters.extractIntervalTicks();
        if (!insertDue && !extractDue) {
            return;
        }
        if (insertDue) {
            insertTick = 0;
        }
        if (extractDue) {
            extractTick = 0;
        }
        ensureCaches(serverLevel);
        if (extractDue) {
            pushItems(parameters.maximumExtractItems());
        }
        if (insertDue && acceptsInput()) {
            pullItems(parameters.maximumInsertItems());
        }
    }

    protected boolean acceptsInput() {
        return true;
    }

    private void ensureCaches(ServerLevel serverLevel) {
        if (neighborCaches != null) {
            return;
        }
        neighborCaches = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            neighborCaches.put(direction, BlockCapabilityCache.create(
                    Capabilities.ItemHandler.BLOCK,
                    serverLevel,
                    worldPosition.relative(direction),
                    direction.getOpposite(),
                    () -> !isRemoved(),
                    () -> {}));
        }
    }

    private void pushItems(int maximum) {
        int remaining = maximum;
        int start = nextOutputDirection;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int index = (start + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (extractionRoute(direction) < 0) {
                continue;
            }
            IItemHandler target = neighborCaches.get(direction).getCapability();
            if (target == null) {
                continue;
            }
            int moved = moveItems(inventory, target, stack -> matchesFilter(direction, stack),
                    remaining, this::dropRemainder);
            if (moved > 0) {
                remaining -= moved;
                nextOutputDirection = (index + 1) % Direction.values().length;
            }
        }
    }

    private void pullItems(int maximum) {
        int remaining = maximum;
        int start = nextInputDirection;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int index = (start + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (insertionRoute(direction) < 0) {
                continue;
            }
            IItemHandler source = neighborCaches.get(direction).getCapability();
            if (source == null) {
                continue;
            }
            int moved = moveItems(source, inventory, stack -> matchesFilter(direction, stack),
                    remaining, this::dropRemainder);
            if (moved > 0) {
                remaining -= moved;
                nextInputDirection = (index + 1) % Direction.values().length;
            }
        }
    }

    public static int moveItems(
            IItemHandler source, IItemHandler target, Predicate<ItemStack> filter, int maximum,
            Consumer<ItemStack> onUnrestored) {
        int moved = 0;
        for (int sourceSlot = 0; sourceSlot < source.getSlots() && moved < maximum; sourceSlot++) {
            while (moved < maximum) {
                ItemStack offered = source.extractItem(sourceSlot, maximum - moved, true);
                if (offered.isEmpty() || !filter.test(offered)) {
                    break;
                }
                int accepted = offered.getCount() - insertAcross(target, offered, true).getCount();
                if (accepted <= 0) {
                    break;
                }
                ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
                if (extracted.isEmpty()) {
                    break;
                }
                ItemStack rejected = insertAcross(target, extracted, false);
                moved += extracted.getCount() - rejected.getCount();
                if (!rejected.isEmpty()) {
                    ItemStack unrestored = insertAcross(source, rejected, false);
                    if (!unrestored.isEmpty()) {
                        onUnrestored.accept(unrestored);
                    }
                    break;
                }
            }
        }
        return moved;
    }

    private void dropRemainder(ItemStack stack) {
        ItemStack stored = insertAcross(inventory, stack, false);
        if (!stored.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, stored);
        }
    }

    private static ItemStack insertAcross(IItemHandler target, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < target.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = target.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    public int tier() {
        return ((BufferBlock)getBlockState().getBlock()).tier();
    }

    public int inventoryColumns() {
        return ((BufferBlock)getBlockState().getBlock()).inventoryColumns();
    }

    public int inventoryRows() {
        return ((BufferBlock)getBlockState().getBlock()).inventoryRows();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public FaceMode faceMode(Direction direction) {
        return switch (sideMode(direction)) {
            case INPUT -> FaceMode.INPUT;
            case OUTPUT -> FaceMode.OUTPUT;
            case INPUT_OUTPUT -> FaceMode.INPUT_OUTPUT;
            case DISABLED -> FaceMode.DISABLED;
        };
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        sideConfiguration.setRoutes(ioFacing(), direction,
                mode.canInput() ? 0 : -1, mode.canOutput() ? 0 : -1);
        updateDisplay();
        ioConfigurationChanged();
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return BufferBlock.PIPE;
    }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() {
        return BufferBlock.FACING;
    }
    @Override public boolean allowsPassiveInsertion(Direction side, IoTransportKind kind) {
        return kind == IoTransportKind.ITEM;
    }
    @Override public boolean allowsPassiveExtraction(Direction side, IoTransportKind kind) {
        return kind == IoTransportKind.ITEM;
    }
    @Override public void ioConfigurationChanged() {
        updateDisplay();
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChangedAndSync();
        ioNeighborsChanged();
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    public BufferConfiguration copyConfiguration() {
        List<Integer> faces = java.util.Arrays.stream(Direction.values())
                .map(direction -> faceMode(direction).ordinal()).toList();
        return new BufferConfiguration(faces);
    }

    public void applyConfiguration(BufferConfiguration configuration) {
        for (Direction direction : Direction.values()) {
            int ordinal = configuration.faces().get(direction.get3DDataValue());
            if (ordinal >= 0 && ordinal < FaceMode.values().length) {
                setFaceMode(direction, FaceMode.values()[ordinal]);
            }
        }
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropped.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return dropped;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BufferMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        saveIoState(tag, registries);
        tag.putInt("insert_tick", insertTick);
        tag.putInt("extract_tick", extractTick);
        tag.putInt("next_output_direction", nextOutputDirection);
        tag.putInt("next_input_direction", nextInputDirection);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            CompoundTag saved = tag.getCompound("inventory").copy();
            saved.putInt("Size", inventory.getSlots());
            inventory.deserializeNBT(registries, saved);
        }
        loadIoState(tag, registries);
        insertTick = Math.max(0, tag.getInt("insert_tick"));
        extractTick = Math.max(0, tag.getInt("extract_tick"));
        nextOutputDirection = Math.floorMod(tag.getInt("next_output_direction"), Direction.values().length);
        nextInputDirection = Math.floorMod(tag.getInt("next_input_direction"), Direction.values().length);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void updateDisplay() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        for (Direction direction : Direction.values()) {
            state = state.setValue(BufferBlock.FACES.get(direction.get3DDataValue()), faceMode(direction));
        }
        if (state != getBlockState()) {
            level.setBlock(worldPosition, state, 3);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public enum FaceMode implements StringRepresentable {
        INPUT,
        OUTPUT,
        DISABLED,
        INPUT_OUTPUT;

        public boolean canInput() {
            return this == INPUT || this == INPUT_OUTPUT;
        }

        public boolean canOutput() {
            return this == OUTPUT || this == INPUT_OUTPUT;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public FaceMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final class BufferItemHandler implements IItemHandler {
        @Nullable
        private final Direction direction;

        private BufferItemHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return acceptsInput() && (direction == null || matchesFilter(direction, stack))
                    ? inventory.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return (direction == null || matchesFilter(direction, stack))
                    && inventory.isItemValid(slot, stack);
        }
    }
}
