package net.claustra01.clayium.datagen.progression;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.gadget.GadgetType;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.NbtPreservingAutoEatRecipe;
import net.claustra01.clayium.registry.AdvancedToolRegistration;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class EquipmentProgressionRecipes {
    public static void buildRecipes(RecipeOutput output) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.REACTOR, 11)
                .requires(ProgressionRecipeSupport.material("pure_antimatter"), 8)
                .requires(ProgressionRecipeSupport.component("integrated_circuit"), 1)
                .output(ProgressionRecipeSupport.component("teleportation_parts"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 11))
                .processingTicks(10_000_000_000_000L)
                .save(output, ClayiumMod.id("progression/equipment/teleportation_parts"));
        shapedTool(output, "clay_pickaxe", AdvancedToolRegistration.CLAY_PICKAXE.get(),
                "###", " | ", " | ", ProgressionRecipeSupport.material("dense_clay_plate"),
                ProgressionRecipeSupport.material("dense_clay_stick"));
        shapedTool(output, "clay_shovel", AdvancedToolRegistration.CLAY_SHOVEL.get(),
                "#", "|", "|", ProgressionRecipeSupport.material("clay_plate"),
                ProgressionRecipeSupport.material("clay_stick"));
        shapedTool(output, "clay_steel_pickaxe", AdvancedToolRegistration.CLAY_STEEL_PICKAXE.get(),
                "###", " | ", " | ", ProgressionRecipeSupport.material("clay_steel_ingot"),
                ProgressionRecipeSupport.material("dense_clay_stick"));
        shapedTool(output, "clay_steel_shovel", AdvancedToolRegistration.CLAY_STEEL_SHOVEL.get(),
                "#", "|", "|", ProgressionRecipeSupport.material("clay_steel_ingot"),
                ProgressionRecipeSupport.material("dense_clay_stick"));

        for (int tier = 4; tier <= 7; tier++) {
            ItemLike result = WeaponRegistration.ALL.get(tier - 4).get();
            machine(output, WeaponRegistration.ALL.get(tier - 4).getId().getPath(),
                    result, 4, ClayiumRecipeEnergy.e(1.0D, tier), 40,
                    ProgressionRecipeSupport.material("az91_d_alloy_plate"), 4,
                    circuit(tier), 1);
        }
        machine(output, "instant_teleporter", WeaponRegistration.INSTANT_TELEPORTER.get(),
                10, ClayiumRecipeEnergy.e(1.0D, 11), 3000,
                WeaponRegistration.SNIPER_CLAY_SHOOTER_PRO.get(), 1,
                ProgressionRecipeSupport.component("teleportation_parts"), 1);

        machine(output, "gadget_holder", GadgetRegistration.GADGET_HOLDER.get(),
                4, ClayiumRecipeEnergy.e(1.0D, 6), 120,
                Items.LEATHER, 4, ProgressionRecipeSupport.material("az91_d_alloy_plate"), 8);
        machine(output, GadgetType.TEMPLATE.id(), gadget(GadgetType.TEMPLATE),
                4, ClayiumRecipeEnergy.e(1.0D, 6), 120,
                ProgressionRecipeSupport.material("industrial_clay_plate"), 8,
                ProgressionRecipeSupport.material("az91_d_alloy_plate"), 4);

        GadgetType[] overclocks = {GadgetType.OVERCLOCK_I, GadgetType.OVERCLOCK_II,
                GadgetType.OVERCLOCK_III, GadgetType.OVERCLOCK_IV};
        String[] augmentMaterials = {"antimatter", "pure_antimatter", "oec", "opa"};
        for (int index = 0; index < overclocks.length; index++) {
            machine(output, overclocks[index].id(), gadget(overclocks[index]), 10,
                    ClayiumRecipeEnergy.e(1.0D, 10 + index), 120,
                    gadget(index == 0 ? GadgetType.TEMPLATE : overclocks[index - 1]), 1,
                    CaRegistration.AUGMENTS.get(augmentMaterials[index] + "_overclocker").block().get(), 1);
        }
        machine(output, GadgetType.FLIGHT.id(), gadget(GadgetType.FLIGHT),
                10, ClayiumRecipeEnergy.e(1.0D, 12), 120,
                gadget(GadgetType.TEMPLATE), 1, circuit(12), 16);
        machine(output, GadgetType.FREE_FLIGHT.id(), gadget(GadgetType.FREE_FLIGHT),
                10, ClayiumRecipeEnergy.e(1.0D, 13), 1200,
                gadget(GadgetType.FLIGHT), 1, circuit(13), 16);
        machine(output, GadgetType.SUPERSONIC_FLIGHT.id(), gadget(GadgetType.SUPERSONIC_FLIGHT),
                10, ClayiumRecipeEnergy.e(1.0D, 14), 12000,
                gadget(GadgetType.FREE_FLIGHT), 1,
                CaRegistration.AUGMENTS.get("opa_overclocker").block().get(), 16);
        GadgetType[] health = {GadgetType.EXTRA_HEALTH_I, GadgetType.EXTRA_HEALTH_II,
                GadgetType.EXTRA_HEALTH_III};
        int[] healthCircuits = {6, 10, 12};
        for (int index = 0; index < health.length; index++) {
            machine(output, health[index].id(), gadget(health[index]), index == 2 ? 10 : 4,
                    ClayiumRecipeEnergy.e(1.0D, healthCircuits[index]), 120,
                    gadget(index == 0 ? GadgetType.TEMPLATE : health[index - 1]), 1,
                    circuit(healthCircuits[index]), 4);
        }
        machine(output, GadgetType.ECONOMICAL_AUTO_EAT.id(), gadget(GadgetType.ECONOMICAL_AUTO_EAT),
                4, ClayiumRecipeEnergy.e(1.0D, 7), 120,
                gadget(GadgetType.TEMPLATE), 1, circuit(7), 2);
        SpecialRecipeBuilder.special(NbtPreservingAutoEatRecipe::new)
                .save(output, ClayiumMod.id("progression/equipment/" + GadgetType.AUTO_EAT.id()));
        machine(output, GadgetType.REPEATED_ATTACK.id(), gadget(GadgetType.REPEATED_ATTACK),
                10, ClayiumRecipeEnergy.e(1.0D, 10), 120,
                gadget(GadgetType.OVERCLOCK_I), 1, circuit(10), 4);
        GadgetType[] arms = {GadgetType.LONG_ARM, GadgetType.LONGER_ARM, GadgetType.LONGEST_ARM};
        for (int index = 0; index < arms.length; index++) {
            int tier = index == 2 ? 10 : 4;
            int originalTier = index == 0 ? 4 : index == 1 ? 8 : 12;
            machine(output, arms[index].id(), gadget(arms[index]), tier,
                    ClayiumRecipeEnergy.e(1.0D, originalTier), 120,
                    gadget(index == 0 ? GadgetType.TEMPLATE : arms[index - 1]), 1,
                    ProgressionRecipeSupport.component("manipulator_mk_" + new String[]{"i", "ii", "iii"}[index]), 1);
        }

        machine(output, "white_list_item_filter", LogisticsRegistration.ITEM_FILTER.get(),
                4, 8, 20, circuit(5), 1,
                ProgressionRecipeSupport.material("industrial_clay_plate"), 3);
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.TOOLS,
                        LogisticsRegistration.FILTER_DUPLICATOR.get())
                .requires(LogisticsRegistration.ITEM_FILTER.get(), 3)
                .unlockedBy("has_item_filter", InventoryChangeTrigger.TriggerInstance
                        .hasItems(LogisticsRegistration.ITEM_FILTER.get()))
                .save(output, ClayiumMod.id("progression/equipment/item_filter_duplicator"));
        machine(output, "synchronizer", InterfaceRegistration.SYNCHRONIZER.get(),
                6, ClayiumRecipeEnergy.e(1.0D, 6), 20,
                ProgressionRecipeSupport.material("az91_d_alloy_plate"), 3,
                ProgressionRecipeSupport.component("synchronous_parts"), 2);
    }

    private static void shapedTool(RecipeOutput output, String id, ItemLike result,
            String top, String middle, String bottom, ItemLike head, ItemLike handle) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .define('#', head).define('|', handle)
                .pattern(top).pattern(middle).pattern(bottom)
                .unlockedBy("has_head", InventoryChangeTrigger.TriggerInstance.hasItems(head))
                .save(output, ClayiumMod.id("progression/equipment/" + id));
    }

    private static void machine(RecipeOutput output, String id, ItemLike result,
            int tier, long energy, long ticks, ItemLike first, int firstCount,
            ItemLike second, int secondCount) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, tier)
                .requires(first, firstCount).requires(second, secondCount)
                .output(result, 1).clayEnergy(energy).processingTicks(ticks)
                .save(output, ClayiumMod.id("progression/equipment/" + id));
    }

    private static ItemLike gadget(GadgetType type) {
        return GadgetRegistration.GADGETS_BY_TYPE.get(type).get();
    }

    private static ItemLike circuit(int tier) {
        return ProgressionRecipeSupport.component(switch (tier) {
            case 4 -> "basic_circuit";
            case 5 -> "advanced_circuit";
            case 6 -> "precision_circuit";
            case 7 -> "integrated_circuit";
            case 10 -> "clay_spirit";
            case 12 -> "clay_anima";
            case 13 -> "clay_psyche";
            default -> throw new IllegalArgumentException("Unsupported circuit tier " + tier);
        });
    }

    private EquipmentProgressionRecipes() {
    }
}
