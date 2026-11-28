package net.claustra01.clayium.blockentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.DirectionalClayContainerBlock;
import net.claustra01.clayium.block.HorizontalClayContainerBlock;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.ContainerIoMemoryData;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.item.ClayToolItem;
import net.claustra01.clayium.item.ClayToolItem.ToolKind;
import net.claustra01.clayium.item.FilterItem;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Core BlockEntity for all Clayium containers, buffers, interfaces, and machines.
 * Handles 6-relative-side IO routing, per-side item filters, auto-insert/extract scheduling,
 * Clay Energy buffer consumption, tool configuration, NBT persistence, and client sync.
 */
public abstract class ClayContainerBlockEntity extends BlockEntity implements IGeneralInterface {
    public static final int ROUTE_DISABLED = -1;

    protected NonNullList<ItemStack> items;
    @Nullable
    protected String customName;

    public final List<int[]> listSlotsInsert = new ArrayList<>();
    public final List<int[]> listSlotsExtract = new ArrayList<>();
    public int[] insertRoutes = {ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
    public int[] extractRoutes = {ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
    public final NonNullList<ItemStack> filters = NonNullList.withSize(6, ItemStack.EMPTY);
    public int[] slotsDrop = new int[0];

    public boolean autoInsert = false;
    public boolean autoExtract = false;
    @Nullable
    public int[] maxAutoInsert;
    @Nullable
    public int[] maxAutoExtract;
    public int maxAutoInsertDefault = 8;
    public int maxAutoExtractDefault = 8;
    public int autoInsertInterval = 20;
    public int autoExtractInterval = 20;
    protected int autoInsertCount = 0;
    protected int autoExtractCount = 0;

    protected final ClayEnergyStorage clayEnergyStorage = new ClayEnergyStorage();
    public int clayEnergySlot = -1;

    private boolean dirtyForSync = false;

    protected ClayContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state);
        this.items = NonNullList.withSize(Math.max(0, inventorySize), ItemStack.EMPTY);
        initParams();
    }

    protected void initParams() {}

    public int getInventorySize() {
        return this.items.size();
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
    }

    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.items.size()) {
            this.items.set(slot, stack);
            markForSync();
        }
    }

    public int getInventoryStackLimit(int slot) {
        if (slot == this.clayEnergySlot) {
            return this.clayEnergyStorage.getEnergySlotLimit();
        }
        return 64;
    }

    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (slot == this.clayEnergySlot) {
            return isEnergySlotValid(stack);
        }
        return true;
    }

    protected boolean isEnergySlotValid(ItemStack stack) {
        return acceptEnergyClay()
                && ClayEnergy.getItemEnergy(stack) > 0L
                && (getStack(this.clayEnergySlot).isEmpty()
                || getStack(this.clayEnergySlot).getCount() < this.clayEnergyStorage.getEnergySlotLimit());
    }

    public boolean acceptEnergyClay() {
        return true;
    }

    @Override
    public boolean acceptInterfaceSync() {
        return true;
    }

    public ClayEnergyStorage getClayEnergyStorage() {
        return this.clayEnergyStorage;
    }

    public long getClayEnergy() {
        return this.clayEnergyStorage.getStored();
    }

    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Nullable
    public String getCustomName() {
        return this.customName;
    }

    public int getInsertRoute(RelativeSide side) {
        return this.insertRoutes[side.id()];
    }

    public int getExtractRoute(RelativeSide side) {
        return this.extractRoutes[side.id()];
    }

    public ItemStack getFilter(RelativeSide side) {
        return this.filters.get(side.id());
    }

    public boolean consumeClayEnergy(long energy) {
        boolean ok = this.clayEnergyStorage.consume(
                energy,
                () -> this.clayEnergySlot >= 0 ? getStack(this.clayEnergySlot) : ItemStack.EMPTY,
                rem -> {
                    if (this.clayEnergySlot >= 0) {
                        setStack(this.clayEnergySlot, rem);
                    }
                }
        );
        if (ok && energy > 0L) {
            markForSync();
        }
        return ok;
    }

    public Direction getFrontDirection() {
        BlockState state = getBlockState();
        if (state.hasProperty(HorizontalClayContainerBlock.FACING)) {
            return state.getValue(HorizontalClayContainerBlock.FACING);
        }
        if (state.hasProperty(DirectionalClayContainerBlock.FACING)) {
            return state.getValue(DirectionalClayContainerBlock.FACING);
        }
        return Direction.NORTH;
    }

    public RelativeSide toRelativeSide(Direction worldSide) {
        return ClayDirection.directionToSide(getFrontDirection(), worldSide);
    }

    public Direction toWorldDirection(RelativeSide side) {
        return ClayDirection.sideToDirection(getFrontDirection(), side);
    }

    public boolean canInsertToSide(int slot, ItemStack stack, RelativeSide relSide) {
        if (stack.isEmpty() || !isItemValidForSlot(slot, stack)) {
            return false;
        }
        ItemStack filter = this.filters.get(relSide.id());
        if (ClayiumFilterMatcher.isFilter(filter) && !ClayiumFilterMatcher.match(filter, stack)) {
            return false;
        }
        return canInsertItemUnsafe(slot, stack, this.insertRoutes[relSide.id()]);
    }

    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        if (route < 0 || route >= this.listSlotsInsert.size()) {
            return false;
        }
        for (int candidateSlot : this.listSlotsInsert.get(route)) {
            if (candidateSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public boolean canExtractFromSide(int slot, ItemStack stack, RelativeSide relSide) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack filter = this.filters.get(relSide.id());
        if (ClayiumFilterMatcher.isFilter(filter) && !ClayiumFilterMatcher.match(filter, stack)) {
            return false;
        }
        return canExtractItemUnsafe(slot, stack, this.extractRoutes[relSide.id()]);
    }

    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        if (route < 0 || route >= this.listSlotsExtract.size()) {
            return false;
        }
        for (int candidateSlot : this.listSlotsExtract.get(route)) {
            if (candidateSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new ContainerItemHandler(side);
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.autoExtract) {
            this.autoExtractCount++;
            if (this.autoExtractCount >= this.autoExtractInterval) {
                this.autoExtractCount = 0;
                doAutoExtract();
            }
        } else {
            this.autoExtractCount = 0;
        }

        if (this.autoInsert) {
            this.autoInsertCount++;
            if (this.autoInsertCount >= this.autoInsertInterval) {
                this.autoInsertCount = 0;
                doAutoInsert();
            }
        } else {
            this.autoInsertCount = 0;
        }

        if (this.dirtyForSync) {
            this.dirtyForSync = false;
            setChanged();
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static void serverTickStatic(Level level, BlockPos pos, BlockState state, ClayContainerBlockEntity be) {
        be.serverTick();
    }

    public void doAutoExtract() {
        for (int i = 0; i < 6; i++) {
            if (this.insertRoutes[i] >= this.listSlotsInsert.size()) {
                this.insertRoutes[i] = ROUTE_DISABLED;
            }
        }
        Direction front = getFrontDirection();
        for (int s = 0; s < 6; s++) {
            doAutoExtractFromSide(RelativeSide.byId(s), front);
        }
    }

    protected void doAutoExtractFromSide(RelativeSide relSide, Direction front) {
        int route = this.insertRoutes[relSide.id()];
        if (route >= 0 && route < this.listSlotsInsert.size()) {
            int max = this.maxAutoExtract != null && route < this.maxAutoExtract.length && this.maxAutoExtract[route] >= 0
                    ? this.maxAutoExtract[route]
                    : this.maxAutoExtractDefault;
            ClayiumTransfer.extract(this, this.listSlotsInsert.get(route), ClayDirection.sideToDirection(front, relSide), max);
        }
    }

    public void doAutoInsert() {
        for (int i = 0; i < 6; i++) {
            if (this.extractRoutes[i] >= this.listSlotsExtract.size()) {
                this.extractRoutes[i] = ROUTE_DISABLED;
            }
        }
        Direction front = getFrontDirection();
        for (int s = 0; s < 6; s++) {
            doAutoInsertToSide(RelativeSide.byId(s), front);
        }
    }

    protected void doAutoInsertToSide(RelativeSide relSide, Direction front) {
        int route = this.extractRoutes[relSide.id()];
        if (route >= 0 && route < this.listSlotsExtract.size()) {
            int max = this.maxAutoInsert != null && route < this.maxAutoInsert.length && this.maxAutoInsert[route] >= 0
                    ? this.maxAutoInsert[route]
                    : this.maxAutoInsertDefault;
            ClayiumTransfer.insert(this, this.listSlotsExtract.get(route), ClayDirection.sideToDirection(front, relSide), max);
        }
    }

    /**
     * Resolves tool mode (-1 if not a container tool) matching original TileClayContainer.getItemUseMode.
     */
    public int getItemUseMode(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return -1;
        }
        Item item = stack.getItem();
        if (item == ClayiumItems.byPath("clay_rolling_pin").get()) {
            return 0;
        }
        if (item == ClayiumItems.byPath("clay_slicer").get()) {
            return 1;
        }
        if (item == ClayiumItems.byPath("clay_spatula").get()) {
            return 2;
        }
        if (item instanceof FilterItem) {
            return 10;
        }
        if (item == ClayiumItems.byPath("raw_clay_rolling_pin").get()
                || item == ClayiumItems.byPath("raw_clay_slicer").get()
                || item == ClayiumItems.byPath("raw_clay_spatula").get()) {
            return 11;
        }
        if (item instanceof ClayToolItem tool) {
            return switch (tool.getToolKind()) {
                case CLAY_WRENCH -> 20;
                case SYNCHRONIZER -> 90;
                case IO_CONFIGURATOR -> player.isShiftKeyDown() ? 1 : 0;
                case PIPING_CONFIGURATOR -> player.isShiftKeyDown() ? 20 : 2;
                case MEMORY_CARD -> player.isShiftKeyDown() ? 30 : 31;
                default -> -1;
            };
        }
        return -1;
    }

    @Override
    public boolean isUsableTool(ItemStack stack, Player player, Direction hitFace) {
        return getItemUseMode(stack, player) != -1;
    }

    @Override
    public boolean onToolUse(ItemStack stack, Player player, Direction hitFace) {
        int mode = getItemUseMode(stack, player);
        if (mode == -1 || this.level == null) {
            return false;
        }
        if (!this.level.isClientSide) {
            RelativeSide relSide = toRelativeSide(hitFace);
            useItemFromSide(stack, player, relSide, mode, hitFace);
            markForInstantSync();
            for (Direction dir : Direction.values()) {
                if (this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof IGeneralInterface neighbor) {
                    neighbor.markForInstantSync();
                }
            }
        }
        return true;
    }

    protected void useItemFromSide(ItemStack stack, Player player, RelativeSide relSide, int mode, Direction hitFace) {
        int sIdx = relSide.id();
        switch (mode) {
            case 0 -> {
                this.insertRoutes[sIdx]++;
                if (this.listSlotsInsert.size() <= this.insertRoutes[sIdx]) {
                    this.insertRoutes[sIdx] = ROUTE_DISABLED;
                    player.sendSystemMessage(Component.literal("Disabled"));
                } else {
                    player.sendSystemMessage(Component.literal("Set insert route " + this.insertRoutes[sIdx]));
                }
                this.level.invalidateCapabilities(this.worldPosition);
            }
            case 1 -> {
                this.extractRoutes[sIdx]++;
                if (this.listSlotsExtract.size() <= this.extractRoutes[sIdx]) {
                    this.extractRoutes[sIdx] = ROUTE_DISABLED;
                    player.sendSystemMessage(Component.literal("Disabled"));
                } else {
                    player.sendSystemMessage(Component.literal("Set extract route " + this.extractRoutes[sIdx]));
                }
                this.level.invalidateCapabilities(this.worldPosition);
            }
            case 2 -> {
                BlockState cur = getBlockState();
                if (cur.getBlock() instanceof ClayContainerBlock cc
                        && cc.canBePipe()
                        && cur.hasProperty(ClayContainerBlock.PIPE_MODE)) {
                    BlockState next = cur.cycle(ClayContainerBlock.PIPE_MODE);
                    this.level.setBlock(this.worldPosition, next, Block.UPDATE_ALL);
                    this.level.invalidateCapabilities(this.worldPosition);
                }
            }
            case 10 -> {
                if (!ClayiumFilterMatcher.isCopyMode(stack)) {
                    ItemStack stored = stack.copyWithCount(1);
                    this.filters.set(sIdx, stored);
                    player.sendSystemMessage(Component.literal("Applied ").append(stored.getHoverName()));
                } else if (!this.filters.get(sIdx).isEmpty()) {
                    ItemStack copied = ClayiumFilterMatcher.withCopyFlag(this.filters.get(sIdx).copyWithCount(1), true);
                    player.setItemInHand(player.getUsedItemHand(), copied);
                    player.sendSystemMessage(Component.literal("Copied ").append(copied.getHoverName()));
                }
            }
            case 11 -> {
                ItemStack existing = this.filters.get(sIdx);
                if (!existing.isEmpty()) {
                    player.sendSystemMessage(Component.literal("Removed ").append(existing.getHoverName()));
                    this.filters.set(sIdx, ItemStack.EMPTY);
                }
            }
            case 20 -> {
                rotateContainerFacing(hitFace);
                this.level.invalidateCapabilities(this.worldPosition);
            }
            case 30 -> {
                Block block = getBlockState().getBlock();
                if (block instanceof ClayContainerBlock cc) {
                    ContainerIoMemoryData memory = new ContainerIoMemoryData(
                            cc.getFacingMode().name(),
                            getFrontDirection(),
                            getBlockState().hasProperty(ClayContainerBlock.PIPE_MODE)
                                    && getBlockState().getValue(ClayContainerBlock.PIPE_MODE),
                            Arrays.stream(this.insertRoutes).boxed().toList(),
                            Arrays.stream(this.extractRoutes).boxed().toList()
                    );
                    stack.set(ClayiumDataComponents.IO_MEMORY.get(), memory);
                    player.sendSystemMessage(Component.literal("Saved settings to memory"));
                }
            }
            case 31 -> {
                ContainerIoMemoryData memory = stack.get(ClayiumDataComponents.IO_MEMORY.get());
                if (memory != null && getBlockState().getBlock() instanceof ClayContainerBlock cc) {
                    if (cc.getFacingMode().name().equals(memory.facingMode())) {
                        applyIoMemory(memory);
                        this.level.invalidateCapabilities(this.worldPosition);
                        player.sendSystemMessage(Component.literal("Loaded settings from memory"));
                    } else {
                        player.sendSystemMessage(Component.literal("Invalid data"));
                    }
                }
            }
            case 90 -> {
                if (acceptInterfaceSync()) {
                    GlobalPos gp = GlobalPos.of(this.level.dimension(), this.worldPosition);
                    stack.set(ClayiumDataComponents.SYNCHRONIZER_TARGET.get(), gp);
                    player.sendSystemMessage(Component.literal(
                            "Loaded Coord (" + this.worldPosition.getX() + ","
                                    + this.worldPosition.getY() + "," + this.worldPosition.getZ()
                                    + ") in dim " + this.level.dimension().location()
                    ));
                }
            }
            default -> {}
        }
    }

    private void rotateContainerFacing(Direction hitFace) {
        BlockState cur = getBlockState();
        if (!(cur.getBlock() instanceof ClayContainerBlock cc) || cc.getFacingMode() == ClayContainerBlock.FacingMode.NONE) {
            return;
        }
        if (cc.getFacingMode() == ClayContainerBlock.FacingMode.HORIZONTAL) {
            if (hitFace.getAxis() == Direction.Axis.Y) {
                return;
            }
            Direction current = cur.getValue(HorizontalClayContainerBlock.FACING);
            Direction next = hitFace == current ? current.getOpposite() : hitFace;
            this.level.setBlock(this.worldPosition, cur.setValue(HorizontalClayContainerBlock.FACING, next), Block.UPDATE_ALL);
        } else if (cur.hasProperty(DirectionalClayContainerBlock.FACING)) {
            Direction current = cur.getValue(DirectionalClayContainerBlock.FACING);
            Direction next = hitFace == current ? current.getOpposite() : hitFace;
            this.level.setBlock(this.worldPosition, cur.setValue(DirectionalClayContainerBlock.FACING, next), Block.UPDATE_ALL);
        }
    }

    private void applyIoMemory(ContainerIoMemoryData memory) {
        BlockState next = getBlockState();
        if (next.hasProperty(HorizontalClayContainerBlock.FACING) && memory.facing().getAxis().isHorizontal()) {
            next = next.setValue(HorizontalClayContainerBlock.FACING, memory.facing());
        } else if (next.hasProperty(DirectionalClayContainerBlock.FACING)) {
            next = next.setValue(DirectionalClayContainerBlock.FACING, memory.facing());
        }
        if (next.hasProperty(ClayContainerBlock.PIPE_MODE)) {
            next = next.setValue(ClayContainerBlock.PIPE_MODE, memory.pipeMode());
        }
        this.level.setBlock(this.worldPosition, next, Block.UPDATE_ALL);
        for (int i = 0; i < 6; i++) {
            this.insertRoutes[i] = i < memory.insertRoutes().size() ? memory.insertRoutes().get(i) : ROUTE_DISABLED;
            this.extractRoutes[i] = i < memory.extractRoutes().size() ? memory.extractRoutes().get(i) : ROUTE_DISABLED;
        }
    }

    public int[] getSlotsToDrop() {
        return this.slotsDrop;
    }

    @Override
    public void markForSync() {
        this.dirtyForSync = true;
        setChanged();
    }

    @Override
    public void markForInstantSync() {
        this.dirtyForSync = false;
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.items.isEmpty()) {
            ContainerHelper.saveAllItems(tag, this.items, false, registries);
        }
        if (this.customName != null && !this.customName.isEmpty()) {
            tag.putString("CustomName", this.customName);
        }
        tag.putIntArray("InsertRoutes", this.insertRoutes);
        tag.putIntArray("ExtractRoutes", this.extractRoutes);
        tag.putBoolean("AutoInsert", this.autoInsert);
        tag.putShort("AutoInsertCount", (short) this.autoInsertCount);
        tag.putBoolean("AutoExtract", this.autoExtract);
        tag.putShort("AutoExtractCount", (short) this.autoExtractCount);
        this.clayEnergyStorage.save(tag);
        CompoundTag filtersTag = new CompoundTag();
        ContainerHelper.saveAllItems(filtersTag, this.filters, false, registries);
        tag.put("FiltersTag", filtersTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        if (tag.contains("Items")) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
        this.customName = tag.contains("CustomName") ? tag.getString("CustomName") : null;
        if (tag.contains("InsertRoutes")) {
            int[] arr = tag.getIntArray("InsertRoutes");
            System.arraycopy(arr, 0, this.insertRoutes, 0, Math.min(arr.length, 6));
        }
        if (tag.contains("ExtractRoutes")) {
            int[] arr = tag.getIntArray("ExtractRoutes");
            System.arraycopy(arr, 0, this.extractRoutes, 0, Math.min(arr.length, 6));
        }
        if (tag.contains("AutoInsert")) {
            this.autoInsert = tag.getBoolean("AutoInsert");
        }
        this.autoInsertCount = tag.getShort("AutoInsertCount");
        if (tag.contains("AutoExtract")) {
            this.autoExtract = tag.getBoolean("AutoExtract");
        }
        this.autoExtractCount = tag.getShort("AutoExtractCount");
        this.clayEnergyStorage.load(tag);
        for (int i = 0; i < 6; i++) {
            this.filters.set(i, ItemStack.EMPTY);
        }
        if (tag.contains("FiltersTag")) {
            ContainerHelper.loadAllItems(tag.getCompound("FiltersTag"), this.filters, registries);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Sided or unsided NeoForge {@link IItemHandler} view backed by this container's slots and filters.
     */
    protected class ContainerItemHandler implements IItemHandler {
        @Nullable
        protected final Direction side;

        public ContainerItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return ClayContainerBlockEntity.this.items.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return getStack(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || slot < 0 || slot >= getSlots()) {
                return stack;
            }
            if (this.side != null) {
                if (!canInsertToSide(slot, stack, toRelativeSide(this.side))) {
                    return stack;
                }
            } else if (!isItemValidForSlot(slot, stack)) {
                return stack;
            }

            int limit = Math.min(stack.getMaxStackSize(), getInventoryStackLimit(slot));
            ItemStack existing = getStack(slot);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            int space = limit - existing.getCount();
            if (space <= 0) {
                return stack;
            }
            int moved = Math.min(stack.getCount(), space);
            if (!simulate) {
                if (existing.isEmpty()) {
                    setStack(slot, stack.copyWithCount(moved));
                } else {
                    existing.grow(moved);
                    markForSync();
                }
            }
            return moved >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || slot < 0 || slot >= getSlots()) {
                return ItemStack.EMPTY;
            }
            ItemStack existing = getStack(slot);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (this.side != null && !canExtractFromSide(slot, existing, toRelativeSide(this.side))) {
                return ItemStack.EMPTY;
            }
            int taken = Math.min(amount, existing.getCount());
            ItemStack result = existing.copyWithCount(taken);
            if (!simulate) {
                existing.shrink(taken);
                if (existing.isEmpty()) {
                    setStack(slot, ItemStack.EMPTY);
                } else {
                    markForSync();
                }
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return getInventoryStackLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (this.side != null) {
                return canInsertToSide(slot, stack, toRelativeSide(this.side));
            }
            return isItemValidForSlot(slot, stack);
        }
    }

    public static String modId() {
        return ClayiumMod.MOD_ID;
    }
}
