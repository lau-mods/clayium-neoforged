package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Redstone Interface (T5..T13): binds to an {@link IExternalControl} machine via Synchronizer,
 * emitting redstone status ("Emit if idle" / "Emit if work scheduled" / "Emit if doing work")
 * or driving startWork/stopWork on incoming redstone edges.
 */
public class RedstoneInterfaceBlockEntity extends BlockEntity implements ISynchronizedInterface {
    public static final String[] STATE_SEQUENCE = {
            "None",
            "Emit if idle",
            "Emit if work scheduled",
            "Emit if doing work",
            "Do work",
            "Do not work",
            "Start work",
            "Stop work",
            "Do work once"
    };

    protected int coreOffsetX = 0;
    protected int coreOffsetY = 0;
    protected int coreOffsetZ = 0;
    protected boolean synced = false;
    protected int lastSignal = 0;
    protected int lastPower = 0;
    protected String controlState = "None";
    @Nullable
    protected ResourceKey<Level> coreDimension = null;

    public RedstoneInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.REDSTONE_INTERFACE.get(), pos, state);
    }

    public String getControlState() {
        return this.controlState;
    }

    public void setControlState(String state) {
        this.controlState = state != null ? state : "None";
        markForSync();
    }

    public void cycleControlState() {
        int idx = 0;
        for (int i = 0; i < STATE_SEQUENCE.length; i++) {
            if (STATE_SEQUENCE[i].equals(this.controlState)) {
                idx = (i + 1) % STATE_SEQUENCE.length;
                break;
            }
        }
        setControlState(STATE_SEQUENCE[idx]);
    }

    @Override
    public boolean setCoreBlockOffset(int dx, int dy, int dz) {
        markForSync();
        this.coreOffsetX = dx;
        this.coreOffsetY = dy;
        this.coreOffsetZ = dz;
        return syncCoreBlock();
    }

    @Override
    public boolean setCoreBlockDimension(ResourceKey<Level> dimension) {
        if (this.level != null && this.level.dimension().equals(dimension)) {
            this.coreDimension = null;
            return true;
        }
        this.coreDimension = dimension;
        return getCoreLevel() != null;
    }

    @Override
    public BlockPos getCoreBlockOffset() {
        return new BlockPos(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ);
    }

    @Override
    public boolean isSynced() {
        return this.synced;
    }

    @Override
    public boolean acceptsCoordChanger() {
        return true;
    }

    @Override
    public boolean acceptInterfaceSync() {
        return false;
    }

    @Nullable
    protected Level getCoreLevel() {
        if (this.level == null) {
            return null;
        }
        if (this.coreDimension == null || this.level.dimension().equals(this.coreDimension)) {
            return this.level;
        }
        return this.level.getServer() != null ? this.level.getServer().getLevel(this.coreDimension) : null;
    }

    @Nullable
    public IExternalControl getCore() {
        syncCoreBlock();
        Level coreLevel = getCoreLevel();
        if (coreLevel == null) {
            return null;
        }
        BlockEntity be = coreLevel.getBlockEntity(this.worldPosition.offset(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ));
        return be instanceof IExternalControl ctrl ? ctrl : null;
    }

    protected boolean syncCoreBlock() {
        Level coreLevel = getCoreLevel();
        if (coreLevel != null && (this.coreOffsetX != 0 || this.coreOffsetY != 0 || this.coreOffsetZ != 0 || this.coreDimension != null)) {
            BlockPos target = this.worldPosition.offset(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ);
            if (coreLevel.getBlockEntity(target) instanceof IExternalControl) {
                this.synced = true;
                return true;
            }
        }
        this.synced = false;
        return false;
    }

    public int getProvidingWeakPower() {
        IExternalControl core = getCore();
        if (core == null) {
            return 0;
        }
        return switch (this.controlState) {
            case "Emit if idle" -> !core.isDoingWork() ? 15 : 0;
            case "Emit if work scheduled" -> core.isScheduledWork() ? 15 : 0;
            case "Emit if doing work" -> core.isDoingWork() ? 15 : 0;
            default -> 0;
        };
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        IExternalControl core = getCore();
        int signal = this.level.getBestNeighborSignal(this.worldPosition);
        int power = getProvidingWeakPower();

        if (core != null) {
            switch (this.controlState) {
                case "Do work" -> {
                    if (this.lastSignal <= 0 && signal > 0) {
                        core.startWork();
                    }
                    if (this.lastSignal != 0 && signal == 0) {
                        core.stopWork();
                    }
                }
                case "Do not work" -> {
                    if (this.lastSignal <= 0 && signal > 0) {
                        core.stopWork();
                    }
                    if (this.lastSignal != 0 && signal == 0) {
                        core.startWork();
                    }
                }
                case "Start work" -> {
                    if (this.lastSignal <= 0 && signal > 0) {
                        core.startWork();
                    }
                }
                case "Stop work" -> {
                    if (this.lastSignal <= 0 && signal > 0) {
                        core.stopWork();
                    }
                }
                case "Do work once" -> {
                    if (this.lastSignal <= 0 && signal > 0) {
                        core.startWork();
                    } else if (core.isDoingWork()) {
                        core.stopWork();
                    }
                }
                default -> {}
            }
        }

        if (this.lastPower != power) {
            this.lastPower = power;
            this.level.updateNeighborsAt(this.worldPosition, getBlockState().getBlock());
            markForInstantSync();
        }
        this.lastSignal = signal;
    }

    public static void serverTickStatic(Level level, BlockPos pos, BlockState state, RedstoneInterfaceBlockEntity be) {
        be.serverTick();
    }

    @Override
    public boolean isUsableTool(ItemStack stack, Player player, Direction hitFace) {
        return !stack.isEmpty() && stack.is(ClayiumItems.byPath("synchronizer").get());
    }

    @Override
    public boolean onToolUse(ItemStack stack, Player player, Direction hitFace) {
        if (this.level == null || !isUsableTool(stack, player, hitFace)) {
            return false;
        }
        if (!this.level.isClientSide) {
            GlobalPos gp = stack.get(ClayiumDataComponents.SYNCHRONIZER_TARGET.get());
            if (gp != null) {
                BlockPos target = gp.pos();
                if (setCoreBlockDimension(gp.dimension())
                        && setCoreBlockOffset(
                                target.getX() - this.worldPosition.getX(),
                                target.getY() - this.worldPosition.getY(),
                                target.getZ() - this.worldPosition.getZ())) {
                    player.sendSystemMessage(Component.literal(
                            "Set Core block Coord (" + target.getX() + "," + target.getY() + "," + target.getZ()
                                    + ") in dim " + gp.dimension().location()
                    ));
                }
            }
        }
        return true;
    }

    @Override
    public void markForSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void markForInstantSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CoreBlockX", this.coreOffsetX);
        tag.putInt("CoreBlockY", this.coreOffsetY);
        tag.putInt("CoreBlockZ", this.coreOffsetZ);
        tag.putBoolean("Synced", this.synced);
        tag.putInt("LastSignal", this.lastSignal);
        tag.putInt("LastPower", this.lastPower);
        tag.putString("ControlState", this.controlState);
        if (this.coreDimension != null) {
            tag.putString("CoreBlockDimension", this.coreDimension.location().toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.coreOffsetX = tag.getInt("CoreBlockX");
        this.coreOffsetY = tag.getInt("CoreBlockY");
        this.coreOffsetZ = tag.getInt("CoreBlockZ");
        this.synced = tag.getBoolean("Synced");
        this.lastSignal = tag.getInt("LastSignal");
        this.lastPower = tag.getInt("LastPower");
        if (tag.contains("ControlState")) {
            this.controlState = tag.getString("ControlState");
        }
        if (tag.contains("CoreBlockDimension")) {
            ResourceLocation loc = ResourceLocation.tryParse(tag.getString("CoreBlockDimension"));
            this.coreDimension = loc != null ? ResourceKey.create(Registries.DIMENSION, loc) : null;
        } else {
            this.coreDimension = null;
        }
    }

    @Override
    @Nullable
    public ResourceKey<Level> getCoreDimension() {
        return this.coreDimension;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
