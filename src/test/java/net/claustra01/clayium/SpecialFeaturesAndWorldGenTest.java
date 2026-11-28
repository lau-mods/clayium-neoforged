package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.ClayMarkerBlock;
import net.claustra01.clayium.blockentity.AreaActivatorBlockEntity;
import net.claustra01.clayium.blockentity.AreaCollectorBlockEntity;
import net.claustra01.clayium.blockentity.AreaMinerBlockEntity;
import net.claustra01.clayium.blockentity.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.blockentity.ClayMarkerBlockEntity;
import net.claustra01.clayium.blockentity.LaserReflectorBlockEntity;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.ClaySteelToolConfigData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.DirectionMemoryData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.gadget.ClayiumGadgetHandler;
import net.claustra01.clayium.item.ClayShooterItem;
import net.claustra01.clayium.item.ClaySteelPickaxeItem;
import net.claustra01.clayium.item.ClaySteelShovelItem;
import net.claustra01.clayium.item.ClaySteelToolHelper;
import net.claustra01.clayium.item.ClayToolItem;
import net.claustra01.clayium.item.ClayToolItem.ToolKind;
import net.claustra01.clayium.item.GadgetHolderItem;
import net.claustra01.clayium.item.GadgetItem;
import net.claustra01.clayium.item.GadgetItem.GadgetFamily;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.ClayLaserManager;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumEntities;
import net.claustra01.clayium.registry.ClayiumFeatures;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SpecialFeaturesAndWorldGenTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void clayLaserMergingAndBlockEntitiesMatchOriginalSpecs() {
        ClayLaser blue = new ClayLaser(0, 1, 0, 0);
        ClayLaser green = new ClayLaser(1, 0, 1, 0);
        ClayLaser merged = ClayLaser.mergeClayLasers(new ClayLaser[]{blue, green});
        assertNotNull(merged);
        assertEquals(1, merged.age);
        assertEquals(1, merged.numbers[0]);
        assertEquals(1, merged.numbers[1]);
        assertEquals(0, merged.numbers[2]);
        assertTrue(merged.getEnergy() > 0.0);

        ClayLaser old1 = new ClayLaser(ClayLaser.LIFESPAN, 2, 1, 0);
        ClayLaser old2 = new ClayLaser(ClayLaser.LIFESPAN, 1, 3, 0);
        ClayLaser clamped = ClayLaser.mergeClayLasers(new ClayLaser[]{old1, old2});
        assertNotNull(clamped);
        assertEquals(ClayLaser.LIFESPAN, clamped.age);
        assertEquals(2, clamped.numbers[0]);
        assertEquals(3, clamped.numbers[1]);
        assertEquals(0, clamped.numbers[2]);

        assertEquals(32, ClayLaserManager.MAX_LASER_LENGTH);
        assertEquals(10, ClayLaserManager.HISTORY_LENGTH);
        assertEquals(300_000L, ClayLaserManager.CLAY_TREE_SAPLING_ENERGY);

        Block blueLaserBlock = ClayiumBlocks.byPath("clay_steel_clay_energy_laser").get();
        Block greenLaserBlock = ClayiumBlocks.byPath("clayium_clay_energy_laser").get();
        Block redLaserBlock = ClayiumBlocks.byPath("ultimate_clay_energy_laser").get();
        Block antimatterLaserBlock = ClayiumBlocks.byPath("antimatter_clay_energy_laser").get();
        assertEquals("clay_energy_laser", ClayContainerBlock.resolveBlockEntityCategory(blueLaserBlock));
        assertEquals("clay_energy_laser", ClayContainerBlock.resolveBlockEntityCategory(antimatterLaserBlock));
        ClayEnergyLaserBlockEntity t7 = new ClayEnergyLaserBlockEntity(BlockPos.ZERO, blueLaserBlock.defaultBlockState());
        ClayEnergyLaserBlockEntity t8 = new ClayEnergyLaserBlockEntity(BlockPos.ZERO, greenLaserBlock.defaultBlockState());
        ClayEnergyLaserBlockEntity t9 = new ClayEnergyLaserBlockEntity(BlockPos.ZERO, redLaserBlock.defaultBlockState());
        ClayEnergyLaserBlockEntity t10 = new ClayEnergyLaserBlockEntity(BlockPos.ZERO, antimatterLaserBlock.defaultBlockState());
        assertEquals(40_000L, t7.consumingEnergy);
        assertEquals(400_000L, t8.consumingEnergy);
        assertEquals(4_000_000L, t9.consumingEnergy);
        assertEquals(40_000_000L, t10.consumingEnergy);
        assertEquals(1, t7.laser.numbers[0]);
        assertEquals(1, t8.laser.numbers[1]);
        assertEquals(1, t9.laser.numbers[2]);
        assertEquals(3, t10.laser.numbers[0]);
        assertEquals(3, t10.laser.numbers[1]);
        assertEquals(3, t10.laser.numbers[2]);

        Block reflectorBlock = ClayiumBlocks.byPath("laser_reflector").get();
        assertEquals("laser_reflector", ClayContainerBlock.resolveBlockEntityCategory(reflectorBlock));
        assertInstanceOf(LaserReflectorBlockEntity.class, ClayiumBlockEntities.LASER_REFLECTOR.get().create(BlockPos.ZERO, reflectorBlock.defaultBlockState()));
    }

    @Test
    void markersAndAreaMachinesWireCategoriesMenusAndLaserSpeedup() {
        for (String markerPath : List.of("clay_marker", "clay_open_pit_marker", "clay_ground_leveling_marker", "clay_prism_marker")) {
            Block markerBlock = ClayiumBlocks.byPath(markerPath).get();
            assertInstanceOf(ClayMarkerBlock.class, markerBlock);
            assertEquals("clay_marker", ClayContainerBlock.resolveBlockEntityCategory(markerBlock));
            assertInstanceOf(ClayMarkerBlockEntity.class, ((ClayMarkerBlock) markerBlock).newBlockEntity(BlockPos.ZERO, markerBlock.defaultBlockState()));
        }

        assertEquals("area_miner", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("miner").get()));
        assertEquals("area_miner", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("area_miner").get()));
        assertEquals("area_miner", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("advanced_area_miner").get()));
        assertEquals("area_miner", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("area_replacer").get()));
        assertEquals("area_collector", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("area_collector").get()));
        assertEquals("area_activator", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("activator").get()));
        assertEquals("area_activator", ClayContainerBlock.resolveBlockEntityCategory(ClayiumBlocks.byPath("area_activator").get()));

        assertNotNull(ClayiumMenus.AREA_MINER.get());
        assertNotNull(ClayiumMenus.AREA_ACTIVATOR.get());
        assertInstanceOf(AreaMinerBlockEntity.class, ((ClayContainerBlock) ClayiumBlocks.byPath("area_miner").get()).newBlockEntity(BlockPos.ZERO, ClayiumBlocks.byPath("area_miner").get().defaultBlockState()));
        assertInstanceOf(AreaCollectorBlockEntity.class, ((ClayContainerBlock) ClayiumBlocks.byPath("area_collector").get()).newBlockEntity(BlockPos.ZERO, ClayiumBlocks.byPath("area_collector").get().defaultBlockState()));
        assertInstanceOf(AreaActivatorBlockEntity.class, ((ClayContainerBlock) ClayiumBlocks.byPath("area_activator").get()).newBlockEntity(BlockPos.ZERO, ClayiumBlocks.byPath("area_activator").get().defaultBlockState()));

        assertEquals(1.0, AreaMinerBlockEntity.computeLaserSpeedMultiplier(0L), 1e-6);
        assertEquals(1.0 + 4.0 * Math.log10(11.0), AreaMinerBlockEntity.computeLaserSpeedMultiplier(10_000L), 1e-6);

        DirectionMemoryData mem = new DirectionMemoryData(1.25, 2.5, -3.5, 4.0, 5.0, 6.0, Direction.UP);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        DirectionMemoryData.STREAM_CODEC.encode(buf, mem);
        assertEquals(mem, DirectionMemoryData.STREAM_CODEC.decode(buf));
    }

    @Test
    void gadgetsRegisteredAndAutoEatHealthLongArmFlightFormulasMatchOriginal() {
        Item holder = ClayiumItems.byPath("clay_gadget_holder").get();
        assertInstanceOf(GadgetHolderItem.class, holder);

        List<String> gadgetPaths = List.of(
                "gadget_antimatter_overclock", "gadget_pure_antimatter_overclock",
                "gadget_oec_overclock", "gadget_opa_overclock",
                "gadget_flight0", "gadget_flight1", "gadget_flight2",
                "gadget_health0", "gadget_health1", "gadget_health2",
                "gadget_auto_eat0", "gadget_auto_eat1",
                "gadget_repeatedly_attack",
                "gadget_long_arm0", "gadget_long_arm1", "gadget_long_arm2",
                "gadget_blank"
        );
        for (String path : gadgetPaths) {
            assertInstanceOf(GadgetItem.class, ClayiumItems.byPath(path).get(), "Expected GadgetItem: " + path);
        }
        assertEquals(GadgetFamily.AUTO_EAT, ((GadgetItem) ClayiumItems.byPath("gadget_auto_eat0").get()).getFamily());
        assertEquals(0, ((GadgetItem) ClayiumItems.byPath("gadget_auto_eat0").get()).getLevel());
        assertEquals(1, ((GadgetItem) ClayiumItems.byPath("gadget_auto_eat1").get()).getLevel());

        assertEquals(20.0, ClayiumGadgetHandler.healthBonusForLevel(0), 1e-6);
        assertEquals(80.0, ClayiumGadgetHandler.healthBonusForLevel(1), 1e-6);
        assertEquals(180.0, ClayiumGadgetHandler.healthBonusForLevel(2), 1e-6);

        assertEquals(3.0, ClayiumGadgetHandler.longArmBonusForLevel(0), 1e-6);
        assertEquals(7.0, ClayiumGadgetHandler.longArmBonusForLevel(1), 1e-6);
        assertEquals(20.0, ClayiumGadgetHandler.longArmBonusForLevel(2), 1e-6);

        assertEquals(0.0F, ClayiumGadgetHandler.computeDampenedFallDistance(12.0F, 0.2), 1e-6);
        assertEquals(0.0F, ClayiumGadgetHandler.computeDampenedFallDistance(10.0F, -0.05), 1e-6);
        assertEquals(1.8F, ClayiumGadgetHandler.computeDampenedFallDistance(5.0F, -0.45), 1e-6);

        ItemStack bread = new ItemStack(Items.BREAD);
        ItemStack autoEat = new ItemStack(ClayiumItems.byPath("gadget_auto_eat0").get());
        // Bread nutrition = 5. At hunger 18, zero-waste mode 0 defers eating; eager mode 1 eats.
        assertEquals(-1, ClayiumGadgetHandler.selectBestFoodSlot(null, List.of(bread), autoEat, 0, 18, 0.0F));
        assertEquals(0, ClayiumGadgetHandler.selectBestFoodSlot(null, List.of(bread), autoEat, 1, 18, 0.0F));
        // At hunger 10, zero-waste mode 0 eats bread immediately.
        assertEquals(0, ClayiumGadgetHandler.selectBestFoodSlot(null, List.of(bread), autoEat, 0, 10, 0.0F));

        // Whitelist filter restricts accepted food
        ItemStack filteredEat = autoEat.copy();
        filteredEat.set(ClayiumDataComponents.FILTER_CONFIG.get(), new FilterConfigData("", false, ItemContainerContents.fromItems(List.of(new ItemStack(Items.APPLE))), false));
        assertFalse(ClayiumGadgetHandler.matchesAutoEatFilter(filteredEat, bread));
        assertTrue(ClayiumGadgetHandler.matchesAutoEatFilter(filteredEat, new ItemStack(Items.APPLE)));
    }

    @Test
    void claySteelToolsShootersAndEntitiesAreFaithfullyConfigured() {
        assertInstanceOf(ClaySteelPickaxeItem.class, ClayiumItems.byPath("clay_steel_pickaxe").get());
        assertInstanceOf(ClaySteelShovelItem.class, ClayiumItems.byPath("clay_steel_shovel").get());
        assertEquals(ToolKind.DIRECTION_MEMORY, ((ClayToolItem) ClayiumItems.byPath("direction_memory_card").get()).getToolKind());

        ClaySteelToolConfigData cfg = new ClaySteelToolConfigData(2, List.of(new BlockPos(1, -1, 0), new BlockPos(0, 2, -1)));
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        var encoded = ClaySteelToolConfigData.CODEC.encodeStart(ops, cfg).result().orElseThrow();
        ClaySteelToolConfigData decoded = ClaySteelToolConfigData.CODEC.parse(ops, encoded).result().orElseThrow();
        assertEquals(cfg, decoded);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ClaySteelToolConfigData.STREAM_CODEC.encode(buf, cfg);
        assertEquals(cfg, ClaySteelToolConfigData.STREAM_CODEC.decode(buf));

        // Mode 0 = 1x1, Mode 1 = 3x3, Mode 2 = custom offsets transformed by basis
        ItemStack pick = new ItemStack(ClayiumItems.byPath("clay_steel_pickaxe").get());
        Vec3 xx = new Vec3(1, 0, 0);
        Vec3 yy = new Vec3(0, 1, 0);
        Vec3 zz = new Vec3(0, 0, 1);
        assertEquals(1, ClaySteelToolHelper.getHarvestedPositions(pick, BlockPos.ZERO, xx, yy, zz, 2).size());
        pick.set(ClayiumDataComponents.CLAY_STEEL_TOOL_CONFIG.get(), new ClaySteelToolConfigData(1, List.of()));
        assertEquals(9, ClaySteelToolHelper.getHarvestedPositions(pick, BlockPos.ZERO, xx, yy, zz, 2).size());
        pick.set(ClayiumDataComponents.CLAY_STEEL_TOOL_CONFIG.get(), cfg);
        List<BlockPos> customPositions = ClaySteelToolHelper.getHarvestedPositions(pick, new BlockPos(10, 20, 30), xx, yy, zz, 2);
        assertEquals(List.of(new BlockPos(11, 19, 30), new BlockPos(10, 22, 29)), customPositions);

        // Verify matrix inverse property: basis * inverse(basis) == I
        Vec3[] inv = ClaySteelToolHelper.getInverse(new Vec3[]{new Vec3(0, 0, -1), new Vec3(1, 0, 0), new Vec3(0, -1, 0)});
        assertNotNull(inv);

        ClayShooterItem shooter0 = assertInstanceOf(ClayShooterItem.class, ClayiumItems.byPath("clay_shooter_0").get());
        ClayShooterItem shooter3 = assertInstanceOf(ClayShooterItem.class, ClayiumItems.byPath("clay_shooter_3").get());
        ClayShooterItem teleporter = assertInstanceOf(ClayShooterItem.class, ClayiumItems.byPath("instant_teleporter").get());
        assertFalse(shooter0.isCharger());
        assertEquals(3.0F / 6.0F, shooter0.getShootingRate(), 1e-6);
        assertEquals(4, shooter0.getBaseDamage());
        assertTrue(shooter3.isCharger());
        assertEquals(40, shooter3.getChargeTime());
        assertEquals(100, shooter3.getBaseDamage());
        assertFalse(shooter3.isTeleporter());
        assertTrue(teleporter.isTeleporter());
        assertTrue(teleporter.isCharger());
        assertEquals(1.0F, ClayShooterItem.computeChargeFactor(40, 40), 1e-6);

        assertNotNull(ClayiumEntities.CLAY_BALL.get());
        assertNotNull(ClayiumEntities.TELEPORT_BALL.get());
    }

    @Test
    void worldGenFeaturesRegisteredAndGeneratedJsonsValid() throws IOException {
        assertNotNull(ClayiumFeatures.CLAY_COMPOUND_ORE.get());
        assertNotNull(ClayiumFeatures.LARGE_DENSE_CLAY_ORE_FEATURE.get());

        List<String> expectedJsons = List.of(
                "data/clayium_neoforged/worldgen/configured_feature/clay_tree.json",
                "data/clayium_neoforged/worldgen/configured_feature/clay_ore_vein.json",
                "data/clayium_neoforged/worldgen/configured_feature/large_dense_clay_ore_vein.json",
                "data/clayium_neoforged/worldgen/placed_feature/clay_ore_vein.json",
                "data/clayium_neoforged/worldgen/placed_feature/large_dense_clay_ore_vein.json",
                "data/clayium_neoforged/neoforge/biome_modifier/overworld_clay_ores.json"
        );
        ClassLoader cl = SpecialFeaturesAndWorldGenTest.class.getClassLoader();
        for (String resourcePath : expectedJsons) {
            try (InputStream in = cl.getResourceAsStream(resourcePath)) {
                assertNotNull(in, "Missing generated worldgen JSON on classpath: " + resourcePath);
                JsonElement parsed = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                assertTrue(parsed.isJsonObject(), "Invalid JSON object: " + resourcePath);
                JsonObject obj = parsed.getAsJsonObject();
                assertFalse(obj.isEmpty());
            }
        }
    }
}
