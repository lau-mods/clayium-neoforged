package net.claustra01.clayium.pan;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PanMenu extends AbstractContainerMenu {
    private static final int ADAPTER_GHOST_SLOTS = PanBlockEntity.ADAPTER_PAGE_SIZE * 2;

    private final PanBlockEntity pan;
    private final BlockPos position;
    private final ContainerData data;
    private final int machineSlots;
    private final ServerPlayer viewer;
    private List<PanScreenPayloads.Candidate> candidates = List.of();
    private int candidatePage;
    private int candidatePages = 1;
    private int candidateSort;
    private boolean candidateReverse;
    private ResourceLocation selectedCandidate = ClayiumMod.id("none");
    private long lastSnapshotHash = Long.MIN_VALUE;
    private int adapterPage;

    public PanMenu(int containerId, Inventory inventory, BlockPos position) {
        this(containerId, inventory, panAt(inventory, position), new SimpleContainerData(12));
    }

    public PanMenu(int containerId, Inventory inventory, PanBlockEntity pan, ContainerData data) {
        super(PanRegistration.MENU.get(), containerId);
        this.pan = pan;
        this.position = pan.getBlockPos();
        this.data = data;
        viewer = inventory.player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        if (pan.kind() == PanBlock.Kind.DUPLICATOR) {
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.SAMPLE_SLOT, 35, 35));
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.ANTIMATTER_SLOT, 35, 57));
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.OUTPUT_SLOT, 125, 35));
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.FUEL_SLOT, 35, 79));
        } else if (pan.kind() == PanBlock.Kind.ADAPTER) {
            IItemHandler patterns = new AdapterPageHandler(pan.adapterPatterns());
            IItemHandler results = new AdapterPageHandler(pan.adapterResults());
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    int slot = column + row * 3;
                    addSlot(new GhostSlot(patterns, slot, 32 + column * 18, 18 + row * 18));
                }
            }
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    int slot = column + row * 3;
                    addSlot(new GhostSlot(results, slot, 92 + column * 18, 18 + row * 18));
                }
            }
            for (int slot = 0; slot < PanBlockEntity.ADAPTER_PAGE_SIZE; slot++) {
                addSlot(new SlotItemHandler(pan.adapterSubItems(), slot, 8 + slot * 18, 74));
            }
        }
        machineSlots = slots.size();
        int playerInventoryY = switch (pan.kind()) {
            case CORE -> 156;
            case ADAPTER -> 108;
            case DUPLICATOR -> 119;
            case CABLE -> 105;
        };
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, playerInventoryY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, playerInventoryY + 58));
        }
        addDataSlots(data);
    }

    private static PanBlockEntity panAt(Inventory inventory, BlockPos position) {
        BlockEntity entity = inventory.player.level().getBlockEntity(position);
        return (PanBlockEntity)entity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()
                || (pan.kind() == PanBlock.Kind.ADAPTER && index < ADAPTER_GHOST_SLOTS)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (pan.kind() == PanBlock.Kind.ADAPTER) {
            if (!moveItemStackTo(moving, ADAPTER_GHOST_SLOTS, machineSlots, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            for (int target = 0; target < machineSlots; target++) {
                if (slots.get(target).mayPlace(moving)
                        && moveItemStackTo(moving, target, target + 1, false)) {
                    moved = true;
                    break;
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return !pan.isRemoved() && player.level().getBlockEntity(position) == pan
                && player.distanceToSqr(position.getX() + 0.5D, position.getY() + 0.5D,
                        position.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (viewer == null || pan.kind() != PanBlock.Kind.CORE) {
            return;
        }
        PanScreenPayloads.Snapshot snapshot = PanScreenPayloads.snapshot(this);
        long hash = snapshot.contentHash();
        if (hash != lastSnapshotHash) {
            lastSnapshotHash = hash;
            PacketDistributor.sendToPlayer(viewer, snapshot);
        }
    }

    public void applySnapshot(PanScreenPayloads.Snapshot snapshot) {
        if (snapshot.containerId() != containerId) {
            return;
        }
        candidatePage = snapshot.page();
        candidatePages = snapshot.pages();
        candidateSort = snapshot.sort();
        candidateReverse = snapshot.reverse();
        selectedCandidate = snapshot.selected();
        candidates = snapshot.candidates();
    }

    public long progress() {
        return read(0, 2);
    }

    public long requiredProgress() {
        return read(2, 2);
    }

    public long requiredEnergy() {
        return read(4, 4);
    }

    public int nodes() {
        return data.get(8);
    }

    public int adapters() {
        return data.get(9);
    }

    public PanBlock.Kind kind() {
        return pan.kind();
    }

    public int tier() {
        return data.get(10);
    }

    public boolean sampleReachable() {
        return data.get(11) != 0;
    }

    public PanBlockEntity pan() {
        return pan;
    }

    public BlockPos position() {
        return position;
    }

    public List<PanScreenPayloads.Candidate> visibleCandidates() {
        return viewer == null ? candidates : pan.visibleCandidates();
    }

    public List<PanScreenPayloads.Candidate> candidates() {
        return viewer == null ? candidates : pan.allCandidates();
    }

    public int candidatePage() {
        return viewer == null ? candidatePage : pan.candidatePage();
    }

    public int candidatePages() {
        return viewer == null ? candidatePages : pan.candidatePages();
    }

    public int candidateSort() {
        return viewer == null ? candidateSort : pan.candidateSort();
    }

    public boolean candidateReverse() {
        return viewer == null ? candidateReverse : pan.candidateReverse();
    }

    public ResourceLocation selectedCandidate() {
        if (viewer == null) {
            return selectedCandidate;
        }
        ResourceLocation selected = pan.selectedCandidate();
        return selected == null ? ClayiumMod.id("none") : selected;
    }

    public int adapterPage() {
        return adapterPage;
    }

    public int adapterPages() {
        return pan.kind() == PanBlock.Kind.ADAPTER ? pan.adapterPages() : 1;
    }

    public boolean setAdapterPage(int page) {
        if (pan.kind() != PanBlock.Kind.ADAPTER || page < 0 || page >= adapterPages()) {
            return false;
        }
        adapterPage = page;
        broadcastChanges();
        return true;
    }

    public int machineSlots() {
        return machineSlots;
    }

    private long read(int start, int length) {
        long value = 0L;
        for (int index = 0; index < length; index++) {
            value |= (long)(data.get(start + index) & 0xFFFF) << index * 16;
        }
        return value;
    }

    private final class AdapterPageHandler implements IItemHandlerModifiable {
        private final IItemHandlerModifiable delegate;

        private AdapterPageHandler(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        private int actual(int slot) {
            return adapterPage * PanBlockEntity.ADAPTER_PAGE_SIZE + slot;
        }

        @Override
        public int getSlots() {
            return PanBlockEntity.ADAPTER_PAGE_SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(actual(slot));
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            delegate.setStackInSlot(actual(slot), stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return delegate.insertItem(actual(slot), stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return delegate.extractItem(actual(slot), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(actual(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return delegate.isItemValid(actual(slot), stack);
        }
    }

    private static final class GhostSlot extends SlotItemHandler {
        private GhostSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            return ItemStack.EMPTY;
        }
    }
}
