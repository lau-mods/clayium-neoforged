package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.claustra01.clayium.util.ProgressionRate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Tiered Cobblestone Generator (T1..T7): generates cobblestone when horizontally adjacent to both water and lava.
 */
public class CobblestoneGeneratorBlockEntity extends BufferBlockEntity implements IExternalControl {
    public static final int PROGRESS_MAX = 100;

    public int progress;
    public int progressEfficiency;
    public int externalControlState;
    public boolean doingWork;

    public CobblestoneGeneratorBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.COBBLESTONE_GENERATOR.get(), pos, state);
    }

    protected CobblestoneGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.progress = 0;
        this.progressEfficiency = 5;
        this.externalControlState = 0;
        this.doingWork = false;
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.autoInsert = true;
        this.autoExtract = false;
    }

    @Override
    protected void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        this.listSlotsInsert.clear();
        this.progressEfficiency = ProgressionRate.multiplyI(switch (tier) {
            case 1 -> 2;
            case 2 -> 5;
            case 3 -> 15;
            case 4 -> 50;
            case 5 -> 200;
            case 6 -> 1000;
            case 7 -> 8000;
            default -> 5;
        });
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canInsertToSide(int slot, ItemStack stack, RelativeSide relSide) {
        return false;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level != null && !this.level.isClientSide) {
            produce();
        }
    }

    public void produce() {
        this.doingWork = false;
        if (canProduce()) {
            if (this.externalControlState >= 0) {
                this.progress += this.progressEfficiency;
            }
            while (this.progress >= PROGRESS_MAX) {
                markForSync();
                ItemStack cobble = new ItemStack(Items.COBBLESTONE);
                this.progress -= PROGRESS_MAX;
                ClayiumTransfer.produceItemStack(cobble, this.items, 0, this.inventoryX * this.inventoryY, getInventoryStackLimit(0));
                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
                this.doingWork = true;
            }
        }
    }

    public boolean canProduce() {
        if (this.level == null) {
            return false;
        }
        boolean hasWater = false;
        boolean hasLava = false;
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction side : sides) {
            FluidState fluid = this.level.getFluidState(this.worldPosition.relative(side));
            if (fluid.is(FluidTags.WATER)) {
                hasWater = true;
            }
            if (fluid.is(FluidTags.LAVA)) {
                hasLava = true;
            }
        }
        return hasWater && hasLava;
    }

    public void doWorkOnce() {
        if (this.externalControlState > 0) {
            this.externalControlState++;
        } else {
            this.externalControlState = 1;
        }
        markForSync();
    }

    @Override
    public void startWork() {
        this.externalControlState = 0;
        markForSync();
    }

    @Override
    public void stopWork() {
        this.externalControlState = -1;
        markForSync();
    }

    @Override
    public boolean isScheduledWork() {
        return canProduce();
    }

    @Override
    public boolean isDoingWork() {
        return this.doingWork;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", this.progress);
        tag.putInt("ProgressEfficiency", this.progressEfficiency);
        tag.putInt("ExternalControlState", this.externalControlState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("Progress");
        if (tag.contains("ProgressEfficiency")) {
            this.progressEfficiency = tag.getInt("ProgressEfficiency");
        }
        this.externalControlState = tag.getInt("ExternalControlState");
    }
}
