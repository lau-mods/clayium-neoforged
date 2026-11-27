package net.claustra01.clayium.datagen.recipe;

import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.COMPRESSED_CLAY_PATHS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.COMPRESSED_PURE_ANTIMATTERS;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.blockItem;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.circuit;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.commonTag;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.compressedClay;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.counted;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.countedTag;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.e;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.hasMatItem;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.item;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.machineHull;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.matStack;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.oreTag;
import static net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper.stack;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.ClayiumConfigCondition;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

/**
 * Ports all 1.7.10 CRecipes material processing recipes across Condenser, Grinder, Decomposer,
 * Bending/Drawing/Cutting/Lathe, Centrifuge, ChemicalReactor, Smelter, AlloySmelter, BlastFurnace,
 * ElectrolysisReactor, Reactor, MatterTransformer, CACondenser, CAInjector, CAReactor, and ChemicalMetalSeparator.
 */
public final class ClayMaterialMachineRecipeGenerator {
    private static final String CAT_BENDING = "bending_machine";
    private static final String CAT_WIRE = "wire_drawing_machine";
    private static final String CAT_PIPE = "pipe_drawing_machine";
    private static final String CAT_CUTTING = "cutting_machine";
    private static final String CAT_LATHE = "lathe";
    private static final String CAT_CONDENSER = "condenser";
    private static final String CAT_GRINDER = "grinder";
    private static final String CAT_DECOMPOSER = "decomposer";
    private static final String CAT_EC_CONDENSER = "energetic_clay_condenser";
    private static final String CAT_EC_DECOMPOSER = "energetic_clay_decomposer";
    private static final String CAT_ASSEMBLER = "assembler";
    private static final String CAT_CENTRIFUGE = "centrifuge";
    private static final String CAT_CHEMICAL_REACTOR = "chemical_reactor";
    private static final String CAT_ALLOY_SMELTER = "alloy_smelter";
    private static final String CAT_BLAST_FURNACE = "blast_furnace";
    private static final String CAT_ELECTROLYSIS = "electrolysis_reactor";
    private static final String CAT_REACTOR = "reactor";
    private static final String CAT_TRANSFORMER = "matter_transformer";
    private static final String CAT_CA_CONDENSER = "ca_condenser";
    private static final String CAT_CA_INJECTOR = "ca_injector";
    private static final String CAT_CA_REACTOR = "ca_reactor";
    private static final String CAT_SMELTER = "smelter";

    private ClayMaterialMachineRecipeGenerator() {}

    public static void build(ClayiumRecipeHelper h) {
        buildMainMaterialMachineRecipes(h);
        buildChemicalMetalSeparator(h);
        buildMaterialProcessingRecipes(h);
        buildMatterTransformerAndCAInjectorChains(h);
    }

    private static void buildMainMaterialMachineRecipes(ClayiumRecipeHelper h) {
        // Condenser: Clay -> DenseClay .. OctupleEnergeticClay
        h.simple(CAT_CONDENSER, Items.CLAY, 9, 0, compressedClay(0, 1), 1L, 4L);
        long[] condEnergies = {1L, 10L, 100L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 1000000000L};
        int[] condTiers =     {0,  0,   0,    4,    4,     4,      5,       5,        5,         5,          5,           5};
        long[] condTimes =    {4L, 4L,  4L,   16L,  16L,   13L,    10L,     8L,       6L,        4L,         3L,          25L};
        for (int i = 0; i < 12; i++) {
            h.simple(CAT_CONDENSER, blockItem(COMPRESSED_CLAY_PATHS[i]), 9, condTiers[i], compressedClay(i + 1, 1), condEnergies[i], condTimes[i]);
        }

        // Energetic Clay Condenser: 3..11 -> 4..12
        long[] ecEnergies = {1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 10000000L};
        int[] ecTiers =     {3,  3,   3,    4,     4,      4,       4,        4,         4};
        long[] ecTimes =    {16L, 32L, 64L, 64L,   64L,    64L,     64L,      64L,       64L};
        for (int idx = 0; idx < 9; idx++) {
            int src = idx + 3;
            h.simple(CAT_EC_CONDENSER, blockItem(COMPRESSED_CLAY_PATHS[src]), 9, ecTiers[idx], compressedClay(src + 1, 1), ecEnergies[idx], ecTimes[idx]);
        }

        // Decomposer & Energetic Clay Decomposer
        h.simple(CAT_DECOMPOSER, Items.CLAY, 1, 0, matStack(CMaterials.CLAY, CShape.BALL, 4), 1L, 3L);
        h.simple(CAT_DECOMPOSER, blockItem(COMPRESSED_CLAY_PATHS[0]), 1, 0, new ItemStack(Items.CLAY, 9), 1L, 3L);
        h.simple(CAT_DECOMPOSER, blockItem(COMPRESSED_CLAY_PATHS[1]), 1, 0, compressedClay(0, 9), 1L, 3L);
        h.simple(CAT_DECOMPOSER, blockItem(COMPRESSED_CLAY_PATHS[2]), 1, 0, compressedClay(1, 9), 1L, 10L);
        h.simple(CAT_DECOMPOSER, blockItem(COMPRESSED_CLAY_PATHS[3]), 1, 0, compressedClay(2, 9), 1L, 20L);

        h.simple(CAT_EC_DECOMPOSER, Items.CLAY, 1, 13, matStack(CMaterials.CLAY, CShape.BALL, 4), 1L, 0L);
        h.simple(CAT_EC_DECOMPOSER, blockItem(COMPRESSED_CLAY_PATHS[0]), 1, 13, new ItemStack(Items.CLAY, 9), 1L, 0L);
        for (int i = 0; i < 12; i++) {
            h.simple(CAT_EC_DECOMPOSER, blockItem(COMPRESSED_CLAY_PATHS[i + 1]), 1, 13, compressedClay(i, 9), 1L, 0L);
        }

        // Bending Machine (Clay/Dense/Ind/AdvInd)
        h.simple(CAT_BENDING, Items.CLAY_BALL, 1, 0, matStack(CMaterials.CLAY, CShape.SMALL_DISC), 1L, 3L);
        h.simple(CAT_BENDING, matStack(CMaterials.CLAY, CShape.LARGE_BALL), matStack(CMaterials.CLAY, CShape.DISC), 1L, 2L);
        h.simple(CAT_BENDING, matStack(CMaterials.CLAY, CShape.DISC), stack("raw_clay_slicer"), 1L, 1L);
        h.simple(CAT_BENDING, Items.CLAY, 1, 0, matStack(CMaterials.CLAY, CShape.PLATE), 1L, 1L);
        h.simple(CAT_BENDING, matStack(CMaterials.CLAY, CShape.PLATE, 4), matStack(CMaterials.CLAY, CShape.LARGE_PLATE), 1L, 4L);
        h.simple(CAT_BENDING, matStack(CMaterials.CLAY, CShape.CYLINDER), matStack(CMaterials.CLAY, CShape.BLADE, 2), 1L, 4L);
        h.simple(CAT_BENDING, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK), matStack(CMaterials.DENSE_CLAY, CShape.PLATE), 1L, 4L);
        h.simple(CAT_BENDING, matStack(CMaterials.DENSE_CLAY, CShape.PLATE, 4), matStack(CMaterials.DENSE_CLAY, CShape.LARGE_PLATE), 1L, 8L);
        h.simple(CAT_BENDING, matStack(CMaterials.DENSE_CLAY, CShape.CYLINDER), matStack(CMaterials.DENSE_CLAY, CShape.BLADE, 2), 1L, 8L);
        h.simple(CAT_BENDING, matStack(CMaterials.IND_CLAY, CShape.BLOCK), 2, matStack(CMaterials.IND_CLAY, CShape.PLATE), 2L, 4L);
        h.simple(CAT_BENDING, matStack(CMaterials.IND_CLAY, CShape.PLATE, 4), 2, matStack(CMaterials.IND_CLAY, CShape.LARGE_PLATE), 2L, 8L);
        h.simple(CAT_BENDING, matStack(CMaterials.ADVIND_CLAY, CShape.BLOCK), 2, matStack(CMaterials.ADVIND_CLAY, CShape.PLATE), 4L, 4L);
        h.simple(CAT_BENDING, matStack(CMaterials.ADVIND_CLAY, CShape.PLATE, 4), 2, matStack(CMaterials.ADVIND_CLAY, CShape.LARGE_PLATE), 4L, 8L);

        // Wire Drawing
        h.simple(CAT_WIRE, Items.CLAY_BALL, 1, 0, matStack(CMaterials.CLAY, CShape.STICK), 1L, 1L);
        h.simple(CAT_WIRE, matStack(CMaterials.CLAY, CShape.CYLINDER), matStack(CMaterials.CLAY, CShape.STICK, 8), 1L, 3L);
        h.simple(CAT_WIRE, matStack(CMaterials.CLAY, CShape.PIPE), matStack(CMaterials.CLAY, CShape.STICK, 4), 1L, 2L);
        h.simple(CAT_WIRE, matStack(CMaterials.CLAY, CShape.SMALL_DISC), matStack(CMaterials.CLAY, CShape.STICK), 1L, 1L);
        h.simple(CAT_WIRE, matStack(CMaterials.DENSE_CLAY, CShape.CYLINDER), matStack(CMaterials.DENSE_CLAY, CShape.STICK, 8), 1L, 6L);
        h.simple(CAT_WIRE, matStack(CMaterials.DENSE_CLAY, CShape.PIPE), matStack(CMaterials.DENSE_CLAY, CShape.STICK, 4), 1L, 4L);
        h.simple(CAT_WIRE, matStack(CMaterials.DENSE_CLAY, CShape.SMALL_DISC), matStack(CMaterials.DENSE_CLAY, CShape.STICK), 1L, 2L);

        // Pipe Drawing
        h.simple(CAT_PIPE, matStack(CMaterials.CLAY, CShape.CYLINDER), matStack(CMaterials.CLAY, CShape.PIPE, 2), 1L, 3L);
        h.simple(CAT_PIPE, matStack(CMaterials.DENSE_CLAY, CShape.CYLINDER), matStack(CMaterials.DENSE_CLAY, CShape.PIPE, 2), 1L, 6L);

        // Cutting
        h.simple(CAT_CUTTING, matStack(CMaterials.CLAY, CShape.LARGE_BALL), matStack(CMaterials.CLAY, CShape.DISC), 1L, 2L);
        h.simple(CAT_CUTTING, matStack(CMaterials.CLAY, CShape.CYLINDER), matStack(CMaterials.CLAY, CShape.SMALL_DISC, 8), 1L, 2L);
        h.simple(CAT_CUTTING, matStack(CMaterials.CLAY, CShape.LARGE_PLATE), matStack(CMaterials.CLAY, CShape.DISC, 2), 1L, 3L);
        h.simple(CAT_CUTTING, matStack(CMaterials.CLAY, CShape.PLATE), matStack(CMaterials.CLAY, CShape.SMALL_DISC, 4), 1L, 3L);
        h.simple(CAT_CUTTING, matStack(CMaterials.CLAY, CShape.STICK), matStack(CMaterials.CLAY, CShape.SHORT_STICK, 2), 1L, 1L);
        h.simple(CAT_CUTTING, matStack(CMaterials.DENSE_CLAY, CShape.CYLINDER), matStack(CMaterials.DENSE_CLAY, CShape.SMALL_DISC, 8), 1L, 4L);
        h.simple(CAT_CUTTING, matStack(CMaterials.DENSE_CLAY, CShape.LARGE_PLATE), matStack(CMaterials.DENSE_CLAY, CShape.DISC, 2), 1L, 6L);
        h.simple(CAT_CUTTING, matStack(CMaterials.DENSE_CLAY, CShape.PLATE), matStack(CMaterials.DENSE_CLAY, CShape.SMALL_DISC, 4), 1L, 6L);
        h.simple(CAT_CUTTING, matStack(CMaterials.DENSE_CLAY, CShape.STICK), matStack(CMaterials.DENSE_CLAY, CShape.SHORT_STICK, 2), 1L, 2L);

        // Lathe
        h.simple(CAT_LATHE, Items.CLAY_BALL, 1, 0, matStack(CMaterials.CLAY, CShape.SHORT_STICK), 1L, 1L);
        h.simple(CAT_LATHE, matStack(CMaterials.CLAY, CShape.LARGE_BALL), matStack(CMaterials.CLAY, CShape.CYLINDER), 1L, 4L);
        h.simple(CAT_LATHE, matStack(CMaterials.CLAY, CShape.CYLINDER), matStack(CMaterials.CLAY, CShape.NEEDLE), 1L, 3L);
        h.simple(CAT_LATHE, matStack(CMaterials.CLAY, CShape.NEEDLE), matStack(CMaterials.CLAY, CShape.STICK, 6), 1L, 3L);
        h.simple(CAT_LATHE, matStack(CMaterials.CLAY, CShape.DISC), matStack(CMaterials.CLAY, CShape.RING), 1L, 2L);
        h.simple(CAT_LATHE, matStack(CMaterials.CLAY, CShape.SMALL_DISC), matStack(CMaterials.CLAY, CShape.SMALL_RING), 1L, 1L);
        h.simple(CAT_LATHE, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK, 2), matStack(CMaterials.DENSE_CLAY, CShape.CYLINDER), 1L, 4L);
        h.simple(CAT_LATHE, matStack(CMaterials.DENSE_CLAY, CShape.CYLINDER), matStack(CMaterials.DENSE_CLAY, CShape.NEEDLE), 1L, 6L);
        h.simple(CAT_LATHE, matStack(CMaterials.DENSE_CLAY, CShape.NEEDLE), matStack(CMaterials.DENSE_CLAY, CShape.STICK, 6), 1L, 6L);
        h.simple(CAT_LATHE, matStack(CMaterials.DENSE_CLAY, CShape.DISC), matStack(CMaterials.DENSE_CLAY, CShape.RING), 1L, 4L);
        h.simple(CAT_LATHE, matStack(CMaterials.DENSE_CLAY, CShape.SMALL_DISC), matStack(CMaterials.DENSE_CLAY, CShape.SMALL_RING), 1L, 2L);

        // Assembler shortcuts for Clay/Dense Clay components (L811-889)
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.CLAY, CShape.STICK, 5)), 3, List.of(matStack(CMaterials.CLAY, CShape.GEAR)), 10L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.CLAY, CShape.SHORT_STICK, 9)), 3, List.of(matStack(CMaterials.CLAY, CShape.GEAR)), 10L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.CLAY, CShape.LARGE_PLATE), counted(Items.CLAY_BALL, 8)), 3, List.of(matStack(CMaterials.CLAY, CShape.SPINDLE)), 10L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.CLAY, CShape.LARGE_PLATE), counted(Items.CLAY, 8)), 3, List.of(matStack(CMaterials.CLAY, CShape.GRINDING_HEAD)), 10L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.CLAY, CShape.LARGE_PLATE), counted(CMaterials.CLAY, CShape.PLATE, 8)), 3, List.of(matStack(CMaterials.CLAY, CShape.CUTTING_HEAD)), 10L, 20L);

        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.DENSE_CLAY, CShape.STICK, 5)), 3, List.of(matStack(CMaterials.DENSE_CLAY, CShape.GEAR)), 10L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.DENSE_CLAY, CShape.SHORT_STICK, 9)), 3, List.of(matStack(CMaterials.DENSE_CLAY, CShape.GEAR)), 10L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.DENSE_CLAY, CShape.LARGE_PLATE), counted(Items.CLAY_BALL, 8)), 3, List.of(matStack(CMaterials.DENSE_CLAY, CShape.SPINDLE)), 100L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.DENSE_CLAY, CShape.LARGE_PLATE), counted(CMaterials.DENSE_CLAY, CShape.BLOCK, 8)), 3, List.of(matStack(CMaterials.DENSE_CLAY, CShape.GRINDING_HEAD)), 100L, 20L);
        h.machine(CAT_ASSEMBLER, List.of(counted(CMaterials.DENSE_CLAY, CShape.LARGE_PLATE), counted(CMaterials.DENSE_CLAY, CShape.PLATE, 8)), 3, List.of(matStack(CMaterials.DENSE_CLAY, CShape.CUTTING_HEAD)), 100L, 20L);

        // Grinder recycling for Clay & Dense Clay parts (L896-915)
        for (CMaterial mat : new CMaterial[]{CMaterials.CLAY, CMaterials.DENSE_CLAY}) {
            int mul = mat == CMaterials.CLAY ? 1 : 4;
            h.simple(CAT_GRINDER, matStack(mat, CShape.PLATE), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.STICK, 4), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.SHORT_STICK, 8), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.RING, 4), matStack(mat, CShape.DUST, 5), 1L, 15L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.SMALL_RING, 8), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.GEAR, 8), matStack(mat, CShape.DUST, 9), 1L, 27L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.BLADE), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.NEEDLE), matStack(mat, CShape.DUST, 2), 1L, 6L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.DISC, 2), matStack(mat, CShape.DUST, 3), 1L, 9L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.SMALL_DISC, 4), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.CYLINDER), matStack(mat, CShape.DUST, 2), 1L, 6L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.PIPE), matStack(mat, CShape.DUST), 1L, 3L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.LARGE_PLATE), matStack(mat, CShape.DUST, 4), 1L, 12L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.GRINDING_HEAD), matStack(mat, CShape.DUST, 16), 1L, 48L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.BEARING, 4), matStack(mat, CShape.DUST, 5), 1L, 15L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.SPINDLE), matStack(mat, CShape.DUST, 4), 1L, 12L * mul);
            h.simple(CAT_GRINDER, matStack(mat, CShape.CUTTING_HEAD), matStack(mat, CShape.DUST, 9), 1L, 27L * mul);
        }

        // Decomposer & Centrifuge clay processing (L917-971)
        h.simple(CAT_DECOMPOSER, matStack(CMaterials.IND_CLAY, CShape.DUST), matStack(CMaterials.ENG_CLAY, CShape.DUST, 3), 1L, 60L);
        h.simple(CAT_DECOMPOSER, matStack(CMaterials.ADVIND_CLAY, CShape.DUST), 4, matStack(CMaterials.ENG_CLAY, CShape.DUST, 28), 1000L, 60L);

        h.machine(CAT_CENTRIFUGE, List.of(counted(CMaterials.CLAY, CShape.DUST, 9)), 0,
                List.of(matStack(CMaterials.DENSE_CLAY, CShape.DUST)), 4L, 20L);
        h.machine(CAT_CENTRIFUGE, List.of(counted(CMaterials.DENSE_CLAY, CShape.DUST, 2)), 0,
                List.of(matStack(CMaterials.CLAY, CShape.DUST, 9), matStack(CMaterials.CAL_CLAY, CShape.DUST, 1)), 4L, 20L);
        h.machine(CAT_CENTRIFUGE, List.of(counted(CMaterials.IND_CLAY, CShape.DUST, 2)), 0,
                List.of(matStack(CMaterials.ENG_CLAY, CShape.DUST, 12), matStack(CMaterials.CLAY, CShape.DUST, 8),
                        matStack(CMaterials.DENSE_CLAY, CShape.DUST, 8), matStack(CMaterials.IND_CLAY, CShape.DUST)),
                4L, 20L);
        h.machine(CAT_CENTRIFUGE, List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST, 2)), 4,
                List.of(matStack(CMaterials.ENG_CLAY, CShape.DUST, 64), matStack(CMaterials.CLAY, CShape.DUST, 64),
                        matStack(CMaterials.DENSE_CLAY, CShape.DUST, 64), matStack(CMaterials.IND_CLAY, CShape.DUST, 12)),
                10000L, 12L);

        // Silicon, Clay Steel, Clayium, Ultimate Alloy, Antimatter & Organic Clay synthesis (L972-1163)
        h.machine(CAT_CHEMICAL_REACTOR,
                List.of(countedTag(CMaterials.SALT, CShape.DUST, 2), counted(CMaterials.CAL_CLAY, CShape.DUST)), 0,
                List.of(matStack(CMaterials.CALCIUM_CHLORIDE, CShape.DUST), matStack(CMaterials.SODIUM_CARBONATE, CShape.DUST)),
                e(5), 120L);
        h.machine(CAT_CHEMICAL_REACTOR,
                List.of(counted(CMaterials.SODIUM_CARBONATE, CShape.DUST), counted(CMaterials.CLAY, CShape.DUST)), 0,
                List.of(matStack(CMaterials.QUARTZ, CShape.DUST)), e(4), 120L);
        h.machine(CAT_CHEMICAL_REACTOR,
                List.of(countedTag(CMaterials.QUARTZ, CShape.DUST), counted(Items.COAL)), 0,
                List.of(matStack(CMaterials.IMPURE_SILICON, CShape.INGOT)), e(4), 120L);
        h.machine(CAT_CHEMICAL_REACTOR,
                List.of(countedTag(CMaterials.QUARTZ, CShape.DUST), counted(Items.CHARCOAL)), 0,
                List.of(matStack(CMaterials.IMPURE_SILICON, CShape.INGOT)), e(4), 120L);
        h.machine(CAT_CHEMICAL_REACTOR,
                List.of(countedTag(CMaterials.SALT, CShape.DUST), counted(CMaterials.IND_CLAY, CShape.DUST)), 8,
                List.of(matStack(CMaterials.QUARTZ, CShape.DUST), matStack(CMaterials.CALCIUM_CHLORIDE, CShape.DUST)),
                e(10.0, 8), 1L);
        h.machine(CAT_CHEMICAL_REACTOR,
                List.of(countedTag(CMaterials.QUARTZ, CShape.DUST), counted(CMaterials.IND_CLAY, CShape.DUST)), 8,
                List.of(matStack(CMaterials.IMPURE_SILICON, CShape.INGOT)), e(10.0, 8), 1L);

        h.machine(CAT_BLAST_FURNACE,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST), counted(CMaterials.IMPURE_SILICON, CShape.INGOT)), 7,
                List.of(matStack(CMaterials.SILICON, CShape.INGOT)), e(7), 100L);

        // Dense clay -> impure silicon dust + aluminium dust (or impure aluminium dust in hardcore)
        h.machine(h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, false), CAT_CHEMICAL_REACTOR,
                List.of(counted(CMaterials.DENSE_CLAY, CShape.DUST)), 5,
                List.of(matStack(CMaterials.IMPURE_SILICON, CShape.DUST), matStack(CMaterials.ALUMINIUM, CShape.DUST)),
                e(5), 30L, 0);
        h.machine(h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, true), CAT_CHEMICAL_REACTOR,
                List.of(counted(CMaterials.DENSE_CLAY, CShape.DUST)), 5,
                List.of(matStack(CMaterials.IMPURE_SILICON, CShape.DUST), matStack(CMaterials.IMPURE_ALUMINIUM, CShape.DUST)),
                e(5), 30L, 0);

        h.machine(CAT_BLAST_FURNACE,
                List.of(counted(CMaterials.IND_CLAY, CShape.DUST, 2), counted(CMaterials.IMPURE_MANGANESE, CShape.DUST)), 6,
                List.of(matStack(CMaterials.CLAY_STEEL, CShape.INGOT, 2)), e(5.0, 6), 200L);
        h.machine(CAT_BLAST_FURNACE,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST), counted(CMaterials.IMPURE_MANGANESE, CShape.DUST)), 7,
                List.of(matStack(CMaterials.CLAY_STEEL, CShape.INGOT)), e(5.0, 7), 5L);
        h.machine(CAT_BLAST_FURNACE,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST), countedTag(CMaterials.MANGANESE, CShape.DUST)), 8,
                List.of(matStack(CMaterials.CLAY_STEEL, CShape.INGOT)), e(5.0, 8), 1L);

        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST, 8), countedTag(CMaterials.LITHIUM, CShape.DUST, 4)), 7,
                List.of(matStack(CMaterials.CLAYIUM, CShape.DUST, 8)), e(10.0, 7), 50000L);
        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST, 8), countedTag(CMaterials.HAFNIUM, CShape.DUST)), 7,
                List.of(matStack(CMaterials.CLAYIUM, CShape.DUST, 8)), e(10.0, 7), 500000L);
        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST, 8), countedTag(CMaterials.BARIUM, CShape.DUST)), 7,
                List.of(matStack(CMaterials.CLAYIUM, CShape.DUST, 8)), e(3.0, 7), 5000000L);
        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST, 8), countedTag(CMaterials.STRONTIUM, CShape.DUST)), 7,
                List.of(matStack(CMaterials.CLAYIUM, CShape.DUST, 8)), e(7), 50000000L);

        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ADVIND_CLAY, CShape.DUST), counted(CMaterials.IMPURE_ULTIMATE_ALLOY, CShape.INGOT)), 8,
                List.of(matStack(CMaterials.ULTIMATE_ALLOY, CShape.INGOT)), e(10.0, 8), 1000000000L);
        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ENG_CLAY, CShape.DUST, 8), countedTag(CMaterials.LITHIUM, CShape.DUST)), 7,
                List.of(matStack(CMaterials.EXC_CLAY, CShape.DUST, 4)), e(7), 2000000L);

        h.simple(CAT_GRINDER, blockItem("clay_tree_log"), 1, 5, matStack(CMaterials.ORG_CLAY, CShape.DUST), e(5), 200L);
        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.ORG_CLAY, CShape.DUST), counted(CMaterials.ADVIND_CLAY, CShape.DUST)), 10,
                List.of(matStack(CMaterials.ORG_CLAY, CShape.DUST, 2)), e(10), 1000000000000L);
        h.machine(CAT_REACTOR,
                List.of(counted(item("clay_soul")), counted(CMaterials.ADVIND_CLAY, CShape.DUST)), 11,
                List.of(matStack(CMaterials.ORG_CLAY, CShape.DUST, 2)), e(11), 100000000000000L);

        h.machine(CAT_REACTOR,
                List.of(counted(CMaterials.CLAYIUM, CShape.INGOT)), 9,
                List.of(stack("antimatter_seed")), e(9), 200000000000000L);
        h.simple(CAT_CA_CONDENSER, stack("antimatter_seed"), 9, matStack(CMaterials.ANTIMATTER, CShape.GEM), e(2.5, 9), 2000L);
        h.machine(CAT_CA_INJECTOR,
                List.of(counted(machineHull(10)), counted(CMaterials.ANTIMATTER, CShape.GEM, 8)), 10,
                List.of(stack("ca_collector")), e(2.0, 10), 4000L);
        h.simple(CAT_CA_REACTOR, matStack(CMaterials.ANTIMATTER, CShape.GEM), 10,
                matStack(CMaterials.PURE_ANTIMATTER, CShape.GEM), e(0.1, 10), 300L);

        for (int i = 0; i < 8; i++) {
            h.simple(CAT_CONDENSER,
                    matStack(COMPRESSED_PURE_ANTIMATTERS[i], CShape.GEM, 9), 10,
                    matStack(COMPRESSED_PURE_ANTIMATTERS[i + 1], CShape.GEM), e(9), 6L);
        }
    }

    private static void buildChemicalMetalSeparator(ClayiumRecipeHelper h) {
        List<ChemicalMetalSeparatorRecipe.WeightedOutput> weights = List.of(
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.DUST), 200),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_MAGNESIUM, CShape.DUST), 60),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_SODIUM, CShape.DUST), 40),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_LITHIUM, CShape.DUST), 7),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_ZIRCONIUM, CShape.DUST), 5),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_ZINC, CShape.DUST), 10),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_MANGANESE, CShape.DUST), 80),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_CALCIUM, CShape.DUST), 20),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_POTASSIUM, CShape.DUST), 15),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_NICKEL, CShape.DUST), 13),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_IRON, CShape.DUST), 9),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_BERYLLIUM, CShape.DUST), 8),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_LEAD, CShape.DUST), 6),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_HAFNIUM, CShape.DUST), 4),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_CHROME, CShape.DUST), 3),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_TITANIUM, CShape.DUST), 3),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_STRONTIUM, CShape.DUST), 2),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_BARIUM, CShape.DUST), 2),
                new ChemicalMetalSeparatorRecipe.WeightedOutput(matStack(CMaterials.IMPURE_COPPER, CShape.DUST), 1)
        );
        // Exact original TileChemicalMetalSeparator energy=5000L, time=40L, tier=6, sum weights=488
        h.output().accept(
                ClayiumMod.id("chemical_metal_separator/industrial_clay_dust"),
                new ChemicalMetalSeparatorRecipe(
                        counted(CMaterials.IND_CLAY, CShape.DUST),
                        6,
                        5000L,
                        40L,
                        weights
                ),
                null
        );
    }

    private static void buildMaterialProcessingRecipes(ClayiumRecipeHelper h) {
        // Clay Grinder & Condenser (L2529-2534)
        h.simple(CAT_GRINDER, Items.CLAY, 1, 0, matStack(CMaterials.CLAY, CShape.DUST), 1L, 3L);
        h.simple(CAT_GRINDER, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK), matStack(CMaterials.DENSE_CLAY, CShape.DUST), 1L, 6L);
        h.simple(CAT_GRINDER, matStack(CMaterials.IND_CLAY, CShape.BLOCK), matStack(CMaterials.IND_CLAY, CShape.DUST), 1L, 12L);
        h.simple(CAT_GRINDER, matStack(CMaterials.ADVIND_CLAY, CShape.BLOCK), 2, matStack(CMaterials.ADVIND_CLAY, CShape.DUST), 2L, 12L);
        h.simple(CAT_CONDENSER, matStack(CMaterials.CLAY, CShape.DUST), new ItemStack(Items.CLAY), 1L, 3L);
        h.simple(CAT_CONDENSER, matStack(CMaterials.DENSE_CLAY, CShape.DUST), matStack(CMaterials.DENSE_CLAY, CShape.BLOCK), 1L, 6L);

        // Main plateable alloys/metals Bending & Grinding (L2535-2631)
        CMaterial[] plateMetals = {
                CMaterials.IMPURE_SILICON, CMaterials.SILICON, CMaterials.SILICONE,
                CMaterials.ALUMINIUM, CMaterials.CLAY_STEEL, CMaterials.CLAYIUM,
                CMaterials.ULTIMATE_ALLOY, CMaterials.AZ91D_ALLOY, CMaterials.ZK60A_ALLOY
        };
        for (CMaterial mat : plateMetals) {
            long bendPlateTime = (long) ((int) (20.0F * mat.hardness()));
            long bendLargeTime = (long) ((int) (40.0F * mat.hardness()));
            long grindTime = (long) ((int) (80.0F * mat.hardness()));
            h.simpleMatTag(CAT_BENDING, mat, CShape.INGOT, 1, 4, matStack(mat, CShape.PLATE), e(4), bendPlateTime);
            h.simpleMatTag(CAT_BENDING, mat, CShape.PLATE, 4, 4, matStack(mat, CShape.LARGE_PLATE), e(4), bendLargeTime);
            h.simpleMatTag(CAT_GRINDER, mat, CShape.INGOT, 1, 0, matStack(mat, CShape.DUST), e(0.25, 4), grindTime);
            h.simpleMatTag(CAT_GRINDER, mat, CShape.PLATE, 1, 0, matStack(mat, CShape.DUST), e(0.25, 4), grindTime);
            h.simple(CAT_GRINDER, matStack(mat, CShape.LARGE_PLATE), matStack(mat, CShape.DUST, 4), e(0.25, 4), grindTime);
        }

        // Hardcore Aluminium conditional Bending & Grinding (L2598-2631)
        RecipeOutput hcAlOut = h.withConfig(ClayiumConfigCondition.HARDCORE_ALUMINIUM, true);
        long hcBend1 = (long) ((int) (20.0F * CMaterials.IMPURE_ALUMINIUM.hardness()));
        long hcBend2 = (long) ((int) (40.0F * CMaterials.IMPURE_ALUMINIUM.hardness()));
        long hcGrind = (long) ((int) (80.0F * CMaterials.IMPURE_ALUMINIUM.hardness()));
        h.machine(hcAlOut, CAT_BENDING, List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.INGOT)), 4, List.of(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.PLATE)), e(4), hcBend1, 0);
        h.machine(hcAlOut, CAT_BENDING, List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.PLATE, 4)), 4, List.of(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.LARGE_PLATE)), e(4), hcBend2, 0);
        h.machine(hcAlOut, CAT_GRINDER, List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.INGOT)), 0, List.of(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.DUST)), e(0.25, 4), hcGrind, 0);
        h.machine(hcAlOut, CAT_GRINDER, List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.PLATE)), 0, List.of(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.DUST)), e(0.25, 4), hcGrind, 0);
        h.machine(hcAlOut, CAT_GRINDER, List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.LARGE_PLATE)), 0, List.of(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.DUST, 4)), e(0.25, 4), hcGrind, 0);

        // 26 metals Ingot -> Dust (standalone: plates did not exist without IC2/GT) (L2634-2686)
        CMaterial[] grindableMetals = {
                CMaterials.IRON, CMaterials.GOLD, CMaterials.MAGNESIUM, CMaterials.SODIUM,
                CMaterials.LITHIUM, CMaterials.ZIRCONIUM, CMaterials.ZINC, CMaterials.MANGANESE,
                CMaterials.CALCIUM, CMaterials.POTASSIUM, CMaterials.NICKEL, CMaterials.BERYLLIUM,
                CMaterials.LEAD, CMaterials.HAFNIUM, CMaterials.CHROME, CMaterials.TITANIUM,
                CMaterials.STRONTIUM, CMaterials.BARIUM, CMaterials.COPPER,
                CMaterials.ZINCALMINIUM_ALLOY, CMaterials.ZINCONIUM_ALLOY,
                CMaterials.BRONZE, CMaterials.BRASS, CMaterials.ELECTRUM, CMaterials.INVAR, CMaterials.STEEL
        };
        for (CMaterial mat : grindableMetals) {
            long grindTime = (long) ((int) (80.0F * mat.hardness()));
            h.simpleMatTag(CAT_GRINDER, mat, CShape.INGOT, 1, 0, matStack(mat, CShape.DUST), e(0.25, 4), grindTime);
        }

        // Antimatter / Pure Antimatter / Octuple Clay / Octuple Pure Antimatter (L2858-2894)
        CMaterial[] highMats = {CMaterials.ANTIMATTER, CMaterials.PURE_ANTIMATTER, CMaterials.OCTUPLE_CLAY, CMaterials.OCTUPLE_PURE_ANTIMATTER};
        CShape[] highShapes = {CShape.GEM, CShape.GEM, CShape.BLOCK, CShape.GEM};
        for (int i = 0; i < highMats.length; i++) {
            CMaterial mat = highMats[i];
            CShape shape = highShapes[i];
            long b1 = (long) ((int) (20.0F * mat.hardness()));
            long b2 = (long) ((int) (40.0F * mat.hardness()));
            long gr = (long) ((int) (80.0F * mat.hardness()));
            h.simple(CAT_BENDING, matStack(mat, shape), 9, matStack(mat, CShape.PLATE), e(9 + i), b1);
            h.simple(CAT_BENDING, matStack(mat, CShape.PLATE, 4), 9, matStack(mat, CShape.LARGE_PLATE), e(9 + i), b2);
            h.simple(CAT_GRINDER, matStack(mat, shape), 10, matStack(mat, CShape.DUST), e(0.25, 9 + i), gr);
            h.simple(CAT_GRINDER, matStack(mat, CShape.PLATE), 10, matStack(mat, CShape.DUST), e(0.25, 9 + i), gr);
            h.simple(CAT_GRINDER, matStack(mat, CShape.LARGE_PLATE), 10, matStack(mat, CShape.DUST, 4), e(0.25, 9 + i), gr);
            h.simple(CAT_CONDENSER, matStack(mat, CShape.DUST), 10, matStack(mat, shape), e(0.25, 9 + i), gr);
        }

        // Smelter (L2914-2938)
        for (CMaterial mat : new CMaterial[]{CMaterials.ALUMINIUM, CMaterials.SODIUM, CMaterials.ZINC, CMaterials.NICKEL}) {
            h.simpleMatTag(CAT_SMELTER, mat, CShape.DUST, 1, 5, matStack(mat, CShape.INGOT), e(0.5, 5), 200L);
        }
        h.machine(hcAlOut, CAT_SMELTER, List.of(counted(CMaterials.IMPURE_ALUMINIUM, CShape.DUST)), 5,
                List.of(matStack(CMaterials.IMPURE_ALUMINIUM, CShape.INGOT)), e(0.5, 5), 200L, 0);

        for (CMaterial mat : new CMaterial[]{
                CMaterials.MAGNESIUM, CMaterials.LITHIUM, CMaterials.ZIRCONIUM,
                CMaterials.ZINCALMINIUM_ALLOY, CMaterials.ZINCONIUM_ALLOY,
                CMaterials.AZ91D_ALLOY, CMaterials.ZK60A_ALLOY}) {
            h.simpleMatTag(CAT_SMELTER, mat, CShape.DUST, 1, 6, matStack(mat, CShape.INGOT), e(0.2, 6), 400L);
        }

        // Blast Furnace (L2940-2985)
        for (CMaterial mat : new CMaterial[]{CMaterials.CALCIUM, CMaterials.POTASSIUM}) {
            h.simpleMatTag(CAT_BLAST_FURNACE, mat, CShape.DUST, 1, 5, matStack(mat, CShape.INGOT), e(5), 500L);
        }
        for (CMaterial mat : new CMaterial[]{CMaterials.BERYLLIUM, CMaterials.HAFNIUM, CMaterials.CLAY_STEEL, CMaterials.STEEL}) {
            h.simpleMatTag(CAT_BLAST_FURNACE, mat, CShape.DUST, 1, 6, matStack(mat, CShape.INGOT), e(6), 500L);
        }
        for (CMaterial mat : new CMaterial[]{CMaterials.MANGANESE, CMaterials.STRONTIUM, CMaterials.BARIUM, CMaterials.CLAYIUM}) {
            h.simpleMatTag(CAT_BLAST_FURNACE, mat, CShape.DUST, 1, 7, matStack(mat, CShape.INGOT), e(2.0, 7), 1000L);
        }
        for (CMaterial mat : new CMaterial[]{CMaterials.TITANIUM, CMaterials.ULTIMATE_ALLOY}) {
            h.simpleMatTag(CAT_BLAST_FURNACE, mat, CShape.DUST, 1, 8, matStack(mat, CShape.INGOT), e(4.0, 8), 2000L);
        }
        h.simpleMatTag(CAT_BLAST_FURNACE, CMaterials.CHROME, CShape.DUST, 1, 9, matStack(CMaterials.CHROME, CShape.INGOT), e(4.0, 9), 2000L);

        RecipeOutput hcOsOut = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, true);
        h.machine(hcOsOut, CAT_BLAST_FURNACE, List.of(countedTag(CMaterials.IMPURE_OSMIUM, CShape.INGOT)), 11,
                List.of(matStack(CMaterials.OSMIUM, CShape.INGOT)), e(4.0, 11), 100L, 0);

        // Gems & Fuel Reactor / Grinder (standalone: LAPIS & QUARTZ have dust forms; COAL & CHARCOAL also covered below) (L2987-3074)
        for (CMaterial mat : new CMaterial[]{CMaterials.LAPIS, CMaterials.QUARTZ}) {
            h.machine(CAT_REACTOR,
                    List.of(countedTag(mat, CShape.DUST), counted(CMaterials.IND_CLAY, CShape.DUST)), 7,
                    List.of(matStack(mat, CShape.GEM)), e(7), 10000L);
        }
        h.machine(CAT_REACTOR,
                List.of(countedTag(CMaterials.COAL, CShape.DUST), counted(CMaterials.IND_CLAY, CShape.DUST)), 7,
                List.of(new ItemStack(Items.COAL)), e(7), 10000L);
        h.machine(CAT_REACTOR,
                List.of(countedTag(CMaterials.CHARCOAL, CShape.DUST), counted(CMaterials.IND_CLAY, CShape.DUST)), 7,
                List.of(new ItemStack(Items.CHARCOAL)), e(7), 10000L);

        // Gem -> Dust (Quarz dust exists in Clayium; Lapis dust exists in Clayium; Coal/Charcoal dust exists in Clayium)
        h.simpleMatTag(CAT_GRINDER, CMaterials.QUARTZ, CShape.GEM, 1, 5, matStack(CMaterials.QUARTZ, CShape.DUST), e(0.25, 5), 80L);
        h.simpleMatTag(CAT_GRINDER, CMaterials.LAPIS, CShape.GEM, 1, 5, matStack(CMaterials.LAPIS, CShape.DUST), e(0.25, 5), 80L);
        h.simple(CAT_GRINDER, Items.COAL, 1, 5, matStack(CMaterials.COAL, CShape.DUST), e(0.25, 5), 80L);
        h.simple(CAT_GRINDER, Items.CHARCOAL, 1, 5, matStack(CMaterials.CHARCOAL, CShape.DUST), e(0.25, 5), 80L);

        // Clay Ore Grinder (L3102-3127)
        h.simpleTag(CAT_GRINDER, commonTag("ores/clay"), 1, 0, stack("compressed_clay_shard", 2), 1L, 3L);
        h.simpleTag(CAT_GRINDER, commonTag("ores/dense_clay"), 1, 0, stack("industrial_clay_shard", 3), 1L, 6L);
        h.simpleTag(CAT_GRINDER, commonTag("ores/large_dense_clay"), 1, 0, stack("advanced_industrial_clay_shard", 5), 1L, 9L);
        h.simple(CAT_CONDENSER, stack("compressed_clay_shard", 4), 0, compressedClay(1, 1), 1L, 3L);
        h.simple(CAT_CONDENSER, stack("industrial_clay_shard", 4), 0, compressedClay(2, 1), 1L, 6L);
        h.simple(CAT_CONDENSER, stack("advanced_industrial_clay_shard", 4), 0, compressedClay(3, 1), 1L, 9L);

        // Ore -> Dust / Gem Grinder (vanilla + Clayium-dust ores; omits GregTech granite/endstone prefixes & mod-only ores)
        CMaterial[] dustOres = {
                CMaterials.SILICON, CMaterials.ALUMINIUM, CMaterials.COAL, CMaterials.IRON,
                CMaterials.GOLD, CMaterials.MAGNESIUM, CMaterials.SODIUM, CMaterials.LITHIUM,
                CMaterials.ZIRCONIUM, CMaterials.ZINC, CMaterials.MANGANESE, CMaterials.CALCIUM,
                CMaterials.POTASSIUM, CMaterials.NICKEL, CMaterials.BERYLLIUM, CMaterials.LEAD,
                CMaterials.HAFNIUM, CMaterials.CHROME, CMaterials.TITANIUM, CMaterials.STRONTIUM,
                CMaterials.BARIUM, CMaterials.COPPER
        };
        for (CMaterial mat : dustOres) {
            h.simpleTag(CAT_GRINDER, oreTag(mat), 1, 5, matStack(mat, CShape.DUST, 2), e(0.025, 5), 80L);
        }
        for (CMaterial mat : new CMaterial[]{CMaterials.QUARTZ, CMaterials.DIAMOND, CMaterials.EMERALD}) {
            h.simpleTag(CAT_GRINDER, oreTag(mat), 1, 5, matStack(mat, CShape.GEM, 2), e(0.025, 5), 80L);
        }
        h.simpleTag(CAT_GRINDER, Tags.Items.ORES_LAPIS, 1, 5, new ItemStack(Items.LAPIS_LAZULI, 10), e(0.025, 5), 80L);
        h.simpleTag(CAT_GRINDER, Tags.Items.ORES_REDSTONE, 1, 5, new ItemStack(Items.REDSTONE, 10), e(0.025, 5), 80L);
        for (CMaterial mat : new CMaterial[]{CMaterials.SULFUR, CMaterials.SALTPETER}) {
            h.simpleTag(CAT_GRINDER, oreTag(mat), 1, 5, matStack(mat, CShape.DUST, 10), e(0.025, 5), 80L);
        }

        // Alloy Smelter (L3362-3448)
        h.machine(CAT_ALLOY_SMELTER,
                List.of(countedTag(CMaterials.ZINC, CShape.INGOT, 9), countedTag(CMaterials.ALUMINIUM, CShape.INGOT)), 6,
                List.of(matStack(CMaterials.ZINCALMINIUM_ALLOY, CShape.INGOT, 10)), e(6), 50L);
        h.machine(CAT_ALLOY_SMELTER,
                List.of(countedTag(CMaterials.MAGNESIUM, CShape.INGOT, 9), counted(CMaterials.ZINCALMINIUM_ALLOY, CShape.INGOT)), 6,
                List.of(matStack(CMaterials.AZ91D_ALLOY, CShape.INGOT, 10)), e(7), 500L);
        h.machine(CAT_ALLOY_SMELTER,
                List.of(countedTag(CMaterials.ZINC, CShape.INGOT, 9), countedTag(CMaterials.ZIRCONIUM, CShape.INGOT)), 6,
                List.of(matStack(CMaterials.ZINCONIUM_ALLOY, CShape.INGOT, 10)), e(3.0, 7), 50L);
        h.machine(CAT_ALLOY_SMELTER,
                List.of(countedTag(CMaterials.MAGNESIUM, CShape.INGOT, 19), counted(CMaterials.ZINCONIUM_ALLOY, CShape.INGOT)), 6,
                List.of(matStack(CMaterials.ZK60A_ALLOY, CShape.INGOT, 20)), e(3.0, 7), 500L);

        CShape[] shapePairs = {CShape.INGOT, CShape.DUST};
        for (CShape s1 : shapePairs) {
            for (CShape s2 : shapePairs) {
                if (hasMatItem(CMaterials.COPPER, s1) && hasMatItem(CMaterials.TIN, s2)) {
                    h.machine(CAT_ALLOY_SMELTER, List.of(countedTag(CMaterials.COPPER, s1, 3), countedTag(CMaterials.TIN, s2)), 0,
                            List.of(matStack(CMaterials.BRONZE, CShape.INGOT, 4)), e(4), 40L);
                }
                if (hasMatItem(CMaterials.COPPER, s1) && hasMatItem(CMaterials.ZINC, s2)) {
                    h.machine(CAT_ALLOY_SMELTER, List.of(countedTag(CMaterials.COPPER, s1, 3), countedTag(CMaterials.ZINC, s2)), 0,
                            List.of(matStack(CMaterials.BRASS, CShape.INGOT, 4)), e(4), 40L);
                }
                if (hasMatItem(CMaterials.GOLD, s1) && hasMatItem(CMaterials.SILVER, s2)) {
                    h.machine(CAT_ALLOY_SMELTER, List.of(countedTag(CMaterials.GOLD, s1), countedTag(CMaterials.SILVER, s2)), 0,
                            List.of(matStack(CMaterials.ELECTRUM, CShape.INGOT, 2)), e(4), 40L);
                }
                if (hasMatItem(CMaterials.IRON, s1) && hasMatItem(CMaterials.NICKEL, s2)) {
                    h.machine(CAT_ALLOY_SMELTER, List.of(countedTag(CMaterials.IRON, s1, 2), countedTag(CMaterials.NICKEL, s2)), 0,
                            List.of(matStack(CMaterials.INVAR, CShape.INGOT, 3)), e(4), 40L);
                }
            }
            h.machine(CAT_BLAST_FURNACE, List.of(countedTag(CMaterials.IRON, s1), counted(Items.COAL, 2)), 6,
                    List.of(matStack(CMaterials.STEEL, CShape.INGOT)), e(6), 500L);
        }

        // Stone / Gravel / Sand / Redstone / Glowstone / Diamonds / Miscellaneous (L3482-3617)
        h.simple(CAT_GRINDER, Items.STONE, 1, 0, new ItemStack(Items.COBBLESTONE), 1L, 3L);
        h.simple(CAT_GRINDER, Items.COBBLESTONE, 1, 0, new ItemStack(Items.GRAVEL), 1L, 10L);
        h.simple(CAT_GRINDER, Items.COBBLESTONE, 4, 0, new ItemStack(Items.GRAVEL, 4), 1L, 10L);
        h.simple(CAT_GRINDER, Items.COBBLESTONE, 16, 0, new ItemStack(Items.GRAVEL, 16), 1L, 10L);
        h.simple(CAT_GRINDER, Items.COBBLESTONE, 64, 0, new ItemStack(Items.GRAVEL, 64), 1L, 10L);
        h.simple(CAT_GRINDER, Items.GRAVEL, 1, 0, new ItemStack(Items.SAND), 10L, 10L);

        h.simple(CAT_CENTRIFUGE, Items.GRAVEL, 1, 4, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK, 1), 1L, 2L);
        h.simple(CAT_CENTRIFUGE, Items.GRAVEL, 4, 4, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK, 4), 2L, 2L);
        h.simple(CAT_CENTRIFUGE, Items.GRAVEL, 16, 5, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK, 16), 4L, 1L);
        h.simple(CAT_CENTRIFUGE, Items.GRAVEL, 64, 6, matStack(CMaterials.DENSE_CLAY, CShape.BLOCK, 64), 8L, 1L);

        h.machine(CAT_REACTOR, List.of(counted(CMaterials.ORG_CLAY, CShape.DUST), counted(Items.GRAVEL)), 7, List.of(new ItemStack(Items.DIRT)), e(7), 100L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.GRAVEL), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.DIRT)), e(2.0, 10), 60L);
        h.simpleTag(CAT_GRINDER, Tags.Items.SANDSTONE_BLOCKS, 1, 3, new ItemStack(Items.SAND, 4), e(3), 100L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.SAND), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.RED_SAND)), e(2.0, 10), 60L);

        h.machine(CAT_CHEMICAL_REACTOR, List.of(counted(CMaterials.ENG_CLAY, CShape.DUST)), 5,
                List.of(matStack(CMaterials.IMPURE_REDSTONE, CShape.DUST), matStack(CMaterials.IMPURE_GLOWSTONE, CShape.DUST)), e(5), 10L);
        h.machine(CAT_REACTOR, List.of(counted(CMaterials.IMPURE_REDSTONE, CShape.DUST)), 7, List.of(new ItemStack(Items.REDSTONE)), e(0.1, 7), 2000L);
        h.machine(CAT_REACTOR, List.of(counted(CMaterials.IMPURE_GLOWSTONE, CShape.DUST)), 7, List.of(new ItemStack(Items.GLOWSTONE_DUST)), e(0.1, 7), 2000L);
        h.machine(CAT_CA_INJECTOR, List.of(countedTag(CMaterials.REDSTONE, CShape.DUST), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.OBSIDIAN)), e(2.0, 10), 60L);

        h.simpleTag(CAT_CONDENSER, commonTag("storage_blocks/coal"), 8, 5, new ItemStack(Items.DIAMOND), e(5), 100L);
        h.machine(CAT_REACTOR, List.of(counted(Items.COAL, 64), counted(CMaterials.IND_CLAY, CShape.DUST)), 7, List.of(new ItemStack(Items.DIAMOND)), e(7), 10000L);
        h.machine(CAT_REACTOR, List.of(counted(CMaterials.ORG_CLAY, CShape.DUST), countedTag(CMaterials.POTASSIUM, CShape.DUST)), 8,
                List.of(matStack(CMaterials.SALTPETER, CShape.DUST)), e(8), 10000L);

        h.simpleTag(CAT_GRINDER, ItemTags.WOOL, 1, 5, new ItemStack(Items.STRING, 4), e(5), 100L);
        h.simpleTag(CAT_GRINDER, ItemTags.WOOL_CARPETS, 1, 5, new ItemStack(Items.STRING, 2), e(5), 100L);
        h.simple(CAT_GRINDER, Items.BLAZE_ROD, 1, 5, new ItemStack(Items.BLAZE_POWDER, 5), e(5), 100L);
        h.simple(CAT_GRINDER, Items.BONE, 1, 5, new ItemStack(Items.BONE_MEAL, 5), e(5), 100L);
        h.simple(CAT_GRINDER, Items.PUMPKIN, 1, 5, new ItemStack(Items.PUMPKIN_PIE, 9), e(5), 100L);

        h.machine(CAT_CENTRIFUGE, List.of(counted(blockItem("clay_tree_log"))), 6,
                List.of(matStack(CMaterials.ADVIND_CLAY, CShape.DUST, 16), matStack(CMaterials.MANGANESE, CShape.DUST, 5),
                        matStack(CMaterials.LITHIUM, CShape.DUST, 3), matStack(CMaterials.ZIRCONIUM, CShape.DUST, 1)),
                10000L, 400L);
        h.machine(CAT_ASSEMBLER, List.of(counted(Items.STONE_BRICKS), counted(Items.VINE)), 6,
                List.of(new ItemStack(Items.MOSSY_STONE_BRICKS)), e(6), 20L);
        h.machine(CAT_REACTOR, List.of(countedTag(CMaterials.ULTIMATE_ALLOY, CShape.DUST, 4), counted(Items.GLASS_BOTTLE)), 11,
                List.of(new ItemStack(Items.EXPERIENCE_BOTTLE)), e(11), 100000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(Items.POTATO)), 11,
                List.of(new ItemStack(Items.POISONOUS_POTATO)), e(9), 10000000000L);

        // Electrolysis Reactor 19 impure metals (L3637-3686)
        CMaterial[][] elecTier6 = {
                {CMaterials.IMPURE_ALUMINIUM, CMaterials.ALUMINIUM},
                {CMaterials.IMPURE_MAGNESIUM, CMaterials.MAGNESIUM},
                {CMaterials.IMPURE_SODIUM, CMaterials.SODIUM},
                {CMaterials.IMPURE_LITHIUM, CMaterials.LITHIUM},
                {CMaterials.IMPURE_ZIRCONIUM, CMaterials.ZIRCONIUM},
                {CMaterials.IMPURE_ZINC, CMaterials.ZINC}
        };
        for (CMaterial[] pair : elecTier6) {
            h.simple(CAT_ELECTROLYSIS, matStack(pair[0], CShape.DUST), 6, matStack(pair[1], CShape.DUST), e(6), 100L);
        }
        CMaterial[][] elecTier7 = {
                {CMaterials.IMPURE_MANGANESE, CMaterials.MANGANESE},
                {CMaterials.IMPURE_POTASSIUM, CMaterials.POTASSIUM},
                {CMaterials.IMPURE_HAFNIUM, CMaterials.HAFNIUM},
                {CMaterials.IMPURE_STRONTIUM, CMaterials.STRONTIUM},
                {CMaterials.IMPURE_BARIUM, CMaterials.BARIUM},
                {CMaterials.IMPURE_CALCIUM, CMaterials.CALCIUM}
        };
        for (CMaterial[] pair : elecTier7) {
            h.simple(CAT_ELECTROLYSIS, matStack(pair[0], CShape.DUST), 7, matStack(pair[1], CShape.DUST), e(10.0, 7), 300L);
        }
        CMaterial[][] elecTier8 = {
                {CMaterials.IMPURE_IRON, CMaterials.IRON},
                {CMaterials.IMPURE_LEAD, CMaterials.LEAD},
                {CMaterials.IMPURE_COPPER, CMaterials.COPPER}
        };
        for (CMaterial[] pair : elecTier8) {
            h.simple(CAT_ELECTROLYSIS, matStack(pair[0], CShape.DUST), 8, matStack(pair[1], CShape.DUST), e(10.0, 8), 1000L);
        }
        CMaterial[][] elecTier9 = {
                {CMaterials.IMPURE_NICKEL, CMaterials.NICKEL},
                {CMaterials.IMPURE_BERYLLIUM, CMaterials.BERYLLIUM},
                {CMaterials.IMPURE_CHROME, CMaterials.CHROME},
                {CMaterials.IMPURE_TITANIUM, CMaterials.TITANIUM}
        };
        for (CMaterial[] pair : elecTier9) {
            h.simple(CAT_ELECTROLYSIS, matStack(pair[0], CShape.DUST), 9, matStack(pair[1], CShape.DUST), e(10.0, 9), 3000L);
        }
    }

    private static void buildMatterTransformerAndCAInjectorChains(ClayiumRecipeHelper h) {
        // Biological CA Injector chain (L3688-3747)
        h.machine(CAT_CA_INJECTOR, List.of(counted(CMaterials.ORG_CLAY, CShape.DUST), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.WHEAT_SEEDS)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.WHEAT_SEEDS), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.DANDELION)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.DANDELION), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.APPLE)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.APPLE), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.SUGAR_CANE)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.SUGAR_CANE), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.OAK_SAPLING)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.OAK_SAPLING), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.OAK_LEAVES)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.OAK_LEAVES), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.OAK_LOG)), e(2.0, 10), 60L);

        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.GRASS_BLOCK), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.SHORT_GRASS)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.MYCELIUM), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.BROWN_MUSHROOM)), e(2.0, 10), 60L);
        h.machine(CAT_REACTOR, List.of(countedTag(CMaterials.CARBON, CShape.DUST), counted(CMaterials.ORG_CLAY, CShape.DUST)), 11, List.of(new ItemStack(Items.ROTTEN_FLESH)), e(11), 100000000000000L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.ROTTEN_FLESH), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.LEATHER)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.LEATHER), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.BONE)), e(2.0, 10), 60L);
        h.machine(CAT_CA_INJECTOR, List.of(counted(Items.BONE), counted(CMaterials.ANTIMATTER, CShape.GEM)), 10, List.of(new ItemStack(Items.SUGAR)), e(2.0, 10), 60L);

        // Element transmutations (Matter Transformer L3748-3886)
        transmute(h, CMaterials.LITHIUM, CShape.INGOT, CMaterials.SODIUM, 7, 10.0);
        transmute(h, CMaterials.SODIUM, CShape.INGOT, CMaterials.POTASSIUM, 7, 30.0);
        transmute(h, CMaterials.POTASSIUM, CShape.INGOT, CMaterials.RUBIDIUM, 8, 10.0);
        transmute(h, CMaterials.RUBIDIUM, CShape.INGOT, CMaterials.CAESIUM, 8, 20.0);
        transmute(h, CMaterials.CAESIUM, CShape.INGOT, CMaterials.FRANCIUM, 8, 30.0);
        transmute(h, CMaterials.FRANCIUM, CShape.INGOT, CMaterials.RADIUM, 8, 50.0);
        transmute(h, CMaterials.RADIUM, CShape.INGOT, CMaterials.ACTINIUM, 9, 10.0);
        transmute(h, CMaterials.ACTINIUM, CShape.INGOT, CMaterials.THORIUM, 9, 20.0);
        transmute(h, CMaterials.THORIUM, CShape.INGOT, CMaterials.PROTACTINIUM, 9, 30.0);
        transmute(h, CMaterials.PROTACTINIUM, CShape.INGOT, CMaterials.URANIUM, 9, 50.0);
        transmute(h, CMaterials.URANIUM, CShape.INGOT, CMaterials.NEPTUNIUM, 9, 80.0);
        transmute(h, CMaterials.NEPTUNIUM, CShape.INGOT, CMaterials.PLUTONIUM, 10, 20.0);
        transmute(h, CMaterials.PLUTONIUM, CShape.INGOT, CMaterials.AMERICIUM, 11, 30.0);
        transmute(h, CMaterials.AMERICIUM, CShape.INGOT, CMaterials.CURIUM, 12, 50.0);

        transmute(h, CMaterials.BERYLLIUM, CShape.INGOT, CMaterials.MAGNESIUM, 7, 10.0);
        transmute(h, CMaterials.MAGNESIUM, CShape.INGOT, CMaterials.CALCIUM, 7, 20.0);
        transmute(h, CMaterials.CALCIUM, CShape.INGOT, CMaterials.STRONTIUM, 7, 30.0);
        transmute(h, CMaterials.STRONTIUM, CShape.INGOT, CMaterials.BARIUM, 7, 50.0);
        transmute(h, CMaterials.BARIUM, CShape.INGOT, CMaterials.LANTHANUM, 8, 10.0);
        transmute(h, CMaterials.LANTHANUM, CShape.INGOT, CMaterials.CERIUM, 8, 30.0);
        transmute(h, CMaterials.CERIUM, CShape.INGOT, CMaterials.PRASEODYMIUM, 8, 90.0);
        transmute(h, CMaterials.PRASEODYMIUM, CShape.INGOT, CMaterials.NEODYMIUM, 9, 20.0);
        transmute(h, CMaterials.NEODYMIUM, CShape.INGOT, CMaterials.PROMETHIUM, 10, 10.0);
        transmute(h, CMaterials.PROMETHIUM, CShape.INGOT, CMaterials.SAMARIUM, 11, 20.0);
        transmute(h, CMaterials.SAMARIUM, CShape.INGOT, CMaterials.EUROPIUM, 12, 60.0);

        transmute(h, CMaterials.ZIRCONIUM, CShape.INGOT, CMaterials.TITANIUM, 8, 60.0);
        transmute(h, CMaterials.TITANIUM, CShape.INGOT, CMaterials.VANADIUM, 9, 60.0);
        transmute(h, CMaterials.MANGANESE, CShape.INGOT, CMaterials.IRON, 7, 90.0);
        transmute(h, CMaterials.IRON, CShape.INGOT, CMaterials.COBALT, 8, 30.0);
        transmute(h, CMaterials.COBALT, CShape.INGOT, CMaterials.NICKEL, 8, 90.0);
        transmute(h, CMaterials.NICKEL, CShape.INGOT, CMaterials.PALLADIUM, 9, 40.0);

        transmute(h, CMaterials.ZINC, CShape.INGOT, CMaterials.COPPER, 8, 20.0);
        transmute(h, CMaterials.COPPER, CShape.INGOT, CMaterials.SILVER, 9, 10.0);
        transmute(h, CMaterials.SILVER, CShape.INGOT, CMaterials.GOLD, 9, 50.0);
        transmute(h, CMaterials.GOLD, CShape.INGOT, CMaterials.PLATINUM, 10, 30.0);
        transmute(h, CMaterials.PLATINUM, CShape.INGOT, CMaterials.IRIDIUM, 11, 10.0);

        // Hardcore osmium conditional steps around Osmium -> Rhenium
        RecipeOutput normalOs = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, false);
        RecipeOutput hcOs = h.withConfig(ClayiumConfigCondition.HARDCORE_OSMIUM, true);
        h.machine(normalOs, CAT_TRANSFORMER, List.of(countedTag(CMaterials.IRIDIUM, CShape.INGOT)), 11, List.of(matStack(CMaterials.OSMIUM, CShape.INGOT)), e(30.0, 11), 200L, 0);
        h.machine(hcOs, CAT_TRANSFORMER, List.of(countedTag(CMaterials.IRIDIUM, CShape.INGOT)), 11, List.of(matStack(CMaterials.IMPURE_OSMIUM, CShape.INGOT)), e(30.0, 11), 200L, 0);
        h.machine(normalOs, CAT_TRANSFORMER, List.of(countedTag(CMaterials.OSMIUM, CShape.INGOT)), 12, List.of(matStack(CMaterials.RHENIUM, CShape.INGOT)), e(10.0, 12), 200L, 0);
        h.machine(hcOs, CAT_TRANSFORMER, List.of(countedTag(CMaterials.IMPURE_OSMIUM, CShape.INGOT)), 12, List.of(matStack(CMaterials.RHENIUM, CShape.INGOT)), e(10.0, 12), 200L, 0);

        transmute(h, CMaterials.HAFNIUM, CShape.INGOT, CMaterials.TANTALUM, 8, 70.0);
        transmute(h, CMaterials.TANTALUM, CShape.INGOT, CMaterials.TUNGSTEN, 9, 40.0);
        transmute(h, CMaterials.TUNGSTEN, CShape.INGOT, CMaterials.MOLYBDENUM, 10, 20.0);
        transmute(h, CMaterials.MOLYBDENUM, CShape.INGOT, CMaterials.CHROME, 11, 10.0);

        transmute(h, CMaterials.LEAD, CShape.INGOT, CMaterials.TIN, 7, 50.0);
        transmute(h, CMaterials.TIN, CShape.INGOT, CMaterials.ANTIMONY, 8, 20.0);
        transmute(h, CMaterials.ANTIMONY, CShape.INGOT, CMaterials.BISMUTH, 9, 10.0);

        transmute(h, CMaterials.SILICON, CShape.DUST, CMaterials.PHOSPHORUS, 7, 10.0);
        transmute(h, CMaterials.PHOSPHORUS, CShape.DUST, CMaterials.SULFUR, 7, 30.0);

        // OD Chain Recipes (L3887-3913): IndClay -> Carbon -> Charcoal -> Coal -> Lapis (other gems unavailable standalone)
        h.registerODChainRecipes(CAT_TRANSFORMER,
                new CMaterial[]{
                        CMaterials.IND_CLAY, CMaterials.CARBON, CMaterials.GRAPHITE,
                        CMaterials.CHARCOAL, CMaterials.COAL, CMaterials.LAPIS,
                        CMaterials.LAZURITE, CMaterials.SODALITE, CMaterials.MONAZITE
                },
                CShape.DUST,
                new int[]{8, 8, 8, 4, 4, 4, 4, 4, 1},
                new int[]{0, 7, 8, 9, 10, 10, 10, 10, 11},
                200);
        h.registerODChainRecipes(CAT_TRANSFORMER,
                new CMaterial[]{
                        CMaterials.DIAMOND, CMaterials.AMBER, CMaterials.AMETHYST,
                        CMaterials.PERIDOT, CMaterials.SAPPHIRE, CMaterials.RUBY, CMaterials.EMERALD
                },
                CShape.GEM,
                new int[]{0, 10, 10, 10, 10, 10, 11},
                200);

        // Vanilla block/plant/mob transmutation chains (L3914-4065)
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.COBBLESTONE, Items.NETHERRACK, Items.MOSSY_COBBLESTONE), new int[]{0, 9, 11}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS), new int[]{0, 8, 8}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.DIRT, Items.PODZOL, Items.GRASS_BLOCK, Items.MYCELIUM, Items.SOUL_SAND), new int[]{0, 7, 8, 9, 10}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG),
                new int[]{0, 7, 7, 8, 8, 8}, 80);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.OAK_LEAVES, Items.SPRUCE_LEAVES, Items.BIRCH_LEAVES, Items.JUNGLE_LEAVES, Items.ACACIA_LEAVES, Items.DARK_OAK_LEAVES),
                new int[]{0, 7, 7, 8, 8, 8}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING),
                new int[]{0, 7, 7, 8, 8, 8}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.COCOA_BEANS, Items.NETHER_WART),
                new int[]{0, 8, 8, 8, 8}, 20);

        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.WHEAT, Items.CARROT, Items.POTATO), new int[]{0, 8, 8}, 20);
        h.machine(CAT_REACTOR, List.of(counted(Items.WHEAT_SEEDS)), 10, List.of(new ItemStack(Items.WHEAT)), e(8), 2000000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(Items.MELON_SEEDS)), 10, List.of(new ItemStack(Items.CACTUS)), e(8), 2000000000000000L);
        h.machine(CAT_REACTOR, List.of(counted(Items.PUMPKIN_SEEDS)), 10, List.of(new ItemStack(Items.PUMPKIN)), e(8), 2000000000000000L);

        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.SHORT_GRASS, Items.FERN, Items.DEAD_BUSH, Items.VINE, Items.LILY_PAD),
                new int[]{0, 7, 7, 8, 9}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(
                        Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM, Items.AZURE_BLUET,
                        Items.RED_TULIP, Items.ORANGE_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP, Items.OXEYE_DAISY,
                        Items.SUNFLOWER, Items.LILAC, Items.TALL_GRASS, Items.LARGE_FERN, Items.ROSE_BUSH, Items.PEONY
                ),
                new int[]{0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.SUGAR_CANE, Items.MELON), new int[]{0, 8}, 20);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.ROTTEN_FLESH, Items.PORKCHOP, Items.BEEF, Items.SPIDER_EYE),
                new int[]{64, 2, 2, 1}, new int[]{0, 9, 9, 9}, 200);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.LEATHER, Items.WHITE_WOOL, Items.FEATHER),
                new int[]{1, 4, 16}, new int[]{0, 9, 9}, 80);

        h.machine(CAT_ASSEMBLER, List.of(counted(Items.LEATHER, 4), counted(Items.STRING, 16)), 10, List.of(new ItemStack(Items.SADDLE)), e(10), 6000L);
        h.machine(CAT_ASSEMBLER, List.of(counted(Items.PAPER, 2), counted(Items.STRING, 4)), 10, List.of(new ItemStack(Items.LEAD)), e(10), 600L);

        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.BONE, Items.BLAZE_ROD, Items.ENDER_PEARL, Items.NETHER_STAR, Items.DRAGON_EGG),
                new int[]{262144, 4096, 1024, 64, 1}, new int[]{0, 9, 9, 12, 12}, 200);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.SUGAR, Items.SLIME_BALL, Items.INK_SAC, Items.ENDER_EYE, Items.GHAST_TEAR),
                new int[]{0, 8, 8, 9, 10}, 100);
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.WITHER_SKELETON_SKULL, Items.SKELETON_SKULL, Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.PLAYER_HEAD),
                new int[]{0, 11, 11, 11, 12}, 1000);
        // gravel -> egg (cinnabar excluded as non-vanilla external mod material)
        h.registerStackChainRecipes(CAT_TRANSFORMER,
                stacks(Items.GRAVEL, Items.EGG),
                new int[]{0, 7}, 1000);
    }

    private static void transmute(ClayiumRecipeHelper h, CMaterial from, CShape shape, CMaterial to, int tier, double energyFactor) {
        h.simpleMatTag(CAT_TRANSFORMER, from, shape, 1, tier, matStack(to, shape), e(energyFactor, tier), 200L);
    }

    private static ItemStack[] stacks(net.minecraft.world.level.ItemLike... items) {
        ItemStack[] out = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            out[i] = new ItemStack(items[i]);
        }
        return out;
    }
}
