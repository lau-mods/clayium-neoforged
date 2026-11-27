package mods.clayium.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mods.clayium.block.tile.IInventoryFlexibleStackLimit;
import mods.clayium.item.CItems;
import mods.clayium.item.ItemCapsule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class UtilTransfer {
    public static UtilTransfer.IInventorySelector defaultSelector = new UtilTransfer.InventorySelector();
    private static ItemCapsule[] capsules = CItems.itemsCapsule;
    private static int[] capacities = new int[]{1000, 125, 25, 5, 1};
    private static ItemCapsule[] aCapsules = CItems.itemsCapsule;
    private static Map<ItemCapsule, Integer> capacityMap;
    private static Map<Integer, ItemCapsule> capsuleMap;
    private static ItemCapsule[] sortedCapsules;
    private static int[] sortedCapacities;
    private static int[] sortedStackLimits;

    public UtilTransfer() {
        super();
    }

    public static int insert(TileEntity from, int[] fromSlots, ForgeDirection direction, int maxTransfer, Object... metadata) {
        UtilTransfer.IInventorySelector selector = defaultSelector;

        for (Object obj : metadata) {
            if (obj instanceof UtilTransfer.IInventorySelector) {
                selector = (UtilTransfer.IInventorySelector)obj;
            }
        }

        if (!selector.selectInventoryToInsertTo(from, direction)) {
            return maxTransfer;
        } else {
            int fromSide = direction.ordinal();
            int toSide = direction.getOpposite().ordinal();
            IInventory to = selector.getSelectedInventory();
            int[] toSlots = selector.getSlotToInsertTo(direction);
            return transfer((IInventory)from, to, fromSlots, toSlots, fromSide, toSide, maxTransfer);
        }
    }

    public static int transfer(IInventory from, IInventory to, int[] fromSlots, int[] toSlots, int fromSide, int toSide, int maxTransfer) {
        int oldTransfer = maxTransfer;
        ISidedInventory fromSided = from instanceof ISidedInventory ? (ISidedInventory)from : null;
        ISidedInventory toSided = to instanceof ISidedInventory ? (ISidedInventory)to : null;

        try {
            for (int fromSlot : fromSlots) {
                ItemStack fromItem = from.func_70301_a(fromSlot);
                if (fromItem != null && fromItem.field_77994_a > 0 && (fromSided == null || fromSided.func_102008_b(fromSlot, fromItem, fromSide))) {
                    if (fromItem.func_77985_e()) {
                        for (int toSlot : toSlots) {
                            ItemStack toItem = to.func_70301_a(toSlot);
                            if (toItem != null
                                && toItem.field_77994_a > 0
                                && (toSided == null || toSided.func_102007_a(toSlot, fromItem, toSide))
                                && fromItem.func_77969_a(toItem)
                                && ItemStack.func_77970_a(toItem, fromItem)) {
                                int stackLimit = to instanceof IInventoryFlexibleStackLimit
                                    ? ((IInventoryFlexibleStackLimit)to).getInventoryStackLimit(toSlot)
                                    : to.func_70297_j_();
                                int maxSize = Math.min(toItem.func_77976_d(), stackLimit);
                                int maxMove = Math.max(Math.min(maxSize - toItem.field_77994_a, Math.min(maxTransfer, fromItem.field_77994_a)), 0);
                                toItem.field_77994_a += maxMove;
                                maxTransfer -= maxMove;
                                fromItem.field_77994_a -= maxMove;
                                if (fromItem.field_77994_a == 0) {
                                    from.func_70299_a(fromSlot, null);
                                }

                                if (maxTransfer == 0) {
                                    return 0;
                                }

                                if (fromItem.field_77994_a == 0) {
                                    break;
                                }
                            }
                        }
                    }

                    if (fromItem.field_77994_a > 0) {
                        for (int toSlotx : toSlots) {
                            ItemStack toItem = to.func_70301_a(toSlotx);
                            if (toItem == null && to.func_94041_b(toSlotx, fromItem) && (toSided == null || toSided.func_102007_a(toSlotx, fromItem, toSide))) {
                                toItem = fromItem.func_77946_l();
                                int stackLimitx = to instanceof IInventoryFlexibleStackLimit
                                    ? ((IInventoryFlexibleStackLimit)to).getInventoryStackLimit(toSlotx)
                                    : to.func_70297_j_();
                                int stackSize = Math.min(Math.min(maxTransfer, fromItem.field_77994_a), stackLimitx);
                                toItem.field_77994_a = stackSize;
                                to.func_70299_a(toSlotx, toItem);
                                maxTransfer -= stackSize;
                                fromItem.field_77994_a -= stackSize;
                                if (fromItem.field_77994_a == 0) {
                                    from.func_70299_a(fromSlot, null);
                                }

                                if (maxTransfer == 0) {
                                    return 0;
                                }

                                if (fromItem.field_77994_a == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (oldTransfer != maxTransfer) {
                to.func_70296_d();
                from.func_70296_d();
            }

            return maxTransfer;
        } finally {
            if (oldTransfer != maxTransfer) {
                to.func_70296_d();
                from.func_70296_d();
            }
        }
    }

    public static int insert(TileEntity from, int[] fromSlots, UtilDirection front, UtilDirection side, int maxTransfer, Object... metadata) {
        ForgeDirection direction = ForgeDirection.getOrientation(front.getSide(side).ordinal());
        return insert(from, fromSlots, direction, maxTransfer, metadata);
    }

    public static int transfer(TileEntity from, int[] fromSlots, UtilDirection front, UtilDirection side, int maxTransfer, Object... metadata) {
        return insert(from, fromSlots, front, side, maxTransfer, metadata);
    }

    public static int transfer(TileEntity from, int[] fromSlots, ForgeDirection direction, int maxTransfer, Object... metadata) {
        return insert(from, fromSlots, direction, maxTransfer, metadata);
    }

    public static int extract(TileEntity to, int[] toSlots, ForgeDirection direction, int maxTransfer, Object... metadata) {
        UtilTransfer.IInventorySelector selector = defaultSelector;

        for (Object obj : metadata) {
            if (obj instanceof UtilTransfer.IInventorySelector) {
                selector = (UtilTransfer.IInventorySelector)obj;
            }
        }

        if (!selector.selectInventoryToExtractFrom(to, direction)) {
            return maxTransfer;
        } else {
            int toSide = direction.ordinal();
            int fromSide = direction.getOpposite().ordinal();
            IInventory from = selector.getSelectedInventory();
            int[] fromSlots = selector.getSlotToExtractFrom(direction);
            return transfer(from, (IInventory)to, fromSlots, toSlots, fromSide, toSide, maxTransfer);
        }
    }

    public static int extract(TileEntity from, int[] fromSlots, UtilDirection front, UtilDirection side, int maxTransfer, Object... metadata) {
        ForgeDirection direction = ForgeDirection.getOrientation(front.getSide(side).ordinal());
        return extract(from, fromSlots, direction, maxTransfer, metadata);
    }

    public static ItemStack produceItemStack(ItemStack itemstack, ItemStack[] inventory, int index, int inventoryStackLimit) {
        if (itemstack == null) {
            return null;
        } else {
            ItemStack res = itemstack.func_77946_l();
            int stackSize = itemstack.field_77994_a;
            if (inventory[index] == null) {
                inventory[index] = itemstack.func_77946_l();
                inventory[index].field_77994_a = Math.min(itemstack.field_77994_a, inventoryStackLimit);
                res.field_77994_a = res.field_77994_a - inventory[index].field_77994_a;
            } else if (inventory[index].func_77969_a(itemstack) && ItemStack.func_77970_a(inventory[index], itemstack)) {
                int a = Math.min(itemstack.field_77994_a, inventory[index].func_77976_d() - inventory[index].field_77994_a);
                inventory[index].field_77994_a += a;
                res.field_77994_a -= a;
            }

            if (res.field_77994_a <= 0) {
                res = null;
            }

            return res;
        }
    }

    public static ItemStack produceItemStack(ItemStack itemstack, ItemStack[] inventory, int i, int j, int inventoryStackLimit) {
        if (itemstack == null) {
            return null;
        } else {
            ItemStack res = itemstack.func_77946_l();

            for (int k = i; k < j; k++) {
                if (inventory[k] != null) {
                    res = produceItemStack(res, inventory, k, inventoryStackLimit);
                }
            }

            for (int kx = i; kx < j; kx++) {
                if (inventory[kx] == null) {
                    res = produceItemStack(res, inventory, kx, inventoryStackLimit);
                }
            }

            return res;
        }
    }

    public static ItemStack[] produceItemStacks(ItemStack[] itemstacks, ItemStack[] inventory, int i, int j, int inventoryStackLimit) {
        if (itemstacks == null) {
            return null;
        } else {
            for (int k = 0; k < itemstacks.length; k++) {
                itemstacks[k] = produceItemStack(itemstacks[k], inventory, i, j, inventoryStackLimit);
            }

            return itemstacks;
        }
    }

    public static int canProduceItemStack(ItemStack itemstack, ItemStack[] inventory, int index, int inventoryStackLimit) {
        if (itemstack == null) {
            return 0;
        } else {
            ItemStack res = itemstack.func_77946_l();
            int stackSize = itemstack.field_77994_a;
            if (inventory[index] == null) {
                return inventoryStackLimit;
            } else {
                return inventory[index].func_77969_a(itemstack) && ItemStack.func_77970_a(inventory[index], itemstack)
                    ? inventory[index].func_77976_d() - inventory[index].field_77994_a
                    : 0;
            }
        }
    }

    public static int canProduceItemStack(ItemStack itemstack, ItemStack[] inventory, int i, int j, int inventoryStackLimit) {
        int rest = 0;

        for (int k = i; k < j; k++) {
            rest += canProduceItemStack(itemstack, inventory, k, inventoryStackLimit);
        }

        return rest;
    }

    public static boolean canProduceItemStacks(ItemStack[] itemstacks, ItemStack[] inventory, int i, int j, int inventoryStackLimit) {
        if (itemstacks == null) {
            return true;
        } else {
            ItemStack[] copyItemstacks = getHardCopy(itemstacks);
            ItemStack[] copyInventory = getHardCopy(inventory);

            for (int k = 0; k < copyItemstacks.length; k++) {
                if (produceItemStack(copyItemstacks[k], copyInventory, i, j, inventoryStackLimit) != null) {
                    return false;
                }
            }

            return true;
        }
    }

    public static ItemStack[] getHardCopy(ItemStack[] itemstacks) {
        if (itemstacks == null) {
            return null;
        } else {
            ItemStack[] res = new ItemStack[itemstacks.length];

            for (int i = 0; i < res.length; i++) {
                if (itemstacks[i] != null) {
                    res[i] = itemstacks[i].func_77946_l();
                }
            }

            return res;
        }
    }

    public static int countItemStack(ItemStack itemstack, ItemStack[] inventory, int i, int j) {
        int res = 0;

        for (int k = i; k < j; k++) {
            if (inventory[k] != null && inventory[k].func_77969_a(itemstack) && ItemStack.func_77970_a(inventory[k], itemstack)) {
                res += inventory[k].field_77994_a;
            }
        }

        return res;
    }

    public static ItemStack consumeItemStack(ItemStack itemstack, ItemStack[] inventory, int i, int j) {
        int stackSize = itemstack.field_77994_a;

        for (int k = i; k < j; k++) {
            if (inventory[k] != null && inventory[k].func_77969_a(itemstack) && ItemStack.func_77970_a(inventory[k], itemstack)) {
                int n = Math.min(stackSize, inventory[k].field_77994_a);
                stackSize -= n;
                inventory[k].field_77994_a -= n;
                if (inventory[k].field_77994_a <= 0) {
                    inventory[k] = null;
                }
            }
        }

        if (stackSize <= 0) {
            return null;
        } else {
            ItemStack res = itemstack.func_77946_l();
            res.field_77994_a = stackSize;
            return res;
        }
    }

    public static ItemStack[] consumeItemStack(ItemStack[] itemstack, ItemStack[] inventory, int i, int j) {
        ItemStack[] res = new ItemStack[itemstack.length];

        for (int k = 0; k < itemstack.length; k++) {
            res[k] = consumeItemStack(itemstack[k], inventory, i, j);
        }

        return res;
    }

    public static int insertToTank(TileEntity fromTe, int[] fromSlots, ForgeDirection direction, int maxTransfer) {
        int oldTransfer = maxTransfer;
        int fromSide = direction.ordinal();
        int toSide = direction.getOpposite().ordinal();
        ForgeDirection toDirection = direction.getOpposite();
        World world = fromTe.func_145831_w();
        IInventory from = (IInventory)fromTe;
        ISidedInventory fromSided = from instanceof ISidedInventory ? (ISidedInventory)from : null;
        TileEntity te = UtilDirection.getTileEntity(world, fromTe.field_145851_c, fromTe.field_145848_d, fromTe.field_145849_e, direction);
        if (!(te instanceof IFluidHandler)) {
            return maxTransfer;
        } else {
            IFluidHandler to = (IFluidHandler)te;
            Block block = UtilDirection.getBlock(world, fromTe.field_145851_c, fromTe.field_145848_d, fromTe.field_145849_e, direction);

            for (int fromSlot : fromSlots) {
                ItemStack fromItem = from.func_70301_a(fromSlot);
                if (fromItem != null
                    && fromItem.field_77994_a > 0
                    && fromItem.func_77973_b() instanceof ItemCapsule
                    && (fromSided == null || fromSided.func_102008_b(fromSlot, fromItem, fromSide))) {
                    ItemCapsule capsule = (ItemCapsule)fromItem.func_77973_b();
                    FluidStack fluidStack = capsule.getFluidStackFromItemStack(fromItem);
                    if (fluidStack != null) {
                        int capacity = fluidStack.amount;
                        if (fromItem.func_77942_o()) {
                            fluidStack.tag = fromItem.func_77978_p();
                        }

                        if (to.canFill(toDirection, fluidStack.getFluid()) && fromItem.func_77985_e()) {
                            int result = to.fill(toDirection, fluidStack, false);
                            if (result >= capacity) {
                                int maxMove = Math.max(Math.min(result / capacity, Math.min(maxTransfer, fromItem.field_77994_a)), 0);
                                fluidStack.amount = maxMove * capacity;
                                result = to.fill(toDirection, fluidStack, false);
                                if (result >= maxMove * capacity) {
                                    to.fill(toDirection, fluidStack, true);
                                    maxTransfer -= maxMove;
                                    fromItem.field_77994_a -= maxMove;
                                    if (fromItem.field_77994_a == 0) {
                                        from.func_70299_a(fromSlot, null);
                                    }

                                    if (maxTransfer == 0) {
                                        return 0;
                                    }

                                    if (fromItem.field_77994_a == 0) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (oldTransfer != maxTransfer) {
                if (to instanceof TileEntity) {
                    ((TileEntity)to).func_70296_d();
                }

                from.func_70296_d();
            }

            return maxTransfer;
        }
    }

    public static int insertToTank(TileEntity from, int[] fromSlots, UtilDirection front, UtilDirection side, int maxTransfer) {
        ForgeDirection direction = ForgeDirection.getOrientation(front.getSide(side).ordinal());
        return insertToTank(from, fromSlots, direction, maxTransfer);
    }

    public static int extractFromTank(TileEntity toTe, int[] toSlots, ForgeDirection direction, int maxTransfer) {
        int oldTransfer = maxTransfer;
        int toSide = direction.ordinal();
        int fromSide = direction.getOpposite().ordinal();
        ForgeDirection fromDirection = direction.getOpposite();
        World world = toTe.func_145831_w();
        IInventory to = (IInventory)toTe;
        ISidedInventory toSided = to instanceof ISidedInventory ? (ISidedInventory)to : null;
        TileEntity te = UtilDirection.getTileEntity(world, toTe.field_145851_c, toTe.field_145848_d, toTe.field_145849_e, direction);
        if (!(te instanceof IFluidHandler)) {
            return 0;
        } else {
            IFluidHandler from = (IFluidHandler)te;
            FluidTankInfo[] infos = from.getTankInfo(fromDirection);
            if (infos == null) {
                return 0;
            } else {
                for (FluidTankInfo info : infos) {
                    FluidStack fluidStack = info.fluid;
                    if (fluidStack != null) {
                        Fluid fluid = fluidStack.getFluid();
                        int fluidId = UtilFluid.getFluidID(fluidStack);
                        if (from.canDrain(direction.getOpposite(), fluid)) {
                            FluidStack toSimulate = fluidStack.copy();
                            toSimulate.amount = Integer.MAX_VALUE;
                            FluidStack result = from.drain(fromDirection, toSimulate, false);
                            int amount = result == null ? fluidStack.amount : result.amount;

                            for (int i = 0; i < capsules.length; i++) {
                                if (amount >= capacities[i]) {
                                    ItemStack fromItem = new ItemStack(capsules[i], 1, fluidId);
                                    if (fluidStack.tag != null) {
                                        fromItem.func_77982_d((NBTTagCompound)fluidStack.tag.func_74737_b());
                                    }

                                    for (int toSlot : toSlots) {
                                        ItemStack toItem = to.func_70301_a(toSlot);
                                        if (toItem != null
                                            && toItem.field_77994_a > 0
                                            && (toSided == null || toSided.func_102007_a(toSlot, fromItem, toSide))
                                            && fromItem.func_77969_a(toItem)
                                            && ItemStack.func_77970_a(toItem, fromItem)) {
                                            int stackLimit = to instanceof IInventoryFlexibleStackLimit
                                                ? ((IInventoryFlexibleStackLimit)to).getInventoryStackLimit(toSlot)
                                                : to.func_70297_j_();
                                            int maxSize = Math.min(toItem.func_77976_d(), stackLimit);
                                            int maxMove = Math.max(Math.min(maxSize - toItem.field_77994_a, Math.min(maxTransfer, amount / capacities[i])), 0);
                                            toSimulate.amount = maxMove * capacities[i];
                                            result = from.drain(fromDirection, toSimulate, false);
                                            if (result != null && result.amount >= maxMove * capacities[i]) {
                                                from.drain(fromDirection, result, true);
                                                toItem.field_77994_a += maxMove;
                                                maxTransfer -= maxMove;
                                                amount -= maxMove * capacities[i];
                                                if (maxTransfer == 0) {
                                                    return 0;
                                                }

                                                if (amount < capacities[i]) {
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (amount >= capacities[i]) {
                                        for (int toSlotx : toSlots) {
                                            ItemStack toItem = to.func_70301_a(toSlotx);
                                            if (toItem == null
                                                && to.func_94041_b(toSlotx, fromItem)
                                                && (toSided == null || toSided.func_102007_a(toSlotx, fromItem, toSide))) {
                                                toItem = fromItem.func_77946_l();
                                                int stackLimit = to instanceof IInventoryFlexibleStackLimit
                                                    ? ((IInventoryFlexibleStackLimit)to).getInventoryStackLimit(toSlotx)
                                                    : to.func_70297_j_();
                                                int stackSize = Math.min(Math.min(maxTransfer, amount / capacities[i]), stackLimit);
                                                toSimulate.amount = stackSize * capacities[i];
                                                result = from.drain(fromDirection, toSimulate, false);
                                                if (result != null && result.amount >= stackSize * capacities[i]) {
                                                    from.drain(fromDirection, result, true);
                                                    toItem.field_77994_a = stackSize;
                                                    to.func_70299_a(toSlotx, toItem);
                                                    maxTransfer -= stackSize;
                                                    amount -= stackSize * capacities[i];
                                                    if (maxTransfer == 0) {
                                                        return 0;
                                                    }

                                                    if (amount < capacities[i]) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (oldTransfer != maxTransfer) {
                                    to.func_70296_d();
                                    if (from instanceof TileEntity) {
                                        ((TileEntity)from).func_70296_d();
                                    }
                                }
                            }
                        }
                    }
                }

                return maxTransfer;
            }
        }
    }

    public static int extractFromTank(TileEntity to, int[] toSlots, UtilDirection front, UtilDirection side, int maxTransfer) {
        ForgeDirection direction = ForgeDirection.getOrientation(front.getSide(side).ordinal());
        return extractFromTank(to, toSlots, direction, maxTransfer);
    }

    public static void initFluidParams() {
        capacityMap = new HashMap<>();
        capsuleMap = new HashMap<>();
        int[] capacities = new int[aCapsules.length];

        for (int i = 0; i < aCapsules.length; i++) {
            capacities[i] = aCapsules[i].getCapacity();
            capacityMap.put(aCapsules[i], capacities[i]);
            capsuleMap.put(capacities[i], aCapsules[i]);
        }

        Arrays.sort(capacities);
        sortedCapacities = new int[capacities.length];

        for (int i = 0; i < sortedCapacities.length; i++) {
            sortedCapacities[i] = capacities[sortedCapacities.length - 1 - i];
        }

        sortedCapsules = new ItemCapsule[sortedCapacities.length];
        sortedStackLimits = new int[sortedCapacities.length];

        for (int i = 0; i < sortedCapsules.length; i++) {
            sortedCapsules[i] = capsuleMap.get(sortedCapacities[i]);
            sortedStackLimits[i] = sortedCapsules[i].func_77639_j();
        }
    }

    public static void sortFluid(ItemStack[] inventory, int[] slots) {
        if (capacityMap == null) {
            initFluidParams();
        }

        List<Integer> emptySlots = new ArrayList<>();
        Map<UtilTransfer.CapsuleKey, List<Integer>> capsuleSlots = new HashMap<>();
        Map<UtilTransfer.CapsuleKey, Integer> amounts = new HashMap<>();
        boolean slotsIsNull = slots == null;
        int iMax = slotsIsNull ? inventory.length : slots.length;
        ItemStack itemstack = null;
        int slot = -1;
        Item item = null;

        for (int i = 0; i < iMax; i++) {
            if (slotsIsNull) {
                slot = i;
                itemstack = inventory[i];
            } else {
                slot = slots[i];
                itemstack = inventory[slot];
            }

            if (itemstack == null) {
                emptySlots.add(slot);
            } else if ((item = itemstack.func_77973_b()) instanceof ItemCapsule) {
                UtilTransfer.CapsuleKey key = new UtilTransfer.CapsuleKey(itemstack);
                List<Integer> list = capsuleSlots.get(key);
                int amount = 0;
                if (list == null) {
                    list = new ArrayList<>();
                    capsuleSlots.put(key, list);
                } else {
                    amount = amounts.get(key);
                }

                list.add(slot);
                amount += itemstack.field_77994_a * capacityMap.get(item);
                amounts.put(key, amount);
            }
        }

        for (Entry<UtilTransfer.CapsuleKey, List<Integer>> entry : capsuleSlots.entrySet()) {
            UtilTransfer.CapsuleKey key = entry.getKey();
            slot = amounts.get(key);
            List<Integer> slots1 = entry.getValue();
            int[] stackSizes = new int[sortedCapsules.length];
            int stackNum = 0;

            for (int i = 0; i < sortedCapsules.length; i++) {
                stackSizes[i] = slot / sortedCapacities[i];
                slot -= stackSizes[i] * sortedCapacities[i];
                stackNum += (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
            }

            if (stackNum <= slots1.size() + emptySlots.size()) {
                boolean tagIsNull = key.tag == null;
                int slotsSize = slots1.size();
                int emptySlotsSize = emptySlots.size();
                int slotMax = slotsSize + emptySlotsSize;
                int slotx = 0;
                List<Integer> toAdd = new ArrayList<>();
                List<Integer> toRemove = new ArrayList<>();
                int i = 0;

                for (int sloti = 0; sloti < slotMax; sloti++) {
                    while (i < stackSizes.length && stackSizes[i] == 0) {
                        i++;
                    }

                    if (sloti < slotsSize) {
                        slotx = slots1.get(sloti);
                    } else {
                        slotx = emptySlots.get(sloti - slotsSize);
                    }

                    if (i >= stackSizes.length) {
                        inventory[slotx] = null;
                        if (sloti < slotsSize) {
                            toAdd.add(slotx);
                        }
                    } else {
                        if (sloti >= slotsSize) {
                            toRemove.add(slotx);
                        }

                        if (stackSizes[i] > sortedStackLimits[i]) {
                            inventory[slotx] = new ItemStack(sortedCapsules[i], sortedStackLimits[i], key.damage);
                            if (!tagIsNull) {
                                inventory[slotx].func_77982_d(key.tag);
                            }

                            stackSizes[i] -= sortedStackLimits[i];
                        } else {
                            inventory[slotx] = new ItemStack(sortedCapsules[i], stackSizes[i], key.damage);
                            if (!tagIsNull) {
                                inventory[slotx].func_77982_d(key.tag);
                            }

                            i++;
                        }
                    }
                }

                emptySlots.addAll(toAdd);
                emptySlots.removeAll(toRemove);
            }
        }
    }

    public static int fillFluid(ItemStack[] inventory, int[] slots, FluidStack resource, boolean doFill) {
        if (capacityMap == null) {
            initFluidParams();
        }

        if (resource == null) {
            return 0;
        } else {
            Fluid resourceFluid = resource.getFluid();
            int resourceFluidId = UtilFluid.getFluidID(resourceFluid);
            List<Integer> emptySlots = new ArrayList<>();
            List<Integer> capsuleSlots = new ArrayList<>();
            int amount = 0;
            boolean slotsIsNull = slots == null;
            int iMax = slotsIsNull ? inventory.length : slots.length;
            ItemStack itemstack = null;
            int slot = -1;
            Item item = null;
            boolean resourceHasTag = resource.tag != null;

            for (int i = 0; i < iMax; i++) {
                if (slotsIsNull) {
                    slot = i;
                    itemstack = inventory[i];
                } else {
                    slot = slots[i];
                    itemstack = inventory[slot];
                }

                if (itemstack == null) {
                    emptySlots.add(slot);
                } else if ((item = itemstack.func_77973_b()) instanceof ItemCapsule
                    && resourceFluidId == itemstack.func_77960_j()
                    && (!resourceHasTag && !itemstack.func_77942_o() || resourceHasTag && resource.tag.equals(itemstack.func_77978_p()))) {
                    capsuleSlots.add(slot);
                    amount += itemstack.field_77994_a * capacityMap.get(item);
                }
            }

            amount += resource.amount;
            int[] stackSizes = new int[sortedCapsules.length];
            int stackNum = 0;

            for (int i = 0; i < sortedCapsules.length; i++) {
                stackSizes[i] = amount / sortedCapacities[i];
                int stackN = (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
                if (stackN + stackNum > capsuleSlots.size() + emptySlots.size()) {
                    stackSizes[i] = (capsuleSlots.size() + emptySlots.size() - stackNum) * sortedStackLimits[i];
                    amount -= stackSizes[i] * sortedCapacities[i];
                    break;
                }

                amount -= stackSizes[i] * sortedCapacities[i];
                stackNum += (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
            }

            if (!doFill) {
                return resource.amount - amount;
            } else {
                boolean tagIsNull = resource.tag == null;
                int slotsSize = capsuleSlots.size();
                resourceHasTag = (boolean)emptySlots.size();
                int slotMax = slotsSize + resourceHasTag;
                int slotx = 0;
                int i = 0;

                for (int sloti = 0; sloti < slotMax; sloti++) {
                    while (i < stackSizes.length && stackSizes[i] == 0) {
                        i++;
                    }

                    if (sloti < slotsSize) {
                        slotx = capsuleSlots.get(sloti);
                    } else {
                        slotx = emptySlots.get(sloti - slotsSize);
                    }

                    if (i >= stackSizes.length) {
                        inventory[slotx] = null;
                    } else if (stackSizes[i] > sortedStackLimits[i]) {
                        inventory[slotx] = new ItemStack(sortedCapsules[i], sortedStackLimits[i], resourceFluidId);
                        if (!tagIsNull) {
                            inventory[slotx].func_77982_d(resource.tag);
                        }

                        stackSizes[i] -= sortedStackLimits[i];
                    } else {
                        inventory[slotx] = new ItemStack(sortedCapsules[i], stackSizes[i], resourceFluidId);
                        if (!tagIsNull) {
                            inventory[slotx].func_77982_d(resource.tag);
                        }

                        i++;
                    }
                }

                return resource.amount - amount;
            }
        }
    }

    public static FluidStack drainFluid(ItemStack[] inventory, int[] slots, FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        } else {
            Fluid resourceFluid = resource.getFluid();
            int resourceFluidId = UtilFluid.getFluidID(resourceFluid);
            FluidStack ret = resource.copy();
            List<Integer> emptySlots = new ArrayList<>();
            List<Integer> capsuleSlots = new ArrayList<>();
            int amount = 0;
            boolean resourceHasTag = resource.tag != null;
            boolean slotsIsNull = slots == null;
            int iMax = slotsIsNull ? inventory.length : slots.length;
            ItemStack itemstack = null;
            int slot = -1;
            Item item = null;

            for (int i = 0; i < iMax; i++) {
                if (slotsIsNull) {
                    slot = i;
                    itemstack = inventory[i];
                } else {
                    slot = slots[i];
                    itemstack = inventory[slot];
                }

                if (itemstack == null) {
                    emptySlots.add(slot);
                } else if ((item = itemstack.func_77973_b()) instanceof ItemCapsule
                    && resourceFluidId == itemstack.func_77960_j()
                    && (!resourceHasTag && !itemstack.func_77942_o() || resourceHasTag && resource.tag.equals(itemstack.func_77978_p()))) {
                    capsuleSlots.add(slot);
                    amount += itemstack.field_77994_a * capacityMap.get(item);
                }
            }

            ret.amount = Math.min(resource.amount, amount);
            amount -= ret.amount;
            int[] stackSizes = new int[sortedCapsules.length];
            int stackNum = 0;

            for (int i = 0; i < sortedCapsules.length; i++) {
                stackSizes[i] = amount / sortedCapacities[i];
                int stackN = (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
                if (stackN + stackNum > capsuleSlots.size() + emptySlots.size()) {
                    return null;
                }

                amount -= stackSizes[i] * sortedCapacities[i];
                stackNum += (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
            }

            if (!doDrain) {
                return ret;
            } else {
                slot = capsuleSlots.size();
                int emptySlotsSize = emptySlots.size();
                int slotMax = slot + emptySlotsSize;
                int slotx = 0;
                int i = 0;

                for (int sloti = 0; sloti < slotMax; sloti++) {
                    while (i < stackSizes.length && stackSizes[i] == 0) {
                        i++;
                    }

                    if (sloti < slot) {
                        slotx = capsuleSlots.get(sloti);
                    } else {
                        slotx = emptySlots.get(sloti - slot);
                    }

                    if (i >= stackSizes.length) {
                        inventory[slotx] = null;
                    } else if (stackSizes[i] > sortedStackLimits[i]) {
                        inventory[slotx] = new ItemStack(sortedCapsules[i], sortedStackLimits[i], resourceFluidId);
                        if (resourceHasTag) {
                            inventory[slotx].func_77982_d(resource.tag);
                        }

                        stackSizes[i] -= sortedStackLimits[i];
                    } else {
                        inventory[slotx] = new ItemStack(sortedCapsules[i], stackSizes[i], resourceFluidId);
                        if (resourceHasTag) {
                            inventory[slotx].func_77982_d(resource.tag);
                        }

                        i++;
                    }
                }

                return ret;
            }
        }
    }

    public static FluidStack drainFluid(ItemStack[] inventory, int[] slots, int maxDrain, boolean doDrain) {
        if (capacityMap == null) {
            initFluidParams();
        }

        if (maxDrain == 0) {
            return null;
        } else {
            Fluid resourceFluid = null;
            int resourceFluidId = -1;
            FluidStack ret = null;
            List<Integer> emptySlots = new ArrayList<>();
            List<Integer> capsuleSlots = new ArrayList<>();
            int amount = 0;
            boolean resourceHasTag = false;
            boolean slotsIsNull = slots == null;
            int iMax = slotsIsNull ? inventory.length : slots.length;
            ItemStack itemstack = null;
            int slot = -1;
            Item item = null;

            for (int i = 0; i < iMax; i++) {
                if (slotsIsNull) {
                    slot = i;
                    itemstack = inventory[i];
                } else {
                    slot = slots[i];
                    itemstack = inventory[slot];
                }

                if (itemstack == null) {
                    emptySlots.add(slot);
                } else if ((item = itemstack.func_77973_b()) instanceof ItemCapsule) {
                    if (ret == null) {
                        resourceFluidId = itemstack.func_77960_j();
                        resourceFluid = UtilFluid.getFluid(resourceFluidId);
                        if (resourceFluid != null) {
                            ret = new FluidStack(resourceFluid, maxDrain);
                            resourceHasTag = itemstack.func_77942_o();
                            if (resourceHasTag) {
                                ret.tag = itemstack.func_77978_p();
                            }
                        } else {
                            resourceFluidId = -1;
                        }
                    }

                    if (ret != null
                        && resourceFluidId == itemstack.func_77960_j()
                        && (!resourceHasTag && !itemstack.func_77942_o() || resourceHasTag && ret.tag.equals(itemstack.func_77978_p()))) {
                        capsuleSlots.add(slot);
                        amount += itemstack.field_77994_a * capacityMap.get(item);
                    }
                }
            }

            if (ret == null) {
                return null;
            } else {
                ret.amount = Math.min(maxDrain, amount);
                amount -= ret.amount;
                int[] stackSizes = new int[sortedCapsules.length];
                int stackNum = 0;

                for (int i = 0; i < sortedCapsules.length; i++) {
                    stackSizes[i] = amount / sortedCapacities[i];
                    int stackN = (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
                    if (stackN + stackNum > capsuleSlots.size() + emptySlots.size()) {
                        return null;
                    }

                    amount -= stackSizes[i] * sortedCapacities[i];
                    stackNum += (stackSizes[i] + sortedStackLimits[i] - 1) / sortedStackLimits[i];
                }

                if (!doDrain) {
                    return ret;
                } else {
                    slot = capsuleSlots.size();
                    int emptySlotsSize = emptySlots.size();
                    int slotMax = slot + emptySlotsSize;
                    int slotx = 0;
                    int i = 0;

                    for (int sloti = 0; sloti < slotMax; sloti++) {
                        while (i < stackSizes.length && stackSizes[i] == 0) {
                            i++;
                        }

                        if (sloti < slot) {
                            slotx = capsuleSlots.get(sloti);
                        } else {
                            slotx = emptySlots.get(sloti - slot);
                        }

                        if (i >= stackSizes.length) {
                            inventory[slotx] = null;
                        } else if (stackSizes[i] > sortedStackLimits[i]) {
                            inventory[slotx] = new ItemStack(sortedCapsules[i], sortedStackLimits[i], resourceFluidId);
                            if (resourceHasTag) {
                                inventory[slotx].func_77982_d(ret.tag);
                            }

                            stackSizes[i] -= sortedStackLimits[i];
                        } else {
                            inventory[slotx] = new ItemStack(sortedCapsules[i], stackSizes[i], resourceFluidId);
                            if (resourceHasTag) {
                                inventory[slotx].func_77982_d(ret.tag);
                            }

                            i++;
                        }
                    }

                    return ret;
                }
            }
        }
    }

    public static boolean canFillFluid(ItemStack[] inventory, int[] slots, Fluid fluid) {
        if (capacityMap == null) {
            initFluidParams();
        }

        if (fluid == null) {
            return false;
        } else {
            int amount = 0;
            int slotNum = 0;
            int emptySlotNum = 0;
            int fluidId = UtilFluid.getFluidID(fluid);
            boolean slotsIsNull = slots == null;
            int iMax = slotsIsNull ? inventory.length : slots.length;
            ItemStack itemstack = null;
            int slot = -1;
            Item item = null;

            for (int i = 0; i < iMax; i++) {
                if (slotsIsNull) {
                    itemstack = inventory[i];
                } else {
                    slot = slots[i];
                    itemstack = inventory[slot];
                }

                if (itemstack == null) {
                    emptySlotNum++;
                } else if ((item = itemstack.func_77973_b()) instanceof ItemCapsule && itemstack.func_77960_j() == fluidId) {
                    amount += itemstack.field_77994_a * capacityMap.get(item);
                    slotNum++;
                }
            }

            return amount <= (slotNum + emptySlotNum) * sortedCapacities[0] * sortedStackLimits[0];
        }
    }

    public static boolean canDrainFluid(ItemStack[] inventory, int[] slots, Fluid fluid) {
        if (capacityMap == null) {
            initFluidParams();
        }

        if (fluid == null) {
            return false;
        } else {
            int fluidId = UtilFluid.getFluidID(fluid);
            boolean slotsIsNull = slots == null;
            int iMax = slotsIsNull ? inventory.length : slots.length;
            ItemStack itemstack = null;
            int slot = -1;

            for (int i = 0; i < iMax; i++) {
                if (slotsIsNull) {
                    itemstack = inventory[i];
                } else {
                    slot = slots[i];
                    itemstack = inventory[slot];
                }

                if (itemstack != null && itemstack.func_77973_b() instanceof ItemCapsule && itemstack.func_77960_j() == fluidId) {
                    return true;
                }
            }

            return false;
        }
    }

    public static FluidTankInfo[] getTankInfo(ItemStack[] inventory, int[] slots) {
        if (capacityMap == null) {
            initFluidParams();
        }

        Map<UtilTransfer.CapsuleKey, Integer> amounts = new HashMap<>();
        Map<UtilTransfer.CapsuleKey, Integer> slotNums = new HashMap<>();
        int emptySlotNum = 0;
        boolean slotsIsNull = slots == null;
        int iMax = slotsIsNull ? inventory.length : slots.length;
        ItemStack itemstack = null;
        int slot = -1;
        Item item = null;

        for (int i = 0; i < iMax; i++) {
            if (slotsIsNull) {
                itemstack = inventory[i];
            } else {
                slot = slots[i];
                itemstack = inventory[slot];
            }

            if (itemstack == null) {
                emptySlotNum++;
            } else if ((item = itemstack.func_77973_b()) instanceof ItemCapsule) {
                UtilTransfer.CapsuleKey key = new UtilTransfer.CapsuleKey(itemstack);
                if (UtilFluid.getFluid(key.damage) != null) {
                    int amount = 0;
                    int slotNum = 0;
                    if (amounts.containsKey(key)) {
                        amount = amounts.get(key);
                        slotNum = slotNums.get(key);
                    }

                    amount += itemstack.field_77994_a * capacityMap.get(item);
                    amounts.put(key, amount);
                    slotNums.put(key, ++slotNum);
                }
            }
        }

        FluidTankInfo[] ret = null;
        if (amounts.size() == 0) {
            return new FluidTankInfo[]{new FluidTankInfo(null, emptySlotNum * sortedCapacities[0] * sortedStackLimits[0])};
        } else {
            FluidTankInfo[] var15 = new FluidTankInfo[amounts.size()];
            iMax = 0;

            for (Entry<UtilTransfer.CapsuleKey, Integer> entry : amounts.entrySet()) {
                UtilTransfer.CapsuleKey key = entry.getKey();
                int amount = entry.getValue();
                FluidStack fluid = new FluidStack(UtilFluid.getFluid(key.damage), amount);
                if (key.tag != null) {
                    fluid.tag = key.tag;
                }

                var15[iMax] = new FluidTankInfo(fluid, (slotNums.get(key) + emptySlotNum) * sortedCapacities[0] * sortedStackLimits[0]);
                iMax++;
            }

            return var15;
        }
    }

    public static class CapsuleKey {
        int damage = -1;
        NBTTagCompound tag = null;

        public CapsuleKey(ItemStack itemstack) {
            super();
            this.damage = itemstack.func_77960_j();
            this.tag = itemstack.func_77942_o() ? itemstack.func_77978_p() : null;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + this.damage;
            return 31 * result + (this.tag == null ? 0 : this.tag.hashCode());
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                UtilTransfer.CapsuleKey other = (UtilTransfer.CapsuleKey)obj;
                if (this.damage != other.damage) {
                    return false;
                } else {
                    if (this.tag == null) {
                        if (other.tag != null) {
                            return false;
                        }
                    } else if (!this.tag.equals(other.tag)) {
                        return false;
                    }

                    return true;
                }
            }
        }
    }

    interface IInventorySelector {
        IInventory getSelectedInventory();

        boolean selectInventoryToInsertTo(TileEntity var1, ForgeDirection var2);

        int[] getSlotToInsertTo(ForgeDirection var1);

        boolean selectInventoryToExtractFrom(TileEntity var1, ForgeDirection var2);

        int[] getSlotToExtractFrom(ForgeDirection var1);
    }

    public static class InventorySelector implements UtilTransfer.IInventorySelector {
        protected IInventory selected = null;

        public InventorySelector() {
            super();
        }

        public IInventory getSelectedInventory() {
            return this.selected;
        }

        public boolean selectInventoryToInsertTo(TileEntity from, ForgeDirection direction) {
            IInventory to = this.selectInventoryToInsertTo(from.func_145831_w(), from.field_145851_c, from.field_145848_d, from.field_145849_e, direction);
            if (to == null) {
                return false;
            } else {
                this.selected = to;
                return true;
            }
        }

        public IInventory selectInventoryToInsertTo(World world, int fromX, int fromY, int fromZ, ForgeDirection direction) {
            TileEntity te = UtilDirection.getTileEntity(world, fromX, fromY, fromZ, direction);
            if (!(te instanceof IInventory)) {
                return null;
            } else {
                IInventory to = (IInventory)te;
                Block block = UtilDirection.getBlock(world, fromX, fromY, fromZ, direction);
                if (block instanceof BlockChest) {
                    IInventory chest = ((BlockChest)block)
                        .func_149951_m(world, fromX + direction.offsetX, fromY + direction.offsetY, fromZ + direction.offsetZ);
                    if (chest != null) {
                        to = chest;
                    }
                }

                return to;
            }
        }

        public int[] getSlotToInsertTo(ForgeDirection direction) {
            if (this.selected == null) {
                return null;
            } else {
                int toSide = direction.getOpposite().ordinal();
                int[] toSlots = null;
                if (!(this.selected instanceof ISidedInventory)) {
                    toSlots = new int[this.selected.func_70302_i_()];
                    int i = 0;

                    while (i < this.selected.func_70302_i_()) {
                        toSlots[i] = i++;
                    }
                } else {
                    toSlots = ((ISidedInventory)this.selected).func_94128_d(toSide);
                }

                return toSlots;
            }
        }

        public boolean selectInventoryToExtractFrom(TileEntity to, ForgeDirection direction) {
            IInventory from = this.selectInventoryToExtractFrom(to.func_145831_w(), to.field_145851_c, to.field_145848_d, to.field_145849_e, direction);
            if (from == null) {
                return false;
            } else {
                this.selected = from;
                return true;
            }
        }

        public IInventory selectInventoryToExtractFrom(World world, int toX, int toY, int toZ, ForgeDirection direction) {
            TileEntity te = UtilDirection.getTileEntity(world, toX, toY, toZ, direction);
            if (!(te instanceof IInventory)) {
                return null;
            } else {
                IInventory from = (IInventory)te;
                Block block = UtilDirection.getBlock(world, toX, toY, toZ, direction);
                if (block instanceof BlockChest) {
                    IInventory chest = ((BlockChest)block).func_149951_m(world, toX + direction.offsetX, toY + direction.offsetY, toZ + direction.offsetZ);
                    if (chest != null) {
                        from = chest;
                    }
                }

                return from;
            }
        }

        public int[] getSlotToExtractFrom(ForgeDirection direction) {
            if (this.selected == null) {
                return null;
            } else {
                int fromSide = direction.getOpposite().ordinal();
                int[] fromSlots = null;
                if (!(this.selected instanceof ISidedInventory)) {
                    fromSlots = new int[this.selected.func_70302_i_()];
                    int i = 0;

                    while (i < this.selected.func_70302_i_()) {
                        fromSlots[i] = i++;
                    }
                } else {
                    fromSlots = ((ISidedInventory)this.selected).func_94128_d(fromSide);
                }

                return fromSlots;
            }
        }
    }
}
