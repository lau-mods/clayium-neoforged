package net.claustra01.clayium.datagen.workbench;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe.ProgressionRate;
import net.claustra01.clayium.registry.FoundationItemRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class WorkbenchRecipeProvider {
    public static void buildWorkbenchRecipes(RecipeOutput output) {
        ItemLike plate = material("clay_plate");
        ItemLike stick = material("clay_stick");
        ItemLike shortStick = material("short_clay_stick");
        ItemLike blade = material("clay_blade");
        ItemLike cylinder = material("clay_cylinder");
        ItemLike pipe = material("clay_pipe");

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WorkbenchRegistration.CLAY_WORK_TABLE.get())
                .define('C', Items.CLAY_BALL)
                .define('P', ItemTags.PLANKS)
                .pattern("CCC").pattern("PPP")
                .unlockedBy("has_clay", has(Items.CLAY_BALL))
                .save(output, ClayiumMod.id("workbench/clay_work_table"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WorkbenchRegistration.CLAY_CRAFTING_BOARD.get())
                .define('P', ItemTags.PLANKS)
                .define('W', WorkbenchRegistration.CLAY_WORK_TABLE.get())
                .pattern("PPP").pattern(" W ")
                .unlockedBy("has_work_table", has(WorkbenchRegistration.CLAY_WORK_TABLE.get()))
                .save(output, ClayiumMod.id("workbench/clay_crafting_board"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.RAW_CLAY_ROLLING_PIN.get())
                .define('S', shortStick).define('C', cylinder).pattern("SCS")
                .unlockedBy("has_clay_part", has(shortStick))
                .save(output, ClayiumMod.id("workbench/raw_clay_rolling_pin"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.RAW_CLAY_SLICER.get())
                .define('S', shortStick).define('P', plate).pattern(" P ").pattern(" S ")
                .unlockedBy("has_clay_part", has(shortStick))
                .save(output, ClayiumMod.id("workbench/raw_clay_slicer"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.RAW_CLAY_SPATULA.get())
                .define('S', shortStick).define('B', blade)
                .pattern("SB")
                .unlockedBy("has_clay_blade", has(blade))
                .save(output, ClayiumMod.id("workbench/raw_clay_spatula"));

        smeltTool(output, WorkbenchRegistration.RAW_CLAY_ROLLING_PIN.get(),
                WorkbenchRegistration.CLAY_ROLLING_PIN.get(), "clay_rolling_pin");
        smeltTool(output, WorkbenchRegistration.RAW_CLAY_SLICER.get(),
                WorkbenchRegistration.CLAY_SLICER.get(), "clay_slicer");
        smeltTool(output, WorkbenchRegistration.RAW_CLAY_SPATULA.get(),
                WorkbenchRegistration.CLAY_SPATULA.get(), "clay_spatula");

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FoundationItemRegistration.CLAY_WRENCH.get())
                .define('P', plate).define('S', stick)
                .pattern("P P").pattern(" S ").pattern(" S ")
                .unlockedBy("has_clay_plate", has(plate))
                .save(output, ClayiumMod.id("workbench/clay_wrench"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.CLAY_IO_CONFIGURATOR.get())
                .define('P', pipe).define('S', stick)
                .pattern(" P ").pattern("PSP").pattern(" S ")
                .unlockedBy("has_clay_pipe", has(pipe))
                .save(output, ClayiumMod.id("workbench/clay_io_configurator"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.CLAY_PIPING_CONFIGURATOR.get())
                .define('P', pipe).define('S', stick)
                .pattern("PPP").pattern(" S ").pattern(" S ")
                .unlockedBy("has_clay_pipe", has(pipe))
                .save(output, ClayiumMod.id("workbench/clay_piping_configurator"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.MEMORY_CARD.get())
                .define('P', plate).define('S', shortStick)
                .pattern("PPP").pattern("PSP").pattern("PPP")
                .unlockedBy("has_clay_plate", has(plate))
                .save(output, ClayiumMod.id("workbench/memory_card"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WorkbenchRegistration.DIRECTION_MEMORY_CARD.get())
                .define('P', pipe).define('M', WorkbenchRegistration.MEMORY_CARD.get())
                .pattern(" P ").pattern("PMP").pattern(" P ")
                .unlockedBy("has_memory_card", has(WorkbenchRegistration.MEMORY_CARD.get()))
                .save(output, ClayiumMod.id("workbench/direction_memory_card"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WorkbenchRegistration.RAW_CLAY_MACHINE_HULL.get())
                .define('P', plate).pattern("PPP").pattern("P P").pattern("PPP")
                .unlockedBy("has_clay_plate", has(plate))
                .save(output, ClayiumMod.id("workbench/raw_clay_machine_hull"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(WorkbenchRegistration.RAW_CLAY_MACHINE_HULL.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        WorkbenchRegistration.CLAY_MACHINE_HULL.get(),
                        0.5F,
                        200)
                .unlockedBy("has_raw_hull", has(WorkbenchRegistration.RAW_CLAY_MACHINE_HULL.get()))
                .save(output, ClayiumMod.id("workbench/clay_machine_hull"));

        workTable(output, 1, Items.CLAY_BALL, 1, shortStick, 1, 1, "hand_short_clay_stick");
        workTable(output, 2, Items.CLAY, 1, cylinder, 1, 4, "hand_clay_cylinder");
        workTable(output, 3, Items.CLAY, 1, plate, 4, 4, "roll_clay_plate");
        workTable(output, 4, plate, 1, stick, 2, 2, "slice_clay_stick");
        workTable(output, 5, plate, 1, material("clay_gear"), 1, 4, "shape_clay_gear");
        workTable(output, 6, plate, 1, material("clay_blade"), 1, 2, "cut_clay_blade");
        workTable(output, 6, cylinder, 1, pipe, 1, 2, "bore_clay_pipe");
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static void smeltTool(RecipeOutput output, ItemLike raw, ItemLike fired, String id) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(raw), RecipeCategory.TOOLS, fired, 0.1F, 200)
                .unlockedBy("has_raw_tool", has(raw))
                .save(output, ClayiumMod.id("workbench/" + id));
    }

    private static void workTable(
            RecipeOutput output,
            int action,
            ItemLike input,
            int inputCount,
            ItemLike result,
            int resultCount,
            int presses,
            String id) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.CLAY_WORK_TABLE, action)
                .requires(input, inputCount)
                .output(result, resultCount)
                .processingTicks(presses)
                .progressionRate(ProgressionRate.TICKS)
                .save(output, ClayiumMod.id("workbench/" + id));
    }

    private WorkbenchRecipeProvider() {
    }
}
