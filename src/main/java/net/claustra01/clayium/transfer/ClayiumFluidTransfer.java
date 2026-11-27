package net.claustra01.clayium.transfer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Exact 1.7.10 fluid-capsule & tank interconversion utilities ({@code 1000 / 125 / 25 / 5 / 1} mB greedy packing).
 */
public final class ClayiumFluidTransfer {
    private static final int[] CAPACITIES_DESC = {1000, 125, 25, 5, 1};
    private static final String[] CAPSULE_PATHS_DESC = {
            "fluid_capsule_1000",
            "fluid_capsule_125",
            "fluid_capsule_25",
            "fluid_capsule_5",
            "fluid_capsule_1"
    };

    private ClayiumFluidTransfer() {}

    public static FluidCapsuleItem capsuleForCapacity(int index) {
        return (FluidCapsuleItem) ClayiumItems.byPath(CAPSULE_PATHS_DESC[index]).get();
    }

    public static ItemStack createFilledCapsule(int capsuleIndex, int count, Fluid fluid, DataComponentPatch patch) {
        FluidCapsuleItem item = capsuleForCapacity(capsuleIndex);
        ItemStack stack = new ItemStack(item, count);
        FluidStack fs = new FluidStack(fluid.builtInRegistryHolder(), item.getCapacityMb(), patch);
        stack.set(ClayiumDataComponents.FLUID_CONTENT.get(), SimpleFluidContent.copyOf(fs));
        return stack;
    }

    public static FluidStack extractCapsuleFluid(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof FluidCapsuleItem capsule)) {
            return FluidStack.EMPTY;
        }
        SimpleFluidContent content = stack.get(ClayiumDataComponents.FLUID_CONTENT.get());
        if (content == null || content.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack copy = content.copy();
        copy.setAmount(capsule.getCapacityMb());
        return copy;
    }

    /**
     * Inserts fluid from capsules in {@code fromSlots} directly into an adjacent {@link IFluidHandler}.
     *
     * @return remaining transfer budget in capsule items
     */
    public static int insertToTank(
            ClayContainerBlockEntity from,
            int[] fromSlots,
            Direction worldDir,
            int maxTransfer) {
        if (maxTransfer <= 0 || fromSlots == null || fromSlots.length == 0) {
            return maxTransfer;
        }
        Level level = from.getLevel();
        if (level == null) {
            return maxTransfer;
        }
        BlockPos targetPos = from.getBlockPos().relative(worldDir);
        IFluidHandler toTank = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, worldDir.getOpposite());
        if (toTank == null) {
            return maxTransfer;
        }
        IItemHandler fromHandler = from.getItemHandler(worldDir);
        if (fromHandler == null) {
            return maxTransfer;
        }

        int remaining = maxTransfer;
        for (int fromSlot : fromSlots) {
            if (fromSlot < 0 || fromSlot >= fromHandler.getSlots()) {
                continue;
            }
            ItemStack inSlot = fromHandler.getStackInSlot(fromSlot);
            FluidStack unitFluid = extractCapsuleFluid(inSlot);
            if (unitFluid.isEmpty()) {
                continue;
            }
            int capacity = unitFluid.getAmount();
            ItemStack extractTest = fromHandler.extractItem(fromSlot, remaining, true);
            if (extractTest.isEmpty()) {
                continue;
            }
            int maxCapsules = extractTest.getCount();
            FluidStack simRequest = unitFluid.copyWithAmount(capacity * maxCapsules);
            int simFilled = toTank.fill(simRequest, FluidAction.SIMULATE);
            int fillableCapsules = Math.min(maxCapsules, simFilled / capacity);
            if (fillableCapsules <= 0) {
                continue;
            }
            int confirmedFill = toTank.fill(unitFluid.copyWithAmount(fillableCapsules * capacity), FluidAction.SIMULATE);
            fillableCapsules = Math.min(fillableCapsules, confirmedFill / capacity);
            if (fillableCapsules <= 0) {
                continue;
            }
            ItemStack extracted = fromHandler.extractItem(fromSlot, fillableCapsules, false);
            if (extracted.isEmpty()) {
                continue;
            }
            toTank.fill(unitFluid.copyWithAmount(extracted.getCount() * capacity), FluidAction.EXECUTE);
            remaining -= extracted.getCount();
            if (remaining <= 0) {
                return 0;
            }
        }
        return remaining;
    }

    /**
     * Extracts fluid from an adjacent {@link IFluidHandler} and converts it into fluid capsules inside {@code toSlots}.
     *
     * @return remaining transfer budget in capsule items
     */
    public static int extractFromTank(
            ClayContainerBlockEntity to,
            int[] toSlots,
            Direction worldDir,
            int maxTransfer) {
        if (maxTransfer <= 0 || toSlots == null || toSlots.length == 0) {
            return maxTransfer;
        }
        Level level = to.getLevel();
        if (level == null) {
            return maxTransfer;
        }
        BlockPos sourcePos = to.getBlockPos().relative(worldDir);
        IFluidHandler fromTank = level.getCapability(Capabilities.FluidHandler.BLOCK, sourcePos, worldDir.getOpposite());
        if (fromTank == null) {
            return maxTransfer;
        }
        IItemHandler toHandler = to.getItemHandler(worldDir);
        if (toHandler == null) {
            return maxTransfer;
        }

        int remaining = maxTransfer;
        for (int tankIdx = 0; tankIdx < fromTank.getTanks() && remaining > 0; tankIdx++) {
            FluidStack tankFluid = fromTank.getFluidInTank(tankIdx);
            if (tankFluid.isEmpty()) {
                continue;
            }
            FluidStack availSim = fromTank.drain(tankFluid.copyWithAmount(Integer.MAX_VALUE), FluidAction.SIMULATE);
            if (availSim.isEmpty()) {
                continue;
            }
            int availAmount = availSim.getAmount();

            for (int i = 0; i < CAPACITIES_DESC.length && remaining > 0; i++) {
                int cap = CAPACITIES_DESC[i];
                if (availAmount < cap) {
                    continue;
                }
                ItemStack proto = createFilledCapsule(i, 1, availSim.getFluid(), availSim.getComponentsPatch());
                int moved = insertCapsulesIntoSlots(fromTank, toHandler, toSlots, availSim, cap, proto, remaining);
                remaining -= moved;
                availAmount -= moved * cap;
            }
        }
        return remaining;
    }

    private static int insertCapsulesIntoSlots(
            IFluidHandler fromTank,
            IItemHandler toHandler,
            int[] toSlots,
            FluidStack template,
            int capPerCapsule,
            ItemStack proto,
            int budget) {
        int totalMoved = 0;
        // Pass 1: existing matching slots; Pass 2: empty slots
        for (int pass = 0; pass < 2 && totalMoved < budget; pass++) {
            for (int toSlot : toSlots) {
                if (toSlot < 0 || toSlot >= toHandler.getSlots()) {
                    continue;
                }
                ItemStack existing = toHandler.getStackInSlot(toSlot);
                if (pass == 0 && (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, proto))) {
                    continue;
                }
                if (pass == 1 && !existing.isEmpty()) {
                    continue;
                }
                int maxInsert = Math.min(budget - totalMoved, toHandler.getSlotLimit(toSlot));
                if (maxInsert <= 0) {
                    continue;
                }
                ItemStack leftover = toHandler.insertItem(toSlot, proto.copyWithCount(maxInsert), true);
                int accepted = maxInsert - leftover.getCount();
                if (accepted <= 0) {
                    continue;
                }
                FluidStack drainSim = fromTank.drain(template.copyWithAmount(accepted * capPerCapsule), FluidAction.SIMULATE);
                int canDrainCount = drainSim.getAmount() / capPerCapsule;
                if (canDrainCount <= 0) {
                    continue;
                }
                FluidStack drained = fromTank.drain(template.copyWithAmount(canDrainCount * capPerCapsule), FluidAction.EXECUTE);
                int actualCount = drained.getAmount() / capPerCapsule;
                if (actualCount > 0) {
                    toHandler.insertItem(toSlot, proto.copyWithCount(actualCount), false);
                    totalMoved += actualCount;
                }
                if (totalMoved >= budget) {
                    break;
                }
            }
        }
        return totalMoved;
    }

    public static void sortFluid(List<ItemStack> inventory, int[] slots) {
        List<Integer> emptySlots = new ArrayList<>();
        Map<FluidKey, List<Integer>> capsuleSlots = new LinkedHashMap<>();
        Map<FluidKey, Integer> amounts = new LinkedHashMap<>();

        int max = slots == null ? inventory.size() : slots.length;
        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) {
                emptySlots.add(slot);
            } else {
                FluidStack fs = extractCapsuleFluid(stack);
                if (!fs.isEmpty()) {
                    FluidKey key = new FluidKey(fs.getFluid(), fs.getComponentsPatch());
                    capsuleSlots.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
                    amounts.merge(key, stack.getCount() * fs.getAmount(), Integer::sum);
                }
            }
        }

        for (var entry : capsuleSlots.entrySet()) {
            FluidKey key = entry.getKey();
            int totalMb = amounts.getOrDefault(key, 0);
            List<Integer> owned = entry.getValue();
            int[] stackSizes = new int[CAPACITIES_DESC.length];
            int requiredStacks = 0;
            for (int i = 0; i < CAPACITIES_DESC.length; i++) {
                stackSizes[i] = totalMb / CAPACITIES_DESC[i];
                totalMb -= stackSizes[i] * CAPACITIES_DESC[i];
                requiredStacks += (stackSizes[i] + 63) / 64;
            }
            if (requiredStacks > owned.size() + emptySlots.size()) {
                continue;
            }
            List<Integer> freed = new ArrayList<>();
            List<Integer> consumedEmpty = new ArrayList<>();
            int cursor = 0;
            int totalSlots = owned.size() + emptySlots.size();
            for (int sIdx = 0; sIdx < totalSlots; sIdx++) {
                while (cursor < stackSizes.length && stackSizes[cursor] == 0) {
                    cursor++;
                }
                int slot = sIdx < owned.size() ? owned.get(sIdx) : emptySlots.get(sIdx - owned.size());
                if (cursor >= stackSizes.length) {
                    inventory.set(slot, ItemStack.EMPTY);
                    if (sIdx < owned.size()) {
                        freed.add(slot);
                    }
                } else {
                    if (sIdx >= owned.size()) {
                        consumedEmpty.add(slot);
                    }
                    int move = Math.min(64, stackSizes[cursor]);
                    inventory.set(slot, createFilledCapsule(cursor, move, key.fluid(), key.patch()));
                    stackSizes[cursor] -= move;
                    if (stackSizes[cursor] == 0) {
                        cursor++;
                    }
                }
            }
            emptySlots.addAll(freed);
            emptySlots.removeAll(consumedEmpty);
        }
    }

    public static int fillFluid(List<ItemStack> inventory, int[] slots, FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        FluidKey targetKey = new FluidKey(resource.getFluid(), resource.getComponentsPatch());
        List<Integer> emptySlots = new ArrayList<>();
        List<Integer> capsuleSlots = new ArrayList<>();
        int existingAmount = 0;
        int max = slots == null ? inventory.size() : slots.length;

        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) {
                emptySlots.add(slot);
            } else {
                FluidStack fs = extractCapsuleFluid(stack);
                if (!fs.isEmpty() && targetKey.equals(new FluidKey(fs.getFluid(), fs.getComponentsPatch()))) {
                    capsuleSlots.add(slot);
                    existingAmount += stack.getCount() * fs.getAmount();
                }
            }
        }

        int totalSlots = capsuleSlots.size() + emptySlots.size();
        if (totalSlots == 0) {
            return 0;
        }
        int totalAmount = existingAmount + resource.getAmount();
        int remaining = totalAmount;
        int[] stackSizes = new int[CAPACITIES_DESC.length];
        int usedStacks = 0;

        for (int i = 0; i < CAPACITIES_DESC.length; i++) {
            int wanted = remaining / CAPACITIES_DESC[i];
            int neededStacks = (wanted + 63) / 64;
            if (usedStacks + neededStacks > totalSlots) {
                int capped = Math.max(0, (totalSlots - usedStacks) * 64);
                stackSizes[i] = capped;
                remaining -= capped * CAPACITIES_DESC[i];
                break;
            }
            stackSizes[i] = wanted;
            remaining -= wanted * CAPACITIES_DESC[i];
            usedStacks += neededStacks;
        }

        int filled = resource.getAmount() - remaining;
        if (action.execute() && filled > 0) {
            applyPackedLayout(inventory, capsuleSlots, emptySlots, stackSizes, targetKey);
        }
        return Math.max(0, filled);
    }

    public static FluidStack drainFluid(List<ItemStack> inventory, int[] slots, FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidKey targetKey = new FluidKey(resource.getFluid(), resource.getComponentsPatch());
        return drainInternal(inventory, slots, targetKey, resource.getAmount(), action);
    }

    public static FluidStack drainFluid(List<ItemStack> inventory, int[] slots, int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        int max = slots == null ? inventory.size() : slots.length;
        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            FluidStack fs = extractCapsuleFluid(inventory.get(slot));
            if (!fs.isEmpty()) {
                return drainInternal(inventory, slots, new FluidKey(fs.getFluid(), fs.getComponentsPatch()), maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    public static FluidStack getTankFluid(List<ItemStack> inventory, int[] slots) {
        int max = slots == null ? inventory.size() : slots.length;
        FluidKey firstKey = null;
        int total = 0;
        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            ItemStack s = inventory.get(slot);
            FluidStack fs = extractCapsuleFluid(s);
            if (!fs.isEmpty()) {
                FluidKey key = new FluidKey(fs.getFluid(), fs.getComponentsPatch());
                if (firstKey == null) {
                    firstKey = key;
                }
                if (firstKey.equals(key)) {
                    total += s.getCount() * fs.getAmount();
                }
            }
        }
        if (firstKey == null || total <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(firstKey.fluid().builtInRegistryHolder(), total, firstKey.patch());
    }

    public static int getTankCapacity(List<ItemStack> inventory, int[] slots) {
        int max = slots == null ? inventory.size() : slots.length;
        int freeOrSame = 0;
        FluidKey firstKey = null;
        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            ItemStack s = inventory.get(slot);
            if (s.isEmpty()) {
                freeOrSame++;
            } else {
                FluidStack fs = extractCapsuleFluid(s);
                if (!fs.isEmpty()) {
                    FluidKey key = new FluidKey(fs.getFluid(), fs.getComponentsPatch());
                    if (firstKey == null || firstKey.equals(key)) {
                        firstKey = key;
                        freeOrSame++;
                    }
                }
            }
        }
        return freeOrSame * 64 * 1000;
    }

    private static FluidStack drainInternal(
            List<ItemStack> inventory,
            int[] slots,
            FluidKey targetKey,
            int requestedDrain,
            FluidAction action) {
        List<Integer> emptySlots = new ArrayList<>();
        List<Integer> capsuleSlots = new ArrayList<>();
        int existingAmount = 0;
        int max = slots == null ? inventory.size() : slots.length;

        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) {
                emptySlots.add(slot);
            } else {
                FluidStack fs = extractCapsuleFluid(stack);
                if (!fs.isEmpty() && targetKey.equals(new FluidKey(fs.getFluid(), fs.getComponentsPatch()))) {
                    capsuleSlots.add(slot);
                    existingAmount += stack.getCount() * fs.getAmount();
                }
            }
        }
        if (existingAmount <= 0) {
            return FluidStack.EMPTY;
        }
        int drainedAmount = Math.min(requestedDrain, existingAmount);
        int leftoverAmount = existingAmount - drainedAmount;

        int[] stackSizes = new int[CAPACITIES_DESC.length];
        int rem = leftoverAmount;
        int requiredStacks = 0;
        for (int i = 0; i < CAPACITIES_DESC.length; i++) {
            stackSizes[i] = rem / CAPACITIES_DESC[i];
            rem -= stackSizes[i] * CAPACITIES_DESC[i];
            requiredStacks += (stackSizes[i] + 63) / 64;
        }
        if (requiredStacks > capsuleSlots.size() + emptySlots.size()) {
            return FluidStack.EMPTY;
        }
        if (action.execute()) {
            applyPackedLayout(inventory, capsuleSlots, emptySlots, stackSizes, targetKey);
        }
        return new FluidStack(targetKey.fluid().builtInRegistryHolder(), drainedAmount, targetKey.patch());
    }

    private static void applyPackedLayout(
            List<ItemStack> inventory,
            List<Integer> capsuleSlots,
            List<Integer> emptySlots,
            int[] stackSizes,
            FluidKey key) {
        int totalSlots = capsuleSlots.size() + emptySlots.size();
        int cursor = 0;
        for (int sIdx = 0; sIdx < totalSlots; sIdx++) {
            while (cursor < stackSizes.length && stackSizes[cursor] == 0) {
                cursor++;
            }
            int slot = sIdx < capsuleSlots.size()
                    ? capsuleSlots.get(sIdx)
                    : emptySlots.get(sIdx - capsuleSlots.size());
            if (cursor >= stackSizes.length) {
                inventory.set(slot, ItemStack.EMPTY);
            } else {
                int move = Math.min(64, stackSizes[cursor]);
                inventory.set(slot, createFilledCapsule(cursor, move, key.fluid(), key.patch()));
                stackSizes[cursor] -= move;
                if (stackSizes[cursor] == 0) {
                    cursor++;
                }
            }
        }
    }

    private record FluidKey(Fluid fluid, DataComponentPatch patch) {}
}
