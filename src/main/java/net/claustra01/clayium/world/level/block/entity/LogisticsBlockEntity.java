/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.EnumMap;
import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.SideMode;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.world.inventory.LogisticsMenu;
import net.claustra01.clayium.world.level.block.LogisticsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.server.level.ServerLevel;

public final class LogisticsBlockEntity extends BaseContainerBlockEntity implements ConfigurableItemDevice {
    public static final int INVENTORY_SLOTS = 54;
    public static final int MAX_SLOTS = 60;
    private NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private final SideMode[] sideModes = new SideMode[6];
    private final FilterSettings[] filters = new FilterSettings[6];
    private final EnumMap<Direction, IItemHandler> handlers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches =
            new EnumMap<>(Direction.class);
    private int transferCooldown;
    private int distributorSide;
    private long storedCount;
    private static final long STORAGE_CAPACITY = 65_536L;

    public LogisticsBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.LOGISTICS_BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(sideModes, SideMode.DISABLED);
        java.util.Arrays.fill(filters, FilterSettings.DEFAULT);
        Direction front = state.hasProperty(LogisticsBlock.FACING)
                ? state.getValue(LogisticsBlock.FACING)
                : Direction.NORTH;
        LogisticsKind initialKind = state.getBlock() instanceof LogisticsBlock block
                ? block.kind()
                : LogisticsKind.BUFFER;
        if (initialKind == LogisticsKind.DISTRIBUTOR) {
            java.util.Arrays.fill(sideModes, SideMode.OUTPUT);
            sideModes[front.getOpposite().ordinal()] = SideMode.INPUT;
        } else if (initialKind == LogisticsKind.STORAGE_CONTAINER) {
            java.util.Arrays.fill(sideModes, SideMode.INPUT_OUTPUT);
        } else if (initialKind == LogisticsKind.VOID_CONTAINER) {
            java.util.Arrays.fill(sideModes, SideMode.INPUT);
        } else {
            sideModes[front.ordinal()] = SideMode.OUTPUT;
            sideModes[front.getOpposite().ordinal()] = SideMode.INPUT;
        }
        for (Direction direction : Direction.values()) {
            handlers.put(direction, new SidedHandler(direction));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LogisticsBlockEntity blockEntity) {
        if (++blockEntity.transferCooldown >= blockEntity.transferInterval()) {
            blockEntity.transferCooldown = 0;
            blockEntity.pushItems();
        }
    }

    private void pushItems() {
        if (level == null || kind() == LogisticsKind.VOID_CONTAINER) {
            return;
        }
        Direction[] directions = Direction.values();
        int start = kind() == LogisticsKind.DISTRIBUTOR ? distributorSide : 0;
        for (int offset = 0; offset < directions.length; offset++) {
            Direction direction = directions[(start + offset) % directions.length];
            if (!sideModes[direction.ordinal()].allowsExtract()) {
                continue;
            }
            IItemHandler target = targetHandler(direction);
            if (target != null && transferTo(target, transferLimit())) {
                if (kind() == LogisticsKind.DISTRIBUTOR) {
                    distributorSide = (direction.ordinal() + 1) % directions.length;
                }
                setChanged();
                return;
            }
        }
    }

    private IItemHandler targetHandler(Direction direction) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return neighborCaches.computeIfAbsent(
                        direction,
                        side -> BlockCapabilityCache.create(
                                Capabilities.ItemHandler.BLOCK,
                                serverLevel,
                                worldPosition.relative(side),
                                side.getOpposite(),
                                () -> !isRemoved(),
                                () -> {
                                }))
                .getCapability();
    }

    private boolean transferTo(IItemHandler target, int maximum) {
        for (int sourceSlot = 0; sourceSlot < activeSlots(); sourceSlot++) {
            ItemStack source = getItem(sourceSlot);
            if (source.isEmpty()) {
                continue;
            }
            ItemStack offered = source.copyWithCount(Math.min(maximum, source.getCount()));
            ItemStack remainder = offered;
            for (int targetSlot = 0; targetSlot < target.getSlots() && !remainder.isEmpty(); targetSlot++) {
                remainder = target.insertItem(targetSlot, remainder, true);
            }
            int accepted = offered.getCount() - remainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            ItemStack committed = offered;
            for (int targetSlot = 0; targetSlot < target.getSlots() && !committed.isEmpty(); targetSlot++) {
                committed = target.insertItem(targetSlot, committed, false);
            }
            int moved = offered.getCount() - committed.getCount();
            if (moved > 0) {
                removeItem(sourceSlot, moved);
                return true;
            }
        }
        return false;
    }

    public IItemHandler itemHandler(Direction direction) {
        return handlers.get(direction);
    }

    public SideMode cycleSide(Direction direction) {
        int index = direction.ordinal();
        sideModes[index] = sideModes[index].next();
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        return sideModes[index];
    }

    public void setFilter(Direction direction, FilterSettings filter) {
        filters[direction.ordinal()] = filter;
        setChanged();
    }

    public IoMemory saveIoMemory() {
        return IoMemory.of(sideModes);
    }

    public void loadIoMemory(IoMemory memory) {
        SideMode[] loaded = memory.modesOrDefault(sideModes);
        System.arraycopy(loaded, 0, sideModes, 0, sideModes.length);
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public int activeSlots() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 1 : block.kind().slots(block.tier());
    }

    public int filterSlots() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 0 : block.kind().tracks(block.tier());
    }

    public int filterSlotIndex(int filter) {
        return INVENTORY_SLOTS + filter;
    }

    public boolean isFilterSlot(int slot) {
        return slot >= INVENTORY_SLOTS && slot < INVENTORY_SLOTS + filterSlots();
    }

    private boolean matchesTrackFilter(int slot, ItemStack stack) {
        if (kind() != LogisticsKind.MULTITRACK_BUFFER) {
            return true;
        }
        int tracks = filterSlots();
        int inventorySlots = activeSlots();
        if (tracks <= 0 || inventorySlots <= 0) {
            return true;
        }
        int trackSize = inventorySlots / tracks;
        int track = Math.min(tracks - 1, slot / Math.max(1, trackSize));
        ItemStack filterStack = items.get(filterSlotIndex(track));
        if (filterStack.isEmpty()) {
            return true;
        }
        FilterSettings settings = filterStack.getOrDefault(
                ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        return settings.matches(stack);
    }

    private int transferLimit() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 1 : block.kind().transferLimit(block.tier());
    }

    private int transferInterval() {
        LogisticsBlock block = logisticsBlock();
        return block != null && block.kind() == LogisticsKind.DISTRIBUTOR ? 1 : 8;
    }

    private LogisticsKind kind() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? LogisticsKind.BUFFER : block.kind();
    }

    private LogisticsBlock logisticsBlock() {
        return getBlockState().getBlock() instanceof LogisticsBlock block ? block : null;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new LogisticsMenu(id, inventory, this);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        items = stacks;
    }

    @Override
    public int getContainerSize() {
        return MAX_SLOTS;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (isFilterSlot(slot)) {
            return stack.is(ClayiumRegistries.SMART_FILTER.get());
        }
        return slot >= 0
                && slot < activeSlots()
                && kind() != LogisticsKind.VOID_CONTAINER
                && matchesTrackFilter(slot, stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (kind() != LogisticsKind.STORAGE_CONTAINER) {
            super.setItem(slot, stack);
            return;
        }
        if (slot != 0) {
            return;
        }
        items.set(0, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize())));
        storedCount = stack.isEmpty() ? 0 : stack.getCount();
        setChanged();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (kind() != LogisticsKind.STORAGE_CONTAINER) {
            return super.removeItem(slot, amount);
        }
        if (slot != 0 || amount <= 0 || storedCount <= 0 || items.get(0).isEmpty()) {
            return ItemStack.EMPTY;
        }
        int removed = (int) Math.min(Math.min(amount, items.get(0).getMaxStackSize()), storedCount);
        ItemStack result = items.get(0).copyWithCount(removed);
        storedCount -= removed;
        syncStorageDisplay();
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (kind() != LogisticsKind.STORAGE_CONTAINER) {
            return super.removeItemNoUpdate(slot);
        }
        return removeItem(slot, Integer.MAX_VALUE);
    }

    private void syncStorageDisplay() {
        if (storedCount <= 0) {
            storedCount = 0;
            items.set(0, ItemStack.EMPTY);
        } else if (!items.get(0).isEmpty()) {
            items.get(0).setCount((int) Math.min(storedCount, items.get(0).getMaxStackSize()));
        }
    }

    public void dropAllStoredContents() {
        if (level == null || kind() != LogisticsKind.STORAGE_CONTAINER) {
            return;
        }
        while (storedCount > 0 && !items.get(0).isEmpty()) {
            ItemStack dropped = removeItem(0, items.get(0).getMaxStackSize());
            if (dropped.isEmpty()) {
                break;
            }
            Containers.dropItemStack(
                    level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5,
                    dropped);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        storedCount = Math.max(items.get(0).getCount(), tag.getLong("StoredCount"));
        syncStorageDisplay();
        int[] savedModes = tag.getIntArray("SideModes");
        for (int index = 0; index < Math.min(6, savedModes.length); index++) {
            sideModes[index] = SideMode.values()[Math.max(0, Math.min(SideMode.values().length - 1, savedModes[index]))];
        }
        distributorSide = Math.floorMod(tag.getInt("DistributorSide"), 6);
        ListTag savedFilters = tag.getList("Filters", Tag.TAG_COMPOUND);
        for (int index = 0; index < Math.min(6, savedFilters.size()); index++) {
            CompoundTag saved = savedFilters.getCompound(index);
            ListTag ids = saved.getList("Items", Tag.TAG_STRING);
            java.util.ArrayList<ResourceLocation> itemIds = new java.util.ArrayList<>();
            for (int itemIndex = 0; itemIndex < ids.size(); itemIndex++) {
                ResourceLocation id = ResourceLocation.tryParse(ids.getString(itemIndex));
                if (id != null) {
                    itemIds.add(id);
                }
            }
            filters[index] = new FilterSettings(saved.getBoolean("Blacklist"), itemIds);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (kind() == LogisticsKind.STORAGE_CONTAINER) {
            tag.putLong("StoredCount", storedCount);
        }
        tag.putIntArray("SideModes", java.util.Arrays.stream(sideModes).mapToInt(Enum::ordinal).toArray());
        tag.putInt("DistributorSide", distributorSide);
        ListTag savedFilters = new ListTag();
        for (FilterSettings filter : filters) {
            CompoundTag saved = new CompoundTag();
            saved.putBoolean("Blacklist", filter.blacklist());
            ListTag ids = new ListTag();
            filter.itemIds().forEach(id -> ids.add(StringTag.valueOf(id.toString())));
            saved.put("Items", ids);
            savedFilters.add(saved);
        }
        tag.put("Filters", savedFilters);
    }

    private final class SidedHandler implements IItemHandler {
        private final Direction side;

        private SidedHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return activeSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            validate(slot);
            return getItem(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            validate(slot);
            if (stack.isEmpty()
                    || !sideModes[side.ordinal()].allowsInsert()
                    || !filters[side.ordinal()].matches(stack)
                    || !matchesTrackFilter(slot, stack)) {
                return stack;
            }
            if (kind() == LogisticsKind.VOID_CONTAINER) {
                return ItemStack.EMPTY;
            }
            if (kind() == LogisticsKind.STORAGE_CONTAINER) {
                ItemStack current = items.get(0);
                if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                    return stack;
                }
                int accepted = (int) Math.min(stack.getCount(), STORAGE_CAPACITY - storedCount);
                if (accepted <= 0) {
                    return stack;
                }
                if (!simulate) {
                    if (current.isEmpty()) {
                        items.set(0, stack.copyWithCount(1));
                    }
                    storedCount += accepted;
                    syncStorageDisplay();
                    setChanged();
                }
                return accepted == stack.getCount()
                        ? ItemStack.EMPTY
                        : stack.copyWithCount(stack.getCount() - accepted);
            }
            ItemStack current = getItem(slot);
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                return stack;
            }
            int accepted = Math.min(stack.getCount(), stack.getMaxStackSize() - current.getCount());
            if (accepted <= 0) {
                return stack;
            }
            if (!simulate) {
                ItemStack result = current.isEmpty() ? stack.copyWithCount(accepted) : current.copy();
                if (!current.isEmpty()) {
                    result.grow(accepted);
                }
                setItem(slot, result);
            }
            return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            validate(slot);
            if (!sideModes[side.ordinal()].allowsExtract() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack current = getItem(slot);
            int count = kind() == LogisticsKind.STORAGE_CONTAINER
                    ? (int) Math.min(Math.min(amount, storedCount), current.getMaxStackSize())
                    : Math.min(amount, current.getCount());
            if (count <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = current.copyWithCount(count);
            if (!simulate) {
                removeItem(slot, count);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            validate(slot);
            return kind() == LogisticsKind.STORAGE_CONTAINER ? (int) STORAGE_CAPACITY : getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validate(slot);
            return sideModes[side.ordinal()].allowsInsert()
                    && filters[side.ordinal()].matches(stack)
                    && matchesTrackFilter(slot, stack);
        }

        private void validate(int slot) {
            if (slot < 0 || slot >= activeSlots()) {
                throw new IndexOutOfBoundsException("Logistics slot: " + slot);
            }
        }
    }
}
