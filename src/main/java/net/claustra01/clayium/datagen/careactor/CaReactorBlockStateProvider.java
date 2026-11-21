package net.claustra01.clayium.datagen.careactor;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CaReactorBlockStateProvider
        extends net.neoforged.neoforge.client.model.generators.BlockStateProvider {
    public CaReactorBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        CaReactorRegistration.CORES.values().forEach(definition -> {
            var model = models().getBuilder(definition.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", ClayiumMod.id("block/machinehull-" + (definition.tier() - 1)))
                    .texture("overlay", ClayiumMod.id("block/careactorcore"))
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
            getVariantBuilder(definition.block().get()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(((int)state.getValue(ClayMachineBlock.FACING).toYRot() + 180) % 360)
                    .build());
            simpleBlockItem(definition.block().get(), model);
        });
        CaReactorRegistration.COILS.values().forEach(definition -> {
            ModelFile model = models().cubeAll(
                    definition.id(), ClayiumMod.id("block/careactorcoil-" + definition.texture()));
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
        CaReactorRegistration.HULLS.values().forEach(definition -> {
            ModelFile model = models().cubeAll(
                    definition.id(), ClayiumMod.id("block/careactorhull-" + (definition.rank() - 1)));
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }

    @Override
    public String getName() {
        return "CA reactor block states";
    }
}
