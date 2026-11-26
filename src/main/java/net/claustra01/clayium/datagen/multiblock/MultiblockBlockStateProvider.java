package net.claustra01.clayium.datagen.multiblock;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.multiblock.MultiblockControllerBlock;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MultiblockBlockStateProvider
        extends net.neoforged.neoforge.client.model.generators.BlockStateProvider {
    public MultiblockBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        MultiblockRegistration.CONTROLLERS.forEach((type, holder) -> {
            ModelFile idle = controllerModel(type.id(), type.minimumTier(), type.frontTexture());
            ModelFile formed = controllerModel(type.id() + "_formed", type.minimumTier(), type.frontTexture() + "_1");
            getVariantBuilder(holder.get()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(state.getValue(MultiblockControllerBlock.FORMED) ? formed : idle)
                    .rotationY(((int)state.getValue(ClayMachineBlock.FACING).toYRot() + 180) % 360)
                    .build());
            simpleBlockItem(holder.get(), idle);
        });
    }

    private ModelFile controllerModel(String id, int tier, String frontTexture) {
        return models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", ClayiumMod.id("block/machinehull-" + (tier - 1)))
                .texture("front", ClayiumMod.id("block/" + frontTexture))
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
    }

    @Override
    public String getName() {
        return "Clayium multiblock block states";
    }
}
