package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ClayiumMod.MOD_ID);

    private ClayiumRecipeSerializers() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
