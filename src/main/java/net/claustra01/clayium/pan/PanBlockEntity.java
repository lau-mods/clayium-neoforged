package net.claustra01.clayium.pan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.recipe.ClayiumRecipeCache;
import net.claustra01.clayium.recipe.ClayiumRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;

public final class PanBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    public static final int CANDIDATES_PER_PAGE = 6;
    public static final int SAMPLE_SLOT = 0;
    public static final int ANTIMATTER_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FUEL_SLOT = 3;
    public static final int ADAPTER_PAGE_SIZE = 9;
    public static final int MAX_ADAPTER_PAGES = 8;

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SAMPLE_SLOT -> PanRecipeClosure.isPlain(stack);
                case ANTIMATTER_SLOT -> stack.is(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get());
                case OUTPUT_SLOT -> false;
                case FUEL_SLOT -> ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                default -> false;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SAMPLE_SLOT ? 1 : super.getSlotLimit(slot);
        }
    };
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private final int[] insertionRoutes = {-1, -1, -1, -1, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final ItemStack[] sideFilters = new ItemStack[6];
    private final SideConfiguration sideConfiguration;
    private boolean loadingAdapterInventory;
    private boolean adapterResultsDirty = true;
    private final ItemStackHandler adapterPatterns = new ItemStackHandler(ADAPTER_PAGE_SIZE * MAX_ADAPTER_PAGES) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!loadingAdapterInventory) adapterPatternChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < adapterPages() * ADAPTER_PAGE_SIZE && PanRecipeClosure.isPlain(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private final ItemStackHandler adapterResults = new ItemStackHandler(ADAPTER_PAGE_SIZE * MAX_ADAPTER_PAGES) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };
    private final ItemStackHandler adapterSubItems = new ItemStackHandler(ADAPTER_PAGE_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            if (loadingAdapterInventory) return;
            adapterResultsDirty = true;
            setChanged();
            if (level != null) PanNetwork.invalidate(level);
        }
    };
    private PanNetwork.Scan scan;
    private PanRecipeClosure.Calculation closureCalculation;
    private PanRecipeClosure.Result closure = new PanRecipeClosure.Result(Map.of(), Map.of());
    private BlockPos connectedCore;
    private long progress;
    private long requiredProgress;
    private long requiredEnergy;
    private ResourceLocation activeSample;
    private long activeConsumption;
    private long knownNetworkVersion = -1L;
    private long knownRecipeVersion = -1L;
    private Set<BlockPos> knownAdapters = Set.of();
    private List<PanRecipeClosure.Edge> knownAdapterEdges = List.of();
    private boolean closureReady;
    private long lastScanTick = Long.MIN_VALUE;
    private int nodeCount;
    private int adapterCount;
    private final Set<ResourceLocation> prohibited = new HashSet<>();
    private int candidateSort;
    private boolean candidateReverse;
    private int candidatePage;
    private ResourceLocation selectedCandidate;
    private List<PanScreenPayloads.Candidate> sortedCandidates;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int)(progress & 0xFFFFL);
                case 1 -> (int)((progress >>> 16) & 0xFFFFL);
                case 2 -> (int)(requiredProgress & 0xFFFFL);
                case 3 -> (int)((requiredProgress >>> 16) & 0xFFFFL);
                case 4 -> (int)(requiredEnergy & 0xFFFFL);
                case 5 -> (int)((requiredEnergy >>> 16) & 0xFFFFL);
                case 6 -> (int)((requiredEnergy >>> 32) & 0xFFFFL);
                case 7 -> (int)((requiredEnergy >>> 48) & 0xFFFFL);
                case 8 -> nodeCount;
                case 9 -> adapterCount;
                case 10 -> tier();
                case 11 -> connectedCore() != null && !connectedCore().isProhibited(inventory.getStackInSlot(SAMPLE_SLOT))
                        && connectedCore().closure.consumption(inventory.getStackInSlot(SAMPLE_SLOT)) > 0L ? 1 : 0;
                case 12, 13, 14, 15 -> (int)((energy.stored().internalValue() >>> ((index - 12) * 16)) & 0xFFFFL);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 16;
        }
    };

    public PanBlockEntity(BlockPos pos, BlockState state) {
        super(PanRegistration.BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(sideFilters, ItemStack.EMPTY);
        if (((PanBlock)state.getBlock()).kind() == PanBlock.Kind.DUPLICATOR) {
            insertionRoutes[1] = 0;
            insertionRoutes[3] = 1;
            extractionRoutes[0] = 0;
        }
        sideConfiguration = new SideConfiguration(insertionRoutes, extractionRoutes, sideFilters,
                () -> kind() == PanBlock.Kind.DUPLICATOR ? 2 : 0,
                () -> kind() == PanBlock.Kind.DUPLICATOR ? 1 : 0, ItemFilter::accepts);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PanBlockEntity pan) {
        pan.tickServer();
    }

    private void tickServer() {
        if (level == null) {
            return;
        }
        long version = PanNetwork.changeVersion(level);
        if (scan == null && (knownNetworkVersion != version
                || kind() == PanBlock.Kind.CORE && knownRecipeVersion != PanNetwork.recipeVersion()
                || level.getGameTime() - lastScanTick >= PanNetwork.REFRESH_INTERVAL)) {
            scan = PanNetwork.begin(level, worldPosition);
            lastScanTick = level.getGameTime();
        }
        if (scan != null && scan.advance(PanNetwork.NODES_PER_TICK)) {
            finishScan(scan);
            scan = null;
            knownNetworkVersion = PanNetwork.changeVersion(level);
        }
        if (kind() == PanBlock.Kind.CORE && closureCalculation != null
                && closureCalculation.advance(PanNetwork.NODES_PER_TICK)) {
            closure = closureCalculation.result();
            sortedCandidates = null;
            clampCandidatePage();
            closureCalculation = null;
            closureReady = true;
            setChanged();
        }
        if (kind() == PanBlock.Kind.DUPLICATOR) {
            tickDuplicator();
        } else if (kind() == PanBlock.Kind.ADAPTER) {
            if (knownRecipeVersion != PanNetwork.recipeVersion()) adapterResultsDirty = true;
            if (adapterResultsDirty) refreshAdapterResults();
        }
    }

    private void finishScan(PanNetwork.Scan finished) {
        nodeCount = finished.nodeCount();
        adapterCount = finished.adapters().size();
        if (kind() == PanBlock.Kind.CORE) {
            beginClosure(finished);
        } else if (kind() == PanBlock.Kind.DUPLICATOR) {
            connectedCore = finished.cores().stream().findFirst().orElse(null);
        }
        setChanged();
    }

    private void beginClosure(PanNetwork.Scan finished) {
        List<PanRecipeClosure.Edge> edges = new ArrayList<>();
        for (BlockPos adapter : finished.adapters()) {
            if (level.getBlockEntity(adapter) instanceof PanBlockEntity pan
                    && pan.kind() == PanBlock.Kind.ADAPTER) {
                edges.addAll(pan.adapterConversionEdges());
            }
        }
        Set<BlockPos> adapters = Set.copyOf(finished.adapters());
        List<PanRecipeClosure.Edge> selectedEdges = List.copyOf(edges);
        long recipeVersion = PanNetwork.recipeVersion();
        if (adapters.equals(knownAdapters) && selectedEdges.equals(knownAdapterEdges)
                && recipeVersion == knownRecipeVersion) {
            return;
        }
        knownAdapters = adapters;
        knownAdapterEdges = selectedEdges;
        knownRecipeVersion = recipeVersion;
        closureCalculation = PanRecipeClosure.beginSelected(selectedEdges);
    }

    private void tickDuplicator() {
        ItemStack sample = inventory.getStackInSlot(SAMPLE_SLOT);
        PanBlockEntity core = connectedCore();
        long consumption = core == null || !core.closureReady || core.isProhibited(sample)
                ? 0L : core.closure.consumption(sample);
        ResourceLocation sampleId = sample.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(sample.getItem());
        if (!java.util.Objects.equals(activeSample, sampleId)
                || core != null && core.closureReady && activeConsumption != consumption) {
            progress = 0L;
            activeSample = sampleId;
            activeConsumption = consumption;
        }
        requiredEnergy = consumption;
        requiredProgress = processingTicks(tier(), consumption);
        if (consumption <= 0L || !canOutput(sample) || inventory.getStackInSlot(ANTIMATTER_SLOT).isEmpty()) {
            return;
        }
        ClayEnergy needed = ClayEnergy.ofInternal(consumption);
        if (!energy.stored().canAfford(needed)) {
            energy.produceClayEnergy(inventory.getStackInSlot(FUEL_SLOT));
        }
        if (!energy.stored().canAfford(needed)) {
            return;
        }
        progress++;
        if (progress < requiredProgress) {
            setChanged();
            return;
        }
        if (energy.consume(needed)) {
            inventory.extractItem(ANTIMATTER_SLOT, 1, false);
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, sample.copyWithCount(1));
            } else {
                output.grow(1);
            }
        }
        progress = 0L;
        setChanged();
    }

    private boolean canOutput(ItemStack sample) {
        if (!PanRecipeClosure.isPlain(sample)) {
            return false;
        }
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, sample)
                && output.getCount() < output.getMaxStackSize();
    }

    @Nullable
    private PanBlockEntity connectedCore() {
        if (level == null || connectedCore == null || !level.hasChunkAt(connectedCore)) {
            return null;
        }
        return level.getBlockEntity(connectedCore) instanceof PanBlockEntity pan
                && pan.kind() == PanBlock.Kind.CORE ? pan : null;
    }

    public static long processingTicks(int tier, long consumption) {
        long perTick = Math.max(1L, (long)(100_000L * TierManager.general(tier).energyMultiplier()));
        return Math.max(1L, consumption / perTick + (consumption % perTick == 0L ? 0L : 1L));
    }

    public PanBlock.Kind kind() {
        return ((PanBlock)getBlockState().getBlock()).kind();
    }

    public int tier() {
        return ((PanBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return new PanItemHandler(direction);
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return PanBlock.PIPE;
    }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() {
        return PanBlock.FACING;
    }
    @Override public boolean togglePipe() {
        return kind() == PanBlock.Kind.DUPLICATOR && ConfigurableIoDevice.super.togglePipe();
    }
    @Override public boolean rotate(Direction side) {
        return kind() == PanBlock.Kind.DUPLICATOR && ConfigurableIoDevice.super.rotate(side);
    }
    @Override public void ioConfigurationChanged() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            if (!level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        ioNeighborsChanged();
    }

    public int adapterPages() {
        return switch (tier()) {
            case 11 -> 2;
            case 12 -> 4;
            case 13 -> 8;
            default -> 1;
        };
    }

    public ItemStackHandler adapterPatterns() {
        return adapterPatterns;
    }

    public ItemStackHandler adapterResults() {
        return adapterResults;
    }

    public ItemStackHandler adapterSubItems() {
        return adapterSubItems;
    }

    public ItemStack adapterPattern(int page, int slot) {
        if (!validAdapterCell(page, slot)) return ItemStack.EMPTY;
        return adapterPatterns.getStackInSlot(page * ADAPTER_PAGE_SIZE + slot).copy();
    }

    public ItemStack adapterResult(int page, int slot) {
        if (!validAdapterCell(page, slot)) return ItemStack.EMPTY;
        return adapterResults.getStackInSlot(page * ADAPTER_PAGE_SIZE + slot).copy();
    }

    public boolean setAdapterPattern(int page, int slot, ItemStack sample) {
        if (kind() != PanBlock.Kind.ADAPTER || !validAdapterCell(page, slot)
                || !sample.isEmpty() && !PanRecipeClosure.isPlain(sample)) {
            return false;
        }
        adapterPatterns.setStackInSlot(page * ADAPTER_PAGE_SIZE + slot,
                sample.isEmpty() ? ItemStack.EMPTY : sample.copyWithCount(1));
        return true;
    }

    private boolean validAdapterCell(int page, int slot) {
        return page >= 0 && page < adapterPages() && slot >= 0 && slot < ADAPTER_PAGE_SIZE;
    }

    private void adapterPatternChanged() {
        adapterResultsDirty = true;
        setChanged();
        if (level != null) PanNetwork.invalidate(level);
    }

    public void refreshAdapterResults() {
        if (kind() != PanBlock.Kind.ADAPTER || level == null || level.isClientSide) return;
        List<AdapterConversion> conversions = adapterConversions();
        loadingAdapterInventory = true;
        try {
            for (int page = 0; page < MAX_ADAPTER_PAGES; page++) {
                AdapterConversion conversion = page < conversions.size() ? conversions.get(page) : null;
                List<ItemStack> outputs = conversion == null ? List.of() : conversion.outputs();
                for (int slot = 0; slot < ADAPTER_PAGE_SIZE; slot++) {
                    adapterResults.setStackInSlot(page * ADAPTER_PAGE_SIZE + slot,
                            slot < outputs.size() ? outputs.get(slot).copy() : ItemStack.EMPTY);
                }
            }
        } finally {
            loadingAdapterInventory = false;
        }
        adapterResultsDirty = false;
        knownRecipeVersion = PanNetwork.recipeVersion();
        setChanged();
    }

    public void adapterNeighborChanged() {
        if (kind() != PanBlock.Kind.ADAPTER) return;
        adapterResultsDirty = true;
        if (level != null) PanNetwork.invalidate(level);
    }

    public List<PanRecipeClosure.Edge> adapterConversionEdges() {
        if (kind() != PanBlock.Kind.ADAPTER || level == null) return List.of();
        List<PanRecipeClosure.Edge> edges = new ArrayList<>();
        for (AdapterConversion conversion : adapterConversions()) {
            if (conversion != null) edges.addAll(conversion.edges());
        }
        return List.copyOf(edges);
    }

    private List<AdapterConversion> adapterConversions() {
        List<AdapterConversion> conversions = new ArrayList<>();
        for (int page = 0; page < adapterPages(); page++) conversions.add(null);
        net.minecraft.core.Direction connected = null;
        search:
        for (int page = 0; page < adapterPages(); page++) {
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                AdapterConversion conversion = adapterConversion(page, direction);
                if (conversion != null) {
                    connected = direction;
                    break search;
                }
            }
        }
        if (connected == null) return conversions;
        for (int page = 0; page < adapterPages(); page++) {
            conversions.set(page, adapterConversion(page, connected));
        }
        return conversions;
    }

    @Nullable
    private AdapterConversion adapterConversion(int page, net.minecraft.core.Direction direction) {
        List<ItemStack> pattern = new ArrayList<>(ADAPTER_PAGE_SIZE);
        boolean any = false;
        for (int slot = 0; slot < ADAPTER_PAGE_SIZE; slot++) {
            ItemStack stack = adapterPattern(page, slot);
            pattern.add(stack);
            any |= !stack.isEmpty();
        }
        if (!any) return null;
        BlockPos target = worldPosition.relative(direction);
        BlockState state = level.getBlockState(target);
        Recipe<?> recipe = null;
        List<ItemStack> outputs = List.of();
        if (state.getBlock() instanceof CraftingTableBlock) {
            CraftingInput input = CraftingInput.of(3, 3, pattern);
            var match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            if (match.isPresent()) {
                recipe = match.get().value();
                ItemStack result = match.get().value().assemble(input, level.registryAccess());
                if (!result.isEmpty()) outputs = List.of(result);
            }
        } else {
            List<ItemStack> nonEmpty = pattern.stream().filter(stack -> !stack.isEmpty()).toList();
            if (nonEmpty.size() == 1) {
                SingleRecipeInput input = new SingleRecipeInput(nonEmpty.getFirst());
                if (state.getBlock() instanceof BlastFurnaceBlock) {
                    var match = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, input, level);
                    if (match.isPresent()) {
                        recipe = match.get().value();
                        ItemStack result = match.get().value().assemble(input, level.registryAccess());
                        if (!result.isEmpty()) outputs = List.of(result);
                    }
                } else if (state.getBlock() instanceof SmokerBlock) {
                    var match = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, input, level);
                    if (match.isPresent()) {
                        recipe = match.get().value();
                        ItemStack result = match.get().value().assemble(input, level.registryAccess());
                        if (!result.isEmpty()) outputs = List.of(result);
                    }
                } else if (state.getBlock() instanceof FurnaceBlock) {
                    var match = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
                    if (match.isPresent()) {
                        recipe = match.get().value();
                        ItemStack result = match.get().value().assemble(input, level.registryAccess());
                        if (!result.isEmpty()) outputs = List.of(result);
                    }
                }
            }
        }
        if (recipe == null && state.getBlock() instanceof ClayMachineBlock machine) {
            ClayiumRecipeInput input = new ClayiumRecipeInput(
                    pattern.stream().filter(stack -> !stack.isEmpty()).toList(), List.<FluidStack>of(),
                    machine.tier(), Long.MAX_VALUE, target);
            var match = ClayiumRecipeCache.find(level.getRecipeManager(),
                    net.claustra01.clayium.registry.RecipeRegistration.type(machine.series().recipeMethod()),
                    input, level);
            if (match.isPresent()) {
                recipe = match.get().value();
                outputs = match.get().value().itemOutputs().stream().map(ItemStack::copy).toList();
            }
        }
        if (recipe == null || outputs.isEmpty()) return null;
        List<ItemStack> limited = List.copyOf(outputs.subList(0, Math.min(ADAPTER_PAGE_SIZE, outputs.size())));
        return new AdapterConversion(limited,
                PanRecipeClosure.edgesForSelectedRecipe(recipe, level.registryAccess()));
    }

    private record AdapterConversion(List<ItemStack> outputs, List<PanRecipeClosure.Edge> edges) {
        private AdapterConversion {
            outputs = outputs.stream().map(ItemStack::copy).toList();
            edges = List.copyOf(edges);
        }
    }

    public ClayEnergyStorage energy() {
        return energy;
    }

    public PanRecipeClosure.Result closure() {
        return closure;
    }

    public List<PanScreenPayloads.Candidate> visibleCandidates() {
        List<PanScreenPayloads.Candidate> candidates = sortedCandidates();
        int start = Math.min(candidates.size(), candidatePage * CANDIDATES_PER_PAGE);
        return List.copyOf(candidates.subList(start,
                Math.min(candidates.size(), start + CANDIDATES_PER_PAGE)));
    }

    public List<PanScreenPayloads.Candidate> allCandidates() {
        return sortedCandidates();
    }

    private List<PanScreenPayloads.Candidate> sortedCandidates() {
        if (sortedCandidates != null) {
            return sortedCandidates;
        }
        List<PanScreenPayloads.Candidate> candidates = new ArrayList<>();
        closure.costs().forEach((item, cost) -> candidates.add(new PanScreenPayloads.Candidate(
                item, cost, closure.consumption().getOrDefault(item, cost), prohibited.contains(item))));
        Comparator<PanScreenPayloads.Candidate> comparator = switch (candidateSort) {
            case 1 -> Comparator.comparingLong(PanScreenPayloads.Candidate::cost)
                    .thenComparing(candidate -> candidate.item().toString());
            case 2 -> Comparator.comparingLong(PanScreenPayloads.Candidate::consumption)
                    .thenComparing(candidate -> candidate.item().toString());
            default -> Comparator.comparing(candidate -> candidate.item().toString());
        };
        candidates.sort(candidateReverse ? comparator.reversed() : comparator);
        sortedCandidates = List.copyOf(candidates);
        return sortedCandidates;
    }

    private void clampCandidatePage() {
        int clamped = Math.clamp(candidatePage, 0, candidatePages() - 1);
        if (candidatePage != clamped) {
            candidatePage = clamped;
            setChanged();
        }
    }

    public int candidatePages() {
        return Math.max(1, (closure.costs().size() + CANDIDATES_PER_PAGE - 1) / CANDIDATES_PER_PAGE);
    }

    public int candidateSort() {
        return candidateSort;
    }

    public boolean candidateReverse() {
        return candidateReverse;
    }

    public int candidatePage() {
        return candidatePage;
    }

    @Nullable
    public ResourceLocation selectedCandidate() {
        return selectedCandidate;
    }

    public boolean isProhibited(ItemStack stack) {
        return !stack.isEmpty() && prohibited.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public boolean isProhibited(ResourceLocation item) {
        return prohibited.contains(item);
    }

    public boolean toggleProhibited(ResourceLocation item) {
        if (kind() != PanBlock.Kind.CORE || !closure.costs().containsKey(item)) {
            return false;
        }
        if (!prohibited.remove(item)) {
            prohibited.add(item);
        }
        sortedCandidates = null;
        setChanged();
        return true;
    }

    public void applyScreenControl(int action, int value) {
        if (kind() != PanBlock.Kind.CORE) {
            return;
        }
        switch (action) {
            case PanScreenPayloads.SORT -> {
                candidateSort = Math.clamp(value, 0, 2);
                sortedCandidates = null;
            }
            case PanScreenPayloads.REVERSE -> {
                candidateReverse = !candidateReverse;
                sortedCandidates = null;
            }
            case PanScreenPayloads.PAGE -> candidatePage = Math.clamp(value, 0, candidatePages() - 1);
            case PanScreenPayloads.SELECT -> {
                List<PanScreenPayloads.Candidate> page = visibleCandidates();
                if (value >= 0 && value < page.size()) {
                    selectedCandidate = page.get(value).item();
                }
            }
            case PanScreenPayloads.TOGGLE_PROHIBITED -> {
                if (selectedCandidate != null && closure.costs().containsKey(selectedCandidate)) {
                    if (!prohibited.remove(selectedCandidate)) {
                        prohibited.add(selectedCandidate);
                    }
                    sortedCandidates = null;
                }
            }
            default -> {
                return;
            }
        }
        clampCandidatePage();
        setChanged();
    }

    public int nodeCount() {
        return nodeCount;
    }

    public long lastScanTick() {
        return lastScanTick;
    }

    public int adapterCount() {
        return adapterCount;
    }

    public void receiveEnergy(long internal) {
        energy.add(ClayEnergy.ofInternal(Math.max(0L, internal)));
    }

    public Container dropInventory() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            container.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return container;
    }

    public Container dropAdapterSubItems() {
        SimpleContainer container = new SimpleContainer(adapterSubItems.getSlots());
        for (int slot = 0; slot < adapterSubItems.getSlots(); slot++) {
            container.setItem(slot, adapterSubItems.getStackInSlot(slot).copy());
            adapterSubItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return container;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PanMenu(containerId, playerInventory, this, data);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) PanNetwork.invalidate(level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        if (kind() == PanBlock.Kind.ADAPTER) {
            tag.put("adapter_patterns", adapterPatterns.serializeNBT(registries));
            tag.put("adapter_sub_items", adapterSubItems.serializeNBT(registries));
        }
        tag.put("energy", energy.serializeNBT(registries));
        tag.putLong("progress", progress);
        tag.putLong("active_consumption", activeConsumption);
        tag.putInt("candidate_sort", candidateSort);
        tag.putBoolean("candidate_reverse", candidateReverse);
        tag.putInt("candidate_page", candidatePage);
        if (selectedCandidate != null) {
            tag.putString("selected_candidate", selectedCandidate.toString());
        }
        ListTag prohibitedItems = new ListTag();
        prohibited.stream().map(ResourceLocation::toString).sorted()
                .forEach(id -> prohibitedItems.add(StringTag.valueOf(id)));
        tag.put("prohibited", prohibitedItems);
        saveIoState(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        loadingAdapterInventory = true;
        try {
            if (kind() == PanBlock.Kind.ADAPTER && tag.contains("adapter_patterns")) {
                adapterPatterns.deserializeNBT(registries, tag.getCompound("adapter_patterns"));
            }
            if (kind() == PanBlock.Kind.ADAPTER && tag.contains("adapter_sub_items")) {
                adapterSubItems.deserializeNBT(registries, tag.getCompound("adapter_sub_items"));
            }
        } finally {
            loadingAdapterInventory = false;
        }
        adapterResultsDirty = true;
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.getCompound("energy"));
        }
        progress = Math.max(0L, tag.getLong("progress"));
        activeSample = inventory.getStackInSlot(SAMPLE_SLOT).isEmpty() ? null
                : BuiltInRegistries.ITEM.getKey(inventory.getStackInSlot(SAMPLE_SLOT).getItem());
        activeConsumption = Math.max(0L, tag.getLong("active_consumption"));
        candidateSort = Math.clamp(tag.getInt("candidate_sort"), 0, 2);
        candidateReverse = tag.getBoolean("candidate_reverse");
        candidatePage = Math.max(0, tag.getInt("candidate_page"));
        selectedCandidate = ResourceLocation.tryParse(tag.getString("selected_candidate"));
        prohibited.clear();
        ListTag prohibitedItems = tag.getList("prohibited", Tag.TAG_STRING);
        for (int index = 0; index < prohibitedItems.size(); index++) {
            ResourceLocation item = ResourceLocation.tryParse(prohibitedItems.getString(index));
            if (item != null) {
                prohibited.add(item);
            }
        }
        knownNetworkVersion = -1L;
        knownRecipeVersion = -1L;
        knownAdapters = Set.of();
        scan = null;
        closureCalculation = null;
        connectedCore = null;
        nodeCount = 0;
        adapterCount = 0;
        closureReady = false;
        closure = new PanRecipeClosure.Result(Map.of(), Map.of());
        sortedCandidates = null;
        loadIoState(tag, registries);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class PanItemHandler implements IItemHandler {
        @Nullable private final Direction direction;

        private PanItemHandler(@Nullable Direction direction) { this.direction = direction; }
        @Override public int getSlots() { return kind() == PanBlock.Kind.DUPLICATOR ? slots().length : 0; }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slots()[slot]); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int actual = slots()[slot];
            return actual == OUTPUT_SLOT || direction != null && !matchesFilter(direction, stack)
                    ? stack : inventory.insertItem(actual, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int actual = slots()[slot];
            return actual == OUTPUT_SLOT ? inventory.extractItem(actual, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slots()[slot]); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            int actual = slots()[slot];
            return actual != OUTPUT_SLOT && (direction == null || matchesFilter(direction, stack))
                    && inventory.isItemValid(actual, stack);
        }
        private int[] slots() {
            if (direction == null) return new int[] {SAMPLE_SLOT, ANTIMATTER_SLOT, OUTPUT_SLOT, FUEL_SLOT};
            int route = insertionRoute(direction);
            int[] insertion = route == 0 ? new int[] {SAMPLE_SLOT, ANTIMATTER_SLOT}
                    : route == 1 ? new int[] {FUEL_SLOT} : new int[0];
            int[] extraction = extractionRoute(direction) == 0 ? new int[] {OUTPUT_SLOT} : new int[0];
            return java.util.stream.IntStream.concat(
                    java.util.Arrays.stream(insertion), java.util.Arrays.stream(extraction)).distinct().toArray();
        }
    }

}
