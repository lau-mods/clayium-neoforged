package net.claustra01.clayium.machine.crucible;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class QuartzCrucibleBlock extends BaseEntityBlock {
    public static final EnumProperty<QuartzCrucibleStage> STAGE =
            EnumProperty.create("stage", QuartzCrucibleStage.class);
    private static final double WALL = 16.0D * 0.0675D;
    private static final double HEIGHT = 16.0D * 0.75D;
    private static final VoxelShape OUTLINE_SHAPE = box(0, 0, 0, 16, HEIGHT, 16);
    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            box(0, 0, 0, 16, WALL, 16),
            box(0, 0, 0, WALL, HEIGHT, 16),
            box(16 - WALL, 0, 0, 16, HEIGHT, 16),
            box(0, 0, 0, 16, HEIGHT, WALL),
            box(0, 0, 16 - WALL, 16, HEIGHT, 16));

    public QuartzCrucibleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, QuartzCrucibleStage.EMPTY));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof QuartzCrucibleBlockEntity crucible)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return stack.is(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get())
                    || stack.is(Items.STRING)
                    ? ItemInteractionResult.SUCCESS
                    : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        int consumed = 0;
        if (stack.is(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get())) {
            consumed = crucible.insertImpureSilicon(stack.getCount());
        } else if (stack.is(Items.STRING) && crucible.useString()) {
            consumed = 1;
        }
        if (consumed == 0 && !stack.is(MaterialRegistration.MATERIAL_ITEMS.get("impure_silicon_ingot").get())
                && !stack.is(Items.STRING)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(consumed);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ItemEntity item
                && entity.getY() - pos.getY() < 0.2D
                && level.getBlockEntity(pos) instanceof QuartzCrucibleBlockEntity crucible) {
            crucible.absorbDroppedItem(item);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuartzCrucibleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, EnvironmentMachineRegistration.QUARTZ_CRUCIBLE_BLOCK_ENTITY.get(),
                        QuartzCrucibleBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof QuartzCrucibleBlockEntity crucible) {
            ItemStack contents = crucible.removeImpureSilicon();
            if (!contents.isEmpty()) {
                Containers.dropItemStack(
                        level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, contents);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
