package net.claustra01.clayium.logistics;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.logistics.filter.ItemFilterRule;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
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
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BufferBlockEntity extends BlockEntity implements MenuProvider {
    public static final int STORAGE_SLOTS = 100;

    private final ItemStackHandler inventory = new ItemStackHandler(STORAGE_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return acceptsInput() && ItemFilter.accepts(filterInventory.getStackInSlot(0), stack);
        }
    };
    private final ItemStackHandler filterInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            updateDisplay();
            setChangedAndSync();
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
    private final EnumMap<Direction, FaceMode> faceModes = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final IItemHandler unsidedHandler = new BufferItemHandler(null);
    private final ContainerData menuData = new BufferContainerData();

    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches;
    private RedstoneCondition redstoneCondition = RedstoneCondition.IGNORE;
    private int transferLimit;
    private int insertTick;
    private int extractTick;
    private int nextOutputDirection;
    private int nextInputDirection;
    private ItemFilterRule.MatchType selectedRuleType = ItemFilterRule.MatchType.FUZZY;
    private int selectedTag;
    private final ItemStackHandler exampleInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public BufferBlockEntity(BlockPos pos, BlockState state) {
        this(LogisticsRegistration.BUFFER_BLOCK_ENTITY.get(), pos, state);
    }

    protected BufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (Direction direction : Direction.values()) {
            faceModes.put(direction, direction == Direction.DOWN ? FaceMode.OUTPUT : FaceMode.INPUT);
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
        if (!redstoneCondition.allows(serverLevel.hasNeighborSignal(worldPosition))) {
            return;
        }
        ensureCaches(serverLevel);
        if (extractDue) {
            int maximum = transferLimit == 0
                    ? parameters.maximumExtractItems()
                    : Math.min(transferLimit, parameters.maximumExtractItems());
            pushItems(maximum);
        }
        if (insertDue && acceptsInput()) {
            int maximum = transferLimit == 0
                    ? parameters.maximumInsertItems()
                    : Math.min(transferLimit, parameters.maximumInsertItems());
            pullItems(maximum);
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
            if (faceModes.get(direction) != FaceMode.OUTPUT) {
                continue;
            }
            IItemHandler target = neighborCaches.get(direction).getCapability();
            if (target == null) {
                continue;
            }
            int moved = moveItems(inventory, target, this::accepts, remaining, this::dropRemainder);
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
            if (faceModes.get(direction) != FaceMode.INPUT) {
                continue;
            }
            IItemHandler source = neighborCaches.get(direction).getCapability();
            if (source == null) {
                continue;
            }
            int moved = moveItems(source, inventory, this::accepts, remaining, this::dropRemainder);
            if (moved > 0) {
                remaining -= moved;
                nextInputDirection = (index + 1) % Direction.values().length;
            }
        }
    }

    public static int moveItems(
            IItemHandler source, IItemHandler target, Predicate<ItemStack> filter, int maximum, Consumer<ItemStack> onUnrestored) {
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
                int inserted = extracted.getCount() - rejected.getCount();
                moved += inserted;
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

    private boolean accepts(ItemStack stack) {
        return ItemFilter.accepts(filterInventory.getStackInSlot(0), stack);
    }

    public int tier() {
        return ((BufferBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ItemStackHandler exampleInventory() {
        return exampleInventory;
    }

    public String selectedTagName() {
        List<String> tags = exampleInventory.getStackInSlot(0).getTags()
                .map(tag -> tag.location().toString()).sorted().toList();
        return tags.isEmpty() ? "" : tags.get(Math.floorMod(selectedTag, tags.size()));
    }

    public void cycleSelectedTag() {
        selectedTag++;
        setChangedAndSync();
    }

    public ItemStackHandler filterInventory() {
        return filterInventory;
    }

    public ItemStack filterStack() {
        return filterInventory.getStackInSlot(0);
    }

    public void setFilterStack(ItemStack filter) {
        filterInventory.setStackInSlot(0, filter.is(LogisticsRegistration.ITEM_FILTER.get())
                ? filter.copyWithCount(1) : ItemStack.EMPTY);
    }

    public FaceMode faceMode(Direction direction) {
        return faceModes.get(direction);
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        faceModes.put(direction, mode);
        updateDisplay();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        setChangedAndSync();
    }

    public RedstoneCondition redstoneCondition() {
        return redstoneCondition;
    }

    public void setRedstoneCondition(RedstoneCondition condition) {
        redstoneCondition = condition;
        setChangedAndSync();
    }

    public int transferLimit() {
        return transferLimit;
    }

    public void setTransferLimit(int limit) {
        transferLimit = Math.max(0, Math.min(TierManager.bufferTransport(tier()).maximumItems(), limit));
        setChangedAndSync();
    }

    public ItemFilterRule.MatchType selectedRuleType() {
        return selectedRuleType;
    }

    public void cycleSelectedRuleType() {
        selectedRuleType = selectedRuleType.next();
        setChangedAndSync();
    }

    public ItemFilterSettings filterSettings() {
        return filterStack().getOrDefault(
                LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL);
    }

    public void cycleFilterMode() {
        ensureFilterItem();
        ItemFilterSettings settings = filterSettings();
        filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings.withMode(settings.mode().next()));
        setChangedAndSync();
    }

    public void addSelectedRule() {
        ItemStack example = exampleInventory.getStackInSlot(0);
        if (example.isEmpty()) {
            return;
        }
        ItemFilterRule rule = ItemFilterRule.fromExample(selectedRuleType, example);
        if (selectedRuleType == ItemFilterRule.MatchType.TAG) {
            List<String> tags = example.getTags().map(tag -> tag.location().toString()).sorted().toList();
            if (!tags.isEmpty()) {
                rule = new ItemFilterRule(selectedRuleType, tags.get(Math.floorMod(selectedTag, tags.size())), ItemStack.EMPTY);
            }
        }
        if ((selectedRuleType == ItemFilterRule.MatchType.TAG && rule.value().isEmpty())
                || (selectedRuleType == ItemFilterRule.MatchType.NESTED && rule.reference().isEmpty())) {
            return;
        }
        ensureFilterItem();
        filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), filterSettings().withRule(rule));
        setChangedAndSync();
    }

    public void removeLastRule() {
        if (filterStack().isEmpty()) {
            return;
        }
        filterStack().set(
                LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), filterSettings().withoutLastRule());
        setChangedAndSync();
    }

    private void ensureFilterItem() {
        if (filterStack().isEmpty()) {
            setFilterStack(LogisticsRegistration.ITEM_FILTER.toStack());
        }
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    public BufferConfiguration copyConfiguration() {
        List<Integer> faces = java.util.Arrays.stream(Direction.values())
                .map(direction -> faceModes.get(direction).ordinal()).toList();
        return new BufferConfiguration(faces, redstoneCondition, transferLimit, filterStack());
    }

    public void applyConfiguration(BufferConfiguration configuration) {
        for (Direction direction : Direction.values()) {
            int ordinal = configuration.faces().get(direction.get3DDataValue());
            if (ordinal >= 0 && ordinal < FaceMode.values().length) {
                faceModes.put(direction, FaceMode.values()[ordinal]);
            }
        }
        redstoneCondition = configuration.redstoneCondition();
        setTransferLimit(configuration.transferLimit());
        ItemStack previousFilter = filterStack().copy();
        if (!previousFilter.isEmpty() && !ItemStack.matches(previousFilter, configuration.filter()) && level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, previousFilter);
        }
        setFilterStack(configuration.filter());
        updateDisplay();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        setChangedAndSync();
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(STORAGE_SLOTS + 2);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropped.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        dropped.setItem(STORAGE_SLOTS, filterStack().copy());
        filterInventory.setStackInSlot(0, ItemStack.EMPTY);
        dropped.setItem(STORAGE_SLOTS + 1, exampleInventory.getStackInSlot(0).copy());
        exampleInventory.setStackInSlot(0, ItemStack.EMPTY);
        return dropped;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BufferMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("filter", filterInventory.serializeNBT(registries));
        tag.put("example", exampleInventory.serializeNBT(registries));
        tag.putInt("selected_tag", selectedTag);
        CompoundTag faces = new CompoundTag();
        faceModes.forEach((direction, mode) -> faces.putString(direction.getName(), mode.name()));
        tag.put("faces", faces);
        tag.putString("redstone_condition", redstoneCondition.name());
        tag.putInt("transfer_limit", transferLimit);
        tag.putInt("insert_tick", insertTick);
        tag.putInt("extract_tick", extractTick);
        tag.putInt("next_output_direction", nextOutputDirection);
        tag.putInt("next_input_direction", nextInputDirection);
        tag.putString("selected_rule_type", selectedRuleType.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("example")) {
            exampleInventory.deserializeNBT(registries, tag.getCompound("example"));
        }
        selectedTag = tag.getInt("selected_tag");
        if (tag.contains("filter")) {
            filterInventory.deserializeNBT(registries, tag.getCompound("filter"));
        }
        CompoundTag faces = tag.getCompound("faces");
        for (Direction direction : Direction.values()) {
            if (faces.contains(direction.getName())) {
                faceModes.put(direction, parseEnum(
                        FaceMode.class, faces.getString(direction.getName()), faceModes.get(direction)));
            }
        }
        redstoneCondition = parseEnum(
                RedstoneCondition.class, tag.getString("redstone_condition"), RedstoneCondition.IGNORE);
        transferLimit = Math.max(0, tag.getInt("transfer_limit"));
        int legacyTransferTick = Math.max(0, tag.getInt("transfer_tick"));
        insertTick = Math.max(0, tag.contains("insert_tick") ? tag.getInt("insert_tick") : legacyTransferTick);
        extractTick = Math.max(0, tag.contains("extract_tick") ? tag.getInt("extract_tick") : legacyTransferTick);
        nextOutputDirection = Math.floorMod(tag.getInt("next_output_direction"), Direction.values().length);
        nextInputDirection = Math.floorMod(tag.getInt("next_input_direction"), Direction.values().length);
        selectedRuleType = parseEnum(
                ItemFilterRule.MatchType.class, tag.getString("selected_rule_type"), ItemFilterRule.MatchType.FUZZY);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
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
        BlockState state = getBlockState().setValue(BufferBlock.FILTERED, !filterStack().isEmpty());
        for (Direction direction : Direction.values()) {
            state = state.setValue(BufferBlock.FACES.get(direction.get3DDataValue()), faceModes.get(direction));
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
        DISABLED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public FaceMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum RedstoneCondition implements StringRepresentable {
        IGNORE,
        POWERED,
        UNPOWERED;

        public static final Codec<RedstoneCondition> CODEC = StringRepresentable.fromEnum(RedstoneCondition::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public RedstoneCondition next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public boolean allows(boolean powered) {
            return this == IGNORE || this == POWERED && powered || this == UNPOWERED && !powered;
        }
    }

    private final class BufferContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> tier();
                case 1 -> redstoneCondition.ordinal();
                case 2 -> transferLimit;
                case 3 -> filterSettings().mode().ordinal();
                case 4, 5, 6, 7, 8, 9 -> faceModes.get(Direction.values()[index - 4]).ordinal();
                case 10 -> filterSettings().rules().size();
                case 11 -> selectedRuleType.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 12;
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
            return direction != null && faceModes.get(direction) == FaceMode.DISABLED ? 0 : inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return acceptsInput() && (direction == null || faceModes.get(direction) == FaceMode.INPUT)
                    ? inventory.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return direction == null || faceModes.get(direction) == FaceMode.OUTPUT
                    ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return (direction == null || faceModes.get(direction) == FaceMode.INPUT)
                    && inventory.isItemValid(slot, stack);
        }
    }
}
