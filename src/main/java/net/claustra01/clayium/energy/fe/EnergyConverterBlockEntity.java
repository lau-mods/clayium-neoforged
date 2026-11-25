package net.claustra01.clayium.energy.fe;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.machine.augment.AugmentScanner;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.IoTransportKind;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class EnergyConverterBlockEntity extends BlockEntity implements MenuProvider, ConfigurableIoDevice {
    private final ClayEnergyStorage clayEnergy = new ClayEnergyStorage();
    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
        }

        @Override
        public int getSlotLimit(int slot) {
            return fuelLimit();
        }
    };
    private final int[] insertionRoutes = {-1, 0, -1, 1, -1, -1};
    private final int[] extractionRoutes = {0, -1, -1, -1, -1, -1};
    private final ItemStack[] sideFilters = new ItemStack[6];
    private final SideConfiguration sideConfiguration = new SideConfiguration(
            insertionRoutes, extractionRoutes, sideFilters, () -> 2, () -> 1, ItemFilter::accepts);

    @Override
    public IoTransportKind extractionTransportKind(int route) {
        return IoTransportKind.ENERGY;
    }
    private final IItemHandler unsidedFuelHandler = new FuelHandler(null);
    private final EnumMap<Direction, IItemHandler> fuelHandlers = new EnumMap<>(Direction.class);
    private final IEnergyStorage unsidedEnergyStorage = new OutputEnergyStorage(null);
    private final EnumMap<Direction, IEnergyStorage> energyStorages = new EnumMap<>(Direction.class);
    private EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighborCaches;
    private int storedFe;
    private int outputThisTick;
    private long outputGameTime = Long.MIN_VALUE;
    private double overclockFactor = 1.0D;
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            long ce = clayEnergy.stored().internalValue();
            return switch (index) {
                case 0, 1, 2, 3 -> (int)(ce >>> (index * 16) & 0xFFFFL);
                case 4 -> storedFe & 0xFFFF;
                case 5 -> storedFe >>> 16;
                case 6 -> specification().generatedFe(overclockFactor) & 0xFFFF;
                case 7 -> specification().generatedFe(overclockFactor) >>> 16;
                case 8 -> specification().outputLimit(overclockFactor) & 0xFFFF;
                case 9 -> specification().outputLimit(overclockFactor) >>> 16;
                case 10 -> specification().capacity() & 0xFFFF;
                case 11 -> specification().capacity() >>> 16;
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
    };

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EnergyConverterMenu(id, inventory, this, menuData);
    }

    public EnergyConverterBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyConverterRegistration.BLOCK_ENTITY.get(), pos, state);
        java.util.Arrays.fill(sideFilters, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) {
            fuelHandlers.put(direction, new FuelHandler(direction));
            energyStorages.put(direction, new OutputEnergyStorage(direction));
        }
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, EnergyConverterBlockEntity converter) {
        converter.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        overclockFactor = AugmentScanner.overclockFactor(serverLevel, worldPosition);
        enforceFuelLimit();
        beginOutputTick(serverLevel.getGameTime());
        if (serverLevel.hasNeighborSignal(worldPosition)) {
            return;
        }
        EnergyConverterSpecification specification = specification();
        int generation = specification.generatedFe(overclockFactor);
        if (generation <= specification.capacity() - storedFe
                && clayEnergy.consume(specification.clayEnergyCost(overclockFactor), fuel.getStackInSlot(0))) {
            storedFe += generation;
            setChanged();
        }
        ensureCaches(serverLevel);
        pushEnergy();
    }

    private void ensureCaches(ServerLevel serverLevel) {
        if (neighborCaches != null) {
            return;
        }
        neighborCaches = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            neighborCaches.put(direction, BlockCapabilityCache.create(
                    Capabilities.EnergyStorage.BLOCK,
                    serverLevel,
                    worldPosition.relative(direction),
                    direction.getOpposite(),
                    () -> !isRemoved(),
                    () -> {}));
        }
    }

    private void pushEnergy() {
        for (Direction direction : Direction.values()) {
            if (extractionRoute(direction) < 0) continue;
            int offered = availableOutput();
            if (offered <= 0) {
                return;
            }
            IEnergyStorage target = neighborCaches.get(direction).getCapability();
            if (target == null || !target.canReceive()) {
                continue;
            }
            int accepted = Math.max(0, Math.min(offered, target.receiveEnergy(offered, false)));
            if (accepted > 0) {
                storedFe -= accepted;
                outputThisTick += accepted;
                setChanged();
            }
        }
    }

    private int availableOutput() {
        refreshOutputTick();
        if (level != null && level.hasNeighborSignal(worldPosition)) {
            return 0;
        }
        return Math.max(0, Math.min(storedFe, specification().outputLimit(overclockFactor) - outputThisTick));
    }

    private void refreshOutputTick() {
        if (level != null) {
            beginOutputTick(level.getGameTime());
        }
    }

    private void beginOutputTick(long gameTime) {
        if (outputGameTime != gameTime) {
            outputGameTime = gameTime;
            outputThisTick = 0;
        }
    }

    public EnergyConverterSpecification specification() {
        return ((EnergyConverterBlock)getBlockState().getBlock()).specification();
    }

    public ClayEnergyStorage clayEnergyStorage() {
        return clayEnergy;
    }

    public IItemHandler fuelHandler(@Nullable Direction direction) {
        return direction == null ? unsidedFuelHandler : fuelHandlers.get(direction);
    }

    public IEnergyStorage energyStorage(@Nullable Direction direction) {
        return direction == null ? unsidedEnergyStorage : energyStorages.get(direction);
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return EnergyConverterBlock.PIPE;
    }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() {
        return EnergyConverterBlock.FACING;
    }
    @Override public void ioConfigurationChanged() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            if (!level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        ioNeighborsChanged();
    }

    public int storedFe() {
        return storedFe;
    }

    public double overclockFactor() {
        return overclockFactor;
    }

    public int fuelLimit() {
        return level == null ? 1 : AugmentScanner.fuelLimit(level, worldPosition);
    }

    private void enforceFuelLimit() {
        ItemStack stack = fuel.getStackInSlot(0);
        int excessCount = stack.getCount() - fuelLimit();
        if (excessCount <= 0 || level == null) {
            return;
        }
        ItemStack excess = fuel.extractItem(0, excessCount, false);
        Containers.dropItemStack(
                level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                excess);
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(1);
        dropped.setItem(0, fuel.getStackInSlot(0).copy());
        fuel.setStackInSlot(0, ItemStack.EMPTY);
        return dropped;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", fuel.serializeNBT(registries));
        tag.put("energy", clayEnergy.serializeNBT(registries));
        tag.putInt("fe_energy", storedFe);
        saveIoState(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            fuel.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            clayEnergy.deserializeNBT(registries, tag.getCompound("energy"));
        }
        storedFe = Math.max(0, Math.min(specification().capacity(), tag.getInt("fe_energy")));
        loadIoState(tag, registries);
    }

    private final class OutputEnergyStorage implements IEnergyStorage {
        @Nullable private final Direction direction;

        private OutputEnergyStorage(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (direction != null && extractionRoute(direction) < 0) return 0;
            int extracted = Math.min(Math.max(0, maxExtract), availableOutput());
            if (!simulate && extracted > 0) {
                storedFe -= extracted;
                outputThisTick += extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return storedFe;
        }

        @Override
        public int getMaxEnergyStored() {
            return specification().capacity();
        }

        @Override
        public boolean canExtract() {
            return direction == null || extractionRoute(direction) >= 0;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }

    private final class FuelHandler implements IItemHandler {
        @Nullable private final Direction direction;

        private FuelHandler(@Nullable Direction direction) { this.direction = direction; }
        private boolean enabled() { return direction == null || insertionRoute(direction) == 1; }
        @Override public int getSlots() { return enabled() ? 1 : 0; }
        @Override public ItemStack getStackInSlot(int slot) { return fuel.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return enabled() && (direction == null || matchesFilter(direction, stack))
                    ? fuel.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return fuel.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return enabled() && (direction == null || matchesFilter(direction, stack)) && fuel.isItemValid(slot, stack);
        }
    }
}
