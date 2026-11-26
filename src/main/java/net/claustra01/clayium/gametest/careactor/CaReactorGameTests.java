package net.claustra01.clayium.gametest.careactor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlock;
import net.claustra01.clayium.multiblock.careactor.CaReactorFormula;
import net.claustra01.clayium.machine.iface.InterfaceBlockEntity;
import net.claustra01.clayium.machine.iface.SynchronizedTarget;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CaReactorGameTests {
    private static final BlockPos CORE = new BlockPos(10, 6, 10);

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void reactorAcceptsOnlyCoveredTierMatchedRingsAndInvalidatesItsCache(GameTestHelper helper) {
        require(CaReactorRegistration.CORES.size() == 4
                        && CaReactorRegistration.COILS.size() == 4
                        && CaReactorRegistration.HULLS.size() == 10,
                "The four cores, four coils, and ten hull ranks are not registered");
        BuildResult valid = build(helper, 2, 10, 1);
        CaReactorCoreBlockEntity core = valid.core();
        tick(helper, core);
        require(core.isFormed() && core.coilCount() == 16 && core.hullCount() >= 50,
                "A fully covered tier-matched ring did not form: " + core.structureFailure());
        require(core.getBlockState().getValue(CaReactorCoreBlock.FORMED),
                "The formed reactor did not publish its visible block state");
        var player = helper.makeMockServerPlayerInLevel();
        ClayMachineMenu menu = (ClayMachineMenu)core.createMenu(0, player.getInventory(), player);
        require(menu.reactorFormed() && menu.reactorCoils() == 16
                        && menu.reactorHulls() == core.hullCount() && menu.reactorAverageRank() == 0.0D,
                "The menu did not expose the formed reactor structure to its client data slots");
        CompoundTag update = core.getUpdateTag(helper.getLevel().registryAccess());
        CaReactorCoreBlockEntity display = new CaReactorCoreBlockEntity(core.getBlockPos(), core.getBlockState());
        display.loadWithComponents(update, helper.getLevel().registryAccess());
        require(display.isFormed() && display.coilCount() == 16
                        && display.hullCount() == core.hullCount() && display.averageRank() == 0.0D,
                "The block-entity update did not restore the formed client display");
        CompoundTag packet = ((ClientboundBlockEntityDataPacket)core.getUpdatePacket()).getTag();
        require(packet.getBoolean("reactor_formed") && packet.getInt("reactor_coils") == 16
                        && packet.getInt("reactor_hulls") == core.hullCount()
                        && packet.getDouble("reactor_average_rank") == 0.0D,
                "The block update packet omitted the reactor display state");
        start(core);
        require(core.activeRecipe() != null, "The valid reactor did not start its recipe");
        long validations = core.structureValidationCount();
        tick(helper, core);
        require(core.structureValidationCount() == validations,
                "An unchanged reactor repeated its expensive structure scan");

        BlockPos removedHull = valid.hulls().getFirst();
        helper.setBlock(removedHull, Blocks.AIR);
        tick(helper, core);
        require(!core.isFormed() && core.structureValidationCount() == validations + 1,
                "Removing a cached component did not invalidate and reject the structure");
        require(!core.getBlockState().getValue(CaReactorCoreBlock.FORMED),
                "The broken reactor kept its formed visible block state");

        helper.setBlock(removedHull, CaReactorRegistration.HULLS.get(1).block().get());
        tick(helper, core);
        require(core.isFormed() && core.structureValidationCount() == validations + 2,
                "Restoring a hull did not form the reactor after one validation");

        InterfaceBlockEntity itemInterface = helper.getBlockEntity(CORE.offset(0, 0, 2));
        CompoundTag changedTarget = itemInterface.saveWithoutMetadata(helper.getLevel().registryAccess());
        changedTarget.remove("target_pos");
        itemInterface.loadWithComponents(changedTarget, helper.getLevel().registryAccess());
        tick(helper, core);
        require(!core.isFormed(), "Changing an interface target through block data kept the reactor formed");
        changedTarget.putLong("target_pos", core.getBlockPos().asLong());
        itemInterface.loadWithComponents(changedTarget, helper.getLevel().registryAccess());
        tick(helper, core);
        require(core.isFormed(), "Restoring the interface target through block data did not reform the reactor");

        BuildResult brokenRing = build(helper, 2, 10, 1);
        helper.setBlock(brokenRing.coils().getFirst(), Blocks.AIR);
        tick(helper, brokenRing.core());
        reject(helper, brokenRing.core(), "A broken coil ring was accepted");

        BuildResult wrongTier = build(helper, 2, 10, 1);
        helper.setBlock(wrongTier.coils().getFirst(), CaReactorRegistration.COILS.get(11).block().get());
        tick(helper, wrongTier.core());
        reject(helper, wrongTier.core(), "A mismatched coil tier was accepted");

        BuildResult tooFewHulls = build(helper, 2, 10, 1);
        for (int index = 0; index < 14; index++) {
            BlockPos position = tooFewHulls.hulls().get(index);
            helper.setBlock(position,
                    InterfaceRegistration.INTERFACES.get("redstone_interface_tier_10").block().get());
            ((InterfaceBlockEntity)helper.getBlockEntity(position)).synchronize(
                    SynchronizedTarget.at(helper.getLevel(),
                            ((CaReactorCoreBlockEntity)helper.getBlockEntity(CORE)).getBlockPos()), null);
        }
        tick(helper, tooFewHulls.core());
        reject(helper, tooFewHulls.core(), "A covered reactor with fewer than 50 hulls was accepted");

        BuildResult unlinked = build(helper, 2, 10, 1);
        ((InterfaceBlockEntity)helper.getBlockEntity(CORE.offset(0, 0, 2))).disconnect();
        reject(helper, unlinked.core(), "An unlinked item interface was accepted");

        BuildResult lowInterface = build(helper, 2, 11, 1);
        helper.setBlock(CORE.offset(0, 0, 2),
                InterfaceRegistration.INTERFACES.get("clay_interface_tier_10").block().get());
        reject(helper, lowInterface.core(), "An interface below the core tier was accepted");

        BuildResult highHull = build(helper, 2, 10, 1);
        helper.setBlock(highHull.hulls().getFirst(), CaReactorRegistration.HULLS.get(3).block().get());
        reject(helper, highHull.core(), "A hull above the core rank limit was accepted");

        BuildResult branch = build(helper, 2, 10, 1);
        helper.setBlock(CORE.offset(1, 0, 2), CaReactorRegistration.COILS.get(10).block().get());
        reject(helper, branch.core(), "A branching coil path was accepted");

        BuildResult shared = build(helper, 2, 10, 1);
        helper.setBlock(CORE.offset(3, 0, 3), CaReactorRegistration.CORES.get(10).block().get());
        reject(helper, shared.core(), "A second core shared the same ring");

        build(helper, 2, 10, 1);
        helper.setBlock(CORE.offset(0, 0, -4), CaReactorRegistration.CORES.get(10).block().get());
        CaReactorCoreBlockEntity detached = helper.getBlockEntity(CORE.offset(0, 0, -4));
        reject(helper, detached, "A detached core claimed a distant ring");

        BlockPos.betweenClosedStream(CORE.offset(-8, -3, -8), CORE.offset(8, 3, 8))
                .forEach(pos -> helper.setBlock(pos, Blocks.AIR));
        BlockPos.betweenClosedStream(CORE.offset(1, 0, 1), CORE.offset(19, 0, 13))
                .forEach(pos -> helper.setBlock(pos, Blocks.AIR));
        helper.setBlock(CORE, CaReactorRegistration.CORES.get(10).block().get());
        Block coil = CaReactorRegistration.COILS.get(10).block().get();
        for (int row = 0; row < 7; row++) {
            for (int x = 1; x <= 19; x++) {
                helper.setBlock(CORE.offset(x, 0, 1 + row * 2), coil);
            }
            if (row < 6) {
                helper.setBlock(CORE.offset(row % 2 == 0 ? 19 : 1, 0, 2 + row * 2), coil);
            }
        }
        CaReactorCoreBlockEntity overlong = helper.getBlockEntity(CORE);
        reject(helper, overlong, "A coil path above 128 blocks was accepted");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void reactorUsesHotfixTwoFormulaCompressionEnergyTimeAndSafeStops(GameTestHelper helper) {
        CaReactorFormula formula = CaReactorFormula.calculate(50, 2.0D).orElseThrow();
        requireClose(formula.efficiency(), 0.2D * Math.pow(1.02D, 50) * Math.pow(7.5D, 2),
                "The hotfix2 efficiency 0.2*1.02^N*7.5^R changed");
        requireClose(formula.energyMultiplier(), Math.pow(1.01D, 100),
                "The CA reactor energy multiplier changed");
        require(formula.compressionRank() == 2, "The average rank did not select compression rank two");
        require(CaReactorFormula.calculate(50, Double.NaN).isEmpty()
                        && CaReactorFormula.calculate(-1, 1.0D).isEmpty()
                        && CaReactorFormula.calculate(Integer.MAX_VALUE, Double.MAX_VALUE).isEmpty(),
                "Non-finite or negative formula inputs were not rejected");
        require(formula.scale(Long.MAX_VALUE, Long.MAX_VALUE).isEmpty(),
                "A process exceeding long range was not stopped safely");

        ProcessResult rankZero = process(helper, 2, 1);
        ProcessResult rankOne = process(helper, 3, 2);
        require(rankZero.hulls() != rankOne.hulls() && rankZero.rank() != rankOne.rank(),
                "The comparison reactors did not have distinct N and R");
        for (ProcessResult result : List.of(rankZero, rankOne)) {
            double efficiency = 0.2D * Math.pow(1.02D, result.hulls()) * Math.pow(7.5D, result.rank());
            long timeToCraft = (long)(1000.0D * 20 * Math.pow(9.0D, (int)result.rank()));
            long expectedTicks = (long)Math.ceil((double)timeToCraft / Math.floor(1000.0D * efficiency));
            long expectedEnergyPerTick = (long)Math.floor(100_000_000.0D
                    * Math.pow(1.01D, result.hulls() * result.rank()));
            require(result.requiredProgress() == expectedTicks && result.elapsedTicks() == expectedTicks,
                    "The reactor did not take the hotfix2 processing time");
            require(result.consumedEnergy() == expectedEnergyPerTick * expectedTicks,
                    "The reactor did not consume the hotfix2 CE amount");
            require(result.output().getCount() == 1,
                    "The reactor did not produce exactly one item per craft");
        }
        require(rankZero.output().is(MaterialRegistration.MATERIAL_ITEMS.get("pure_antimatter").get())
                        && rankOne.output().is(MaterialRegistration.MATERIAL_ITEMS.get(
                                "compressed_pure_antimatter").get()),
                "Runtime output did not use the structure compression rank");
        helper.succeed();
    }

    private static void start(CaReactorCoreBlockEntity core) {
        core.inventory().setStackInSlot(0,
                new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
        core.receiveClayEnergy(ClayEnergy.ofInternal(Long.MAX_VALUE / 2));
        core.tickServer(net.minecraft.util.RandomSource.create(1L));
    }

    private static void reject(GameTestHelper helper, CaReactorCoreBlockEntity core, String message) {
        start(core);
        tick(helper, core);
        require(!core.isFormed() && core.activeRecipe() == null
                        && core.inventory().getStackInSlot(0).getCount() == 1,
                message);
    }

    private static ProcessResult process(GameTestHelper helper, int radius, int rank) {
        BuildResult build = build(helper, radius, 10, rank);
        CaReactorCoreBlockEntity core = build.core();
        core.inventory().setStackInSlot(
                0, new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()));
        core.receiveClayEnergy(ClayEnergy.ofInternal(Long.MAX_VALUE));
        tick(helper, core);
        require(core.isFormed() && core.activeRecipe() != null,
                "A valid CA reactor did not start: " + core.structureFailure());
        long requiredProgress = core.requiredProgress();
        long beforeEnergy = core.energyStorage().stored().internalValue();
        long elapsed = 0L;
        while (elapsed <= requiredProgress && core.inventory().getStackInSlot(core.outputSlot()).isEmpty()) {
            tick(helper, core);
            elapsed++;
        }
        ItemStack output = core.inventory().getStackInSlot(core.outputSlot()).copy();
        require(!output.isEmpty(), "The CA reactor did not finish its pure-antimatter process");
        return new ProcessResult(core.hullCount(), core.averageRank(), requiredProgress, elapsed,
                beforeEnergy - core.energyStorage().stored().internalValue(), output);
    }

    private static BuildResult build(GameTestHelper helper, int radius, int tier, int rank) {
        BlockPos.betweenClosedStream(CORE.offset(-8, -3, -8), CORE.offset(8, 3, 8))
                .forEach(pos -> helper.setBlock(pos, Blocks.AIR));
        helper.setBlock(CORE, CaReactorRegistration.CORES.get(tier).block().get());
        BlockPos ringCenter = CORE.offset(0, 0, radius + 1);
        Set<BlockPos> coilSet = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) == radius) {
                    coilSet.add(ringCenter.offset(x, 0, z));
                }
            }
        }
        Block coil = CaReactorRegistration.COILS.get(tier).block().get();
        Block hull = CaReactorRegistration.HULLS.get(rank).block().get();
        coilSet.forEach(pos -> helper.setBlock(pos, coil));
        Set<BlockPos> hullSet = new HashSet<>();
        for (BlockPos coilPos : coilSet) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = coilPos.relative(direction);
                if (!coilSet.contains(neighbor) && !neighbor.equals(CORE)) {
                    hullSet.add(neighbor.immutable());
                }
            }
        }
        hullSet.forEach(pos -> helper.setBlock(pos, hull));
        BlockPos itemInterface = ringCenter.offset(0, 0, -radius + 1);
        BlockPos laserInterface = ringCenter.offset(0, 0, radius - 1);
        helper.setBlock(itemInterface,
                InterfaceRegistration.INTERFACES.get("clay_interface_tier_" + tier).block().get());
        helper.setBlock(laserInterface,
                InterfaceRegistration.INTERFACES.get("clay_laser_interface_tier_" + tier).block().get());
        ((InterfaceBlockEntity)helper.getBlockEntity(itemInterface)).synchronize(
                SynchronizedTarget.at(helper.getLevel(),
                            ((CaReactorCoreBlockEntity)helper.getBlockEntity(CORE)).getBlockPos()), null);
        ((InterfaceBlockEntity)helper.getBlockEntity(laserInterface)).synchronize(
                SynchronizedTarget.at(helper.getLevel(),
                            ((CaReactorCoreBlockEntity)helper.getBlockEntity(CORE)).getBlockPos()), null);
        hullSet.remove(itemInterface);
        hullSet.remove(laserInterface);
        return new BuildResult(
                helper.getBlockEntity(CORE), new ArrayList<>(coilSet), new ArrayList<>(hullSet));
    }

    private static void tick(GameTestHelper helper, CaReactorCoreBlockEntity core) {
        CaReactorCoreBlockEntity.serverTick(
                helper.getLevel(), core.getBlockPos(), core.getBlockState(), core);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static void requireClose(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > Math.max(1.0E-9D, Math.abs(expected) * 1.0E-12D)) {
            throw new GameTestAssertException(message + ": " + actual + " != " + expected);
        }
    }

    private record BuildResult(
            CaReactorCoreBlockEntity core, List<BlockPos> coils, List<BlockPos> hulls) {
    }

    private record ProcessResult(
            int hulls, double rank, long requiredProgress, long elapsedTicks,
            long consumedEnergy, ItemStack output) {
    }

    private CaReactorGameTests() {
    }
}
