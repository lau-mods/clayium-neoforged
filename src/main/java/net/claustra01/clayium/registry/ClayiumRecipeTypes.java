package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTRY =
            DeferredRegister.create(Registries.RECIPE_TYPE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayWorkTableRecipe>> CLAY_WORK_TABLE =
            regSimple("clay_work_table");

    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalMetalSeparatorRecipe>> CHEMICAL_METAL_SEPARATOR =
            regSimple("chemical_metal_separator");

    private static final Map<String, DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>>> MACHINE_CATEGORIES =
            new LinkedHashMap<>();

    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> BENDING_MACHINE = regMachine("bending_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> WIRE_DRAWING_MACHINE = regMachine("wire_drawing_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> PIPE_DRAWING_MACHINE = regMachine("pipe_drawing_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CUTTING_MACHINE = regMachine("cutting_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> LATHE = regMachine("lathe");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> MILLING_MACHINE = regMachine("milling_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CONDENSER = regMachine("condenser");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> GRINDER = regMachine("grinder");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> DECOMPOSER = regMachine("decomposer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> ENERGETIC_CLAY_CONDENSER = regMachine("energetic_clay_condenser");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> ASSEMBLER = regMachine("assembler");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> INSCRIBER = regMachine("inscriber");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CENTRIFUGE = regMachine("centrifuge");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> SMELTER = regMachine("smelter");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CHEMICAL_REACTOR = regMachine("chemical_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> ALLOY_SMELTER = regMachine("alloy_smelter");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> BLAST_FURNACE = regMachine("blast_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> ELECTROLYSIS_REACTOR = regMachine("electrolysis_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> REACTOR = regMachine("reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> MATTER_TRANSFORMER = regMachine("matter_transformer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CA_CONDENSER = regMachine("ca_condenser");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CA_INJECTOR = regMachine("ca_injector");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> CA_REACTOR = regMachine("ca_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> ENERGETIC_CLAY_DECOMPOSER = regMachine("energetic_clay_decomposer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> FLUID_TRANSFER_MACHINE = regMachine("fluid_transfer_machine");

    private ClayiumRecipeTypes() {}

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> regSimple(String name) {
        return REGISTRY.register(name, () -> RecipeType.simple(ClayiumMod.id(name)));
    }

    private static DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> regMachine(String categoryId) {
        DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> holder = regSimple(categoryId);
        MACHINE_CATEGORIES.put(categoryId, holder);
        return holder;
    }

    public static DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> byCategory(String categoryId) {
        DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>> holder = MACHINE_CATEGORIES.get(categoryId);
        if (holder == null) {
            throw new IllegalArgumentException("Unknown machine recipe category: " + categoryId);
        }
        return holder;
    }

    public static Map<String, DeferredHolder<RecipeType<?>, RecipeType<ClayMachineRecipe>>> machineCategories() {
        return Collections.unmodifiableMap(MACHINE_CATEGORIES);
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
