package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.ITieredBlock;
import net.claustra01.clayium.util.ClayDirection;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Abstract multiblock controller extending 2-in / 2-out {@link ChemicalReactorBlockEntity}
 * with periodic relative-coordinate structure verification and active-state overlay display.
 */
public abstract class AbstractMultiblockMachineBlockEntity extends ChemicalReactorBlockEntity {
    public boolean constructed = false;
    private int constructionCounter = 0;
    protected int constructionTime = 20;

    protected AbstractMultiblockMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean checkStructureConstructed();

    protected void onConstruction() {
        markForSync();
    }

    protected void onDestruction() {
        markForSync();
    }

    @Override
    protected boolean shouldDisplayActiveOverlay() {
        return this.constructed;
    }

    public boolean canProceedCraftWhenConstructed() {
        return super.canProceedCraft();
    }

    @Override
    public boolean canProceedCraft() {
        return this.constructed && canProceedCraftWhenConstructed();
    }

    @Override
    public void serverTick() {
        if (this.level != null && !this.level.isClientSide) {
            this.constructionCounter++;
            if (this.constructionCounter >= this.constructionTime) {
                verifyConstruction();
                this.constructionCounter = 0;
            }
        }
        super.serverTick();
    }

    public void forceVerification() {
        verifyConstruction();
        this.constructionCounter = 0;
    }

    public void verifyConstruction() {
        if (this.level == null) {
            return;
        }
        boolean newConstructed = checkStructureConstructed();
        if (newConstructed && !this.constructed) {
            onConstruction();
        }
        if (!newConstructed && this.constructed) {
            onDestruction();
        }
        this.constructed = newConstructed;
        syncActiveBlockState();
    }

    public BlockPos getRelativePos(int xx, int yy, int zz) {
        Direction front = getFrontDirection();
        Direction xxVec = ClayDirection.sideToDirection(front, RelativeSide.RIGHT);
        Direction zzVec = front.getOpposite();
        return this.worldPosition
                .relative(xxVec, xx)
                .relative(Direction.UP, yy)
                .relative(zzVec, zz);
    }

    protected BlockEntity getRelativeBlockEntity(int xx, int yy, int zz) {
        return this.level == null ? null : this.level.getBlockEntity(getRelativePos(xx, yy, zz));
    }

    protected BlockState getRelativeBlockState(int xx, int yy, int zz) {
        return this.level == null ? null : this.level.getBlockState(getRelativePos(xx, yy, zz));
    }

    protected Block getRelativeBlock(int xx, int yy, int zz) {
        BlockState state = getRelativeBlockState(xx, yy, zz);
        return state == null ? null : state.getBlock();
    }

    protected int getRelativeBlockTier(int xx, int yy, int zz) {
        Block block = getRelativeBlock(xx, yy, zz);
        return block instanceof ITieredBlock tiered ? tiered.getTier() : -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Constructed", this.constructed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.constructed = tag.getBoolean("Constructed");
    }
}
