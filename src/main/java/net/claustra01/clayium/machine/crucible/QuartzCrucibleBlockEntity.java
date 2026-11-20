package net.claustra01.clayium.machine.crucible;

import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class QuartzCrucibleBlockEntity extends BlockEntity {
    public static final int CAPACITY = 8;
    public static final int HEATING_TICKS_PER_INGOT = 600;

    private int impureSilicon;
    private int heatingTicks;

    public QuartzCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuartzCrucibleBlockEntity crucible) {
        crucible.tickServer();
    }

    public void tickServer() {
        if (impureSilicon == 0 || heatingTicks == 0) {
            return;
        }
        heatingTicks--;
        if (heatingTicks == 0) {
            updateStage(QuartzCrucibleStage.READY);
        }
        setChanged();
    }

    public int insertImpureSilicon(int offered) {
        if (offered <= 0 || isReady()) {
            return 0;
        }
        int inserted = Math.min(offered, CAPACITY - impureSilicon);
        if (inserted == 0) {
            return 0;
        }
        impureSilicon += inserted;
        heatingTicks += inserted * HEATING_TICKS_PER_INGOT;
        updateStage(QuartzCrucibleStage.HEATING);
        setChanged();
        return inserted;
    }

    public boolean useString() {
        if (!isReady()) {
            return false;
        }
        int outputCount = impureSilicon;
        impureSilicon = 0;
        heatingTicks = 0;
        if (level != null) {
            ItemEntity output = new ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 1.05D,
                    worldPosition.getZ() + 0.5D,
                    new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("silicon_ingot").get(), outputCount));
            output.setDefaultPickUpDelay();
            level.addFreshEntity(output);
        }
        updateStage(QuartzCrucibleStage.EMPTY);
        setChanged();
        return true;
    }

    public int impureSilicon() {
        return impureSilicon;
    }

    public int heatingTicks() {
        return heatingTicks;
    }

    public boolean isReady() {
        return impureSilicon > 0 && heatingTicks == 0;
    }

    public ItemStack removeImpureSilicon() {
        if (impureSilicon == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack contents = new ItemStack(
                MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get(), impureSilicon);
        impureSilicon = 0;
        heatingTicks = 0;
        setChanged();
        return contents;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("impure_silicon", impureSilicon);
        tag.putInt("heating_ticks", heatingTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        impureSilicon = tag.getInt("impure_silicon");
        heatingTicks = tag.getInt("heating_ticks");
    }

    private void updateStage(QuartzCrucibleStage stage) {
        if (level != null && getBlockState().getValue(QuartzCrucibleBlock.STAGE) != stage) {
            level.setBlock(worldPosition, getBlockState().setValue(QuartzCrucibleBlock.STAGE, stage), 3);
        }
    }
}
