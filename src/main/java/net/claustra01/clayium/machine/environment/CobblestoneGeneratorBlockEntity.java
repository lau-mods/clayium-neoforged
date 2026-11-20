package net.claustra01.clayium.machine.environment;

import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
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

public final class CobblestoneGeneratorBlockEntity extends BlockEntity {
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

    public CobblestoneGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(EnvironmentMachineRegistration.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), pos, state);
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
        return switch (tier()) {
            case 1 -> 40;
            case 2 -> 20;
            case 3 -> 10;
            case 4 -> 5;
            case 5 -> 2;
            default -> 1;
        };
    }

    public int tier() {
        return ((CobblestoneGeneratorBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler output() {
        return output;
    }

    public IItemHandler itemHandler() {
        return output;
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            output.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = tag.getInt("progress");
    }

}
