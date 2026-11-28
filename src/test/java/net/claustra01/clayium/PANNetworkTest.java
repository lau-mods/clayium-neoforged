package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.PANAdapterBlock;
import net.claustra01.clayium.block.PANCoreBlock;
import net.claustra01.clayium.block.PANDuplicatorBlock;
import net.claustra01.clayium.block.PanCableBlock;
import net.claustra01.clayium.blockentity.PANAdapterBlockEntity;
import net.claustra01.clayium.blockentity.PANCoreBlockEntity;
import net.claustra01.clayium.blockentity.PANCoreBlockEntity.PANItemCost;
import net.claustra01.clayium.blockentity.PANDuplicatorBlockEntity;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.network.PANCoreListPayload;
import net.claustra01.clayium.pan.IPANComponent;
import net.claustra01.clayium.pan.IPANConductor;
import net.claustra01.clayium.pan.PANConversion;
import net.claustra01.clayium.pan.PANCraftingTableFactory;
import net.claustra01.clayium.pan.PANFurnaceFactory;
import net.claustra01.clayium.pan.UtilPAN;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe.WeightedOutput;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PANNetworkTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void panCableShapeAndConductorConnectivityMatchOriginal() {
        Block cableBlock = ClayiumBlocks.byPath("pan_cable").get();
        PanCableBlock cable = assertInstanceOf(PanCableBlock.class, cableBlock);
        assertInstanceOf(IPANConductor.class, cable);
        assertEquals(11, cable.getTier());

        VoxelShape coreShape = cable.defaultBlockState().getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        AABB coreBounds = coreShape.bounds();
        assertEquals(6.0 / 16.0, coreBounds.minX, 1e-6);
        assertEquals(6.0 / 16.0, coreBounds.minY, 1e-6);
        assertEquals(6.0 / 16.0, coreBounds.minZ, 1e-6);
        assertEquals(10.0 / 16.0, coreBounds.maxX, 1e-6);
        assertEquals(10.0 / 16.0, coreBounds.maxY, 1e-6);
        assertEquals(10.0 / 16.0, coreBounds.maxZ, 1e-6);

        BlockState northArm = cable.defaultBlockState().setValue(PanCableBlock.NORTH, true);
        AABB northBounds = northArm.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()).bounds();
        assertEquals(0.0, northBounds.minZ, 1e-6);
        assertEquals(10.0 / 16.0, northBounds.maxZ, 1e-6);

        Block coreBlock = ClayiumBlocks.byPath("pan_core").get();
        assertInstanceOf(PANCoreBlock.class, coreBlock);
        assertInstanceOf(IPANConductor.class, coreBlock);

        assertTrue(PanCableBlock.canConnectTo(cable.defaultBlockState()));
        assertTrue(PanCableBlock.canConnectTo(coreBlock.defaultBlockState()));
        assertTrue(PanCableBlock.canConnectTo(ClayiumBlocks.byPath("antimatter_pan_adapter").get().defaultBlockState()));
        assertTrue(PanCableBlock.canConnectTo(ClayiumBlocks.byPath("basic_pan_duplicator").get().defaultBlockState()));
        assertFalse(PanCableBlock.canConnectTo(Blocks.COBBLESTONE.defaultBlockState()));
        assertFalse(PanCableBlock.canConnectTo(ClayiumBlocks.byPath("clay_bending_machine").get().defaultBlockState()));
        assertFalse(UtilPAN.isPANConductor(null, BlockPos.ZERO));
    }

    @Test
    void panAdapterTiersPageCountsAndFactories() {
        List<String> adapters = List.of(
                "antimatter_pan_adapter",
                "pure_antimatter_pan_adapter",
                "oec_pan_adapter",
                "opa_pan_adapter"
        );
        int[] expectedPages = {1, 2, 4, 8};
        for (int i = 0; i < adapters.size(); i++) {
            Block block = ClayiumBlocks.byPath(adapters.get(i)).get();
            PANAdapterBlock adapterBlock = assertInstanceOf(PANAdapterBlock.class, block);
            assertTrue(adapterBlock.getTier() >= 11);
            assertEquals("pan_adapter", ClayContainerBlock.resolveBlockEntityCategory(block));
            PANAdapterBlockEntity be = new PANAdapterBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            assertEquals(expectedPages[i], be.getPageNum());
            assertFalse(be.acceptEnergyClay());
        }
        assertNotNull(ClayiumBlockEntities.PAN_ADAPTER.get());
        assertEquals(10.0, PANCraftingTableFactory.ENERGY, 1e-6);
        assertEquals(800.0, PANFurnaceFactory.ENERGY, 1e-6);
    }

    @Test
    void panCoreResolvesCostsExcludesProhibitedAndDrivesDuplicator() {
        Block coreBlock = ClayiumBlocks.byPath("pan_core").get();
        assertEquals("pan_core", ClayContainerBlock.resolveBlockEntityCategory(coreBlock));
        PANCoreBlockEntity panCore = new PANCoreBlockEntity(BlockPos.ZERO, coreBlock.defaultBlockState());
        assertNotNull(ClayiumBlockEntities.PAN_CORE.get());

        Item ironDust = CMaterials.getItem(CMaterials.IRON, CShape.DUST);
        ChemicalMetalSeparatorRecipe sep = new ChemicalMetalSeparatorRecipe(
                new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1),
                6,
                100L,
                10L,
                List.of(new WeightedOutput(new ItemStack(ironDust), 1), new WeightedOutput(new ItemStack(Items.DIRT), 3))
        );

        // Chained conversions: Cobblestone (1) -> Stone (+800), Stone -> Iron Ingot (+500), plus prohibited Clay Plate
        Item clayPlate = ClayiumItems.byPath("clay_plate").get();
        PANConversion stoneConv = new PANConversion(
                List.of(new CountedIngredient(Ingredient.of(Blocks.COBBLESTONE), 1)),
                List.of(new ItemStack(Blocks.STONE)),
                800.0
        );
        PANConversion ironFromStone = new PANConversion(
                List.of(new CountedIngredient(Ingredient.of(Blocks.STONE), 2)),
                List.of(new ItemStack(Items.IRON_INGOT)),
                500.0
        );
        PANConversion plateConv = new PANConversion(
                List.of(new CountedIngredient(Ingredient.of(Blocks.CLAY), 1)),
                List.of(new ItemStack(clayPlate)),
                50.0
        );
        panCore.setConversionListForTest(List.of(ironFromStone, stoneConv, plateConv));
        panCore.refreshItemSet(List.of(sep));

        // Base clays (10^1 .. 10^13) & compressed clays are prohibited from duplication
        Block denseClay = ClayiumBlocks.byPath("dense_clay").get();
        Block octupleClay = ClayiumBlocks.byPath("octuple_compressed_energetic_clay").get();
        assertTrue(panCore.isProhibited(new ItemStack(denseClay)));
        assertTrue(panCore.isProhibited(new ItemStack(octupleClay)));
        assertTrue(panCore.isProhibited(new ItemStack(clayPlate)));
        assertTrue(panCore.isProhibited(new ItemStack(ClayiumItems.byPath("antimatter").get())));

        double denseCost = findCost(panCore.getIngredientItemList(), new ItemStack(denseClay));
        double octupleCost = findCost(panCore.getIngredientItemList(), new ItemStack(octupleClay));
        assertEquals(10.0, denseCost, 1e-6);
        assertEquals(1e13, octupleCost, 1e-6);

        // Separator base = (100 * 10 + 1000) = 2000; ironDust weight 1/4 -> 8000 CE
        assertEquals(8000.0, findCost(panCore.getIngredientItemList(), new ItemStack(ironDust)), 1e-6);

        // Stone = 1 + 800 = 801; Iron Ingot = 2 * 801 + 500 = 2102
        assertEquals(801.0, findCost(panCore.getConversionItemList(), new ItemStack(Blocks.STONE)), 1e-6);
        assertEquals(2102.0, findCost(panCore.getConversionItemList(), new ItemStack(Items.IRON_INGOT)), 1e-6);

        // Prohibited items never appear in duplicatable conversions
        assertFalse(panCore.getConversionEntries().stream().anyMatch(e -> ItemStack.isSameItem(e.stack(), new ItemStack(clayPlate))));
        assertTrue(panCore.getConversionEntries().stream().anyMatch(e -> ItemStack.isSameItem(e.stack(), new ItemStack(Items.IRON_INGOT))));

        // Duplicator end-to-end duplication backed by PANCore
        Block dupBlock = ClayiumBlocks.byPath("antimatter_pan_duplicator").get();
        assertInstanceOf(PANDuplicatorBlock.class, dupBlock);
        assertInstanceOf(IPANConductor.class, dupBlock);
        PANDuplicatorBlockEntity dup = new PANDuplicatorBlockEntity(BlockPos.ZERO, dupBlock.defaultBlockState());
        assertInstanceOf(IPANComponent.class, dup);
        dup.setPANCore(panCore, 400);
        dup.getClayEnergyStorage().addEnergy(10_000_000L);
        dup.setStack(0, new ItemStack(Items.IRON_INGOT, 1));
        dup.setStack(1, new ItemStack(ClayiumItems.byPath("antimatter").get(), 2));

        assertTrue(dup.canProceedCraft());
        dup.proceedCraft();
        assertEquals(1, dup.getStack(2).getCount());
        assertTrue(ItemStack.isSameItem(dup.getStack(2), new ItemStack(Items.IRON_INGOT)));
        // Pattern template kept untouched; antimatter consumed by 1
        assertEquals(1, dup.getStack(0).getCount());
        assertEquals(1, dup.getStack(1).getCount());
    }

    @Test
    void panCoreListPayloadAndMenusRoundTrip() {
        assertNotNull(ClayiumMenus.PAN_ADAPTER.get());
        assertNotNull(ClayiumMenus.PAN_CORE.get());

        PANCoreListPayload payload = new PANCoreListPayload(
                7,
                List.of(new PANCoreListPayload.Entry(new ItemStack(Items.IRON_INGOT), 2102.0, 2102.0)),
                List.of(new ItemStack(ClayiumItems.byPath("clay_plate").get()))
        );
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, ConnectionType.NEOFORGE);
        PANCoreListPayload.STREAM_CODEC.encode(buf, payload);
        PANCoreListPayload decoded = PANCoreListPayload.STREAM_CODEC.decode(buf);
        assertEquals(7, decoded.containerId());
        assertEquals(1, decoded.ingredients().size());
        assertEquals(2102.0, decoded.ingredients().getFirst().cost(), 1e-6);
        assertTrue(ItemStack.isSameItem(new ItemStack(Items.IRON_INGOT), decoded.ingredients().getFirst().stack()));
        assertEquals(1, decoded.prohibited().size());
    }

    private static double findCost(List<PANItemCost> list, ItemStack target) {
        for (PANItemCost entry : list) {
            if (entry.matchesType(target)) {
                return entry.consumption();
            }
        }
        return -1.0;
    }
}
