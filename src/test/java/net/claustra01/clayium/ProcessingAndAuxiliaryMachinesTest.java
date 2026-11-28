package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.blockentity.AssemblerBlockEntity;
import net.claustra01.clayium.blockentity.AutoClayCondenserBlockEntity;
import net.claustra01.clayium.blockentity.AutoCrafterBlockEntity;
import net.claustra01.clayium.blockentity.AutoTraderBlockEntity;
import net.claustra01.clayium.blockentity.BlastFurnaceBlockEntity;
import net.claustra01.clayium.blockentity.CACollectorBlockEntity;
import net.claustra01.clayium.blockentity.CACondenserBlockEntity;
import net.claustra01.clayium.blockentity.CAInjectorBlockEntity;
import net.claustra01.clayium.blockentity.CAReactorBlockEntity;
import net.claustra01.clayium.blockentity.CentrifugeBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalMetalSeparatorBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalReactorBlockEntity;
import net.claustra01.clayium.blockentity.ClayChunkLoaderBlockEntity;
import net.claustra01.clayium.blockentity.ClayCraftingTableBlockEntity;
import net.claustra01.clayium.blockentity.ClayFabricatorBlockEntity;
import net.claustra01.clayium.blockentity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.blockentity.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.blockentity.CreativeEnergySourceBlockEntity;
import net.claustra01.clayium.blockentity.IPANCoreAccess;
import net.claustra01.clayium.blockentity.LaserInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.PANDuplicatorBlockEntity;
import net.claustra01.clayium.blockentity.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.blockentity.ReactorBlockEntity;
import net.claustra01.clayium.blockentity.SaltExtractorBlockEntity;
import net.claustra01.clayium.blockentity.SimpleClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.SolarClayFabricatorBlockEntity;
import net.claustra01.clayium.blockentity.WaterWheelBlockEntity;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe.WeightedOutput;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.util.CompressedClayHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProcessingAndAuxiliaryMachinesTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void allMachineBlockEntityTypesAreNonOverlappingAndInstantiateExactClasses() {
        List<BlockEntityType<?>> allTypes = List.of(
                ClayiumBlockEntities.BUFFER.get(),
                ClayiumBlockEntities.MULTITRACK_BUFFER.get(),
                ClayiumBlockEntities.FLUID_TRANSLATOR.get(),
                ClayiumBlockEntities.STORAGE_CONTAINER.get(),
                ClayiumBlockEntities.VACUUM_CONTAINER.get(),
                ClayiumBlockEntities.METAL_CHEST.get(),
                ClayiumBlockEntities.DISTRIBUTOR.get(),
                ClayiumBlockEntities.CLAY_INTERFACE.get(),
                ClayiumBlockEntities.REDSTONE_INTERFACE.get(),
                ClayiumBlockEntities.LASER_INTERFACE.get(),
                ClayiumBlockEntities.SIMPLE_MACHINE.get(),
                ClayiumBlockEntities.ASSEMBLER.get(),
                ClayiumBlockEntities.CENTRIFUGE.get(),
                ClayiumBlockEntities.CHEMICAL_REACTOR.get(),
                ClayiumBlockEntities.CHEMICAL_METAL_SEPARATOR.get(),
                ClayiumBlockEntities.CA_INJECTOR.get(),
                ClayiumBlockEntities.CA_CONDENSER.get(),
                ClayiumBlockEntities.CA_COLLECTOR.get(),
                ClayiumBlockEntities.BLAST_FURNACE.get(),
                ClayiumBlockEntities.REACTOR.get(),
                ClayiumBlockEntities.CA_REACTOR.get(),
                ClayiumBlockEntities.AUTO_CLAY_CONDENSER.get(),
                ClayiumBlockEntities.SOLAR_CLAY_FABRICATOR.get(),
                ClayiumBlockEntities.CLAY_FABRICATOR.get(),
                ClayiumBlockEntities.COBBLESTONE_GENERATOR.get(),
                ClayiumBlockEntities.SALT_EXTRACTOR.get(),
                ClayiumBlockEntities.WATER_WHEEL.get(),
                ClayiumBlockEntities.CREATIVE_CE_SOURCE.get(),
                ClayiumBlockEntities.AUTO_CRAFTER.get(),
                ClayiumBlockEntities.AUTO_TRADER.get(),
                ClayiumBlockEntities.PAN_DUPLICATOR.get(),
                ClayiumBlockEntities.CLAY_WORK_TABLE.get(),
                ClayiumBlockEntities.CLAY_CRAFTING_TABLE.get(),
                ClayiumBlockEntities.QUARTZ_CRUCIBLE.get(),
                ClayiumBlockEntities.CLAY_CHUNK_LOADER.get()
        );

        Map<Block, BlockEntityType<?>> ownerMap = new HashMap<>();
        for (BlockEntityType<?> type : allTypes) {
            assertNotNull(type);
        }
        for (var holder : ClayiumBlocks.all().values()) {
            Block b = holder.get();
            BlockEntityType<?> matched = null;
            for (BlockEntityType<?> type : allTypes) {
                if (type.isValid(b.defaultBlockState())) {
                    assertNull(matched, "Block " + holder.getId() + " matched multiple BlockEntityTypes");
                    matched = type;
                    ownerMap.put(b, type);
                }
            }
            if (b instanceof EntityBlock eb && ownerMap.containsKey(b)) {
                BlockEntity be = eb.newBlockEntity(BlockPos.ZERO, b.defaultBlockState());
                assertNotNull(be, "BlockEntity must not be null for " + holder.getId());
                assertEquals(ownerMap.get(b), be.getType(), "Type mismatch for " + holder.getId());
            }
        }

        // Spot check exact BlockEntity subclasses & critical properties
        assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("clay_bending_machine"));
        assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("precision_smelter"));
        assertInstanceOf(AssemblerBlockEntity.class, createBe("simple_assembler"));
        assertInstanceOf(AssemblerBlockEntity.class, createBe("precision_alloy_smelter"));
        assertInstanceOf(CentrifugeBlockEntity.class, createBe("precision_centrifuge"));
        assertInstanceOf(ChemicalReactorBlockEntity.class, createBe("basic_chemical_reactor"));
        assertInstanceOf(ChemicalReactorBlockEntity.class, createBe("advanced_fluid_transfer_machine"));
        assertInstanceOf(ChemicalMetalSeparatorBlockEntity.class, createBe("chemical_metal_separator"));
        assertInstanceOf(CAInjectorBlockEntity.class, createBe("pure_antimatter_ca_injector"));
        assertInstanceOf(CACondenserBlockEntity.class, createBe("antimatter_ca_condenser"));
        assertInstanceOf(CACollectorBlockEntity.class, createBe("ca_collector"));
        assertInstanceOf(BlastFurnaceBlockEntity.class, createBe("clay_blast_furnace"));
        assertInstanceOf(ReactorBlockEntity.class, createBe("clay_reactor"));
        assertInstanceOf(CAReactorBlockEntity.class, createBe("opa_ca_reactor_core"));
        assertInstanceOf(LaserInterfaceBlockEntity.class, createBe("clay_steel_clay_laser_interface"));
        assertInstanceOf(AutoClayCondenserBlockEntity.class, createBe("auto_clay_condenser"));
        assertInstanceOf(SolarClayFabricatorBlockEntity.class, createBe("lithium_solar_clay_fabricator"));
        assertInstanceOf(ClayFabricatorBlockEntity.class, createBe("clay_fabricator_mk2"));
        assertInstanceOf(CobblestoneGeneratorBlockEntity.class, createBe("basic_cobblestone_generator"));
        assertInstanceOf(SaltExtractorBlockEntity.class, createBe("precision_salt_extractor"));
        assertInstanceOf(WaterWheelBlockEntity.class, createBe("dense_clay_water_wheel"));
        assertInstanceOf(CreativeEnergySourceBlockEntity.class, createBe("creative_ce_source"));
        assertInstanceOf(AutoCrafterBlockEntity.class, createBe("ultimate_auto_crafter"));
        assertInstanceOf(AutoTraderBlockEntity.class, createBe("auto_trader"));
        assertInstanceOf(PANDuplicatorBlockEntity.class, createBe("opa_pan_duplicator"));
        assertInstanceOf(ClayWorkTableBlockEntity.class, createBe("clay_work_table"));
        assertInstanceOf(ClayCraftingTableBlockEntity.class, createBe("clay_crafting_table"));
        assertInstanceOf(QuartzCrucibleBlockEntity.class, createBe("quartz_crucible"));
        assertInstanceOf(ClayChunkLoaderBlockEntity.class, createBe("clay_chunk_loader"));
    }

    @Test
    void simpleMachinesApplyTierTablesManualButtonAndRecipeExecution() {
        SimpleClayMachineBlockEntity bendingT1 = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("clay_bending_machine"));
        assertEquals("bending_machine", bendingT1.recipeCategoryId);
        assertEquals(1, bendingT1.getTier());
        assertFalse(bendingT1.acceptEnergyClay());
        assertFalse(bendingT1.autoInsert);
        assertFalse(bendingT1.autoExtract);

        ClayMachineRecipe lowRecipe = new ClayMachineRecipe(
                "bending_machine",
                List.of(new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1)),
                List.of(new ItemStack(Items.BRICK, 1)),
                1,
                10L,
                20L,
                0
        );
        ClayMachineRecipe highRecipe = new ClayMachineRecipe(
                "bending_machine",
                List.of(new CountedIngredient(Ingredient.of(Items.IRON_INGOT), 1)),
                List.of(new ItemStack(Items.IRON_NUGGET, 9)),
                5,
                10L,
                20L,
                0
        );
        bendingT1.setOverrideRecipesForTest(List.of(lowRecipe, highRecipe));

        // High-tier recipe rejected on T1 machine
        bendingT1.setStack(0, new ItemStack(Items.IRON_INGOT, 1));
        assertFalse(bendingT1.canProceedCraft());

        // Low-tier recipe fits, and manual button grants +5 internal CE
        bendingT1.setStack(0, new ItemStack(Items.CLAY_BALL, 2));
        assertTrue(bendingT1.canProceedCraft());
        assertEquals(1, bendingT1.canPushButton());
        for (int i = 0; i < 40; i++) {
            bendingT1.pushButton(); // 40 * 5 = 200 internal CE (20 ticks * 10 CE/t)
        }
        assertEquals(200L, bendingT1.getClayEnergyStorage().getStored());

        for (int tick = 0; tick < 20; tick++) {
            bendingT1.proceedCraft();
        }
        assertEquals(1, bendingT1.getStack(1).getCount());
        assertEquals(Items.BRICK, bendingT1.getStack(1).getItem());
        assertEquals(1, bendingT1.getStack(0).getCount());
        assertTrue(bendingT1.getStack(2).isEmpty());
        assertEquals(0L, bendingT1.getClayEnergyStorage().getStored());

        // Check UtilTier multipliers on Condenser / Grinder / Smelter
        SimpleClayMachineBlockEntity condenserT5 = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("advanced_condenser"));
        assertEquals("condenser", condenserT5.recipeCategoryId);
        assertEquals(0.25F, condenserT5.initCraftTime, 1e-6F);
        assertEquals(5.0F, condenserT5.initConsumingEnergy, 1e-6F);

        SimpleClayMachineBlockEntity grinderT6 = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("precision_grinder"));
        assertEquals("grinder", grinderT6.recipeCategoryId);
        assertEquals(0.0625F, grinderT6.initCraftTime, 1e-6F);
        assertEquals(25.0F, grinderT6.initConsumingEnergy, 1e-6F);

        SimpleClayMachineBlockEntity smelterT9 = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("ultimate_smelter"));
        assertEquals("smelter", smelterT9.recipeCategoryId);
        assertEquals(0.0025F, smelterT9.initCraftTime, 1e-6F);
        assertEquals(560000.0F, smelterT9.initConsumingEnergy, 1e-1F);
    }

    @Test
    void assemblerSupportsPermutedAndSingleInputMatchingEvenWhenBothSlotsOccupied() {
        AssemblerBlockEntity asm = assertInstanceOf(AssemblerBlockEntity.class, createBe("precision_assembler"));
        assertEquals("assembler", asm.recipeCategoryId);
        assertTrue(asm.acceptEnergyClay());

        ClayMachineRecipe twoInput = new ClayMachineRecipe(
                "assembler",
                List.of(
                        new CountedIngredient(Ingredient.of(Items.IRON_INGOT), 2),
                        new CountedIngredient(Ingredient.of(Items.REDSTONE), 1)
                ),
                List.of(new ItemStack(Items.COMPASS, 1)),
                3,
                20L,
                5L,
                0
        );
        ClayMachineRecipe oneInput = new ClayMachineRecipe(
                "assembler",
                List.of(new CountedIngredient(Ingredient.of(Items.GOLD_INGOT), 1)),
                List.of(new ItemStack(Items.GOLD_NUGGET, 9)),
                3,
                10L,
                2L,
                0
        );
        asm.setOverrideRecipesForTest(List.of(twoInput, oneInput));
        asm.getClayEnergyStorage().setStored(10_000L);

        // Swapped slots [1, 0]
        asm.setStack(0, new ItemStack(Items.REDSTONE, 1));
        asm.setStack(1, new ItemStack(Items.IRON_INGOT, 2));
        assertTrue(asm.canProceedCraft());
        for (int i = 0; i < 5; i++) {
            asm.proceedCraft();
        }
        assertEquals(Items.COMPASS, asm.getStack(2).getItem());
        assertTrue(asm.getStack(0).isEmpty());
        assertTrue(asm.getStack(1).isEmpty());

        // Single-input recipe matches slot 1 even when slot 0 holds an unrelated item!
        asm.setStack(2, ItemStack.EMPTY);
        asm.setStack(0, new ItemStack(Items.CLAY_BALL, 1));
        asm.setStack(1, new ItemStack(Items.GOLD_INGOT, 1));
        assertTrue(asm.canProceedCraft());
        for (int i = 0; i < 2; i++) {
            asm.proceedCraft();
        }
        assertEquals(Items.GOLD_NUGGET, asm.getStack(2).getItem());
        assertEquals(9, asm.getStack(2).getCount());
        assertEquals(1, asm.getStack(0).getCount());
        assertTrue(asm.getStack(1).isEmpty());
    }

    @Test
    void centrifugeChemicalReactorSeparatorAndCaMachinesFollowExactRules() {
        CentrifugeBlockEntity cent3 = assertInstanceOf(CentrifugeBlockEntity.class, createBe("simple_centrifuge"));
        CentrifugeBlockEntity cent6 = assertInstanceOf(CentrifugeBlockEntity.class, createBe("precision_centrifuge"));
        assertEquals(1, cent3.resultSlotNum);
        assertEquals(4, cent6.resultSlotNum);
        assertEquals(0.0625F, cent6.initCraftTime, 1e-6F);
        assertEquals(25.0F, cent6.initConsumingEnergy, 1e-6F);

        ClayMachineRecipe multiOut = new ClayMachineRecipe(
                "centrifuge",
                List.of(new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1)),
                List.of(
                        new ItemStack(Items.IRON_INGOT, 1),
                        new ItemStack(Items.GOLD_INGOT, 1),
                        new ItemStack(Items.DIAMOND, 1),
                        new ItemStack(Items.EMERALD, 1)
                ),
                3,
                10L,
                10L,
                0
        );
        cent3.setOverrideRecipesForTest(List.of(multiOut));
        cent3.getClayEnergyStorage().setStored(10_000L);
        cent3.setStack(0, new ItemStack(Items.CLAY_BALL, 1));
        for (int i = 0; i < 10; i++) {
            cent3.proceedCraft();
        }
        assertEquals(Items.IRON_INGOT, cent3.getStack(1).getItem());
        assertTrue(cent3.getStack(2).isEmpty()); // T3 only fills 1 result slot

        ChemicalReactorBlockEntity fluidTransfer = assertInstanceOf(
                ChemicalReactorBlockEntity.class, createBe("advanced_fluid_transfer_machine"));
        assertEquals("fluid_transfer_machine", fluidTransfer.recipeCategoryId);

        ChemicalMetalSeparatorBlockEntity sep = assertInstanceOf(
                ChemicalMetalSeparatorBlockEntity.class, createBe("chemical_metal_separator"));
        ChemicalMetalSeparatorRecipe sepRecipe = new ChemicalMetalSeparatorRecipe(
                new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1),
                6,
                100L,
                2L,
                List.of(new WeightedOutput(new ItemStack(Items.IRON_NUGGET, 1), 3),
                        new WeightedOutput(new ItemStack(Items.GOLD_NUGGET, 1), 1))
        );
        sep.setOverrideRecipesForTest(List.of(sepRecipe));
        sep.getClayEnergyStorage().setStored(10_000L);
        sep.setStack(0, new ItemStack(Items.CLAY_BALL, 1));
        assertTrue(sep.canProceedCraft());
        sep.proceedCraft();
        sep.proceedCraft();
        assertFalse(sep.getStack(1).isEmpty());

        // When output slots 1..16 are full with other items, canProceedCraft must be false
        for (int s = 1; s <= 16; s++) {
            sep.setStack(s, new ItemStack(Items.COBBLESTONE, 64));
        }
        sep.setStack(0, new ItemStack(Items.CLAY_BALL, 1));
        assertFalse(sep.canProceedCraft());

        CAInjectorBlockEntity inj9 = assertInstanceOf(CAInjectorBlockEntity.class, createBe("ultimate_ca_injector"));
        CAInjectorBlockEntity inj11 = assertInstanceOf(CAInjectorBlockEntity.class, createBe("pure_antimatter_ca_injector"));
        assertEquals(0.2, inj9.caFactorExponent, 1e-6);
        assertEquals(3.0, inj11.caFactorExponent, 1e-6);

        CACondenserBlockEntity caCond11 = assertInstanceOf(CACondenserBlockEntity.class, createBe("pure_antimatter_ca_condenser"));
        assertEquals(0.01F, caCond11.initCraftTime, 1e-6F);
        assertEquals(100.0F, caCond11.initConsumingEnergy, 1e-6F);

        CACollectorBlockEntity collector = assertInstanceOf(CACollectorBlockEntity.class, createBe("ca_collector"));
        assertEquals(9, collector.getInventorySize());
        assertFalse(collector.acceptEnergyClay());
    }

    @Test
    void multiblocksAndCaReactorFormulasMatchOriginal() {
        BlastFurnaceBlockEntity bf = assertInstanceOf(BlastFurnaceBlockEntity.class, createBe("clay_blast_furnace"));
        assertEquals("blast_furnace", bf.recipeCategoryId);
        assertEquals(1, bf.autoInsertInterval);
        assertEquals(1, bf.autoExtractInterval);
        assertTrue(bf.acceptEnergyClay());

        ReactorBlockEntity reactor = assertInstanceOf(ReactorBlockEntity.class, createBe("clay_reactor"));
        assertEquals("reactor", reactor.recipeCategoryId);
        ClayLaser laser = new ClayLaser(0, 2, 1, 0);
        assertTrue(laser.getEnergy() > 0.0);
        assertTrue(reactor.irradiateClayLaser(laser, Direction.UP));

        CAReactorBlockEntity ca10 = assertInstanceOf(CAReactorBlockEntity.class, createBe("antimatter_ca_reactor_core"));
        CAReactorBlockEntity ca11 = assertInstanceOf(CAReactorBlockEntity.class, createBe("pure_antimatter_ca_reactor_core"));
        CAReactorBlockEntity ca13 = assertInstanceOf(CAReactorBlockEntity.class, createBe("opa_ca_reactor_core"));
        assertEquals(1, ca10.reactorHullMaxRank);
        assertEquals(5, ca11.reactorHullMaxRank);
        assertEquals(9, ca13.reactorHullMaxRank);
        assertFalse(ca10.canOverclock());
        assertTrue(ca13.canOverclock());

        ca13.reactorHullNum = 60;
        ca13.reactorRank = 8.4;
        assertEquals(ClayiumItems.byPath("octuple_pure_antimatter").get(), ca13.getResultPureAntimatter().getItem());
        assertEquals(Math.pow(9.0, 8), ca13.getCraftTimePureAntimatterMultiplier(), 1e-6);
    }

    @Test
    void auxiliaryMachinesAndCraftingTablesMatchOriginalFormulas() {
        // Auto Clay Condenser: 9 compressed clay -> 1 next tier + MK2 tier check
        AutoClayCondenserBlockEntity acc1 = assertInstanceOf(AutoClayCondenserBlockEntity.class, createBe("auto_clay_condenser"));
        assertFalse(acc1.acceptEnergyClay());
        acc1.setStack(0, CompressedClayHelper.getCompressedClay(0, 9));
        assertTrue(acc1.canProceedCraft());
        acc1.proceedCraft();
        assertEquals(1, acc1.getQuantityOfClay()[1]);
        assertEquals(0, acc1.getQuantityOfClay()[0]);

        // Solar & Clay Fabricators formulas
        SolarClayFabricatorBlockEntity solar5 = assertInstanceOf(SolarClayFabricatorBlockEntity.class, createBe("solar_clay_fabricator_mk1"));
        SolarClayFabricatorBlockEntity solar7 = assertInstanceOf(SolarClayFabricatorBlockEntity.class, createBe("lithium_solar_clay_fabricator"));
        assertEquals(4, solar5.acceptableTier);
        assertEquals(9, solar7.acceptableTier);
        assertEquals(2.0F, solar7.baseCraftTime, 1e-6F);
        assertTrue(solar5.isItemValidForSlot(0, new ItemStack(Items.SAND)));

        ClayFabricatorBlockEntity fab8 = assertInstanceOf(ClayFabricatorBlockEntity.class, createBe("clay_fabricator_mk1"));
        ClayFabricatorBlockEntity fab13 = assertInstanceOf(ClayFabricatorBlockEntity.class, createBe("clay_fabricator_mk3"));
        assertEquals(11, fab8.acceptableTier);
        assertEquals(0.85F, fab8.exponentOfNumber, 1e-6F);
        assertEquals(13, fab13.acceptableTier);
        assertEquals(0.06F, fab13.exponentOfNumber, 1e-6F);
        assertFalse(fab8.isItemValidForSlot(0, new ItemStack(Items.SAND)));

        // Duplication preserves source stack in slot 0 and outputs copy in slot 1
        fab8.setStack(0, CompressedClayHelper.getCompressedClay(5, 4));
        assertTrue(fab8.canProceedCraft());
        fab8.machineCraftTime = 100000L;
        fab8.proceedCraft();
        assertEquals(4, fab8.getStack(0).getCount());
        assertEquals(4, fab8.getStack(1).getCount());

        // Cobblestone Generator & Salt Extractor
        CobblestoneGeneratorBlockEntity cobble7 = assertInstanceOf(CobblestoneGeneratorBlockEntity.class, createBe("clay_steel_cobblestone_generator"));
        assertEquals(8000, cobble7.progressEfficiency);
        assertFalse(cobble7.isItemValidForSlot(0, new ItemStack(Items.CLAY_BALL)));

        SaltExtractorBlockEntity salt5 = assertInstanceOf(SaltExtractorBlockEntity.class, createBe("advanced_salt_extractor"));
        assertEquals(200, salt5.progressEfficiency);
        assertEquals(7, salt5.getInventorySize()); // 2x3 buffer + 1 CE slot
        assertTrue(salt5.acceptEnergyClay());

        // Water Wheel
        WaterWheelBlockEntity wheel1 = assertInstanceOf(WaterWheelBlockEntity.class, createBe("clay_water_wheel"));
        WaterWheelBlockEntity wheel2 = assertInstanceOf(WaterWheelBlockEntity.class, createBe("dense_clay_water_wheel"));
        assertEquals(1000, wheel1.progressEfficiency);
        assertEquals(8000, wheel2.progressEfficiency);

        // Creative CE Source
        CreativeEnergySourceBlockEntity creative = assertInstanceOf(CreativeEnergySourceBlockEntity.class, createBe("creative_ce_source"));
        assertEquals(64, creative.getStack(0).getCount());
        assertEquals(ClayiumItems.byPath("octuple_compressed_energetic_clay").get(), creative.getStack(0).getItem());
        assertEquals(0, creative.getSlotsToDrop().length);

        // Auto Crafter tiers
        AutoCrafterBlockEntity ac5 = assertInstanceOf(AutoCrafterBlockEntity.class, createBe("advanced_auto_crafter"));
        AutoCrafterBlockEntity ac9 = assertInstanceOf(AutoCrafterBlockEntity.class, createBe("ultimate_auto_crafter"));
        assertFalse(ac5.acceptEnergyClay());
        assertTrue(ac9.acceptEnergyClay());
        assertEquals(256, ac9.numAutomation);
        assertEquals(64.0F, ac9.multConsumingEnergy, 1e-6F);

        // PAN Duplicator
        PANDuplicatorBlockEntity dup6 = assertInstanceOf(PANDuplicatorBlockEntity.class, createBe("precision_pan_duplicator"));
        assertEquals(10.0F, dup6.initConsumingEnergy, 1e-6F);
        IPANCoreAccess mockCore = () -> Set.of(new IPANCoreAccess.PANConversionEntry(new ItemStack(Items.DIAMOND), 2_000_000.0));
        dup6.setPANCore(mockCore, 20);
        dup6.getClayEnergyStorage().setStored(10_000_000L);
        dup6.setStack(0, new ItemStack(ClayiumItems.byPath("antimatter").get(), 1));
        dup6.setStack(1, new ItemStack(Items.DIAMOND, 1));
        assertTrue(dup6.canProceedCraft());
        for (int i = 0; i < 3; i++) {
            dup6.proceedCraft();
        }
        assertTrue(dup6.getStack(0).isEmpty()); // Antimatter consumed
        assertEquals(1, dup6.getStack(1).getCount()); // Target item preserved
        assertEquals(1, dup6.getStack(2).getCount()); // Duplicate produced
        assertEquals(Items.DIAMOND, dup6.getStack(2).getItem());

        // Clay Work Table buttons & tool wear
        ClayWorkTableBlockEntity wt = assertInstanceOf(ClayWorkTableBlockEntity.class, createBe("clay_work_table"));
        ClayWorkTableRecipe wtRecipe = new ClayWorkTableRecipe(
                new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1),
                3,
                new ItemStack(ClayiumItems.byPath("clay_plate").get(), 1),
                new ItemStack(Items.CLAY_BALL, 1),
                2
        );
        wt.setOverrideRecipesForTest(List.of(wtRecipe));
        wt.setStack(0, new ItemStack(Items.CLAY_BALL, 1));
        assertEquals(0, wt.canPushButton(3)); // Missing rolling pin
        wt.setStack(1, new ItemStack(ClayiumItems.byPath("clay_rolling_pin").get()));
        assertEquals(1, wt.canPushButton(3));
        wt.pushButton(3);
        assertEquals(1, wt.cookTime);
        assertEquals(1, wt.getStack(1).getDamageValue());
        wt.pushButton(3);
        assertEquals(0, wt.cookTime);
        assertEquals(ClayiumItems.byPath("clay_plate").get(), wt.getStack(2).getItem());
        assertEquals(Items.CLAY_BALL, wt.getStack(3).getItem());
        assertEquals(2, wt.getStack(1).getDamageValue());

        // Quartz Crucible
        QuartzCrucibleBlockEntity qc = assertInstanceOf(QuartzCrucibleBlockEntity.class, createBe("quartz_crucible"));
        for (int i = 0; i < 9; i++) {
            assertTrue(qc.putIngot());
        }
        assertFalse(qc.putIngot());
        assertEquals(9, qc.ingotQuantity);

        // NBT round-trip on processing machine
        SimpleClayMachineBlockEntity sm = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("basic_smelter"));
        sm.recipeCategoryId = "smelter";
        sm.machineCraftTime = 42L;
        sm.machineTimeToCraft = 100L;
        sm.machineConsumingEnergy = 500L;
        sm.getClayEnergyStorage().setStored(123456L);
        CompoundTag saved = sm.saveWithoutMetadata(registries);
        SimpleClayMachineBlockEntity reloaded = assertInstanceOf(SimpleClayMachineBlockEntity.class, createBe("basic_smelter"));
        reloaded.loadWithComponents(saved, registries);
        assertEquals(42L, reloaded.machineCraftTime);
        assertEquals(100L, reloaded.machineTimeToCraft);
        assertEquals(500L, reloaded.machineConsumingEnergy);
        assertEquals(123456L, reloaded.getClayEnergyStorage().getStored());
    }

    private static BlockEntity createBe(String path) {
        Block block = ClayiumBlocks.byPath(path).get();
        return ((EntityBlock) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
    }
}
