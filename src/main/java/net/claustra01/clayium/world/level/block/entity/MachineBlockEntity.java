/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.Optional;
import java.util.List;
import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergyReceiver;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.machine.MachineLayout;
import net.claustra01.clayium.machine.MachinePerformance;
import net.claustra01.clayium.machine.MachineModifiers;
import net.claustra01.clayium.machine.ClayBlastFurnaceStructure;
import net.claustra01.clayium.machine.ConfigurableClayEnergyMachine;
import net.claustra01.clayium.recipe.MachineIngredient;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.recipe.MachineRecipeInput;
import net.claustra01.clayium.recipe.MachineRecipeLookup;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.minecraft.server.level.ServerLevel;

/** Server-owned runtime for the common Clayium machine recipe layouts. */
public final class MachineBlockEntity extends BaseContainerBlockEntity
        implements ConfigurableClayEnergyMachine, net.claustra01.clayium.laser.ClayLaserReceiver {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = MachineLayout.STORAGE_SLOT_COUNT;
    private static final int IDLE_RECIPE_RECHECK_TICKS = 20;
    // The original machines did not have a shared finite internal CE capacity.
    // Long.MAX_VALUE is an overflow guard, not a gameplay storage limit.
    private static final long ENERGY_CAPACITY = Long.MAX_VALUE;
    private static final long ENERGY_TRANSFER_LIMIT = Long.MAX_VALUE;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ClayEnergyStorage energy =
            new ClayEnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER_LIMIT, ENERGY_TRANSFER_LIMIT);
    @Nullable
    private ResourceLocation activeRecipeId;
    private long progress;
    private long totalProgress;
    private int recipeRecheckDelay;
    private long activeEnergyPerTick;
    private StopReason stopReason = StopReason.NO_RECIPE;
    private final int[] insertionRoutes = new int[]{-1, 0, -1, 1, -1, -1};
    private final int[] extractionRoutes = new int[]{0, -1, -1, -1, -1, -1};
    private final ItemStack[] filters = new ItemStack[6];
    private final net.claustra01.clayium.logistics.SideConfiguration sideConfiguration;
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches =
            new EnumMap<>(Direction.class);
    private int automationCooldown;
    private int structureCheckDelay;
    private boolean structureFormed;
    private net.claustra01.clayium.tier.ClayTier structureTier = net.claustra01.clayium.tier.ClayTier.RAW;
    private net.claustra01.clayium.machine.CAReactorStructure.Result caReactorStructure =
            net.claustra01.clayium.machine.CAReactorStructure.Result.invalid(
                    net.claustra01.clayium.tier.ClayTier.RAW);
    private boolean externalWorkEnabled = true;
    private boolean externalSingleRun;
    private long pendingLaserProgress;
    private int fabricationBatchSize;
    private double activeResonance = 1.0D;
    private int modifierCheckDelay;
    private MachineModifiers.Snapshot modifiers = MachineModifiers.Snapshot.DEFAULT;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> displayedProgress();
                case 1 -> displayedTotalProgress();
                case 2 -> (int) energy.energyStored();
                case 3 -> (int) (energy.energyStored() >>> 32);
                case 4 -> stopReason.ordinal();
                case 5 -> {
                    MachineBlock block = machineBlock();
                    yield block == null ? 0 : recipeTier().progressionIndex();
                }
                case 6 -> (int) activeEnergyPerTick;
                case 7 -> (int) (activeEnergyPerTick >>> 32);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, value);
                case 1 -> totalProgress = Math.max(0, value);
                case 2 -> energy.setEnergy((energy.energyStored() & 0xFFFFFFFF00000000L)
                        | Integer.toUnsignedLong(value));
                case 3 -> energy.setEnergy((Integer.toUnsignedLong(value) << 32)
                        | (energy.energyStored() & 0xFFFFFFFFL));
                case 4 -> stopReason = StopReason.byOrdinal(value);
                case 6 -> activeEnergyPerTick = (activeEnergyPerTick & 0xFFFFFFFF00000000L)
                        | Integer.toUnsignedLong(value);
                case 7 -> activeEnergyPerTick = (Integer.toUnsignedLong(value) << 32)
                        | (activeEnergyPerTick & 0xFFFFFFFFL);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    private int displayedProgress() {
        if (totalProgress <= Integer.MAX_VALUE) return (int)Math.min(progress, Integer.MAX_VALUE);
        return (int)Math.min(1_000_000L, progress >= totalProgress ? 1_000_000L
                : progress * 1_000_000L / totalProgress);
    }

    private int displayedTotalProgress() {
        return totalProgress <= Integer.MAX_VALUE ? (int)totalProgress : 1_000_000;
    }

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            validateSlot(slot);
            return getItem(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            validateSlot(slot);
            if ((!machineLayout().isInputSlot(slot) && slot != MachineLayout.ENERGY_SLOT) || stack.isEmpty()) {
                return stack;
            }
            if (slot == MachineLayout.ENERGY_SLOT
                    && (!acceptsEnergeticClay() || !EnergeticClayFuel.isFuel(stack))) {
                return stack;
            }
            ItemStack current = getItem(slot);
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                return stack;
            }
            int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            int accepted = Math.min(stack.getCount(), limit - current.getCount());
            if (accepted <= 0) {
                return stack;
            }
            if (!simulate) {
                ItemStack next = current.isEmpty() ? stack.copyWithCount(accepted) : current.copy();
                if (!current.isEmpty()) {
                    next.grow(accepted);
                }
                setItem(slot, next);
            }
            return accepted == stack.getCount()
                    ? ItemStack.EMPTY
                    : stack.copyWithCount(stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            validateSlot(slot);
            if (!machineLayout().isOutputSlot(slot, machineTier()) || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack current = getItem(slot);
            int extracted = Math.min(amount, current.getCount());
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = current.copyWithCount(extracted);
            if (!simulate) {
                removeItem(slot, extracted);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            validateSlot(slot);
            return slot == MachineLayout.ENERGY_SLOT && acceptsEnergeticClay()
                    ? modifiers.energySlotLimit() : getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validateSlot(slot);
            return machineLayout().isInputSlot(slot)
                    || slot == MachineLayout.ENERGY_SLOT
                            && acceptsEnergeticClay()
                            && EnergeticClayFuel.isFuel(stack);
        }

        private void validateSlot(int slot) {
            if (slot < 0 || slot >= SLOT_COUNT) {
                throw new IndexOutOfBoundsException("Machine slot: " + slot);
            }
        }
    };

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.MACHINE_BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(filters, ItemStack.EMPTY);
        sideConfiguration = new net.claustra01.clayium.logistics.SideConfiguration(
                insertionRoutes, extractionRoutes, filters,
                this::insertionRouteCount, this::extractionRouteCount);
        for (Direction direction : Direction.values()) {
            sidedHandlers.put(direction, new SidedMachineHandler(direction));
        }
    }

    @Override public net.claustra01.clayium.logistics.SideConfiguration sideConfiguration(){return sideConfiguration;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity ioOwner(){return this;}
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty(){return MachineBlock.PIPE;}
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty(){return MachineBlock.FACING;}
    @Override public void ioConfigurationChanged(){configurationChanged();}

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity machine) {
        boolean mayRun = machine.refreshStructureState();
        if (mayRun) {
            machine.tickAutomation();
        }
        machine.serverTick();
    }

    private boolean refreshStructureState() {
        MachineBlock block = machineBlock();
        if (block == null || (!block.machineId().equals(
                net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_BLAST_FURNACE)
                && !block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)
                && !block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR))) {
            return true;
        }
        if (structureCheckDelay-- > 0) {
            return structureFormed;
        }
        structureCheckDelay = 20;
        if (!(level instanceof ServerLevel serverLevel)) {
            structureFormed = false;
            structureTier = net.claustra01.clayium.tier.ClayTier.RAW;
            return false;
        }
        if (block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR)) {
            caReactorStructure = net.claustra01.clayium.machine.CAReactorStructure.validate(
                    serverLevel, worldPosition, getBlockState().getValue(MachineBlock.FACING), block.tier());
            structureFormed = caReactorStructure.formed();
            structureTier = block.tier();
        } else if (block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)) {
            net.claustra01.clayium.machine.ClayReactorStructure.Result result =
                    net.claustra01.clayium.machine.ClayReactorStructure.validate(serverLevel, worldPosition,
                            getBlockState().getValue(MachineBlock.FACING));
            structureFormed = result.formed();
            structureTier = result.tier();
        } else {
            ClayBlastFurnaceStructure.Result result = ClayBlastFurnaceStructure.validate(
                    serverLevel, worldPosition, getBlockState().getValue(MachineBlock.FACING));
            structureFormed = result.formed();
            structureTier = result.recipeTier();
        }
        if (getBlockState().getValue(MachineBlock.FORMED) != structureFormed) {
            level.setBlock(worldPosition, getBlockState().setValue(MachineBlock.FORMED, structureFormed), 3);
        }
        return structureFormed;
    }

    private void tickAutomation() {
        if (level == null || machineTier().progressionIndex() < 3
                || ++automationCooldown < automationInterval()) {
            return;
        }
        automationCooldown = 0;
        for (Direction direction : Direction.values()) {
            int relative = relativeIndex(direction);
            if (insertionRoutes[relative] >= 0 && pullFrom(direction, insertionRoutes[relative])) {
                return;
            }
            if (extractionRoutes[relative] < 0) {
                continue;
            }
            IItemHandler target = targetHandler(direction);
            if (target == null) {
                continue;
            }
            for (int outputSlot : extractionSlots(extractionRoutes[relative])) {
                ItemStack output = getItem(outputSlot);
                if (output.isEmpty()) {
                    continue;
                }
                ItemStack offered = output.copyWithCount(Math.min(automationLimit(), output.getCount()));
                ItemStack remainder = offered;
                for (int slot = 0; slot < target.getSlots() && !remainder.isEmpty(); slot++) {
                    remainder = target.insertItem(slot, remainder, false);
                }
                int moved = offered.getCount() - remainder.getCount();
                if (moved > 0) {
                    removeItem(outputSlot, moved);
                    setChanged();
                    return;
                }
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
            ItemStack offered = source.extractItem(sourceSlot, automationLimit(), true);
            if (offered.isEmpty()) {
                continue;
            }
            int accepted = 0;
            ItemStack remainder = offered;
            for (int slot : insertionSlots(route)) {
                ItemStack next = destination.insertItem(slot, remainder, true);
                accepted += remainder.getCount() - next.getCount();
                remainder = next;
            }
            if (accepted <= 0) {
                continue;
            }
            ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
            ItemStack uninserted = extracted;
            for (int slot : insertionSlots(route)) {
                uninserted = destination.insertItem(slot, uninserted, false);
            }
            return uninserted.isEmpty();
        }
        return false;
    }

    private int automationInterval() {
        int tier = machineTier().progressionIndex();
        return tier <= 4 ? 20 : tier == 5 ? 2 : 1;
    }

    private int automationLimit() {
        int tier = machineTier().progressionIndex();
        return tier <= 4 ? 8 : tier == 5 ? 16 : 64;
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

    private void serverTick() {
        if (modifierCheckDelay-- <= 0) {
            modifiers = MachineModifiers.scan(level, worldPosition);
            modifierCheckDelay = 20;
        }
        MachineBlock machineBlock = machineBlock();
        if (machineBlock == null) {
            stopReason = StopReason.INVALID_BLOCK;
            return;
        }
        if ((machineBlock.machineId().equals(
                net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_BLAST_FURNACE)
                || machineBlock.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)
                || machineBlock.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR))
                && !structureFormed) {
            stopReason = StopReason.INVALID_STRUCTURE;
            resetProcessing();
            return;
        }
        if (!externalWorkEnabled && !externalSingleRun) {
            stopReason = StopReason.REDSTONE_DISABLED;
            return;
        }
        if (machineBlock.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.SOLAR_CLAY_FABRICATOR)
                && !level.canSeeSky(worldPosition.above())) {
            stopReason = StopReason.NO_SKY_ACCESS;
            return;
        }
        Optional<RecipeHolder<MachineRecipe>> recipe = resolveRecipe(machineBlock);
        if (recipe.isEmpty()) {
            stopReason = recipeInput().isEmpty() ? StopReason.NO_INPUT : StopReason.NO_RECIPE;
            resetProcessing();
            return;
        }

        MachineRecipe value = recipe.get().value();
        if (progress == 0) {
            activeResonance = net.claustra01.clayium.machine.ResonanceField.at(level, worldPosition);
        }
        if (fabricationBatchSize <= 0) {
            fabricationBatchSize = net.claustra01.clayium.machine.MachineProcessPolicy.batchSize(
                    machineBlock.machineId(), getItem(machineLayout().inputSlots()[0]).getCount());
        }
        long baseProcessingTime = net.claustra01.clayium.machine.MachineProcessPolicy.processingTime(
                MachinePerformance.processingTime(value, machineBlock.machineId(), recipeTier()),
                machineBlock.machineId(), machineTier(), fabricationBatchSize);
        baseProcessingTime = net.claustra01.clayium.machine.MachineProcessPolicy.resonanceProcessingTime(
                baseProcessingTime, machineBlock.machineId(), machineTier(), activeResonance);
        if (machineBlock.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR)) {
            baseProcessingTime = net.claustra01.clayium.machine.CAReactorStructure.processingTime(
                    baseProcessingTime, caReactorStructure);
        }
        totalProgress = Math.max(1L, (long)Math.ceil(baseProcessingTime / modifiers.overclockFactor()));
        long energyPerTick = Math.max(0L, Math.round(MachinePerformance.clayEnergyPerTick(
                value, machineBlock.machineId(), recipeTier()) * modifiers.energyFactor()));
        if (machineBlock.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR)) {
            double adjusted = energyPerTick * caReactorStructure.energyMultiplier();
            energyPerTick = adjusted >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(0L, Math.round(adjusted));
        }
        activeEnergyPerTick = energyPerTick;
        if (!canOutput(value)) {
            stopReason = StopReason.OUTPUT_BLOCKED;
            return;
        }
        if (energy.extract(energyPerTick, true) != energyPerTick) {
            if (!consumeOneEnergeticClay()
                    || energy.extract(energyPerTick, true) != energyPerTick) {
                stopReason = StopReason.INSUFFICIENT_ENERGY;
                return;
            }
        }

        energy.extract(energyPerTick, false);
        progress++;
        if (machineBlock.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)
                && pendingLaserProgress > 0) {
            progress = pendingLaserProgress > Long.MAX_VALUE - progress
                    ? Long.MAX_VALUE
                    : progress + pendingLaserProgress;
            pendingLaserProgress = 0;
        }
        stopReason = StopReason.RUNNING;
        if (progress >= totalProgress) {
            complete(value);
            if (externalSingleRun) {
                externalSingleRun = false;
                externalWorkEnabled = false;
            }
        }
        setChanged();
    }

    private boolean consumeOneEnergeticClay() {
        if (!acceptsEnergeticClay()) {
            return false;
        }
        ItemStack fuel = getItem(MachineLayout.ENERGY_SLOT);
        long value = EnergeticClayFuel.value(fuel);
        if (value <= 0 || energy.energyStored() > Long.MAX_VALUE - value) {
            return false;
        }
        if (energy.receive(value, true) == value) {
            energy.receive(value, false);
            fuel.shrink(1);
            setChanged();
            return true;
        }
        return false;
    }

    private Optional<RecipeHolder<MachineRecipe>> resolveRecipe(MachineBlock block) {
        MachineRecipeInput input = recipeInput();
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }
        if (activeRecipeId != null) {
            Optional<RecipeHolder<MachineRecipe>> resolved = level.getRecipeManager().getRecipeFor(
                    ClayiumRecipes.MACHINE_RECIPE_TYPE.get(),
                    input,
                    level,
                    activeRecipeId);
            if (resolved.isPresent()
                    && resolved.get().value().machine().equals(block.machineId())
                    && recipeTier().isAtLeast(resolved.get().value().minimumTier())) {
                return resolved;
            }
            Optional<RecipeHolder<MachineRecipe>> adapted =
                    MachineRecipeLookup.find(level, block.machineId(), recipeTier(), inputStacks());
            if (adapted.isPresent() && adapted.get().id().equals(activeRecipeId)) {
                return adapted;
            }
            resetProcessing();
        }
        if (recipeRecheckDelay > 0) {
            recipeRecheckDelay--;
            return Optional.empty();
        }
        recipeRecheckDelay = IDLE_RECIPE_RECHECK_TICKS;
        Optional<RecipeHolder<MachineRecipe>> found =
                MachineRecipeLookup.find(level, block.machineId(), recipeTier(), inputStacks());
        found.ifPresent(holder -> {
            activeRecipeId = holder.id();
            totalProgress = MachinePerformance.processingTime(
                    holder.value(), block.machineId(), recipeTier());
        });
        return found;
    }

    private void complete(MachineRecipe recipe) {
        MachineRecipeInput input = recipeInput();
        Optional<int[]> matchedSlots = recipe.matchInputSlots(input);
        if (matchedSlots.isEmpty() || !canOutput(recipe)) {
            stopReason = StopReason.OUTPUT_BLOCKED;
            return;
        }
        int[] outputSlots = machineLayout().outputSlots(machineTier());
        List<ItemStack> results = recipe.results();
        for (int index = 0; index < Math.min(outputSlots.length, results.size()); index++) {
            int outputSlot = outputSlots[index];
            ItemStack result = processingResult(recipe, results.get(index));
            ItemStack output = getItem(outputSlot);
            if (output.isEmpty()) {
                items.set(outputSlot, result.copy());
            } else {
                output.grow(result.getCount());
            }
        }
        if (net.claustra01.clayium.machine.MachineProcessPolicy.consumesInputs(recipe.machine())) {
            int[] inputSlots = machineLayout().inputSlots();
            for (int ingredientIndex = 0; ingredientIndex < recipe.ingredients().size(); ingredientIndex++) {
                int inventorySlot = inputSlots[matchedSlots.get()[ingredientIndex]];
                MachineIngredient ingredient = recipe.ingredients().get(ingredientIndex);
                getItem(inventorySlot).shrink(ingredient.count());
            }
        }
        resetProcessing();
        recipeRecheckDelay = 0;
    }

    private boolean canOutput(MachineRecipe recipe) {
        int[] outputSlots = machineLayout().outputSlots(machineTier());
        List<ItemStack> results = recipe.results();
        for (int index = 0; index < Math.min(outputSlots.length, results.size()); index++) {
            ItemStack result = processingResult(recipe, results.get(index));
            ItemStack output = getItem(outputSlots[index]);
            if (!output.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(output, result)
                            || result.getCount() > output.getMaxStackSize() - output.getCount())) {
                return false;
            }
        }
        return true;
    }

    private ItemStack processingResult(MachineRecipe recipe, ItemStack recipeResult) {
        if (recipe.machine().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR)) {
            return net.claustra01.clayium.machine.LateGameMaterials.pureAntimatter(
                    caReactorStructure.productRank());
        }
        int count = net.claustra01.clayium.machine.MachineProcessPolicy.outputCount(
                recipe.machine(), recipeResult.getCount(), fabricationBatchSize);
        count = net.claustra01.clayium.machine.MachineProcessPolicy.resonanceOutputCount(
                recipe.machine(), count, activeResonance);
        count = Math.min(count, recipeResult.getMaxStackSize());
        return recipeResult.copyWithCount(count);
    }

    private void resetProcessing() {
        activeRecipeId = null;
        progress = 0;
        totalProgress = 0;
        activeEnergyPerTick = 0;
        fabricationBatchSize = 0;
        activeResonance = 1.0D;
    }

    @Nullable
    private MachineBlock machineBlock() {
        return getBlockState().getBlock() instanceof MachineBlock block ? block : null;
    }

    private MachineLayout machineLayout() {
        MachineBlock block = machineBlock();
        return block == null ? MachineLayout.SIMPLE : MachineLayout.forMachine(block.machineId());
    }

    private net.claustra01.clayium.tier.ClayTier machineTier() {
        MachineBlock block = machineBlock();
        return block == null ? net.claustra01.clayium.tier.ClayTier.RAW : block.tier();
    }

    private net.claustra01.clayium.tier.ClayTier recipeTier() {
        MachineBlock block = machineBlock();
        return block != null && (block.machineId().equals(
                net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_BLAST_FURNACE)
                || block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)
                || block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR))
                ? structureTier : machineTier();
    }

    @Override public boolean receiveClayLaser(net.claustra01.clayium.laser.ClayLaser laser, Direction incomingSide) {
        MachineBlock block = machineBlock();
        if (block == null || !block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)) return false;
        pendingLaserProgress = Math.max(pendingLaserProgress, Math.max(1L, (long)laser.energy()));
        return true;
    }

    /** Resolves the conversion exposed through an adjacent PAN Adapter without mutating this machine. */
    public Optional<net.claustra01.clayium.pan.PanConversion> panConversion(
            List<ItemStack> patternInputs, List<ItemStack> auxiliaryItems) {
        MachineBlock block = machineBlock();
        if (level == null || block == null) return Optional.empty();
        boolean structured = block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_BLAST_FURNACE)
                || block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)
                || block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR);
        if (structured && !structureFormed) return Optional.empty();
        Optional<RecipeHolder<MachineRecipe>> found = MachineRecipeLookup.find(
                level, block.machineId(), recipeTier(), patternInputs);
        if (found.isEmpty()) return Optional.empty();
        MachineRecipe recipe = found.get().value();
        Optional<int[]> matched = recipe.matchInputSlots(new MachineRecipeInput(patternInputs));
        if (matched.isEmpty()) return Optional.empty();
        List<ItemStack> ingredients = new java.util.ArrayList<>();
        for (int index = 0; index < recipe.ingredients().size(); index++) {
            ingredients.add(patternInputs.get(matched.get()[index]).copyWithCount(recipe.ingredients().get(index).count()));
        }

        double resonance = net.claustra01.clayium.machine.ResonanceField.at(level, worldPosition);
        int batch = net.claustra01.clayium.machine.MachineProcessPolicy.batchSize(
                block.machineId(), patternInputs.isEmpty() ? 1 : patternInputs.getFirst().getCount());
        long time = net.claustra01.clayium.machine.MachineProcessPolicy.processingTime(
                MachinePerformance.processingTime(recipe, block.machineId(), recipeTier()),
                block.machineId(), machineTier(), batch);
        time = net.claustra01.clayium.machine.MachineProcessPolicy.resonanceProcessingTime(
                time, block.machineId(), machineTier(), resonance);
        long energyPerTick = Math.max(0L, Math.round(MachinePerformance.clayEnergyPerTick(
                recipe, block.machineId(), recipeTier()) * modifiers.energyFactor()));
        time = Math.max(1L, (long)Math.ceil((double)time / modifiers.overclockFactor()));

        List<ItemStack> results = new java.util.ArrayList<>();
        if (block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CA_REACTOR)) {
            time = net.claustra01.clayium.machine.CAReactorStructure.processingTime(time, caReactorStructure);
            double adjusted = energyPerTick * caReactorStructure.energyMultiplier();
            energyPerTick = adjusted >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(0L, Math.round(adjusted));
            results.add(net.claustra01.clayium.machine.LateGameMaterials.pureAntimatter(caReactorStructure.productRank()));
        } else {
            for (ItemStack result : recipe.results()) {
                int count = net.claustra01.clayium.machine.MachineProcessPolicy.outputCount(
                        block.machineId(), result.getCount(), batch);
                count = net.claustra01.clayium.machine.MachineProcessPolicy.resonanceOutputCount(
                        block.machineId(), count, resonance);
                results.add(result.copyWithCount(Math.min(result.getMaxStackSize(), count)));
            }
        }

        if (block.machineId().equals(net.claustra01.clayium.machine.ClayiumMachineIds.CLAY_REACTOR)) {
            int blue = 0, green = 0, red = 0;
            for (ItemStack stack : auxiliaryItems) {
                if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)
                        || !(blockItem.getBlock() instanceof net.claustra01.clayium.world.level.block.ClayEnergyLaserBlock laser)) continue;
                switch (laser.tier().progressionIndex()) {
                    case 7 -> blue += stack.getCount();
                    case 8 -> green += stack.getCount();
                    case 9 -> red += stack.getCount();
                    default -> { }
                }
            }
            long acceleration = Math.max(1L, (long)(net.claustra01.clayium.laser.ClayLaser.energy(blue, green, red) + 1.0D));
            time = Math.max(1L, Math.max(0L, time - 1L) / acceleration + 1L);
            energyPerTick = saturatingAdd(energyPerTick,
                    saturatingAdd(40_000L * (long)blue,
                            saturatingAdd(400_000L * (long)green, 4_000_000L * (long)red)));
        }
        return Optional.of(new net.claustra01.clayium.pan.PanConversion(
                ingredients, results, saturatingProduct(time, energyPerTick)));
    }

    private static long saturatingAdd(long first, long second) {
        if (first > Long.MAX_VALUE - second) return Long.MAX_VALUE;
        return first + second;
    }

    private static double saturatingProduct(long first, long second) {
        double product = (double)first * second;
        return Double.isFinite(product) ? product : Double.MAX_VALUE;
    }

    private boolean acceptsEnergeticClay() {
        MachineBlock block = machineBlock();
        return machineTier().progressionIndex() >= 4 && block != null
                && net.claustra01.clayium.machine.MachineProcessPolicy.acceptsEnergeticClay(block.machineId());
    }

    private List<ItemStack> inputStacks() {
        return java.util.Arrays.stream(machineLayout().inputSlots())
                .mapToObj(this::getItem)
                .toList();
    }

    private MachineRecipeInput recipeInput() {
        return new MachineRecipeInput(inputStacks());
    }

    public IItemHandler itemHandler() {
        return itemHandler(Direction.UP);
    }

    public IItemHandler itemHandler(Direction direction) {
        return sidedHandlers.get(direction);
    }

    /** Core inventory view used by a synchronized Clay Interface's own side routes. */
    public IItemHandler interfaceItemHandler(int insertRoute, int extractRoute, ItemStack filter) {
        return new IItemHandler() {
            @Override public int getSlots() { return itemHandler.getSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(slot); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return contains(insertionSlots(insertRoute), slot) && matchesFilter(filter, stack)
                        ? itemHandler.insertItem(slot, stack, simulate) : stack;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return contains(extractionSlots(extractRoute), slot)
                        ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return contains(insertionSlots(insertRoute), slot)
                        && matchesFilter(filter, stack) && itemHandler.isItemValid(slot, stack);
            }
        };
    }

    @Override
    public long receiveClayEnergy(long amount, boolean simulate) {
        return energy.receive(amount, simulate);
    }

    public long clayEnergyStored() {
        return energy.energyStored();
    }

    public boolean isWorkScheduled() { return activeRecipeId != null; }
    public boolean isDoingWork() { return stopReason == StopReason.RUNNING; }
    public void setExternalWorkEnabled(boolean enabled) {
        if (externalWorkEnabled == enabled && !externalSingleRun) return;
        externalWorkEnabled = enabled;
        if (!enabled) externalSingleRun = false;
        setChanged();
    }
    public void runOnce() {
        externalSingleRun = true;
        externalWorkEnabled = false;
        setChanged();
    }

    public int machineTierIndex() {
        return machineTier().progressionIndex();
    }

    public boolean addManualEnergy() {
        if (level == null || level.isClientSide) {
            return false;
        }
        MachineBlock block = machineBlock();
        Optional<RecipeHolder<MachineRecipe>> recipe = block == null
                ? Optional.empty()
                : resolveRecipe(block);
        if (recipe.isEmpty() || !canOutput(recipe.get().value())) {
            return false;
        }
        long accepted = energy.receive(5, false);
        if (accepted > 0) {
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    protected Component getDefaultName() {
        MachineBlock block = machineBlock();
        return block == null
                ? Component.translatable("container.clayium_neoforged.machine")
                : Component.translatable(block.getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return createInterfaceMenu(containerId, inventory);
    }

    public AbstractContainerMenu createInterfaceMenu(int containerId, Inventory inventory) {
        return new MachineMenu(containerId, inventory, this, menuData, machineLayout(), machineTier());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return machineLayout().isInputSlot(slot)
                || slot == MachineLayout.ENERGY_SLOT
                        && acceptsEnergeticClay()
                        && EnergeticClayFuel.isFuel(stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == MachineLayout.ENERGY_SLOT && acceptsEnergeticClay()) {
            stack.limitSize(Math.min(stack.getMaxStackSize(), modifiers.energySlotLimit()));
        }
        super.setItem(slot, stack);
        if (machineLayout().isInputSlot(slot)) {
            resetProcessing();
            recipeRecheckDelay = 0;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        energy.load(tag);
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe"))
                : null;
        progress = Math.max(0L, tag.getLong("Progress"));
        totalProgress = Math.max(0L, tag.getLong("TotalProgress"));
        fabricationBatchSize = Math.max(0, Math.min(64, tag.getInt("FabricationBatchSize")));
        activeResonance = Math.max(1.0D, tag.contains("ActiveResonance")
                ? tag.getDouble("ActiveResonance") : 1.0D);
        externalWorkEnabled = !tag.contains("ExternalWorkEnabled") || tag.getBoolean("ExternalWorkEnabled");
        externalSingleRun = tag.getBoolean("ExternalSingleRun");
        sideConfiguration.replaceRoutes(
                loadRoutes(tag, "InsertionRoutes", insertionRoutes, insertionRouteCount()),
                loadRoutes(tag, "ExtractionRoutes", extractionRoutes, extractionRouteCount()));
        ListTag savedFilters = tag.getList("Filters", Tag.TAG_COMPOUND);
        for (int index = 0; index < Math.min(6, savedFilters.size()); index++) {
            filters[index] = ItemStack.parseOptional(registries, savedFilters.getCompound(index));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        energy.save(tag);
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        }
        tag.putLong("Progress", progress);
        tag.putLong("TotalProgress", totalProgress);
        tag.putInt("FabricationBatchSize", fabricationBatchSize);
        tag.putDouble("ActiveResonance", activeResonance);
        tag.putBoolean("ExternalWorkEnabled", externalWorkEnabled);
        tag.putBoolean("ExternalSingleRun", externalSingleRun);
        tag.putIntArray("InsertionRoutes", insertionRoutes);
        tag.putIntArray("ExtractionRoutes", extractionRoutes);
        ListTag savedFilters = new ListTag();
        for (ItemStack filter : filters) {
            savedFilters.add(filter.saveOptional(registries));
        }
        tag.put("Filters", savedFilters);
    }

    private static boolean matchesFilter(ItemStack filter, ItemStack stack) {
        return filter.isEmpty() || ClayFilterItem.matches(filter, stack);
    }

    public String insertionIcon(Direction side) {
        return interfaceInsertionIcon(insertionRoute(side));
    }

    public String interfaceInsertionIcon(int route) {
        if (machineLayout() == MachineLayout.ASSEMBLER || machineLayout() == MachineLayout.CHEMICAL) {
            return switch (route) {
                case 0 -> "import";
                case 1 -> "import_energy";
                case 2 -> "import_1";
                case 3 -> "import_2";
                default -> "";
            };
        }
        return route == 0 ? "import" : route == 1 ? "import_energy" : "";
    }

    public String extractionIcon(Direction side) {
        return interfaceExtractionIcon(extractionRoute(side));
    }

    public String interfaceExtractionIcon(int route) {
        if (machineLayout() == MachineLayout.CHEMICAL) {
            return switch (route) {
                case 0 -> "export";
                case 1 -> "export_1";
                case 2 -> "export_2";
                default -> "";
            };
        }
        return route == 0 ? "export" : "";
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
        Direction front = getBlockState().getValue(MachineBlock.FACING);
        return RelativeFace.index(front, direction);
    }

    private int insertionRouteCount() {
        return machineLayout() == MachineLayout.ASSEMBLER || machineLayout() == MachineLayout.CHEMICAL
                ? 4 : 2;
    }

    private int extractionRouteCount() {
        return machineLayout() == MachineLayout.CHEMICAL ? 3 : 1;
    }

    public int interfaceInsertionRouteCount() {
        return insertionRouteCount();
    }

    public int interfaceExtractionRouteCount() {
        return extractionRouteCount();
    }

    private int[] insertionSlots(int route) {
        if (route < 0) {
            return new int[0];
        }
        if (machineLayout() == MachineLayout.ASSEMBLER || machineLayout() == MachineLayout.CHEMICAL) {
            return switch (route) {
                case 0 -> new int[]{0, 1};
                case 1 -> acceptsEnergeticClay() ? new int[]{MachineLayout.ENERGY_SLOT} : new int[0];
                case 2 -> new int[]{0};
                case 3 -> new int[]{1};
                default -> new int[0];
            };
        }
        return switch (route) {
            case 0 -> machineLayout().inputSlots();
            case 1 -> acceptsEnergeticClay() ? new int[]{MachineLayout.ENERGY_SLOT} : new int[0];
            default -> new int[0];
        };
    }

    private int[] extractionSlots(int route) {
        if (machineLayout() == MachineLayout.CHEMICAL) {
            return switch (route) {
                case 0 -> new int[]{2, 3};
                case 1 -> new int[]{2};
                case 2 -> new int[]{3};
                default -> new int[0];
            };
        }
        return route == 0 ? machineLayout().outputSlots(machineTier()) : new int[0];
    }

    private void configurationChanged() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static int nextRoute(int route, int routeCount) {
        return route + 1 >= routeCount ? -1 : route + 1;
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

    private static boolean contains(int[] slots, int wanted) {
        for (int slot : slots) {
            if (slot == wanted) {
                return true;
            }
        }
        return false;
    }

    private final class SidedMachineHandler implements IItemHandler {
        private final Direction side;

        private SidedMachineHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int relative = relativeIndex(side);
            int route = insertionRoutes[relative];
            return contains(insertionSlots(route), slot) && matchesFilter(filters[relative], stack)
                    ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int route = extractionRoutes[relativeIndex(side)];
            return contains(extractionSlots(route), slot)
                    ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int relative = relativeIndex(side);
            return contains(insertionSlots(insertionRoutes[relative]), slot)
                    && matchesFilter(filters[relative], stack)
                    && itemHandler.isItemValid(slot, stack);
        }
    }

    public enum StopReason {
        RUNNING,
        NO_INPUT,
        NO_RECIPE,
        INSUFFICIENT_ENERGY,
        OUTPUT_BLOCKED,
        NO_SKY_ACCESS,
        INVALID_STRUCTURE,
        REDSTONE_DISABLED,
        INVALID_BLOCK;

        public static StopReason byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : INVALID_BLOCK;
        }
    }
}
