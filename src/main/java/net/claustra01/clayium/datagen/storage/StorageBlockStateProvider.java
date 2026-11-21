package net.claustra01.clayium.datagen.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.storage.StorageContainerBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class StorageBlockStateProvider extends BlockStateProvider {
    public StorageBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Storage Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile storage = models().orientable("storage_container",
                ClayiumMod.id("block/storagecontainerside"),
                ClayiumMod.id("block/storagecontainer"),
                ClayiumMod.id("block/storagecontainertop"));
        ModelFile expanded = models().orientable("storage_container_expanded",
                ClayiumMod.id("block/storagecontainerside"),
                ClayiumMod.id("block/storagecontainerex"),
                ClayiumMod.id("block/storagecontainertop"));
        getVariantBuilder(StorageRegistration.STORAGE_CONTAINER.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(StorageContainerBlock.EXPANDED) ? expanded : storage)
                .rotationY(((int)state.getValue(StorageContainerBlock.FACING).toYRot() + 180) % 360)
                .build());
        simpleBlockItem(StorageRegistration.STORAGE_CONTAINER.get(), storage);

        ModelFile voidModel = models().orientable("void_container",
                ClayiumMod.id("block/vacuumcontainerside"),
                ClayiumMod.id("block/vacuumcontainer"),
                ClayiumMod.id("block/vacuumcontainertop"));
        simpleBlock(StorageRegistration.VOID_CONTAINER.get(), voidModel);
        simpleBlockItem(StorageRegistration.VOID_CONTAINER.get(), voidModel);

        ModelFile particle = models().getBuilder("metal_chest_particle")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", ClayiumMod.id("block/metalchest_particle"));
        StorageRegistration.METAL_CHESTS.values().forEach(definition -> simpleBlock(definition.block().get(), particle));
    }
}
