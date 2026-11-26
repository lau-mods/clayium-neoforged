package net.claustra01.clayium.world.area;

import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AreaMarkerBlockEntity extends BlockEntity {
    private BlockPos secondCorner;
    @Nullable private UUID owner;

    public AreaMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(AreaMachineRegistration.MARKER_BLOCK_ENTITY.get(), pos, state);
        secondCorner = pos;
    }

    public AreaMarkerType markerType() {
        return ((AreaMarkerBlock)getBlockState().getBlock()).markerType();
    }

    public AreaBounds selectedArea() {
        return level == null
                ? new AreaBounds(worldPosition, secondCorner)
                : AreaBounds.between(level, markerType(), worldPosition, secondCorner);
    }

    public void select(BlockPos otherCorner) {
        secondCorner = otherCorner.immutable();
        sync();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("second_corner", secondCorner.asLong());
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        secondCorner = tag.contains("second_corner") ? BlockPos.of(tag.getLong("second_corner")) : worldPosition;
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
