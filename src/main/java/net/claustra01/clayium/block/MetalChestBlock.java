package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Metal chest block storing its material, original slot layout (cols, rows, pages),
 * and BlockEntity container. Cannot enter piping mode ({@code canBePipe() == false}).
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean canBePipe() {
        return false;
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MetalChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ClayiumBlockEntities.METAL_CHEST.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (lvl, p, st, be) -> {
                if (be instanceof MetalChestBlockEntity chest) {
                    MetalChestBlockEntity.clientTickStatic(lvl, p, st, chest);
                }
            };
        }
        return (lvl, p, st, be) -> {
            if (be instanceof MetalChestBlockEntity chest) {
                chest.serverTick();
            }
        };
    }
}
