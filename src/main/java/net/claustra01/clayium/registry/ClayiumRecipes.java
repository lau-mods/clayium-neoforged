/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.recipe.MachineRecipe;
import net.claustra01.clayium.recipe.MachineRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Clayium.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Clayium.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> MACHINE_RECIPE_TYPE =
            TYPES.register("machine", () -> RecipeType.simple(Clayium.id("machine")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> MACHINE_RECIPE_SERIALIZER =
            SERIALIZERS.register("machine", () -> MachineRecipeSerializer.INSTANCE);

    private ClayiumRecipes() {
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
    }
}
