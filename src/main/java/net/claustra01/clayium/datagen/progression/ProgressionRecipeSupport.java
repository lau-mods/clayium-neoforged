package net.claustra01.clayium.datagen.progression;

import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.world.level.ItemLike;

final class ProgressionRecipeSupport {
    static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    static ItemLike component(String id) {
        return MaterialRegistration.COMPONENTS.get(id).get();
    }

    private ProgressionRecipeSupport() {
    }
}
