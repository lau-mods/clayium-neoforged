package net.claustra01.clayium.machine.crucible;

import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
            syncVisual();
        }
        setChanged();
    }

    public void absorbDroppedItem(ItemEntity entity) {
        if (level == null || level.isClientSide || !entity.isAlive()) {
            return;
        }
        double x = entity.getX() - worldPosition.getX();
        double z = entity.getZ() - worldPosition.getZ();
        if (x < 0.1875D || x > 0.8125D || z < 0.1875D || z > 0.8125D
                || entity.getY() < worldPosition.getY() + 0.25D) {
            return;
        }
        ItemStack stack = entity.getItem();
        int consumed = 0;
        if (stack.is(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get())) {
            consumed = insertImpureSilicon(1);
        } else if (stack.is(net.minecraft.world.item.Items.STRING) && useString()) {
            consumed = 1;
        }
        if (consumed <= 0) {
            return;
        }
        ItemStack remaining = stack.copy();
        remaining.shrink(consumed);
        if (remaining.isEmpty()) {
            entity.discard();
        } else {
            entity.setItem(remaining);
        }
    }

    public int insertImpureSilicon(int offered) {
        if (offered <= 0) {
            return 0;
        }
        int inserted = Math.min(offered, CAPACITY - impureSilicon);
        if (inserted == 0) {
            return 0;
        }
        impureSilicon += inserted;
        heatingTicks += inserted * HEATING_TICKS_PER_INGOT;
        updateStage(QuartzCrucibleStage.HEATING);
        syncVisual();
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
                    worldPosition.getX() + 0.4D,
                    worldPosition.getY() + 0.4D,
                    worldPosition.getZ() + 0.4D,
                    new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("silicon_ingot").get(), outputCount));
            level.addFreshEntity(output);
        }
        updateStage(QuartzCrucibleStage.EMPTY);
        syncVisual();
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

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("impure_silicon", impureSilicon);
        tag.putInt("heating_ticks", heatingTicks);
        return tag;
    }

    private void updateStage(QuartzCrucibleStage stage) {
        if (level != null && getBlockState().getValue(QuartzCrucibleBlock.STAGE) != stage) {
            level.setBlock(worldPosition, getBlockState().setValue(QuartzCrucibleBlock.STAGE, stage), 3);
        }
    }

    private void syncVisual() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
