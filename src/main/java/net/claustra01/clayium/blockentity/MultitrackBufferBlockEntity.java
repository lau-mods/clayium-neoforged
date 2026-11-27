package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Multitrack Buffer (T4..T13): partitions slots into up to 6 isolated colored tracks,
 * each with its own filter slot in indices {@code 54..59}.
 */
public class MultitrackBufferBlockEntity extends TieredClayContainerBlockEntity {
    public static final int FILTER_SLOT_START = 54;

    public int[][] tracks = new int[0][];
    public int[] slot2track = new int[0];
    public int[] allSlots = new int[0];
    public int trackCount = 0;
    public int trackSize = 0;

    public MultitrackBufferBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.MULTITRACK_BUFFER.get(), pos, state, 60);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.clayEnergySlot = -1;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.BUFFER);
        int trackNum = 6;
        int trackInvSize = 9;
        switch (tier) {
            case 4 -> {
                trackNum = 2;
                trackInvSize = 1;
            }
            case 5 -> {
                trackNum = 3;
                trackInvSize = 2;
            }
            case 6 -> {
                trackNum = 4;
                trackInvSize = 4;
            }
            case 7 -> {
                trackNum = 5;
                trackInvSize = 4;
            }
            case 8 -> {
                trackNum = 6;
                trackInvSize = 6;
            }
            case 9, 10, 11, 12, 13 -> {
                trackNum = 6;
                trackInvSize = 9;
            }
            default -> {
                trackNum = 2;
                trackInvSize = 1;
            }
        }
        this.trackCount = trackNum;
        this.trackSize = trackInvSize;
        int slotNum = trackNum * trackInvSize;
        int[] allIn = new int[slotNum];
        int[] allOut = new int[slotNum];
        for (int i = 0; i < slotNum; i++) {
            allIn[i] = i;
            allOut[i] = slotNum - i - 1;
        }
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(allIn);
        this.listSlotsExtract.add(allOut);
        this.slotsDrop = allIn;
        this.allSlots = allIn;
        this.tracks = new int[trackNum][];
        this.slot2track = new int[slotNum];

        for (int t = 0; t < trackNum; t++) {
            int[] tIn = new int[trackInvSize];
            int[] tOut = new int[trackInvSize];
            for (int i = 0; i < trackInvSize; i++) {
                int idx = i + t * trackInvSize;
                tIn[i] = idx;
                tOut[i] = (trackInvSize - i - 1) + t * trackInvSize;
                this.slot2track[idx] = t;
            }
            this.listSlotsInsert.add(tIn);
            this.listSlotsExtract.add(tOut);
            this.tracks[t] = tIn;
        }
    }

    public int getTrack(int slot) {
        return slot >= 0 && slot < this.slot2track.length ? this.slot2track[slot] : -1;
    }

    public boolean isItemValidForTrack(int track, ItemStack stack) {
        if (stack.isEmpty() || track < 0 || track >= this.trackCount) {
            return false;
        }
        ItemStack filter = getStack(FILTER_SLOT_START + track);
        if (filter.isEmpty()) {
            return true;
        }
        if (ClayiumFilterMatcher.isFilter(filter)) {
            return ClayiumFilterMatcher.match(filter, stack);
        }
        return ItemStack.isSameItemSameComponents(filter, stack);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        int track = getTrack(slot);
        return track >= 0 && isItemValidForTrack(track, stack);
    }

    @Override
    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        if (slot < 0 || slot >= this.allSlots.length) {
            return false;
        }
        return route == ROUTE_DISABLED ? isItemValidForSlot(slot, stack) : super.canInsertItemUnsafe(slot, stack, route);
    }

    @Override
    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        if (slot < 0 || slot >= this.allSlots.length) {
            return false;
        }
        return route == ROUTE_DISABLED ? true : super.canExtractItemUnsafe(slot, stack, route);
    }

    @Override
    protected void doAutoExtractFromSide(RelativeSide relSide, Direction front) {
        int route = this.insertRoutes[relSide.id()];
        if (route < 0) {
            return;
        }
        int max = this.maxAutoExtract != null && route < this.maxAutoExtract.length && this.maxAutoExtract[route] >= 0
                ? this.maxAutoExtract[route]
                : this.maxAutoExtractDefault;
        Direction worldDir = ClayDirection.sideToDirection(front, relSide);
        for (int k = 0; k < this.tracks.length; k++) {
            if (route == 0 || route - 1 == k) {
                ClayiumTransfer.extract(this, this.tracks[k], worldDir, max, k);
            }
        }
    }

    @Override
    protected void doAutoInsertToSide(RelativeSide relSide, Direction front) {
        int route = this.extractRoutes[relSide.id()];
        if (route < 0) {
            return;
        }
        int max = this.maxAutoInsert != null && route < this.maxAutoInsert.length && this.maxAutoInsert[route] >= 0
                ? this.maxAutoInsert[route]
                : this.maxAutoInsertDefault;
        Direction worldDir = ClayDirection.sideToDirection(front, relSide);
        for (int k = 0; k < this.tracks.length; k++) {
            if (route == 0 || route - 1 == k) {
                ClayiumTransfer.insert(this, this.tracks[k], worldDir, max, k);
            }
        }
    }

    public IItemHandler getItemHandlerForTrack(@Nullable Direction side, int track) {
        return new TrackItemHandler(side, track);
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    private final class TrackItemHandler extends ContainerItemHandler {
        private final int track;

        TrackItemHandler(@Nullable Direction side, int track) {
            super(side);
            this.track = track;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (this.track >= 0 && getTrack(slot) != this.track) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (this.track >= 0 && getTrack(slot) != this.track) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
