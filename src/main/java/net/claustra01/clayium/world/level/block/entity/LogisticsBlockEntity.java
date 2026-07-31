/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.EnumMap;
import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.LogisticsKind;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    public static final int STORAGE_CONTENT_SLOT = 0;
    public static final int STORAGE_INPUT_SLOT = 1;
    public static final int CONTAINER_FILTER_SLOT = INVENTORY_SLOTS;
    public static final long STORAGE_CAPACITY = 65_536L;
    private NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private int[] insertionRoutes = new int[]{-1, -1, -1, 0, -1, -1};
    private int[] extractionRoutes = new int[]{-1, -1, -1, -1, -1, -1};
    private final FilterSettings[] filters = new FilterSettings[6];
    private final EnumMap<Direction, IItemHandler> handlers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches =
            new EnumMap<>(Direction.class);
    private int transferCooldown;
    private int distributorSide;
    private long storedCount;

    public LogisticsBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.LOGISTICS_BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(filters, FilterSettings.DEFAULT);
        LogisticsKind initialKind = state.getBlock() instanceof LogisticsBlock block
                ? block.kind()
                : LogisticsKind.BUFFER;
        if (initialKind == LogisticsKind.DISTRIBUTOR) {
            extractionRoutes = new int[]{0, 0, 0, -1, 0, 0};
        } else if (initialKind == LogisticsKind.STORAGE_CONTAINER) {
            insertionRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        } else if (initialKind == LogisticsKind.VOID_CONTAINER) {
            insertionRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        }
        for (Direction direction : Direction.values()) {
            handlers.put(direction, new SidedHandler(direction));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LogisticsBlockEntity blockEntity) {
        if (++blockEntity.transferCooldown >= blockEntity.transferInterval()) {
            blockEntity.transferCooldown = 0;
            blockEntity.transferItems();
        }
    }

    private void transferItems() {
        if (level == null) {
            return;
        }
        Direction[] directions = Direction.values();
        int start = kind() == LogisticsKind.DISTRIBUTOR ? distributorSide : 0;
        for (int offset = 0; offset < directions.length; offset++) {
            Direction direction = directions[(start + offset) % directions.length];
            int relative = relativeIndex(direction);
            if (insertionRoutes[relative] >= 0 && pullFrom(direction, insertionRoutes[relative])) {
                return;
            }
            if (kind() == LogisticsKind.VOID_CONTAINER || extractionRoutes[relative] < 0) {
                continue;
            }
            IItemHandler target = targetHandler(direction);
            if (target != null && transferTo(target, transferLimit(), extractionRoutes[relative])) {
                if (kind() == LogisticsKind.DISTRIBUTOR) {
                    distributorSide = (direction.ordinal() + 1) % directions.length;
                }
                setChanged();
                return;
            }
        }
    }

    private boolean pullFrom(Direction direction, int route) {
        IItemHandler source = targetHandler(direction);
        if (source == null) {
            return false;
        }
        IItemHandler destination = itemHandler(direction);
        for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
            ItemStack offered = source.extractItem(sourceSlot, transferLimit(), true);
            if (offered.isEmpty()) {
                continue;
            }
            ItemStack remainder = offered;
            for (int targetSlot : slotsForRoute(route)) {
                if (remainder.isEmpty()) {
                    break;
                }
                remainder = destination.insertItem(targetSlot, remainder, true);
            }
            int accepted = offered.getCount() - remainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
            ItemStack uninserted = extracted;
            for (int targetSlot : slotsForRoute(route)) {
                if (uninserted.isEmpty()) {
                    break;
                }
                uninserted = destination.insertItem(targetSlot, uninserted, false);
            }
            return uninserted.isEmpty();
        }
        return false;
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

    private boolean transferTo(IItemHandler target, int maximum, int route) {
        for (int sourceSlot : slotsForRoute(route)) {
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

    public int cycleInsertRoute(Direction direction) {
        int index = relativeIndex(direction);
        insertionRoutes[index] = nextRoute(insertionRoutes[index]);
        configurationChanged();
        return insertionRoutes[index];
    }

    @Override
    public int cycleExtractRoute(Direction direction) {
        int index = relativeIndex(direction);
        extractionRoutes[index] = nextRoute(extractionRoutes[index]);
        configurationChanged();
        return extractionRoutes[index];
    }

    @Override
    public boolean togglePipe() {
        if (level == null) {
            return false;
        }
        boolean pipe = !getBlockState().getValue(LogisticsBlock.PIPE);
        level.setBlock(worldPosition, getBlockState().setValue(LogisticsBlock.PIPE, pipe), 3);
        configurationChanged();
        return pipe;
    }

    @Override
    public boolean rotate(Direction clickedFace) {
        if (level == null || !clickedFace.getAxis().isHorizontal()) {
            return false;
        }
        Direction current = getBlockState().getValue(LogisticsBlock.FACING);
        Direction next = clickedFace == current ? clickedFace.getOpposite() : clickedFace;
        level.setBlock(worldPosition, getBlockState().setValue(LogisticsBlock.FACING, next), 3);
        configurationChanged();
        return true;
    }

    public void setFilter(Direction direction, FilterSettings filter) {
        filters[relativeIndex(direction)] = filter;
        configurationChanged();
    }

    public IoMemory saveIoMemory() {
        return IoMemory.of(
                insertionRoutes,
                extractionRoutes,
                getBlockState().getValue(LogisticsBlock.PIPE),
                getBlockState().getValue(LogisticsBlock.FACING).getName());
    }

    public void loadIoMemory(IoMemory memory) {
        insertionRoutes = sanitizeRoutes(memory.insertionRoutesOrDefault(insertionRoutes), routeCount());
        extractionRoutes = sanitizeRoutes(memory.extractionRoutesOrDefault(extractionRoutes), routeCount());
        if (level != null) {
            Direction facing = Direction.byName(memory.facing());
            BlockState state = getBlockState().setValue(LogisticsBlock.PIPE, memory.pipe());
            if (facing != null && facing.getAxis().isHorizontal()) {
                state = state.setValue(LogisticsBlock.FACING, facing);
            }
            level.setBlock(worldPosition, state, 3);
        }
        configurationChanged();
    }

    public int activeSlots() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 1 : block.kind().slots(block.tier());
    }

    public int filterSlots() {
        LogisticsBlock block = logisticsBlock();
        if (block == null) {
            return 0;
        }
        return block.kind() == LogisticsKind.STORAGE_CONTAINER
                        || block.kind() == LogisticsKind.VOID_CONTAINER
                ? 1
                : block.kind().tracks(block.tier());
    }

    public int inventoryColumns() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 1 : block.kind().columns(block.tier());
    }

    public int inventoryRows() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 1 : block.kind().rows(block.tier());
    }

    public boolean isMultitrack() {
        return kind() == LogisticsKind.MULTITRACK_BUFFER;
    }

    public LogisticsKind kindValue() {
        return kind();
    }

    public int tierValue() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 4 : block.tier();
    }

    public long storedCount() {
        return storedCount;
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

    private boolean matchesContainerFilter(ItemStack stack) {
        if (kind() != LogisticsKind.STORAGE_CONTAINER && kind() != LogisticsKind.VOID_CONTAINER) {
            return true;
        }
        ItemStack filterStack = items.get(CONTAINER_FILTER_SLOT);
        if (filterStack.isEmpty()) {
            return true;
        }
        return filterStack
                .getOrDefault(ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT)
                .matches(stack);
    }

    private int transferLimit() {
        LogisticsBlock block = logisticsBlock();
        return block == null ? 1 : block.kind().transferLimit(block.tier());
    }

    private int transferInterval() {
        LogisticsBlock block = logisticsBlock();
        if (block == null) {
            return 8;
        }
        int tier = block.tier();
        return tier <= 4 ? 8 : tier == 5 ? 4 : tier == 6 ? 2 : 1;
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
        if (kind() == LogisticsKind.STORAGE_CONTAINER) {
            ItemStack current = items.get(STORAGE_CONTENT_SLOT);
            return slot == STORAGE_INPUT_SLOT
                    && matchesContainerFilter(stack)
                    && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack))
                    && storedCount < STORAGE_CAPACITY;
        }
        if (kind() == LogisticsKind.VOID_CONTAINER) {
            return slot == STORAGE_CONTENT_SLOT && matchesContainerFilter(stack);
        }
        return slot >= 0
                && slot < activeSlots()
                && matchesTrackFilter(slot, stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (kind() == LogisticsKind.VOID_CONTAINER && slot == STORAGE_CONTENT_SLOT) {
            items.set(slot, matchesContainerFilter(stack) ? ItemStack.EMPTY : stack);
            setChanged();
            return;
        }
        if (kind() != LogisticsKind.STORAGE_CONTAINER) {
            super.setItem(slot, stack);
            return;
        }
        if (slot == CONTAINER_FILTER_SLOT) {
            super.setItem(slot, stack);
            return;
        }
        if (slot != STORAGE_INPUT_SLOT || stack.isEmpty() || !canPlaceItem(slot, stack)) {
            return;
        }
        int accepted = (int) Math.min(stack.getCount(), STORAGE_CAPACITY - storedCount);
        if (items.get(STORAGE_CONTENT_SLOT).isEmpty()) {
            items.set(STORAGE_CONTENT_SLOT, stack.copyWithCount(1));
        }
        storedCount += accepted;
        items.set(
                STORAGE_INPUT_SLOT,
                accepted == stack.getCount()
                        ? ItemStack.EMPTY
                        : stack.copyWithCount(stack.getCount() - accepted));
        syncStorageDisplay();
        setChanged();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (kind() != LogisticsKind.STORAGE_CONTAINER) {
            return super.removeItem(slot, amount);
        }
        if (slot == CONTAINER_FILTER_SLOT || slot == STORAGE_INPUT_SLOT) {
            return super.removeItem(slot, amount);
        }
        if (slot != STORAGE_CONTENT_SLOT
                || amount <= 0
                || storedCount <= 0
                || items.get(STORAGE_CONTENT_SLOT).isEmpty()) {
            return ItemStack.EMPTY;
        }
        int removed = (int) Math.min(
                Math.min(amount, items.get(STORAGE_CONTENT_SLOT).getMaxStackSize()), storedCount);
        ItemStack result = items.get(STORAGE_CONTENT_SLOT).copyWithCount(removed);
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
        if (slot == CONTAINER_FILTER_SLOT || slot == STORAGE_INPUT_SLOT) {
            return super.removeItemNoUpdate(slot);
        }
        return removeItem(slot, Integer.MAX_VALUE);
    }

    private void syncStorageDisplay() {
        if (storedCount <= 0) {
            storedCount = 0;
            items.set(STORAGE_CONTENT_SLOT, ItemStack.EMPTY);
        } else if (!items.get(STORAGE_CONTENT_SLOT).isEmpty()) {
            items.get(STORAGE_CONTENT_SLOT).setCount(
                    (int) Math.min(storedCount, items.get(STORAGE_CONTENT_SLOT).getMaxStackSize()));
        }
    }

    public void dropAllStoredContents() {
        if (level == null || kind() != LogisticsKind.STORAGE_CONTAINER) {
            return;
        }
        while (storedCount > 0 && !items.get(STORAGE_CONTENT_SLOT).isEmpty()) {
            ItemStack dropped = removeItem(
                    STORAGE_CONTENT_SLOT, items.get(STORAGE_CONTENT_SLOT).getMaxStackSize());
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
        if (kind() == LogisticsKind.STORAGE_CONTAINER) {
            storedCount = Math.max(
                    items.get(STORAGE_CONTENT_SLOT).getCount(), tag.getLong("StoredCount"));
            syncStorageDisplay();
        } else {
            storedCount = 0;
        }
        insertionRoutes = loadRoutes(tag, "InsertionRoutes", insertionRoutes, routeCount());
        extractionRoutes = loadRoutes(tag, "ExtractionRoutes", extractionRoutes, routeCount());
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
        tag.putIntArray("InsertionRoutes", insertionRoutes);
        tag.putIntArray("ExtractionRoutes", extractionRoutes);
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

    public int insertionRoute(Direction side) {
        return insertionRoutes[relativeIndex(side)];
    }

    public int extractionRoute(Direction side) {
        return extractionRoutes[relativeIndex(side)];
    }

    public boolean hasFilter(Direction side) {
        return !filters[relativeIndex(side)].equals(FilterSettings.DEFAULT);
    }

    public boolean isPassivePipeEndpoint() {
        return kind() == LogisticsKind.BUFFER
                || kind() == LogisticsKind.MULTITRACK_BUFFER
                || kind() == LogisticsKind.STORAGE_CONTAINER;
    }

    public boolean pipeConnects(Direction side) {
        if (level == null) {
            return false;
        }
        boolean ownActive = insertionRoute(side) >= 0 || extractionRoute(side) >= 0;
        var neighbor = level.getBlockEntity(worldPosition.relative(side));
        if (neighbor instanceof ConfigurableItemDevice device) {
            boolean neighborActive =
                    device.insertionRoute(side.getOpposite()) >= 0
                            || device.extractionRoute(side.getOpposite()) >= 0;
            boolean neighborPassive =
                    neighbor instanceof LogisticsBlockEntity logistics && logistics.isPassivePipeEndpoint();
            return ownActive && (neighborActive || neighborPassive)
                    || isPassivePipeEndpoint() && neighborActive;
        }
        return ownActive && neighbor != null;
    }

    public String insertionIcon(Direction side) {
        int route = insertionRoute(side);
        if (route < 0) {
            return "";
        }
        return kind() == LogisticsKind.MULTITRACK_BUFFER ? "import_m" + route : "import";
    }

    public String extractionIcon(Direction side) {
        int route = extractionRoute(side);
        if (route < 0) {
            return "";
        }
        return kind() == LogisticsKind.MULTITRACK_BUFFER ? "export_m" + route : "export";
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private int relativeIndex(Direction direction) {
        Direction front = getBlockState().getValue(LogisticsBlock.FACING);
        return RelativeFace.index(front, direction);
    }

    private void configurationChanged() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private int nextRoute(int route) {
        return route + 1 >= routeCount() ? -1 : route + 1;
    }

    private int routeCount() {
        return kind() == LogisticsKind.MULTITRACK_BUFFER ? filterSlots() + 1 : 1;
    }

    private int[] slotsForRoute(int route) {
        if (route < 0 || route >= routeCount()) {
            return new int[0];
        }
        if (kind() != LogisticsKind.MULTITRACK_BUFFER || route == 0) {
            int[] slots = new int[activeSlots()];
            java.util.Arrays.setAll(slots, index -> index);
            return slots;
        }
        int trackSize = activeSlots() / filterSlots();
        int[] slots = new int[trackSize];
        java.util.Arrays.setAll(slots, index -> (route - 1) * trackSize + index);
        return slots;
    }

    private boolean routeContains(int route, int slot) {
        for (int candidate : slotsForRoute(route)) {
            if (candidate == slot) {
                return true;
            }
        }
        return false;
    }

    private static int[] loadRoutes(CompoundTag tag, String key, int[] defaults, int routeCount) {
        return tag.contains(key) ? sanitizeRoutes(tag.getIntArray(key), routeCount) : defaults;
    }

    private static int[] sanitizeRoutes(int[] routes, int routeCount) {
        if (routes.length != RelativeFace.COUNT) {
            return new int[]{-1, -1, -1, -1, -1, -1};
        }
        int[] result = routes.clone();
        for (int index = 0; index < result.length; index++) {
            if (result[index] < -1 || result[index] >= routeCount) {
                result[index] = -1;
            }
        }
        return result;
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
                    || !routeContains(insertionRoutes[relativeIndex(side)], slot)
                    || !filters[relativeIndex(side)].matches(stack)
                    || !matchesContainerFilter(stack)
                    || !matchesTrackFilter(slot, stack)) {
                return stack;
            }
            if (kind() == LogisticsKind.VOID_CONTAINER) {
                return ItemStack.EMPTY;
            }
            if (kind() == LogisticsKind.STORAGE_CONTAINER) {
                ItemStack current = items.get(STORAGE_CONTENT_SLOT);
                if (!matchesContainerFilter(stack)
                        || !current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                    return stack;
                }
                int accepted = (int) Math.min(stack.getCount(), STORAGE_CAPACITY - storedCount);
                if (accepted <= 0) {
                    return stack;
                }
                if (!simulate) {
                    if (current.isEmpty()) {
                        items.set(STORAGE_CONTENT_SLOT, stack.copyWithCount(1));
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
            if (!routeContains(extractionRoutes[relativeIndex(side)], slot) || amount <= 0) {
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
            return routeContains(insertionRoutes[relativeIndex(side)], slot)
                    && filters[relativeIndex(side)].matches(stack)
                    && matchesTrackFilter(slot, stack);
        }

        private void validate(int slot) {
            if (slot < 0 || slot >= activeSlots()) {
                throw new IndexOutOfBoundsException("Logistics slot: " + slot);
            }
        }
    }
}
