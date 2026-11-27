package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.QuartzCrucibleBlock;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Quartz Crucible (Tier 5): melts up to 9 Impure Silicon Ingots over {@code 600 * count} ticks
 * and yields purified Silicon Ingots when touched by String.
 */
public class QuartzCrucibleBlockEntity extends BlockEntity {
    public static final int TIME_PER_INGOT = 600;
    public static final int MAX_CAPACITY = 9;

    public int craftTime = 0;
    public int ingotQuantity = 0;

    public QuartzCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.QUARTZ_CRUCIBLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuartzCrucibleBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        if (be.ingotQuantity > 0) {
            be.craftTime++;
            be.setChanged();
        }
    }

    public boolean putIngot() {
        if ((this.level != null && this.level.isClientSide) || this.ingotQuantity >= MAX_CAPACITY) {
            return false;
        }
        this.ingotQuantity++;
        syncLevelProperty();
        setChanged();
        return true;
    }

    public boolean consumeString() {
        if (this.level == null
                || this.level.isClientSide
                || this.ingotQuantity <= 0
                || this.craftTime < this.ingotQuantity * TIME_PER_INGOT) {
            return false;
        }
        ItemStack result = new ItemStack(ClayiumItems.byPath("silicon_ingot").get(), this.ingotQuantity);
        ItemEntity drop = new ItemEntity(
                this.level,
                this.worldPosition.getX() + 0.4,
                this.worldPosition.getY() + 0.4,
                this.worldPosition.getZ() + 0.4,
                result
        );
        this.level.addFreshEntity(drop);
        this.ingotQuantity = 0;
        this.craftTime = 0;
        syncLevelProperty();
        setChanged();
        return true;
    }

    private void syncLevelProperty() {
        if (this.level == null) {
            return;
        }
        BlockState state = getBlockState();
        int clamped = Math.max(0, Math.min(MAX_CAPACITY, this.ingotQuantity));
        if (state.hasProperty(QuartzCrucibleBlock.LEVEL) && state.getValue(QuartzCrucibleBlock.LEVEL) != clamped) {
            this.level.setBlock(this.worldPosition, state.setValue(QuartzCrucibleBlock.LEVEL, clamped), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("CraftTime", (short) this.craftTime);
        tag.putShort("IngotQuantity", (short) this.ingotQuantity);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.craftTime = tag.getShort("CraftTime");
        this.ingotQuantity = tag.getShort("IngotQuantity");
    }
}
