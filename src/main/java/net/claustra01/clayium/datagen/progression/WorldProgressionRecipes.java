package net.claustra01.clayium.datagen.progression;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.LaserRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class WorldProgressionRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (var wheel : WaterWheelRegistration.DEFINITIONS) {
            ItemLike hull = wheel.tier() == 1
                    ? WorkbenchRegistration.CLAY_MACHINE_HULL.get()
                    : AssemblyMachineRegistration.MACHINE_HULLS.get(2).get();
            ItemLike part = material(wheel.tier() == 1 ? "clay_wheel" : "dense_clay_wheel");
            ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, wheel.block().get())
                    .requires(hull).requires(part)
                    .unlockedBy("has_wheel", InventoryChangeTrigger.TriggerInstance.hasItems(part))
                    .save(output, ClayiumMod.id("progression/power/" + wheel.id()));
        }

        for (var laser : LaserRegistration.LASERS.values()) {
            int tier = laser.specification().tier();
            machine(output, "laser/" + laser.specification().id(), laser.block().get(),
                    ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, tier), 480, 1,
                    AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get(), 1,
                    ProgressionRecipeSupport.component("laser_parts"), 4);
        }
        machine(output, "laser/laser_reflector", LaserRegistration.LASER_REFLECTOR.get(),
                ClayiumRecipeMethod.BLAST_FURNACE, 7, ClayiumRecipeEnergy.e(0.2D, 7), 100, 1,
                material("industrial_clay_dust"), 1, material("quartz_dust"), 8);

        machine(output, "environment/quartz_crucible", EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get(),
                ClayiumRecipeMethod.ASSEMBLER, 0, 1000, 20, 1,
                material("quartz_dust"), 16, null, 0);
        for (var generator : EnvironmentMachineRegistration.COBBLESTONE_GENERATORS) {
            int tier = generator.tier();
            if (tier <= 3) {
                ItemLike hull = tier == 1 ? WorkbenchRegistration.CLAY_MACHINE_HULL.get()
                        : AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get();
                String material = tier == 1 ? "clay" : "dense_clay";
                ItemLike gear = material(material + "_gear");
                ItemLike pipe = material(material + "_pipe");
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, generator.block().get())
                        .define('#', hull).define('*', gear).define('=', pipe)
                        .pattern(" * ").pattern("=#=").pattern(" * ")
                        .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                        .save(output, ClayiumMod.id("progression/environment/" + generator.id()));
            } else {
                machine(output, "environment/" + generator.id(), generator.block().get(),
                        ClayiumRecipeMethod.ASSEMBLER, 4, ClayiumRecipeEnergy.e(1.0D, tier), 40, 1,
                        item("buffer_tier_" + tier), 1,
                        ProgressionRecipeSupport.component("simple_circuit"), 1);
            }
        }
        for (var extractor : EnvironmentMachineRegistration.SALT_EXTRACTORS.values()) {
            int tier = extractor.tier();
            machine(output, "environment/" + extractor.id(), extractor.block().get(),
                    ClayiumRecipeMethod.ASSEMBLER, 4, ClayiumRecipeEnergy.e(1.0D, tier), 40, 1,
                    item("buffer_tier_" + tier), 1,
                    ProgressionRecipeSupport.component("basic_circuit"), 1);
        }

        machine(output, "world/clay_marker", item("clay_marker"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 7), 480, 1,
                Items.CLAY, 1, ProgressionRecipeSupport.component("integrated_circuit"), 1);
        machine(output, "world/clay_marker_open_pit", item("clay_marker_open_pit"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 8), 480, 1,
                item("dense_clay"), 1, ProgressionRecipeSupport.component("clay_core"), 1);
        machine(output, "world/clay_marker_ground_leveling", item("clay_marker_ground_leveling"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 8), 480, 1,
                item("compressed_clay"), 1, ProgressionRecipeSupport.component("clay_core"), 1);
        machine(output, "world/clay_marker_prism", item("clay_marker_prism"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 8), 480, 1,
                item("clay_marker_open_pit"), 1, item("clay_marker_ground_leveling"), 1);

        machine(output, "world/area_item_collector", item("area_item_collector"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 7), 6000, 1,
                item("zk60_a_alloy_hull"), 1, ProgressionRecipeSupport.component("integrated_circuit"), 64);
        machine(output, "world/block_breaker", item("block_breaker"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 6), 60, 1,
                item("az91_d_alloy_hull"), 1, ProgressionRecipeSupport.component("precision_circuit"), 1);
        machine(output, "world/area_miner", item("area_miner"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 8), 6000, 1,
                item("zk60_a_alloy_hull"), 1, ProgressionRecipeSupport.component("clay_core"), 64);
        machine(output, "world/advanced_area_miner", item("advanced_area_miner"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 9), 6000, 1,
                item("area_miner"), 1, ProgressionRecipeSupport.component("clay_brain"), 64);
        machine(output, "world/area_replacer", item("area_replacer"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 10), 6000, 1,
                item("advanced_area_miner"), 1, ProgressionRecipeSupport.component("clay_spirit"), 64);
        machine(output, "world/activator", item("activator"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 6), 60, 1,
                item("az91_d_alloy_hull"), 1, ProgressionRecipeSupport.component("advanced_circuit"), 1);
        machine(output, "world/area_activator", item("area_activator"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 8), 6000, 1,
                item("area_item_collector"), 1, ProgressionRecipeSupport.component("clay_core"), 1);
        machine(output, "world/auto_trader", item("auto_trader"),
                ClayiumRecipeMethod.ASSEMBLER, 6, ClayiumRecipeEnergy.e(1.0D, 8), 120, 1,
                item("az91_d_alloy_hull"), 1, ProgressionRecipeSupport.component("clay_core"), 4);

        ItemLike hull = AssemblyMachineRegistration.MACHINE_HULL_DEFINITIONS.get("zk60_a_alloy_hull").block().get();
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ChunkLoaderRegistration.CHUNK_LOADER.get())
                .define('#', hull)
                .define('C', ProgressionRecipeSupport.component("precision_circuit"))
                .define('P', ProgressionRecipeSupport.component("advanced_circuit"))
                .pattern("PCP").pattern("C#C").pattern("PCP")
                .unlockedBy("has_hull", InventoryChangeTrigger.TriggerInstance.hasItems(hull))
                .save(output, ClayiumMod.id("progression/world/" + ChunkLoaderRegistration.ID));

        machine(output, "storage/storage_container", StorageRegistration.STORAGE_CONTAINER.get(),
                ClayiumRecipeMethod.ASSEMBLER, 4, ClayiumRecipeEnergy.e(1.0D, 6), 120, 4,
                item("az91_d_alloy_hull"), 4, item("clay_interface_tier_5"), 1);
        machine(output, "storage/void_container", StorageRegistration.VOID_CONTAINER.get(),
                ClayiumRecipeMethod.ASSEMBLER, 4, ClayiumRecipeEnergy.e(1.0D, 6), 120, 4,
                item("az91_d_alloy_hull"), 4, item("redstone_interface_tier_5"), 1);
    }

    private static void machine(RecipeOutput output, String path, ItemLike result,
            ClayiumRecipeMethod method, int tier, long energy, long ticks, int resultCount,
            ItemLike first, int firstCount, ItemLike second, int secondCount) {
        ClayiumRecipeBuilder recipe = ClayiumRecipeBuilder.machine(method, tier)
                .requires(first, firstCount).output(result, resultCount)
                .clayEnergy(energy).processingTicks(ticks);
        if (second != null) {
            recipe.requires(second, secondCount);
        }
        recipe.save(output, ClayiumMod.id("progression/" + path));
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.get(ClayiumMod.id(id));
    }

    private WorldProgressionRecipes() {
    }
}
