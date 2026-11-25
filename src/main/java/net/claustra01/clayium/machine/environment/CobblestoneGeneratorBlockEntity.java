package net.claustra01.clayium.machine.environment;

import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;
import net.claustra01.clayium.logistics.SideConfiguration;
import net.claustra01.clayium.util.WaterDetection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class CobblestoneGeneratorBlockEntity extends BlockEntity implements ConfigurableIoDevice {
    private final ItemStackHandler output = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };
    private int progress;
    private final int[] insertionRoutes = {-1, -1, -1, -1, -1, -1};
    private final int[] extractionRoutes = {-1, -1, 0, -1, -1, -1};
    private final SideConfiguration sideConfiguration = SideConfiguration.withoutFilters(
            insertionRoutes, extractionRoutes, () -> 0, () -> 1);
    private final java.util.EnumMap<Direction, IItemHandler> sidedHandlers = new java.util.EnumMap<>(Direction.class);

    public CobblestoneGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(EnvironmentMachineRegistration.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), pos, state);
        for (Direction direction : Direction.values()) sidedHandlers.put(direction, new OutputHandler(direction));
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, CobblestoneGeneratorBlockEntity generator) {
        generator.tickServer();
    }

    public void tickServer() {
        if (level == null || !hasHorizontalWaterAndLava()) {
            return;
        }
        ItemStack stored = output.getStackInSlot(0);
        if (!stored.isEmpty() && (!stored.is(Items.COBBLESTONE) || stored.getCount() >= stored.getMaxStackSize())) {
            return;
        }
        progress++;
        if (progress < intervalTicks()) {
            setChanged();
            return;
        }
        progress = 0;
        if (stored.isEmpty()) {
            output.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        } else {
            stored.grow(1);
            setChanged();
        }
    }

    private boolean hasHorizontalWaterAndLava() {
        if (level == null) {
            return false;
        }
        boolean water = false;
        boolean lava = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = worldPosition.relative(direction);
            water |= WaterDetection.hasWater(level, adjacent);
            lava |= level.getFluidState(adjacent).is(FluidTags.LAVA);
        }
        return water && lava;
    }

    public int intervalTicks() {
        return intervalTicks(tier());
    }

    public static int intervalTicks(int tier) {
        int baseInterval = switch (tier) {
            case 1 -> 40;
            case 2 -> 20;
            case 3 -> 10;
            case 4 -> 5;
            case 5 -> 2;
            default -> 1;
        };
        return (int)ClayiumConfig.divideProgressionTicks(baseInterval);
    }

    public int tier() {
        return ((CobblestoneGeneratorBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler output() {
        return output;
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return direction == null ? output : sidedHandlers.get(direction);
    }

    @Override public SideConfiguration sideConfiguration() { return sideConfiguration; }
    @Override public BlockEntity ioOwner() { return this; }
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty() { return CobblestoneGeneratorBlock.PIPE; }
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty() { return CobblestoneGeneratorBlock.FACING; }
    @Override public void ioConfigurationChanged() {
        if (level != null) level.invalidateCapabilities(worldPosition);
        setChanged();
        ioNeighborsChanged();
    }

    public Container dropInventory() {
        SimpleContainer dropped = new SimpleContainer(1);
        dropped.setItem(0, output.getStackInSlot(0).copy());
        output.setStackInSlot(0, ItemStack.EMPTY);
        return dropped;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", output.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putIntArray("insertion_routes", insertionRoutes);
        tag.putIntArray("extraction_routes", extractionRoutes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            output.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = tag.getInt("progress");
        if (tag.contains("insertion_routes") || tag.contains("extraction_routes")) {
            sideConfiguration.replaceRoutes(tag.getIntArray("insertion_routes"), tag.getIntArray("extraction_routes"));
        }
    }

    private final class OutputHandler implements IItemHandler {
        private final Direction direction;
        private OutputHandler(Direction direction) { this.direction = direction; }
        @Override public int getSlots() { return extractionRoute(direction) >= 0 ? output.getSlots() : 0; }
        @Override public ItemStack getStackInSlot(int slot) { return output.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return extractionRoute(direction) >= 0 ? output.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return output.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }

}
