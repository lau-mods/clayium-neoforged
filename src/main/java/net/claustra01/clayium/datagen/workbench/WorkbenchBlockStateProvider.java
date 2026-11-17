package net.claustra01.clayium.datagen.workbench;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class WorkbenchBlockStateProvider extends BlockStateProvider {
    public WorkbenchBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Clayium workbench block states";
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile workTable = models().cubeBottomTop(
                "clay_work_table",
                texture("clayworktable_side"),
                texture("clayworktable_side"),
                texture("clayworktable"));
        simpleBlock(WorkbenchRegistration.CLAY_WORK_TABLE.get(), workTable);
        simpleBlockItem(WorkbenchRegistration.CLAY_WORK_TABLE.get(), workTable);

        ModelFile board = models().getBuilder("clay_crafting_board")
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("top", texture("claycraftingtable"))
                .texture("side", texture("clayworktable_side"))
                .texture("particle", "#top")
                .element().from(0, 0, 0).to(16, 4, 16)
                .face(Direction.DOWN).texture("#side").uvs(0, 12, 16, 16).cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#top").end()
                .face(Direction.NORTH).texture("#side").uvs(0, 12, 16, 16).cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#side").uvs(0, 12, 16, 16).cullface(Direction.SOUTH).end()
                .face(Direction.WEST).texture("#side").uvs(0, 12, 16, 16).cullface(Direction.WEST).end()
                .face(Direction.EAST).texture("#side").uvs(0, 12, 16, 16).cullface(Direction.EAST).end()
                .end();
        simpleBlock(WorkbenchRegistration.CLAY_CRAFTING_BOARD.get(), board);
        simpleBlockItem(WorkbenchRegistration.CLAY_CRAFTING_BOARD.get(), board);

        cube(WorkbenchRegistration.RAW_CLAY_MACHINE_HULL.get(), "rawclaymachinehull-0");
        cube(WorkbenchRegistration.CLAY_MACHINE_HULL.get(), "machinehull-0");
    }

    private void cube(net.minecraft.world.level.block.Block block, String texture) {
        ModelFile model = models().cubeAll(BuiltInRegistries.BLOCK.getKey(block).getPath(), texture(texture));
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    private ResourceLocation texture(String name) {
        return ClayiumMod.id("block/" + name);
    }
}
