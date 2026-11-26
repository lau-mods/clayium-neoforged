package net.claustra01.clayium.registry;

import java.util.EnumMap;
import java.util.Map;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.StorageContainerExpansionRecipe;
import net.claustra01.clayium.recipe.NbtPreservingAutoEatRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class RecipeRegistration {
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClayiumMachineRecipe>> MACHINE_SERIALIZER =
            ClayiumRegistries.RECIPE_SERIALIZERS.register("machine", ClayiumMachineRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StorageContainerExpansionRecipe>>
            STORAGE_CONTAINER_EXPANSION_SERIALIZER = ClayiumRegistries.RECIPE_SERIALIZERS.register(
                    "storage_container_expansion",
                    () -> new SimpleCraftingRecipeSerializer<>(StorageContainerExpansionRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NbtPreservingAutoEatRecipe>>
            NBT_PRESERVING_AUTO_EAT_SERIALIZER = ClayiumRegistries.RECIPE_SERIALIZERS.register(
                    "nbt_preserving_auto_eat",
                    () -> new SimpleCraftingRecipeSerializer<>(NbtPreservingAutoEatRecipe::new));

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
