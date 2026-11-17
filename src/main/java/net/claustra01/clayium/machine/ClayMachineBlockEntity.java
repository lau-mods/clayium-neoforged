package net.claustra01.clayium.machine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeCache;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.claustra01.clayium.recipe.ClayiumRecipeInput;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ClayMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int RESERVED_SLOT = 2;
    public static final int FUEL_SLOT = 3;

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

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> true;
                case FUEL_SLOT -> ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                default -> false;
            };
        }
    };
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private final EnumMap<Direction, FaceMode> faceModes = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final IItemHandler unsidedHandler = new SidedItemHandler(null);
    private final ContainerData menuData = new MachineContainerData();

    private MachineState state = MachineState.IDLE;
    private ResourceLocation activeRecipe;
    private ClayiumMachineRecipe activeRecipeValue;
    private ItemStack pendingOutput = ItemStack.EMPTY;
    private long progress;
    private long requiredProgress;
    private long energyPerProgress;
    private double progressRemainder;
    private double progressPerTick = 1.0D;
    private boolean validateRecipeOnLoad;

    public ClayMachineBlockEntity(BlockPos pos, BlockState state) {
        super(MachineRegistration.MACHINE_BLOCK_ENTITY.get(), pos, state);
        LOADED_MACHINES.add(new WeakReference<>(this));
        for (Direction direction : Direction.values()) {
            faceModes.put(direction, direction == Direction.DOWN ? FaceMode.OUTPUT : FaceMode.INPUT);
            sidedHandlers.put(direction, new SidedItemHandler(direction));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClayMachineBlockEntity machine) {
        machine.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (activeRecipe == null && !inventory.getStackInSlot(RESERVED_SLOT).isEmpty()) {
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
            ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)serverLevel.getRecipeManager().byKey(activeRecipe).orElseThrow().value();
            TierManager.ProcessingParameters parameters = TierManager.calculate(
                    series().parameters(tier()), recipe.processingTicks(), recipe.clayEnergy(), overclockFactor());
            ItemStack fixedOutput = combinedFixedOutput(recipe);
            if (requiredProgress != parameters.requiredProgress()
                    || energyPerProgress != parameters.energyPerProgress().internalValue()
                    || !fixedOutput.isEmpty() && (!ItemStack.isSameItemSameComponents(fixedOutput, pendingOutput)
                    || fixedOutput.getCount() > pendingOutput.getCount())) {
                abortProcess();
                return;
            }
            activeRecipeValue = recipe;
        }
        if (!canAcceptOutput(pendingOutput)) {
            setState(MachineState.OUTPUT_FULL);
            return;
        }
        double nextRemainder = progressRemainder + progressPerTick;
        long completedProgress = Math.min(requiredProgress - progress, Math.max(1L, (long)Math.floor(nextRemainder)));
        if (!consumeProcessingEnergy(completedProgress)) {
            setState(MachineState.WAITING_FOR_ENERGY);
            return;
        }
        setState(MachineState.RUNNING);
        progressRemainder = nextRemainder - completedProgress;
        progress = Math.min(requiredProgress, saturatedAdd(progress, completedProgress));
        if (progress >= requiredProgress) {
            finishProcess();
        } else {
            setChanged();
        }
    }

    private void tryStart(ServerLevel serverLevel) {
        if (inventory.getStackInSlot(INPUT_SLOT).isEmpty()
                || !inventory.getStackInSlot(RESERVED_SLOT).isEmpty()) {
            setState(MachineState.IDLE);
            return;
        }
        ProcessingSeries series = series();
        ClayiumRecipeInput input = new ClayiumRecipeInput(
                List.of(inventory.getStackInSlot(INPUT_SLOT)),
                List.of(),
                tier(),
                availableClayEnergy(),
                worldPosition);
        Optional<RecipeHolder<ClayiumMachineRecipe>> match = ClayiumRecipeCache.find(
                serverLevel.getRecipeManager(), RecipeRegistration.type(series.recipeMethod()), input, serverLevel);
        if (match.isEmpty()) {
            setState(MachineState.IDLE);
            return;
        }
        ClayiumMachineRecipe recipe = match.get().value();
        ItemStack fixedOutput = combinedFixedOutput(recipe);
        if (!fixedOutput.isEmpty() && !canAcceptOutput(fixedOutput)) {
            setState(MachineState.OUTPUT_FULL);
            return;
        }
        TierManager.ProcessingParameters parameters = TierManager.calculate(
                series.parameters(tier()), recipe.processingTicks(), recipe.clayEnergy(), overclockFactor());
        if (!canPower(parameters.energyPerProgress().internalValue())) {
            setState(MachineState.WAITING_FOR_ENERGY);
            return;
        }
        ItemStack output = combinedOutput(recipe, fixedOutput);
        if (output.isEmpty() || !canAcceptOutput(output) || !reserveInput(recipe)) {
            setState(output.isEmpty() ? MachineState.IDLE : MachineState.OUTPUT_FULL);
            return;
        }
        activeRecipe = match.get().id();
        activeRecipeValue = recipe;

        pendingOutput = output;
        progress = 0L;
        requiredProgress = parameters.requiredProgress();
        energyPerProgress = parameters.energyPerProgress().internalValue();
        progressRemainder = 0.0D;
        progressPerTick = parameters.progressPerTick();
        setState(MachineState.RUNNING);
        setChanged();
    }

    private ItemStack combinedFixedOutput(ClayiumMachineRecipe recipe) {
        ItemStack combined = ItemStack.EMPTY;
        for (ItemStack result : recipe.itemOutputs()) {
            combined = appendOutput(combined, result);
            if (combined.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return combined;
    }

    private ItemStack combinedOutput(ClayiumMachineRecipe recipe, ItemStack fixedOutput) {
        ItemStack combined = fixedOutput.copy();
        for (ClayiumMachineRecipe.RandomOutput randomOutput : recipe.randomOutputs()) {
            if (level != null && level.random.nextFloat() <= randomOutput.chance()) {
                combined = appendOutput(combined, randomOutput.stack());
                if (combined.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return combined;
    }

    private static ItemStack appendOutput(ItemStack combined, ItemStack result) {
        if (combined.isEmpty()) {
            return result.copy();
        }
        if (!ItemStack.isSameItemSameComponents(combined, result)
                || combined.getCount() + result.getCount() > combined.getMaxStackSize()) {
            return ItemStack.EMPTY;
        }
        combined.grow(result.getCount());
        return combined;
    }

    private boolean reserveInput(ClayiumMachineRecipe recipe) {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (recipe.itemInputs().size() != 1) {
            return false;
        }
        SizedIngredient required = recipe.itemInputs().getFirst();
        if (!required.ingredient().test(input) || input.getCount() < required.count()) {
            return false;
        }
        ItemStack reserved = input.copyWithCount(required.count());
        inventory.extractItem(INPUT_SLOT, required.count(), false);
        inventory.setStackInSlot(RESERVED_SLOT, reserved);
        return true;
    }

    private boolean consumeProcessingEnergy(long completedProgress) {
        if (energyPerProgress == 0L) {
            return true;
        }
        ItemStack fuel = tier() >= 4 ? inventory.getStackInSlot(FUEL_SLOT) : ItemStack.EMPTY;
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
                || tier() >= 4 && ClayEnergyStorage.fuelValue(inventory.getStackInSlot(FUEL_SLOT)).internalValue() > 0L;
    }

    private long availableClayEnergy() {
        long available = energy.stored().internalValue();
        if (tier() >= 4) {
            available = saturatedAdd(
                    available,
                    ClayEnergyStorage.fuelValue(inventory.getStackInSlot(FUEL_SLOT)).internalValue());
        }
        return available;
    }

    private boolean canAcceptOutput(ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        ItemStack existing = inventory.getStackInSlot(OUTPUT_SLOT);
        return existing.isEmpty()
                || ItemStack.isSameItemSameComponents(existing, result)
                && existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private void finishProcess() {
        ItemStack existing = inventory.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, pendingOutput.copy());
        } else {
            existing.grow(pendingOutput.getCount());
        }
        inventory.setStackInSlot(RESERVED_SLOT, ItemStack.EMPTY);
        activeRecipe = null;
        activeRecipeValue = null;
        pendingOutput = ItemStack.EMPTY;
        progress = 0L;
        requiredProgress = 0L;
        energyPerProgress = 0L;
        progressRemainder = 0.0D;
        progressPerTick = 1.0D;

        setState(MachineState.IDLE);
        setChanged();
    }

    private boolean activeRecipeIsCurrent(RecipeManager manager) {
        return manager.byKey(activeRecipe)
                .filter(holder -> holder.value() instanceof ClayiumMachineRecipe)
                .map(holder -> (ClayiumMachineRecipe)holder.value())
                .filter(recipe -> recipe.processingMethod() == series().recipeMethod())
                .filter(recipe -> recipe.requiredTier() <= tier())
                .filter(recipe -> activeRecipeValue == null || activeRecipeValue == recipe)
                .filter(recipe -> recipe.itemInputs().size() == 1
                        && recipe.itemInputs().getFirst().ingredient().test(inventory.getStackInSlot(RESERVED_SLOT))
                        && recipe.itemInputs().getFirst().count() == inventory.getStackInSlot(RESERVED_SLOT).getCount())
                .isPresent();
    }

    private void abortProcess() {
        activeRecipe = null;
        activeRecipeValue = null;
        pendingOutput = ItemStack.EMPTY;
        progress = 0L;
        requiredProgress = 0L;
        energyPerProgress = 0L;
        progressRemainder = 0.0D;
        progressPerTick = 1.0D;

        if (recoverReservedInput()) {
            setState(MachineState.IDLE);
        } else {
            setState(MachineState.RECIPE_CHANGED);
        }
        setChanged();
    }

    private boolean recoverReservedInput() {
        ItemStack reserved = inventory.getStackInSlot(RESERVED_SLOT);
        if (reserved.isEmpty()) {
            return true;
        }
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, reserved)) {
            return false;
        }
        int room = input.isEmpty() ? reserved.getMaxStackSize() : input.getMaxStackSize() - input.getCount();
        if (room < reserved.getCount()) {
            return false;
        }
        if (input.isEmpty()) {
            inventory.setStackInSlot(INPUT_SLOT, reserved.copy());
        } else {
            input.grow(reserved.getCount());
        }
        inventory.setStackInSlot(RESERVED_SLOT, ItemStack.EMPTY);
        return true;
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    public double overclockFactor() {
        return 1.0D;
    }

    public ProcessingSeries series() {
        return ((ClayMachineBlock)getBlockState().getBlock()).series();
    }

    public int tier() {
        return ((ClayMachineBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ClayEnergyStorage energyStorage() {
        return energy;
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
        return faceModes.get(direction);
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        faceModes.put(direction, mode);
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        setChangedAndSync();
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(4);
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
        tag.putString("state", state.name());
        if (activeRecipe != null) {
            tag.putString("active_recipe", activeRecipe.toString());
        }
        if (!pendingOutput.isEmpty()) {
            tag.put("pending_output", pendingOutput.save(registries));
        }
        tag.putLong("progress", progress);
        tag.putLong("required_progress", requiredProgress);
        tag.putLong("energy_per_progress", energyPerProgress);
        tag.putDouble("progress_remainder", progressRemainder);
        tag.putDouble("progress_per_tick", progressPerTick);
        CompoundTag faces = new CompoundTag();
        faceModes.forEach((direction, mode) -> faces.putString(direction.getName(), mode.name()));
        tag.put("faces", faces);
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
        state = parseEnum(MachineState.class, tag.getString("state"), MachineState.IDLE);
        activeRecipe = tag.contains("active_recipe") ? ResourceLocation.tryParse(tag.getString("active_recipe")) : null;
        activeRecipeValue = null;
        pendingOutput = tag.contains("pending_output")
                ? ItemStack.parseOptional(registries, tag.getCompound("pending_output"))
                : ItemStack.EMPTY;
        progress = tag.getLong("progress");
        requiredProgress = tag.getLong("required_progress");
        energyPerProgress = tag.getLong("energy_per_progress");
        progressRemainder = tag.getDouble("progress_remainder");
        progressPerTick = tag.contains("progress_per_tick") ? tag.getDouble("progress_per_tick") : 1.0D;
        validateRecipeOnLoad = activeRecipe != null;
        CompoundTag faces = tag.getCompound("faces");
        for (Direction direction : Direction.values()) {
            if (faces.contains(direction.getName())) {
                faceModes.put(direction, parseEnum(FaceMode.class, faces.getString(direction.getName()), faceModes.get(direction)));
            }
        }
        if (activeRecipe != null && (pendingOutput.isEmpty() || inventory.getStackInSlot(RESERVED_SLOT).isEmpty())) {
            clearCorruptProcess();
        }
    }

    private void clearCorruptProcess() {
        activeRecipe = null;
        activeRecipeValue = null;
        pendingOutput = ItemStack.EMPTY;
        progress = 0L;
        requiredProgress = 0L;
        energyPerProgress = 0L;
        progressRemainder = 0.0D;
        progressPerTick = 1.0D;

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

    private void setState(MachineState newState) {
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
        DISABLED;

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
                case 8, 9, 10, 11 -> (int)(energy.stored().internalValue() >>> ((index - 8) * 16)) & 0xFFFF;
                case 12 -> state.ordinal();
                case 13 -> tier();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 14;
        }
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
                return 3;
            }
            return faceModes.get(direction) == FaceMode.DISABLED
                    ? 0
                    : faceModes.get(direction) == FaceMode.INPUT ? 2 : 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(mappedSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int mappedSlot = mappedSlot(slot);
            return (direction == null && mappedSlot != OUTPUT_SLOT)
                            || direction != null && faceModes.get(direction) == FaceMode.INPUT
                    ? inventory.insertItem(mappedSlot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return direction == null && mappedSlot(slot) == OUTPUT_SLOT
                            || direction != null && faceModes.get(direction) == FaceMode.OUTPUT
                    ? inventory.extractItem(OUTPUT_SLOT, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(mappedSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            int mappedSlot = mappedSlot(slot);
            return ((direction == null && mappedSlot != OUTPUT_SLOT)
                            || direction != null && faceModes.get(direction) == FaceMode.INPUT)
                    && inventory.isItemValid(mappedSlot, stack);
        }

        private int mappedSlot(int slot) {
            if (direction == null) {
                return switch (slot) {
                    case 0 -> INPUT_SLOT;
                    case 1 -> FUEL_SLOT;
                    default -> OUTPUT_SLOT;
                };
            }
            if (faceModes.get(direction) == FaceMode.OUTPUT) {
                return OUTPUT_SLOT;
            }
            return slot == 0 ? INPUT_SLOT : FUEL_SLOT;
        }
    }
}
