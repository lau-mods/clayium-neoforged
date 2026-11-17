package net.claustra01.clayium.datagen.clayproduction;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ClayProductionBlockStateProvider extends BlockStateProvider {
    public ClayProductionBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ClayProductionRegistration.BLOCKS.forEach((type, block) -> {
            ResourceLocation hull = ClayiumMod.id("block/machinehull-" + (type.tier() - 1));
            BlockModelBuilder model = models().getBuilder(type.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", hull)
                    .texture("machine", ClayiumMod.id("block/" + type.texture()))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end();
            if (type.kind() == ClayProductionType.Kind.SOLAR_FABRICATOR) {
                model.texture("solar", ClayiumMod.id("block/solar"))
                        .element().from(0, 16, 0).to(16, 16.01F, 16)
                        .face(Direction.UP).texture("#solar").end().end();
            } else {
                model.element().from(0, 0, -0.01F).to(16, 16, 0)
                        .face(Direction.NORTH).texture("#machine").end().end();
            }
            horizontalBlock(block.get(), model);
            simpleBlockItem(block.get(), model);
        });
    }

    @Override
    public String getName() {
        return "Clay Production Block States: " + ClayiumMod.MOD_ID;
    }
}
