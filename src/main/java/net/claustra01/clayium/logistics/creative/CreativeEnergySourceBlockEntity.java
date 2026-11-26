package net.claustra01.clayium.logistics.creative;

import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class CreativeEnergySourceBlockEntity extends BufferBlockEntity {
    public static final int SUPPLY_COUNT = 64;

    public CreativeEnergySourceBlockEntity(BlockPos pos, BlockState state) {
        super(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, CreativeEnergySourceBlockEntity source) {
        source.replenishFuel();
        source.tickServer();
    }

    @Override
    protected boolean acceptsInput() {
        return false;
    }

    private void replenishFuel() {
        Item fuel = MaterialRegistration.COMPRESSION_BLOCKS
                .get(CompressionTier.OCTUPLE_COMPRESSED_ENERGETIC_CLAY.id()).get().asItem();
        int fuelCount = 0;
        int destination = -1;
        for (int slot = 0; slot < inventory().getSlots(); slot++) {
            ItemStack stack = inventory().getStackInSlot(slot);
            if (stack.is(fuel)) {
                fuelCount += stack.getCount();
                if (destination < 0 && stack.getCount() < stack.getMaxStackSize()) {
                    destination = slot;
                }
            } else if (destination < 0 && stack.isEmpty()) {
                destination = slot;
            }
        }
        if (fuelCount >= SUPPLY_COUNT || destination < 0) {
            return;
        }
        ItemStack stack = inventory().getStackInSlot(destination);
        int replenished = Math.min(SUPPLY_COUNT - fuelCount,
                stack.isEmpty() ? fuel.getDefaultMaxStackSize() : stack.getMaxStackSize() - stack.getCount());
        if (stack.isEmpty()) {
            inventory().setStackInSlot(destination, new ItemStack(fuel, replenished));
        } else {
            stack.grow(replenished);
            inventory().setStackInSlot(destination, stack);
        }
    }
}
