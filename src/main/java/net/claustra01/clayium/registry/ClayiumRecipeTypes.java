package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTRY =
            DeferredRegister.create(Registries.RECIPE_TYPE, ClayiumMod.MOD_ID);

    private ClayiumRecipeTypes() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
