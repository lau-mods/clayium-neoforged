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
        ore(WorldgenRegistration.CLAY_ORE.get(), "clayore-0");
        ore(WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(), "clayore-0");
        ore(WorldgenRegistration.DENSE_CLAY_ORE.get(), "clayore-1");
        ore(WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get(), "clayore-1");
        ore(WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get(), "clayore-2");
        ore(WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(), "clayore-2");
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
