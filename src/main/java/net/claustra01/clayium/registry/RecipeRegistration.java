package net.claustra01.clayium.registry;

import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class RecipeRegistration {
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClayiumMachineRecipe>> MACHINE_SERIALIZER =
            ClayiumRegistries.RECIPE_SERIALIZERS.register("machine", ClayiumMachineRecipe.Serializer::new);

    private static final Map<ClayiumRecipeMethod, DeferredHolder<RecipeType<?>, RecipeType<ClayiumMachineRecipe>>> TYPES =
            registerTypes();

    private static Map<ClayiumRecipeMethod, DeferredHolder<RecipeType<?>, RecipeType<ClayiumMachineRecipe>>> registerTypes() {
        Map<ClayiumRecipeMethod, DeferredHolder<RecipeType<?>, RecipeType<ClayiumMachineRecipe>>> types =
                new EnumMap<>(ClayiumRecipeMethod.class);
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            types.put(method, ClayiumRegistries.RECIPE_TYPES.register(method.id(),
                    () -> RecipeType.simple(method.location())));
        }
        return Map.copyOf(types);
    }

    public static RecipeType<ClayiumMachineRecipe> type(ClayiumRecipeMethod method) {
        return TYPES.get(method).get();
    }

    public static Map<ClayiumRecipeMethod, DeferredHolder<RecipeType<?>, RecipeType<ClayiumMachineRecipe>>> types() {
        return TYPES;
    }

    private RecipeRegistration() {
    }
}
