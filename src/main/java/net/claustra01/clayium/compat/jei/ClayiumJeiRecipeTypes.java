/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.compat.jei;

import java.util.LinkedHashMap;
import java.util.Map;
import mezz.jei.api.recipe.RecipeType;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.machine.ClayiumMachineIds;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.minecraft.resources.ResourceLocation;

public final class ClayiumJeiRecipeTypes {
    public static final RecipeType<ClayWorkTableRecipe> CLAY_WORK_TABLE =
            RecipeType.create(Clayium.MODID, "clay_work_table", ClayWorkTableRecipe.class);
    public static final RecipeType<MachineRecipe> CLAY_BENDING_MACHINE = machine("clay_bending_machine");
    public static final RecipeType<MachineRecipe> ELEMENTAL_MILLING_MACHINE = machine("elemental_milling_machine");
    public static final RecipeType<MachineRecipe> GRINDER = machine("grinder");
    public static final RecipeType<MachineRecipe> CONDENSER = machine("condenser");
    public static final RecipeType<MachineRecipe> DECOMPOSER = machine("decomposer");
    public static final RecipeType<MachineRecipe> SMELTER = machine("smelter");
    public static final RecipeType<MachineRecipe> LATHE = machine("lathe");
    public static final RecipeType<MachineRecipe> CUTTING_MACHINE = machine("cutting_machine");
    public static final RecipeType<MachineRecipe> WIRE_DRAWING_MACHINE = machine("wire_drawing_machine");
    public static final RecipeType<MachineRecipe> PIPE_DRAWING_MACHINE = machine("pipe_drawing_machine");
    public static final RecipeType<MachineRecipe> ASSEMBLER = machine("assembler");
    public static final RecipeType<MachineRecipe> INSCRIBER = machine("inscriber");
    public static final RecipeType<MachineRecipe> CENTRIFUGE = machine("centrifuge");
    public static final RecipeType<MachineRecipe> CHEMICAL_REACTOR = machine("chemical_reactor");
    public static final RecipeType<MachineRecipe> ELECTROLYSIS_REACTOR = machine("electrolysis_reactor");
    public static final RecipeType<MachineRecipe> ALLOY_SMELTER = machine("alloy_smelter");
    public static final RecipeType<MachineRecipe> ENERGETIC_CLAY_CONDENSER = machine("energetic_clay_condenser");

    public static final Map<ResourceLocation, RecipeType<MachineRecipe>> MACHINES = createMachineTypes();

    private ClayiumJeiRecipeTypes() {
    }

    private static RecipeType<MachineRecipe> machine(String id) {
        return RecipeType.create(Clayium.MODID, id, MachineRecipe.class);
    }

    private static Map<ResourceLocation, RecipeType<MachineRecipe>> createMachineTypes() {
        Map<ResourceLocation, RecipeType<MachineRecipe>> result = new LinkedHashMap<>();
        result.put(ClayiumMachineIds.CLAY_BENDING_MACHINE, CLAY_BENDING_MACHINE);
        result.put(ClayiumMachineIds.ELEMENTAL_MILLING_MACHINE, ELEMENTAL_MILLING_MACHINE);
        result.put(ClayiumMachineIds.GRINDER, GRINDER);
        result.put(ClayiumMachineIds.CONDENSER, CONDENSER);
        result.put(ClayiumMachineIds.DECOMPOSER, DECOMPOSER);
        result.put(ClayiumMachineIds.SMELTER, SMELTER);
        result.put(ClayiumMachineIds.LATHE, LATHE);
        result.put(ClayiumMachineIds.CUTTING_MACHINE, CUTTING_MACHINE);
        result.put(ClayiumMachineIds.WIRE_DRAWING_MACHINE, WIRE_DRAWING_MACHINE);
        result.put(ClayiumMachineIds.PIPE_DRAWING_MACHINE, PIPE_DRAWING_MACHINE);
        result.put(ClayiumMachineIds.ASSEMBLER, ASSEMBLER);
        result.put(ClayiumMachineIds.INSCRIBER, INSCRIBER);
        result.put(ClayiumMachineIds.CENTRIFUGE, CENTRIFUGE);
        result.put(ClayiumMachineIds.CHEMICAL_REACTOR, CHEMICAL_REACTOR);
        result.put(ClayiumMachineIds.ELECTROLYSIS_REACTOR, ELECTROLYSIS_REACTOR);
        result.put(ClayiumMachineIds.ALLOY_SMELTER, ALLOY_SMELTER);
        result.put(ClayiumMachineIds.ENERGETIC_CLAY_CONDENSER, ENERGETIC_CLAY_CONDENSER);
        return Map.copyOf(result);
    }
}
