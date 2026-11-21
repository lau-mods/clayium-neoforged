package net.claustra01.clayium.datagen.claytree;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ClayTreeBlockStateProvider extends BlockStateProvider {
    public ClayTreeBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile sapling = models().cross(
                        "clay_tree_sapling", ClayiumMod.id("block/sapling_claytree"))
                .renderType("cutout");
        simpleBlock(ClayTreeRegistration.CLAY_TREE_SAPLING.get(), sapling);
        itemModels().withExistingParent("clay_tree_sapling", "item/generated")
                .texture("layer0", ClayiumMod.id("block/sapling_claytree"));

        axisBlock(
                ClayTreeRegistration.CLAY_TREE_LOG.get(),
                ClayiumMod.id("block/log_claytree"),
                ClayiumMod.id("block/log_claytree_top"));
        simpleBlockItem(
                ClayTreeRegistration.CLAY_TREE_LOG.get(),
                models().getExistingFile(ClayiumMod.id("block/clay_tree_log")));

        BlockModelBuilder leaves = models().cubeAll(
                        "clay_tree_leaf", ClayiumMod.id("block/leaves_claytree"))
                .renderType("cutout_mipped");
        simpleBlock(ClayTreeRegistration.CLAY_TREE_LEAF.get(), leaves);
        simpleBlockItem(ClayTreeRegistration.CLAY_TREE_LEAF.get(), leaves);
    }

    @Override
    public String getName() {
        return "Clayium Clay Tree Block States";
    }
}
