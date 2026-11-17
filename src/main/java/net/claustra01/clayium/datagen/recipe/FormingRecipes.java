package net.claustra01.clayium.datagen.recipe;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.series.FormingSeries;
import net.claustra01.clayium.material.ClayMaterials;
import net.claustra01.clayium.material.MaterialShape;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class FormingRecipes {
    public static void buildRecipes(RecipeOutput output) {
        bending(output);
        wireDrawing(output);
        pipeDrawing(output);
        cutting(output);
        lathe(output);
        milling(output);
        machineCrafting(output);
        clayParts(output);
    }

    private static void bending(RecipeOutput output) {
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 3)
                .requires(Items.CLAY_BALL).output(material("small_clay_disc"), 1)
                .save(output, ClayiumMod.id("bending_machine/small_clay_disc"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 2)
                .requires(material("large_clay_ball")).output(material("clay_disc"), 1)
                .save(output, ClayiumMod.id("bending_machine/clay_disc"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 1)
                .requires(Items.CLAY).output(material("clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 4)
                .requires(material("clay_plate"), 4).output(material("large_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/large_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 4)
                .requires(material("dense_clay"), 1).output(material("dense_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/dense_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 8)
                .requires(material("dense_clay_plate"), 4).output(material("large_dense_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/large_dense_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 2, 2L, 4)
                .requires(material("industrial_clay"), 1).output(material("industrial_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/industrial_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 2, 2L, 8)
                .requires(material("industrial_clay_plate"), 4).output(material("large_industrial_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/large_industrial_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 2, 4L, 4)
                .requires(material("advanced_industrial_clay"), 1)
                .output(material("advanced_industrial_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/advanced_industrial_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 2, 4L, 8)
                .requires(material("advanced_industrial_clay_plate"), 4)
                .output(material("large_advanced_industrial_clay_plate"), 1)
                .save(output, ClayiumMod.id("bending_machine/large_advanced_industrial_clay_plate"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 4)
                .requires(material("clay_cylinder")).output(material("clay_blade"), 2)
                .save(output, ClayiumMod.id("bending_machine/clay_blade"));
        machine(ClayiumRecipeMethod.BENDING_MACHINE, 0, 1L, 8)
                .requires(material("dense_clay_cylinder")).output(material("dense_clay_blade"), 2)
                .save(output, ClayiumMod.id("bending_machine/dense_clay_blade"));
        for (var variant : ClayMaterials.VARIANTS) {
            if (variant.shape() != MaterialShape.PLATE) {
                continue;
            }
            String id = variant.id();
            String base = variant.material().id();
            if (!java.util.Set.of("impure_silicon", "silicon", "silicone", "aluminium", "clay_steel",
                    "clayium", "ultimate_alloy", "az91d_alloy", "zk60a_alloy").contains(base)) {
                continue;
            }
            float hardness = switch (base) {
                case "silicon" -> 0.2F;
                case "clay_steel" -> 3.0F;
                case "clayium" -> 6.0F;
                case "ultimate_alloy" -> 9.0F;
                default -> 1.0F;
            };
            String ingot = base.equals("az91d_alloy") ? "az91_d_alloy_ingot"
                    : base.equals("zk60a_alloy") ? "zk60_a_alloy_ingot" : base + "_ingot";
            String largePlate = "large_" + id;
            machine(ClayiumRecipeMethod.BENDING_MACHINE, 4, ClayiumRecipeEnergy.e(1.0D, 4),
                    (int)(20.0F * hardness))
                    .requires(material(ingot)).output(material(id), 1)
                    .save(output, ClayiumMod.id("bending_machine/" + id));
            machine(ClayiumRecipeMethod.BENDING_MACHINE, 4, ClayiumRecipeEnergy.e(1.0D, 4),
                    (int)(40.0F * hardness))
                    .requires(material(id), 4).output(material(largePlate), 1)
                    .save(output, ClayiumMod.id("bending_machine/" + largePlate));
        }
        String[] highInputs = {"antimatter", "pure_antimatter",
            "octuple_compressed_energetic_clay", "octuple_compressed_pure_antimatter"};
        String[] highPlates = {"antimatter_plate", "pure_antimatter_plate",
            "octuple_energetic_clay_plate", "octuple_pure_antimatter_plate"};
        for (int index = 0; index < highInputs.length; index++) {
            int tier = 9 + index;
            String plate = highPlates[index];
            machine(ClayiumRecipeMethod.BENDING_MACHINE, 9, ClayiumRecipeEnergy.e(1.0D, tier), 20)
                    .requires(material(highInputs[index])).output(material(plate), 1)
                    .save(output, ClayiumMod.id("bending_machine/" + plate));
            machine(ClayiumRecipeMethod.BENDING_MACHINE, 9, ClayiumRecipeEnergy.e(1.0D, tier), 40)
                    .requires(material(plate), 4).output(material("large_" + plate), 1)
                    .save(output, ClayiumMod.id("bending_machine/large_" + plate));
        }
    }

    private static void wireDrawing(RecipeOutput output) {
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 1)
                .requires(Items.CLAY_BALL).output(material("clay_stick"), 1)
                .save(output, ClayiumMod.id("wire_drawing_machine/clay_stick"));
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 3)
                .requires(material("clay_cylinder"), 1).output(material("clay_stick"), 8)
                .save(output, ClayiumMod.id("wire_drawing_machine/clay_cylinder_to_sticks"));
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 2)
                .requires(material("clay_pipe"), 1).output(material("clay_stick"), 4)
                .save(output, ClayiumMod.id("wire_drawing_machine/clay_pipe_to_sticks"));
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 6)
                .requires(material("dense_clay_cylinder"), 1).output(material("dense_clay_stick"), 8)
                .save(output, ClayiumMod.id("wire_drawing_machine/dense_clay_cylinder_to_sticks"));
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 4)
                .requires(material("dense_clay_pipe"), 1).output(material("dense_clay_stick"), 4)
                .save(output, ClayiumMod.id("wire_drawing_machine/dense_clay_pipe_to_sticks"));
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 1)
                .requires(material("small_clay_disc")).output(material("clay_stick"), 1)
                .save(output, ClayiumMod.id("wire_drawing_machine/clay_disc_to_stick"));
        machine(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE, 0, 1L, 2)
                .requires(material("small_dense_clay_disc")).output(material("dense_clay_stick"), 1)
                .save(output, ClayiumMod.id("wire_drawing_machine/dense_clay_disc_to_stick"));
    }

    private static void pipeDrawing(RecipeOutput output) {
        machine(ClayiumRecipeMethod.PIPE_DRAWING_MACHINE, 0, 1L, 3)
                .requires(material("clay_cylinder"), 1).output(material("clay_pipe"), 2)
                .save(output, ClayiumMod.id("pipe_drawing_machine/clay_pipe"));
        machine(ClayiumRecipeMethod.PIPE_DRAWING_MACHINE, 0, 1L, 6)
                .requires(material("dense_clay_cylinder"), 1).output(material("dense_clay_pipe"), 2)
                .save(output, ClayiumMod.id("pipe_drawing_machine/dense_clay_pipe"));
    }

    private static void cutting(RecipeOutput output) {
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 2)
                .requires(material("large_clay_ball")).output(material("clay_disc"), 1)
                .save(output, ClayiumMod.id("cutting_machine/clay_disc"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 1)
                .requires(material("clay_stick"), 1).output(material("short_clay_stick"), 2)
                .save(output, ClayiumMod.id("cutting_machine/short_clay_stick"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 3)
                .requires(material("clay_plate"), 1).output(material("small_clay_disc"), 4)
                .save(output, ClayiumMod.id("cutting_machine/small_clay_disc"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 2)
                .requires(material("dense_clay_stick"), 1).output(material("short_dense_clay_stick"), 2)
                .save(output, ClayiumMod.id("cutting_machine/short_dense_clay_stick"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 6)
                .requires(material("dense_clay_plate"), 1).output(material("small_dense_clay_disc"), 4)
                .save(output, ClayiumMod.id("cutting_machine/small_dense_clay_disc"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 2)
                .requires(material("clay_cylinder")).output(material("small_clay_disc"), 8)
                .save(output, ClayiumMod.id("cutting_machine/clay_cylinder_to_discs"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 3)
                .requires(material("large_clay_plate")).output(material("clay_disc"), 2)
                .save(output, ClayiumMod.id("cutting_machine/large_clay_plate_to_discs"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 4)
                .requires(material("dense_clay_cylinder")).output(material("small_dense_clay_disc"), 8)
                .save(output, ClayiumMod.id("cutting_machine/dense_clay_cylinder_to_discs"));
        machine(ClayiumRecipeMethod.CUTTING_MACHINE, 0, 1L, 6)
                .requires(material("large_dense_clay_plate")).output(material("dense_clay_disc"), 2)
                .save(output, ClayiumMod.id("cutting_machine/large_dense_clay_plate_to_discs"));
    }

    private static void lathe(RecipeOutput output) {
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 1)
                .requires(Items.CLAY_BALL).output(material("short_clay_stick"), 1)
                .save(output, ClayiumMod.id("lathe/short_clay_stick"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 2)
                .requires(material("clay_disc"), 1).output(material("clay_ring"), 1)
                .save(output, ClayiumMod.id("lathe/clay_ring"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 4)
                .requires(material("dense_clay_disc"), 1).output(material("dense_clay_ring"), 1)
                .save(output, ClayiumMod.id("lathe/dense_clay_ring"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 4)
                .requires(material("large_clay_ball")).output(material("clay_cylinder"), 1)
                .save(output, ClayiumMod.id("lathe/clay_cylinder"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 3)
                .requires(material("clay_cylinder")).output(material("clay_needle"), 1)
                .save(output, ClayiumMod.id("lathe/clay_needle"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 3)
                .requires(material("clay_needle")).output(material("clay_stick"), 6)
                .save(output, ClayiumMod.id("lathe/clay_needle_to_sticks"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 1)
                .requires(material("small_clay_disc")).output(material("small_clay_ring"), 1)
                .save(output, ClayiumMod.id("lathe/small_clay_ring"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 4)
                .requires(material("dense_clay")).output(material("dense_clay_cylinder"), 1)
                .save(output, ClayiumMod.id("lathe/dense_clay_cylinder"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 6)
                .requires(material("dense_clay_cylinder")).output(material("dense_clay_needle"), 1)
                .save(output, ClayiumMod.id("lathe/dense_clay_needle"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 6)
                .requires(material("dense_clay_needle")).output(material("dense_clay_stick"), 6)
                .save(output, ClayiumMod.id("lathe/dense_clay_needle_to_sticks"));
        machine(ClayiumRecipeMethod.LATHE, 0, 1L, 2)
                .requires(material("small_dense_clay_disc")).output(material("small_dense_clay_ring"), 1)
                .save(output, ClayiumMod.id("lathe/small_dense_clay_ring"));
    }

    private static void milling(RecipeOutput output) {
        machine(ClayiumRecipeMethod.MILLING_MACHINE, 0, 1L, 32)
                .requires(material("dense_clay_plate"), 1)
                .output(MaterialRegistration.COMPONENTS.get("clay_circuit_board").get(), 1)
                .save(output, ClayiumMod.id("milling_machine/clay_circuit_board_from_dense_clay"));
        machine(ClayiumRecipeMethod.MILLING_MACHINE, 0, 1L, 1)
                .requires(material("industrial_clay_plate"), 1)
                .output(MaterialRegistration.COMPONENTS.get("clay_circuit_board").get(), 1)
                .save(output, ClayiumMod.id("milling_machine/clay_circuit_board"));
        machine(ClayiumRecipeMethod.MILLING_MACHINE, 3, 2L, 32)
                .requires(material("advanced_industrial_clay_plate"), 1)
                .output(MaterialRegistration.COMPONENTS.get("cee_board").get(), 1)
                .save(output, ClayiumMod.id("milling_machine/cee_board"));
    }

    private static void machineCrafting(RecipeOutput output) {
        for (FormingMachineRegistration.MachineDefinition definition
                : FormingMachineRegistration.MACHINES.values()) {
            ItemLike plate = machinePlate(definition.tier());
            ItemLike circuit = machineCircuit(definition.tier());
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, definition.block().get())
                    .define('P', plate)
                    .define('H', WorkbenchRegistration.CLAY_MACHINE_HULL.get())
                    .define('C', circuit)
                    .define('S', seriesPart(definition.series()))
                    .pattern(" S ").pattern("CHC").pattern(" P ")
                    .unlockedBy("has_machine_hull", InventoryChangeTrigger.TriggerInstance.hasItems(
                            WorkbenchRegistration.CLAY_MACHINE_HULL.get()))
                    .save(output, ClayiumMod.id("forming_machine/" + definition.id()));
        }
    }

    private static void clayParts(RecipeOutput output) {
        for (String prefix : new String[] {"clay", "dense_clay"}) {
            ItemLike ball = prefix.equals("clay") ? Items.CLAY_BALL : material("dense_clay_ball");
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, material(prefix + "_cutting_head"))
                    .define('i', material(prefix + "_blade"))
                    .define('o', material(prefix + "_ring"))
                    .pattern("iii").pattern("ioi").pattern("iii")
                    .unlockedBy("has_blade", InventoryChangeTrigger.TriggerInstance.hasItems(
                            material(prefix + "_blade")))
                    .save(output, ClayiumMod.id("forming_parts/" + prefix + "_cutting_head"));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, material(prefix + "_bearing"))
                    .define('i', ball)
                    .define('o', material(prefix + "_ring"))
                    .pattern("iii").pattern("ioi").pattern("iii")
                    .unlockedBy("has_ring", InventoryChangeTrigger.TriggerInstance.hasItems(
                            material(prefix + "_ring")))
                    .save(output, ClayiumMod.id("forming_parts/" + prefix + "_bearing"));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, material(prefix + "_spindle"))
                    .define('0', material("small_" + prefix + "_ring"))
                    .define('#', material(prefix + "_plate"))
                    .define('i', material(prefix + "_stick"))
                    .define('o', material(prefix + "_bearing"))
                    .define('O', material(prefix + "_ring"))
                    .pattern("0#0").pattern("ioO").pattern("0#0")
                    .unlockedBy("has_bearing", InventoryChangeTrigger.TriggerInstance.hasItems(
                            material(prefix + "_bearing")))
                    .save(output, ClayiumMod.id("forming_parts/" + prefix + "_spindle"));
        }
    }

    private static ItemLike seriesPart(FormingSeries series) {
        return switch (series) {
            case BENDING_MACHINE -> material("clay_plate");
            case WIRE_DRAWING_MACHINE -> material("clay_stick");
            case PIPE_DRAWING_MACHINE -> material("clay_pipe");
            case CUTTING_MACHINE -> WorkbenchRegistration.RAW_CLAY_SLICER.get();
            case LATHE -> WorkbenchRegistration.RAW_CLAY_ROLLING_PIN.get();
            case MILLING_MACHINE -> material("clay_gear");
        };
    }

    private static ItemLike machinePlate(int tier) {
        return switch (tier) {
            case 1 -> material("clay_plate");
            case 2 -> material("dense_clay_plate");
            case 3 -> material("industrial_clay_plate");
            case 4 -> material("advanced_industrial_clay_plate");
            case 5, 6 -> material("aluminium_plate");
            case 7 -> material("clay_steel_plate");
            default -> material("ultimate_alloy_plate");
        };
    }

    private static ItemLike machineCircuit(int tier) {
        String id = switch (tier) {
            case 1, 2 -> "clay_circuit";
            case 3 -> "simple_circuit";
            case 4 -> "basic_circuit";
            case 5 -> "advanced_circuit";
            case 6 -> "precision_circuit";
            default -> "integrated_circuit";
        };
        return MaterialRegistration.COMPONENTS.get(id).get();
    }

    private static ClayiumRecipeBuilder machine(
            ClayiumRecipeMethod method, int tier, long energy, int ticks) {
        return ClayiumRecipeBuilder.machine(method, tier).clayEnergy(energy).processingTicks(ticks);
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private FormingRecipes() {
    }
}
