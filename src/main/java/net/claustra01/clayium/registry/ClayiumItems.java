package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.item.ClayMaterialItem;
import net.claustra01.clayium.item.ClayPickaxeItem;
import net.claustra01.clayium.item.ClayShooterItem;
import net.claustra01.clayium.item.ClayShovelItem;
import net.claustra01.clayium.item.ClaySteelPickaxeItem;
import net.claustra01.clayium.item.ClaySteelShovelItem;
import net.claustra01.clayium.item.ClayToolItem;
import net.claustra01.clayium.item.ClayToolItem.ToolKind;
import net.claustra01.clayium.item.CraftingToolItem;
import net.claustra01.clayium.item.FilterItem;
import net.claustra01.clayium.item.FilterItem.FilterKind;
import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.item.GadgetHolderItem;
import net.claustra01.clayium.item.GadgetItem;
import net.claustra01.clayium.item.GadgetItem.GadgetFamily;
import net.claustra01.clayium.item.SimpleTieredItem;
import net.claustra01.clayium.item.TieredBlockItem;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all 309 standalone Clayium items and all 427 BlockItems (736 total).
 */
public final class ClayiumItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(ClayiumMod.MOD_ID);
    private static final Map<String, DeferredItem<? extends Item>> STANDALONE_ITEMS = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<TieredBlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<? extends Item>> ALL_ITEMS = new LinkedHashMap<>();

    static {
        registerStandaloneItems();
        registerBlockItems();
        CMaterials.bindDefaults();
    }

    private ClayiumItems() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }

    public static Map<String, DeferredItem<? extends Item>> standaloneItems() {
        return Collections.unmodifiableMap(STANDALONE_ITEMS);
    }

    public static Map<String, DeferredItem<TieredBlockItem>> blockItems() {
        return Collections.unmodifiableMap(BLOCK_ITEMS);
    }

    public static DeferredItem<? extends Item> byPath(String path) {
        DeferredItem<? extends Item> item = ALL_ITEMS.get(path);
        if (item == null) {
            throw new IllegalArgumentException("Unknown Clayium item path: " + path);
        }
        return item;
    }

    private static <T extends Item> DeferredItem<T> reg(String path, Supplier<T> factory) {
        DeferredItem<T> holder = REGISTRY.register(path, factory);
        STANDALONE_ITEMS.put(path, holder);
        ALL_ITEMS.put(path, holder);
        return holder;
    }

    private static void regMat(String path, CMaterial mat, CShape shape, Integer tier, boolean layered, String icon) {
        DeferredItem<ClayMaterialItem> holder = reg(path, () -> new ClayMaterialItem(new Properties(), mat, shape, tier, layered, icon));
        CMaterials.bind(mat, shape, holder);
    }

    private static void registerBlockItems() {
        for (Map.Entry<String, DeferredBlock<? extends Block>> entry : ClayiumBlocks.all().entrySet()) {
            String path = entry.getKey();
            DeferredBlock<? extends Block> block = entry.getValue();
            DeferredItem<TieredBlockItem> itemHolder = REGISTRY.register(path, () -> new TieredBlockItem(block.get(), new Properties()));
            BLOCK_ITEMS.put(path, itemHolder);
            ALL_ITEMS.put(path, itemHolder);
        }
        CMaterials.bind(CMaterials.DENSE_CLAY, CShape.BLOCK, () -> ClayiumItems.byPath("dense_clay").get());
        CMaterials.bind(CMaterials.IND_CLAY, CShape.BLOCK, () -> ClayiumItems.byPath("industrial_clay").get());
        CMaterials.bind(CMaterials.ADVIND_CLAY, CShape.BLOCK, () -> ClayiumItems.byPath("advanced_industrial_clay").get());
        CMaterials.bind(CMaterials.OCTUPLE_CLAY, CShape.BLOCK, () -> ClayiumItems.byPath("octuple_energetic_clay").get());
        for (String path : ClayiumBlocks.all().keySet()) {
            if (path.endsWith("_block")) {
                String prefix = path.substring(0, path.length() - "_block".length());
                CMaterials.byName().values().stream()
                        .filter(m -> m.idPrefix().equals(prefix))
                        .findFirst()
                        .ifPresent(mat -> CMaterials.bind(mat, CShape.BLOCK, () -> ClayiumItems.byPath(path).get()));
            }
        }
    }

    private static void registerStandaloneItems() {
        reg("raw_clay_rolling_pin", () -> new SimpleTieredItem(new Properties(), null, "rawclayrollingpin"));
        reg("raw_clay_slicer", () -> new SimpleTieredItem(new Properties(), null, "rawclayslicer"));
        reg("raw_clay_spatula", () -> new SimpleTieredItem(new Properties(), null, "rawclayspatula"));
        reg("clay_rolling_pin", () -> new CraftingToolItem(60, 4, "clayrollingpin"));
        reg("clay_slicer", () -> new CraftingToolItem(60, 3, "clayslicer"));
        reg("clay_spatula", () -> new CraftingToolItem(36, 2, "clayspatula"));
        reg("clay_wrench", () -> new ClayToolItem(new Properties().stacksTo(1), null, "claywrench", ToolKind.CLAY_WRENCH));
        reg("clay_shovel", ClayShovelItem::new);
        reg("clay_pickaxe", ClayPickaxeItem::new);
        reg("clay_steel_pickaxe", ClaySteelPickaxeItem::new);
        reg("clay_steel_shovel", ClaySteelShovelItem::new);
        reg("compressed_clay_shard", () -> new SimpleTieredItem(new Properties(), null, "compressedclay-shard-1"));
        reg("industrial_clay_shard", () -> new SimpleTieredItem(new Properties(), null, "compressedclay-shard-2"));
        reg("advanced_industrial_clay_shard", () -> new SimpleTieredItem(new Properties(), null, "compressedclay-shard-3"));
        reg("clay_io_configurator", () -> new ClayToolItem(new Properties().stacksTo(1), null, "iotool", ToolKind.IO_CONFIGURATOR));
        reg("clay_piping_configurator", () -> new ClayToolItem(new Properties().stacksTo(1), null, "pipingtool", ToolKind.PIPING_CONFIGURATOR));
        reg("memory_card", () -> new ClayToolItem(new Properties().stacksTo(1), null, "memorycard", ToolKind.MEMORY_CARD));
        reg("item_filter_duplicator", () -> new FilterItem(7, "filterduplicator", FilterKind.DUPLICATOR));
        reg("whitelist_item_filter", () -> new FilterItem(5, "filterwhitelist", FilterKind.WHITELIST));
        reg("blacklist_item_filter", () -> new FilterItem(5, "filterblacklist", FilterKind.BLACKLIST));
        reg("fuzzy_item_filter", () -> new FilterItem(7, "filterfuzzy", FilterKind.FUZZY));
        reg("tag_item_filter", () -> new FilterItem(6, "filterod", FilterKind.TAG));
        reg("display_name_item_filter", () -> new FilterItem(6, "filtername", FilterKind.DISPLAY_NAME));
        reg("translation_key_item_filter", () -> new FilterItem(6, "filteruname", FilterKind.TRANSLATION_KEY));
        reg("registry_id_item_filter", () -> new FilterItem(6, "filterid", FilterKind.REGISTRY_ID));
        reg("mod_id_item_filter", () -> new FilterItem(6, "filtermodid", FilterKind.MOD_ID));
        reg("damage_value_item_filter", () -> new FilterItem(6, "filtermeta", FilterKind.DAMAGE_VALUE));
        reg("block_state_item_filter", () -> new FilterItem(6, "filterblockmeta", FilterKind.BLOCK_STATE));
        reg("harvestable_block_item_filter", () -> new FilterItem(6, "filterblockmeta", FilterKind.HARVESTABLE_BLOCK));
        reg("clay_shooter_0", () -> new ClayShooterItem(10000, 6, "clayshooter0", 2, 1.2F, 25.0F, 4, 6, 0, false));
        reg("clay_shooter_1", () -> new ClayShooterItem(1000, 6, "clayshooter1", 4, 4.5F, 3.0F, 7, 12, 0, false));
        reg("clay_shooter_2", () -> new ClayShooterItem(8000, 6, "clayshooter2", 2, 15.0F, 0.0F, 25, 8, 20, false));
        reg("clay_shooter_3", () -> new ClayShooterItem(2500, 6, "clayshooter3", 3, 30.0F, 0.0F, 100, 8, 40, false));
        reg("instant_teleporter", () -> new ClayShooterItem(2500, 11, "instantteleporter", 3, 30.0F, 0.0F, 100, 8, 40, true));
        reg("synchronizer", () -> new ClayToolItem(new Properties().stacksTo(1), null, "synchronizer", ToolKind.SYNCHRONIZER));
        reg("direction_memory_card", () -> new ClayToolItem(new Properties().stacksTo(1), null, "directionmemory", ToolKind.DIRECTION_MEMORY));
        reg("clay_circuit", () -> new SimpleTieredItem(new Properties(), 2, "claycircuit"));
        reg("clay_circuit_board", () -> new SimpleTieredItem(new Properties(), 2, "claycircuitboard"));
        reg("simple_circuit", () -> new SimpleTieredItem(new Properties(), 3, "simplecircuit"));
        reg("cee_board", () -> new SimpleTieredItem(new Properties(), 3, "ceeboard"));
        reg("cee_circuit", () -> new SimpleTieredItem(new Properties(), 3, "ceecircuit"));
        reg("cee", () -> new SimpleTieredItem(new Properties(), 3, "cee"));
        reg("laser_parts", () -> new SimpleTieredItem(new Properties(), 7, "laserparts"));
        reg("antimatter_seed", () -> new SimpleTieredItem(new Properties(), 9, "antimatterseed"));
        reg("synchronous_parts", () -> new SimpleTieredItem(new Properties(), 9, "synchronousparts"));
        reg("teleportation_parts", () -> new SimpleTieredItem(new Properties(), 11, "teleportationparts"));
        reg("manipulator1", () -> new SimpleTieredItem(new Properties(), 6, "manipulator"));
        reg("manipulator2", () -> new SimpleTieredItem(new Properties(), 8, "manipulator2"));
        reg("manipulator3", () -> new SimpleTieredItem(new Properties(), 12, "manipulator3"));
        reg("basic_circuit", () -> new SimpleTieredItem(new Properties(), 4, "basiccircuit"));
        reg("advanced_circuit", () -> new SimpleTieredItem(new Properties(), 5, "advancedcircuit"));
        reg("precision_circuit", () -> new SimpleTieredItem(new Properties(), 6, "precisioncircuit"));
        reg("integrated_circuit", () -> new SimpleTieredItem(new Properties(), 7, "integratedcircuit"));
        reg("clay_core", () -> new SimpleTieredItem(new Properties(), 8, "claycore"));
        reg("clay_brain", () -> new SimpleTieredItem(new Properties(), 9, "claybrain"));
        reg("clay_spirit", () -> new SimpleTieredItem(new Properties(), 10, "clayspirit"));
        reg("clay_soul", () -> new SimpleTieredItem(new Properties(), 11, "claysoul"));
        reg("clay_anima", () -> new SimpleTieredItem(new Properties(), 12, "clayanima"));
        reg("clay_psyche", () -> new SimpleTieredItem(new Properties(), 13, "claypsyche"));
        reg("fluid_capsule_1000", () -> new FluidCapsuleItem(1000, "capsule1000"));
        reg("fluid_capsule_125", () -> new FluidCapsuleItem(125, "capsule0125"));
        reg("fluid_capsule_25", () -> new FluidCapsuleItem(25, "capsule0025"));
        reg("fluid_capsule_5", () -> new FluidCapsuleItem(5, "capsule0005"));
        reg("fluid_capsule_1", () -> new FluidCapsuleItem(1, "capsule0001"));
        reg("clay_gadget_holder", GadgetHolderItem::new);
        reg("gadget_antimatter_overclock", () -> new GadgetItem(10, "gadget_antimatteroverclocker", GadgetFamily.OVERCLOCK, 0));
        reg("gadget_pure_antimatter_overclock", () -> new GadgetItem(11, "gadget_pureantimatteroverclocker", GadgetFamily.OVERCLOCK, 1));
        reg("gadget_oec_overclock", () -> new GadgetItem(12, "gadget_oecoverclocker", GadgetFamily.OVERCLOCK, 2));
        reg("gadget_opa_overclock", () -> new GadgetItem(13, "gadget_opaoverclocker", GadgetFamily.OVERCLOCK, 3));
        reg("gadget_flight0", () -> new GadgetItem(12, "gadget_flight0", GadgetFamily.FLIGHT, 0));
        reg("gadget_flight1", () -> new GadgetItem(13, "gadget_flight1", GadgetFamily.FLIGHT, 1));
        reg("gadget_flight2", () -> new GadgetItem(13, "gadget_flight2", GadgetFamily.FLIGHT, 2));
        reg("gadget_health0", () -> new GadgetItem(6, "gadget_health0", GadgetFamily.HEALTH, 0));
        reg("gadget_health1", () -> new GadgetItem(10, "gadget_health1", GadgetFamily.HEALTH, 1));
        reg("gadget_health2", () -> new GadgetItem(12, "gadget_health2", GadgetFamily.HEALTH, 2));
        reg("gadget_auto_eat0", () -> new GadgetItem(7, "gadget_autoeat0", GadgetFamily.AUTO_EAT, 0));
        reg("gadget_auto_eat1", () -> new GadgetItem(7, "gadget_autoeat1", GadgetFamily.AUTO_EAT, 1));
        reg("gadget_repeatedly_attack", () -> new GadgetItem(10, "gadget_repattack", GadgetFamily.REPEATEDLY_ATTACK, 0));
        reg("gadget_long_arm0", () -> new GadgetItem(6, "gadget_longarm0", GadgetFamily.LONG_ARM, 0));
        reg("gadget_long_arm1", () -> new GadgetItem(8, "gadget_longarm1", GadgetFamily.LONG_ARM, 1));
        reg("gadget_long_arm2", () -> new GadgetItem(12, "gadget_longarm2", GadgetFamily.LONG_ARM, 2));
        reg("gadget_blank", () -> new GadgetItem(6, "gadget_blank", GadgetFamily.BLANK, 0));
        regMat("clay_plate", CMaterials.CLAY, CShape.PLATE, 1, false, "clayplate");
        regMat("clay_stick", CMaterials.CLAY, CShape.STICK, 1, false, "claystick");
        regMat("clay_short_stick", CMaterials.CLAY, CShape.SHORT_STICK, 1, false, "shortclaystick");
        regMat("clay_ring", CMaterials.CLAY, CShape.RING, 1, false, "clayring");
        regMat("clay_small_ring", CMaterials.CLAY, CShape.SMALL_RING, 1, false, "smallclayring");
        regMat("clay_gear", CMaterials.CLAY, CShape.GEAR, 1, false, "claygear");
        regMat("clay_blade", CMaterials.CLAY, CShape.BLADE, 1, false, "clayblade");
        regMat("clay_needle", CMaterials.CLAY, CShape.NEEDLE, 1, false, "clayneedle");
        regMat("clay_disc", CMaterials.CLAY, CShape.DISC, 1, false, "claydisc");
        regMat("clay_small_disc", CMaterials.CLAY, CShape.SMALL_DISC, 1, false, "smallclaydisc");
        regMat("clay_cylinder", CMaterials.CLAY, CShape.CYLINDER, 1, false, "claycylinder");
        regMat("clay_pipe", CMaterials.CLAY, CShape.PIPE, 1, false, "claypipe");
        regMat("clay_large_ball", CMaterials.CLAY, CShape.LARGE_BALL, 1, false, "largeclayball");
        regMat("clay_large_plate", CMaterials.CLAY, CShape.LARGE_PLATE, 1, false, "largeclayplate");
        regMat("clay_grinding_head", CMaterials.CLAY, CShape.GRINDING_HEAD, 1, false, "claygrindinghead");
        regMat("clay_bearing", CMaterials.CLAY, CShape.BEARING, 1, false, "claybearing");
        regMat("clay_spindle", CMaterials.CLAY, CShape.SPINDLE, 1, false, "clayspindle");
        regMat("clay_cutting_head", CMaterials.CLAY, CShape.CUTTING_HEAD, 1, false, "claycuttinghead");
        regMat("clay_wheel", CMaterials.CLAY, CShape.WATER_WHEEL, 1, false, "claywaterwheel");
        regMat("dense_clay_plate", CMaterials.DENSE_CLAY, CShape.PLATE, 2, false, "denseclayplate");
        regMat("dense_clay_stick", CMaterials.DENSE_CLAY, CShape.STICK, 2, false, "denseclaystick");
        regMat("dense_clay_short_stick", CMaterials.DENSE_CLAY, CShape.SHORT_STICK, 2, false, "shortdenseclaystick");
        regMat("dense_clay_ring", CMaterials.DENSE_CLAY, CShape.RING, 2, false, "denseclayring");
        regMat("dense_clay_small_ring", CMaterials.DENSE_CLAY, CShape.SMALL_RING, 2, false, "smalldenseclayring");
        regMat("dense_clay_gear", CMaterials.DENSE_CLAY, CShape.GEAR, 2, false, "denseclaygear");
        regMat("dense_clay_blade", CMaterials.DENSE_CLAY, CShape.BLADE, 2, false, "denseclayblade");
        regMat("dense_clay_needle", CMaterials.DENSE_CLAY, CShape.NEEDLE, 2, false, "denseclayneedle");
        regMat("dense_clay_disc", CMaterials.DENSE_CLAY, CShape.DISC, 2, false, "denseclaydisc");
        regMat("dense_clay_small_disc", CMaterials.DENSE_CLAY, CShape.SMALL_DISC, 2, false, "smalldenseclaydisc");
        regMat("dense_clay_cylinder", CMaterials.DENSE_CLAY, CShape.CYLINDER, 2, false, "denseclaycylinder");
        regMat("dense_clay_pipe", CMaterials.DENSE_CLAY, CShape.PIPE, 2, false, "denseclaypipe");
        regMat("dense_clay_large_plate", CMaterials.DENSE_CLAY, CShape.LARGE_PLATE, 2, false, "largedenseclayplate");
        regMat("dense_clay_grinding_head", CMaterials.DENSE_CLAY, CShape.GRINDING_HEAD, 2, false, "denseclaygrindinghead");
        regMat("dense_clay_bearing", CMaterials.DENSE_CLAY, CShape.BEARING, 2, false, "denseclaybearing");
        regMat("dense_clay_spindle", CMaterials.DENSE_CLAY, CShape.SPINDLE, 2, false, "denseclayspindle");
        regMat("dense_clay_cutting_head", CMaterials.DENSE_CLAY, CShape.CUTTING_HEAD, 2, false, "denseclaycuttinghead");
        regMat("dense_clay_wheel", CMaterials.DENSE_CLAY, CShape.WATER_WHEEL, 2, false, "denseclaywaterwheel");
        regMat("industrial_clay_plate", CMaterials.IND_CLAY, CShape.PLATE, 3, false, "indclayplate");
        regMat("industrial_clay_large_plate", CMaterials.IND_CLAY, CShape.LARGE_PLATE, 3, false, "indclaylargeplate");
        regMat("advanced_industrial_clay_plate", CMaterials.ADVIND_CLAY, CShape.PLATE, 4, false, "advindclayplate");
        regMat("advanced_industrial_clay_large_plate", CMaterials.ADVIND_CLAY, CShape.LARGE_PLATE, 4, false, "advindclaylargeplate");
        regMat("clay_dust", CMaterials.CLAY, CShape.DUST, 1, false, "claydust");
        regMat("dense_clay_dust", CMaterials.DENSE_CLAY, CShape.DUST, 2, false, "denseclaydust");
        regMat("industrial_clay_dust", CMaterials.IND_CLAY, CShape.DUST, 3, false, "indclaydust");
        regMat("advanced_industrial_clay_dust", CMaterials.ADVIND_CLAY, CShape.DUST, 4, false, "advindclaydust");
        regMat("energized_clay_dust", CMaterials.ENG_CLAY, CShape.DUST, 3, false, "engclaydust");
        regMat("excited_clay_dust", CMaterials.EXC_CLAY, CShape.DUST, 7, false, "excclaydust");
        regMat("salt", CMaterials.SALT, CShape.DUST, 4, false, "saltdust");
        regMat("calcareous_clay_dust", CMaterials.CAL_CLAY, CShape.DUST, 4, false, "calclaydust");
        regMat("calcium_chloride_dust", CMaterials.CALCIUM_CHLORIDE, CShape.DUST, 4, false, "calciumchloridedust");
        regMat("sodium_carbonate_dust", CMaterials.SODIUM_CARBONATE, CShape.DUST, 4, false, "sodiumcarbonatedust");
        regMat("quartz_dust", CMaterials.QUARTZ, CShape.DUST, 4, false, "quartzdust");
        regMat("impure_silicon_dust", CMaterials.IMPURE_SILICON, CShape.DUST, 5, true, "dust");
        regMat("impure_silicon_ingot", CMaterials.IMPURE_SILICON, CShape.INGOT, 5, false, "impuresiliconingot");
        regMat("impure_silicon_plate", CMaterials.IMPURE_SILICON, CShape.PLATE, 5, false, "impuresiliconplate");
        regMat("impure_silicon_large_plate", CMaterials.IMPURE_SILICON, CShape.LARGE_PLATE, 5, false, "impuresiliconlargeplate");
        regMat("silicone_dust", CMaterials.SILICONE, CShape.DUST, 5, true, "dust");
        regMat("silicone_ingot", CMaterials.SILICONE, CShape.INGOT, 5, true, "ingot");
        regMat("silicone_plate", CMaterials.SILICONE, CShape.PLATE, 5, true, "plate");
        regMat("silicone_large_plate", CMaterials.SILICONE, CShape.LARGE_PLATE, 5, true, "largeplate");
        regMat("silicon_dust", CMaterials.SILICON, CShape.DUST, 5, true, "dust");
        regMat("silicon_ingot", CMaterials.SILICON, CShape.INGOT, 5, true, "ingot");
        regMat("silicon_plate", CMaterials.SILICON, CShape.PLATE, 5, true, "plate");
        regMat("silicon_large_plate", CMaterials.SILICON, CShape.LARGE_PLATE, 5, true, "largeplate");
        regMat("aluminium_dust", CMaterials.ALUMINIUM, CShape.DUST, 6, true, "dust");
        regMat("aluminium_ingot", CMaterials.ALUMINIUM, CShape.INGOT, 6, true, "ingot");
        regMat("aluminium_plate", CMaterials.ALUMINIUM, CShape.PLATE, 6, true, "plate");
        regMat("aluminium_large_plate", CMaterials.ALUMINIUM, CShape.LARGE_PLATE, 6, true, "largeplate");
        regMat("impure_aluminium_ingot", CMaterials.IMPURE_ALUMINIUM, CShape.INGOT, 6, true, "ingot");
        regMat("impure_aluminium_plate", CMaterials.IMPURE_ALUMINIUM, CShape.PLATE, 6, true, "plate");
        regMat("impure_aluminium_large_plate", CMaterials.IMPURE_ALUMINIUM, CShape.LARGE_PLATE, 6, true, "largeplate");
        regMat("clay_steel_dust", CMaterials.CLAY_STEEL, CShape.DUST, 7, true, "dust");
        regMat("clay_steel_ingot", CMaterials.CLAY_STEEL, CShape.INGOT, 7, true, "ingot");
        regMat("clay_steel_plate", CMaterials.CLAY_STEEL, CShape.PLATE, 7, true, "plate");
        regMat("clay_steel_large_plate", CMaterials.CLAY_STEEL, CShape.LARGE_PLATE, 7, true, "largeplate");
        regMat("clayium_dust", CMaterials.CLAYIUM, CShape.DUST, 8, true, "dust");
        regMat("clayium_ingot", CMaterials.CLAYIUM, CShape.INGOT, 8, true, "ingot");
        regMat("clayium_plate", CMaterials.CLAYIUM, CShape.PLATE, 8, true, "plate");
        regMat("clayium_large_plate", CMaterials.CLAYIUM, CShape.LARGE_PLATE, 8, true, "largeplate");
        regMat("ultimate_alloy_dust", CMaterials.ULTIMATE_ALLOY, CShape.DUST, 9, true, "dust");
        regMat("ultimate_alloy_ingot", CMaterials.ULTIMATE_ALLOY, CShape.INGOT, 9, true, "ingot");
        regMat("ultimate_alloy_plate", CMaterials.ULTIMATE_ALLOY, CShape.PLATE, 9, true, "plate");
        regMat("ultimate_alloy_large_plate", CMaterials.ULTIMATE_ALLOY, CShape.LARGE_PLATE, 9, true, "largeplate");
        regMat("antimatter_dust", CMaterials.ANTIMATTER, CShape.DUST, 10, true, "dust");
        regMat("antimatter", CMaterials.ANTIMATTER, CShape.GEM, 10, true, "matter");
        regMat("antimatter_plate", CMaterials.ANTIMATTER, CShape.PLATE, 10, true, "plate");
        regMat("antimatter_large_plate", CMaterials.ANTIMATTER, CShape.LARGE_PLATE, 10, true, "largeplate");
        regMat("pure_antimatter_dust", CMaterials.PURE_ANTIMATTER, CShape.DUST, 11, true, "dust");
        regMat("pure_antimatter", CMaterials.PURE_ANTIMATTER, CShape.GEM, 11, true, "matter");
        regMat("pure_antimatter_plate", CMaterials.PURE_ANTIMATTER, CShape.PLATE, 11, true, "plate");
        regMat("pure_antimatter_large_plate", CMaterials.PURE_ANTIMATTER, CShape.LARGE_PLATE, 11, true, "largeplate");
        regMat("pure_antimatter_1", CMaterials.COMPRESSED_PURE_ANTIMATTER_1, CShape.GEM, 11, true, "matter");
        regMat("pure_antimatter_2", CMaterials.COMPRESSED_PURE_ANTIMATTER_2, CShape.GEM, 11, true, "matter2");
        regMat("pure_antimatter_3", CMaterials.COMPRESSED_PURE_ANTIMATTER_3, CShape.GEM, 11, true, "matter2");
        regMat("pure_antimatter_4", CMaterials.COMPRESSED_PURE_ANTIMATTER_4, CShape.GEM, 12, true, "matter3");
        regMat("pure_antimatter_5", CMaterials.COMPRESSED_PURE_ANTIMATTER_5, CShape.GEM, 12, true, "matter3");
        regMat("pure_antimatter_6", CMaterials.COMPRESSED_PURE_ANTIMATTER_6, CShape.GEM, 12, true, "matter4");
        regMat("pure_antimatter_7", CMaterials.COMPRESSED_PURE_ANTIMATTER_7, CShape.GEM, 12, true, "matter4");
        regMat("octuple_energetic_clay_dust", CMaterials.OCTUPLE_CLAY, CShape.DUST, 12, true, "dust");
        regMat("octuple_energetic_clay_plate", CMaterials.OCTUPLE_CLAY, CShape.PLATE, 12, true, "plate");
        regMat("octuple_energetic_clay_large_plate", CMaterials.OCTUPLE_CLAY, CShape.LARGE_PLATE, 12, true, "largeplate");
        regMat("octuple_pure_antimatter_dust", CMaterials.COMPRESSED_PURE_ANTIMATTER_8, CShape.DUST, 13, true, "dust");
        regMat("octuple_pure_antimatter", CMaterials.COMPRESSED_PURE_ANTIMATTER_8, CShape.GEM, 13, true, "matter5");
        regMat("octuple_pure_antimatter_plate", CMaterials.COMPRESSED_PURE_ANTIMATTER_8, CShape.PLATE, 13, true, "plate");
        regMat("octuple_pure_antimatter_large_plate", CMaterials.COMPRESSED_PURE_ANTIMATTER_8, CShape.LARGE_PLATE, 13, true, "largeplate");
        regMat("organic_clay_dust", CMaterials.ORG_CLAY, CShape.DUST, null, true, "dust");
        regMat("impure_redstone_dust", CMaterials.IMPURE_REDSTONE, CShape.DUST, null, true, "dust");
        regMat("impure_glowstone_dust", CMaterials.IMPURE_GLOWSTONE, CShape.DUST, null, true, "dust");
        regMat("impure_ultimate_alloy_ingot", CMaterials.IMPURE_ULTIMATE_ALLOY, CShape.INGOT, 8, true, "ingot");
        regMat("magnesium_dust", CMaterials.MAGNESIUM, CShape.DUST, null, true, "dust");
        regMat("magnesium_ingot", CMaterials.MAGNESIUM, CShape.INGOT, null, true, "ingot");
        regMat("sodium_dust", CMaterials.SODIUM, CShape.DUST, null, true, "dust");
        regMat("sodium_ingot", CMaterials.SODIUM, CShape.INGOT, null, true, "ingot");
        regMat("lithium_dust", CMaterials.LITHIUM, CShape.DUST, null, true, "dust");
        regMat("lithium_ingot", CMaterials.LITHIUM, CShape.INGOT, null, true, "ingot");
        regMat("zirconium_dust", CMaterials.ZIRCONIUM, CShape.DUST, null, true, "dust");
        regMat("zirconium_ingot", CMaterials.ZIRCONIUM, CShape.INGOT, null, true, "ingot");
        regMat("zinc_dust", CMaterials.ZINC, CShape.DUST, null, true, "dust");
        regMat("zinc_ingot", CMaterials.ZINC, CShape.INGOT, null, true, "ingot");
        regMat("manganese_dust", CMaterials.MANGANESE, CShape.DUST, null, true, "dust");
        regMat("manganese_ingot", CMaterials.MANGANESE, CShape.INGOT, null, true, "ingot");
        regMat("calcium_dust", CMaterials.CALCIUM, CShape.DUST, null, true, "dust");
        regMat("calcium_ingot", CMaterials.CALCIUM, CShape.INGOT, null, true, "ingot");
        regMat("potassium_dust", CMaterials.POTASSIUM, CShape.DUST, null, true, "dust");
        regMat("potassium_ingot", CMaterials.POTASSIUM, CShape.INGOT, null, true, "ingot");
        regMat("nickel_dust", CMaterials.NICKEL, CShape.DUST, null, true, "dust");
        regMat("nickel_ingot", CMaterials.NICKEL, CShape.INGOT, null, true, "ingot");
        regMat("beryllium_dust", CMaterials.BERYLLIUM, CShape.DUST, null, true, "dust");
        regMat("beryllium_ingot", CMaterials.BERYLLIUM, CShape.INGOT, null, true, "ingot");
        regMat("lead_dust", CMaterials.LEAD, CShape.DUST, null, true, "dust");
        regMat("lead_ingot", CMaterials.LEAD, CShape.INGOT, null, true, "ingot");
        regMat("hafnium_dust", CMaterials.HAFNIUM, CShape.DUST, null, true, "dust");
        regMat("hafnium_ingot", CMaterials.HAFNIUM, CShape.INGOT, null, true, "ingot");
        regMat("chrome_dust", CMaterials.CHROME, CShape.DUST, null, true, "dust");
        regMat("chrome_ingot", CMaterials.CHROME, CShape.INGOT, null, true, "ingot");
        regMat("titanium_dust", CMaterials.TITANIUM, CShape.DUST, null, true, "dust");
        regMat("titanium_ingot", CMaterials.TITANIUM, CShape.INGOT, null, true, "ingot");
        regMat("strontium_dust", CMaterials.STRONTIUM, CShape.DUST, null, true, "dust");
        regMat("strontium_ingot", CMaterials.STRONTIUM, CShape.INGOT, null, true, "ingot");
        regMat("barium_dust", CMaterials.BARIUM, CShape.DUST, null, true, "dust");
        regMat("barium_ingot", CMaterials.BARIUM, CShape.INGOT, null, true, "ingot");
        regMat("copper_dust", CMaterials.COPPER, CShape.DUST, null, true, "dust");
        regMat("copper_ingot", CMaterials.COPPER, CShape.INGOT, null, true, "ingot");
        regMat("iron_dust", CMaterials.IRON, CShape.DUST, null, true, "dust");
        regMat("gold_dust", CMaterials.GOLD, CShape.DUST, null, true, "dust");
        regMat("bronze_dust", CMaterials.BRONZE, CShape.DUST, null, true, "dust");
        regMat("bronze_ingot", CMaterials.BRONZE, CShape.INGOT, null, true, "ingot");
        regMat("brass_dust", CMaterials.BRASS, CShape.DUST, null, true, "dust");
        regMat("brass_ingot", CMaterials.BRASS, CShape.INGOT, null, true, "ingot");
        regMat("electrum_dust", CMaterials.ELECTRUM, CShape.DUST, null, true, "dust");
        regMat("electrum_ingot", CMaterials.ELECTRUM, CShape.INGOT, null, true, "ingot");
        regMat("invar_dust", CMaterials.INVAR, CShape.DUST, null, true, "dust");
        regMat("invar_ingot", CMaterials.INVAR, CShape.INGOT, null, true, "ingot");
        regMat("steel_dust", CMaterials.STEEL, CShape.DUST, null, true, "dust");
        regMat("steel_ingot", CMaterials.STEEL, CShape.INGOT, null, true, "ingot");
        regMat("impure_aluminium_dust", CMaterials.IMPURE_ALUMINIUM, CShape.DUST, null, true, "dust");
        regMat("impure_magnesium_dust", CMaterials.IMPURE_MAGNESIUM, CShape.DUST, null, true, "dust");
        regMat("impure_sodium_dust", CMaterials.IMPURE_SODIUM, CShape.DUST, null, true, "dust");
        regMat("impure_lithium_dust", CMaterials.IMPURE_LITHIUM, CShape.DUST, null, true, "dust");
        regMat("impure_zirconium_dust", CMaterials.IMPURE_ZIRCONIUM, CShape.DUST, null, true, "dust");
        regMat("impure_zinc_dust", CMaterials.IMPURE_ZINC, CShape.DUST, null, true, "dust");
        regMat("impure_manganese_dust", CMaterials.IMPURE_MANGANESE, CShape.DUST, null, true, "dust");
        regMat("impure_calcium_dust", CMaterials.IMPURE_CALCIUM, CShape.DUST, null, true, "dust");
        regMat("impure_potassium_dust", CMaterials.IMPURE_POTASSIUM, CShape.DUST, null, true, "dust");
        regMat("impure_nickel_dust", CMaterials.IMPURE_NICKEL, CShape.DUST, null, true, "dust");
        regMat("impure_iron_dust", CMaterials.IMPURE_IRON, CShape.DUST, null, true, "dust");
        regMat("impure_beryllium_dust", CMaterials.IMPURE_BERYLLIUM, CShape.DUST, null, true, "dust");
        regMat("impure_lead_dust", CMaterials.IMPURE_LEAD, CShape.DUST, null, true, "dust");
        regMat("impure_hafnium_dust", CMaterials.IMPURE_HAFNIUM, CShape.DUST, null, true, "dust");
        regMat("impure_chrome_dust", CMaterials.IMPURE_CHROME, CShape.DUST, null, true, "dust");
        regMat("impure_titanium_dust", CMaterials.IMPURE_TITANIUM, CShape.DUST, null, true, "dust");
        regMat("impure_strontium_dust", CMaterials.IMPURE_STRONTIUM, CShape.DUST, null, true, "dust");
        regMat("impure_barium_dust", CMaterials.IMPURE_BARIUM, CShape.DUST, null, true, "dust");
        regMat("impure_copper_dust", CMaterials.IMPURE_COPPER, CShape.DUST, null, true, "dust");
        regMat("zincalminium_dust", CMaterials.ZINCALMINIUM_ALLOY, CShape.DUST, 6, true, "dust");
        regMat("zincalminium_ingot", CMaterials.ZINCALMINIUM_ALLOY, CShape.INGOT, 6, true, "ingot");
        regMat("zinconium_dust", CMaterials.ZINCONIUM_ALLOY, CShape.DUST, 6, true, "dust");
        regMat("zinconium_ingot", CMaterials.ZINCONIUM_ALLOY, CShape.INGOT, 6, true, "ingot");
        regMat("az91d_dust", CMaterials.AZ91D_ALLOY, CShape.DUST, 6, true, "dust");
        regMat("az91d_ingot", CMaterials.AZ91D_ALLOY, CShape.INGOT, 6, true, "ingot");
        regMat("az91d_plate", CMaterials.AZ91D_ALLOY, CShape.PLATE, 6, true, "plate");
        regMat("az91d_large_plate", CMaterials.AZ91D_ALLOY, CShape.LARGE_PLATE, 6, true, "largeplate");
        regMat("zk60a_dust", CMaterials.ZK60A_ALLOY, CShape.DUST, 6, true, "dust");
        regMat("zk60a_ingot", CMaterials.ZK60A_ALLOY, CShape.INGOT, 6, true, "ingot");
        regMat("zk60a_plate", CMaterials.ZK60A_ALLOY, CShape.PLATE, 6, true, "plate");
        regMat("zk60a_large_plate", CMaterials.ZK60A_ALLOY, CShape.LARGE_PLATE, 6, true, "largeplate");
        regMat("carbon_dust", CMaterials.CARBON, CShape.DUST, null, true, "dust");
        regMat("phosphorus_dust", CMaterials.PHOSPHORUS, CShape.DUST, null, true, "dust");
        regMat("sulfur_dust", CMaterials.SULFUR, CShape.DUST, null, true, "dust");
        regMat("lapis_dust", CMaterials.LAPIS, CShape.DUST, null, true, "dust");
        regMat("coal_dust", CMaterials.COAL, CShape.DUST, null, true, "dust");
        regMat("charcoal_dust", CMaterials.CHARCOAL, CShape.DUST, null, true, "dust");
        regMat("saltpeter_dust", CMaterials.SALTPETER, CShape.DUST, null, true, "dust");
        regMat("rubidium_ingot", CMaterials.RUBIDIUM, CShape.INGOT, null, true, "ingot");
        regMat("caesium_ingot", CMaterials.CAESIUM, CShape.INGOT, null, true, "ingot");
        regMat("francium_ingot", CMaterials.FRANCIUM, CShape.INGOT, null, true, "ingot");
        regMat("radium_ingot", CMaterials.RADIUM, CShape.INGOT, null, true, "ingot");
        regMat("actinium_ingot", CMaterials.ACTINIUM, CShape.INGOT, null, true, "ingot");
        regMat("thorium_ingot", CMaterials.THORIUM, CShape.INGOT, null, true, "ingot");
        regMat("protactinium_ingot", CMaterials.PROTACTINIUM, CShape.INGOT, null, true, "ingot");
        regMat("uranium_ingot", CMaterials.URANIUM, CShape.INGOT, null, true, "ingot");
        regMat("neptunium_ingot", CMaterials.NEPTUNIUM, CShape.INGOT, null, true, "ingot");
        regMat("plutonium_ingot", CMaterials.PLUTONIUM, CShape.INGOT, null, true, "ingot");
        regMat("americium_ingot", CMaterials.AMERICIUM, CShape.INGOT, null, true, "ingot");
        regMat("curium_ingot", CMaterials.CURIUM, CShape.INGOT, null, true, "ingot");
        regMat("lanthanum_ingot", CMaterials.LANTHANUM, CShape.INGOT, null, true, "ingot");
        regMat("cerium_ingot", CMaterials.CERIUM, CShape.INGOT, null, true, "ingot");
        regMat("praseodymium_ingot", CMaterials.PRASEODYMIUM, CShape.INGOT, null, true, "ingot");
        regMat("neodymium_ingot", CMaterials.NEODYMIUM, CShape.INGOT, null, true, "ingot");
        regMat("promethium_ingot", CMaterials.PROMETHIUM, CShape.INGOT, null, true, "ingot");
        regMat("samarium_ingot", CMaterials.SAMARIUM, CShape.INGOT, null, true, "ingot");
        regMat("europium_ingot", CMaterials.EUROPIUM, CShape.INGOT, null, true, "ingot");
        regMat("vanadium_ingot", CMaterials.VANADIUM, CShape.INGOT, null, true, "ingot");
        regMat("cobalt_ingot", CMaterials.COBALT, CShape.INGOT, null, true, "ingot");
        regMat("palladium_ingot", CMaterials.PALLADIUM, CShape.INGOT, null, true, "ingot");
        regMat("silver_ingot", CMaterials.SILVER, CShape.INGOT, null, true, "ingot");
        regMat("platinum_ingot", CMaterials.PLATINUM, CShape.INGOT, null, true, "ingot");
        regMat("iridium_ingot", CMaterials.IRIDIUM, CShape.INGOT, null, true, "ingot");
        regMat("osmium_ingot", CMaterials.OSMIUM, CShape.INGOT, null, true, "ingot");
        regMat("rhenium_ingot", CMaterials.RHENIUM, CShape.INGOT, null, true, "ingot");
        regMat("tantalum_ingot", CMaterials.TANTALUM, CShape.INGOT, null, true, "ingot");
        regMat("tungsten_ingot", CMaterials.TUNGSTEN, CShape.INGOT, null, true, "ingot");
        regMat("molybdenum_ingot", CMaterials.MOLYBDENUM, CShape.INGOT, null, true, "ingot");
        regMat("tin_ingot", CMaterials.TIN, CShape.INGOT, null, true, "ingot");
        regMat("antimony_ingot", CMaterials.ANTIMONY, CShape.INGOT, null, true, "ingot");
        regMat("bismuth_ingot", CMaterials.BISMUTH, CShape.INGOT, null, true, "ingot");
        regMat("impure_osmium_ingot", CMaterials.IMPURE_OSMIUM, CShape.INGOT, null, true, "ingot");
    }
}
