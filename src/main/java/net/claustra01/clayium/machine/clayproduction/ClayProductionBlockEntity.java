package net.claustra01.clayium.machine.clayproduction;

import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ClayProductionBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int RESERVED_SLOT = 2;
    public static final int FUEL_SLOT = 3;
    public static final int LIMIT_SLOT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> validInput(stack);
                case FUEL_SLOT -> type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER
                        && ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                case LIMIT_SLOT -> type().kind() == ClayProductionType.Kind.AUTOMATIC_CONDENSER
                        && ClayProductionRules.productionLevel(stack).isPresent();
                default -> false;
            };
        }
    };
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private final ContainerData menuData = new ProductionContainerData();

    private State state = State.IDLE;
    private long progress;
    private long requiredProgress;
    private long energyPerTick;
    private long displayedCePerTick;
    private int activeInputLevel = -1;
    private ResourceLocation activeRecipe;
    private ClayiumMachineRecipe activeRecipeValue;

    public ClayProductionBlockEntity(BlockPos pos, BlockState state) {
        super(ClayProductionRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClayProductionBlockEntity machine) {
        if (level instanceof ServerLevel serverLevel) {
            machine.tickServer(serverLevel);
        }
    }

    private void tickServer(ServerLevel level) {
        switch (type().kind()) {
            case ENERGETIC_CONDENSER -> tickEnergeticCondenser();
            case AUTOMATIC_CONDENSER -> tickAutomaticCondenser();
            case SOLAR_FABRICATOR -> tickSolarFabricator(level);
            case CLAY_FABRICATOR -> tickClayFabricator();
        }
    }

    private void tickEnergeticCondenser() {
        ServerLevel server = (ServerLevel)level;
        if (activeRecipe != null) {
            ClayiumMachineRecipe current = server.getRecipeManager().byKey(activeRecipe)
                    .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                    .map(holder -> (ClayiumMachineRecipe)holder.value()).orElse(null);
            if (current == null || activeRecipeValue != null && current != activeRecipeValue
                    || current.requiredTier() > type().tier()
                    || current.processingTicks() != requiredProgress
                    || current.clayEnergy() != energyPerTick
                    || !current.itemInputs().getFirst().ingredient().test(inventory.getStackInSlot(RESERVED_SLOT))) {
                ItemStack reserved = inventory.getStackInSlot(RESERVED_SLOT);
                ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
                if (input.isEmpty()) {
                    inventory.setStackInSlot(INPUT_SLOT, reserved.copy());
                } else if (ItemStack.isSameItemSameComponents(input, reserved)) {
                    input.grow(reserved.getCount());
                }
                inventory.setStackInSlot(RESERVED_SLOT, ItemStack.EMPTY);
                activeRecipe = null;
                activeRecipeValue = null;
                setIdle(State.INVALID_INPUT);
                setChanged();
                return;
            }
            activeRecipeValue = current;
        }
        if (activeRecipe == null) {
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            RecipeHolder<ClayiumMachineRecipe> match = server.getRecipeManager()
                    .getAllRecipesFor(RecipeRegistration.type(ClayiumRecipeMethod.EC_CONDENSER)).stream()
                    .filter(holder -> holder.value().requiredTier() <= type().tier())
                    .filter(holder -> holder.value().itemInputs().size() == 1
                            && holder.value().itemInputs().getFirst().ingredient().test(input)
                            && input.getCount() >= holder.value().itemInputs().getFirst().count())
                    .findFirst().orElse(null);
            if (match == null) {
                setIdle(State.INVALID_INPUT);
                return;
            }
            ClayiumMachineRecipe recipe = match.value();
            ItemStack result = recipe.itemOutputs().getFirst();
            if (!canAcceptOutput(result)) {
                setIdle(State.OUTPUT_FULL);
                return;
            }
            if (!canPower(recipe.clayEnergy())) {
                setIdle(State.WAITING_FOR_ENERGY);
                return;
            }
            activeInputLevel = ClayProductionRules.productionLevel(input).orElse(-1);
            inventory.setStackInSlot(RESERVED_SLOT, inventory.extractItem(INPUT_SLOT,
                    recipe.itemInputs().getFirst().count(), false));
            activeRecipe = match.id();
            activeRecipeValue = recipe;
            progress = 0L;
            requiredProgress = recipe.processingTicks();
            energyPerTick = recipe.clayEnergy();
            displayedCePerTick = -energyPerTick;
        }
        ItemStack result = activeRecipeValue.itemOutputs().getFirst();
        if (!canAcceptOutput(result)) {
            state = State.OUTPUT_FULL;
            return;
        }
        if (!energy.consume(ClayEnergy.ofInternal(energyPerTick), inventory.getStackInSlot(FUEL_SLOT))) {
            state = State.WAITING_FOR_ENERGY;
            return;
        }
        state = State.RUNNING;
        progress++;
        if (progress >= requiredProgress) {
            finish(result);
        }
        setChanged();
    }

    private void tickAutomaticCondenser() {
        int maximum = inventory.getStackInSlot(LIMIT_SLOT).isEmpty()
                ? 13 : ClayProductionRules.productionLevel(inventory.getStackInSlot(LIMIT_SLOT)).orElse(13);
        boolean compressed = compressAutomaticStack(INPUT_SLOT, maximum);
        if (!compressed) {
            compressed = compressAutomaticStack(OUTPUT_SLOT, maximum);
        }
        if (compressed && type() == ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_II) {
            while (compressAutomaticStack(OUTPUT_SLOT, maximum)) {
            }
        }
        if (!compressed) {
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            int level = ClayProductionRules.productionLevel(input).orElse(-1);
            ItemStack result = level >= 0 && level < maximum
                    ? new ItemStack(ClayProductionRules.itemForLevel(level + 1)) : ItemStack.EMPTY;
            State idleState = State.IDLE;
            if (!result.isEmpty() && input.getCount() >= 9 && !canAcceptOutput(result)) {
                idleState = State.OUTPUT_FULL;
            } else if (level < 0 && !input.isEmpty()) {
                idleState = State.INVALID_INPUT;
            }
            setIdle(idleState);
        } else {
            progress = 1L;
            requiredProgress = 1L;
            energyPerTick = 0L;
            displayedCePerTick = 0L;
            state = State.RUNNING;
            setChanged();
        }
    }

    private boolean compressAutomaticStack(int sourceSlot, int maximum) {
        ItemStack source = inventory.getStackInSlot(sourceSlot);
        int level = ClayProductionRules.productionLevel(source).orElse(-1);
        if (level < 0 || level >= maximum || source.getCount() < 9) {
            return false;
        }
        if (sourceSlot == OUTPUT_SLOT && source.getCount() % 9 != 0) {
            return false;
        }
        ItemStack result = new ItemStack(ClayProductionRules.itemForLevel(level + 1));
        if (sourceSlot == INPUT_SLOT && !canAcceptOutput(result)) {
            return false;
        }
        source.shrink(9);
        if (sourceSlot == OUTPUT_SLOT) {
            result.setCount(source.getCount() / 9 + 1);
            inventory.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            addOutput(result);
        }
        activeInputLevel = level;
        return true;
    }

    private void tickSolarFabricator(ServerLevel level) {
        BlockPos top = worldPosition.above();
        if (level.getBlockState(top).isSolidRender(level, top)
                || !level.canSeeSky(top) || !level.isDay() || level.isRaining()) {
            state = State.SKY_BLOCKED;
            setChanged();
            return;
        }
        if (inventory.getStackInSlot(RESERVED_SLOT).isEmpty()) {
            int inputLevel = ClayProductionRules.solarInputLevel(type(), inventory.getStackInSlot(INPUT_SLOT));
            if (inputLevel < 0 || inputLevel > type().maximumProductionLevel() || inputLevel >= 13) {
                setIdle(State.INVALID_INPUT);
                return;
            }
            ItemStack result = new ItemStack(ClayProductionRules.itemForLevel(inputLevel + 1));
            if (!canAcceptOutput(result)) {
                setIdle(State.OUTPUT_FULL);
                return;
            }
            inventory.setStackInSlot(RESERVED_SLOT, inventory.extractItem(INPUT_SLOT, 1, false));
            activeInputLevel = inputLevel;
            progress = 0L;
            requiredProgress = ClayiumConfig.divideProgressionTicks(
                    ClayProductionRules.solarTicks(type(), inputLevel));
            energyPerTick = 0L;
            displayedCePerTick = (ClayProductionRules.clayEnergyValue(inputLevel + 1)
                    - ClayProductionRules.clayEnergyValue(inputLevel)) / requiredProgress;
        }
        ItemStack result = new ItemStack(ClayProductionRules.itemForLevel(activeInputLevel + 1));
        if (!canAcceptOutput(result)) {
            state = State.OUTPUT_FULL;
            return;
        }
        state = State.RUNNING;
        progress++;
        if (progress >= requiredProgress) {
            finish(result);
        }
        setChanged();
    }

    private void tickClayFabricator() {
        if (inventory.getStackInSlot(RESERVED_SLOT).isEmpty()) {
            ItemStack sample = inventory.getStackInSlot(INPUT_SLOT);
            int inputLevel = ClayProductionRules.productionLevel(sample).orElse(-1);
            if (inputLevel < 0 || inputLevel > type().maximumProductionLevel()) {
                setIdle(State.INVALID_INPUT);
                return;
            }
            if (!canAcceptOutput(sample)) {
                setIdle(State.OUTPUT_FULL);
                return;
            }
            inventory.setStackInSlot(RESERVED_SLOT, sample.copy());
            activeInputLevel = inputLevel;
            progress = 0L;
            requiredProgress = ClayiumConfig.divideProgressionTicks(
                    ClayProductionRules.fabricatorTicks(type(), inputLevel, sample.getCount()));
            energyPerTick = 0L;
            displayedCePerTick = ClayProductionRules.clayEnergyValue(inputLevel) * sample.getCount() / requiredProgress;
        }
        ItemStack result = inventory.getStackInSlot(RESERVED_SLOT).copy();
        if (!canAcceptOutput(result)) {
            state = State.OUTPUT_FULL;
            return;
        }
        state = State.RUNNING;
        progress++;
        if (progress >= requiredProgress) {
            finish(result);
        }
        setChanged();
    }

    private void finish(ItemStack result) {
        addOutput(result);
        inventory.setStackInSlot(RESERVED_SLOT, ItemStack.EMPTY);
        progress = 0L;
        requiredProgress = 0L;
        energyPerTick = 0L;
        displayedCePerTick = 0L;
        activeInputLevel = -1;
        activeRecipe = null;
        activeRecipeValue = null;
        state = State.IDLE;
    }

    private void addOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    private boolean canAcceptOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private boolean canPower(long required) {
        return required == 0L || energy.stored().internalValue() >= required
                || type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER && ClayEnergyStorage.fuelValue(inventory.getStackInSlot(FUEL_SLOT)).internalValue() > 0L;
    }

    private boolean validInput(ItemStack stack) {
        return switch (type().kind()) {
            case SOLAR_FABRICATOR -> ClayProductionRules.solarInputLevel(type(), stack) >= 0;
            default -> ClayProductionRules.productionLevel(stack).isPresent();
        };
    }

    private void setIdle(State idleState) {
        if (inventory.getStackInSlot(RESERVED_SLOT).isEmpty()) {
            progress = 0L;
            requiredProgress = 0L;
            energyPerTick = 0L;
            displayedCePerTick = 0L;
            activeInputLevel = -1;
            state = idleState;
        }
    }

    public ClayProductionType type() {
        return ((ClayProductionBlock)getBlockState().getBlock()).type();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ClayEnergyStorage energyStorage() {
        return energy;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return new ProductionItemHandler(direction);
    }

    public void receiveClayEnergy(ClayEnergy amount) {
        energy.add(amount);
        setChanged();
    }

    public State state() {
        return state;
    }

    public long progress() {
        return progress;
    }

    public long requiredProgress() {
        return requiredProgress;
    }

    public long displayedCePerTick() {
        return displayedCePerTick;
    }

    public int activeInputLevel() {
        return activeInputLevel;
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(5);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (slot == RESERVED_SLOT && type().kind() == ClayProductionType.Kind.CLAY_FABRICATOR) {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                continue;
            }
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
        return new ClayProductionMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putString("state", state.name());
        tag.putLong("progress", progress);
        tag.putLong("required_progress", requiredProgress);
        tag.putLong("energy_per_tick", energyPerTick);
        tag.putLong("displayed_ce_per_tick", displayedCePerTick);
        tag.putInt("active_input_level", activeInputLevel);
        if (activeRecipe != null) {
            tag.putString("active_recipe", activeRecipe.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        energy.deserializeNBT(registries, tag.getCompound("energy"));
        state = State.valueOf(tag.getString("state"));
        progress = tag.getLong("progress");
        requiredProgress = tag.getLong("required_progress");
        energyPerTick = tag.getLong("energy_per_tick");
        displayedCePerTick = tag.getLong("displayed_ce_per_tick");
        activeInputLevel = tag.getInt("active_input_level");
        activeRecipe = tag.contains("active_recipe") ? ResourceLocation.parse(tag.getString("active_recipe")) : null;
        activeRecipeValue = null;
    }

    public enum State {
        IDLE,
        RUNNING,
        OUTPUT_FULL,
        WAITING_FOR_ENERGY,
        SKY_BLOCKED,
        INVALID_INPUT
    }

    private final class ProductionContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0, 1, 2, 3 -> word(progress, index);
                case 4, 5, 6, 7 -> word(requiredProgress, index - 4);
                case 8, 9, 10, 11 -> word(energy.stored().internalValue(), index - 8);
                case 12, 13, 14, 15 -> word(displayedCePerTick, index - 12);
                case 16 -> state.ordinal();
                case 17 -> type().tier();
                case 18 -> activeInputLevel >= 0 ? activeInputLevel
                        : ClayProductionRules.productionLevel(inventory.getStackInSlot(INPUT_SLOT)).orElseGet(() ->
                                ClayProductionRules.productionLevel(inventory.getStackInSlot(OUTPUT_SLOT)).orElse(0));
                default -> 0;
            };
        }

        private int word(long value, int index) {
            return (int)(value >>> (index * 16)) & 0xFFFF;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 19;
        }
    }

    private final class ProductionItemHandler implements IItemHandler {
        @Nullable
        private final Direction direction;

        private ProductionItemHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getSlots() {
            if (direction == null) {
                return 4;
            }
            return direction == Direction.DOWN ? 1 : 3;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(mappedSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int mappedSlot = mappedSlot(slot);
            return mappedSlot == OUTPUT_SLOT ? stack : inventory.insertItem(mappedSlot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return mappedSlot(slot) == OUTPUT_SLOT
                    ? inventory.extractItem(OUTPUT_SLOT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(mappedSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int mappedSlot = mappedSlot(slot);
            return mappedSlot != OUTPUT_SLOT && inventory.isItemValid(mappedSlot, stack);
        }

        private int mappedSlot(int slot) {
            if (direction == Direction.DOWN) {
                return OUTPUT_SLOT;
            }
            if (direction == null && slot == 3) {
                return OUTPUT_SLOT;
            }
            return switch (slot) {
                case 0 -> INPUT_SLOT;
                case 1 -> FUEL_SLOT;
                default -> LIMIT_SLOT;
            };
        }
    }
}
