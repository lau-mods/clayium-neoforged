package net.claustra01.clayium.machine.iface;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.multiblock.MultiblockStructureNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public final class InterfaceBlockEntity extends BlockEntity implements MenuProvider, LaserReceiver {
    private final IItemHandler[] itemHandlers = new IItemHandler[Direction.values().length + 1];
    private final ContainerData menuData = new InterfaceContainerData();

    private SynchronizedTarget target;
    private UUID owner;

    public InterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(InterfaceRegistration.BLOCK_ENTITY.get(), pos, state);
        for (int index = 0; index < itemHandlers.length; index++) {
            itemHandlers[index] = new TargetItemHandler(index == Direction.values().length
                    ? null : Direction.from3DDataValue(index));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InterfaceBlockEntity blockEntity) {
        if (blockEntity.interfaceType() != InterfaceType.REDSTONE) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        blockEntity.resolveTarget().ifPresent(target -> {
            RedstoneControlReceiver receiver = target.getLevel().getCapability(
                    InterfaceCapabilities.REDSTONE_RECEIVER, target.getBlockPos(), null);
            if (receiver != null) {
                receiver.setRemoteRedstonePowered(powered);
            }
        });
    }

    public InterfaceType interfaceType() {
        return ((InterfaceBlock)getBlockState().getBlock()).interfaceType();
    }

    public int tier() {
        return ((InterfaceBlock)getBlockState().getBlock()).tier();
    }

    public void synchronize(SynchronizedTarget newTarget, @Nullable UUID newOwner) {
        if (newTarget.pos().equals(worldPosition) && level != null && newTarget.dimension().equals(level.dimension())) {
            return;
        }
        target = newTarget;
        owner = newOwner;
        setChangedAndSync();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            MultiblockStructureNotifier.componentChanged(level, worldPosition);
        }
    }

    public void disconnect() {
        target = null;
        owner = null;
        setChangedAndSync();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            MultiblockStructureNotifier.componentChanged(level, worldPosition);
        }
    }

    @Nullable
    public SynchronizedTarget target() {
        return target;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public ConnectionStatus connectionStatus() {
        if (target == null) {
            return ConnectionStatus.UNLINKED;
        }
        return resolveTarget().isPresent()
                ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;
    }

    private Optional<BlockEntity> resolveTarget() {
        if (!(level instanceof ServerLevel sourceLevel) || target == null) {
            return Optional.empty();
        }
        ServerLevel targetLevel = sourceLevel.getServer().getLevel(target.dimension());
        if (targetLevel == null || !targetLevel.isLoaded(target.pos())) {
            return Optional.empty();
        }
        if (owner != null) {
            ServerPlayer player = sourceLevel.getServer().getPlayerList().getPlayer(owner);
            if (player == null || !player.mayInteract(targetLevel, target.pos())) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(targetLevel.getBlockEntity(target.pos()));
    }

    private IItemHandler resolveItemHandler(@Nullable Direction direction) {
        if (interfaceType() != InterfaceType.ITEM) {
            return null;
        }
        return resolveTarget().map(target -> target.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, target.getBlockPos(), direction)).orElse(null);
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return itemHandlers[direction == null ? Direction.values().length : direction.get3DDataValue()];
    }

    @Override
    public boolean receiveLaser(LaserBeam beam) {
        if (interfaceType() != InterfaceType.LASER) {
            return false;
        }
        return resolveTarget().map(target -> {
            LaserReceiver receiver = target.getLevel().getCapability(
                    InterfaceCapabilities.LASER_RECEIVER, target.getBlockPos(), null);
            return receiver != null && receiver.receiveLaser(beam);
        }).orElse(false);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new InterfaceMenu(containerId, inventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (target != null) {
            tag.putString("target_dimension", target.dimension().location().toString());
            tag.putLong("target_pos", target.pos().asLong());
        }
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("target_dimension"));
        target = dimension == null || !tag.contains("target_pos") ? null : new SynchronizedTarget(
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimension),
                BlockPos.of(tag.getLong("target_pos")));
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (target != null) {
            tag.putString("target_dimension", target.dimension().location().toString());
            tag.putLong("target_pos", target.pos().asLong());
        }
        return tag;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public enum ConnectionStatus {
        UNLINKED,
        CONNECTED,
        DISCONNECTED
    }

    private final class InterfaceContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> tier();
                case 1 -> connectionStatus().ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private final class TargetItemHandler implements IItemHandler {
        private final Direction direction;

        private TargetItemHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getSlots() {
            IItemHandler targetHandler = resolveItemHandler(direction);
            return targetHandler == null ? 0 : targetHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            IItemHandler targetHandler = resolveItemHandler(direction);
            return targetHandler == null || slot < 0 || slot >= targetHandler.getSlots()
                    ? ItemStack.EMPTY : targetHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            IItemHandler targetHandler = resolveItemHandler(direction);
            return targetHandler == null || slot < 0 || slot >= targetHandler.getSlots()
                    ? stack : targetHandler.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler targetHandler = resolveItemHandler(direction);
            return targetHandler == null || slot < 0 || slot >= targetHandler.getSlots()
                    ? ItemStack.EMPTY : targetHandler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler targetHandler = resolveItemHandler(direction);
            return targetHandler == null || slot < 0 || slot >= targetHandler.getSlots()
                    ? 0 : targetHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            IItemHandler targetHandler = resolveItemHandler(direction);
            return targetHandler != null && slot >= 0 && slot < targetHandler.getSlots()
                    && targetHandler.isItemValid(slot, stack);
        }
    }
}
