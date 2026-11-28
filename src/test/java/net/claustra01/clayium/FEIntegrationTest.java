package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.DirectionalClayContainerBlock;
import net.claustra01.clayium.block.FEGeneratorBlock;
import net.claustra01.clayium.blockentity.ClayContainerIoIcons;
import net.claustra01.clayium.blockentity.FEGeneratorBlockEntity;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.FEGeneratorSpecs;
import net.claustra01.clayium.menu.ClayiumBaseMenu;
import net.claustra01.clayium.menu.FEGeneratorMenu;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FEIntegrationTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void all10ConvertersMatchOriginalTierAndSpecValues() {
        record Expected(String path, String configName, int tier, long ce, int feProd, int feOut, int cap) {}
        Expected[] expected = {
                new Expected("basic_rf_generator", "BasicRFGenerator", 4, 100L, 10, 10, 10_000),
                new Expected("advanced_rf_generator", "AdvancedRFGenerator", 5, 1_000L, 30, 30, 30_000),
                new Expected("precision_rf_generator", "PrecisionRFGenerator", 6, 10_000L, 90, 90, 90_000),
                new Expected("clay_steel_rf_generator", "ClaySteelRFGenerator", 7, 100_000L, 270, 270, 270_000),
                new Expected("clayium_rf_generator", "ClayiumRFGenerator", 8, 1_000_000L, 810, 810, 810_000),
                new Expected("ultimate_rf_generator", "UltimateRFGenerator", 9, 10_000_000L, 2_430, 2_430, 2_430_000),
                new Expected("antimatter_rf_generator", "AntimatterRFGenerator", 10, 100_000_000L, 7_290, 7_290, 7_290_000),
                new Expected("pure_antimatter_rf_generator", "PureAntimatterRFGenerator", 11, 1_000_000_000L, 21_870, 21_870, 21_870_000),
                new Expected("oec_rf_generator", "OECRFGenerator", 12, 10_000_000_000L, 65_610, 65_610, 65_610_000),
                new Expected("opa_rf_generator", "OPARFGenerator", 13, 100_000_000_000L, 196_830, 196_830, 196_830_000)
        };

        assertNotNull(ClayiumBlockEntities.FE_GENERATOR.get());
        assertNotNull(ClayiumMenus.FE_GENERATOR.get());

        for (Expected e : expected) {
            Block block = ClayiumBlocks.byPath(e.path()).get();
            FEGeneratorBlock feBlock = assertInstanceOf(FEGeneratorBlock.class, block);
            assertEquals(e.tier(), feBlock.getTier());
            assertEquals(e.configName(), feBlock.getConfigBlockName());
            assertEquals("fe_generator", ClayContainerBlock.resolveBlockEntityCategory(block));

            FEGeneratorSpecs.Spec spec = FEGeneratorSpecs.forBlock(block);
            assertNotNull(spec);
            assertEquals(e.tier(), spec.tier());
            assertEquals(e.ce(), spec.ceConsumptionPerTick());
            assertEquals(e.feProd(), spec.feProductionPerTick());
            assertEquals(e.feOut(), spec.feOutputPerTick());
            assertEquals(e.cap(), spec.feStorageSize());
            assertEquals(1.0, spec.overclockExponent());

            FEGeneratorBlockEntity be = assertInstanceOf(
                    FEGeneratorBlockEntity.class,
                    ClayiumBlockEntities.FE_GENERATOR.get().create(BlockPos.ZERO, feBlock.defaultBlockState())
            );
            assertEquals(e.tier(), be.getTier());
            assertEquals(e.ce(), be.ceConsumptionPerTick);
            assertEquals(e.feProd(), be.feProductionPerTick);
            assertEquals(e.feOut(), be.feOutputPerTick);
            assertEquals(e.cap(), be.maxFeStored);
        }
    }

    @Test
    void directionalCapabilityAndIoIconsFollowFrontAndBackRoutes() {
        Block block = ClayiumBlocks.byPath("basic_rf_generator").get();
        BlockState northState = block.defaultBlockState();
        FEGeneratorBlockEntity be = new FEGeneratorBlockEntity(BlockPos.ZERO, northState);

        assertEquals(0, be.getInsertRoute(RelativeSide.BACK));
        assertEquals(0, be.getExtractRoute(RelativeSide.FRONT));
        assertEquals("import_energy", ClayContainerIoIcons.getInsertIconName(be, RelativeSide.BACK, false));
        assertEquals("import_energy_p", ClayContainerIoIcons.getInsertIconName(be, RelativeSide.BACK, true));
        assertEquals("export_rf", ClayContainerIoIcons.getExtractIconName(be, RelativeSide.FRONT, false));
        assertEquals("export_rf_p", ClayContainerIoIcons.getExtractIconName(be, RelativeSide.FRONT, true));
        assertNull(ClayContainerIoIcons.getExtractIconName(be, RelativeSide.BACK, false));

        assertNull(be.getEnergyStorage(null));
        assertNotNull(be.getEnergyStorage(Direction.NORTH));
        for (Direction dir : Direction.values()) {
            if (dir != Direction.NORTH) {
                assertNull(be.getEnergyStorage(dir), "Unexpected energy cap on disabled side " + dir);
            }
        }

        IEnergyStorage frontCap = be.getEnergyStorage(Direction.NORTH);
        assertNotNull(frontCap);
        assertFalse(frontCap.canReceive());
        assertEquals(0, frontCap.receiveEnergy(1000, false));
        assertTrue(frontCap.canExtract());
        assertEquals(10_000, frontCap.getMaxEnergyStored());

        // Rotating facing to EAST moves FE output capability from NORTH to EAST
        FEGeneratorBlockEntity eastBe = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                northState.setValue(DirectionalClayContainerBlock.FACING, Direction.EAST)
        );
        assertNull(eastBe.getEnergyStorage(Direction.NORTH));
        assertNotNull(eastBe.getEnergyStorage(Direction.EAST));
    }

    @Test
    void conversionStopsWhenBufferFullAndSimulatesPurely() {
        FEGeneratorBlockEntity be = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("basic_rf_generator").get().defaultBlockState()
        );
        // Basic: 100 internal CE/t -> 10 FE/t, output cap 10 FE/t, max 10,000 FE
        // Energetic Clay = 100,000 internal units (1 CE) -> worth 1,000 ticks of 100 units
        ItemStack energeticClay = new ItemStack(ClayiumBlocks.byPath("energetic_clay").get(), 2);
        be.setStack(0, energeticClay);

        assertTrue(be.tickGeneration());
        assertEquals(10, be.feStored);
        assertEquals(1, be.getStack(0).getCount());
        assertEquals(100_000L - 100L, be.getClayEnergy());

        // Fill buffer up to capacity and verify CE is not wasted once full
        be.feStored = be.maxFeStored;
        long ceBeforeFullTick = be.getClayEnergy();
        assertFalse(be.tickGeneration());
        assertEquals(ceBeforeFullTick, be.getClayEnergy());
        assertEquals(be.maxFeStored, be.feStored);

        IEnergyStorage front = be.getEnergyStorage(Direction.NORTH);
        assertNotNull(front);
        // Simulation must not drain feStored
        assertEquals(10, front.extractEnergy(50, true));
        assertEquals(be.maxFeStored, be.feStored);
        assertEquals(0, front.extractEnergy(0, false));
        assertEquals(0, front.extractEnergy(-100, false));
        assertEquals(be.maxFeStored, be.feStored);

        // Actual extraction obeys output cap (10 FE/t)
        assertEquals(10, front.extractEnergy(50, false));
        assertEquals(be.maxFeStored - 10, be.feStored);

        // Receiver returning invalid (> available or negative) values cannot duplicate or corrupt energy
        IEnergyStorage overReportingReceiver = new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return maxReceive + 500;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                return 100_000;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
        int beforePush = be.feStored;
        int pushed = be.pushEnergyToReceiver(Direction.NORTH, overReportingReceiver);
        assertEquals(10, pushed);
        assertEquals(beforePush - 10, be.feStored);

        IEnergyStorage negativeReportingReceiver = new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return -50;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                return 100_000;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
        int beforeNegPush = be.feStored;
        assertEquals(0, be.pushEnergyToReceiver(Direction.NORTH, negativeReportingReceiver));
        assertEquals(beforeNegPush, be.feStored);
    }

    @Test
    void redstoneSignalPausesEmissionWithoutStoppingInternalBuffering() {
        FEGeneratorBlockEntity be = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("advanced_rf_generator").get().defaultBlockState()
        );
        be.getClayEnergyStorage().setStored(10_000L);
        be.setPowered(true);

        IEnergyStorage front = be.getEnergyStorage(Direction.NORTH);
        assertNotNull(front);
        assertFalse(front.canExtract());
        assertTrue(be.tickGeneration());
        assertEquals(30, be.feStored);
        assertEquals(0, front.extractEnergy(30, false));
        assertEquals(30, be.feStored);

        EnergyStorage neighborBuffer = new EnergyStorage(1_000);
        assertEquals(0, be.pushEnergyToReceiver(Direction.NORTH, neighborBuffer));
        assertEquals(0, neighborBuffer.getEnergyStored());

        be.setPowered(false);
        assertTrue(front.canExtract());
        assertEquals(30, be.pushEnergyToReceiver(Direction.NORTH, neighborBuffer));
        assertEquals(30, neighborBuffer.getEnergyStored());
        assertEquals(0, be.feStored);
    }

    @Test
    void overclockExponentAndExtremeValuesClampWithoutOverflow() {
        FEGeneratorBlockEntity be = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("clay_steel_rf_generator").get().defaultBlockState()
        );
        // Custom spec with overclockExponent != 1.0
        FEGeneratorSpecs.Spec custom = new FEGeneratorSpecs.Spec(
                "CustomRFGenerator",
                "clay_steel_rf_generator",
                7,
                100_000L,
                200,
                150,
                500_000,
                2.0
        );
        be.applySpec(custom);
        be.recalculateRates(3.0, ClayEnergyStorage.MAX_ENERGY_SLOT_LIMIT);
        assertEquals(ClayEnergyStorage.MAX_ENERGY_SLOT_LIMIT, be.getClayEnergyStorage().getEnergySlotLimit());
        assertEquals(900_000L, be.ceConsumptionPerTick);
        assertEquals(1_800, be.feProductionPerTick);
        assertEquals(1_350, be.feOutputPerTick);

        // Extreme overclock clamps safely to Integer.MAX_VALUE / Long.MAX_VALUE rather than overflowing negative
        be.recalculateRates(1.0e12, 4);
        assertEquals(Long.MAX_VALUE, be.ceConsumptionPerTick);
        assertEquals(Integer.MAX_VALUE, be.feProductionPerTick);
        assertEquals(Integer.MAX_VALUE, be.feOutputPerTick);

        // Addition overflow guard when feStored is already near Integer.MAX_VALUE
        be.maxFeStored = Integer.MAX_VALUE;
        be.feStored = Integer.MAX_VALUE - 5;
        be.ceConsumptionPerTick = 10L;
        be.getClayEnergyStorage().setStored(100L);
        assertTrue(be.tickGeneration());
        assertEquals(Integer.MAX_VALUE, be.feStored);
    }

    @Test
    void nbtRoundTripAndMenuDataEncodingPreserveAllMetrics() {
        FEGeneratorBlockEntity be = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("opa_rf_generator").get().defaultBlockState()
        );
        be.getClayEnergyStorage().setStored(987_654_321_012L);
        be.feStored = 123_456_789;
        be.setPowered(true);
        be.recalculateRates(4.5, 8);

        CompoundTag saved = be.saveWithoutMetadata(registries);
        assertEquals(123_456_789, saved.getInt("FEStored"));
        assertEquals(123_456_789, saved.getInt("RFStored"));

        FEGeneratorBlockEntity loaded = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("opa_rf_generator").get().defaultBlockState()
        );
        loaded.loadWithComponents(saved, registries);
        assertEquals(123_456_789, loaded.feStored);
        assertTrue(loaded.isPowered());
        assertEquals(987_654_321_012L, loaded.getClayEnergy());

        // Legacy RFStored fallback when FEStored is absent
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putString("BlockName", "OPARFGenerator");
        legacyTag.putBoolean("Powered", false);
        legacyTag.putInt("RFStored", 77_777);
        FEGeneratorBlockEntity legacyLoaded = new FEGeneratorBlockEntity(
                BlockPos.ZERO,
                ClayiumBlocks.byPath("opa_rf_generator").get().defaultBlockState()
        );
        legacyLoaded.loadWithComponents(legacyTag, registries);
        assertEquals(77_777, legacyLoaded.feStored);
        assertFalse(legacyLoaded.isPowered());

        // 24-short Menu synchronization round-trip
        int[] encoded = FEGeneratorMenu.encodeBlockEntityData(be);
        assertEquals(FEGeneratorMenu.DATA_SLOT_COUNT, encoded.length);
        assertEquals(be.getClayEnergy(), ClayiumBaseMenu.readLongFrom4Shorts(encoded[0], encoded[1], encoded[2], encoded[3]));
        assertEquals(be.feStored, FEGeneratorMenu.readIntFrom2Shorts(encoded[4], encoded[5]));
        assertEquals(be.maxFeStored, FEGeneratorMenu.readIntFrom2Shorts(encoded[6], encoded[7]));
        assertEquals(be.ceConsumptionPerTick, ClayiumBaseMenu.readLongFrom4Shorts(encoded[8], encoded[9], encoded[10], encoded[11]));
        assertEquals(be.feProductionPerTick, FEGeneratorMenu.readIntFrom2Shorts(encoded[12], encoded[13]));
        assertEquals(be.feOutputPerTick, FEGeneratorMenu.readIntFrom2Shorts(encoded[14], encoded[15]));
        assertEquals(
                be.overclockTotalFactor,
                Double.longBitsToDouble(ClayiumBaseMenu.readLongFrom4Shorts(encoded[16], encoded[17], encoded[18], encoded[19]))
        );
        assertEquals(
                be.overclockExponent,
                Double.longBitsToDouble(ClayiumBaseMenu.readLongFrom4Shorts(encoded[20], encoded[21], encoded[22], encoded[23]))
        );
    }
}
