package net.claustra01.clayium.machine.miner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.MinerRegistration;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BasicOperationBlockEntity extends BlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_START = 1;
    public static final int OUTPUT_SLOTS = 9;
    private static final int OPERATION_INTERVAL = 20;

    private final ItemStackHandler inventory = new ItemStackHandler(OUTPUT_START + OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT;
        }
    };
    private final IItemHandler itemHandler = new OperationItemHandler();
    @Nullable private UUID owner;
    private int operationTick;

    public BasicOperationBlockEntity(BlockPos pos, BlockState state) {
        super(MinerRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, BasicOperationBlockEntity operation) {
        if (++operation.operationTick >= OPERATION_INTERVAL) {
            operation.operationTick = 0;
            operation.runOperationNow();
            operation.setChanged();
        }
    }

    public BlockOperationResult runOperationNow() {
        if (!(level instanceof ServerLevel serverLevel) || owner == null) {
            return BlockOperationResult.FAILED;
        }
        Direction facing = getBlockState().getValue(BasicOperationBlock.FACING);
        BlockPos target = worldPosition.relative(facing);
        return operationType() == BasicOperationType.BLOCK_BREAKER
                ? breakTarget(serverLevel, target)
                : activateTarget(serverLevel, target, facing);
    }

    private BlockOperationResult breakTarget(ServerLevel level, BlockPos target) {
        BlockOperationResult result = BlockOperationService.breakBlock(
                level,
                worldPosition,
                target,
                owner,
                inventory.getStackInSlot(INPUT_SLOT).copy(),
                stack -> inventory.setStackInSlot(INPUT_SLOT, stack),
                stacks -> insertOutputs(stacks, true).isEmpty(),
                stacks -> insertOutputs(stacks, false));
        return result;
    }

    private BlockOperationResult activateTarget(ServerLevel level, BlockPos target, Direction facing) {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        ItemStack used = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
        BlockOperationService.UseResult use = BlockOperationService.useBlock(
                level, worldPosition, target, facing, owner, used);
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
            inventory.setStackInSlot(INPUT_SLOT, remainingInput);
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

    private List<ItemStack> insertOutputs(List<ItemStack> stacks, boolean simulate) {
        List<ItemStack> contents = new ArrayList<>(OUTPUT_SLOTS);
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            contents.add(inventory.getStackInSlot(OUTPUT_START + slot).copy());
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
                inventory.setStackInSlot(OUTPUT_START + slot, contents.get(slot));
            }
        }
        return remainders;
    }

    public BasicOperationType operationType() {
        return ((BasicOperationBlock)getBlockState().getBlock()).operationType();
    }

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

    public Container dropInventory() {
        SimpleContainer drops = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            drops.setItem(slot, inventory.getStackInSlot(slot));
        }
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putInt("operation_tick", operationTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        operationTick = Math.max(0, tag.getInt("operation_tick"));
    }

    private final class OperationItemHandler implements IItemHandler {
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
            return slot == INPUT_SLOT ? inventory.insertItem(slot, stack, simulate) : stack;
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
            return slot == INPUT_SLOT;
        }
    }
}
