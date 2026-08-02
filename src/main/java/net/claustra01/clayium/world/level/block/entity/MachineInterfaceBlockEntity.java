/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.Arrays;
import java.util.EnumMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.machine.ControllerLinkedDevice;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.claustra01.clayium.world.level.block.MachineInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/** Item-only proxy and transporter for a linked multiblock controller. */
public final class MachineInterfaceBlockEntity extends BlockEntity
        implements ConfigurableItemDevice, ControllerLinkedDevice, MenuProvider {
    private final int[] insertionRoutes = {-1, -1, -1, 0, -1, -1};
    private final int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final ItemStack[] filters = new ItemStack[6];
    private final SideConfiguration sideConfiguration;
    private final EnumMap<Direction, IItemHandler> handlers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> neighborCaches =
            new EnumMap<>(Direction.class);
    @Nullable private BlockPos linkedMachine;
    private int transferCooldown;

    public MachineInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.MACHINE_INTERFACE_BLOCK_ENTITY.get(), pos, state);
        Arrays.fill(filters, ItemStack.EMPTY);
        sideConfiguration = new SideConfiguration(insertionRoutes, extractionRoutes, filters,
                this::insertionRouteCount, this::extractionRouteCount);
        for (Direction side : Direction.values()) handlers.put(side, new LinkedMachineHandler(side));
    }

    public void serverTick() {
        if (++transferCooldown < transferInterval()) return;
        transferCooldown = 0;
        for (Direction side : Direction.values()) {
            int relative = relative(side);
            if (insertionRoutes[relative] >= 0 && pullFrom(side)) return;
            if (extractionRoutes[relative] >= 0 && pushTo(side)) return;
        }
    }

    private boolean pullFrom(Direction side) {
        IItemHandler source = neighbor(side);
        IItemHandler destination = itemHandler(side);
        return source != null && transfer(source, destination);
    }

    private boolean pushTo(Direction side) {
        IItemHandler destination = neighbor(side);
        return destination != null && transfer(itemHandler(side), destination);
    }

    private boolean transfer(IItemHandler source, IItemHandler destination) {
        for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
            ItemStack offered = source.extractItem(sourceSlot, transferLimit(), true);
            if (offered.isEmpty()) continue;
            ItemStack remainder = offered;
            for (int targetSlot = 0; targetSlot < destination.getSlots() && !remainder.isEmpty(); targetSlot++) {
                remainder = destination.insertItem(targetSlot, remainder, true);
            }
            int accepted = offered.getCount() - remainder.getCount();
            if (accepted <= 0) continue;
            ItemStack committed = source.extractItem(sourceSlot, accepted, false);
            for (int targetSlot = 0; targetSlot < destination.getSlots() && !committed.isEmpty(); targetSlot++) {
                committed = destination.insertItem(targetSlot, committed, false);
            }
            return committed.isEmpty();
        }
        return false;
    }

    @Nullable
    private IItemHandler neighbor(Direction side) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return neighborCaches.computeIfAbsent(side, direction -> BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK, serverLevel, worldPosition.relative(direction), direction.getOpposite(),
                () -> !isRemoved(), () -> {})).getCapability();
    }

    public IItemHandler itemHandler(Direction side) {
        return handlers.get(side);
    }

    @Override
    public void linkMachine(BlockPos controller) {
        if (controller.equals(linkedMachine)) return;
        linkedMachine = controller.immutable();
        sideConfiguration.replaceRoutes(insertionRoutes, extractionRoutes);
        configurationChanged();
    }

    @Override
    public void unlinkMachine(BlockPos controller) {
        if (!controller.equals(linkedMachine)) return;
        linkedMachine = null;
        configurationChanged();
    }

    @Nullable
    private MachineBlockEntity linkedMachine() {
        return linkedMachine != null && level != null
                && level.getBlockEntity(linkedMachine) instanceof MachineBlockEntity machine ? machine : null;
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() {
        return MachineInterfaceBlock.PIPE;
    }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return null; }
    @Override public void ioConfigurationChanged() { configurationChanged(); }
    @Override public boolean rotate(Direction clickedFace) { return false; }
    @Override public boolean allowsPassiveInsertion(Direction side) { return true; }
    @Override public boolean allowsPassiveExtraction(Direction side) { return true; }

    @Override
    public String insertionIcon(Direction side) {
        int route = insertionRoute(side);
        MachineBlockEntity machine = linkedMachine();
        return route < 0 ? "" : machine == null ? "import" : machine.interfaceInsertionIcon(route);
    }

    @Override
    public String extractionIcon(Direction side) {
        int route = extractionRoute(side);
        MachineBlockEntity machine = linkedMachine();
        return route < 0 ? "" : machine == null ? "export" : machine.interfaceExtractionIcon(route);
    }

    @Override
    public Component getDisplayName() {
        MachineBlockEntity machine = linkedMachine();
        return machine == null ? Component.translatable(getBlockState().getBlock().getDescriptionId()) : machine.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        MachineBlockEntity machine = linkedMachine();
        return machine == null ? null : machine.createInterfaceMenu(id, inventory);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        linkedMachine = tag.contains("LinkedMachine") ? BlockPos.of(tag.getLong("LinkedMachine")) : null;
        sideConfiguration.replaceRoutes(
                loadRoutes(tag, "InsertionRoutes", insertionRoutes, insertionRouteCount()),
                loadRoutes(tag, "ExtractionRoutes", extractionRoutes, extractionRouteCount()));
        ListTag list = tag.getList("Filters", Tag.TAG_COMPOUND);
        for (int index = 0; index < Math.min(filters.length, list.size()); index++) {
            filters[index] = ItemStack.parseOptional(registries, list.getCompound(index));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (linkedMachine != null) tag.putLong("LinkedMachine", linkedMachine.asLong());
        tag.putIntArray("InsertionRoutes", insertionRoutes);
        tag.putIntArray("ExtractionRoutes", extractionRoutes);
        ListTag list = new ListTag();
        for (ItemStack filter : filters) list.add(filter.saveOptional(registries));
        tag.put("Filters", list);
    }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveCustomOnly(registries); }

    private int insertionRouteCount() {
        MachineBlockEntity machine = linkedMachine();
        return machine == null ? 4 : machine.interfaceInsertionRouteCount();
    }

    private int extractionRouteCount() {
        MachineBlockEntity machine = linkedMachine();
        return machine == null ? 3 : machine.interfaceExtractionRouteCount();
    }

    private int tierIndex() {
        return getBlockState().getBlock() instanceof MachineInterfaceBlock block
                ? block.tier().progressionIndex() : 5;
    }

    private int transferLimit() { return tierIndex() >= 6 ? 16 : 4; }
    private int transferInterval() { return tierIndex() == 5 ? 4 : tierIndex() == 6 ? 2 : 1; }
    private int relative(Direction side) { return RelativeFace.index(Direction.NORTH, side); }

    private void configurationChanged() {
        neighborCaches.clear();
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static int[] loadRoutes(CompoundTag tag, String key, int[] fallback, int routeCount) {
        if (!tag.contains(key, Tag.TAG_INT_ARRAY)) return fallback.clone();
        int[] result = Arrays.copyOf(tag.getIntArray(key), 6);
        for (int index = 0; index < result.length; index++) {
            if (result[index] < -1 || result[index] >= routeCount) result[index] = -1;
        }
        return result;
    }

    private final class LinkedMachineHandler implements IItemHandler {
        private final Direction side;
        private LinkedMachineHandler(Direction side) { this.side = side; }
        private IItemHandler delegate() {
            MachineBlockEntity machine = linkedMachine();
            int relative = relative(side);
            return machine == null ? EmptyItemHandler.INSTANCE
                    : machine.interfaceItemHandler(insertionRoutes[relative], extractionRoutes[relative], filters[relative]);
        }
        @Override public int getSlots() { return delegate().getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return delegate().getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!matchesFilter(filters[relative(side)], stack)) return stack;
            return delegate().insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return delegate().extractItem(slot, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return delegate().getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return matchesFilter(filters[relative(side)], stack) && delegate().isItemValid(slot, stack);
        }
    }

    private static boolean matchesFilter(ItemStack filter, ItemStack stack) {
        return filter.isEmpty() || ClayFilterItem.matches(filter, stack);
    }

    private enum EmptyItemHandler implements IItemHandler {
        INSTANCE;
        @Override public int getSlots() { return 0; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }
}
