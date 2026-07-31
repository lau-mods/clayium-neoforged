/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.Optional;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergyReceiver;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.machine.MachineLayout;
import net.claustra01.clayium.machine.MachinePerformance;
import net.claustra01.clayium.recipe.MachineIngredient;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.recipe.MachineRecipeInput;
import net.claustra01.clayium.recipe.MachineRecipeLookup;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.MachineMenu;
import net.claustra01.clayium.world.level.block.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

/** Server-owned runtime for the common Clayium machine recipe layouts. */
public final class MachineBlockEntity extends BaseContainerBlockEntity implements ClayEnergyReceiver {
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
    private int progress;
    private int totalProgress;
    private int recipeRecheckDelay;
    private long activeEnergyPerTick;
    private StopReason stopReason = StopReason.NO_RECIPE;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> totalProgress;
                case 2 -> (int) energy.energyStored();
                case 3 -> (int) (energy.energyStored() >>> 32);
                case 4 -> stopReason.ordinal();
                case 5 -> {
                    MachineBlock block = machineBlock();
                    yield block == null ? 0 : block.tier().progressionIndex();
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
            if (!machineLayout().isInputSlot(slot) || stack.isEmpty()) {
                return stack;
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
            return getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validateSlot(slot);
            return machineLayout().isInputSlot(slot);
        }

        private void validateSlot(int slot) {
            if (slot < 0 || slot >= SLOT_COUNT) {
                throw new IndexOutOfBoundsException("Machine slot: " + slot);
            }
        }
    };

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity machine) {
        machine.serverTick();
    }

    private void serverTick() {
        MachineBlock machineBlock = machineBlock();
        if (machineBlock == null) {
            stopReason = StopReason.INVALID_BLOCK;
            return;
        }

        Optional<RecipeHolder<MachineRecipe>> recipe = resolveRecipe(machineBlock);
        if (recipe.isEmpty()) {
            stopReason = recipeInput().isEmpty() ? StopReason.NO_INPUT : StopReason.NO_RECIPE;
            resetProcessing();
            return;
        }

        MachineRecipe value = recipe.get().value();
        totalProgress = MachinePerformance.processingTime(value, machineBlock.machineId(), machineBlock.tier());
        long energyPerTick =
                MachinePerformance.clayEnergyPerTick(value, machineBlock.machineId(), machineBlock.tier());
        activeEnergyPerTick = energyPerTick;
        if (!canOutput(value)) {
            stopReason = StopReason.OUTPUT_BLOCKED;
            return;
        }
        if (energy.extract(energyPerTick, true) != energyPerTick) {
            stopReason = StopReason.INSUFFICIENT_ENERGY;
            return;
        }

        energy.extract(energyPerTick, false);
        progress++;
        stopReason = StopReason.RUNNING;
        if (progress >= totalProgress) {
            complete(value);
        }
        setChanged();
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
                    && block.tier().isAtLeast(resolved.get().value().minimumTier())) {
                return resolved;
            }
            Optional<RecipeHolder<MachineRecipe>> adapted =
                    MachineRecipeLookup.find(level, block.machineId(), block.tier(), inputStacks());
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
                MachineRecipeLookup.find(level, block.machineId(), block.tier(), inputStacks());
        found.ifPresent(holder -> {
            activeRecipeId = holder.id();
            totalProgress = MachinePerformance.processingTime(
                    holder.value(), block.machineId(), block.tier());
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
            ItemStack result = results.get(index);
            ItemStack output = getItem(outputSlot);
            if (output.isEmpty()) {
                items.set(outputSlot, result.copy());
            } else {
                output.grow(result.getCount());
            }
        }
        int[] inputSlots = machineLayout().inputSlots();
        for (int ingredientIndex = 0; ingredientIndex < recipe.ingredients().size(); ingredientIndex++) {
            int inventorySlot = inputSlots[matchedSlots.get()[ingredientIndex]];
            MachineIngredient ingredient = recipe.ingredients().get(ingredientIndex);
            getItem(inventorySlot).shrink(ingredient.count());
        }
        resetProcessing();
        recipeRecheckDelay = 0;
    }

    private boolean canOutput(MachineRecipe recipe) {
        int[] outputSlots = machineLayout().outputSlots(machineTier());
        List<ItemStack> results = recipe.results();
        for (int index = 0; index < Math.min(outputSlots.length, results.size()); index++) {
            ItemStack result = results.get(index);
            ItemStack output = getItem(outputSlots[index]);
            if (!output.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(output, result)
                            || result.getCount() > output.getMaxStackSize() - output.getCount())) {
                return false;
            }
        }
        return true;
    }

    private void resetProcessing() {
        activeRecipeId = null;
        progress = 0;
        totalProgress = 0;
        activeEnergyPerTick = 0;
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

    private List<ItemStack> inputStacks() {
        return java.util.Arrays.stream(machineLayout().inputSlots())
                .mapToObj(this::getItem)
                .toList();
    }

    private MachineRecipeInput recipeInput() {
        return new MachineRecipeInput(inputStacks());
    }

    public IItemHandler itemHandler() {
        return itemHandler;
    }

    @Override
    public long receiveClayEnergy(long amount, boolean simulate) {
        return energy.receive(amount, simulate);
    }

    public long clayEnergyStored() {
        return energy.energyStored();
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
        return machineLayout().isInputSlot(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
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
        progress = Math.max(0, tag.getInt("Progress"));
        totalProgress = Math.max(0, tag.getInt("TotalProgress"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        energy.save(tag);
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        }
        tag.putInt("Progress", progress);
        tag.putInt("TotalProgress", totalProgress);
    }

    public enum StopReason {
        RUNNING,
        NO_INPUT,
        NO_RECIPE,
        INSUFFICIENT_ENERGY,
        OUTPUT_BLOCKED,
        INVALID_BLOCK;

        public static StopReason byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : INVALID_BLOCK;
        }
    }
}
