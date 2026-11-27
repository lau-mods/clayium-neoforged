package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.AutoEatGadgetSwitchRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.FluidCapsuleContainerRecipe;
import net.claustra01.clayium.recipe.FluidCapsuleTransferRecipe;
import net.claustra01.clayium.recipe.StorageContainerUpgradeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ClayiumMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClayWorkTableRecipe>> CLAY_WORK_TABLE =
            REGISTRY.register("clay_work_table", ClayWorkTableRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClayMachineRecipe>> CLAY_MACHINE =
            REGISTRY.register("clay_machine", ClayMachineRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChemicalMetalSeparatorRecipe>> CHEMICAL_METAL_SEPARATOR =
            REGISTRY.register("chemical_metal_separator", ChemicalMetalSeparatorRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AutoEatGadgetSwitchRecipe>> AUTO_EAT_GADGET_SWITCH =
            REGISTRY.register("auto_eat_gadget_switch", () -> new SimpleCraftingRecipeSerializer<>(AutoEatGadgetSwitchRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidCapsuleTransferRecipe>> FLUID_CAPSULE_TRANSFER =
            REGISTRY.register("fluid_capsule_transfer", () -> new SimpleCraftingRecipeSerializer<>(FluidCapsuleTransferRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidCapsuleContainerRecipe>> FLUID_CAPSULE_CONTAINER =
            REGISTRY.register("fluid_capsule_container", () -> new SimpleCraftingRecipeSerializer<>(FluidCapsuleContainerRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StorageContainerUpgradeRecipe>> STORAGE_CONTAINER_UPGRADE =
            REGISTRY.register("storage_container_upgrade", () -> new SimpleCraftingRecipeSerializer<>(StorageContainerUpgradeRecipe::new));

    private ClayiumRecipeSerializers() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
