package net.claustra01.clayium.logistics.multitrack;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
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

public final class MultitrackBufferBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    public static final int TRACKS = 6;
    public static final int SLOTS_PER_TRACK = 9;

    private final Track[] tracks = new Track[TRACKS];
    private final int[] insertionRoutes = {-1, -1, -1, 0, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, this::trackCount, this::trackCount);
    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches;
    private int insertTick;
    private int extractTick;

    public MultitrackBufferBlockEntity(BlockPos pos, BlockState state) {
        super(MultitrackRegistration.MULTITRACK_BUFFER_BLOCK_ENTITY.get(), pos, state);
        for (int index = 0; index < TRACKS; index++) {
            tracks[index] = new Track();
        }
    }

    @Override public String insertionFaceTexture(int route) { return "import_m" + route; }
    @Override public String extractionFaceTexture(int route) { return "export_m" + route; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MultitrackBufferBlockEntity buffer) {
        buffer.tickServer();
    }

    private void tickServer() {
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
        for (int trackIndex = 0; trackIndex < trackCount(); trackIndex++) {
            if (extractDue) {
                pushTrack(serverLevel, trackIndex, parameters.maximumExtractItems());
            }
            if (insertDue) {
                pullTrack(serverLevel, trackIndex, parameters.maximumInsertItems());
            }
        }
    }

    private void pushTrack(ServerLevel serverLevel, int trackIndex, int maximum) {
        Track track = tracks[trackIndex];
        int remaining = maximum;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int directionIndex = (track.nextOutputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[directionIndex];
            if (extractionRoute(direction) != trackIndex) {
                continue;
            }
            IItemHandler target = neighborTrackHandler(serverLevel, direction, trackIndex, true);
            if (target == null) {
                continue;
            }
            int moved = BufferBlockEntity.moveItems(
                    track.inventory, target, track::accepts, remaining, stack -> recover(track, stack));
            if (moved > 0) {
                remaining -= moved;
                track.nextOutputDirection = (directionIndex + 1) % Direction.values().length;
            }
        }
    }

    private void pullTrack(ServerLevel serverLevel, int trackIndex, int maximum) {
        Track track = tracks[trackIndex];
        int remaining = maximum;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int directionIndex = (track.nextInputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[directionIndex];
            if (insertionRoute(direction) != trackIndex) {
                continue;
            }
            IItemHandler source = neighborTrackHandler(serverLevel, direction, trackIndex, false);
            if (source == null) {
                continue;
            }
            int moved = BufferBlockEntity.moveItems(
                    source, track.inventory, track::accepts, remaining, stack -> recover(track, stack));
            if (moved > 0) {
                remaining -= moved;
                track.nextInputDirection = (directionIndex + 1) % Direction.values().length;
            }
        }
    }

    @Nullable
    private IItemHandler neighborTrackHandler(ServerLevel serverLevel, Direction direction, int trackIndex, boolean output) {
        BlockEntity neighbor = serverLevel.getBlockEntity(worldPosition.relative(direction));
        if (neighbor instanceof MultitrackBufferBlockEntity multitrack) {
            if (trackIndex >= multitrack.trackCount()) return null;
            boolean connected = output
                    ? multitrack.insertionRoute(direction.getOpposite()) == trackIndex
                    : multitrack.extractionRoute(direction.getOpposite()) == trackIndex;
            return connected
                    ? multitrack.trackHandler(trackIndex, direction.getOpposite()) : null;
        }
        if ((output ? extractionRoute(direction) : insertionRoute(direction)) != trackIndex) {
            return null;
        }
        return neighborCaches.get(direction).getCapability();
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

    private void recover(Track track, ItemStack stack) {
        ItemStack remainder = insertAcross(track.inventory, stack);
        if (!remainder.isEmpty() && level != null) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, remainder);
        }
    }

    private static ItemStack insertAcross(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    public int tier() {
        return ((MultitrackBufferBlock)getBlockState().getBlock()).tier();
    }

    public int trackCount() {
        return Math.min(6, tier() - 2);
    }

    public Track track(int index) {
        return tracks[index];
    }

    public int faceTrack(Direction direction) {
        int insertion = insertionRoute(direction);
        return insertion >= 0 ? insertion : Math.max(0, extractionRoute(direction));
    }

    public void setFaceTrack(Direction direction, int track) {
        int insertion = insertionRoute(direction) >= 0 ? track : -1;
        int extraction = extractionRoute(direction) >= 0 ? track : -1;
        if (insertion < 0 && extraction < 0) insertion = track;
        sideConfiguration.setRoutes(ioFacing(), direction, insertion, extraction);
        ioConfigurationChanged();
    }

    public void setFaceMode(int trackIndex, Direction direction, BufferBlockEntity.FaceMode mode) {
        int insertion = insertionRoute(direction);
        int extraction = extractionRoute(direction);
        switch (mode) {
            case INPUT -> { insertion = trackIndex; if (extraction == trackIndex) extraction = -1; }
            case OUTPUT -> { extraction = trackIndex; if (insertion == trackIndex) insertion = -1; }
            case INPUT_OUTPUT -> { insertion = trackIndex; extraction = trackIndex; }
            case DISABLED -> {
                if (insertion == trackIndex) insertion = -1;
                if (extraction == trackIndex) extraction = -1;
            }
        }
        sideConfiguration.setRoutes(ioFacing(), direction, insertion, extraction);
        ioConfigurationChanged();
    }

    public BufferBlockEntity.FaceMode faceMode(int trackIndex, Direction direction) {
        boolean input = insertionRoute(direction) == trackIndex;
        boolean output = extractionRoute(direction) == trackIndex;
        if (input && output) return BufferBlockEntity.FaceMode.INPUT_OUTPUT;
        if (input) return BufferBlockEntity.FaceMode.INPUT;
        if (output) return BufferBlockEntity.FaceMode.OUTPUT;
        return BufferBlockEntity.FaceMode.DISABLED;
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() { return MultitrackBufferBlock.PIPE; }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return MultitrackBufferBlock.FACING; }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChanged();
        syncFaces();
        ioNeighborsChanged();
    }

    private void syncFaces() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? combinedHandler() : new RoutedFaceHandler(direction);
    }

    public IItemHandler trackHandler(int trackIndex, @Nullable Direction direction) {
        return new TrackHandler(track(trackIndex), direction);
    }

    private IItemHandler combinedHandler() {
        return new CombinedTrackHandler();
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(trackCount() * (SLOTS_PER_TRACK + 1));
        int droppedSlot = 0;
        for (int index = 0; index < trackCount(); index++) {
            Track track = tracks[index];
            for (int slot = 0; slot < SLOTS_PER_TRACK; slot++) {
                dropped.setItem(droppedSlot++, track.inventory.getStackInSlot(slot).copy());
                track.inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
            dropped.setItem(droppedSlot++, track.filter.getStackInSlot(0).copy());
            track.filter.setStackInSlot(0, ItemStack.EMPTY);
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
        return new MultitrackBufferMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag trackData = new CompoundTag();
        for (int index = 0; index < trackCount(); index++) {
            Track track = tracks[index];
            CompoundTag saved = new CompoundTag();
            saved.put("inventory", track.inventory.serializeNBT(registries));
            saved.put("filter", track.filter.serializeNBT(registries));
            saved.putInt("next_output_direction", track.nextOutputDirection);
            saved.putInt("next_input_direction", track.nextInputDirection);
            trackData.put(Integer.toString(index), saved);
        }
        tag.put("tracks", trackData);
        saveIoState(tag, registries);
        tag.putInt("insert_tick", insertTick);
        tag.putInt("extract_tick", extractTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag trackData = tag.getCompound("tracks");
        for (int index = 0; index < trackCount(); index++) {
            if (!trackData.contains(Integer.toString(index))) {
                continue;
            }
            Track track = tracks[index];
            CompoundTag saved = trackData.getCompound(Integer.toString(index));
            track.inventory.deserializeNBT(registries, saved.getCompound("inventory"));
            track.filter.deserializeNBT(registries, saved.getCompound("filter"));
            track.nextOutputDirection = saved.getInt("next_output_direction");
            track.nextInputDirection = saved.getInt("next_input_direction");
        }
        loadIoState(tag, registries);
        insertTick = Math.max(0, tag.getInt("insert_tick"));
        extractTick = Math.max(0, tag.getInt("extract_tick"));
    }

    public final class Track {
        private final ItemStackHandler inventory;
        private final ItemStackHandler filter;
        private int nextOutputDirection;
        private int nextInputDirection;

        private Track() {
            inventory = new ItemStackHandler(SLOTS_PER_TRACK) {
                @Override
                protected void onContentsChanged(int slot) {
                    setChanged();
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return accepts(stack);
                }
            };
            filter = new ItemStackHandler(1) {
                @Override
                protected void onContentsChanged(int slot) {
                    setChanged();
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return stack.is(LogisticsRegistration.ITEM_FILTER.get());
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            };
        }

        public ItemStackHandler inventory() {
            return inventory;
        }

        public ItemStackHandler filter() {
            return filter;
        }

        public BufferBlockEntity.FaceMode faceMode(Direction direction) {
            return MultitrackBufferBlockEntity.this.faceMode(java.util.Arrays.asList(tracks).indexOf(this), direction);
        }

        private boolean accepts(ItemStack stack) {
            return ItemFilter.accepts(filter.getStackInSlot(0), stack);
        }
    }

    private final class TrackHandler implements IItemHandler {
        private final Track track;
        @Nullable private final Direction direction;

        private TrackHandler(Track track, @Nullable Direction direction) {
            this.track = track;
            this.direction = direction;
        }

        @Override public int getSlots() { return direction != null
                && track.faceMode(direction) == BufferBlockEntity.FaceMode.DISABLED ? 0 : SLOTS_PER_TRACK; }
        @Override public ItemStack getStackInSlot(int slot) { return track.inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return direction == null || track.faceMode(direction).canInput()
                    ? track.inventory.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return direction == null || track.faceMode(direction).canOutput()
                    ? track.inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return track.inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return (direction == null || track.faceMode(direction).canInput())
                    && track.accepts(stack);
        }
    }

    private final class RoutedFaceHandler implements IItemHandler {
        private final Direction direction;

        private RoutedFaceHandler(Direction direction) {
            this.direction = direction;
        }

        private int inputTrack() { return insertionRoute(direction); }
        private int outputTrack() { return extractionRoute(direction); }
        private boolean sharedTrack() { return inputTrack() >= 0 && inputTrack() == outputTrack(); }

        @Override public int getSlots() {
            return (inputTrack() >= 0 ? SLOTS_PER_TRACK : 0)
                    + (outputTrack() >= 0 && !sharedTrack() ? SLOTS_PER_TRACK : 0);
        }

        private int trackForSlot(int slot) {
            if (inputTrack() >= 0 && slot < SLOTS_PER_TRACK) return inputTrack();
            return outputTrack();
        }

        private int localSlot(int slot) {
            return inputTrack() >= 0 && slot >= SLOTS_PER_TRACK ? slot - SLOTS_PER_TRACK : slot;
        }

        @Override public ItemStack getStackInSlot(int slot) {
            return track(trackForSlot(slot)).inventory.getStackInSlot(localSlot(slot));
        }

        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (inputTrack() < 0 || slot >= SLOTS_PER_TRACK) return stack;
            return track(inputTrack()).inventory.insertItem(slot, stack, simulate);
        }

        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int outputStart = inputTrack() >= 0 && !sharedTrack() ? SLOTS_PER_TRACK : 0;
            if (outputTrack() < 0 || slot < outputStart) return ItemStack.EMPTY;
            return track(outputTrack()).inventory.extractItem(slot - outputStart, amount, simulate);
        }

        @Override public int getSlotLimit(int slot) {
            return track(trackForSlot(slot)).inventory.getSlotLimit(localSlot(slot));
        }

        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return inputTrack() >= 0 && slot < SLOTS_PER_TRACK && track(inputTrack()).accepts(stack);
        }
    }

    private final class CombinedTrackHandler implements IItemHandler {
        @Override public int getSlots() { return trackCount() * SLOTS_PER_TRACK; }
        @Override public ItemStack getStackInSlot(int slot) { return track(slot / SLOTS_PER_TRACK).inventory.getStackInSlot(slot % SLOTS_PER_TRACK); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return track(slot / SLOTS_PER_TRACK).inventory.insertItem(slot % SLOTS_PER_TRACK, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return track(slot / SLOTS_PER_TRACK).inventory.extractItem(slot % SLOTS_PER_TRACK, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return track(slot / SLOTS_PER_TRACK).inventory.getSlotLimit(slot % SLOTS_PER_TRACK); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return track(slot / SLOTS_PER_TRACK).accepts(stack); }
    }
}
