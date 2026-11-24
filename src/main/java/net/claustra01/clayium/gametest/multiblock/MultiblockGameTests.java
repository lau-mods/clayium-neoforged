package net.claustra01.clayium.gametest.multiblock;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.laser.ClayEnergyLaserBlock;
import net.claustra01.clayium.laser.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.multiblock.MultiblockControllerBlockEntity;
import net.claustra01.clayium.multiblock.MultiblockMachineType;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.LaserRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiblockGameTests {
    private static final BlockPos CONTROLLER = new BlockPos(3, 3, 2);
    private static final Direction FACING = Direction.NORTH;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void blastFurnaceRejectsMissingLowTierAndMisplacedInterfaces(GameTestHelper helper) {
        build(helper, MultiblockMachineType.BLAST_FURNACE, 6, false, false, true);
        require(helper.getBlockState(CONTROLLER).isAir(), "The missing blast-furnace controller was present");
        IItemHandler missingRoute = interfaceAt(helper, itemInterfacePosition()).itemHandler(null);
        ItemStack missingInput = new ItemStack(material("industrial_clay_dust"), 2);
        require(missingRoute == null || missingRoute.insertItem(0, missingInput, false).getCount() == 2,
                "A missing controller accepted material through its interface");

        MultiblockControllerBlockEntity lowTier = build(
                helper, MultiblockMachineType.BLAST_FURNACE, 5, true, false, true);
        insertBlastInput(helper, lowTier);
        tick(lowTier);
        require(!lowTier.isFormed() && lowTier.activeRecipe() == null,
                "A blast furnace made from under-tier hulls started processing");

        MultiblockControllerBlockEntity misplaced = build(
                helper, MultiblockMachineType.BLAST_FURNACE, 6, true, true, false);
        BlockPos misplacedPosition = CONTROLLER.relative(FACING.getOpposite()).offset(1, 1, 1);
        tick(misplaced);
        SynchronizedTarget expectedTarget = SynchronizedTarget.at(
                helper.getLevel(), helper.absolutePos(CONTROLLER));
        require(expectedTarget.equals(interfaceAt(helper, misplacedPosition).target()),
                "The position-independent item interface was not auto-linked");
        require(expectedTarget.equals(interfaceAt(helper, redstoneInterfacePosition()).target()),
                "The redstone interface was not auto-linked");
        IItemHandler misplacedRoute = interfaceAt(helper, misplacedPosition).itemHandler(null);
        require(misplacedRoute.insertItem(0, new ItemStack(material("industrial_clay_dust"), 2), false).isEmpty(),
                "The misplaced interface did not route its input");
        misplacedRoute.insertItem(1, new ItemStack(material("impure_manganese_dust")), false);
        misplaced.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000L));
        tick(misplaced);
        require(misplaced.isFormed() && misplaced.activeRecipe() != null,
                "The original position-independent item interface did not auto-link to the furnace");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void blastFurnaceProcessesOnlyThroughItsSynchronizedInterface(GameTestHelper helper) {
        MultiblockControllerBlockEntity furnace = build(
                helper, MultiblockMachineType.BLAST_FURNACE, 6, true, false, false);
        InterfaceBlockEntity itemInterface = interfaceAt(helper, itemInterfacePosition());
        tick(furnace);
        require(SynchronizedTarget.at(helper.getLevel(), helper.absolutePos(CONTROLLER)).equals(itemInterface.target()),
                "The original structure scan did not auto-link its item interface");
        IItemHandler routed = itemInterface.itemHandler(null);
        require(routed.insertItem(0, new ItemStack(material("industrial_clay_dust"), 2), false).isEmpty()
                        && routed.insertItem(1, new ItemStack(material("impure_manganese_dust")), false).isEmpty(),
                "The synchronized item interface did not route the blast-furnace input");
        furnace.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000L));
        tick(furnace);
        require(furnace.isFormed() && furnace.activeRecipe() != null,
                "A correct blast furnace did not start its high-temperature recipe");
        for (int tick = 0; tick < 400 && furnace.activeRecipe() != null; tick++) {
            tick(furnace);
        }
        ItemStack result = routed.extractItem(furnace.inputSlots(), 2, false);
        require(result.is(material("clay_steel_ingot")) && result.getCount() == 2,
                "The blast-furnace output was not available through its specified item interface");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void blastFurnaceStartsAdvancedClaySteelRecipe(GameTestHelper helper) {
        MultiblockControllerBlockEntity furnace = build(
                helper, MultiblockMachineType.BLAST_FURNACE, 7, true, false, true);
        IItemHandler routed = interfaceAt(helper, itemInterfacePosition()).itemHandler(null);
        require(routed.insertItem(0, new ItemStack(material("advanced_industrial_clay_dust")), false).isEmpty()
                        && routed.insertItem(1, new ItemStack(material("impure_manganese_dust")), false).isEmpty(),
                "The Tier 7 blast-furnace ingredients did not enter through the item interface");
        furnace.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000L));
        tick(furnace);
        require(furnace.isFormed() && furnace.activeRecipe() != null,
                "The original advanced clay-steel recipe did not start at Tier 7");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void blastFurnaceStartsOriginalDustSmeltingRecipe(GameTestHelper helper) {
        MultiblockControllerBlockEntity furnace = build(
                helper, MultiblockMachineType.BLAST_FURNACE, 6, true, false, true);
        IItemHandler routed = interfaceAt(helper, itemInterfacePosition()).itemHandler(null);
        require(routed.insertItem(0, new ItemStack(material("clay_steel_dust")), false).isEmpty(),
                "The clay-steel dust did not enter through the synchronized interface");
        furnace.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000L));
        tick(furnace);
        require(furnace.isFormed() && furnace.activeRecipe() != null,
                "The original high-temperature dust recipe did not start");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void everyShellChangeInvalidatesTheCachedStructure(GameTestHelper helper) {
        MultiblockControllerBlockEntity furnace = build(
                helper, MultiblockMachineType.BLAST_FURNACE, 6, true, false, true);
        tick(furnace);
        require(furnace.isFormed(), "The complete blast furnace did not form");
        helper.setBlock(CONTROLLER.relative(FACING.getOpposite()), Blocks.STONE);
        tick(furnace);
        require(!furnace.isFormed(), "Filling the hollow center did not invalidate the structure");

        furnace = build(helper, MultiblockMachineType.BLAST_FURNACE, 6, true, false, true);
        tick(furnace);
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.AIR);
        tick(furnace);
        require(!furnace.isFormed(), "Removing a precision hull did not invalidate the structure");

        furnace = build(helper, MultiblockMachineType.BLAST_FURNACE, 6, true, false, true);
        tick(furnace);
        helper.setBlock(itemInterfacePosition(), hull(6));
        tick(furnace);
        require(furnace.isFormed(),
                "Replacing the original optional item interface with a hull invalidated the structure");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void reactorLaserAcceleratesAndStructureDamageStopsProcessing(GameTestHelper helper) {
        MultiblockControllerBlockEntity reactor = build(
                helper, MultiblockMachineType.REACTOR, 7, true, false, true);
        InterfaceBlockEntity itemInterface = interfaceAt(helper, itemInterfacePosition());
        IItemHandler routed = itemInterface.itemHandler(null);
        require(routed.insertItem(0, new ItemStack(component("integrated_circuit"), 6), false).isEmpty()
                        && routed.insertItem(1, new ItemStack(material("excited_clay_dust")), false).isEmpty(),
                "The reactor interface did not route both original inputs");
        reactor.receiveClayEnergy(ClayEnergy.ofInternal(10_000_000_000L));
        tick(reactor);
        require(reactor.isFormed() && reactor.activeRecipe() != null,
                "A correct clay reactor did not start the reactor recipe");

        long validationCount = reactor.structureValidationCount();
        long unacceleratedStart = reactor.progress();
        tick(reactor);
        long unacceleratedProgress = reactor.progress() - unacceleratedStart;
        require(reactor.structureValidationCount() == validationCount,
                "The cached 3x3x3 structure was rescanned every processing tick");

        BlockPos laserPos = laserInterfacePosition().below();
        helper.setBlock(laserPos, LaserRegistration.LASERS.get(7).block().get().defaultBlockState()
                .setValue(ClayEnergyLaserBlock.FACING, Direction.UP));
        ClayEnergyLaserBlockEntity laser = helper.getBlockEntity(laserPos);
        laser.energyStorage().add(ClayEnergy.ofInternal(1_000_000L));
        ClayEnergyLaserBlockEntity.serverTick(helper.getLevel(), laser.getBlockPos(), laser.getBlockState(), laser);
        long acceleratedStart = reactor.progress();
        tick(reactor);
        require(reactor.progress() - acceleratedStart == 2 * unacceleratedProgress,
                "One blue beam must add floor(1.460226...) = 1 craft tick to each normal tick");

        helper.setBlock(new BlockPos(2, 2, 2), Blocks.AIR);
        tick(reactor);
        require(!reactor.isFormed() && reactor.activeRecipe() == null,
                "The reactor continued processing after a hull was removed");
        for (int tick = 0; tick < 5; tick++) {
            tick(reactor);
        }
        require(reactor.progress() == 0L
                        && reactor.inventory().getStackInSlot(0).getCount() == 6
                        && reactor.inventory().getStackInSlot(1).getCount() == 1,
                "The broken reactor advanced or failed to return its reserved inputs");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void reactorRequiresOnlyItsLaserAndProcessesWithoutSynchronizer(GameTestHelper helper) {
        MultiblockControllerBlockEntity reactor = build(
                helper, MultiblockMachineType.REACTOR, 7, true, false, false);
        helper.setBlock(laserInterfacePosition(), hull(7));
        tick(reactor);
        require(!reactor.isFormed(), "A clay reactor formed without its required laser interface");

        reactor = build(helper, MultiblockMachineType.REACTOR, 7, true, false, false);
        helper.setBlock(itemInterfacePosition(), hull(7));
        helper.setBlock(redstoneInterfacePosition(), hull(7));
        tick(reactor);
        InterfaceBlockEntity laserInterface = interfaceAt(helper, laserInterfacePosition());
        require(reactor.isFormed(),
                "A clay reactor without optional item/redstone interfaces did not form");
        require(SynchronizedTarget.at(helper.getLevel(), helper.absolutePos(CONTROLLER))
                        .equals(laserInterface.target()),
                "The required laser interface was not auto-linked during structure inspection");

        require(reactor.inventory().insertItem(
                        0, new ItemStack(component("integrated_circuit"), 6), false).isEmpty()
                        && reactor.inventory().insertItem(
                                1, new ItemStack(material("excited_clay_dust")), false).isEmpty(),
                "The reactor did not accept direct GUI-equivalent input without an item interface");
        reactor.receiveClayEnergy(ClayEnergy.ofInternal(10_000_000_000L));
        tick(reactor);
        require(reactor.activeRecipe() != null,
                "The reactor did not start manual processing without a synchronizer");
        helper.succeed();
    }

    private static MultiblockControllerBlockEntity build(
            GameTestHelper helper,
            MultiblockMachineType type,
            int hullTier,
            boolean controllerPresent,
            boolean misplacedItemInterface,
            boolean synchronizeInterfaces) {
        clear(helper);
        BlockPos center = CONTROLLER.relative(FACING.getOpposite());
        Block hull = hull(hullTier);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        helper.setBlock(center.offset(x, y, z), hull);
                    }
                }
            }
        }
        if (controllerPresent) {
            helper.setBlock(CONTROLLER, MultiblockRegistration.CONTROLLERS.get(type).get().defaultBlockState()
                    .setValue(ClayMachineBlock.FACING, FACING));
        } else {
            helper.setBlock(CONTROLLER, Blocks.AIR);
        }
        BlockPos itemPosition = misplacedItemInterface ? center.offset(1, 1, 1) : itemInterfacePosition();
        placeInterface(helper, itemPosition, InterfaceType.ITEM, Math.max(type.minimumTier(), hullTier));
        placeInterface(helper, redstoneInterfacePosition(), InterfaceType.REDSTONE, Math.max(type.minimumTier(), hullTier));
        if (type.requiresLaserInterface()) {
            placeInterface(helper, laserInterfacePosition(), InterfaceType.LASER, Math.max(type.minimumTier(), hullTier));
        }
        if (controllerPresent && synchronizeInterfaces) {
            synchronize(helper, itemPosition);
            synchronize(helper, redstoneInterfacePosition());
            if (type.requiresLaserInterface()) {
                synchronize(helper, laserInterfacePosition());
            }
        }
        return controllerPresent ? helper.getBlockEntity(CONTROLLER) : null;
    }

    private static void placeInterface(
            GameTestHelper helper, BlockPos position, InterfaceType type, int tier) {
        var definition = InterfaceRegistration.INTERFACES.get(type.idStem() + "_tier_" + tier);
        helper.setBlock(position, definition.block().get().defaultBlockState());
    }

    private static void synchronize(GameTestHelper helper, BlockPos position) {
        interfaceAt(helper, position).synchronize(
                SynchronizedTarget.at(helper.getLevel(), helper.absolutePos(CONTROLLER)), null);
    }

    private static void insertBlastInput(GameTestHelper helper, MultiblockControllerBlockEntity furnace) {
        InterfaceBlockEntity itemInterface = interfaceAt(helper, itemInterfacePosition());
        itemInterface.itemHandler(null).insertItem(0, new ItemStack(material("industrial_clay_dust"), 2), false);
        itemInterface.itemHandler(null).insertItem(1, new ItemStack(material("impure_manganese_dust")), false);
        furnace.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000L));
    }

    private static InterfaceBlockEntity interfaceAt(GameTestHelper helper, BlockPos position) {
        return helper.getBlockEntity(position);
    }

    private static BlockPos itemInterfacePosition() {
        return CONTROLLER.relative(FACING.getOpposite(), 2);
    }

    private static BlockPos redstoneInterfacePosition() {
        return CONTROLLER.relative(FACING.getOpposite()).above();
    }

    private static BlockPos laserInterfacePosition() {
        return CONTROLLER.relative(FACING.getOpposite()).below();
    }

    private static Block hull(int tier) {
        return AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get();
    }

    private static net.minecraft.world.item.Item material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static net.minecraft.world.item.Item component(String id) {
        return MaterialRegistration.COMPONENTS.get(id).get();
    }

    private static void tick(MultiblockControllerBlockEntity controller) {
        controller.tickServer(controller.getLevel().random);
    }

    private static void clear(GameTestHelper helper) {
        for (int x = 1; x <= 5; x++) {
            for (int y = 1; y <= 5; y++) {
                for (int z = 1; z <= 5; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private MultiblockGameTests() {
    }
}
