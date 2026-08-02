/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserPath;
import net.claustra01.clayium.laser.ClayLaserReceiver;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.LaserReflectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class LaserReflectorBlockEntity extends BlockEntity implements ClayLaserReceiver {
    private final List<ClayLaser> received = new ArrayList<>();
    private final List<ClayLaser> ready = new ArrayList<>();
    private long receivedAt = Long.MIN_VALUE;
    private ClayLaser outputLaser = new ClayLaser(0, 0, 0, 0);
    private int beamLength;
    private boolean irradiating;
    public LaserReflectorBlockEntity(BlockPos pos, BlockState state) { super(ClayiumRegistries.LASER_REFLECTOR_BLOCK_ENTITY.get(), pos, state); }
    @Override public boolean receiveClayLaser(ClayLaser laser, Direction incomingSide) {
        if (laser == null || level == null) return false;
        long gameTime = level.getGameTime();
        if (receivedAt != gameTime && !received.isEmpty()) {
            ready.addAll(received);
            received.clear();
        }
        received.add(laser);
        receivedAt = gameTime;
        return true;
    }
    public void serverTick() {
        if (!(level instanceof ServerLevel server)) return;
        if (ready.isEmpty() && receivedAt < server.getGameTime() && !received.isEmpty()) {
            ready.addAll(received);
            received.clear();
            receivedAt = Long.MIN_VALUE;
        }
        if (ready.isEmpty()) {
            updateOutput(new ClayLaser(0, 0, 0, 0), 0, false);
            return;
        }
        ClayLaser merged = ClayLaser.merge(List.copyOf(ready));
        ready.clear();
        ClayLaserPath.Result result = ClayLaserPath.irradiate(
                server, worldPosition, getBlockState().getValue(LaserReflectorBlock.FACING), merged);
        updateOutput(merged, result.length(), true);
    }

    private void updateOutput(ClayLaser laser, int length, boolean active) {
        if (outputLaser.equals(laser) && beamLength == length && irradiating == active) return;
        outputLaser = laser;
        beamLength = length;
        irradiating = active;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public ClayLaser outputLaser() { return outputLaser; }
    public int beamLength() { return beamLength; }
    public boolean irradiating() { return irradiating; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LaserAge", outputLaser.age());
        tag.putInt("LaserBlue", outputLaser.blue());
        tag.putInt("LaserGreen", outputLaser.green());
        tag.putInt("LaserRed", outputLaser.red());
        tag.putInt("BeamLength", beamLength);
        tag.putBoolean("Irradiating", irradiating);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        outputLaser = new ClayLaser(
                Math.max(0, tag.getInt("LaserAge")),
                Math.max(0, tag.getInt("LaserBlue")),
                Math.max(0, tag.getInt("LaserGreen")),
                Math.max(0, tag.getInt("LaserRed")));
        beamLength = Math.max(0, Math.min(ClayLaserPath.MAX_LENGTH, tag.getInt("BeamLength")));
        irradiating = tag.getBoolean("Irradiating");
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if (tag != null) loadAdditional(tag, registries);
    }
}
