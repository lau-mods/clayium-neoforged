package net.claustra01.clayium.world.chunkloading;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ChunkLoaderBlockEntity extends BlockEntity implements MenuProvider {
    private final ContainerData menuData = new ChunkLoaderContainerData();
    private TicketStatus ticketStatus = TicketStatus.WAITING;

    public ChunkLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(ChunkLoaderRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, ChunkLoaderBlockEntity chunkLoader) {
        if (chunkLoader.ticketStatus == TicketStatus.ACTIVE) {
            return;
        }
        chunkLoader.requestTickets();
    }

    public TicketStatus ticketStatus() {
        return ticketStatus;
    }

    public void releaseTickets() {
        if (level instanceof ServerLevel serverLevel) {
            ChunkLoaderTicketService.release(serverLevel, worldPosition);
        }
        setTicketStatus(TicketStatus.WAITING);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ChunkLoaderMenu(containerId, inventory, this, menuData);
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
    }

    private void requestTickets() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        setTicketStatus(ChunkLoaderTicketService.acquire(serverLevel, worldPosition)
                ? TicketStatus.ACTIVE : TicketStatus.UNAVAILABLE);
    }

    private void setTicketStatus(TicketStatus status) {
        if (ticketStatus == status) {
            return;
        }
        ticketStatus = status;
        setChanged();
    }

    public enum TicketStatus {
        WAITING("waiting"),
        ACTIVE("active"),
        UNAVAILABLE("unavailable");

        private final String serializedName;

        TicketStatus(String serializedName) {
            this.serializedName = serializedName;
        }

        public String getSerializedName() {
            return serializedName;
        }
    }

    private final class ChunkLoaderContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return index == 0 ? ticketStatus.ordinal() : 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}
