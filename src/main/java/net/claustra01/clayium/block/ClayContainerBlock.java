package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.BufferBlockEntity;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.blockentity.ClayInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.DistributorBlockEntity;
import net.claustra01.clayium.blockentity.FluidTranslatorBlockEntity;
import net.claustra01.clayium.blockentity.IGeneralInterface;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.blockentity.VacuumContainerBlockEntity;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Base block for Clayium machines, buffers, interfaces, and special containers (metaMode=0: no facing property).
 */
public class ClayContainerBlock extends Block implements ITieredBlock, EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty PIPE_MODE = BooleanProperty.create("pipe_mode");

    private static final VoxelShape PIPE_CORE_SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape PIPE_ARM_NORTH = Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
    private static final VoxelShape PIPE_ARM_SOUTH = Block.box(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
    private static final VoxelShape PIPE_ARM_WEST = Block.box(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);
    private static final VoxelShape PIPE_ARM_EAST = Block.box(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
    private static final VoxelShape PIPE_ARM_DOWN = Block.box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);
    private static final VoxelShape PIPE_ARM_UP = Block.box(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);

    public enum FacingMode {
        NONE,
        HORIZONTAL,
        ALL
    }

    public enum OverlayMode {
        FRONT_ONLY,
        ALL_FACES,
        TOP_ONLY,
        FRONT_AND_BACK_MINER,
        PAN_DUPLICATOR,
        AUTO_CRAFTER,
        AUTO_TRADER,
        STORAGE_CONTAINER,
        VACUUM_CONTAINER
    }

    private final int tier;
    private final String baseTexture;
    @Nullable
    private final String overlayTexture;
    @Nullable
    private final String categoryKey;
    @Nullable
    private final String tooltipKey;
    private final OverlayMode overlayMode;

    public ClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey) {
        this(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, OverlayMode.FRONT_ONLY);
    }

    public ClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey,
            OverlayMode overlayMode) {
        super(properties);
        this.tier = tier;
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
        this.categoryKey = categoryKey;
        this.tooltipKey = tooltipKey;
        this.overlayMode = overlayMode;
        if (this.stateDefinition.getProperty("pipe_mode") != null) {
            this.registerDefaultState(this.defaultBlockState().setValue(PIPE_MODE, Boolean.FALSE));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PIPE_MODE);
    }

    public boolean canBePipe() {
        return true;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public FacingMode getFacingMode() {
        return FacingMode.NONE;
    }

    public Direction getFacing(BlockState state) {
        return Direction.NORTH;
    }

    public boolean hasActiveOverlay() {
        return false;
    }

    public OverlayMode getOverlayMode() {
        return this.overlayMode;
    }

    public String getBaseTexture() {
        return this.baseTexture;
    }

    @Nullable
    public String getOverlayTexture() {
        return this.overlayTexture;
    }

    @Nullable
    public String getCategoryKey() {
        return this.categoryKey;
    }

    @Nullable
    public String getTooltipKey() {
        return this.tooltipKey;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.hasProperty(PIPE_MODE) || !state.getValue(PIPE_MODE)) {
            return Shapes.block();
        }
        VoxelShape shape = PIPE_CORE_SHAPE;
        for (Direction dir : Direction.values()) {
            if (isPipeConnected(level, pos, dir)) {
                shape = Shapes.or(shape, armForDirection(dir));
            }
        }
        return shape;
    }

    public boolean isPipeConnected(BlockGetter level, BlockPos pos, Direction dir) {
        BlockEntity self = level.getBlockEntity(pos);
        BlockEntity other = level.getBlockEntity(pos.relative(dir));
        if (self == null || other == null) {
            return false;
        }
        int impSelf = connectionScoreImport(self, dir, other);
        int expOther = connectionScoreExport(other, dir.getOpposite(), self);
        if (impSelf >= 0 && expOther >= 0 && impSelf + expOther >= 1) {
            return true;
        }
        int expSelf = connectionScoreExport(self, dir, other);
        int impOther = connectionScoreImport(other, dir.getOpposite(), self);
        return expSelf >= 0 && impOther >= 0 && expSelf + impOther >= 1;
    }

    private static int connectionScoreImport(BlockEntity self, Direction dir, BlockEntity other) {
        if (self instanceof FluidTranslatorBlockEntity && other instanceof FluidTranslatorBlockEntity == false
                && other.getLevel() != null
                && other.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, other.getBlockPos(), dir.getOpposite()) != null) {
            return 2;
        }
        if (self instanceof ClayContainerBlockEntity cc) {
            int route = cc.insertRoutes[cc.toRelativeSide(dir).id()];
            if (route != ClayContainerBlockEntity.ROUTE_DISABLED) {
                return cc.autoExtract ? 1 : 0;
            }
            if (cc instanceof BufferBlockEntity || cc instanceof MultitrackBufferBlockEntity || cc instanceof StorageContainerBlockEntity) {
                return 0;
            }
            return -1;
        }
        return 1;
    }

    private static int connectionScoreExport(BlockEntity self, Direction dir, BlockEntity other) {
        if (self instanceof FluidTranslatorBlockEntity && !(other instanceof FluidTranslatorBlockEntity)
                && other.getLevel() != null
                && other.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, other.getBlockPos(), dir.getOpposite()) != null) {
            return 2;
        }
        if (self instanceof ClayContainerBlockEntity cc) {
            int route = cc.extractRoutes[cc.toRelativeSide(dir).id()];
            if (route != ClayContainerBlockEntity.ROUTE_DISABLED) {
                return cc.autoInsert ? 1 : 0;
            }
            if (cc instanceof BufferBlockEntity || cc instanceof MultitrackBufferBlockEntity || cc instanceof StorageContainerBlockEntity) {
                return 0;
            }
            return -1;
        }
        return 1;
    }

    private static VoxelShape armForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> PIPE_ARM_NORTH;
            case SOUTH -> PIPE_ARM_SOUTH;
            case WEST -> PIPE_ARM_WEST;
            case EAST -> PIPE_ARM_EAST;
            case DOWN -> PIPE_ARM_DOWN;
            case UP -> PIPE_ARM_UP;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IGeneralInterface gen && gen.isUsableTool(stack, player, hit.getDirection())) {
            gen.onToolUse(stack, player, hit.getDirection());
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ClayContainerBlockEntity container) {
                for (int slot : container.getSlotsToDrop()) {
                    ItemStack stack = container.getStack(slot);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                    }
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if ("buffer".equals(this.categoryKey)) {
            if ("tooltip.MultitrackBuffer".equals(this.tooltipKey)) {
                return new MultitrackBufferBlockEntity(pos, state);
            }
            return new BufferBlockEntity(pos, state);
        }
        if ("fluid".equals(this.categoryKey) && "tooltip.FluidTranslator".equals(this.tooltipKey)) {
            return new FluidTranslatorBlockEntity(pos, state);
        }
        if ("storage".equals(this.categoryKey)) {
            return new VacuumContainerBlockEntity(pos, state);
        }
        if ("tooltip.Distributor".equals(this.tooltipKey)) {
            return new DistributorBlockEntity(pos, state);
        }
        if ("tooltip.Interface".equals(this.tooltipKey)) {
            return new ClayInterfaceBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type == ClayiumBlockEntities.BUFFER.get()
                || type == ClayiumBlockEntities.MULTITRACK_BUFFER.get()
                || type == ClayiumBlockEntities.FLUID_TRANSLATOR.get()
                || type == ClayiumBlockEntities.STORAGE_CONTAINER.get()
                || type == ClayiumBlockEntities.VACUUM_CONTAINER.get()
                || type == ClayiumBlockEntities.METAL_CHEST.get()
                || type == ClayiumBlockEntities.DISTRIBUTOR.get()
                || type == ClayiumBlockEntities.CLAY_INTERFACE.get()) {
            return (lvl, p, st, be) -> {
                if (be instanceof ClayContainerBlockEntity container) {
                    container.serverTick();
                }
            };
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> serverType,
            BlockEntityType<E> expectedType,
            BlockEntityTicker<? super E> ticker) {
        return expectedType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }
}
