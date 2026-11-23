package net.claustra01.clayium.datagen.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.storage.StorageContainerBlock;
import net.minecraft.resources.ResourceLocation;
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

    private ModelFile containerModel(String name, ResourceLocation side, ResourceLocation front, ResourceLocation top) {
        return models().withExistingParent(name, modLoc("block/hull_with_overlays"))
                .renderType("minecraft:translucent")
                .texture("hull", modLoc("block/az91dhull"))
                .texture("side", side).texture("front", front).texture("top", top);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile storage = containerModel("storage_container",
                ClayiumMod.id("block/storagecontainerside"),
                ClayiumMod.id("block/storagecontainer"),
                ClayiumMod.id("block/storagecontainertop"));
        ModelFile expanded = containerModel("storage_container_expanded",
                ClayiumMod.id("block/storagecontainerside"),
                ClayiumMod.id("block/storagecontainerex"),
                ClayiumMod.id("block/storagecontainertop"));
        getVariantBuilder(StorageRegistration.STORAGE_CONTAINER.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(StorageContainerBlock.EXPANDED) ? expanded : storage)
                .rotationY(((int)state.getValue(StorageContainerBlock.FACING).toYRot() + 180) % 360)
                .build());
        simpleBlockItem(StorageRegistration.STORAGE_CONTAINER.get(), storage);

        ModelFile voidModel = containerModel("void_container",
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
