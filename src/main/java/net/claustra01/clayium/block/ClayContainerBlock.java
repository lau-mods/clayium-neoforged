package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.AreaActivatorBlockEntity;
import net.claustra01.clayium.blockentity.AreaCollectorBlockEntity;
import net.claustra01.clayium.blockentity.AreaMinerBlockEntity;
import net.claustra01.clayium.blockentity.AssemblerBlockEntity;
import net.claustra01.clayium.blockentity.AutoClayCondenserBlockEntity;
import net.claustra01.clayium.blockentity.AutoCrafterBlockEntity;
import net.claustra01.clayium.blockentity.AutoTraderBlockEntity;
import net.claustra01.clayium.blockentity.BlastFurnaceBlockEntity;
import net.claustra01.clayium.blockentity.BufferBlockEntity;
import net.claustra01.clayium.blockentity.CACollectorBlockEntity;
import net.claustra01.clayium.blockentity.CACondenserBlockEntity;
import net.claustra01.clayium.blockentity.CAInjectorBlockEntity;
import net.claustra01.clayium.blockentity.CAReactorBlockEntity;
import net.claustra01.clayium.blockentity.CentrifugeBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalMetalSeparatorBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalReactorBlockEntity;
import net.claustra01.clayium.blockentity.ClayChunkLoaderBlockEntity;
import net.claustra01.clayium.blockentity.ClayContainerBlockEntity;
import net.claustra01.clayium.blockentity.ClayCraftingTableBlockEntity;
import net.claustra01.clayium.blockentity.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.blockentity.ClayFabricatorBlockEntity;
import net.claustra01.clayium.blockentity.ClayInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.blockentity.CreativeEnergySourceBlockEntity;
import net.claustra01.clayium.blockentity.DistributorBlockEntity;
import net.claustra01.clayium.blockentity.FEGeneratorBlockEntity;
import net.claustra01.clayium.blockentity.FluidTranslatorBlockEntity;
import net.claustra01.clayium.blockentity.IGeneralInterface;
import net.claustra01.clayium.blockentity.LaserInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.LaserReflectorBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.blockentity.PANAdapterBlockEntity;
import net.claustra01.clayium.blockentity.PANCoreBlockEntity;
import net.claustra01.clayium.blockentity.PANDuplicatorBlockEntity;
import net.claustra01.clayium.blockentity.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.blockentity.ReactorBlockEntity;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.SaltExtractorBlockEntity;
import net.claustra01.clayium.blockentity.SimpleClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.SolarClayFabricatorBlockEntity;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.blockentity.VacuumContainerBlockEntity;
import net.claustra01.clayium.blockentity.WaterWheelBlockEntity;
import net.claustra01.clayium.menu.AreaActivatorMenu;
import net.claustra01.clayium.menu.AreaMinerMenu;
import net.claustra01.clayium.menu.AutoClayCondenserMenu;
import net.claustra01.clayium.menu.AutoCrafterMenu;
import net.claustra01.clayium.menu.AutoTraderMenu;
import net.claustra01.clayium.menu.ClayCraftingTableMenu;
import net.claustra01.clayium.menu.DistributorMenu;
import net.claustra01.clayium.menu.EnergyOnlyMenu;
import net.claustra01.clayium.menu.FEGeneratorMenu;
import net.claustra01.clayium.menu.MultitrackBufferMenu;
import net.claustra01.clayium.menu.NormalInventoryMenu;
import net.claustra01.clayium.menu.PANAdapterMenu;
import net.claustra01.clayium.menu.PANCoreMenu;
import net.claustra01.clayium.menu.ProcessingMachineMenu;
import net.claustra01.clayium.menu.StorageContainerMenu;
import net.claustra01.clayium.menu.WaterWheelMenu;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
        if (level instanceof Level lvl
                && lvl.getCapability(Capabilities.EnergyStorage.BLOCK, pos, dir) != null
                && lvl.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite()) != null) {
            return true;
        }
        BlockEntity self = level.getBlockEntity(pos);
        BlockEntity other = level.getBlockEntity(pos.relative(dir));
        if (self == null || other == null) {
            return false;
        }
        if (self.getLevel() != null && other.getLevel() != null
                && self.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, pos, dir) != null
                && other.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite()) != null) {
            return true;
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
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return hasContainerMenu(level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return openMenuAt(level, pos, pos, player) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    public static boolean hasContainerMenu(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ClayInterfaceBlockEntity iface) {
            return iface.isSynced() && iface.getCore() != null;
        }
        if (be instanceof ClayChunkLoaderBlockEntity
                || be instanceof LaserInterfaceBlockEntity
                || be instanceof LaserReflectorBlockEntity
                || be instanceof RedstoneInterfaceBlockEntity
                || be instanceof QuartzCrucibleBlockEntity) {
            return false;
        }
        return be instanceof ClayContainerBlockEntity;
    }

    public static boolean openMenuAt(Level level, BlockPos targetPos, BlockPos interactionPos, Player player) {
        BlockEntity be = level.getBlockEntity(targetPos);
        if (be instanceof ClayInterfaceBlockEntity iface) {
            if (!iface.isSynced()) {
                return false;
            }
            BlockEntity core = iface.getCore();
            Level coreLevel = iface.getCoreLevel();
            if (core == null || coreLevel == null || core == iface) {
                return false;
            }
            return openMenuAt(coreLevel, core.getBlockPos(), interactionPos, player);
        }
        if (!(be instanceof ClayContainerBlockEntity container)) {
            return false;
        }

        Component title = container.hasCustomName()
                ? Component.literal(container.getCustomName())
                : container.getBlockState().getBlock().getName();

        if (container instanceof net.claustra01.clayium.blockentity.AbstractClayMachineBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new ProcessingMachineMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof AutoClayCondenserBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new AutoClayCondenserMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof AutoCrafterBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new AutoCrafterMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof AutoTraderBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new AutoTraderMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof MultitrackBufferBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new MultitrackBufferMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof DistributorBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new DistributorMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof StorageContainerBlockEntity storage) {
            boolean vacuum = storage.isVacuumContainer();
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new StorageContainerMenu(id, inv, targetPos, interactionPos, vacuum),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                        buf.writeBoolean(vacuum);
                    });
            return true;
        }
        if (container instanceof WaterWheelBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new WaterWheelMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof ClayCraftingTableBlockEntity) {
            BlockPos chestPos = ClayCraftingTableMenu.findAdjacentChest(level, targetPos);
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new ClayCraftingTableMenu(id, inv, targetPos, interactionPos, chestPos),
                            title),
                    buf -> ClayCraftingTableMenu.writeInitData(buf, targetPos, interactionPos, chestPos));
            return true;
        }
        if (container instanceof SolarClayFabricatorBlockEntity
                || container instanceof ClayFabricatorBlockEntity
                || container instanceof CreativeEnergySourceBlockEntity
                || container instanceof ClayEnergyLaserBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new EnergyOnlyMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof BufferBlockEntity
                || container instanceof net.claustra01.clayium.blockentity.MetalChestBlockEntity
                || container instanceof CACollectorBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new NormalInventoryMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof AreaMinerBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new AreaMinerMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof AreaActivatorBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new AreaActivatorMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof PANAdapterBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new PANAdapterMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof FEGeneratorBlockEntity) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new FEGeneratorMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            return true;
        }
        if (container instanceof PANCoreBlockEntity core) {
            core.refreshNetwork();
            core.refreshItemSet();
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new PANCoreMenu(id, inv, targetPos, interactionPos),
                            title),
                    buf -> {
                        buf.writeBlockPos(targetPos);
                        buf.writeBlockPos(interactionPos);
                    });
            if (player instanceof ServerPlayer sp && sp.containerMenu instanceof PANCoreMenu panMenu) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, panMenu.createSyncPayload());
            }
            return true;
        }
        return false;
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
    public static String resolveBlockEntityCategory(Block block) {
        if (block instanceof ClayMarkerBlock) {
            return "clay_marker";
        }
        if (block instanceof MetalChestBlock) {
            return "metal_chest";
        }
        if (block instanceof RedstoneInterfaceBlock) {
            return "redstone_interface";
        }
        if (block instanceof QuartzCrucibleBlock) {
            return "quartz_crucible";
        }
        if (block instanceof StorageContainerBlock) {
            return "storage_container";
        }
        if (block instanceof ClayWorkTableBlock) {
            return "clay_work_table";
        }
        if (block instanceof ClayCraftingTableBlock) {
            return "clay_crafting_table";
        }
        if (block instanceof PANDuplicatorBlock) {
            return "pan_duplicator";
        }
        if (block instanceof PANCoreBlock) {
            return "pan_core";
        }
        if (block instanceof PANAdapterBlock) {
            return "pan_adapter";
        }
        if (block instanceof FEGeneratorBlock) {
            return "fe_generator";
        }
        if (!(block instanceof ClayContainerBlock cc)) {
            return null;
        }
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if ("buffer".equals(cc.categoryKey)) {
            return "tooltip.MultitrackBuffer".equals(cc.tooltipKey) ? "multitrack_buffer" : "buffer";
        }
        if ("fluid".equals(cc.categoryKey)) {
            if ("tooltip.FluidTranslator".equals(cc.tooltipKey)) {
                return "fluid_translator";
            }
            if ("advanced_fluid_transfer_machine".equals(path)) {
                return "chemical_reactor";
            }
        }
        if ("storage".equals(cc.categoryKey)) {
            return "vacuum_container";
        }
        if ("tooltip.Distributor".equals(cc.tooltipKey)) {
            return "distributor";
        }
        if ("tooltip.Interface".equals(cc.tooltipKey)) {
            return "clay_interface";
        }
        if ("tooltip.LaserInterface".equals(cc.tooltipKey)) {
            return "laser_interface";
        }
        if (block instanceof LaserReflectorBlock) {
            return "laser_reflector";
        }
        if ("laser".equals(cc.categoryKey)) {
            return "clay_energy_laser";
        }
        if ("area".equals(cc.categoryKey)) {
            if ("area_collector".equals(path)) {
                return "area_collector";
            }
            if ("activator".equals(path) || "area_activator".equals(path)) {
                return "area_activator";
            }
            return "area_miner";
        }
        if ("clay_chunk_loader".equals(path)) {
            return "clay_chunk_loader";
        }
        if ("generator".equals(cc.categoryKey)) {
            return "water_wheel";
        }
        if ("fe_generator".equals(cc.categoryKey)) {
            return "fe_generator";
        }
        if ("clay_blast_furnace".equals(path)) {
            return "blast_furnace";
        }
        if ("clay_reactor".equals(path)) {
            return "reactor";
        }
        if (path.endsWith("_ca_reactor_core")) {
            return "ca_reactor";
        }
        if ("creative_ce_source".equals(path)) {
            return "creative_ce_source";
        }
        if (path.startsWith("auto_clay_condenser")) {
            return "auto_clay_condenser";
        }
        if (path.contains("solar_clay_fabricator")) {
            return "solar_clay_fabricator";
        }
        if (path.startsWith("clay_fabricator_")) {
            return "clay_fabricator";
        }
        if ("chemical_metal_separator".equals(path)) {
            return "chemical_metal_separator";
        }
        if (path.endsWith("_cobblestone_generator")) {
            return "cobblestone_generator";
        }
        if (path.endsWith("_salt_extractor")) {
            return "salt_extractor";
        }
        if (path.endsWith("_auto_crafter")) {
            return "auto_crafter";
        }
        if ("auto_trader".equals(path)) {
            return "auto_trader";
        }
        if ("ca_collector".equals(path)) {
            return "ca_collector";
        }
        if (path.endsWith("_ca_condenser")) {
            return "ca_condenser";
        }
        if (path.endsWith("_ca_injector")) {
            return "ca_injector";
        }
        if (path.endsWith("_centrifuge")) {
            return "centrifuge";
        }
        if (path.endsWith("_chemical_reactor")) {
            return "chemical_reactor";
        }
        if (path.endsWith("_assembler") || path.endsWith("_inscriber") || path.endsWith("_alloy_smelter")) {
            return "assembler";
        }
        if ("machine".equals(cc.categoryKey)) {
            return "simple_machine";
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        String key = resolveBlockEntityCategory(state.getBlock());
        if (key == null) {
            return null;
        }
        return switch (key) {
            case "buffer" -> new BufferBlockEntity(pos, state);
            case "multitrack_buffer" -> new MultitrackBufferBlockEntity(pos, state);
            case "fluid_translator" -> new FluidTranslatorBlockEntity(pos, state);
            case "vacuum_container" -> new VacuumContainerBlockEntity(pos, state);
            case "distributor" -> new DistributorBlockEntity(pos, state);
            case "clay_interface" -> new ClayInterfaceBlockEntity(pos, state);
            case "laser_interface" -> new LaserInterfaceBlockEntity(pos, state);
            case "clay_crafting_table" -> new ClayCraftingTableBlockEntity(pos, state);
            case "clay_chunk_loader" -> new ClayChunkLoaderBlockEntity(pos, state);
            case "water_wheel" -> new WaterWheelBlockEntity(pos, state);
            case "blast_furnace" -> new BlastFurnaceBlockEntity(pos, state);
            case "reactor" -> new ReactorBlockEntity(pos, state);
            case "ca_reactor" -> new CAReactorBlockEntity(pos, state);
            case "creative_ce_source" -> new CreativeEnergySourceBlockEntity(pos, state);
            case "auto_clay_condenser" -> new AutoClayCondenserBlockEntity(pos, state);
            case "solar_clay_fabricator" -> new SolarClayFabricatorBlockEntity(pos, state);
            case "clay_fabricator" -> new ClayFabricatorBlockEntity(pos, state);
            case "chemical_metal_separator" -> new ChemicalMetalSeparatorBlockEntity(pos, state);
            case "cobblestone_generator" -> new CobblestoneGeneratorBlockEntity(pos, state);
            case "salt_extractor" -> new SaltExtractorBlockEntity(pos, state);
            case "auto_crafter" -> new AutoCrafterBlockEntity(pos, state);
            case "auto_trader" -> new AutoTraderBlockEntity(pos, state);
            case "ca_collector" -> new CACollectorBlockEntity(pos, state);
            case "ca_condenser" -> new CACondenserBlockEntity(pos, state);
            case "ca_injector" -> new CAInjectorBlockEntity(pos, state);
            case "centrifuge" -> new CentrifugeBlockEntity(pos, state);
            case "chemical_reactor" -> new ChemicalReactorBlockEntity(pos, state);
            case "assembler" -> new AssemblerBlockEntity(pos, state);
            case "simple_machine" -> new SimpleClayMachineBlockEntity(pos, state);
            case "pan_core" -> new PANCoreBlockEntity(pos, state);
            case "pan_adapter" -> new PANAdapterBlockEntity(pos, state);
            case "pan_duplicator" -> new PANDuplicatorBlockEntity(pos, state);
            case "clay_energy_laser" -> new ClayEnergyLaserBlockEntity(pos, state);
            case "laser_reflector" -> new LaserReflectorBlockEntity(pos, state);
            case "area_miner" -> new AreaMinerBlockEntity(pos, state);
            case "area_collector" -> new AreaCollectorBlockEntity(pos, state);
            case "area_activator" -> new AreaActivatorBlockEntity(pos, state);
            case "fe_generator" -> new FEGeneratorBlockEntity(pos, state);
            default -> null;
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (resolveBlockEntityCategory(state.getBlock()) != null
                && type != ClayiumBlockEntities.CLAY_WORK_TABLE.get()
                && type != ClayiumBlockEntities.QUARTZ_CRUCIBLE.get()) {
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
