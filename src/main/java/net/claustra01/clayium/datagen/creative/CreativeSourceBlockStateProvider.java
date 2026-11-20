package net.claustra01.clayium.datagen.creative;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlock;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CreativeSourceBlockStateProvider extends BlockStateProvider {
    public CreativeSourceBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Creative Energy Source Block State: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile source = models().cubeAll(
                CreativeSourceRegistration.ID, ClayiumMod.id("block/creativeenergy"));
        var multipart = getMultipartBuilder(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get());
        multipart.part().modelFile(source).addModel().end();
        for (Direction direction : Direction.values()) {
            for (BufferBlockEntity.FaceMode mode : new BufferBlockEntity.FaceMode[] {
                    BufferBlockEntity.FaceMode.INPUT, BufferBlockEntity.FaceMode.OUTPUT}) {
                multipart.part()
                        .modelFile(models().getExistingFile(ClayiumMod.id(
                                "block/buffer_face_" + direction.getName() + "_" + mode.getSerializedName())))
                        .addModel()
                        .condition(BufferBlock.FACES.get(direction.get3DDataValue()), mode)
                        .end();
            }
        }
        simpleBlockItem(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get(), source);
    }
}
