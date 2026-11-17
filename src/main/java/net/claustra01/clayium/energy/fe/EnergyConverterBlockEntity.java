package net.claustra01.clayium.energy.fe;

import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
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

public final class EnergyConverterBlockEntity extends BlockEntity {
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
    };
    private final IEnergyStorage energyStorage = new OutputEnergyStorage();
    private EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighborCaches;
    private int storedFe;
    private int outputThisTick;
    private long outputGameTime = Long.MIN_VALUE;
    private double overclockFactor = 1.0D;

    public EnergyConverterBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyConverterRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, EnergyConverterBlockEntity converter) {
        converter.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
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
        return fuel;
    }

    public IEnergyStorage energyStorage(@Nullable Direction direction) {
        return energyStorage;
    }

    public int storedFe() {
        return storedFe;
    }

    public double overclockFactor() {
        return overclockFactor;
    }

    public void setOverclockFactor(double factor) {
        overclockFactor = Double.isFinite(factor) ? Math.max(1.0D, factor) : 1.0D;
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
    }

    private final class OutputEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
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
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
