package net.claustra01.clayium.datagen.progression;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class CaPanProgressionRecipes {
    private static final String[] COMPRESSED_PURE_ANTIMATTER = {
            "pure_antimatter", "compressed_pure_antimatter", "double_compressed_pure_antimatter",
            "triple_compressed_pure_antimatter", "quadruple_compressed_pure_antimatter",
            "quintuple_compressed_pure_antimatter", "sextuple_compressed_pure_antimatter",
            "septuple_compressed_pure_antimatter", "octuple_compressed_pure_antimatter"};

    public static void buildRecipes(RecipeOutput output) {
        for (String material : new String[]{"antimatter", "pure_antimatter"}) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, item(material + "_block"))
                    .define('#', material(material))
                    .pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_gem", InventoryChangeTrigger.TriggerInstance.hasItems(
                            material(material)))
                    .save(output, ClayiumMod.id("progression/material/" + material + "_block"));
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, item("opa_block"))
                .define('#', material("octuple_compressed_pure_antimatter"))
                .pattern("###").pattern("###").pattern("###")
                .unlockedBy("has_octuple_antimatter", InventoryChangeTrigger.TriggerInstance.hasItems(
                        material("octuple_compressed_pure_antimatter")))
                .save(output, ClayiumMod.id("progression/material/opa_block"));
        for (int index = 0; index < COMPRESSED_PURE_ANTIMATTER.length - 1; index++) {
            String lower = COMPRESSED_PURE_ANTIMATTER[index];
            String upper = COMPRESSED_PURE_ANTIMATTER[index + 1];
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CONDENSER, 10)
                    .requires(material(lower), 9).output(material(upper), 1)
                    .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 9)).processingTicks(6)
                    .save(output, ClayiumMod.id("progression/material/condense_" + upper));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, material(lower), 9)
                    .requires(material(upper))
                    .unlockedBy("has_compressed_antimatter", InventoryChangeTrigger.TriggerInstance.hasItems(
                            material(upper)))
                    .save(output, ClayiumMod.id("progression/material/decompress_" + upper));
        }
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 9)
                .requires(material("clayium_ingot"), 1)
                .output(component("antimatter_seed"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 9))
                .processingTicks(200_000_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/antimatter_seed"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,
                        material("ultimate_compound_ingot"), 9)
                .requires(Ingredient.of(ingot("barium")))
                .requires(Ingredient.of(ingot("strontium")))
                .requires(Ingredient.of(ingot("calcium")))
                .requires(Ingredient.of(ingot("clayium")))
                .requires(Ingredient.of(ingot("aluminium")), 5)
                .unlockedBy("has_clayium", InventoryChangeTrigger.TriggerInstance.hasItems(
                        material("clayium_ingot")))
                .save(output, ClayiumMod.id("progression/material/impure_ultimate_alloy_ingot"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 8)
                .requires(material("advanced_industrial_clay_dust"), 1)
                .requires(material("ultimate_compound_ingot"), 1)
                .output(material("ultimate_alloy_ingot"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(10.0D, 8)).processingTicks(1_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/ultimate_alloy_ingot"));
        TagKey<Item> lithiumDust = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "dusts/lithium"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 7)
                .requires(material("energized_clay_dust"), 8)
                .requires(Ingredient.of(lithiumDust), 1)
                .output(material("excited_clay_dust"), 4)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 7)).processingTicks(2_000_000)
                .save(output, ClayiumMod.id("progression/reactor/excited_clay_dust"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 8)
                .requires(component("clay_core"), 6)
                .requires(material("excited_clay_dust"), 12)
                .output(component("clay_brain"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(10.0D, 8)).processingTicks(4_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/clay_brain"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 9)
                .requires(component("clay_brain"), 6)
                .requires(material("excited_clay_dust"), 32)
                .output(component("clay_spirit"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(10.0D, 9)).processingTicks(10_000_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/clay_spirit"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 10)
                .requires(component("clay_spirit"), 6)
                .requires(material("antimatter"), 4)
                .output(component("clay_soul"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(10.0D, 10)).processingTicks(10_000_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/clay_soul"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 11)
                .requires(component("clay_soul"), 6)
                .requires(material("antimatter"), 16)
                .output(component("clay_anima"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(30.0D, 11)).processingTicks(100_000_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/clay_anima"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 12)
                .requires(component("clay_anima"), 6)
                .requires(material("antimatter"), 64)
                .output(component("clay_psyche"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(90.0D, 12)).processingTicks(1_000_000_000_000_000L)
                .save(output, ClayiumMod.id("progression/reactor/clay_psyche"));
        String[] metals = {"lithium", "hafnium", "barium", "strontium"};
        int[] ticks = {50_000, 500_000, 5_000_000, 50_000_000};
        double[] energy = {10.0D, 10.0D, 3.0D, 1.0D};
        int[] amounts = {4, 1, 1, 1};
        for (int index = 0; index < metals.length; index++) {
            TagKey<Item> metalDust = TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "dusts/" + metals[index]));
            ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 7)
                    .requires(material("advanced_industrial_clay_dust"), 8)
                    .requires(Ingredient.of(metalDust), amounts[index])
                    .output(material("clayium_dust"), 8)
                    .clayEnergy(ClayiumRecipeEnergy.e(energy[index], 7))
                    .processingTicks(ticks[index])
                    .save(output, ClayiumMod.id("progression/reactor/clayium_dust_" + metals[index]));
        }
        for (var definition : CaRegistration.MACHINES.values()) {
            int tier = definition.tier();
            ItemLike hull = AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get();
            ItemLike second;
            int count;
            int assemblerTier;
            if (definition.id().startsWith("ca_condenser")) {
                second = item("transformer_tier_" + tier);
                count = 16;
                assemblerTier = tier == 9 ? 6 : 10;
            } else {
                second = tier == 9 ? item("clay_reactor") : item("ca_injector_tier_" + (tier - 1));
                count = tier == 9 ? 16 : 2;
                assemblerTier = tier == 9 ? 6 : 10;
            }
            machine(output, "ca/" + definition.id(), definition.block().get(),
                    ClayiumRecipeMethod.ASSEMBLER, assemblerTier,
                    ClayiumRecipeEnergy.e(1.0D, tier), 480,
                    hull, 1, second, count, 1);
        }

        String[] coilPlates = {"antimatter_plate", "pure_antimatter_plate",
                "octuple_energetic_clay_plate", "octuple_pure_antimatter_plate"};
        String[] coilIngots = {"platinum_ingot", "iridium_ingot", "osmium_ingot", "rhenium_ingot"};
        int[] coilCounts = {1, 4, 16, 64};
        long[] coilTicks = {10_000_000_000_000L, 100_000_000_000_000L,
                1_000_000_000_000_000L, 1_000_000_000_000_000L};
        for (int tier = 10; tier <= 13; tier++) {
            var core = CaReactorRegistration.CORES.get(tier);
            var coil = CaReactorRegistration.COILS.get(tier);
            machine(output, "ca_reactor/" + core.id(), core.block().get(),
                    ClayiumRecipeMethod.ASSEMBLER, 10, ClayiumRecipeEnergy.e(1.0D, tier), 120,
                    AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get(), 1,
                    item("clay_reactor"), 16, 1);
            machine(output, "ca_reactor/" + coil.id(), coil.block().get(),
                    ClayiumRecipeMethod.REACTOR, tier, ClayiumRecipeEnergy.e(1.0D, tier),
                    coilTicks[tier - 10], material(coilPlates[tier - 10]), 6,
                    material(coilIngots[tier - 10]), coilCounts[tier - 10], 1);
        }
        String[] hullIngots = {"rubidium_ingot", "cerium_ingot", "tantalum_ingot",
                "praseodymium_ingot", "protactinium_ingot", "neptunium_ingot",
                "promethium_ingot", "samarium_ingot", "curium_ingot", "europium_ingot"};
        for (int rank = 1; rank <= 10; rank++) {
            ItemLike result = CaReactorRegistration.HULLS.get(rank).block().get();
            ItemLike center = rank == 1
                    ? AssemblyMachineRegistration.MACHINE_HULLS.get(10).get()
                    : CaReactorRegistration.HULLS.get(rank - 1).block().get();
            ItemLike gem = material(rank == 1 ? "antimatter" : COMPRESSED_PURE_ANTIMATTER[rank - 2]);
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result)
                    .define('#', gem).define('I', material(hullIngots[rank - 1])).define('H', center)
                    .pattern("#I#").pattern("#H#").pattern("###")
                    .unlockedBy("has_gem", InventoryChangeTrigger.TriggerInstance.hasItems(gem))
                    .save(output, ClayiumMod.id("progression/ca_reactor/ca_reactor_hull_rank_" + rank));
        }

        machine(output, "pan/pan_cable", item("pan_cable"),
                ClayiumRecipeMethod.ASSEMBLER, 10, ClayiumRecipeEnergy.e(1.0D, 10), 2,
                material("pure_antimatter"), 3, Items.GLASS, 2, 12);
        machine(output, "pan/pan_core", item("pan_core"),
                ClayiumRecipeMethod.REACTOR, 11, ClayiumRecipeEnergy.e(1.0D, 11),
                100_000_000_000_000L, item("pan_adapter_tier_10"), 4,
                ProgressionRecipeSupport.component("clay_soul"), 1, 1);
        String[] resonators = {"antimatter_resonator", "pure_antimatter_resonator",
                "oec_resonator", "opa_resonator"};
        for (int tier = 10; tier <= 13; tier++) {
            var adapter = PanRegistration.DEFINITIONS.get("pan_adapter_tier_" + tier);
            machine(output, "pan/" + adapter.id(), adapter.block().get(),
                    ClayiumRecipeMethod.ASSEMBLER, 10, ClayiumRecipeEnergy.e(1.0D, tier), 60,
                    item(resonators[tier - 10]), 1, item("pan_cable"), 6, 1);
        }
        machine(output, "pan/pan_duplicator_tier_4", item("pan_duplicator_tier_4"),
                ClayiumRecipeMethod.ASSEMBLER, 10, ClayiumRecipeEnergy.e(1.0D, 10), 20,
                AssemblyMachineRegistration.MACHINE_HULLS.get(4).get(), 1,
                item("pan_cable"), 4, 1);
        String[] duplicatorIngots = {"rubidium_ingot", "lanthanum_ingot", "caesium_ingot",
                "francium_ingot", "radium_ingot", "tantalum_ingot", "bismuth_ingot",
                "actinium_ingot", "vanadium_ingot"};
        for (int tier = 5; tier <= 13; tier++) {
            ItemLike gem = material(COMPRESSED_PURE_ANTIMATTER[tier - 5]);
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS,
                            item("pan_duplicator_tier_" + tier))
                    .define('#', gem)
                    .define('I', material(duplicatorIngots[tier - 5]))
                    .define('D', item("pan_duplicator_tier_" + (tier - 1)))
                    .define('M', AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get())
                    .pattern("#I#").pattern("DMD").pattern("#I#")
                    .unlockedBy("has_duplicator", InventoryChangeTrigger.TriggerInstance.hasItems(
                            item("pan_duplicator_tier_" + (tier - 1))))
                    .save(output, ClayiumMod.id("progression/pan/pan_duplicator_tier_" + tier));
        }
    }

    private static void machine(RecipeOutput output, String path, ItemLike result,
            ClayiumRecipeMethod method, int tier, long energy, long ticks,
            ItemLike first, int firstCount, ItemLike second, int secondCount, int resultCount) {
        ClayiumRecipeBuilder.machine(method, tier)
                .requires(first, firstCount).requires(second, secondCount)
                .output(result, resultCount).clayEnergy(energy).processingTicks(ticks)
                .save(output, ClayiumMod.id("progression/" + path));
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.get(ClayiumMod.id(id));
    }

    private static ItemLike material(String id) {
        return ProgressionRecipeSupport.material(id);
    }

    private static ItemLike component(String id) {
        return ProgressionRecipeSupport.component(id);
    }

    private static TagKey<Item> ingot(String id) {
        return TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "ingots/" + id));
    }

    private CaPanProgressionRecipes() {
    }
}
