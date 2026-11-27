package net.claustra01.clayium.datagen.recipe;

import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Exact 16-entry port of 1.7.10 ClayWorkTableRecipes.
 */
public final class ClayWorkTableRecipeGenerator {
    private ClayWorkTableRecipeGenerator() {}

    public static void build(ClayiumRecipeHelper h) {
        h.workTable("clay_stick",
                h.counted(Items.CLAY_BALL, 1), 1,
                h.matStack(CMaterials.CLAY, CShape.STICK), ItemStack.EMPTY, 4);

        h.workTable("clay_disc_by_hand",
                h.counted(CMaterials.CLAY, CShape.LARGE_BALL, 1), 2,
                h.matStack(CMaterials.CLAY, CShape.DISC), ItemStack.EMPTY, 30);

        h.workTable("clay_disc_by_rolling_pin",
                h.counted(CMaterials.CLAY, CShape.LARGE_BALL, 1), 3,
                h.matStack(CMaterials.CLAY, CShape.DISC), new ItemStack(Items.CLAY_BALL, 2), 4);

        h.workTable("clay_cylinder",
                h.counted(CMaterials.CLAY, CShape.LARGE_BALL, 1), 1,
                h.matStack(CMaterials.CLAY, CShape.CYLINDER), ItemStack.EMPTY, 4);

        h.workTable("clay_blade_by_hand",
                h.counted(CMaterials.CLAY, CShape.PLATE, 1), 2,
                h.matStack(CMaterials.CLAY, CShape.BLADE), ItemStack.EMPTY, 10);

        h.workTable("clay_blade_by_rolling_pin",
                h.counted(CMaterials.CLAY, CShape.PLATE, 1), 3,
                h.matStack(CMaterials.CLAY, CShape.BLADE), new ItemStack(Items.CLAY_BALL, 2), 1);

        h.workTable("clay_sticks_from_plate",
                h.counted(CMaterials.CLAY, CShape.PLATE, 1), 6,
                h.matStack(CMaterials.CLAY, CShape.STICK, 4), ItemStack.EMPTY, 3);

        h.workTable("clay_plate_from_disc",
                h.counted(CMaterials.CLAY, CShape.DISC, 1), 4,
                h.matStack(CMaterials.CLAY, CShape.PLATE), new ItemStack(Items.CLAY_BALL, 2), 4);

        h.workTable("clay_ring_from_disc",
                h.counted(CMaterials.CLAY, CShape.DISC, 1), 5,
                h.matStack(CMaterials.CLAY, CShape.RING), h.matStack(CMaterials.CLAY, CShape.SMALL_DISC), 2);

        h.workTable("clay_small_ring_from_small_disc",
                h.counted(CMaterials.CLAY, CShape.SMALL_DISC, 1), 5,
                h.matStack(CMaterials.CLAY, CShape.SMALL_RING), h.matStack(CMaterials.CLAY, CShape.SHORT_STICK), 1);

        h.workTable("clay_needle",
                h.counted(CMaterials.CLAY, CShape.CYLINDER, 1), 1,
                h.matStack(CMaterials.CLAY, CShape.NEEDLE), ItemStack.EMPTY, 3);

        h.workTable("clay_small_disc_from_cylinder",
                h.counted(CMaterials.CLAY, CShape.CYLINDER, 1), 6,
                h.matStack(CMaterials.CLAY, CShape.SMALL_DISC, 8), ItemStack.EMPTY, 7);

        h.workTable("raw_clay_slicer_by_hand",
                h.counted(CMaterials.CLAY, CShape.DISC, 1), 2,
                ClayiumRecipeHelper.stack("raw_clay_slicer"), ItemStack.EMPTY, 15);

        h.workTable("raw_clay_slicer_by_rolling_pin",
                h.counted(CMaterials.CLAY, CShape.DISC, 1), 3,
                ClayiumRecipeHelper.stack("raw_clay_slicer"), ItemStack.EMPTY, 2);

        h.workTable("clay_large_plate",
                h.counted(CMaterials.CLAY, CShape.PLATE, 6), 3,
                h.matStack(CMaterials.CLAY, CShape.LARGE_PLATE), ItemStack.EMPTY, 10);

        h.workTable("clay_large_ball_from_plates",
                h.counted(CMaterials.CLAY, CShape.PLATE, 3), 1,
                h.matStack(CMaterials.CLAY, CShape.LARGE_BALL), ItemStack.EMPTY, 40);
    }
}
