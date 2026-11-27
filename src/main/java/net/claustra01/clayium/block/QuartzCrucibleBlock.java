package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Quartz Crucible (tier 5, fill level 0..9, hollow basin shape).
 */
public class QuartzCrucibleBlock extends Block implements ITieredBlock, EntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 9);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 1.0, 12.0, 16.0),
            Block.box(15.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 1.0),
            Block.box(0.0, 0.0, 15.0, 16.0, 12.0, 16.0)
    );

    public QuartzCrucibleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    public int getTier() {
        return 5;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ItemEntity itemEntity)) {
            return;
        }
        if (entity.getY() - (double) pos.getY() >= 0.2) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof QuartzCrucibleBlockEntity crucible)) {
            return;
        }
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.is(ClayiumItems.byPath("impure_silicon_ingot").get())) {
            if (crucible.putIngot()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
            }
        } else if (stack.is(Items.STRING)) {
            if (crucible.consumeString()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuartzCrucibleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ClayiumBlockEntities.QUARTZ_CRUCIBLE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof QuartzCrucibleBlockEntity crucible) {
                QuartzCrucibleBlockEntity.serverTick(lvl, pos, st, crucible);
            }
        };
    }
}
