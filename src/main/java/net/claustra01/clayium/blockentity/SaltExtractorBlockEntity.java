package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Salt Extractor (T4..T7): consumes CE proportional to efficiency and extracts Salt Dust from adjacent water blocks.
 */
public class SaltExtractorBlockEntity extends CobblestoneGeneratorBlockEntity {
    public static final int ENERGY_PER_WORK = 30;

    public SaltExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.SALT_EXTRACTOR.get(), pos, state);
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        int storageSize = this.inventoryX * this.inventoryY;
        this.clayEnergySlot = storageSize;
        this.items = NonNullList.withSize(storageSize + 1, ItemStack.EMPTY);
        this.listSlotsInsert.clear();
        this.listSlotsInsert.add(new int[]{this.clayEnergySlot});
        int[] drops = new int[storageSize + 1];
        for (int i = 0; i < storageSize; i++) {
            drops[i] = i;
        }
        drops[storageSize] = this.clayEnergySlot;
        this.slotsDrop = drops;
    }

    @Override
    public int getInventorySize() {
        return this.clayEnergySlot >= 0 ? this.clayEnergySlot + 1 : super.getInventorySize();
    }

    @Override
    public boolean acceptEnergyClay() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == this.clayEnergySlot && isEnergySlotValid(stack);
    }

    @Override
    public boolean canInsertToSide(int slot, ItemStack stack, RelativeSide relSide) {
        return slot == this.clayEnergySlot && super.canInsertToSide(slot, stack, relSide);
    }

    @Override
    public void produce() {
        int count = countWater();
        this.doingWork = false;
        if (this.externalControlState >= 0
                && count > 0
                && consumeClayEnergy((long) this.progressEfficiency * ENERGY_PER_WORK)) {
            this.progress += this.progressEfficiency * count;
            this.doingWork = true;
            while (this.progress >= PROGRESS_MAX) {
                markForSync();
                ItemStack salt = new ItemStack(ClayiumItems.byPath("salt_dust").get());
                this.progress -= PROGRESS_MAX;
                ClayiumTransfer.produceItemStack(salt, this.items, 0, this.inventoryX * this.inventoryY, getInventoryStackLimit(0));
                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
            }
        }
    }

    @Override
    public boolean isScheduledWork() {
        return countWater() > 0;
    }

    public int countWater() {
        if (this.level == null) {
            return 0;
        }
        int count = 0;
        for (Direction side : Direction.values()) {
            if (this.level.getFluidState(this.worldPosition.relative(side)).is(FluidTags.WATER)) {
                count++;
            }
        }
        return count;
    }
}
