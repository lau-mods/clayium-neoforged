package net.claustra01.clayium.machine.miner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.registry.MinerRegistration;
import net.claustra01.clayium.world.area.AreaGuiHost;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.claustra01.clayium.world.area.RayTraceMemory;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Original Tier 6 blockMiner (TileAreaMiner) and blockActivator (TileAreaActivator) without
 * area mode: a 3x3 inventory, a harvest SlotMemory filter, and the start/stop/loop/box buttons.
 * Without area mode the original consumes no CE.
 */
public final class BasicOperationBlockEntity extends BlockEntity implements MenuProvider, AreaGuiHost {
    public static final int GRID_SLOTS = 9;
    public static final int HARVEST_FILTER_SLOT = 9;
    private static final int OPERATION_INTERVAL = 20;

    private final ItemStackHandler inventory = new ItemStackHandler(GRID_SLOTS + 1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < GRID_SLOTS;
        }
    };
    private final IItemHandler itemHandler = new OperationItemHandler();
    @Nullable private UUID owner;
    private int operationTick;
    /** TileAreaMiner.initState without area mode: state 3 (loop). */
    private AreaMachineBlockEntity.RunMode runMode = AreaMachineBlockEntity.RunMode.LOOP;
    private int boxState;
    private AreaMachineBlockEntity.ActivatorTarget activatorTarget = AreaMachineBlockEntity.ActivatorTarget.BLOCK;
    private boolean activatorRayTrace;
    private boolean activatorSneaking;

    public BasicOperationBlockEntity(BlockPos pos, BlockState state) {
        super(MinerRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, BasicOperationBlockEntity operation) {
        if (operation.runMode == AreaMachineBlockEntity.RunMode.STOPPED) {
            return;
        }
        if (++operation.operationTick >= OPERATION_INTERVAL) {
            operation.operationTick = 0;
            BlockOperationResult result = operation.runOperationNow();
            if (operation.runMode == AreaMachineBlockEntity.RunMode.ONCE
                    && result != BlockOperationResult.UNLOADED) {
                operation.runMode = AreaMachineBlockEntity.RunMode.STOPPED;
            }
            operation.setChanged();
        }
    }

    public BlockOperationResult runOperationNow() {
        if (!(level instanceof ServerLevel serverLevel) || owner == null) {
            return BlockOperationResult.FAILED;
        }
        Direction facing = getBlockState().getValue(BasicOperationBlock.FACING);
        BlockPos target = worldPosition.relative(facing);
        if (operationType() == BasicOperationType.BLOCK_BREAKER || activatorTarget == AreaMachineBlockEntity.ActivatorTarget.BLOCK) {
            BlockState targetState = serverLevel.getBlockState(target);
            if (targetState.isAir() || !ItemFilter.accepts(
                    inventory.getStackInSlot(HARVEST_FILTER_SLOT), new ItemStack(targetState.getBlock().asItem()))) {
                return BlockOperationResult.NO_TARGET;
            }
        }
        return operationType() == BasicOperationType.BLOCK_BREAKER
                ? breakTarget(serverLevel, target)
                : activateTarget(serverLevel, target, facing);
    }

    /** Original-style toolless inventory: evaluate drops with an unenchanted diamond pickaxe. */
    private BlockOperationResult breakTarget(ServerLevel level, BlockPos target) {
        return BlockOperationService.breakBlock(
                level,
                worldPosition,
                target,
                owner,
                new ItemStack(Items.DIAMOND_PICKAXE),
                stack -> { },
                stacks -> insertOutputs(stacks, true).isEmpty(),
                stacks -> insertOutputs(stacks, false));
    }

    private BlockOperationResult activateTarget(ServerLevel level, BlockPos target, Direction facing) {
        int useSlot = useSlot();
        ItemStack input = useSlot < 0 ? ItemStack.EMPTY : inventory.getStackInSlot(useSlot);
        ItemStack used = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
        BlockOperationService.UseResult use = null;
        if (activatorTarget != AreaMachineBlockEntity.ActivatorTarget.BLOCK) {
            RayTraceMemory memory = RayTraceMemory.standard(facing);
            use = BlockOperationService.useEntity(level, worldPosition, target, owner, used,
                    memory.startAt(target), memory.look(), activatorRayTrace, activatorSneaking);
            if (use.result() != BlockOperationResult.SUCCESS
                    && activatorTarget == AreaMachineBlockEntity.ActivatorTarget.ENTITY) {
                return use.result();
            }
        }
        if (use == null || use.result() != BlockOperationResult.SUCCESS) {
            use = BlockOperationService.useBlock(level, worldPosition,
                    RayTraceMemory.standard(facing).directHit(target), owner, used, activatorSneaking);
        }
        if (use.result() != BlockOperationResult.SUCCESS) {
            return use.result();
        }
        List<ItemStack> returned = new ArrayList<>(use.returnedItems());
        if (!input.isEmpty()) {
            ItemStack remainingInput = input.copy();
            remainingInput.shrink(1);
            ItemStack held = use.heldStack();
            if (!held.isEmpty() && input.is(held.getItem()) && held.getCount() == 1) {
                if (remainingInput.isEmpty()) {
                    remainingInput = held.copy();
                } else if (ItemStack.isSameItemSameComponents(remainingInput, held)) {
                    remainingInput.grow(1);
                } else {
                    returned.add(held.copy());
                }
            } else if (!held.isEmpty()) {
                returned.add(held.copy());
            }
            inventory.setStackInSlot(useSlot, remainingInput);
        }
        for (ItemStack stack : returned) {
            insertOutputs(List.of(stack), false).forEach(remainder ->
                    net.minecraft.world.Containers.dropItemStack(level,
                            worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D,
                            worldPosition.getZ() + 0.5D, remainder));
        }
        setChanged();
        return BlockOperationResult.SUCCESS;
    }

    /** Original TileAreaActivator: the first non-empty inventory slot is used. */
    private int useSlot() {
        for (int slot = 0; slot < GRID_SLOTS; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private List<ItemStack> insertOutputs(List<ItemStack> stacks, boolean simulate) {
        List<ItemStack> contents = new ArrayList<>(GRID_SLOTS);
        for (int slot = 0; slot < GRID_SLOTS; slot++) {
            contents.add(inventory.getStackInSlot(slot).copy());
        }
        List<ItemStack> remainders = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack remaining = stack.copy();
            for (int slot = 0; slot < contents.size() && !remaining.isEmpty(); slot++) {
                ItemStack existing = contents.get(slot);
                if (existing.isEmpty()) {
                    int inserted = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                    contents.set(slot, remaining.copyWithCount(inserted));
                    remaining.shrink(inserted);
                } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int inserted = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(inserted);
                    remaining.shrink(inserted);
                }
            }
            if (!remaining.isEmpty()) {
                remainders.add(remaining);
            }
        }
        if (!simulate) {
            for (int slot = 0; slot < contents.size(); slot++) {
                inventory.setStackInSlot(slot, contents.get(slot));
            }
        }
        return remainders;
    }

    public BasicOperationType operationType() {
        return ((BasicOperationBlock)getBlockState().getBlock()).operationType();
    }

    @Override
    public ItemStackHandler inventory() {
        return inventory;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return itemHandler;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public AreaMachineBlockEntity.RunMode runMode() {
        return runMode;
    }

    public int boxState() {
        return boxState;
    }

    public Container dropInventory() {
        SimpleContainer drops = new SimpleContainer(GRID_SLOTS);
        for (int slot = 0; slot < GRID_SLOTS; slot++) {
            drops.setItem(slot, inventory.getStackInSlot(slot));
        }
        return drops;
    }

    @Override public int guiTier() { return 6; }
    @Override public int gridColumns() { return 3; }
    @Override public int gridRows() { return 3; }
    @Override public boolean hasReplacementGrid() { return false; }
    @Override public int harvestFilterSlot() { return HARVEST_FILTER_SLOT; }
    @Override public int fortuneFilterSlot() { return -1; }
    @Override public int silkTouchFilterSlot() { return -1; }
    @Override public int fuelSlot() { return -1; }
    @Override public boolean isActivator() { return operationType() == BasicOperationType.ACTIVATOR; }
    @Override public long storedEnergy() { return 0L; }
    @Override public long laserEnergy() { return 0L; }
    @Override public AreaMachineBlockEntity.ActivatorTarget activatorTarget() { return activatorTarget; }
    @Override public boolean activatorRayTrace() { return activatorRayTrace; }
    @Override public boolean activatorSneaking() { return activatorSneaking; }

    @Override
    public boolean applyControl(int action) {
        switch (action) {
            case BUTTON_START -> { runMode = AreaMachineBlockEntity.RunMode.ONCE; operationTick = 0; }
            case BUTTON_STOP -> runMode = AreaMachineBlockEntity.RunMode.STOPPED;
            case BUTTON_LOOP -> { runMode = AreaMachineBlockEntity.RunMode.LOOP; operationTick = 0; }
            case BUTTON_BOX -> boxState = (boxState + 1) % 3;
            case BUTTON_TARGET, BUTTON_RAY_TRACE, BUTTON_SNEAK -> {
                if (!isActivator()) {
                    return false;
                }
                switch (action) {
                    case BUTTON_TARGET -> activatorTarget = activatorTarget.next();
                    case BUTTON_RAY_TRACE -> activatorRayTrace = !activatorRayTrace;
                    default -> activatorSneaking = !activatorSneaking;
                }
            }
            default -> {
                return false;
            }
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putInt("operation_tick", operationTick);
        tag.putString("run_mode", runMode.name());
        tag.putInt("box_state", boxState);
        tag.putString("activator_target", activatorTarget.name());
        tag.putBoolean("activator_ray_trace", activatorRayTrace);
        tag.putBoolean("activator_sneaking", activatorSneaking);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            CompoundTag saved = tag.getCompound("inventory").copy();
            saved.putInt("Size", inventory.getSlots());
            inventory.deserializeNBT(registries, saved);
        }
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        operationTick = Math.max(0, tag.getInt("operation_tick"));
        runMode = tag.contains("run_mode")
                ? AreaMachineBlockEntity.RunMode.valueOf(tag.getString("run_mode"))
                : AreaMachineBlockEntity.RunMode.LOOP;
        boxState = Math.floorMod(tag.getInt("box_state"), 3);
        activatorTarget = tag.contains("activator_target")
                ? AreaMachineBlockEntity.ActivatorTarget.valueOf(tag.getString("activator_target"))
                : AreaMachineBlockEntity.ActivatorTarget.BLOCK;
        activatorRayTrace = tag.getBoolean("activator_ray_trace");
        activatorSneaking = tag.getBoolean("activator_sneaking");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("box_state", boxState);
        return tag;
    }

    private final class OperationItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return GRID_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isActivator() ? inventory.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isActivator();
        }
    }
}
