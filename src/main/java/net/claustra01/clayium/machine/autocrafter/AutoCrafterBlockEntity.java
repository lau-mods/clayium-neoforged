package net.claustra01.clayium.machine.autocrafter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class AutoCrafterBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    private static final List<WeakReference<AutoCrafterBlockEntity>> LOADED_CRAFTERS = new ArrayList<>();

    static {
        NeoForge.EVENT_BUS.addListener(ClayiumRecipesReloadedEvent.class, event -> {
            LOADED_CRAFTERS.removeIf(reference -> reference.get() == null);
            for (WeakReference<AutoCrafterBlockEntity> reference : LOADED_CRAFTERS) {
                AutoCrafterBlockEntity crafter = reference.get();
                if (crafter != null && crafter.level instanceof ServerLevel serverLevel
                        && serverLevel.getRecipeManager() == event.recipeManager()) {
                    crafter.previewDirty = true;
                }
            }
        });
    }
    public static final int PATTERN_SLOTS = 9;
    public static final int INPUT_SLOTS = 9;
    public static final int OUTPUT_SLOTS = 6;
    public static final int OUTPUT_START = INPUT_SLOTS;
    public static final int FUEL_SLOT = OUTPUT_START + OUTPUT_SLOTS;

    private final ItemStackHandler inventory = new ItemStackHandler(FUEL_SLOT + 1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < INPUT_SLOTS
                    || slot == FUEL_SLOT && ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
        }
    };
    private final ItemStackHandler pattern = new ItemStackHandler(PATTERN_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            previewDirty = true;
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private final ItemStackHandler preview = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private final int[] insertionRoutes = {-1, 0, -1, 1, -1, -1};
    private final int[] extractionRoutes = {0, -1, -1, -1, -1, -1};
    private final ItemStack[] sideFilters = new ItemStack[6];
    private final SideConfiguration sideConfiguration = new SideConfiguration(
            insertionRoutes, extractionRoutes, sideFilters, () -> 2, () -> 1, ItemFilter::accepts);
    private final IItemHandler unsidedHandler = new AutoCrafterItemHandler(null);
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final ContainerData menuData = new AutoCrafterContainerData();

    private int craftTick;
    private boolean previewDirty = true;

    public AutoCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(AutoCrafterRegistration.BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(sideFilters, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sidedHandlers.put(direction, new AutoCrafterItemHandler(direction));
        LOADED_CRAFTERS.add(new WeakReference<>(this));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutoCrafterBlockEntity autoCrafter) {
        autoCrafter.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (previewDirty) {
            refreshPreview(serverLevel);
        }
        AutoCrafterSpecification specification = specification();
        craftTick++;
        if (craftTick < specification.intervalTicks()) {
            setChanged();
            return;
        }
        craftTick = 0;
        RecipeHolder<CraftingRecipe> lastRecipe = null;
        for (int craft = 0; craft < specification.craftsPerCycle(); craft++) {
            CraftPlan plan = planCraft(serverLevel, lastRecipe);
            if (plan == null || !craftOne(plan, specification.energyPerCraft())) {
                break;
            }
            lastRecipe = plan.recipe();
        }
        if (specification.intervalTicks() > 1) {
            setChanged();
        }
    }

    private boolean craftOne(CraftPlan plan, long energyPerCraft) {
        if (!energy.consume(ClayEnergy.ofInternal(energyPerCraft), inventory.getStackInSlot(FUEL_SLOT))) {
            return false;
        }
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (plan.consumed()[slot] > 0) {
                inventory.extractItem(slot, plan.consumed()[slot], false);
            }
        }
        for (ItemStack result : plan.outputs()) {
            ItemStack remaining = insertIntoOutputs(result);
            if (!remaining.isEmpty()) {
                throw new IllegalStateException("Simulated auto crafter output no longer fits");
            }
        }
        return true;
    }

    @Nullable
    private CraftPlan planCraft(ServerLevel serverLevel, @Nullable RecipeHolder<CraftingRecipe> lastRecipe) {
        int[] remaining = new int[INPUT_SLOTS];
        int[] consumed = new int[INPUT_SLOTS];
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            remaining[slot] = inventory.getStackInSlot(slot).getCount();
        }
        List<ItemStack> grid = new ArrayList<>(PATTERN_SLOTS);
        for (int gridSlot = 0; gridSlot < PATTERN_SLOTS; gridSlot++) {
            ItemStack expected = pattern.getStackInSlot(gridSlot);
            if (expected.isEmpty()) {
                grid.add(ItemStack.EMPTY);
                continue;
            }
            int source = -1;
            for (int inputSlot = 0; inputSlot < INPUT_SLOTS; inputSlot++) {
                ItemStack available = inventory.getStackInSlot(inputSlot);
                if (remaining[inputSlot] > 0 && ItemStack.isSameItemSameComponents(expected, available)) {
                    source = inputSlot;
                    break;
                }
            }
            if (source < 0) {
                return null;
            }
            remaining[source]--;
            consumed[source]++;
            grid.add(inventory.getStackInSlot(source).copyWithCount(1));
        }
        CraftingInput input = CraftingInput.of(3, 3, grid);
        if (input.isEmpty()) {
            return null;
        }
        RecipeHolder<CraftingRecipe> holder = serverLevel.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, serverLevel, lastRecipe).orElse(null);
        if (holder == null) {
            return null;
        }
        if (!holder.value().matches(input, serverLevel)) {
            return null;
        }
        ItemStack result = holder.value().assemble(input, serverLevel.registryAccess());
        if (result.isEmpty() || !result.isItemEnabled(serverLevel.enabledFeatures())) {
            return null;
        }
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(result.copy());
        holder.value().getRemainingItems(input).stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .forEach(outputs::add);
        if (!outputsFit(outputs)) {
            return null;
        }
        return new CraftPlan(consumed, List.copyOf(outputs), holder);
    }

    private boolean outputsFit(List<ItemStack> outputs) {
        List<ItemStack> simulated = new ArrayList<>(OUTPUT_SLOTS);
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            simulated.add(inventory.getStackInSlot(OUTPUT_START + slot).copy());
        }
        for (ItemStack output : outputs) {
            ItemStack remaining = output.copy();
            for (int slot = 0; slot < simulated.size() && !remaining.isEmpty(); slot++) {
                ItemStack existing = simulated.get(slot);
                if (existing.isEmpty()) {
                    int inserted = Math.min(remaining.getCount(), outputLimit(slot, remaining));
                    simulated.set(slot, remaining.copyWithCount(inserted));
                    remaining.shrink(inserted);
                } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int inserted = Math.min(remaining.getCount(), outputLimit(slot, existing) - existing.getCount());
                    existing.grow(inserted);
                    remaining.shrink(inserted);
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private ItemStack insertIntoOutputs(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < OUTPUT_SLOTS && !remaining.isEmpty(); slot++) {
            int actual = OUTPUT_START + slot;
            ItemStack existing = inventory.getStackInSlot(actual);
            if (existing.isEmpty()) {
                int inserted = Math.min(remaining.getCount(), outputLimit(slot, remaining));
                inventory.setStackInSlot(actual, remaining.copyWithCount(inserted));
                remaining.shrink(inserted);
            } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int inserted = Math.min(remaining.getCount(), outputLimit(slot, existing) - existing.getCount());
                if (inserted > 0) {
                    ItemStack merged = existing.copy();
                    merged.grow(inserted);
                    inventory.setStackInSlot(actual, merged);
                    remaining.shrink(inserted);
                }
            }
        }
        return remaining;
    }

    private int outputLimit(int outputSlot, ItemStack stack) {
        return Math.min(inventory.getSlotLimit(OUTPUT_START + outputSlot), stack.getMaxStackSize());
    }

    private void refreshPreview(ServerLevel serverLevel) {
        previewDirty = false;
        List<ItemStack> grid = new ArrayList<>(PATTERN_SLOTS);
        for (int slot = 0; slot < PATTERN_SLOTS; slot++) {
            grid.add(pattern.getStackInSlot(slot).copyWithCount(1));
        }
        CraftingInput input = CraftingInput.of(3, 3, grid);
        ItemStack result = input.isEmpty() ? ItemStack.EMPTY : serverLevel.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, serverLevel)
                .map(holder -> holder.value().assemble(input, serverLevel.registryAccess()))
                .filter(stack -> stack.isItemEnabled(serverLevel.enabledFeatures()))
                .orElse(ItemStack.EMPTY);
        preview.setStackInSlot(0, result);
    }

    public void setPatternStack(int slot, ItemStack stack) {
        pattern.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public AutoCrafterSpecification specification() {
        return ((AutoCrafterBlock)getBlockState().getBlock()).specification();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ItemStackHandler pattern() {
        return pattern;
    }

    public ItemStackHandler preview() {
        return preview;
    }

    public ClayEnergyStorage energyStorage() {
        return energy;
    }

    public ClayEnergy receiveClayEnergy(ClayEnergy amount) {
        ClayEnergy accepted = energy.add(amount);
        if (accepted.internalValue() > 0L) {
            setChanged();
        }
        return accepted;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return AutoCrafterBlock.PIPE;
    }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() {
        return AutoCrafterBlock.FACING;
    }
    @Override public void ioConfigurationChanged() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            if (!level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        ioNeighborsChanged();
    }

    public Container dropInventory() {
        SimpleContainer drops = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            drops.setItem(slot, inventory.getStackInSlot(slot));
        }
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AutoCrafterMenu(containerId, inventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("pattern", pattern.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putInt("craft_tick", craftTick);
        tag.putIntArray("insertion_routes", insertionRoutes);
        tag.putIntArray("extraction_routes", extractionRoutes);
        ListTag filters = new ListTag();
        for (ItemStack filter : sideFilters) filters.add(filter.saveOptional(registries));
        tag.put("side_filters", filters);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        pattern.deserializeNBT(registries, tag.getCompound("pattern"));
        energy.deserializeNBT(registries, tag.getCompound("energy"));
        craftTick = Math.max(0, tag.getInt("craft_tick"));
        if (tag.contains("insertion_routes") || tag.contains("extraction_routes")) {
            sideConfiguration.replaceRoutes(tag.getIntArray("insertion_routes"), tag.getIntArray("extraction_routes"));
        }
        ListTag filters = tag.getList("side_filters", Tag.TAG_COMPOUND);
        for (int index = 0; index < Math.min(filters.size(), sideFilters.length); index++) {
            sideFilters[index] = ItemStack.parseOptional(registries, filters.getCompound(index));
        }
        previewDirty = true;
    }

    private record CraftPlan(int[] consumed, List<ItemStack> outputs, RecipeHolder<CraftingRecipe> recipe) {
    }

    private final class AutoCrafterItemHandler implements IItemHandler {
        @Nullable private final Direction direction;

        private AutoCrafterItemHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getSlots() {
            return slots().length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(mappedSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int actual = mappedSlot(slot);
            if (direction != null && !contains(insertionSlots(insertionRoute(direction)), actual)
                    || actual >= OUTPUT_START && actual < FUEL_SLOT) {
                return stack;
            }
            if (direction != null && !matchesFilter(direction, stack)) return stack;
            return inventory.insertItem(actual, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int actual = mappedSlot(slot);
            if (direction != null && !contains(extractionSlots(extractionRoute(direction)), actual)
                    || actual < OUTPUT_START || actual >= FUEL_SLOT) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(actual, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(mappedSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int actual = mappedSlot(slot);
            return (direction == null || contains(insertionSlots(insertionRoute(direction)), actual))
                    && (direction == null || matchesFilter(direction, stack))
                    && inventory.isItemValid(actual, stack);
        }

        private int mappedSlot(int slot) { return slots()[slot]; }

        private int[] slots() {
            if (direction == null) return java.util.stream.IntStream.range(0, FUEL_SLOT + 1).toArray();
            return java.util.stream.IntStream.concat(
                    java.util.Arrays.stream(insertionSlots(insertionRoute(direction))),
                    java.util.Arrays.stream(extractionSlots(extractionRoute(direction)))).distinct().toArray();
        }
    }

    private static int[] insertionSlots(int route) {
        if (route == 0) return java.util.stream.IntStream.range(0, INPUT_SLOTS).toArray();
        return route == 1 ? new int[] {FUEL_SLOT} : new int[0];
    }

    private static int[] extractionSlots(int route) {
        return route == 0
                ? java.util.stream.IntStream.range(OUTPUT_START, OUTPUT_START + OUTPUT_SLOTS).toArray()
                : new int[0];
    }

    private static boolean contains(int[] slots, int slot) {
        for (int candidate : slots) if (candidate == slot) return true;
        return false;
    }

    private final class AutoCrafterContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> specification().tier();
                case 1 -> specification().intervalTicks();
                case 2 -> specification().craftsPerCycle();
                case 3, 4, 5, 6 -> (int)(energy.stored().internalValue() >>> ((index - 3) * 16)) & 0xFFFF;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 7;
        }
    }
}
