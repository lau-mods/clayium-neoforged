/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.Arrays;
import java.util.EnumMap;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.machine.ConfigurableClayEnergyMachine;
import net.claustra01.clayium.machine.MachineModifiers;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/** Reusable transport, CE, persistence and synchronization for dedicated machine mechanisms. */
public abstract class AbstractConfigurableMachineBlockEntity extends BaseContainerBlockEntity
        implements ConfigurableClayEnergyMachine {
    private NonNullList<ItemStack> items;
    protected final ClayEnergyStorage energy = new ClayEnergyStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
    protected int progress;
    private final int slotCount;
    private final int[] insertionRoutes = {-1, 0, -1, -1, -1, -1};
    private final int[] extractionRoutes = {0, -1, -1, -1, -1, -1};
    private final ItemStack[] filters = new ItemStack[6];
    private final SideConfiguration sideConfiguration;
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches =
            new EnumMap<>(Direction.class);
    private int automationCooldown;
    private int modifierCheckDelay;
    private MachineModifiers.Snapshot modifiers = MachineModifiers.Snapshot.DEFAULT;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            long displayed = displayedEnergy();
            return switch (index) {
                case 0 -> progress;
                case 1 -> totalProgress();
                case 2 -> (int) displayed;
                case 3 -> (int) (displayed >>> 32);
                default -> additionalMenuData(index);
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) progress = Math.max(0, value);
            else if (index == 2) energy.setEnergy((energy.energyStored() & 0xffffffff00000000L) | Integer.toUnsignedLong(value));
            else if (index == 3) energy.setEnergy((Integer.toUnsignedLong(value) << 32) | (energy.energyStored() & 0xffffffffL));
            else setAdditionalMenuData(index, value);
        }

        @Override
        public int getCount() {
            return menuDataSize();
        }
    };

    protected AbstractConfigurableMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                                       int slotCount, boolean acceptsEnergy) {
        super(type, pos, state);
        this.slotCount = slotCount;
        items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        Arrays.fill(filters, ItemStack.EMPTY);
        if (acceptsEnergy) insertionRoutes[3] = 1;
        sideConfiguration = new SideConfiguration(insertionRoutes, extractionRoutes, filters,
                () -> acceptsClayEnergy() ? 2 : 1, () -> 1);
        for (Direction side : Direction.values()) sidedHandlers.put(side, new SidedHandler(side));
    }

    public final void serverTick() {
        if (level != null && modifierCheckDelay-- <= 0) {
            modifiers = MachineModifiers.scan(level, worldPosition);
            modifierCheckDelay = 20;
        }
        tickAutomation();
        tickMachine();
    }

    protected abstract void tickMachine();
    protected abstract boolean acceptsClayEnergy();
    protected abstract boolean isExternalInput(int slot, ItemStack stack);
    protected abstract boolean isExternalOutput(int slot);
    protected abstract boolean isNormalInputSlot(int slot);
    protected abstract boolean isEnergySlot(int slot);
    public abstract int totalProgress();
    public abstract int tierIndex();
    public abstract long energyPerTick();

    protected long displayedEnergy() {
        return energy.energyStored();
    }

    protected int menuDataSize() { return 4; }
    protected int additionalMenuData(int index) { return 0; }
    protected void setAdditionalMenuData(int index, int value) {}
    protected final double overclockFactor() { return modifiers.overclockFactor(); }
    protected final int energySlotLimit() { return modifiers.energySlotLimit(); }
    public final int inventorySlotLimit(int slot) {
        checkSlot(slot);
        return isEnergySlot(slot) ? energySlotLimit() : getMaxStackSize();
    }

    protected final NonNullList<ItemStack> inventory() {
        return items;
    }

    public final ContainerData menuData() {
        return data;
    }

    public final IItemHandler itemHandler(Direction side) {
        return sidedHandlers.get(side);
    }

    @Override
    public final SideConfiguration sideConfiguration() {
        return sideConfiguration;
    }

    @Override
    public final net.minecraft.world.level.block.entity.BlockEntity ioOwner() {
        return this;
    }

    @Override
    public final net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return AbstractTieredIoMachineBlock.PIPE;
    }

    @Override
    public final net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() {
        return AbstractTieredIoMachineBlock.FACING;
    }

    @Override
    public final void ioConfigurationChanged() {
        neighborCaches.clear();
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public final long receiveClayEnergy(long amount, boolean simulate) {
        return acceptsClayEnergy() ? energy.receive(amount, simulate) : 0;
    }

    public final long clayEnergyStored() {
        return energy.energyStored();
    }

    public final String insertionIcon(Direction side) {
        return insertionRoute(side) == 0 ? "import" : insertionRoute(side) == 1 ? "import_energy" : "";
    }

    public final String extractionIcon(Direction side) {
        return extractionRoute(side) == 0 ? "export" : "";
    }

    private void tickAutomation() {
        int interval = tierIndex() >= 6 ? 2 : 4;
        if (++automationCooldown < interval) return;
        automationCooldown = 0;
        for (Direction side : Direction.values()) {
            int insert = insertionRoute(side);
            if (insert >= 0 && pull(side, insert)) return;
            if (extractionRoute(side) >= 0 && push(side)) return;
        }
    }

    private boolean pull(Direction side, int route) {
        IItemHandler source = neighbor(side);
        if (source == null) return false;
        for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
            ItemStack offered = source.extractItem(sourceSlot, transferLimit(), true);
            if (offered.isEmpty() || !matchesFilter(filters[relative(side)], offered)) continue;
            for (int targetSlot = 0; targetSlot < slotCount; targetSlot++) {
                if (route == 0 && !isNormalInputSlot(targetSlot) || route == 1 && !isEnergySlot(targetSlot)) continue;
                ItemStack remainder = insert(targetSlot, offered, true);
                int accepted = offered.getCount() - remainder.getCount();
                if (accepted <= 0) continue;
                ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
                ItemStack failed = insert(targetSlot, extracted, false);
                if (!failed.isEmpty()) source.insertItem(sourceSlot, failed, false);
                return true;
            }
        }
        return false;
    }

    private boolean push(Direction side) {
        IItemHandler target = neighbor(side);
        if (target == null) return false;
        for (int sourceSlot = 0; sourceSlot < slotCount; sourceSlot++) {
            if (!isExternalOutput(sourceSlot)) continue;
            ItemStack stored = getItem(sourceSlot);
            if (stored.isEmpty() || !matchesFilter(filters[relative(side)], stored)) continue;
            ItemStack offered = stored.copyWithCount(Math.min(transferLimit(), stored.getCount()));
            ItemStack remainder = offered;
            for (int targetSlot = 0; targetSlot < target.getSlots() && !remainder.isEmpty(); targetSlot++) {
                remainder = target.insertItem(targetSlot, remainder, false);
            }
            int moved = offered.getCount() - remainder.getCount();
            if (moved > 0) {
                removeItem(sourceSlot, moved);
                return true;
            }
        }
        return false;
    }

    private int transferLimit() {
        return tierIndex() >= 6 ? 16 : 4;
    }

    private IItemHandler neighbor(Direction side) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return neighborCaches.computeIfAbsent(side, direction -> BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK, serverLevel, worldPosition.relative(direction), direction.getOpposite(),
                () -> !isRemoved(), () -> {})).getCapability();
    }

    private ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        checkSlot(slot);
        if (stack.isEmpty() || !isExternalInput(slot, stack)) return stack;
        ItemStack current = getItem(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return stack;
        int limit = Math.min(stack.getMaxStackSize(), inventorySlotLimit(slot));
        int accepted = Math.min(stack.getCount(), limit - current.getCount());
        if (accepted <= 0) return stack;
        if (!simulate) {
            if (current.isEmpty()) setItem(slot, stack.copyWithCount(accepted));
            else {
                current.grow(accepted);
                setChanged();
            }
        }
        return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
    }

    protected final boolean consumeFuel(int slot) {
        ItemStack fuel = getItem(slot);
        long value = EnergeticClayFuel.value(fuel);
        if (value <= 0 || energy.receive(value, true) != value) return false;
        energy.receive(value, false);
        fuel.shrink(1);
        setChanged();
        return true;
    }

    protected final boolean canStore(int from, int to, ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = from; slot < to; slot++) {
            ItemStack current = getItem(slot);
            if (current.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(current, stack)) {
                remaining -= current.getMaxStackSize() - current.getCount();
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    protected final void store(int from, int to, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = from; slot < to && !remaining.isEmpty(); slot++) {
            ItemStack current = getItem(slot);
            if (current.isEmpty()) {
                items.set(slot, remaining);
                return;
            }
            if (ItemStack.isSameItemSameComponents(current, remaining)) {
                int moved = Math.min(remaining.getCount(), current.getMaxStackSize() - current.getCount());
                current.grow(moved);
                remaining.shrink(moved);
            }
        }
    }

    @Override
    protected final NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected final void setItems(NonNullList<ItemStack> values) {
        items = values;
    }

    @Override
    public final int getContainerSize() {
        return slotCount;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        checkSlot(slot);
        stack.limitSize(Math.min(stack.getMaxStackSize(), inventorySlotLimit(slot)));
        super.setItem(slot, stack);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        energy.load(tag);
        progress = Math.max(0, tag.getInt("Progress"));
        sideConfiguration.replaceRoutes(
                loadRoutes(tag, "InsertionRoutes", insertionRoutes, acceptsClayEnergy() ? 2 : 1),
                loadRoutes(tag, "ExtractionRoutes", extractionRoutes, 1));
        ListTag list = tag.getList("Filters", Tag.TAG_COMPOUND);
        for (int index = 0; index < Math.min(filters.length, list.size()); index++) {
            filters[index] = ItemStack.parseOptional(registries, list.getCompound(index));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        energy.save(tag);
        tag.putInt("Progress", progress);
        tag.putIntArray("InsertionRoutes", insertionRoutes);
        tag.putIntArray("ExtractionRoutes", extractionRoutes);
        ListTag list = new ListTag();
        for (ItemStack filter : filters) list.add(filter.saveOptional(registries));
        tag.put("Filters", list);
    }

    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private int relative(Direction side) {
        return RelativeFace.index(getBlockState().getValue(AbstractTieredIoMachineBlock.FACING), side);
    }

    private static boolean matchesFilter(ItemStack filter, ItemStack stack) {
        return filter.isEmpty() || ClayFilterItem.matches(filter, stack);
    }

    private static int[] loadRoutes(CompoundTag tag, String key, int[] fallback, int count) {
        if (!tag.contains(key, Tag.TAG_INT_ARRAY)) return fallback.clone();
        int[] result = Arrays.copyOf(tag.getIntArray(key), 6);
        for (int index = 0; index < result.length; index++) {
            if (result[index] < -1 || result[index] >= count) result[index] = -1;
        }
        return result;
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= slotCount) throw new IndexOutOfBoundsException(slot);
    }

    private final class SidedHandler implements IItemHandler {
        private final Direction side;

        private SidedHandler(Direction side) {
            this.side = side;
        }

        @Override public int getSlots() { return slotCount; }
        @Override public ItemStack getStackInSlot(int slot) { checkSlot(slot); return getItem(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int route = insertionRoute(side);
            if (route < 0 || route == 0 && !isNormalInputSlot(slot) || route == 1 && !isEnergySlot(slot)
                    || !matchesFilter(filters[relative(side)], stack)) return stack;
            return insert(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            checkSlot(slot);
            if (extractionRoute(side) < 0 || !isExternalOutput(slot) || amount <= 0) return ItemStack.EMPTY;
            ItemStack current = getItem(slot);
            int count = Math.min(amount, current.getCount());
            ItemStack result = current.copyWithCount(count);
            if (!simulate && count > 0) removeItem(slot, count);
            return result;
        }
        @Override public int getSlotLimit(int slot) {
            checkSlot(slot);
            return isEnergySlot(slot) ? energySlotLimit() : getMaxStackSize();
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            checkSlot(slot);
            int route = insertionRoute(side);
            return (route == 0 && isNormalInputSlot(slot) || route == 1 && isEnergySlot(slot))
                    && isExternalInput(slot, stack);
        }
    }
}
