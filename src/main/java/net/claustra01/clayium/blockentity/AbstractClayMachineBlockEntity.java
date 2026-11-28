package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.energy.ClayEnergyUpgradeMath.AdjacentUpgradeSummary;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared operational foundation for Step 09/11 Clayium machines:
 * handles adjacent Overclocker / Energy Storage Upgrade modifiers, fractional overclock ticks,
 * {@link IExternalControl} redstone interface states, and active {@link BlockState} synchronization.
 */
public abstract class AbstractClayMachineBlockEntity extends TieredClayContainerBlockEntity implements IExternalControl {
    public long machineCraftTime = 0L;
    public long machineTimeToCraft = 0L;
    public long machineConsumingEnergy = 0L;

    public float initCraftTime = 1.0F;
    public float initConsumingEnergy = 1.0F;
    public float multCraftTime = 1.0F;
    public float multConsumingEnergy = 1.0F;

    public int externalControlState = 0;
    public boolean doingWork = false;
    public double overclockTick = 0.0;
    public double overclockFactor = 1.0;
    public double overclockTotalFactor = 1.0;

    protected AbstractClayMachineBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public boolean canOverclock() {
        return true;
    }

    public String getRecipeCategoryId() {
        return "";
    }

    @Override
    public boolean acceptEnergyClay() {
        return UtilTier.acceptEnergyClay(getTier());
    }

    public int canPushButton() {
        return canProceedCraft() ? 1 : 0;
    }

    public void pushButton() {
        if (canPushButton() == 1) {
            this.clayEnergyStorage.addEnergy(5L);
        }
        markForSync();
    }

    protected boolean shouldDisplayActiveOverlay() {
        return this.doingWork;
    }

    protected boolean canFitOutputStack(int slot, net.minecraft.world.item.ItemStack result) {
        if (result.isEmpty()) {
            return true;
        }
        net.minecraft.world.item.ItemStack existing = getStack(slot);
        if (existing.isEmpty()) {
            return true;
        }
        if (!net.minecraft.world.item.ItemStack.isSameItemSameComponents(existing, result)) {
            return false;
        }
        int total = existing.getCount() + result.getCount();
        return total <= getInventoryStackLimit(slot) && total <= existing.getMaxStackSize();
    }

    protected void addOutputStack(int slot, net.minecraft.world.item.ItemStack result) {
        if (result.isEmpty()) {
            return;
        }
        net.minecraft.world.item.ItemStack existing = getStack(slot);
        if (existing.isEmpty()) {
            setStack(slot, result.copy());
        } else if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(existing, result)) {
            existing.grow(result.getCount());
            markForSync();
        }
    }

    public boolean isCrafting() {
        return this.machineCraftTime > 0L;
    }

    @Override
    public boolean isDoingWork() {
        return this.doingWork;
    }

    @Override
    public boolean isScheduledWork() {
        return isDoingWork() || (this.externalControlState >= 0 && canProceedCraft());
    }

    @Override
    public void startWork() {
        this.externalControlState = 1;
        markForSync();
    }

    @Override
    public void stopWork() {
        this.externalControlState = -1;
        markForSync();
    }

    public abstract boolean canProceedCraft();

    public abstract void proceedCraft();

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.doingWork = false;
        this.clayEnergyStorage.setEnergySlotLimit(1);
        this.overclockTotalFactor = 1.0;

        if (canOverclock()) {
            AdjacentUpgradeSummary upgrades = ClayEnergyUpgradeMath.scanAdjacentUpgrades(this.level, this.worldPosition);
            this.overclockTotalFactor = upgrades.overclockTotalFactor();
            this.clayEnergyStorage.setEnergySlotLimit(upgrades.energySlotLimit());
        } else {
            AdjacentUpgradeSummary upgrades = ClayEnergyUpgradeMath.scanAdjacentUpgrades(this.level, this.worldPosition);
            this.clayEnergyStorage.setEnergySlotLimit(upgrades.energySlotLimit());
        }

        this.overclockFactor = ClayEnergyUpgradeMath.effectiveOverclockFactor(this.overclockTotalFactor);
        this.overclockTick += ClayEnergyUpgradeMath.tickIncrement(this.overclockTotalFactor, this.overclockFactor);
        this.multCraftTime = ClayEnergyUpgradeMath.craftTimeMultiplier(this.initCraftTime, this.overclockFactor);
        this.multConsumingEnergy = ClayEnergyUpgradeMath.energyConsumptionMultiplier(this.initConsumingEnergy, this.overclockFactor);

        while (this.overclockTick >= 1.0) {
            this.overclockTick -= 1.0;
            if (this.externalControlState >= 0 && canProceedCraft()) {
                proceedCraft();
            }
        }

        syncActiveBlockState();
    }

    protected void syncActiveBlockState() {
        if (this.level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(ClayContainerBlock.ACTIVE)) {
            boolean shouldBeActive = shouldDisplayActiveOverlay();
            if (state.getValue(ClayContainerBlock.ACTIVE) != shouldBeActive) {
                this.level.setBlock(this.worldPosition, state.setValue(ClayContainerBlock.ACTIVE, shouldBeActive), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("CraftTime", this.machineCraftTime);
        tag.putLong("TimeToCraft", this.machineTimeToCraft);
        tag.putLong("ConsumingEnergy", this.machineConsumingEnergy);
        tag.putFloat("CraftTimeMultiplier", this.multCraftTime);
        tag.putFloat("ConsumingEnergyMultiplier", this.multConsumingEnergy);
        tag.putFloat("CraftTimeInitMultiplier", this.initCraftTime);
        tag.putFloat("ConsumingEnergyInitMultiplier", this.initConsumingEnergy);
        tag.putInt("ExternalControlState", this.externalControlState);
        tag.putDouble("OverclockTick", this.overclockTick);
        tag.putDouble("OverclockFactor", this.overclockFactor);
        tag.putDouble("OverclockTotalFactor", this.overclockTotalFactor);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.machineCraftTime = tag.getLong("CraftTime");
        this.machineTimeToCraft = tag.getLong("TimeToCraft");
        this.machineConsumingEnergy = tag.getLong("ConsumingEnergy");
        if (tag.contains("CraftTimeMultiplier")) {
            this.multCraftTime = tag.getFloat("CraftTimeMultiplier");
        }
        if (tag.contains("ConsumingEnergyMultiplier")) {
            this.multConsumingEnergy = tag.getFloat("ConsumingEnergyMultiplier");
        }
        if (tag.contains("CraftTimeInitMultiplier")) {
            this.initCraftTime = tag.getFloat("CraftTimeInitMultiplier");
        }
        if (tag.contains("ConsumingEnergyInitMultiplier")) {
            this.initConsumingEnergy = tag.getFloat("ConsumingEnergyInitMultiplier");
        }
        this.externalControlState = tag.getInt("ExternalControlState");
        this.overclockTick = tag.getDouble("OverclockTick");
        if (tag.contains("OverclockFactor")) {
            this.overclockFactor = tag.getDouble("OverclockFactor");
        }
        if (tag.contains("OverclockTotalFactor")) {
            this.overclockTotalFactor = tag.getDouble("OverclockTotalFactor");
        }
    }
}
