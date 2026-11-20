package net.claustra01.clayium.fluid;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.FluidRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class FluidTransportBlockEntity extends BlockEntity implements MenuProvider {
    private final FluidTank tank;
    private final FluidTank pending;
    private final ItemStackHandler filterInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(FluidRegistration.FLUID_FILTER.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private final EnumMap<Direction, FaceMode> faces = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, IFluidHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final IFluidHandler unsidedHandler = new SidedFluidHandler(null);
    private final ContainerData menuData = new TransportContainerData();

    private EnumMap<Direction, BlockCapabilityCache<IFluidHandler, Direction>> neighborCaches;
    private RedstoneCondition redstoneCondition = RedstoneCondition.IGNORE;
    private int nextOutputDirection;
    private int nextInputDirection;
    private long transferTick = Long.MIN_VALUE;
    private int transferredThisTick;
    private final EnumMap<Direction, Long> filledAt = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, Long> drainedAt = new EnumMap<>(Direction.class);

    public FluidTransportBlockEntity(BlockPos pos, BlockState state) {
        super(FluidRegistration.BLOCK_ENTITY.get(), pos, state);
        int capacity = specification(state).capacity();
        tank = createTank(capacity);
        pending = createTank(capacity);
        for (Direction direction : Direction.values()) {
            faces.put(direction, direction == Direction.DOWN ? FaceMode.OUTPUT : FaceMode.INPUT);
            sidedHandlers.put(direction, new SidedFluidHandler(direction));
        }
    }

    private FluidTank createTank(int capacity) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                setChangedAndSync();
            }
        };
    }

    private static FluidTransportSpecification specification(BlockState state) {
        return ((FluidTransportBlock)state.getBlock()).specification();
    }

    public FluidTransportSpecification specification() {
        return specification(getBlockState());
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, FluidTransportBlockEntity transport) {
        transport.tickServer();
    }

    private void tickServer() {
        if (!(level instanceof ServerLevel serverLevel)
                || !redstoneCondition.allows(serverLevel.hasNeighborSignal(worldPosition))) {
            return;
        }
        ensureCaches(serverLevel);
        int remaining = remainingTransfer();
        Set<Direction> usedOutputs = new HashSet<>();
        remaining -= resendPending(remaining, usedOutputs);
        if (pending.isEmpty()) {
            remaining -= pushStored(remaining, usedOutputs);
        }
        pullExternal(remaining, usedOutputs);
    }

    private void ensureCaches(ServerLevel serverLevel) {
        if (neighborCaches != null) {
            return;
        }
        neighborCaches = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            neighborCaches.put(direction, BlockCapabilityCache.create(
                    Capabilities.FluidHandler.BLOCK,
                    serverLevel,
                    worldPosition.relative(direction),
                    direction.getOpposite(),
                    () -> !isRemoved(),
                    () -> {}));
        }
    }

    private int resendPending(int maximum, Set<Direction> usedOutputs) {
        int moved = 0;
        for (int offset = 0; offset < Direction.values().length && moved < maximum && !pending.isEmpty(); offset++) {
            int index = (nextOutputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (!faces.get(direction).canOutput() || filledAt.getOrDefault(direction, Long.MIN_VALUE) == level.getGameTime()) {
                continue;
            }
            IFluidHandler target = neighborCaches.get(direction).getCapability();
            if (target == null) {
                continue;
            }
            FluidStack offered = pending.drain(maximum - moved, IFluidHandler.FluidAction.SIMULATE);
            int accepted = boundedFill(target, offered, IFluidHandler.FluidAction.SIMULATE);
            if (accepted == 0) {
                continue;
            }
            FluidStack executing = offered.copyWithAmount(accepted);
            int inserted = boundedFill(target, executing, IFluidHandler.FluidAction.EXECUTE);
            if (inserted > 0) {
                pending.drain(inserted, IFluidHandler.FluidAction.EXECUTE);
                moved += inserted;
                transferredThisTick += inserted;
                usedOutputs.add(direction);
                nextOutputDirection = (index + 1) % Direction.values().length;
            }
        }
        return moved;
    }

    private int pushStored(int maximum, Set<Direction> usedOutputs) {
        int moved = 0;
        for (int offset = 0; offset < Direction.values().length && moved < maximum && !tank.isEmpty(); offset++) {
            int index = (nextOutputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (!faces.get(direction).canOutput() || filledAt.getOrDefault(direction, Long.MIN_VALUE) == level.getGameTime()) {
                continue;
            }
            IFluidHandler target = neighborCaches.get(direction).getCapability();
            if (target == null) {
                continue;
            }
            FluidStack offered = tank.drain(maximum - moved, IFluidHandler.FluidAction.SIMULATE);
            if (!accepts(offered)) {
                continue;
            }
            int accepted = boundedFill(target, offered, IFluidHandler.FluidAction.SIMULATE);
            if (accepted == 0 || pending.fill(
                    offered.copyWithAmount(accepted), IFluidHandler.FluidAction.SIMULATE) != accepted) {
                continue;
            }
            FluidStack extracted = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            int inserted = boundedFill(target, extracted, IFluidHandler.FluidAction.EXECUTE);
            int rejected = extracted.getAmount() - inserted;
            if (rejected > 0) {
                pending.fill(extracted.copyWithAmount(rejected), IFluidHandler.FluidAction.EXECUTE);
            }
            moved += extracted.getAmount();
            transferredThisTick += extracted.getAmount();
            usedOutputs.add(direction);
            nextOutputDirection = (index + 1) % Direction.values().length;
        }
        return moved;
    }

    private int pullExternal(int maximum, Set<Direction> usedOutputs) {
        int moved = 0;
        for (int offset = 0; offset < Direction.values().length && moved < maximum; offset++) {
            int index = (nextInputDirection + offset) % Direction.values().length;
            Direction direction = Direction.values()[index];
            if (!faces.get(direction).canInput() || usedOutputs.contains(direction)
                    || drainedAt.getOrDefault(direction, Long.MIN_VALUE) == level.getGameTime()) {
                continue;
            }
            IFluidHandler source = neighborCaches.get(direction).getCapability();
            if (source == null) {
                continue;
            }
            FluidStack offered = source.drain(maximum - moved, IFluidHandler.FluidAction.SIMULATE);
            if (offered.isEmpty() || !accepts(offered)) {
                continue;
            }
            int accepted = tank.fill(offered, IFluidHandler.FluidAction.SIMULATE);
            if (accepted == 0 || pending.fill(
                    offered.copyWithAmount(accepted), IFluidHandler.FluidAction.SIMULATE) != accepted) {
                continue;
            }
            FluidStack extracted = source.drain(
                    offered.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
            if (extracted.isEmpty()) {
                continue;
            }
            int stored = tank.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
            int rejected = extracted.getAmount() - stored;
            if (rejected > 0) {
                pending.fill(extracted.copyWithAmount(rejected), IFluidHandler.FluidAction.EXECUTE);
            }
            moved += extracted.getAmount();
            transferredThisTick += extracted.getAmount();
            nextInputDirection = (index + 1) % Direction.values().length;
        }
        return moved;
    }

    private static int boundedFill(IFluidHandler handler, FluidStack stack, IFluidHandler.FluidAction action) {
        return stack.isEmpty() ? 0 : Math.max(0, Math.min(stack.getAmount(), handler.fill(stack, action)));
    }

    private int remainingTransfer() {
        if (level == null) {
            return specification().transferPerTick();
        }
        if (transferTick != level.getGameTime()) {
            transferTick = level.getGameTime();
            transferredThisTick = 0;
        }
        return specification().transferPerTick() - transferredThisTick;
    }

    private boolean accepts(FluidStack fluid) {
        return filterSettings().accepts(fluid);
    }

    public FluidFilterSettings filterSettings() {
        return filterInventory.getStackInSlot(0).getOrDefault(
                FluidRegistration.FLUID_FILTER_SETTINGS.get(), FluidFilterSettings.ALLOW_ALL);
    }

    public void toggleFilterMode() {
        ItemStack filter = filterInventory.getStackInSlot(0);
        if (filter.isEmpty()) {
            return;
        }
        filter.set(FluidRegistration.FLUID_FILTER_SETTINGS.get(), filterSettings().toggled());
        setChangedAndSync();
    }

    public void filterCurrentFluid() {
        if (tank.isEmpty() || filterInventory.getStackInSlot(0).isEmpty()) {
            return;
        }
        ItemStack filter = filterInventory.getStackInSlot(0);
        filter.set(FluidRegistration.FLUID_FILTER_SETTINGS.get(), new FluidFilterSettings(
                java.util.Optional.of(tank.getFluid().getFluid().builtInRegistryHolder().key().location()),
                filterSettings().blacklist()));
        setChangedAndSync();
    }

    public void clearFilterFluid() {
        if (filterInventory.getStackInSlot(0).isEmpty()) {
            return;
        }
        ItemStack filter = filterInventory.getStackInSlot(0);
        filter.set(FluidRegistration.FLUID_FILTER_SETTINGS.get(), new FluidFilterSettings(
                java.util.Optional.empty(), filterSettings().blacklist()));
        setChangedAndSync();
    }

    public FluidTank tank() {
        return tank;
    }

    public FluidTank pending() {
        return pending;
    }

    public ItemStackHandler filterInventory() {
        return filterInventory;
    }

    public IFluidHandler fluidHandler(@Nullable Direction direction) {
        return direction == null ? unsidedHandler : sidedHandlers.get(direction);
    }

    public FaceMode faceMode(Direction direction) {
        return faces.get(direction);
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        faces.put(direction, mode);
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        setChangedAndSync();
    }

    public RedstoneCondition redstoneCondition() {
        return redstoneCondition;
    }

    public void setRedstoneCondition(RedstoneCondition condition) {
        redstoneCondition = condition;
        setChangedAndSync();
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(1);
        dropped.setItem(0, filterInventory.getStackInSlot(0).copy());
        filterInventory.setStackInSlot(0, ItemStack.EMPTY);
        return dropped;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FluidTransportMenu(containerId, inventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.put("pending", pending.writeToNBT(registries, new CompoundTag()));
        tag.put("filter", filterInventory.serializeNBT(registries));
        CompoundTag savedFaces = new CompoundTag();
        faces.forEach((direction, mode) -> savedFaces.putString(direction.getName(), mode.name()));
        tag.put("faces", savedFaces);
        tag.putString("redstone_condition", redstoneCondition.name());
        tag.putInt("next_output_direction", nextOutputDirection);
        tag.putInt("next_input_direction", nextInputDirection);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag.getCompound("tank"));
        pending.readFromNBT(registries, tag.getCompound("pending"));
        if (tag.contains("filter")) {
            filterInventory.deserializeNBT(registries, tag.getCompound("filter"));
        }
        CompoundTag savedFaces = tag.getCompound("faces");
        for (Direction direction : Direction.values()) {
            if (savedFaces.contains(direction.getName())) {
                try {
                    faces.put(direction, FaceMode.valueOf(savedFaces.getString(direction.getName())));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        try {
            redstoneCondition = RedstoneCondition.valueOf(tag.getString("redstone_condition"));
        } catch (IllegalArgumentException ignored) {
            redstoneCondition = RedstoneCondition.IGNORE;
        }
        nextOutputDirection = Math.floorMod(tag.getInt("next_output_direction"), Direction.values().length);
        nextInputDirection = Math.floorMod(tag.getInt("next_input_direction"), Direction.values().length);
    }

    private void setChangedAndSync() {
        setChanged();
    }

    public enum FaceMode {
        INPUT,
        OUTPUT,
        BOTH,
        DISABLED;

        public boolean canInput() {
            return this == INPUT || this == BOTH;
        }

        public boolean canOutput() {
            return this == OUTPUT || this == BOTH;
        }

        public FaceMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

    }

    public enum RedstoneCondition {
        IGNORE,
        POWERED,
        UNPOWERED;

        public boolean allows(boolean powered) {
            return this == IGNORE || this == POWERED && powered || this == UNPOWERED && !powered;
        }

        public RedstoneCondition next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final class SidedFluidHandler implements IFluidHandler {
        @Nullable
        private final Direction direction;

        private SidedFluidHandler(@Nullable Direction direction) {
            this.direction = direction;
        }

        @Override
        public int getTanks() {
            return direction != null && faces.get(direction) == FaceMode.DISABLED ? 0 : 1;
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return (direction == null || faces.get(direction).canInput()) && accepts(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if ((direction != null && !faces.get(direction).canInput()) || !accepts(resource)) {
                return 0;
            }
            int filled = tank.fill(resource.copyWithAmount(Math.min(resource.getAmount(), remainingTransfer())), action);
            if (action.execute() && filled > 0 && level != null) {
                transferredThisTick += filled;
                if (direction != null) {
                    filledAt.put(direction, level.getGameTime());
                }
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (direction != null && !faces.get(direction).canOutput() || !accepts(resource)) {
                return FluidStack.EMPTY;
            }
            FluidStack drained = tank.drain(resource.copyWithAmount(Math.min(resource.getAmount(), remainingTransfer())), action);
            markExternal(action, drained);
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (direction != null && !faces.get(direction).canOutput() || !accepts(tank.getFluid())) {
                return FluidStack.EMPTY;
            }
            FluidStack drained = tank.drain(Math.min(maxDrain, remainingTransfer()), action);
            markExternal(action, drained);
            return drained;
        }

        private void markExternal(FluidAction action, FluidStack moved) {
            if (action.execute() && !moved.isEmpty() && level != null) {
                transferredThisTick += moved.getAmount();
                if (direction != null) {
                    drainedAt.put(direction, level.getGameTime());
                }
            }
        }
    }

    private final class TransportContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> specification().tier();
                case 1 -> tank.isEmpty() ? 0 : net.minecraft.core.registries.BuiltInRegistries.FLUID.getId(
                        tank.getFluid().getFluid()) + 1;
                case 2, 3 -> (tank.getFluidAmount() >>> ((index - 2) * 16)) & 0xFFFF;
                case 4, 5 -> (tank.getCapacity() >>> ((index - 4) * 16)) & 0xFFFF;
                case 6, 7 -> (pending.getFluidAmount() >>> ((index - 6) * 16)) & 0xFFFF;
                case 8, 9 -> (specification().transferPerTick() >>> ((index - 8) * 16)) & 0xFFFF;
                case 10 -> redstoneCondition.ordinal();
                case 11, 12, 13, 14, 15, 16 -> faces.get(Direction.values()[index - 11]).ordinal();
                case 17 -> filterSettings().blacklist() ? 1 : 0;
                case 18 -> filterSettings().fluid().map(location -> {
                    Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(location);
                    return fluid == null ? 0 : net.minecraft.core.registries.BuiltInRegistries.FLUID.getId(fluid) + 1;
                }).orElse(0);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 19;
        }
    }
}
