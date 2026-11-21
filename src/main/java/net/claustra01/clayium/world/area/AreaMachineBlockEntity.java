package net.claustra01.clayium.world.area;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class AreaMachineBlockEntity extends BlockEntity implements LaserReceiver, MenuProvider {
    public static final int OPERATION_SLOT = 0;
    public static final int FILTER_SLOT = 1;
    public static final int OUTPUT_START = 2;
    public static final int OUTPUT_SLOTS = 9;
    public static final int FUEL_SLOT = OUTPUT_START + OUTPUT_SLOTS;
    public static final int MAX_OPERATIONS_PER_TICK = 1000;

    private final ItemStackHandler inventory = new ItemStackHandler(FUEL_SLOT + 1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case FILTER_SLOT -> stack.is(LogisticsRegistration.ITEM_FILTER.get());
                case FUEL_SLOT -> ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                case OPERATION_SLOT -> !stack.is(LogisticsRegistration.ITEM_FILTER.get());
                default -> true;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == FILTER_SLOT ? 1 : super.getSlotLimit(slot);
        }
    };
    private final IItemHandler itemHandler = new AreaItemHandler();
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private AreaBounds workArea;
    private long cursor;
    private String targetDimension = "";
    @Nullable private UUID owner;
    private LaserBeam pendingLaser = new LaserBeam(0L, 0L, 0L);
    private long progress;
    private OperationStatus status = OperationStatus.IDLE;

    public AreaMachineBlockEntity(BlockPos pos, BlockState state) {
        super(AreaMachineRegistration.MACHINE_BLOCK_ENTITY.get(), pos, state);
        workArea = new AreaBounds(pos, pos);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, AreaMachineBlockEntity machine) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (machine.owner == null) {
            machine.status = OperationStatus.NO_OWNER;
            return;
        }
        if (!machine.targetDimension.isEmpty()
                && !machine.targetDimension.equals(level.dimension().location().toString())) {
            machine.status = OperationStatus.WRONG_DIMENSION;
            return;
        }
        double factor = 1.0D + 4.0D * Math.log10(1.0D + (long)machine.pendingLaser.energy() / 1000L);
        machine.pendingLaser = new LaserBeam(0L, 0L, 0L);
        long cost = (long)(machine.machineType().energyPerTick() * factor);
        if (!machine.energy.consume(ClayEnergy.ofInternal(cost), machine.inventory.getStackInSlot(FUEL_SLOT))) {
            machine.status = OperationStatus.NO_ENERGY;
            return;
        }
        machine.status = OperationStatus.IDLE;
        machine.progress += (long)(100.0D * factor);
        int operations = (int)Math.min(MAX_OPERATIONS_PER_TICK,
                machine.progress / machine.machineType().progressPerJob());
        if (operations > 0) {
            machine.runOperationsNow(operations);
            machine.progress %= machine.machineType().progressPerJob();
        }
        machine.setChanged();
    }

    public int runOperationsNow(int maximumOperations) {
        status = OperationStatus.IDLE;
        if (!(level instanceof ServerLevel serverLevel) || owner == null) {
            status = OperationStatus.NO_OWNER;
            return 0;
        }
        if (!targetDimension.isEmpty() && !targetDimension.equals(serverLevel.dimension().location().toString())) {
            status = OperationStatus.WRONG_DIMENSION;
            return 0;
        }
        workArea = workArea.clampHeight(serverLevel);
        int budget = Math.max(0, Math.min(maximumOperations, MAX_OPERATIONS_PER_TICK));
        int completed = machineType() == AreaMachineType.ITEM_COLLECTOR
                ? collectItems(serverLevel, budget)
                : scanBlocks(serverLevel, budget);
        if (completed > 0 && status == OperationStatus.IDLE) {
            status = OperationStatus.WORKING;
        }
        setChanged();
        return completed;
    }

    private int scanBlocks(ServerLevel serverLevel, int budget) {
        int completed = 0;
        for (int inspected = 0; inspected < budget; inspected++) {
            BlockPos target = workArea.positionAt(cursor);
            if (!serverLevel.isLoaded(target)) {
                status = OperationStatus.UNLOADED;
                break;
            }
            BlockState targetState = serverLevel.getBlockState(target);
            if (targetState.isAir() || !accepts(new ItemStack(targetState.getBlock().asItem()))) {
                advanceCursor();
                continue;
            }
            BlockOperationResult result = switch (machineType()) {
                case MINER, ADVANCED_MINER -> breakTarget(serverLevel, target);
                case REPLACER -> replaceTarget(serverLevel, target);
                case ACTIVATOR -> activateTarget(serverLevel, target);
                case ITEM_COLLECTOR -> BlockOperationResult.FAILED;
            };
            if (result == BlockOperationResult.SUCCESS) {
                completed++;
                advanceCursor();
            } else if (result == BlockOperationResult.NO_TARGET) {
                advanceCursor();
            } else if (result == BlockOperationResult.UNLOADED) {
                status = OperationStatus.UNLOADED;
                break;
            } else if (result == BlockOperationResult.DENIED) {
                status = OperationStatus.PROTECTED;
                break;
            } else if (result == BlockOperationResult.NO_OUTPUT_SPACE) {
                status = OperationStatus.OUTPUT_FULL;
                break;
            } else {
                status = machineType() == AreaMachineType.REPLACER
                        ? OperationStatus.NO_MATERIAL : OperationStatus.PROTECTED;
                break;
            }
        }
        setChanged();
        return completed;
    }

    private void advanceCursor() {
        cursor = (cursor + 1L) % workArea.volume();
    }

    private BlockOperationResult breakTarget(ServerLevel serverLevel, BlockPos target) {
        return BlockOperationService.breakBlock(
                serverLevel,
                worldPosition,
                target,
                owner,
                inventory.getStackInSlot(OPERATION_SLOT).copy(),
                stack -> inventory.setStackInSlot(OPERATION_SLOT, stack),
                stacks -> insertOutputs(stacks, true).isEmpty(),
                stacks -> insertOutputs(stacks, false));
    }

    private BlockOperationResult replaceTarget(ServerLevel serverLevel, BlockPos target) {
        ItemStack replacement = inventory.getStackInSlot(OPERATION_SLOT);
        if (!(replacement.getItem() instanceof BlockItem blockItem)) {
            return BlockOperationResult.FAILED;
        }
        BlockState replacementState = blockItem.getBlock().defaultBlockState();
        if (serverLevel.getBlockState(target).is(replacementState.getBlock())) {
            return BlockOperationResult.NO_TARGET;
        }
        FakePlayer placer = FakePlayerFactory.get(serverLevel, new GameProfile(owner, "[Clayium]"));
        placer.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D);
        placer.setItemInHand(InteractionHand.MAIN_HAND, replacement.copyWithCount(1));
        BlockPos against = target.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atBottomCenterOf(target), Direction.UP, against, false);
        BlockPlaceContext context = new BlockPlaceContext(placer, InteractionHand.MAIN_HAND,
                placer.getMainHandItem(), hit);
        BlockState placement = blockItem.getBlock().getStateForPlacement(context);
        if (placement == null || !placement.canSurvive(serverLevel, target)
                || !serverLevel.mayInteract(placer, target)
                || EventHooks.onBlockPlace(placer,
                        BlockSnapshot.create(serverLevel.dimension(), serverLevel, target), Direction.UP)) {
            placer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return BlockOperationResult.DENIED;
        }
        placer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        BlockOperationResult result = breakTarget(serverLevel, target);
        if (result != BlockOperationResult.SUCCESS) {
            return result;
        }
        serverLevel.setBlockAndUpdate(target, placement);
        ItemStack storedReplacement = inventory.getStackInSlot(OPERATION_SLOT);
        storedReplacement.shrink(1);
        inventory.setStackInSlot(OPERATION_SLOT, storedReplacement);
        return BlockOperationResult.SUCCESS;
    }

    private BlockOperationResult activateTarget(ServerLevel serverLevel, BlockPos target) {
        ItemStack input = inventory.getStackInSlot(OPERATION_SLOT);
        ItemStack used = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
        Direction facing = getBlockState().getValue(AreaMachineBlock.FACING);
        BlockOperationService.UseResult use = BlockOperationService.useBlock(
                serverLevel, worldPosition, target, facing, owner, used);
        if (use.result() != BlockOperationResult.SUCCESS) {
            return use.result();
        }

        List<ItemStack> returned = new ArrayList<>(use.returnedItems());
        if (!input.isEmpty()) {
            ItemStack remaining = input.copy();
            remaining.shrink(1);
            ItemStack held = use.heldStack();
            if (!held.isEmpty() && input.is(held.getItem()) && held.getCount() == 1) {
                if (remaining.isEmpty()) {
                    remaining = held.copy();
                } else if (ItemStack.isSameItemSameComponents(remaining, held)) {
                    remaining.grow(1);
                } else {
                    returned.add(held.copy());
                }
            } else if (!held.isEmpty()) {
                returned.add(held.copy());
            }
            inventory.setStackInSlot(OPERATION_SLOT, remaining);
        }
        returned.forEach(stack -> insertOutputs(List.of(stack), false).forEach(remainder ->
                Containers.dropItemStack(serverLevel,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 1.0D,
                        worldPosition.getZ() + 0.5D,
                        remainder)));
        return BlockOperationResult.SUCCESS;
    }

    private int collectItems(ServerLevel serverLevel, int budget) {
        for (int chunkX = workArea.minimum().getX() >> 4;
                chunkX <= workArea.maximum().getX() >> 4; chunkX++) {
            for (int chunkZ = workArea.minimum().getZ() >> 4;
                    chunkZ <= workArea.maximum().getZ() >> 4; chunkZ++) {
                if (!serverLevel.isLoaded(new BlockPos(chunkX << 4, workArea.minimum().getY(), chunkZ << 4))) {
                    status = OperationStatus.UNLOADED;
                    return 0;
                }
            }
        }
        int collected = 0;
        List<ItemEntity> entities = serverLevel.getEntitiesOfClass(
                ItemEntity.class, workArea.box(), entity -> entity.isAlive() && accepts(entity.getItem()));
        for (ItemEntity entity : entities) {
            if (collected >= budget) {
                break;
            }
            ItemStack stack = entity.getItem();
            if (!insertOutputs(List.of(stack), true).isEmpty()) {
                status = OperationStatus.OUTPUT_FULL;
                break;
            }
            insertOutputs(List.of(stack), false);
            entity.discard();
            collected++;
        }
        return collected;
    }

    private boolean accepts(ItemStack candidate) {
        return !candidate.isEmpty() && ItemFilter.accepts(inventory.getStackInSlot(FILTER_SLOT), candidate);
    }

    private List<ItemStack> insertOutputs(List<ItemStack> stacks, boolean simulate) {
        List<ItemStack> remainingStacks = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack remaining = stack.copy();
            for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_SLOTS && !remaining.isEmpty(); slot++) {
                remaining = inventory.insertItem(slot, remaining, simulate);
            }
            if (!remaining.isEmpty()) {
                remainingStacks.add(remaining);
            }
        }
        return remainingStacks;
    }

    public void configureArea(AreaBounds area) {
        workArea = level == null ? area : area.clampHeight(level);
        targetDimension = level == null ? targetDimension : level.dimension().location().toString();
        cursor = 0L;
        setChangedAndSync();
    }

    public AreaBounds workArea() {
        return workArea;
    }

    public long cursor() {
        return cursor;
    }

    public OperationStatus status() {
        return status;
    }

    public AreaMachineType machineType() {
        return ((AreaMachineBlock)getBlockState().getBlock()).machineType();
    }

    @Override
    public boolean receiveLaser(LaserBeam beam) {
        if (beam.isEmpty()) {
            return false;
        }
        pendingLaser = LaserBeam.merge(pendingLaser, beam);
        return true;
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AreaMachineMenu(containerId, playerInventory, this);
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return itemHandler;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public Container dropInventory() {
        SimpleContainer drops = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            drops.setItem(slot, inventory.getStackInSlot(slot).copy());
        }
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putLong("progress", progress);
        tag.putLong("area_min", workArea.minimum().asLong());
        tag.putLong("area_max", workArea.maximum().asLong());
        tag.putLong("cursor", cursor);
        tag.putString("target_dimension", targetDimension);
        tag.putString("status", status.name());
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.getCompound("energy"));
        }
        progress = tag.getLong("progress");
        if (tag.contains("area_min") && tag.contains("area_max")) {
            workArea = new AreaBounds(BlockPos.of(tag.getLong("area_min")), BlockPos.of(tag.getLong("area_max")));
        }
        cursor = Math.floorMod(tag.getLong("cursor"), workArea.volume());
        targetDimension = tag.getString("target_dimension");
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        status = tag.contains("status") ? OperationStatus.valueOf(tag.getString("status")) : OperationStatus.IDLE;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("area_min", workArea.minimum().asLong());
        tag.putLong("area_max", workArea.maximum().asLong());
        return tag;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public enum OperationStatus {
        IDLE,
        WORKING,
        NO_OWNER,
        WRONG_DIMENSION,
        UNLOADED,
        PROTECTED,
        OUTPUT_FULL,
        NO_MATERIAL,
        NO_ENERGY
    }

    private final class AreaItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot < OUTPUT_START || slot == FUEL_SLOT ? inventory.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= OUTPUT_START ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(slot, stack);
        }
    }
}
