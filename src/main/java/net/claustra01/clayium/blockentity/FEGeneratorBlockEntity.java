package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.energy.FEGeneratorSpecs;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * CE-FE Converter BlockEntity replacing the 1.7.10 RF generator ({@code TileClayRFGenerator}) with
 * NeoForge's directional {@link IEnergyStorage} capability while preserving CE->FE rates, buffering,
 * output caps, overclocking, and redstone emission pause.
 */
public class FEGeneratorBlockEntity extends TieredClayContainerBlockEntity {
    @Nullable
    protected String configBlockName;
    public int feStored = 0;
    public int maxFeStored = 10_000;
    public long ceConsumptionPerTick = 100L;
    public int feProductionPerTick = 10;
    public int feOutputPerTick = 10;
    public long ceConsumptionPerTickBase = 100L;
    public int feProductionPerTickBase = 10;
    public int feOutputPerTickBase = 10;
    public double overclockExponent = 1.0;
    public double overclockTotalFactor = 1.0;
    private boolean powered = false;
    private boolean initializedSpec = false;

    public FEGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.FE_GENERATOR.get(), pos, state, 1);
        ensureTierInitialized();
        ensureSpecInitialized();
    }

    @Override
    protected void initParams() {
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.autoInsert = false;
        this.autoExtract = true;
        this.maxAutoInsertDefault = 1;
        this.maxAutoExtractDefault = 1;
        this.autoInsertInterval = 8;
        this.autoExtractInterval = 8;
        this.listSlotsInsert.clear();
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsExtract.clear();
        this.listSlotsExtract.add(new int[0]);
        this.slotsDrop = new int[]{0};
        this.clayEnergySlot = 0;
    }

    public void ensureSpecInitialized() {
        if (this.initializedSpec) {
            return;
        }
        FEGeneratorSpecs.Spec spec = FEGeneratorSpecs.forBlock(getBlockState().getBlock());
        if (spec == null && this.configBlockName != null) {
            spec = FEGeneratorSpecs.byConfigName(this.configBlockName);
        }
        if (spec != null) {
            applySpec(spec);
        }
    }

    public void initFromConfigName(@Nullable String name) {
        this.configBlockName = name;
        if (!this.initializedSpec && name != null && !name.isEmpty()) {
            FEGeneratorSpecs.Spec spec = FEGeneratorSpecs.byConfigName(name);
            if (spec != null) {
                applySpec(spec);
            }
        }
    }

    public void applySpec(FEGeneratorSpecs.Spec spec) {
        this.configBlockName = spec.configBlockName();
        this.ceConsumptionPerTickBase = Math.max(0L, spec.ceConsumptionPerTick());
        this.feProductionPerTickBase = Math.max(0, spec.feProductionPerTick());
        this.feOutputPerTickBase = Math.max(0, spec.feOutputPerTick());
        this.maxFeStored = Math.max(0, spec.feStorageSize());
        this.overclockExponent = Double.isFinite(spec.overclockExponent()) ? spec.overclockExponent() : 1.0;
        this.initializedSpec = true;
        recalculateRates(this.overclockTotalFactor, this.clayEnergyStorage.getEnergySlotLimit());
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markForInstantSync();
            if (this.level != null && !this.level.isClientSide) {
                this.level.invalidateCapabilities(this.worldPosition);
            }
        }
    }

    public boolean canConnectEnergy(@Nullable Direction side) {
        if (side == null) {
            return false;
        }
        int routeIdx = toRelativeSide(side).id();
        return this.extractRoutes[routeIdx] == 0;
    }

    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return canConnectEnergy(side) ? new SidedEnergyStorage(side) : null;
    }

    public int extractEnergyFromSide(@Nullable Direction side, int maxExtract, boolean simulate) {
        if (!canConnectEnergy(side) || isPowered() || maxExtract <= 0 || this.feStored <= 0 || this.feOutputPerTick <= 0) {
            return 0;
        }
        int extracted = Math.min(this.feStored, Math.min(this.feOutputPerTick, maxExtract));
        if (!simulate && extracted > 0) {
            this.feStored -= extracted;
            markForSync();
        }
        return extracted;
    }

    public void recalculateRates(double ocTotalFactor, int energySlotLimit) {
        this.overclockTotalFactor = (!Double.isFinite(ocTotalFactor) || ocTotalFactor < 0.0) ? 1.0 : ocTotalFactor;
        this.clayEnergyStorage.setEnergySlotLimit(energySlotLimit);
        double expFactor = Math.pow(this.overclockTotalFactor, this.overclockExponent);
        if (Double.isNaN(expFactor) || expFactor < 0.0) {
            expFactor = 0.0;
        }
        this.ceConsumptionPerTick = clampDoubleToLong((double) this.ceConsumptionPerTickBase * expFactor);
        this.feProductionPerTick = clampDoubleToInt((double) this.feProductionPerTickBase * expFactor);
        this.feOutputPerTick = clampDoubleToInt((double) this.feOutputPerTickBase * expFactor);
    }

    private static long clampDoubleToLong(double value) {
        if (Double.isNaN(value) || value <= 0.0) {
            return 0L;
        }
        if (Double.isInfinite(value) || value >= (double) Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return (long) value;
    }

    private static int clampDoubleToInt(double value) {
        if (Double.isNaN(value) || value <= 0.0) {
            return 0;
        }
        if (Double.isInfinite(value) || value >= (double) Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    /**
     * Converts stored Clay Energy into internal FE buffer up to {@link #maxFeStored}.
     * Redstone signals pause emission, not internal buffer charging, matching 1.7.10 {@code TileClayRFGenerator}.
     */
    public boolean tickGeneration() {
        if (this.feProductionPerTick > 0
                && this.feStored < this.maxFeStored
                && consumeClayEnergy(this.ceConsumptionPerTick)) {
            long sum = (long) this.feStored + (long) this.feProductionPerTick;
            this.feStored = sum >= (long) Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
            markForSync();
            return true;
        }
        return false;
    }

    public int pushEnergyToReceiver(Direction side, @Nullable IEnergyStorage receiver) {
        if (receiver == null || !receiver.canReceive()) {
            return 0;
        }
        int available = extractEnergyFromSide(side, this.feOutputPerTick, true);
        if (available <= 0) {
            return 0;
        }
        int accepted = receiver.receiveEnergy(available, false);
        int clamped = Math.max(0, Math.min(available, accepted));
        if (clamped > 0) {
            return extractEnergyFromSide(side, clamped, false);
        }
        return 0;
    }

    public void pushEnergyToNeighbors() {
        if (this.level == null || this.level.isClientSide || isPowered() || this.feStored <= 0 || this.feOutputPerTick <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (canConnectEnergy(dir)) {
                IEnergyStorage receiver = this.level.getCapability(
                        Capabilities.EnergyStorage.BLOCK,
                        this.worldPosition.relative(dir),
                        dir.getOpposite()
                );
                pushEnergyToReceiver(dir, receiver);
            }
        }
    }

    @Override
    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        ensureSpecInitialized();
        ClayEnergyUpgradeMath.AdjacentUpgradeSummary upgrades =
                ClayEnergyUpgradeMath.scanAdjacentUpgrades(this.level, this.worldPosition);
        recalculateRates(upgrades.overclockTotalFactor(), upgrades.energySlotLimit());
        tickGeneration();
        pushEnergyToNeighbors();
        super.serverTick();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.configBlockName != null && !this.configBlockName.isEmpty()) {
            tag.putString("BlockName", this.configBlockName);
        }
        tag.putBoolean("Powered", this.powered);
        tag.putInt("FEStored", Math.max(0, this.feStored));
        tag.putInt("RFStored", Math.max(0, this.feStored));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BlockName") && this.configBlockName == null) {
            initFromConfigName(tag.getString("BlockName"));
        }
        this.powered = tag.getBoolean("Powered");
        int loadedFe = tag.contains("FEStored") ? tag.getInt("FEStored") : tag.getInt("RFStored");
        this.feStored = Math.max(0, loadedFe);
    }

    private final class SidedEnergyStorage implements IEnergyStorage {
        private final Direction side;

        SidedEnergyStorage(Direction side) {
            this.side = side;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return extractEnergyFromSide(this.side, maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return Math.max(0, FEGeneratorBlockEntity.this.feStored);
        }

        @Override
        public int getMaxEnergyStored() {
            return Math.max(0, FEGeneratorBlockEntity.this.maxFeStored);
        }

        @Override
        public boolean canExtract() {
            return canConnectEnergy(this.side) && !isPowered();
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
