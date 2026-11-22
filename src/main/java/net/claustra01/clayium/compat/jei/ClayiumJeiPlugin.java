package net.claustra01.clayium.compat.jei;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerCatalysts;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public final class ClayiumJeiPlugin implements IModPlugin {
    private static final Map<ClayiumRecipeMethod, RecipeType<RecipeHolder<ClayiumMachineRecipe>>> TYPES = types();
    static final RecipeType<PanDuplicationRecipe> PAN_DUPLICATION = RecipeType.create(
            ClayiumMod.MOD_ID, "pan_duplication", PanDuplicationRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ClayiumMod.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            registration.addRecipeCategories(new MachineRecipeCategory(method, TYPES.get(method), gui));
        }
        registration.addRecipeCategories(new PanDuplicationCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        var manager = Minecraft.getInstance().level.getRecipeManager();
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            List<RecipeHolder<ClayiumMachineRecipe>> recipes = manager
                    .getAllRecipesFor(RecipeRegistration.type(method));
            registration.addRecipes(TYPES.get(method), recipes);
        }
        registration.addRecipes(PAN_DUPLICATION,
                java.util.stream.IntStream.rangeClosed(4, 13).mapToObj(PanDuplicationRecipe::new).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        TYPES.forEach((method, type) -> RecipeViewerCatalysts.forMethod(method)
                .forEach(stack -> registration.addRecipeCatalyst(stack, type)));
        for (var definition : PanRegistration.DEFINITIONS.values()) {
            if (definition.kind() == net.claustra01.clayium.pan.PanBlock.Kind.DUPLICATOR) {
                registration.addRecipeCatalyst(definition.block().get(), PAN_DUPLICATION);
            }
        }
    }

    private static Map<ClayiumRecipeMethod, RecipeType<RecipeHolder<ClayiumMachineRecipe>>> types() {
        Map<ClayiumRecipeMethod, RecipeType<RecipeHolder<ClayiumMachineRecipe>>> types =
                new EnumMap<>(ClayiumRecipeMethod.class);
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            types.put(method, RecipeType.createRecipeHolderType(method.location()));
        }
        return Map.copyOf(types);
    }
}
