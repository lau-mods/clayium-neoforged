package net.claustra01.clayium.datagen.multitrack;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.data.PackOutput;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MultitrackBlockStateProvider extends BlockStateProvider {
    public MultitrackBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ClayiumMod.MOD_ID, helper);
    }
    @Override public String getName() { return "Multitrack Logistics Block States: " + ClayiumMod.MOD_ID; }
    @Override protected void registerStatesAndModels() {
        MultitrackRegistration.MULTITRACK_BUFFERS.values().forEach(definition -> {
            var hull = ClayiumMod.id("block/machinehull-" + (definition.tier() - 1));
            var model = models().cubeAll(definition.id(), hull);
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
        MultitrackRegistration.DISTRIBUTORS.values().forEach(definition -> {
            var model = models().getBuilder(definition.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", ClayiumMod.id("block/machinehull-" + (definition.tier() - 1)))
                    .texture("overlay", ClayiumMod.id("block/distributor"))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end();
            model.element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#overlay").end().end();
            model.element().from(0, 0, 16).to(16, 16, 16.01F)
                    .face(Direction.SOUTH).texture("#overlay").end().end();
            model.element().from(-0.01F, 0, 0).to(0, 16, 16)
                    .face(Direction.WEST).texture("#overlay").end().end();
            model.element().from(16, 0, 0).to(16.01F, 16, 16)
                    .face(Direction.EAST).texture("#overlay").end().end();
            model.element().from(0, 16, 0).to(16, 16.01F, 16)
                    .face(Direction.UP).texture("#overlay").end().end();
            model.element().from(0, -0.01F, 0).to(16, 0, 16)
                    .face(Direction.DOWN).texture("#overlay").end().end();
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }
}
