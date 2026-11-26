package net.claustra01.clayium.laser;

import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.registry.LaserRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ClayEnergyLaserBlockEntity extends BlockEntity implements LaserVisual, net.minecraft.world.MenuProvider {
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
        }
    };

    private LaserPath path = new LaserPath(LaserPath.MAX_LENGTH, null,
            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
    private long nextPathCheck;
    private boolean pathDirty = true;
    private boolean irradiating;
    private final LaserIrradiation irradiation = new LaserIrradiation();

    public ClayEnergyLaserBlockEntity(BlockPos pos, BlockState state) {
        super(LaserRegistration.LASER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, ClayEnergyLaserBlockEntity laser) {
        laser.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction direction = direction();
        if (pathDirty || serverLevel.getGameTime() >= nextPathCheck || !path.remainsValid(serverLevel)) {
            LaserPath oldPath = path;
            path = LaserPath.find(serverLevel, worldPosition, direction);
            pathDirty = false;
            nextPathCheck = serverLevel.getGameTime() + 10L;
            if (oldPath.length() != path.length()) {
                sync();
            }
        }
        boolean powered = serverLevel.hasNeighborSignal(worldPosition);
        boolean stopped = ClayiumConfig.INVERT_CLAY_LASER_RS_CONDITION.get() ? !powered : powered;
        boolean active = !stopped && energy.consume(specification().energyPerTick(), fuel.getStackInSlot(0));
        if (active) {
            path.deliver(serverLevel, specification().beam(), direction, irradiation, worldPosition);
            setChanged();
        }
        if (irradiating != active) {
            irradiating = active;
            sync();
        }
    }

    public LaserSpecification specification() {
        return ((ClayEnergyLaserBlock)getBlockState().getBlock()).specification();
    }

    public ItemStackHandler fuelInventory() {
        return fuel;
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int id, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
        return new ClayEnergyLaserMenu(id, inventory, this);
    }

    public ClayEnergyStorage energyStorage() {
        return energy;
    }

    public IItemHandler fuelHandler(@Nullable Direction side) {
        return fuel;
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(1);
        dropped.setItem(0, fuel.getStackInSlot(0).copy());
        fuel.setStackInSlot(0, ItemStack.EMPTY);
        return dropped;
    }

    public void markPathDirty() {
        pathDirty = true;
    }

    @Override
    public Direction direction() {
        return getBlockState().getValue(ClayEnergyLaserBlock.FACING);
    }

    @Override
    public LaserBeam visibleBeam() {
        return specification().beam();
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", fuel.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        CompoundTag irradiationTag = new CompoundTag();
        irradiation.save(irradiationTag);
        tag.put("irradiation", irradiationTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            fuel.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.getCompound("energy"));
        }
        if (tag.contains("irradiation")) {
            irradiation.load(tag.getCompound("irradiation"));
        }
        if (level != null && level.isClientSide) {
            irradiating = tag.getBoolean("irradiating");
            path = new LaserPath(tag.getInt("laser_length"), null,
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }
        pathDirty = true;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("irradiating", irradiating);
        tag.putInt("laser_length", path.length());
        return tag;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
