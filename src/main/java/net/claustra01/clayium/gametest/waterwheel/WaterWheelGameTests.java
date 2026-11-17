package net.claustra01.clayium.gametest.waterwheel;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.waterwheel.WaterWheelBlockEntity;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.claustra01.clayium.util.WaterDetection;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WaterWheelGameTests {
    private static final BlockPos WHEEL_POS = new BlockPos(2, 2, 2);
    private static final BlockPos TARGET_POS = WHEEL_POS.east();

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void flowingWaterUsesOriginalRollWearTierAndSupplyCap(GameTestHelper helper) {
        WaterWheelBlockEntity clayWheel = placeWheel(helper, WaterWheelRegistration.CLAY);
        ClayMachineBlockEntity tierTwo = placeMachine(helper, "condenser_tier_2", TARGET_POS);
        RandomSource dryRandom = RandomSource.create(91L);
        for (int tick = 0; tick < 2_000; tick++) {
            clayWheel.runGenerationTick(dryRandom);
        }
        require(clayWheel.progress() == 0L, "A water wheel progressed without flowing water");
        require(tierTwo.energyStorage().stored().internalValue() == 0L,
                "A water wheel supplied CE without flowing water");

        placeFlowingWater(helper, WHEEL_POS.north(), WHEEL_POS.south(), WHEEL_POS.west(), WHEEL_POS.above());
        clayWheel.invalidateWaterCache();
        require(clayWheel.flowingWaterCount() == 4, "The 3x3x3 scan did not count all flowing water");
        RandomSource clayRandom = RandomSource.create(92L);
        RandomSource expectedRandom = RandomSource.create(92L);
        long expectedProgress = 0L;
        int expectedEfficiency = 1_000;
        long expectedEnergy = 0L;
        for (int tick = 0; tick < 400; tick++) {
            clayWheel.runGenerationTick(clayRandom);
            if (expectedRandom.nextInt(40) < 4) {
                expectedProgress += expectedEfficiency;
                if (expectedProgress >= WaterWheelBlockEntity.PROGRESS_LIMIT) {
                    expectedProgress -= WaterWheelBlockEntity.PROGRESS_LIMIT;
                    if (expectedRandom.nextInt(5) == 0) {
                        expectedEfficiency--;
                    }
                    expectedEnergy = Math.min(5L, expectedEnergy + 1L);
                }
            }
        }
        require(clayWheel.progress() == expectedProgress && clayWheel.efficiency() == expectedEfficiency,
                "Flowing-water progress did not follow random.nextInt(40) and the original efficiency formula");
        require(tierTwo.energyStorage().stored().internalValue() == expectedEnergy,
                "The seeded original generation rolls supplied the wrong CE amount");

        int initialEfficiency = clayWheel.efficiency();
        RandomSource wearRandom = RandomSource.create(94L);
        for (int tick = 0; tick < 20_000; tick++) {
            clayWheel.runGenerationTick(wearRandom);
        }
        require(clayWheel.efficiency() < initialEfficiency,
                "The original 20% efficiency wear did not occur over time");
        require(tierTwo.energyStorage().stored().internalValue() == clayWheel.generationLimit(),
                "Tier 1 generation did not stop at the original five-emission cap");

        CompoundTag saved = clayWheel.saveWithFullMetadata(helper.getLevel().registryAccess());
        WaterWheelBlockEntity loaded = new WaterWheelBlockEntity(
                helper.absolutePos(WHEEL_POS), clayWheel.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.progress() == clayWheel.progress() && loaded.efficiency() == clayWheel.efficiency(),
                "Water-wheel progress or efficiency wear was not persisted");

        WaterWheelBlockEntity denseWheel = placeWheel(helper, WaterWheelRegistration.DENSE_CLAY);
        ClayMachineBlockEntity tierThree = placeMachine(helper, "grinder_tier_3", TARGET_POS);
        ClayMachineBlockEntity tierFour = placeMachine(helper, "grinder_tier_4", WHEEL_POS.west());
        denseWheel.invalidateWaterCache();
        require(denseWheel.generationAmount() == 256L && denseWheel.generationLimit() == 1_280L,
                "Tier 2 water-wheel output did not use tier^8 and its five-emission cap");
        RandomSource denseRandom = RandomSource.create(93L);
        RandomSource denseExpectedRandom = RandomSource.create(93L);
        int denseFlowingWater = denseWheel.flowingWaterCount();
        long denseExpectedProgress = 0L;
        int denseExpectedEfficiency = 8_000;
        long denseExpectedEnergy = 0L;
        for (int tick = 0; tick < 4_000; tick++) {
            denseWheel.runGenerationTick(denseRandom);
            if (denseExpectedRandom.nextInt(40) < denseFlowingWater) {
                denseExpectedProgress += denseExpectedEfficiency;
                if (denseExpectedProgress >= WaterWheelBlockEntity.PROGRESS_LIMIT) {
                    denseExpectedProgress -= WaterWheelBlockEntity.PROGRESS_LIMIT;
                    if (denseExpectedRandom.nextInt(5) == 0) {
                        denseExpectedEfficiency--;
                    }
                    denseExpectedEnergy = Math.min(1_280L, denseExpectedEnergy + 256L);
                }
            }
            require(denseWheel.progress() == denseExpectedProgress
                            && denseWheel.efficiency() == denseExpectedEfficiency,
                    "Tier 2 progress or wear differed from the seeded original rolls at tick " + tick);
            require(tierThree.energyStorage().stored().internalValue() == denseExpectedEnergy,
                    "Tier 2 CE emission or cap differed at tick " + tick);
        }
        require(tierThree.energyStorage().stored().internalValue() == 1_280L,
                "The dense water wheel did not supply a Tier 3 machine up to its cap");
        require(tierFour.energyStorage().stored().internalValue() == 0L,
                "A Tier 4 machine accepted low-tier water-wheel CE");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void waterloggedSourcesCountAsWaterButNotFlowingPower(GameTestHelper helper) {
        WaterWheelBlockEntity wheel = placeWheel(helper, WaterWheelRegistration.CLAY);
        BlockState waterloggedFence = Blocks.OAK_FENCE.defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, true);
        for (BlockPos relative : BlockPos.betweenClosed(WHEEL_POS.offset(-1, -1, -1), WHEEL_POS.offset(1, 1, 1))) {
            if (!relative.equals(WHEEL_POS)) {
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(relative), waterloggedFence);
            }
        }
        BlockPos waterloggedPos = helper.absolutePos(WHEEL_POS.north());
        require(WaterDetection.hasWater(helper.getLevel(), waterloggedPos),
                "A waterlogged block was not recognized as containing water");
        require(WaterDetection.isWaterSource(helper.getLevel(), waterloggedPos),
                "A waterlogged source was not recognized as a source");
        require(!WaterDetection.isFlowingWater(helper.getLevel(), waterloggedPos),
                "A still waterlogged source was treated as flowing water");
        wheel.invalidateWaterCache();
        require(wheel.flowingWaterCount() == 0, "Waterlogged source blocks powered the water wheel");
        RandomSource sourceOnlyRandom = RandomSource.create(101L);
        for (int tick = 0; tick < 2_000; tick++) {
            wheel.runGenerationTick(sourceOnlyRandom);
        }
        require(wheel.progress() == 0L, "Source-only water advanced the wheel");

        BlockPos flowingPos = helper.absolutePos(WHEEL_POS.north());
        helper.getLevel().setBlockAndUpdate(flowingPos, Fluids.FLOWING_WATER.getFlowing(7, false).createLegacyBlock());
        wheel.invalidateWaterCache();
        require(WaterDetection.isFlowingWater(helper.getLevel(), flowingPos),
                "A non-source water FluidState was not recognized as flowing");
        require(wheel.flowingWaterCount() == 1, "The added flowing water did not enter the cached scan");
        RandomSource flowingRandom = RandomSource.create(102L);
        for (int tick = 0; tick < 2_000 && wheel.progress() == 0L; tick++) {
            wheel.runGenerationTick(flowingRandom);
        }
        require(wheel.progress() > 0L, "Flowing water did not drive the water wheel");
        helper.succeed();
    }

    private static WaterWheelBlockEntity placeWheel(
            GameTestHelper helper, WaterWheelRegistration.WaterWheelDefinition definition) {
        BlockPos absolutePos = helper.absolutePos(WHEEL_POS);
        helper.getLevel().setBlockAndUpdate(absolutePos, definition.block().get().defaultBlockState());
        require(helper.getLevel().getBlockEntity(absolutePos) instanceof WaterWheelBlockEntity,
                definition.id() + " did not create its block entity");
        return (WaterWheelBlockEntity)helper.getLevel().getBlockEntity(absolutePos);
    }

    private static ClayMachineBlockEntity placeMachine(GameTestHelper helper, String id, BlockPos relativePos) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get(id);
        require(definition != null, "Missing processing machine " + id);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().setBlockAndUpdate(absolutePos, definition.block().get().defaultBlockState());
        require(helper.getLevel().getBlockEntity(absolutePos) instanceof ClayMachineBlockEntity,
                id + " did not create its shared block entity");
        return (ClayMachineBlockEntity)helper.getLevel().getBlockEntity(absolutePos);
    }

    private static void placeFlowingWater(GameTestHelper helper, BlockPos... positions) {
        BlockState flowingWater = Fluids.FLOWING_WATER.getFlowing(7, false).createLegacyBlock();
        for (BlockPos pos : positions) {
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(pos), flowingWater);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private WaterWheelGameTests() {
    }
}
