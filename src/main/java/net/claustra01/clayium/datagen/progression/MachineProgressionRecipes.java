package net.claustra01.clayium.datagen.progression;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.level.ItemLike;

public final class MachineProgressionRecipes {
    public static void buildRecipes(RecipeOutput output) {
        initialFormingAndParts(output);
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 4)
                .requires(item("chemical_reactor_tier_5"), 1)
                .requires(item("smelter_tier_6"), 1)
                .output(item("chemical_metal_separator"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 6)).processingTicks(40)
                .save(output, ClayiumMod.id("progression/machines/chemical_metal_separator"));
        for (var definition : MachineRegistration.MACHINES.values()) {
            if (definition.series().recipeMethod() == ClayiumRecipeMethod.EC_DECOMPOSER) {
                ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 10)
                        .requires(item("opa_machine_hull"), 1).requires(item("ca_reactor_coil_mk_iv"), 1)
                        .output(definition.block().get(), 1).clayEnergy(ClayiumRecipeEnergy.e(1.0D, 13))
                        .processingTicks(120).save(output, ClayiumMod.id("progression/machines/" + definition.id()));
                continue;
            }
            if (definition.id().equals("chemical_metal_separator")) {
                continue;
            }
            String series = definition.series().recipeMethod().id();
            int tier = definition.tier();
            ItemLike result = definition.block().get();
            ItemLike hull = AssemblyMachineRegistration.MACHINE_HULLS.get(tier).get();
            String path = "progression/machines/" + definition.id();
            if (tier <= 4 && (series.equals("condenser") || series.equals("grinder")
                    || series.equals("decomposer"))) {
                ItemLike spindle = material("dense_clay_spindle");
                ItemLike gear = material("dense_clay_gear");
                ItemLike plate = material("dense_clay_plate");
                ItemLike pipe = material("dense_clay_pipe");
                if (series.equals("condenser")) {
                    shaped(output, path, result, new String[]{"*P*", "P#P", "*P*"},
                            Map.of('#', hull, '*', gear, 'P', plate));
                } else if (series.equals("grinder")) {
                    shaped(output, path, result, new String[]{"P0P", "o#o", "P*P"},
                            Map.of('#', hull, 'o', spindle, '*', gear, 'P', plate,
                                    '0', material("dense_clay_grinding_head")));
                } else {
                    shaped(output, path, result, new String[]{"*o*", "C#C", "*=*"},
                            Map.of('#', hull, 'o', spindle, '*', gear, '=', pipe,
                                    'C', circuit(tier)));
                }
                continue;
            }
            switch (series) {
                case "condenser" -> {
                    if (tier == 5) {
                        assembler(output, path, result, 4, tier, 120,
                                hull, 1, item("buffer_tier_5"), 1);
                    } else {
                        injectorUpgrade(output, path, result, "condenser_tier_5", 5, tier);
                    }
                }
                case "grinder" -> {
                    if (tier <= 6) {
                        assembler(output, path, result, 4, tier, 120,
                                hull, 1, material("dense_clay_grinding_head"), 1);
                    } else {
                        injectorUpgrade(output, path, result, "grinder_tier_6", 6, tier);
                    }
                }
                case "smelter" -> {
                    if (tier <= 7) {
                        assembler(output, path, result, 4, tier, 120,
                                hull, 1, circuit(3), tier - 3);
                    } else {
                        assembler(output, path, result, 6, tier, 2000,
                                hull, 1, item("smelter_tier_" + (tier - 1)), 16);
                    }
                }
                case "alloy_smelter" -> assembler(output, path, result, 4, tier, 40,
                        item("smelter_tier_6"), 1, circuit(6), 1);
                case "electrolysis_reactor" -> assembler(output, path, result, 4, tier, 40,
                        item("chemical_reactor_tier_5"), 1, circuit(tier), 1);
                case "chemical_reactor" -> {
                    if (tier <= 5) {
                        assembler(output, path, result, 4, tier, 120,
                                hull, 1, circuit(4), tier - 3);
                    } else {
                        assembler(output, path, result, 4, tier, 480,
                                hull, 1, item("chemical_reactor_tier_5"), 1);
                    }
                }
                case "matter_transformer" -> {
                    if (tier <= 9) {
                        assembler(output, path, result, 6, tier, 120,
                                item("clay_reactor"), 1, item("electrolysis_reactor_tier_" + tier), 1);
                    } else {
                        assembler(output, path, result, 10, tier, 120,
                                item("ca_injector_tier_" + tier), 1, item("clay_reactor"), 1);
                    }
                }
                default -> throw new IllegalStateException("Unmapped original machine recipe: " + definition.id());
            }
        }
    }

    private static void initialFormingAndParts(RecipeOutput output) {
        ItemLike hull = WorkbenchRegistration.CLAY_MACHINE_HULL.get();
        ItemLike spindle = material("clay_spindle");
        ItemLike cylinder = material("clay_cylinder");
        ItemLike gear = material("clay_gear");
        ItemLike plate = material("clay_plate");
        ItemLike pipe = material("clay_pipe");
        shaped(output, "progression/forming/clay_bending_machine", item("clay_bending_machine"),
                new String[]{"o-*", "P#P", "o-*"},
                Map.of('#', hull, 'o', spindle, '-', cylinder, '*', gear, 'P', plate));
        shaped(output, "progression/forming/wire_drawing_machine_tier_1",
                item("wire_drawing_machine_tier_1"), new String[]{"*o*", "=#=", "*o*"},
                Map.of('#', hull, 'o', spindle, '*', gear, '=', pipe));
        shaped(output, "progression/forming/pipe_drawing_machine_tier_1",
                item("pipe_drawing_machine_tier_1"), new String[]{"*o*", "-#=", "*o*"},
                Map.of('#', hull, 'o', spindle, '-', cylinder, '*', gear, '=', pipe));
        shaped(output, "progression/forming/cutting_machine_tier_1",
                item("cutting_machine_tier_1"), new String[]{"P*P", "o#|", "P*P"},
                Map.of('#', hull, 'o', spindle, '*', gear, 'P', plate,
                        '|', material("clay_cutting_head")));
        shaped(output, "progression/forming/lathe_tier_1",
                item("lathe_tier_1"), new String[]{"P*P", "-#o", "P*P"},
                Map.of('#', hull, 'o', spindle, '*', gear, 'P', plate,
                        '-', material("clay_stick")));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, material("clay_ring"))
                .requires(cylinder)
                .unlockedBy("has_cylinder", InventoryChangeTrigger.TriggerInstance.hasItems(cylinder))
                .save(output, ClayiumMod.id("progression/material/clay_ring"));
        ItemLike shortStick = material("short_clay_stick");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, material("small_clay_ring"))
                .requires(shortStick)
                .unlockedBy("has_short_stick", InventoryChangeTrigger.TriggerInstance.hasItems(shortStick))
                .save(output, ClayiumMod.id("progression/material/small_clay_ring"));
        shaped(output, "progression/material/dense_clay_gear", material("dense_clay_gear"),
                new String[]{"iii", "ioi", "iii"},
                Map.of('i', material("short_dense_clay_stick"), 'o', material("small_dense_clay_ring")));
    }

    private static void shaped(RecipeOutput output, String path, ItemLike result,
            String[] rows, Map<Character, ItemLike> ingredients) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result);
        ingredients.forEach(builder::define);
        for (String row : rows) {
            builder.pattern(row);
        }
        char unlock = switch (path) {
            case "progression/forming/clay_bending_machine",
                    "progression/forming/lathe_tier_1",
                    "progression/forming/pipe_drawing_machine_tier_1" -> '-';
            case "progression/material/dense_clay_gear" -> 'o';
            default -> '#';
        };
        builder.unlockedBy("has_material", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ingredients.get(unlock)))
                .save(output, ClayiumMod.id(path));
    }

    private static void assembler(RecipeOutput output, String path, ItemLike result,
            int requiredTier, int energyTier, long ticks, ItemLike first, int firstCount,
            ItemLike second, int secondCount) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, requiredTier)
                .requires(first, firstCount).requires(second, secondCount)
                .output(result, 1).clayEnergy(ClayiumRecipeEnergy.e(1.0D, energyTier))
                .processingTicks(ticks).save(output, ClayiumMod.id(path));
    }

    private static void injectorUpgrade(RecipeOutput output, String path, ItemLike result,
            String previous, int previousTier, int tier) {
        int gems = 0;
        for (int step = previousTier + 1; step <= tier; step++) {
            gems = Math.min(64, (int)(gems + Math.pow(1.3F, step)));
        }
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CA_INJECTOR, tier)
                .requires(item(previous)).requires(material("antimatter"), gems)
                .output(result, 1).clayEnergy(ClayiumRecipeEnergy.e(3.0D, tier))
                .processingTicks(4000).save(output, ClayiumMod.id(path));
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.get(ClayiumMod.id(id));
    }

    private static ItemLike material(String id) {
        return ProgressionRecipeSupport.material(id);
    }

    private static ItemLike circuit(int tier) {
        return ProgressionRecipeSupport.component(tier <= 2 ? "clay_circuit"
                : tier == 3 ? "simple_circuit" : tier == 4 ? "basic_circuit"
                : tier == 5 ? "advanced_circuit" : tier == 6 ? "precision_circuit"
                : tier == 7 ? "integrated_circuit" : tier == 8 ? "clay_core"
                : tier == 9 ? "clay_brain" : tier == 10 ? "clay_spirit"
                : tier == 11 ? "clay_soul" : tier == 12 ? "clay_anima" : "clay_psyche");
    }

    private MachineProgressionRecipes() {
    }
}
