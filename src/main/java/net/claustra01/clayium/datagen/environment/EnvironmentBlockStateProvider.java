package net.claustra01.clayium.datagen.environment;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleBlock;
import net.claustra01.clayium.machine.crucible.QuartzCrucibleStage;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class EnvironmentBlockStateProvider extends BlockStateProvider {
    public EnvironmentBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Environment Machine Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        EnvironmentMachineRegistration.COBBLESTONE_GENERATORS.forEach(definition -> {
            ModelFile model = machineModel(
                    definition.id(), definition.tier(), ClayiumMod.id("block/cobblestonegenerator"));
            horizontalBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });

        ModelFile empty = crucibleModel("quartz_crucible", null);
        ModelFile heating = crucibleModel("quartz_crucible_heating", ClayiumMod.id("block/impuresilicon"));
        ModelFile ready = crucibleModel("quartz_crucible_ready", ClayiumMod.id("block/silicon"));
        getVariantBuilder(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get())
                .partialState().with(QuartzCrucibleBlock.STAGE, QuartzCrucibleStage.EMPTY)
                .modelForState().modelFile(empty).addModel()
                .partialState().with(QuartzCrucibleBlock.STAGE, QuartzCrucibleStage.HEATING)
                .modelForState().modelFile(heating).addModel()
                .partialState().with(QuartzCrucibleBlock.STAGE, QuartzCrucibleStage.READY)
                .modelForState().modelFile(ready).addModel();
        simpleBlockItem(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get(), empty);
    }

    private ModelFile machineModel(String id, int tier, ResourceLocation front) {
        ResourceLocation hull = ClayiumMod.id("block/machinehull-" + Math.max(0, tier - 1));
        return models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", hull)
                .texture("front", front)
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
    }

    private ModelFile crucibleModel(String id, ResourceLocation contents) {
        var builder = models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("quartz", ResourceLocation.withDefaultNamespace("block/quartz_block_side"))
                .texture("particle", "#quartz")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 4, 16).cube("#quartz").end()
                .element().from(0, 4, 0).to(3, 16, 16).cube("#quartz").end()
                .element().from(13, 4, 0).to(16, 16, 16).cube("#quartz").end()
                .element().from(3, 4, 0).to(13, 16, 3).cube("#quartz").end()
                .element().from(3, 4, 13).to(13, 16, 16).cube("#quartz").end();
        if (contents != null) {
            builder.texture("contents", contents)
                    .element().from(3, 4, 3).to(13, 15, 13)
                    .face(Direction.UP).texture("#contents").end()
                    .face(Direction.NORTH).texture("#contents").end()
                    .face(Direction.SOUTH).texture("#contents").end()
                    .face(Direction.EAST).texture("#contents").end()
                    .face(Direction.WEST).texture("#contents").end().end();
        }
        return builder;
    }

}
