package net.claustra01.clayium.machine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.augment.AugmentScanner;
import net.claustra01.clayium.machine.ca.CaMachineRules;
import net.claustra01.clayium.machine.ca.CaSeries;
import net.claustra01.clayium.machine.environment.EnvironmentSeries;
import net.claustra01.clayium.machine.series.MachineSeries;
import net.claustra01.clayium.machine.series.SmeltingSeries;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.machine.iface.RedstoneControlReceiver;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeCache;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ProgressionRateRules;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.claustra01.clayium.recipe.ClayiumRecipeInput;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class ClayMachineBlockEntity extends BlockEntity
        implements MenuProvider, RedstoneControlReceiver, ConfigurableIoDevice {

    private static final List<WeakReference<ClayMachineBlockEntity>> LOADED_MACHINES = new ArrayList<>();

    static {
        NeoForge.EVENT_BUS.addListener(ClayiumRecipesReloadedEvent.class, event -> {
            LOADED_MACHINES.removeIf(reference -> reference.get() == null);
            for (WeakReference<ClayMachineBlockEntity> reference : LOADED_MACHINES) {
                ClayMachineBlockEntity machine = reference.get();
                if (machine != null && machine.level instanceof ServerLevel serverLevel
                        && serverLevel.getRecipeManager() == event.recipeManager()
                        && machine.activeRecipe != null
                        && !machine.activeRecipeIsCurrent(event.recipeManager())) {
                    machine.abortProcess();
                }
            }
        });
    }

    private final int inputSlots;
    private final int outputSlots;
    private final int outputSlot;
    private final int reservedSlot;
    private final int fuelSlot;
    private final ItemStackHandler inventory;
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private final int[] insertionRoutes = {-1, 0, -1, 1, -1, -1};
    private final int[] extractionRoutes = {0, -1, -1, -1, -1, -1};
    private final ItemStack[] sideFilters = new ItemStack[RelativeFace.COUNT];
    private final SideConfiguration sideConfiguration;
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final IItemHandler unsidedHandler;
    private final FluidTank fluidInput = tank();
    private final FluidTank reservedFluid = tank();
    private final FluidTank fluidOutput = tank();
    private final IFluidHandler fluidHandler = new MachineFluidHandler();
    private final ContainerData menuData = new MachineContainerData();
    private EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborItemCaches;

    private MachineState state = MachineState.IDLE;
    private ResourceLocation activeRecipe;
    private ClayiumMachineRecipe activeRecipeValue;
    private boolean activeVanillaSmeltingRecipe;
    private List<ItemStack> pendingOutputs = List.of();
    private FluidStack pendingFluidOutput = FluidStack.EMPTY;
    private long progress;
    private long requiredProgress;
    private long energyPerProgress;
    private double progressRemainder;
    private double progressPerTick = 1.0D;
    private double activeProgressMultiplier = 1.0D;
    private boolean validateRecipeOnLoad;
    private long lastRemoteRedstoneTick = Long.MIN_VALUE;
    private int autoInsertTick;
    private int autoExtractTick;
    private int nextInputDirection;
    private int nextOutputDirection;

    public ClayMachineBlockEntity(BlockPos pos, BlockState state) {
        this(MachineRegistration.MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    protected ClayMachineBlockEntity(
            net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
        MachineSeries series = ((ClayMachineBlock)state.getBlock()).series();
        inputSlots = series.inputSlots();
        outputSlots = series.outputSlots();
        outputSlot = inputSlots;
        reservedSlot = outputSlot + outputSlots;
        fuelSlot = reservedSlot + inputSlots;
        inventory = new ItemStackHandler(fuelSlot + 1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot < inputSlots
                        || slot == fuelSlot && ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == fuelSlot ? fuelLimit() : super.getSlotLimit(slot);
            }
        };
        java.util.Arrays.fill(sideFilters, ItemStack.EMPTY);
        if (series == EnvironmentSeries.SALT_EXTRACTOR) {
            java.util.Arrays.fill(insertionRoutes, -1);
            java.util.Arrays.fill(extractionRoutes, -1);
            insertionRoutes[3] = 0;
            extractionRoutes[2] = 0;
        }
        sideConfiguration = new SideConfiguration(
                insertionRoutes, extractionRoutes, sideFilters,
                this::insertionRouteCount, () -> 1, ItemFilter::accepts);
        unsidedHandler = new SidedItemHandler(null);
        LOADED_MACHINES.add(new WeakReference<>(this));
        for (Direction direction : Direction.values()) {
            sidedHandlers.put(direction, new SidedItemHandler(direction));
        }
    }

    private int insertionRouteCount() {
        return series() == EnvironmentSeries.SALT_EXTRACTOR ? 1 : 2;
    }

    private FluidTank tank() {
        return new FluidTank(16_000) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClayMachineBlockEntity machine) {
        machine.tickServer(level.random);
    }

    public void tickServer(RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        tickAutomaticItemTransfer(serverLevel);
        if (!canProcess()) {
            setState(MachineState.IDLE);
            return;
        }
        enforceFuelLimit();
        if (isRemoteRedstonePowered()) {
            setState(MachineState.IDLE);
            return;
        }
        if (activeRecipe == null && hasReservedInput()) {
            if (!recoverReservedInput()) {
                setState(MachineState.RECIPE_CHANGED);
                return;
            }
        }
        if (activeRecipe == null) {
            tryStart(serverLevel);
            return;
        }
        if (validateRecipeOnLoad) {
            validateRecipeOnLoad = false;
            if (!activeRecipeIsCurrent(serverLevel.getRecipeManager())) {
                abortProcess();
                return;
            }
            ClayiumMachineRecipe recipe = currentRecipe(serverLevel.getRecipeManager()).orElseThrow();
            if (!pendingFluidContains(recipe.fluidOutputs())) {
                abortProcess();
                return;
            }
            activeRecipeValue = recipe;
        }
        if (activeRecipeValue != null && !activeRecipeValue.environment().matches(serverLevel, worldPosition)) {
            setState(MachineState.IDLE);
            return;
        }
        if (!canAcceptOutputs(pendingOutputs)
                || !allRandomOutputsFit(pendingOutputs, activeRecipeValue.randomOutputs())
                || !canAcceptFluid(pendingFluidOutput)) {
            setState(MachineState.OUTPUT_FULL);
            return;
        }
        double nextRemainder = progressRemainder + progressPerTick * processingProgressMultiplier();
        long completedProgress = Math.min(requiredProgress - progress, Math.max(1L, (long)Math.floor(nextRemainder)));
        if (!consumeProcessingEnergy(completedProgress)) {
            setState(MachineState.WAITING_FOR_ENERGY);
            return;
        }
        setState(MachineState.RUNNING);
        progressRemainder = nextRemainder - completedProgress;
        progress = Math.min(requiredProgress, saturatedAdd(progress, completedProgress));
        if (progress >= requiredProgress) {
            finishProcess(random);
            tryStart(serverLevel);
        } else {
            setChanged();
        }
    }

    private void tickAutomaticItemTransfer(ServerLevel serverLevel) {
        TierManager.TransportParameters parameters = TierManager.machineTransport(tier());
        autoInsertTick++;
        autoExtractTick++;
        boolean insertDue = autoInsertTick >= parameters.insertIntervalTicks();
        boolean extractDue = autoExtractTick >= parameters.extractIntervalTicks();
        if (!insertDue && !extractDue) {
            return;
        }
        if (insertDue) {
            autoInsertTick = 0;
        }
        if (extractDue) {
            autoExtractTick = 0;
        }
        ensureNeighborItemCaches(serverLevel);
        if (extractDue) {
            pushOutputItems(parameters.maximumExtractItems());
        }
        if (insertDue) {
            pullInputItems(parameters.maximumInsertItems());
        }
    }

    private void ensureNeighborItemCaches(ServerLevel serverLevel) {
        if (neighborItemCaches != null) {
            return;
        }
        neighborItemCaches = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            neighborItemCaches.put(direction, BlockCapabilityCache.create(
                    Capabilities.ItemHandler.BLOCK,
                    serverLevel,
                    worldPosition.relative(direction),
                    direction.getOpposite(),
                    () -> !isRemoved(),
                    () -> {}));
        }
    }

    private void pushOutputItems(int maximum) {
        int remaining = maximum;
        int start = nextOutputDirection;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int index = (start + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (extractionRoute(direction) < 0) {
                continue;
            }
            IItemHandler target = neighborItemCaches.get(direction).getCapability();
            if (target == null) {
                continue;
            }
            int moved = BufferBlockEntity.moveItems(
                    sidedHandlers.get(direction), target, stack -> true, remaining, this::dropTransferRemainder);
            if (moved > 0) {
                remaining -= moved;
                nextOutputDirection = (index + 1) % Direction.values().length;
            }
        }
    }

    private void pullInputItems(int maximum) {
        int remaining = maximum;
        int start = nextInputDirection;
        for (int offset = 0; offset < Direction.values().length && remaining > 0; offset++) {
            int index = (start + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (insertionRoute(direction) < 0) {
                continue;
            }
            IItemHandler source = neighborItemCaches.get(direction).getCapability();
            if (source == null) {
                continue;
            }
            int moved = BufferBlockEntity.moveItems(
                    source, sidedHandlers.get(direction), stack -> true, remaining, this::dropTransferRemainder);
            if (moved > 0) {
                remaining -= moved;
                nextInputDirection = (index + 1) % Direction.values().length;
            }
        }
    }

    private void dropTransferRemainder(ItemStack stack) {
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D, stack);
    }

    private void tryStart(ServerLevel serverLevel) {
        List<ItemStack> inputs = inputStacks();
        if ((!series().canStartWithoutItemInput() && inputs.stream().allMatch(ItemStack::isEmpty))
                || hasReservedInput()) {
            setState(MachineState.IDLE);
            return;
        }
        MachineSeries series = series();
        ClayiumRecipeInput input = new ClayiumRecipeInput(
                inputs,
                fluidInput.getFluid().isEmpty() ? List.of() : List.of(fluidInput.getFluid().copy()),
                tier(),
                availableClayEnergy(),
                worldPosition);
        Optional<SelectedRecipe> match = findRecipe(serverLevel, series, input);
        if (match.isEmpty()) {
            setState(MachineState.IDLE);
            return;
        }
        ClayiumMachineRecipe recipe = match.get().recipe();
        List<ItemStack> fixedOutputs = ProgressionRateRules.adjustedRecipeOutputs(recipe, fixedOutputs(recipe));
        if (!canAcceptOutputs(fixedOutputs)
                || !canAcceptFluidOutputs(recipe.fluidOutputs())
                || !allRandomOutputsFit(fixedOutputs, recipe.randomOutputs())) {
            setState(MachineState.OUTPUT_FULL);
            return;
        }
        TierManager.ProcessingParameters parameters = processingParameters(match.get().id(), recipe);
        if (parameters == null) {
            setState(MachineState.IDLE);
            return;
        }
        if (!canPower(parameters.energyPerProgress().internalValue())) {
            setState(MachineState.WAITING_FOR_ENERGY);
            return;
        }
        FluidStack fluidResult = combinedFluidOutput(recipe.fluidOutputs());
        if (fixedOutputs.isEmpty() && recipe.randomOutputs().isEmpty() && fluidResult.isEmpty()) {
            setState(MachineState.IDLE);
            return;
        }
        if (!reserveInput(recipe)) {
            setState(MachineState.OUTPUT_FULL);
            return;
        }
        activeRecipe = match.get().id();
        activeRecipeValue = recipe;
        activeVanillaSmeltingRecipe = match.get().vanillaSmelting();

        pendingOutputs = copyItems(fixedOutputs);
        pendingFluidOutput = fluidResult.copy();
        progress = 0L;
        requiredProgress = parameters.requiredProgress();
        energyPerProgress = parameters.energyPerProgress().internalValue();
        progressRemainder = 0.0D;
        progressPerTick = parameters.progressPerTick();
        activeProgressMultiplier = series == CaSeries.INJECTOR
                ? CaMachineRules.injectorProgressMultiplier(tier(),
                        AugmentScanner.resonance(level, worldPosition)) : 1.0D;
        setState(MachineState.RUNNING);
        setChanged();
    }

    private Optional<SelectedRecipe> findRecipe(
            ServerLevel serverLevel,
            MachineSeries series,
            ClayiumRecipeInput input) {
        Optional<RecipeHolder<ClayiumMachineRecipe>> machineRecipe = ClayiumRecipeCache.find(
                serverLevel.getRecipeManager(), RecipeRegistration.type(series.recipeMethod()), input, serverLevel);
        if (machineRecipe.isPresent()) {
            RecipeHolder<ClayiumMachineRecipe> holder = machineRecipe.get();
            return Optional.of(new SelectedRecipe(
                    holder.id(), ProgressionRateRules.adjustedRecipe(holder.value()), false));
        }
        if (series != SmeltingSeries.SMELTER || input.items().size() != 1) {
            return Optional.empty();
        }
        SingleRecipeInput smeltingInput = new SingleRecipeInput(input.items().getFirst());
        return serverLevel.getRecipeManager().getRecipeFor(RecipeType.SMELTING, smeltingInput, serverLevel)
                .filter(holder -> !holder.value().assemble(smeltingInput, serverLevel.registryAccess()).isEmpty())
                .map(holder -> new SelectedRecipe(
                        holder.id(), vanillaSmeltingRecipe(holder.value(), smeltingInput, serverLevel), true));
    }

    private ClayiumMachineRecipe vanillaSmeltingRecipe(
            SmeltingRecipe smeltingRecipe,
            SingleRecipeInput input,
            ServerLevel serverLevel) {
        return new ClayiumMachineRecipe(
                ClayiumRecipeMethod.SMELTER,
                0,
                List.of(new SizedIngredient(Ingredient.of(input.item().getItem()), 1)),
                List.of(),
                List.of(smeltingRecipe.assemble(input, serverLevel.registryAccess())),
                List.of(),
                4L,
                200,
                ClayiumMachineRecipe.EnvironmentCondition.ANY,
                List.of());
    }

    protected List<ItemStack> fixedOutputs(ClayiumMachineRecipe recipe) {
        if (series() != CaSeries.CONDENSER || level == null) {
            return combineStacks(recipe.itemOutputs());
        }
        double resonance = AugmentScanner.resonance(level, worldPosition);
        return combineStacks(recipe.itemOutputs().stream().map(stack -> stack.copyWithCount(Math.min(
                stack.getMaxStackSize(), CaMachineRules.condenserOutput(stack.getCount(), resonance)))).toList());
    }

    private List<ItemStack> selectedOutputs(ClayiumMachineRecipe recipe, List<ItemStack> fixedOutputs,
                                            RandomSource random) {
        List<ItemStack> outputs = new ArrayList<>(copyItems(fixedOutputs));
        if (recipe.randomOutputs().isEmpty() || level == null) {
            return List.copyOf(outputs);
        }
        int totalWeight = recipe.randomOutputs().stream().mapToInt(ClayiumMachineRecipe.RandomOutput::weight).sum();
        int roll = random.nextInt(totalWeight);
        ClayiumMachineRecipe.RandomOutput selected = recipe.randomOutputs().getLast();
        for (ClayiumMachineRecipe.RandomOutput candidate : recipe.randomOutputs()) {
            if (roll < candidate.weight()) {
                selected = candidate;
                break;
            }
            roll -= candidate.weight();
        }
        if (selected.chance() >= 1.0F || random.nextFloat() <= selected.chance()) {
            outputs.add(selected.stack().copy());
        }
        return combineStacks(outputs);
    }

    private static List<ItemStack> combineStacks(List<ItemStack> stacks) {
        List<ItemStack> combined = new ArrayList<>();
        for (ItemStack result : stacks) {
            if (result.isEmpty()) {
                continue;
            }
            ItemStack matching = combined.stream()
                    .filter(stack -> ItemStack.isSameItemSameComponents(stack, result))
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
            if (matching.isEmpty()) {
                combined.add(result.copy());
            } else if (matching.getCount() + result.getCount() <= matching.getMaxStackSize()) {
                matching.grow(result.getCount());
            } else {
                return List.of();
            }
        }
        return List.copyOf(combined);
    }

    private boolean reserveInput(ClayiumMachineRecipe recipe) {
        if (recipe.itemInputs().size() > inputSlots) {
            return false;
        }
        int[] remaining = new int[inputSlots];
        for (int index = 0; index < inputSlots; index++) {
            remaining[index] = inventory.getStackInSlot(index).getCount();
        }
        for (SizedIngredient required : recipe.itemInputs()) {
            int missing = required.count();
            for (int index = 0; index < inputSlots && missing > 0; index++) {
                ItemStack input = inventory.getStackInSlot(index);
                if (remaining[index] > 0 && required.ingredient().test(input)) {
                    int used = Math.min(missing, remaining[index]);
                    missing -= used;
                    remaining[index] -= used;
                }
            }
            if (missing > 0) {
                return false;
            }
        }
        if (!canReserveFluids(recipe.fluidInputs())) {
            return false;
        }
        for (int index = 0; index < inputSlots; index++) {
            ItemStack input = inventory.getStackInSlot(index);
            int used = input.getCount() - remaining[index];
            if (used > 0) {
                inventory.setStackInSlot(reservedSlot + index, input.copyWithCount(used));
                inventory.extractItem(index, used, false);
            }
        }
        for (FluidStack required : recipe.fluidInputs()) {
            FluidStack drained = fluidInput.drain(required, IFluidHandler.FluidAction.EXECUTE);
            reservedFluid.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }
        return true;
    }

    private boolean canReserveFluids(List<FluidStack> requiredFluids) {
        if (requiredFluids.isEmpty()) {
            return true;
        }
        FluidStack available = fluidInput.getFluid();
        int requiredAmount = 0;
        for (FluidStack required : requiredFluids) {
            if (!FluidStack.isSameFluidSameComponents(available, required)) {
                return false;
            }
            requiredAmount += required.getAmount();
        }
        return available.getAmount() >= requiredAmount && reservedFluid.isEmpty();
    }

    private boolean consumeProcessingEnergy(long completedProgress) {
        if (energyPerProgress == 0L) {
            return true;
        }
        ItemStack fuel = tier() >= 4 ? inventory.getStackInSlot(fuelSlot) : ItemStack.EMPTY;
        if (energyPerProgress > Long.MAX_VALUE / completedProgress) {
            return false;
        }
        boolean consumed = energy.consume(ClayEnergy.ofInternal(energyPerProgress * completedProgress), fuel);
        if (consumed) {
            setChanged();
        }
        return consumed;
    }

    private boolean canPower(long required) {
        return required == 0L
                || energy.stored().internalValue() >= required
                || tier() >= 4 && ClayEnergyStorage.fuelValue(inventory.getStackInSlot(fuelSlot)).internalValue() > 0L;
    }

    private long availableClayEnergy() {
        long available = energy.stored().internalValue();
        if (tier() >= 4) {
            available = saturatedAdd(
                    available,
                    ClayEnergyStorage.fuelValue(inventory.getStackInSlot(fuelSlot)).internalValue());
        }
        return available;
    }

    private boolean allRandomOutputsFit(
            List<ItemStack> fixedOutputs,
            List<ClayiumMachineRecipe.RandomOutput> randomOutputs) {
        for (ClayiumMachineRecipe.RandomOutput randomOutput : randomOutputs) {
            List<ItemStack> possible = new ArrayList<>(fixedOutputs);
            possible.add(randomOutput.stack());
            if (!canAcceptOutputs(combineStacks(possible))) {
                return false;
            }
        }
        return true;
    }

    private boolean canAcceptOutputs(List<ItemStack> results) {
        if (results.size() > outputSlots) {
            return false;
        }
        List<ItemStack> simulated = new ArrayList<>(outputSlots);
        for (int slot = 0; slot < outputSlots; slot++) {
            simulated.add(inventory.getStackInSlot(outputSlot + slot).copy());
        }
        return placeOutputs(simulated, results);
    }

    private boolean placeOutputs(List<ItemStack> slots, List<ItemStack> results) {
        for (ItemStack result : results) {
            boolean stored = false;
            for (ItemStack existing : slots) {
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, result)
                        && existing.getCount() + result.getCount() <= existing.getMaxStackSize()) {
                    existing.grow(result.getCount());
                    stored = true;
                    break;
                }
            }
            if (!stored) {
                for (int slot = 0; slot < slots.size(); slot++) {
                    if (slots.get(slot).isEmpty()) {
                        slots.set(slot, result.copy());
                        stored = true;
                        break;
                    }
                }
            }
            if (!stored) {
                return false;
            }
        }
        return true;
    }

    private void storeOutputs(List<ItemStack> results) {
        List<ItemStack> placed = new ArrayList<>(outputSlots);
        for (int slot = 0; slot < outputSlots; slot++) {
            placed.add(inventory.getStackInSlot(outputSlot + slot).copy());
        }
        if (!placeOutputs(placed, results)) {
            throw new IllegalStateException("Output capacity changed during processing");
        }
        for (int slot = 0; slot < outputSlots; slot++) {
            inventory.setStackInSlot(outputSlot + slot, placed.get(slot));
        }
    }

    private boolean canAcceptFluidOutputs(List<FluidStack> results) {
        FluidStack combined = combinedFluidOutput(results);
        return results.isEmpty() || !combined.isEmpty() && canAcceptFluid(combined);
    }

    private FluidStack combinedFluidOutput(List<FluidStack> results) {
        FluidStack combined = FluidStack.EMPTY;
        for (FluidStack result : results) {
            if (combined.isEmpty()) {
                combined = result.copy();
            } else if (FluidStack.isSameFluidSameComponents(combined, result)) {
                combined.grow(result.getAmount());
            } else {
                return FluidStack.EMPTY;
            }
        }
        return combined;
    }

    private boolean canAcceptFluid(FluidStack result) {
        return result.isEmpty()
                || fluidOutput.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
    }

    private boolean pendingFluidContains(List<FluidStack> expected) {
        FluidStack combined = combinedFluidOutput(expected);
        return expected.isEmpty() ? pendingFluidOutput.isEmpty()
                : !combined.isEmpty() && FluidStack.isSameFluidSameComponents(combined, pendingFluidOutput)
                && pendingFluidOutput.getAmount() >= combined.getAmount();
    }

    private static List<ItemStack> copyItems(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    private void finishProcess(RandomSource random) {
        storeOutputs(selectedOutputs(activeRecipeValue, pendingOutputs, random));
        if (!pendingFluidOutput.isEmpty()) {
            fluidOutput.fill(pendingFluidOutput, IFluidHandler.FluidAction.EXECUTE);
        }
        clearReservedInputs();
        activeRecipe = null;
        activeRecipeValue = null;
        activeVanillaSmeltingRecipe = false;
        pendingOutputs = List.of();
        pendingFluidOutput = FluidStack.EMPTY;
        progress = 0L;
        requiredProgress = 0L;
        energyPerProgress = 0L;
        progressRemainder = 0.0D;
        progressPerTick = 1.0D;
        activeProgressMultiplier = 1.0D;

        setChanged();
    }

    private boolean activeRecipeIsCurrent(RecipeManager manager) {
        return currentRecipe(manager)
                .filter(recipe -> recipe.processingMethod() == series().recipeMethod())
                .filter(recipe -> recipe.requiredTier() <= tier())
                .filter(recipe -> activeRecipeValue == null || activeVanillaSmeltingRecipe
                        || activeRecipeValue.equals(recipe))
                .filter(this::matchesReservedInputs)
                .isPresent();
    }

    private Optional<ClayiumMachineRecipe> currentRecipe(RecipeManager manager) {
        if (!activeVanillaSmeltingRecipe) {
            return manager.byKey(activeRecipe)
                    .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                    .map(holder -> ProgressionRateRules.adjustedRecipe(
                            (ClayiumMachineRecipe)holder.value()));
        }
        ItemStack reserved = inventory.getStackInSlot(reservedSlot);
        if (!(level instanceof ServerLevel serverLevel) || reserved.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(reserved);
        return manager.byKey(activeRecipe)
                .filter(holder -> holder.value() instanceof SmeltingRecipe)
                .map(holder -> vanillaSmeltingRecipe((SmeltingRecipe)holder.value(), input, serverLevel));
    }

    private boolean matchesReservedInputs(ClayiumMachineRecipe recipe) {
        List<ItemStack> reserved = reservedStacks();
        int reservedCount = reserved.stream().mapToInt(ItemStack::getCount).sum();
        int requiredCount = recipe.itemInputs().stream().mapToInt(SizedIngredient::count).sum();
        if (reservedCount != requiredCount) {
            return false;
        }
        int[] remaining = reserved.stream().mapToInt(ItemStack::getCount).toArray();
        for (SizedIngredient required : recipe.itemInputs()) {
            int missing = required.count();
            for (int index = 0; index < reserved.size() && missing > 0; index++) {
                if (remaining[index] > 0 && required.ingredient().test(reserved.get(index))) {
                    int used = Math.min(missing, remaining[index]);
                    missing -= used;
                    remaining[index] -= used;
                }
            }
            if (missing > 0) {
                return false;
            }
        }
        if (recipe.fluidInputs().isEmpty()) {
            return reservedFluid.isEmpty();
        }
        int requiredAmount = recipe.fluidInputs().stream().mapToInt(FluidStack::getAmount).sum();
        return recipe.fluidInputs().stream()
                        .allMatch(required -> FluidStack.isSameFluidSameComponents(required, reservedFluid.getFluid()))
                && reservedFluid.getFluidAmount() == requiredAmount;
    }

    protected void abortProcess() {
        activeRecipe = null;
        activeRecipeValue = null;
        activeVanillaSmeltingRecipe = false;
        pendingOutputs = List.of();
        pendingFluidOutput = FluidStack.EMPTY;
        progress = 0L;
        requiredProgress = 0L;
        energyPerProgress = 0L;
        progressRemainder = 0.0D;
        progressPerTick = 1.0D;
        activeProgressMultiplier = 1.0D;

        if (recoverReservedInput()) {
            setState(MachineState.IDLE);
        } else {
            setState(MachineState.RECIPE_CHANGED);
        }
        setChanged();
    }

    private boolean recoverReservedInput() {
        if (!reservedFluid.isEmpty()
                && fluidInput.fill(reservedFluid.getFluid(), IFluidHandler.FluidAction.SIMULATE)
                != reservedFluid.getFluidAmount()) {
            return false;
        }
        for (int index = 0; index < inputSlots; index++) {
            ItemStack reserved = inventory.getStackInSlot(reservedSlot + index);
            ItemStack input = inventory.getStackInSlot(index);
            if (!reserved.isEmpty()
                    && (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, reserved)
                    || (input.isEmpty() ? reserved.getMaxStackSize() : input.getMaxStackSize() - input.getCount())
                    < reserved.getCount())) {
                return false;
            }
        }
        for (int index = 0; index < inputSlots; index++) {
            ItemStack reserved = inventory.getStackInSlot(reservedSlot + index);
            if (reserved.isEmpty()) {
                continue;
            }
            ItemStack input = inventory.getStackInSlot(index);
            if (input.isEmpty()) {
                inventory.setStackInSlot(index, reserved.copy());
            } else {
                input.grow(reserved.getCount());
            }
            inventory.setStackInSlot(reservedSlot + index, ItemStack.EMPTY);
        }
        if (!reservedFluid.isEmpty()) {
            fluidInput.fill(reservedFluid.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE),
                    IFluidHandler.FluidAction.EXECUTE);
        }
        return true;
    }

    private boolean hasReservedInput() {
        return !reservedFluid.isEmpty() || reservedStacks().stream().anyMatch(stack -> !stack.isEmpty());
    }

    private List<ItemStack> inputStacks() {
        List<ItemStack> inputs = new ArrayList<>(inputSlots);
        for (int index = 0; index < inputSlots; index++) {
            inputs.add(inventory.getStackInSlot(index));
        }
        return List.copyOf(inputs);
    }

    private List<ItemStack> reservedStacks() {
        List<ItemStack> reserved = new ArrayList<>(inputSlots);
        for (int index = 0; index < inputSlots; index++) {
            reserved.add(inventory.getStackInSlot(reservedSlot + index));
        }
        return List.copyOf(reserved);
    }

    private void clearReservedInputs() {
        for (int index = 0; index < inputSlots; index++) {
            inventory.setStackInSlot(reservedSlot + index, ItemStack.EMPTY);
        }
        reservedFluid.setFluid(FluidStack.EMPTY);
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    public double overclockFactor() {
        return level == null ? 1.0D : AugmentScanner.overclockFactor(level, worldPosition);
    }

    protected double processingProgressMultiplier() {
        return activeProgressMultiplier;
    }

    protected boolean canProcess() {
        return true;
    }

    @Nullable
    protected TierManager.ProcessingParameters processingParameters(
            ResourceLocation recipeId, ClayiumMachineRecipe recipe) {
        return TierManager.calculate(
                series().parameters(tier()),
                ProgressionRateRules.adjustedRecipeTicks(recipe),
                recipe.clayEnergy(), overclockFactor());
    }

    public int fuelLimit() {
        return level == null ? 1 : AugmentScanner.fuelLimit(level, worldPosition);
    }

    private void enforceFuelLimit() {
        ItemStack fuel = inventory.getStackInSlot(fuelSlot);
        int excessCount = fuel.getCount() - fuelLimit();
        if (excessCount <= 0 || level == null) {
            return;
        }
        ItemStack excess = inventory.extractItem(fuelSlot, excessCount, false);
        Containers.dropItemStack(
                level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                excess);
    }

    public MachineSeries series() {
        return ((ClayMachineBlock)getBlockState().getBlock()).series();
    }

    public int tier() {
        return ((ClayMachineBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public int inputSlots() {
        return inputSlots;
    }

    public int outputSlot() {
        return outputSlot;
    }

    public int outputSlot(int outputIndex) {
        return outputSlot + outputIndex;
    }

    public int outputSlots() {
        return outputSlots;
    }

    public int reservedSlot(int inputIndex) {
        return reservedSlot + inputIndex;
    }

    public int fuelSlot() {
        return fuelSlot;
    }

    public ClayEnergyStorage energyStorage() {
        return energy;
    }

    public FluidTank fluidInput() {
        return fluidInput;
    }

    public FluidTank fluidOutput() {
        return fluidOutput;
    }

    public IFluidHandler fluidHandler(@Nullable Direction direction) {
        return fluidHandler;
    }

    public void receiveClayEnergy(ClayEnergy amount) {
        energy.add(amount);
        setChanged();
    }

    public MachineState state() {
        return state;
    }

    public long progress() {
        return progress;
    }

    public long requiredProgress() {
        return requiredProgress;
    }

    @Nullable
    public ResourceLocation activeRecipe() {
        return activeRecipe;
    }

    public FaceMode faceMode(Direction direction) {
        int insertion = insertionRoute(direction);
        int extraction = extractionRoute(direction);
        if (insertion >= 0 && extraction >= 0) return FaceMode.INPUT_OUTPUT;
        if (extraction >= 0) return FaceMode.OUTPUT;
        if (insertion == 1) return FaceMode.FUEL_INPUT;
        if (insertion == 0) return FaceMode.INPUT;
        return FaceMode.DISABLED;
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        switch (mode) {
            case INPUT -> sideConfiguration.setRoutes(ioFacing(), direction, 0, -1);
            case FUEL_INPUT -> sideConfiguration.setRoutes(ioFacing(), direction,
                    insertionRouteCount() > 1 ? 1 : 0, -1);
            case OUTPUT -> sideConfiguration.setRoutes(ioFacing(), direction, -1, 0);
            case INPUT_OUTPUT -> sideConfiguration.setRoutes(ioFacing(), direction, 0, 0);
            case DISABLED -> sideConfiguration.setRoutes(ioFacing(), direction, -1, -1);
        }
        ioConfigurationChanged();
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return ClayMachineBlock.PIPE;
    }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() {
        return ClayMachineBlock.FACING;
    }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChangedAndSync();
        ioNeighborsChanged();
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    @Override
    public void setRemoteRedstonePowered(boolean powered) {
        if (powered && level != null) {
            lastRemoteRedstoneTick = level.getGameTime();
        } else if (!powered) {
            lastRemoteRedstoneTick = Long.MIN_VALUE;
        }
    }

    @Override
    public boolean isRemoteRedstonePowered() {
        return level != null && lastRemoteRedstoneTick >= level.getGameTime() - 1;
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropped.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
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
        return new ClayMachineMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.put("fluid_input", fluidInput.writeToNBT(registries, new CompoundTag()));
        tag.put("reserved_fluid", reservedFluid.writeToNBT(registries, new CompoundTag()));
        tag.put("fluid_output", fluidOutput.writeToNBT(registries, new CompoundTag()));
        tag.putString("state", state.name());
        if (activeRecipe != null) {
            tag.putString("active_recipe", activeRecipe.toString());
            tag.putBoolean("vanilla_smelting_recipe", activeVanillaSmeltingRecipe);
        }
        if (!pendingOutputs.isEmpty()) {
            ListTag outputs = new ListTag();
            pendingOutputs.forEach(stack -> outputs.add(stack.save(registries)));
            tag.put("pending_outputs", outputs);
        }
        if (!pendingFluidOutput.isEmpty()) {
            tag.put("pending_fluid_output", pendingFluidOutput.save(registries));
        }
        tag.putLong("progress", progress);
        tag.putLong("required_progress", requiredProgress);
        tag.putLong("energy_per_progress", energyPerProgress);
        tag.putDouble("progress_remainder", progressRemainder);
        tag.putDouble("progress_per_tick", progressPerTick);
        tag.putDouble("active_progress_multiplier", activeProgressMultiplier);
        tag.putInt("auto_insert_tick", autoInsertTick);
        tag.putInt("auto_extract_tick", autoExtractTick);
        tag.putInt("next_input_direction", nextInputDirection);
        tag.putInt("next_output_direction", nextOutputDirection);
        saveIoState(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.getCompound("energy"));
        }
        fluidInput.readFromNBT(registries, tag.getCompound("fluid_input"));
        reservedFluid.readFromNBT(registries, tag.getCompound("reserved_fluid"));
        fluidOutput.readFromNBT(registries, tag.getCompound("fluid_output"));
        state = parseEnum(MachineState.class, tag.getString("state"), MachineState.IDLE);
        activeRecipe = tag.contains("active_recipe") ? ResourceLocation.tryParse(tag.getString("active_recipe")) : null;
        activeRecipeValue = null;
        activeVanillaSmeltingRecipe = tag.getBoolean("vanilla_smelting_recipe");
        ListTag outputs = tag.getList("pending_outputs", Tag.TAG_COMPOUND);
        List<ItemStack> loadedOutputs = new ArrayList<>(outputs.size());
        for (int index = 0; index < outputs.size(); index++) {
            loadedOutputs.add(ItemStack.parseOptional(registries, outputs.getCompound(index)));
        }
        pendingOutputs = List.copyOf(loadedOutputs);
        pendingFluidOutput = tag.contains("pending_fluid_output")
                ? FluidStack.parseOptional(registries, tag.getCompound("pending_fluid_output"))
                : FluidStack.EMPTY;
        progress = tag.getLong("progress");
        requiredProgress = tag.getLong("required_progress");
        energyPerProgress = tag.getLong("energy_per_progress");
        progressRemainder = tag.getDouble("progress_remainder");
        progressPerTick = tag.getDouble("progress_per_tick");
        activeProgressMultiplier = tag.getDouble("active_progress_multiplier");
        autoInsertTick = Math.max(0, tag.getInt("auto_insert_tick"));
        autoExtractTick = Math.max(0, tag.getInt("auto_extract_tick"));
        nextInputDirection = Math.floorMod(tag.getInt("next_input_direction"), Direction.values().length);
        nextOutputDirection = Math.floorMod(tag.getInt("next_output_direction"), Direction.values().length);
        validateRecipeOnLoad = activeRecipe != null;
        loadIoState(tag, registries);
        if (activeRecipe != null && (pendingOutputs.isEmpty() && pendingFluidOutput.isEmpty() || !hasReservedInput())) {
            clearCorruptProcess();
        }
    }

    private void clearCorruptProcess() {
        activeRecipe = null;
        activeRecipeValue = null;
        activeVanillaSmeltingRecipe = false;
        pendingOutputs = List.of();
        pendingFluidOutput = FluidStack.EMPTY;
        progress = 0L;
        requiredProgress = 0L;
        energyPerProgress = 0L;
        progressRemainder = 0.0D;
        progressPerTick = 1.0D;
        activeProgressMultiplier = 1.0D;

        state = MachineState.IDLE;
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

    protected void setState(MachineState newState) {
        if (state != newState) {
            state = newState;
            setChanged();
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public enum MachineState {
        IDLE,
        RUNNING,
        OUTPUT_FULL,
        WAITING_FOR_ENERGY,
        RECIPE_CHANGED
    }

    public enum FaceMode {
        INPUT,
        OUTPUT,
        DISABLED,
        FUEL_INPUT,
        INPUT_OUTPUT;

        public boolean allowsInput() {
            return this == INPUT || this == FUEL_INPUT || this == INPUT_OUTPUT;
        }

        public FaceMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final class MachineContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0, 1, 2, 3 -> (int)(progress >>> (index * 16)) & 0xFFFF;
                case 4, 5, 6, 7 -> (int)(requiredProgress >>> ((index - 4) * 16)) & 0xFFFF;
                case 8 -> state.ordinal();
                case 9 -> tier();
                case 10 -> fluidInput.isEmpty() ? 0 : BuiltInRegistries.FLUID.getId(fluidInput.getFluid().getFluid()) + 1;
                case 11 -> fluidInput.getFluidAmount();
                case 12 -> fluidOutput.isEmpty() ? 0 : BuiltInRegistries.FLUID.getId(fluidOutput.getFluid().getFluid()) + 1;
                case 13 -> fluidOutput.getFluidAmount();
                case 14, 15, 16, 17 -> (int)(requiredEnergy() >>> ((index - 14) * 16)) & 0xFFFF;
                case 18 -> ClayMachineBlockEntity.this instanceof CaReactorCoreBlockEntity reactor
                        && reactor.isFormed() ? 1 : 0;
                case 19 -> ClayMachineBlockEntity.this instanceof CaReactorCoreBlockEntity reactor
                        ? reactor.coilCount() : 0;
                case 20 -> ClayMachineBlockEntity.this instanceof CaReactorCoreBlockEntity reactor
                        ? reactor.hullCount() : 0;
                case 21 -> ClayMachineBlockEntity.this instanceof CaReactorCoreBlockEntity reactor
                        ? (int)Math.round(reactor.averageRank() * 100.0D) : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 22;
        }
    }

    public long requiredEnergy() {
        return requiredProgress == 0L || energyPerProgress <= Long.MAX_VALUE / requiredProgress
                ? requiredProgress * energyPerProgress : Long.MAX_VALUE;
    }

    private final class SidedItemHandler implements IItemHandler {
        @Nullable
        private final Direction direction;

        private SidedItemHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getSlots() {
            if (direction == null) {
                return inputSlots + outputSlots + 1;
            }
            return slotsFor(direction).length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(mappedSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int mappedSlot = mappedSlot(slot);
            return (direction == null && !isOutputSlot(mappedSlot))
                            || direction != null && contains(insertionSlots(insertionRoute(direction)), mappedSlot)
                    ? inventory.insertItem(mappedSlot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mappedSlot = mappedSlot(slot);
            return direction == null && isOutputSlot(mappedSlot)
                            || direction != null && contains(extractionSlots(extractionRoute(direction)), mappedSlot)
                    ? inventory.extractItem(mappedSlot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(mappedSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int mappedSlot = mappedSlot(slot);
            return ((direction == null && !isOutputSlot(mappedSlot))
                            || direction != null && contains(insertionSlots(insertionRoute(direction)), mappedSlot))
                    && (direction == null || matchesFilter(direction, stack))
                    && inventory.isItemValid(mappedSlot, stack);
        }

        private int mappedSlot(int slot) {
            if (direction == null) {
                if (slot < inputSlots) {
                    return slot;
                }
                return slot < inputSlots + outputSlots
                        ? outputSlot + slot - inputSlots : fuelSlot;
            }
            int[] slots = slotsFor(direction);
            if (slot < 0 || slot >= slots.length) throw new IndexOutOfBoundsException("Machine side slot: " + slot);
            return slots[slot];
        }

        private boolean isOutputSlot(int slot) {
            return slot >= outputSlot && slot < outputSlot + outputSlots;
        }

        private int[] slotsFor(Direction side) {
            int[] insertion = insertionSlots(insertionRoute(side));
            int[] extraction = extractionSlots(extractionRoute(side));
            return java.util.stream.IntStream.concat(
                    java.util.Arrays.stream(insertion), java.util.Arrays.stream(extraction)).distinct().toArray();
        }
    }

    private int[] insertionSlots(int route) {
        if (route == 0) return java.util.stream.IntStream.range(0, inputSlots).toArray();
        if (route == 1 && insertionRouteCount() > 1) return new int[] {fuelSlot};
        return new int[0];
    }

    private int[] extractionSlots(int route) {
        return route == 0
                ? java.util.stream.IntStream.range(outputSlot, outputSlot + outputSlots).toArray()
                : new int[0];
    }

    private static boolean contains(int[] slots, int slot) {
        for (int candidate : slots) if (candidate == slot) return true;
        return false;
    }

    private final class MachineFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? fluidInput.getFluid() : fluidOutput.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? fluidInput.getCapacity() : fluidOutput.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && fluidInput.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return fluidInput.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return fluidOutput.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return fluidOutput.drain(maxDrain, action);
        }
    }

    private record SelectedRecipe(ResourceLocation id, ClayiumMachineRecipe recipe, boolean vanillaSmelting) {
    }
}
