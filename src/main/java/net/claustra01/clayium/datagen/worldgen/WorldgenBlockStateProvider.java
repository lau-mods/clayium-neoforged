package net.claustra01.clayium.datagen.worldgen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class WorldgenBlockStateProvider extends BlockStateProvider {
    public WorldgenBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Clayium Worldgen Block States";
    }

    @Override
    protected void registerStatesAndModels() {
        ore(WorldgenRegistration.CLAY_ORE.get(), "clay_ore");
        ore(WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(), "deepslate_clay_ore");
        ore(WorldgenRegistration.DENSE_CLAY_ORE.get(), "dense_clay_ore");
        ore(WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get(), "deepslate_dense_clay_ore");
        ore(WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get(), "large_dense_clay_ore");
        ore(WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(), "deepslate_large_dense_clay_ore");
    }

    private void ore(Block block, String texture) {
        ModelFile model = models().cubeAll(name(block), ClayiumMod.id("block/" + texture));
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    private String name(Block block) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
