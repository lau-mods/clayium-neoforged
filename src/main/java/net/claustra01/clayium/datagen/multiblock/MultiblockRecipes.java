package net.claustra01.clayium.datagen.multiblock;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeBuilder;
import net.claustra01.clayium.multiblock.MultiblockMachineType;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class MultiblockRecipes {
    public static void buildRecipes(RecipeOutput output) {
        for (String material : new String[] {"calcium", "potassium"}) {
            dust(output, material, 5, 1.0D, 500);
        }
        for (String material : new String[] {"beryllium", "hafnium", "clay_steel", "steel"}) {
            dust(output, material, 6, 1.0D, 500);
        }
        for (String material : new String[] {"manganese", "strontium", "barium", "clayium"}) {
            dust(output, material, 7, 2.0D, 1_000);
        }
        for (String material : new String[] {"titanium", "ultimate_alloy"}) {
            dust(output, material, 8, 4.0D, 2_000);
        }
        dust(output, "chrome", 9, 4.0D, 2_000);
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.BLAST_FURNACE, 6)
                .requires(Items.IRON_INGOT).requires(Items.COAL, 2)
                .output(material("steel_ingot"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 6)).processingTicks(500)
                .save(output, ClayiumMod.id("blast_furnace/steel_from_iron"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.BLAST_FURNACE, 6)
                .requires(material("industrial_clay_dust"), 2)
                .requires(material("impure_manganese_dust"))
                .output(material("clay_steel_ingot"), 2)
                .clayEnergy(ClayiumRecipeEnergy.e(5.0D, 6)).processingTicks(200)
                .save(output, ClayiumMod.id("blast_furnace/clay_steel_impure_manganese"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.BLAST_FURNACE, 7)
                .requires(material("advanced_industrial_clay_dust"))
                .requires(material("impure_manganese_dust"))
                .output(material("clay_steel_ingot"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(5.0D, 7)).processingTicks(5)
                .save(output, ClayiumMod.id("blast_furnace/clay_steel_advanced"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.BLAST_FURNACE, 8)
                .requires(material("advanced_industrial_clay_dust"))
                .requires(material("manganese_dust"))
                .output(material("clay_steel_ingot"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(5.0D, 8)).processingTicks(1)
                .save(output, ClayiumMod.id("blast_furnace/clay_steel_manganese"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 4)
                .requires(MachineRegistration.MACHINES.get("smelter_tier_6").block().get())
                .requires(InterfaceRegistration.INTERFACES.get("clay_interface_tier_6").block().get())
                .output(MultiblockRegistration.CONTROLLERS.get(MultiblockMachineType.BLAST_FURNACE).get(), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 6)).processingTicks(120)
                .save(output, ClayiumMod.id("multiblock/clay_blast_furnace"));
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.ASSEMBLER, 6)
                .requires(AssemblyMachineRegistration.MACHINE_HULLS.get(7).get())
                .requires(InterfaceRegistration.INTERFACES.get("clay_laser_interface_tier_7").block().get())
                .output(MultiblockRegistration.CONTROLLERS.get(MultiblockMachineType.REACTOR).get(), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(1.0D, 7)).processingTicks(1_200)
                .save(output, ClayiumMod.id("multiblock/clay_reactor"));
    }

    private static Item material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static void dust(RecipeOutput output, String material, int tier, double energy, int ticks) {
        ClayiumRecipeBuilder.machine(ClayiumRecipeMethod.BLAST_FURNACE, tier)
                .requires(material(material + "_dust"))
                .output(material(material + "_ingot"), 1)
                .clayEnergy(ClayiumRecipeEnergy.e(energy, tier)).processingTicks(ticks)
                .save(output, ClayiumMod.id("blast_furnace/" + material));
    }

    private MultiblockRecipes() {
    }
}
