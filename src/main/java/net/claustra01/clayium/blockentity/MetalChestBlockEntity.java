package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tiered Metal Chest (up to 832 slots / 8 pages). Supports manual route assignment when configured with tools.
 */
public class MetalChestBlockEntity extends ClayContainerBlockEntity {
    private final int cols;
    private final int rows;
    private final int pages;

    public MetalChestBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.METAL_CHEST.get(), pos, state, computeSlotCount(state));
        if (state.getBlock() instanceof MetalChestBlock chest) {
            this.cols = chest.getCols();
            this.rows = chest.getRows();
            this.pages = chest.getPages();
        } else {
            this.cols = 9;
            this.rows = 6;
            this.pages = 1;
        }
        int total = this.cols * this.rows * this.pages;
        int[] allSlots = new int[total];
        for (int i = 0; i < total; i++) {
            allSlots[i] = i;
        }
        this.listSlotsInsert.add(allSlots);
        this.listSlotsExtract.add(allSlots);
        this.slotsDrop = allSlots;
    }

    private static int computeSlotCount(BlockState state) {
        if (state.getBlock() instanceof MetalChestBlock chest) {
            return Math.max(1, chest.getTotalSlots());
        }
        return 54;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return this.rows;
    }

    public int getPages() {
        return this.pages;
    }

    @Override
    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        return route == 0;
    }

    @Override
    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        return route == 0;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }
}
