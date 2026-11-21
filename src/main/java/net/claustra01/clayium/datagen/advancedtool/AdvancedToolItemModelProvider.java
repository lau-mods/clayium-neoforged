package net.claustra01.clayium.datagen.advancedtool;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class AdvancedToolItemModelProvider extends ItemModelProvider {
    private static final Map<String, String> TEXTURES = Map.of(
            "clay_pickaxe", "claypickaxe",
            "clay_shovel", "clayshovel",
            "clay_steel_pickaxe", "claysteelpickaxe",
            "clay_steel_shovel", "claysteelshovel");

    public AdvancedToolItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Advanced Tool Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        TEXTURES.forEach((id, texture) -> getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/handheld"))
                .texture("layer0", ClayiumMod.id("item/" + texture)));
    }
}
