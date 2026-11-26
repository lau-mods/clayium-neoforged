package net.claustra01.clayium.datagen.autocrafter;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class AutoCrafterBlockStateProvider extends BlockStateProvider {
    public AutoCrafterBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Auto Crafter Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        AutoCrafterRegistration.AUTO_CRAFTERS.values().forEach(definition -> {
            ResourceLocation hull = ClayiumMod.id("block/machinehull-" + (definition.specification().tier() - 1));
            ModelFile model = models().getBuilder(definition.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", hull)
                    .texture("side", ClayiumMod.id("block/autocrafterside"))
                    .texture("top", ClayiumMod.id("block/autocraftertop"))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#side").end().end()
                    .element().from(0, 0, 16).to(16, 16, 16.01F)
                    .face(Direction.SOUTH).texture("#side").end().end()
                    .element().from(-0.01F, 0, 0).to(0, 16, 16)
                    .face(Direction.WEST).texture("#side").end().end()
                    .element().from(16, 0, 0).to(16.01F, 16, 16)
                    .face(Direction.EAST).texture("#side").end().end()
                    .element().from(0, 16, 0).to(16, 16.01F, 16)
                    .face(Direction.UP).texture("#top").end().end();
            horizontalBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }
}
