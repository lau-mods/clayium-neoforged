package net.claustra01.clayium.transfer;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Automated sided item insertion/extraction via NeoForge {@link Capabilities.ItemHandler#BLOCK}
 * and internal stack production/consumption helpers matching original 1.7.10 {@code UtilTransfer}.
 */
public final class ClayiumTransfer {
    private ClayiumTransfer() {}

    /**
     * Pushes up to {@code maxTransfer} items from {@code from}'s {@code fromSlots} into the neighbor at {@code worldDir}.
     *
     * @return remaining transfer budget after moving items
     */
    public static int insert(
            ClayContainerBlockEntity from,
            int[] fromSlots,
            Direction worldDir,
            int maxTransfer) {
        return insert(from, fromSlots, worldDir, maxTransfer, -1);
    }

    public static int insert(
            ClayContainerBlockEntity from,
            int[] fromSlots,
            Direction worldDir,
            int maxTransfer,
            int track) {
        if (maxTransfer <= 0 || fromSlots == null || fromSlots.length == 0) {
            return maxTransfer;
        }
        Level level = from.getLevel();
        if (level == null) {
            return maxTransfer;
        }
        BlockPos targetPos = from.getBlockPos().relative(worldDir);
        Direction targetSide = worldDir.getOpposite();
        IItemHandler targetHandler = resolveItemHandler(level, targetPos, targetSide, track);
        if (targetHandler == null) {
            return maxTransfer;
        }

        IItemHandler sourceHandler = from.getItemHandler(worldDir);
        if (sourceHandler == null) {
            return maxTransfer;
        }
        return transfer(sourceHandler, fromSlots, targetHandler, targetSlotArray(targetHandler), maxTransfer);
    }

    /**
     * Pulls up to {@code maxTransfer} items from the neighbor at {@code worldDir} into {@code to}'s {@code toSlots}.
     *
     * @return remaining transfer budget after moving items
     */
    public static int extract(
            ClayContainerBlockEntity to,
            int[] toSlots,
            Direction worldDir,
            int maxTransfer) {
        return extract(to, toSlots, worldDir, maxTransfer, -1);
    }

    public static int extract(
            ClayContainerBlockEntity to,
            int[] toSlots,
            Direction worldDir,
            int maxTransfer,
            int track) {
        if (maxTransfer <= 0 || toSlots == null || toSlots.length == 0) {
            return maxTransfer;
        }
        Level level = to.getLevel();
        if (level == null) {
            return maxTransfer;
        }
        BlockPos sourcePos = to.getBlockPos().relative(worldDir);
        Direction sourceSide = worldDir.getOpposite();
        IItemHandler sourceHandler = resolveItemHandler(level, sourcePos, sourceSide, track);
        if (sourceHandler == null) {
            return maxTransfer;
        }

        IItemHandler targetHandler = to.getItemHandler(worldDir);
        if (targetHandler == null) {
            return maxTransfer;
        }
        return transfer(sourceHandler, targetSlotArray(sourceHandler), targetHandler, toSlots, maxTransfer);
    }

    @Nullable
    private static IItemHandler resolveItemHandler(Level level, BlockPos pos, Direction side, int track) {
        if (track >= 0) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MultitrackBufferBlockEntity mt) {
                return mt.getItemHandlerForTrack(side, track);
            }
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
    }

    private static int[] targetSlotArray(IItemHandler handler) {
        int size = handler.getSlots();
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = i;
        }
        return out;
    }

    /**
     * Two-pass slot transfer preserving original Clayium behavior: merge into matching stacks first,
     * then fill empty slots.
     */
    public static int transfer(
            IItemHandler from,
            int[] fromSlots,
            IItemHandler to,
            int[] toSlots,
            int maxTransfer) {
        int remaining = maxTransfer;
        if (remaining <= 0) {
            return remaining;
        }
        for (int fromSlot : fromSlots) {
            if (fromSlot < 0 || fromSlot >= from.getSlots()) {
                continue;
            }
            ItemStack inFrom = from.getStackInSlot(fromSlot);
            if (inFrom.isEmpty()) {
                continue;
            }

            // Pass 1: merge into non-empty matching destination slots
            for (int toSlot : toSlots) {
                if (toSlot < 0 || toSlot >= to.getSlots()) {
                    continue;
                }
                ItemStack inTo = to.getStackInSlot(toSlot);
                if (inTo.isEmpty() || !ItemStack.isSameItemSameComponents(inTo, inFrom)) {
                    continue;
                }
                remaining = moveBetweenSlots(from, fromSlot, to, toSlot, remaining);
                if (remaining <= 0) {
                    return 0;
                }
                if (from.getStackInSlot(fromSlot).isEmpty()) {
                    break;
                }
            }

            if (from.getStackInSlot(fromSlot).isEmpty()) {
                continue;
            }

            // Pass 2: place into empty destination slots
            for (int toSlot : toSlots) {
                if (toSlot < 0 || toSlot >= to.getSlots()) {
                    continue;
                }
                if (!to.getStackInSlot(toSlot).isEmpty()) {
                    continue;
                }
                remaining = moveBetweenSlots(from, fromSlot, to, toSlot, remaining);
                if (remaining <= 0) {
                    return 0;
                }
                if (from.getStackInSlot(fromSlot).isEmpty()) {
                    break;
                }
            }
        }
        return remaining;
    }

    private static int moveBetweenSlots(IItemHandler from, int fromSlot, IItemHandler to, int toSlot, int remaining) {
        ItemStack extractedSim = from.extractItem(fromSlot, remaining, true);
        if (extractedSim.isEmpty()) {
            return remaining;
        }
        ItemStack leftoverSim = to.insertItem(toSlot, extractedSim, true);
        int accepted = extractedSim.getCount() - leftoverSim.getCount();
        if (accepted <= 0) {
            return remaining;
        }
        ItemStack extractedReal = from.extractItem(fromSlot, accepted, false);
        if (extractedReal.isEmpty()) {
            return remaining;
        }
        ItemStack rejected = to.insertItem(toSlot, extractedReal, false);
        int moved = extractedReal.getCount() - rejected.getCount();
        if (!rejected.isEmpty()) {
            from.insertItem(fromSlot, rejected, false);
        }
        return Math.max(0, remaining - moved);
    }

    public static ItemStack produceItemStack(
            ItemStack stack,
            List<ItemStack> inventory,
            int index,
            int slotLimit) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack res = stack.copy();
        ItemStack existing = inventory.get(index);
        if (existing.isEmpty()) {
            int max = Math.min(stack.getMaxStackSize(), slotLimit);
            int moved = Math.min(res.getCount(), max);
            if (moved > 0) {
                inventory.set(index, res.copyWithCount(moved));
                res.shrink(moved);
            }
        } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int max = Math.min(existing.getMaxStackSize(), slotLimit);
            int space = Math.max(0, max - existing.getCount());
            int moved = Math.min(res.getCount(), space);
            if (moved > 0) {
                existing.grow(moved);
                res.shrink(moved);
            }
        }
        return res.isEmpty() ? ItemStack.EMPTY : res;
    }

    public static ItemStack produceItemStack(
            ItemStack stack,
            List<ItemStack> inventory,
            int startInclusive,
            int endExclusive,
            int slotLimit) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack res = stack.copy();
        for (int i = startInclusive; i < endExclusive && !res.isEmpty(); i++) {
            if (!inventory.get(i).isEmpty()) {
                res = produceItemStack(res, inventory, i, slotLimit);
            }
        }
        for (int i = startInclusive; i < endExclusive && !res.isEmpty(); i++) {
            if (inventory.get(i).isEmpty()) {
                res = produceItemStack(res, inventory, i, slotLimit);
            }
        }
        return res;
    }

    public static boolean canProduceItemStacks(
            List<ItemStack> outputs,
            List<ItemStack> inventory,
            int startInclusive,
            int endExclusive,
            int slotLimit) {
        if (outputs == null || outputs.isEmpty()) {
            return true;
        }
        List<ItemStack> simInv = new java.util.ArrayList<>(inventory.size());
        for (ItemStack s : inventory) {
            simInv.add(s.copy());
        }
        for (ItemStack out : outputs) {
            if (!out.isEmpty() && !produceItemStack(out.copy(), simInv, startInclusive, endExclusive, slotLimit).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static int canProduceItemStack(
            ItemStack stack,
            List<ItemStack> inventory,
            int startInclusive,
            int endExclusive,
            int slotLimit) {
        if (stack.isEmpty()) {
            return 0;
        }
        int totalSpace = 0;
        int limit = Math.min(stack.getMaxStackSize(), slotLimit);
        for (int i = startInclusive; i < endExclusive; i++) {
            ItemStack existing = inventory.get(i);
            if (existing.isEmpty()) {
                totalSpace += limit;
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                totalSpace += Math.max(0, limit - existing.getCount());
            }
        }
        return totalSpace;
    }

    public static int countItemStack(ItemStack target, List<ItemStack> inventory, int startInclusive, int endExclusive) {
        int count = 0;
        for (int i = startInclusive; i < endExclusive; i++) {
            ItemStack s = inventory.get(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, target)) {
                count += s.getCount();
            }
        }
        return count;
    }

    public static ItemStack consumeItemStack(ItemStack target, List<ItemStack> inventory, int startInclusive, int endExclusive) {
        int remaining = target.getCount();
        for (int i = startInclusive; i < endExclusive && remaining > 0; i++) {
            ItemStack s = inventory.get(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, target)) {
                int take = Math.min(remaining, s.getCount());
                s.shrink(take);
                if (s.isEmpty()) {
                    inventory.set(i, ItemStack.EMPTY);
                }
                remaining -= take;
            }
        }
        return remaining <= 0 ? ItemStack.EMPTY : target.copyWithCount(remaining);
    }
}
