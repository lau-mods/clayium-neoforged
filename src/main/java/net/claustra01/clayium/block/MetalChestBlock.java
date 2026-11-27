package net.claustra01.clayium.block;

import net.claustra01.clayium.material.CMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Metal chest block storing its material and original slot layout (cols, rows, pages).
 */
public class MetalChestBlock extends HorizontalClayContainerBlock {
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    private final CMaterial material;
    private final int cols;
    private final int rows;
    private final int pages;

    public MetalChestBlock(Properties properties, CMaterial material, int cols, int rows, int pages) {
        super(properties, -1, "metalblock_base", null, "metal_chest", null);
        this.material = material;
        this.cols = cols;
        this.rows = rows;
        this.pages = pages;
    }

    public CMaterial getMaterial() {
        return this.material;
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

    public int getTotalSlots() {
        return this.cols * this.rows * this.pages;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
