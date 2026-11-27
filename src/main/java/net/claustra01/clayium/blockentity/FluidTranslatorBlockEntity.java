package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.transfer.ClayiumFluidTransfer;
import net.claustra01.clayium.util.ClayDirection;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Fluid Translator (T4..T13): route 0 performs fluid tank <-> capsule interconversion and auto-compaction;
 * route 1 transfers capsule items directly. Exposes {@link IFluidHandler}.
 */
public class FluidTranslatorBlockEntity extends BufferBlockEntity {
    private final IFluidHandler fluidHandler = new CapsuleFluidHandler();

    public FluidTranslatorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.FLUID_TRANSLATOR.get(), pos, state);
    }

    @Override
    protected void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        // Route 1 mirrors full slot range for raw item transport
        if (!this.listSlotsInsert.isEmpty() && this.listSlotsInsert.size() == 1) {
            this.listSlotsInsert.add(this.slotsDrop);
        }
        if (!this.listSlotsExtract.isEmpty() && this.listSlotsExtract.size() == 1) {
            this.listSlotsExtract.add(this.slotsDrop);
        }
    }

    @Override
    public void doAutoExtract() {
        super.doAutoExtract();
        if (!this.listSlotsInsert.isEmpty()) {
            ClayiumFluidTransfer.sortFluid(this.items, this.listSlotsInsert.getFirst());
        }
    }

    @Override
    protected void doAutoExtractFromSide(RelativeSide relSide, Direction front) {
        int route = this.insertRoutes[relSide.id()];
        if (route == 0 && !this.listSlotsInsert.isEmpty()) {
            int max = this.maxAutoExtract != null && route < this.maxAutoExtract.length && this.maxAutoExtract[route] >= 0
                    ? this.maxAutoExtract[route]
                    : this.maxAutoExtractDefault;
            ClayiumFluidTransfer.extractFromTank(this, this.listSlotsInsert.getFirst(), ClayDirection.sideToDirection(front, relSide), max);
        } else {
            super.doAutoExtractFromSide(relSide, front);
        }
    }

    @Override
    protected void doAutoInsertToSide(RelativeSide relSide, Direction front) {
        int route = this.extractRoutes[relSide.id()];
        if (route == 0 && !this.listSlotsExtract.isEmpty()) {
            int max = this.maxAutoInsert != null && route < this.maxAutoInsert.length && this.maxAutoInsert[route] >= 0
                    ? this.maxAutoInsert[route]
                    : this.maxAutoInsertDefault;
            ClayiumFluidTransfer.insertToTank(this, this.listSlotsExtract.getFirst(), ClayDirection.sideToDirection(front, relSide), max);
        } else {
            super.doAutoInsertToSide(relSide, front);
        }
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return this.fluidHandler;
    }

    private final class CapsuleFluidHandler implements IFluidHandler {
        private int[] slots() {
            return FluidTranslatorBlockEntity.this.listSlotsInsert.isEmpty()
                    ? new int[0]
                    : FluidTranslatorBlockEntity.this.listSlotsInsert.getFirst();
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return ClayiumFluidTransfer.getTankFluid(FluidTranslatorBlockEntity.this.items, slots());
        }

        @Override
        public int getTankCapacity(int tank) {
            return ClayiumFluidTransfer.getTankCapacity(FluidTranslatorBlockEntity.this.items, slots());
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = ClayiumFluidTransfer.fillFluid(FluidTranslatorBlockEntity.this.items, slots(), resource, action);
            if (action.execute() && filled > 0) {
                markForSync();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained = ClayiumFluidTransfer.drainFluid(FluidTranslatorBlockEntity.this.items, slots(), resource, action);
            if (action.execute() && !drained.isEmpty()) {
                markForSync();
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained = ClayiumFluidTransfer.drainFluid(FluidTranslatorBlockEntity.this.items, slots(), maxDrain, action);
            if (action.execute() && !drained.isEmpty()) {
                markForSync();
            }
            return drained;
        }
    }
}
