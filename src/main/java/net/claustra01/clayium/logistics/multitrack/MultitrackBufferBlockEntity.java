package net.claustra01.clayium.logistics.multitrack;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.logistics.BufferBlockEntity;
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

public final class MultitrackBufferBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TRACKS = 6;
    public static final int SLOTS_PER_TRACK = 9;

    private final Track[] tracks = new Track[TRACKS];
    private final int[] faceTracks = new int[Direction.values().length];
    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches;
    private int transferTick;

    public MultitrackBufferBlockEntity(BlockPos pos, BlockState state) {
        super(MultitrackRegistration.MULTITRACK_BUFFER_BLOCK_ENTITY.get(), pos, state);
        for (int index = 0; index < TRACKS; index++) {
            tracks[index] = new Track();
        }
        for (Direction direction : Direction.values()) {
            faceTracks[direction.get3DDataValue()] = direction.get3DDataValue() % trackCount();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MultitrackBufferBlockEntity buffer) {
        buffer.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        TierManager.TransportParameters parameters = TierManager.bufferTransport(tier());
        transferTick++;
        if (transferTick < parameters.intervalTicks()) {
            return;
        }
        transferTick = 0;
        ensureCaches(serverLevel);
        for (int trackIndex = 0; trackIndex < trackCount(); trackIndex++) {
            pushTrack(serverLevel, trackIndex, parameters.maximumItems());
            pullTrack(serverLevel, trackIndex, parameters.maximumItems());
        }
    }

    private void pushTrack(ServerLevel serverLevel, int trackIndex, int maximum) {
        Track track = tracks[trackIndex];
        int remaining = maximum;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int directionIndex = (track.nextOutputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[directionIndex];
            if (track.faceModes.get(direction) != BufferBlockEntity.FaceMode.OUTPUT) {
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
            if (track.faceModes.get(direction) != BufferBlockEntity.FaceMode.INPUT) {
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
            BufferBlockEntity.FaceMode needed = output
                    ? BufferBlockEntity.FaceMode.INPUT : BufferBlockEntity.FaceMode.OUTPUT;
            return multitrack.track(trackIndex).faceModes.get(direction.getOpposite()) == needed
                    ? multitrack.trackHandler(trackIndex, direction.getOpposite()) : null;
        }
        if (faceTracks[direction.get3DDataValue()] != trackIndex) {
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
        return faceTracks[direction.get3DDataValue()];
    }

    public void setFaceTrack(Direction direction, int track) {
        faceTracks[direction.get3DDataValue()] = track;
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        setChanged();
        syncFaces();
    }

    public void setFaceMode(int trackIndex, Direction direction, BufferBlockEntity.FaceMode mode) {
        track(trackIndex).faceModes.put(direction, mode);
        setChanged();
        syncFaces();
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
        return direction == null ? combinedHandler() : trackHandler(faceTrack(direction), direction);
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
            CompoundTag faces = new CompoundTag();
            track.faceModes.forEach((direction, mode) -> faces.putString(direction.getName(), mode.name()));
            saved.put("faces", faces);
            saved.putInt("next_output_direction", track.nextOutputDirection);
            saved.putInt("next_input_direction", track.nextInputDirection);
            trackData.put(Integer.toString(index), saved);
        }
        tag.put("tracks", trackData);
        tag.putIntArray("face_tracks", faceTracks);
        tag.putInt("transfer_tick", transferTick);
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
            CompoundTag faces = saved.getCompound("faces");
            for (Direction direction : Direction.values()) {
                if (faces.contains(direction.getName())) {
                    track.faceModes.put(direction, BufferBlockEntity.FaceMode.valueOf(
                            faces.getString(direction.getName())));
                }
            }
            track.nextOutputDirection = saved.getInt("next_output_direction");
            track.nextInputDirection = saved.getInt("next_input_direction");
        }
        int[] savedFaceTracks = tag.getIntArray("face_tracks");
        for (int index = 0; index < Math.min(savedFaceTracks.length, faceTracks.length); index++) {
            faceTracks[index] = savedFaceTracks[index];
        }
        transferTick = tag.getInt("transfer_tick");
    }

    public final class Track {
        private final ItemStackHandler inventory;
        private final ItemStackHandler filter;
        private final EnumMap<Direction, BufferBlockEntity.FaceMode> faceModes = new EnumMap<>(Direction.class);
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
            for (Direction direction : Direction.values()) {
                faceModes.put(direction, direction == Direction.DOWN
                        ? BufferBlockEntity.FaceMode.OUTPUT : BufferBlockEntity.FaceMode.INPUT);
            }
        }

        public ItemStackHandler inventory() {
            return inventory;
        }

        public ItemStackHandler filter() {
            return filter;
        }

        public BufferBlockEntity.FaceMode faceMode(Direction direction) {
            return faceModes.get(direction);
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

        @Override public int getSlots() { return direction != null && track.faceMode(direction) == BufferBlockEntity.FaceMode.DISABLED ? 0 : SLOTS_PER_TRACK; }
        @Override public ItemStack getStackInSlot(int slot) { return track.inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return direction == null || track.faceMode(direction) == BufferBlockEntity.FaceMode.INPUT
                    ? track.inventory.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return direction == null || track.faceMode(direction) == BufferBlockEntity.FaceMode.OUTPUT
                    ? track.inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return track.inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return (direction == null || track.faceMode(direction) == BufferBlockEntity.FaceMode.INPUT)
                    && track.accepts(stack);
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
