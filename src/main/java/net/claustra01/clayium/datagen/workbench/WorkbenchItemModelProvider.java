package net.claustra01.clayium.datagen.workbench;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class WorkbenchItemModelProvider extends ItemModelProvider {
    private static final Map<String, String> TEXTURES = Map.ofEntries(
            Map.entry("raw_clay_rolling_pin", "rawclayrollingpin"),
            Map.entry("raw_clay_slicer", "rawclayslicer"),
            Map.entry("raw_clay_spatula", "rawclayspatula"),
            Map.entry("clay_rolling_pin", "clayrollingpin"),
            Map.entry("clay_slicer", "clayslicer"),
            Map.entry("clay_spatula", "clayspatula"),
            Map.entry("clay_io_configurator", "iotool"),
            Map.entry("clay_piping_configurator", "pipingtool"),
            Map.entry("memory_card", "memorycard"),
            Map.entry("direction_memory_card", "directionmemory"));

    public WorkbenchItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Clayium workbench item models";
    }

    @Override
    protected void registerModels() {
        TEXTURES.forEach((id, texture) -> getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", ClayiumMod.id("item/" + texture)));
    }
}
