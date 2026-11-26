package net.claustra01.clayium.laser;

import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.LaserRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class LaserReflectorBlockEntity extends BlockEntity implements LaserReceiver, LaserVisual {
    private LaserPath path = new LaserPath(0, null, Blocks.AIR.defaultBlockState());
    private LaserBeam visibleBeam = new LaserBeam(0L, 0L, 0L);
    private LaserBeam pendingBeam = new LaserBeam(0L, 0L, 0L);
    private LaserBeam outgoingBeam = new LaserBeam(0L, 0L, 0L);
    private long pendingGameTime = Long.MIN_VALUE;
    private long nextPathCheck;
    private boolean pathDirty = true;
    private boolean irradiating;
    private final LaserIrradiation irradiation = new LaserIrradiation();

    public LaserReflectorBlockEntity(BlockPos pos, BlockState state) {
        super(LaserRegistration.REFLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, LaserReflectorBlockEntity reflector) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long gameTime = level.getGameTime();
        if (reflector.pendingGameTime < gameTime) {
            reflector.outgoingBeam = reflector.pendingBeam;
            reflector.pendingBeam = new LaserBeam(0L, 0L, 0L);
        }
        if (!reflector.outgoingBeam.isEmpty()) {
            LaserBeam outgoing = reflector.outgoingBeam;
            reflector.outgoingBeam = new LaserBeam(0L, 0L, 0L);
            Direction output = reflector.direction();
            int oldLength = reflector.path.length();
            if (reflector.pathDirty || gameTime >= reflector.nextPathCheck
                    || !reflector.path.remainsValid(serverLevel)) {
                reflector.path = LaserPath.find(serverLevel, pos, output);
                reflector.pathDirty = false;
                reflector.nextPathCheck = gameTime + 10L;
            }
            reflector.path.deliver(serverLevel, outgoing, output, reflector.irradiation, pos);
            boolean changed = !reflector.irradiating || !reflector.visibleBeam.equals(outgoing)
                    || oldLength != reflector.path.length();
            reflector.visibleBeam = outgoing;
            reflector.irradiating = true;
            if (changed) {
                reflector.sync();
            }
        } else if (reflector.irradiating) {
            reflector.irradiating = false;
            reflector.sync();
        }
    }

    @Override
    public boolean receiveLaser(LaserBeam beam) {
        if (!(level instanceof ServerLevel serverLevel) || beam.isEmpty() || beam.age() >= LaserPath.MAX_LENGTH) {
            return false;
        }
        LaserBeam reflected = beam.reflected();
        if (pendingGameTime == serverLevel.getGameTime()) {
            pendingBeam = LaserBeam.merge(pendingBeam, reflected);
        } else {
            if (!pendingBeam.isEmpty()) {
                outgoingBeam = pendingBeam;
            }
            pendingBeam = reflected;
        }
        pendingGameTime = serverLevel.getGameTime();
        return true;
    }

    public void markPathDirty() {
        pathDirty = true;
    }

    @Override
    public Direction direction() {
        return getBlockState().getValue(LaserReflectorBlock.FACING);
    }

    @Override
    public LaserBeam visibleBeam() {
        return visibleBeam;
    }

    @Override
    public int laserLength() {
        return path.length();
    }

    @Override
    public boolean isIrradiating() {
        return irradiating;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("irradiation")) {
            irradiation.load(tag.getCompound("irradiation"));
        }
        if (level != null && level.isClientSide) {
            visibleBeam = new LaserBeam(tag.getLong("laser_blue"), tag.getLong("laser_green"),
                    tag.getLong("laser_red"), tag.getInt("laser_age"));
            path = new LaserPath(tag.getInt("laser_length"), null, Blocks.AIR.defaultBlockState());
            irradiating = tag.getBoolean("irradiating");
        }
        pathDirty = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag irradiationTag = new CompoundTag();
        irradiation.save(irradiationTag);
        tag.put("irradiation", irradiationTag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("laser_blue", visibleBeam.blue());
        tag.putLong("laser_green", visibleBeam.green());
        tag.putLong("laser_red", visibleBeam.red());
        tag.putInt("laser_age", visibleBeam.age());
        tag.putInt("laser_length", path.length());
        tag.putBoolean("irradiating", irradiating);
        return tag;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
