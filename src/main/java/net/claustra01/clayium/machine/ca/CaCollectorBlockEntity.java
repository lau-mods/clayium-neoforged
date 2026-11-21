package net.claustra01.clayium.machine.ca;

import javax.annotation.Nullable;
import net.claustra01.clayium.machine.augment.AugmentScanner;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class CaCollectorBlockEntity extends BlockEntity {
    public static final long BASE_CRAFT_TIME = 10_000L;
    private static final int OUTPUT_SLOTS = 9;

    private final ItemStackHandler output = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };
    private long progress;
    private double progressRemainder;
    private double resonance = 1.0D;
    private boolean resonanceDirty = true;

    public CaCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(CaRegistration.COLLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, CaCollectorBlockEntity collector) {
        collector.tickServer();
    }

    private void tickServer() {
        if (level == null) {
            return;
        }
        if (resonanceDirty) {
            resonance = AugmentScanner.resonance(level, worldPosition);
            resonanceDirty = false;
        }
        if (resonance <= 1.0D || outputRoom() == 0) {
            return;
        }
        double accumulated = progressRemainder + resonance - 1.0D;
        long wholeProgress = (long)Math.floor(accumulated);
        progressRemainder = accumulated - wholeProgress;
        progress = saturatedAdd(progress, wholeProgress);
        int produced = (int)Math.min(Math.min(progress / BASE_CRAFT_TIME, 64L), outputRoom());
        if (produced > 0) {
            insertOutput(new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get(), produced));
            progress -= BASE_CRAFT_TIME * produced;
        }
        setChanged();
    }

    private int outputRoom() {
        ItemStack antimatter = new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get());
        int room = 0;
        for (int slot = 0; slot < output.getSlots(); slot++) {
            ItemStack stack = output.getStackInSlot(slot);
            if (stack.isEmpty()) {
                room += antimatter.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, antimatter)) {
                room += stack.getMaxStackSize() - stack.getCount();
            }
        }
        return room;
    }

    private void insertOutput(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < output.getSlots() && !remaining.isEmpty(); slot++) {
            ItemStack existing = output.getStackInSlot(slot);
            if (existing.isEmpty()) {
                int placed = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                output.setStackInSlot(slot, remaining.copyWithCount(placed));
                remaining.shrink(placed);
            } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int placed = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(placed);
                remaining.shrink(placed);
            }
        }
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    public IItemHandler itemHandler(@Nullable net.minecraft.core.Direction direction) {
        return output;
    }

    public ItemStackHandler output() {
        return output;
    }

    public long progress() {
        return progress;
    }

    public void invalidateResonance() {
        resonanceDirty = true;
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(output.getSlots());
        for (int slot = 0; slot < output.getSlots(); slot++) {
            dropped.setItem(slot, output.getStackInSlot(slot).copy());
            output.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return dropped;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", output.serializeNBT(registries));
        tag.putLong("progress", progress);
        tag.putDouble("progress_remainder", progressRemainder);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            output.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = Math.max(0L, tag.getLong("progress"));
        progressRemainder = Math.max(0.0D, tag.getDouble("progress_remainder"));
        resonanceDirty = true;
    }
}
