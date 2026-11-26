package net.claustra01.clayium.world.chunkloading;

import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ChunkLoaderBlockEntity extends BlockEntity {
    private TicketStatus ticketStatus = TicketStatus.WAITING;

    public ChunkLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(ChunkLoaderRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, ChunkLoaderBlockEntity chunkLoader) {
        if (chunkLoader.ticketStatus == TicketStatus.ACTIVE) {
            chunkLoader.publishTicketStatus();
            return;
        }
        chunkLoader.requestTickets();
    }

    public TicketStatus ticketStatus() {
        return ticketStatus;
    }

    void releaseTicketsForRemoval() {
        if (level instanceof ServerLevel serverLevel) {
            ChunkLoaderTicketService.release(serverLevel, worldPosition);
        }
        ticketStatus = TicketStatus.WAITING;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ticketStatus == TicketStatus.UNAVAILABLE) {
            tag.putString("ticket_status", ticketStatus.getSerializedName());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ticketStatus = "unavailable".equals(tag.getString("ticket_status"))
                ? TicketStatus.UNAVAILABLE : TicketStatus.WAITING;
        publishTicketStatus();
    }

    private void requestTickets() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        setTicketStatus(ChunkLoaderTicketService.acquire(serverLevel, worldPosition)
                ? TicketStatus.ACTIVE : TicketStatus.UNAVAILABLE);
    }

    private void setTicketStatus(TicketStatus status) {
        boolean changed = ticketStatus != status;
        ticketStatus = status;
        publishTicketStatus();
        if (changed) {
            setChanged();
        }
    }

    private void publishTicketStatus() {
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.is(ChunkLoaderRegistration.CHUNK_LOADER.get())
                    && state.getValue(ChunkLoaderBlock.STATUS) != ticketStatus) {
                level.setBlock(worldPosition, state.setValue(ChunkLoaderBlock.STATUS, ticketStatus), 3);
            }
        }
    }

    public enum TicketStatus implements StringRepresentable {
        WAITING("waiting"),
        ACTIVE("active"),
        UNAVAILABLE("unavailable");

        private final String serializedName;

        TicketStatus(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
