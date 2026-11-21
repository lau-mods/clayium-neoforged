package net.claustra01.clayium.multiblock;

import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class MultiblockControllerBlockEntity extends ClayMachineBlockEntity implements LaserReceiver {
    private boolean structureDirty = true;
    private boolean formed;
    private int structureTier;
    private long structureValidationCount;
    private LaserBeam receivedLaser = new LaserBeam(0L, 0L, 0L);
    private long receivedLaserTick = Long.MIN_VALUE;

    public MultiblockControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MultiblockRegistration.CONTROLLER_BLOCK_ENTITY.get(), pos, state);
        structureTier = machineType().minimumTier();
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            MultiblockControllerBlockEntity controller) {
        controller.tickServer(level.random);
    }

    @Override
    public void tickServer(RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (structureDirty) {
            validateStructure(serverLevel);
        }
        if (!formed) {
            setState(MachineState.IDLE);
            return;
        }
        super.tickServer(random);
    }

    private void validateStructure(ServerLevel level) {
        structureDirty = false;
        structureValidationCount++;
        MultiblockStructure structure = MultiblockStructure.inspect(
                level,
                worldPosition,
                getBlockState().getValue(ClayMachineBlock.FACING),
                machineType());
        boolean changed = formed != structure.formed() || structureTier != structure.tier();
        if (changed && activeRecipe() != null) {
            abortProcess();
        }
        formed = structure.formed();
        structureTier = structure.tier();
        if (getBlockState().getValue(MultiblockControllerBlock.FORMED) != formed) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(MultiblockControllerBlock.FORMED, formed),
                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void markStructureDirty() {
        structureDirty = true;
    }

    public boolean isFormed() {
        return formed;
    }

    public long structureValidationCount() {
        return structureValidationCount;
    }

    @Override
    public boolean receiveLaser(LaserBeam beam) {
        if (!formed || machineType() != MultiblockMachineType.REACTOR || level == null || beam.isEmpty()) {
            return false;
        }
        long gameTime = level.getGameTime();
        receivedLaser = receivedLaserTick == gameTime ? LaserBeam.merge(receivedLaser, beam) : beam;
        receivedLaserTick = gameTime;
        return true;
    }

    @Override
    protected double processingProgressMultiplier() {
        if (machineType() != MultiblockMachineType.REACTOR
                || level == null
                || receivedLaserTick < level.getGameTime() - 1L) {
            return 1.0D;
        }
        return 1.0D + (long)receivedLaser.energy();
    }

    @Override
    public int tier() {
        return formed ? structureTier : machineType().minimumTier();
    }

    public MultiblockMachineType machineType() {
        return ((MultiblockControllerBlock)getBlockState().getBlock()).machineType();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("formed", formed);
        tag.putInt("structure_tier", structureTier);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        formed = tag.getBoolean("formed");
        structureTier = Math.max(machineType().minimumTier(), tag.getInt("structure_tier"));
        structureDirty = true;
    }
}
