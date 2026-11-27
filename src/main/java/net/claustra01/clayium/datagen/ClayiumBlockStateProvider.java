package net.claustra01.clayium.datagen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.ClayContainerBlock.FacingMode;
import net.claustra01.clayium.block.ClayTreeLeavesBlock;
import net.claustra01.clayium.block.ClayTreeLogBlock;
import net.claustra01.clayium.block.ClayTreeSaplingBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.PanCableBlock;
import net.claustra01.clayium.block.QuartzCrucibleBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.block.TieredBlock;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ClayiumBlockStateProvider extends BlockStateProvider {
    public ClayiumBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ClayiumMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile tintedMetalBlock = buildThreeLayerTintedCube(
                "tinted_metal_block",
                modLoc("block/metalblock_base"),
                modLoc("block/metalblock_dark"),
                modLoc("block/metalblock_light"),
                0, 0, 0, 16, 16, 16
        );
        ModelFile tintedMetalChest = buildThreeLayerTintedCube(
                "tinted_metal_chest",
                modLoc("block/metalblock_base"),
                modLoc("block/metalblock_dark"),
                modLoc("block/metalblock_light"),
                1, 0, 1, 15, 14, 15
        );
        ModelFile tintedSilicone = models()
                .getBuilder("block/tinted_silicone_block")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("all", modLoc("block/siliconecolored"))
                .texture("particle", modLoc("block/siliconecolored"))
                .element()
                .from(0, 0, 0)
                .to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#all").cullface(dir).tintindex(0))
                .end();

        for (var entry : ClayiumBlocks.all().entrySet()) {
            String path = entry.getKey();
            Block block = entry.getValue().get();

            if (block instanceof SiliconeColoredBlock) {
                simpleBlockWithItem(block, tintedSilicone);
            } else if (block instanceof MetalMaterialBlock metalBlock) {
                if (metalBlock.isLayeredTint()) {
                    simpleBlockWithItem(block, tintedMetalBlock);
                } else {
                    ModelFile model = cubeAll(path, metalBlock.getTextureName());
                    simpleBlockWithItem(block, model);
                }
            } else if (block instanceof MetalChestBlock) {
                horizontalBlock(block, tintedMetalChest);
                simpleBlockItem(block, tintedMetalChest);
            } else if (block instanceof ClayTreeSaplingBlock) {
                ModelFile model = models().cross(path, modLoc("block/sapling_claytree")).renderType("minecraft:cutout");
                simpleBlock(block, model);
                itemModels().withExistingParent(path, mcLoc("item/generated")).texture("layer0", modLoc("block/sapling_claytree"));
            } else if (block instanceof ClayTreeLeavesBlock) {
                ModelFile model = models().cubeAll(path, modLoc("block/leaves_claytree")).renderType("minecraft:cutout_mipped");
                simpleBlockWithItem(block, model);
            } else if (block instanceof ClayTreeLogBlock logBlock) {
                axisBlock(logBlock, modLoc("block/log_claytree"), modLoc("block/log_claytree_top"));
                simpleBlockItem(logBlock, models().getExistingFile(modLoc("block/" + path)));
            } else if (block instanceof PanCableBlock) {
                registerPanCable(path, block);
            } else if (block instanceof QuartzCrucibleBlock) {
                registerQuartzCrucible(path, block);
            } else if (block instanceof ClayContainerBlock container) {
                registerContainerBlock(path, container);
            } else if (block instanceof TieredBlock tiered) {
                ModelFile model = cubeAll(path, tiered.getTextureName());
                simpleBlockWithItem(block, model);
            }
        }
    }

    private ModelFile cubeAll(String name, String textureStem) {
        return models().cubeAll(name, modLoc("block/" + textureStem));
    }

    private ModelFile buildThreeLayerTintedCube(
            String name,
            ResourceLocation baseTex,
            ResourceLocation darkTex,
            ResourceLocation lightTex,
            float x1, float y1, float z1,
            float x2, float y2, float z2) {
        return models().getBuilder("block/" + name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType("minecraft:cutout")
                .texture("base", baseTex)
                .texture("dark", darkTex)
                .texture("light", lightTex)
                .texture("particle", baseTex)
                .element().from(x1, y1, z1).to(x2, y2, z2)
                .allFaces((dir, face) -> face.texture("#base").tintindex(0)).end()
                .element().from(x1, y1, z1).to(x2, y2, z2)
                .allFaces((dir, face) -> face.texture("#dark").tintindex(1)).end()
                .element().from(x1, y1, z1).to(x2, y2, z2)
                .allFaces((dir, face) -> face.texture("#light").tintindex(2)).end();
    }

    private void registerContainerBlock(String path, ClayContainerBlock container) {
        ModelFile model = buildContainerModel(path, container);
        FacingMode mode = container.getFacingMode();
        if (mode == FacingMode.HORIZONTAL) {
            horizontalBlock(container, model);
        } else if (mode == FacingMode.ALL) {
            directionalBlock(container, model);
        } else {
            simpleBlock(container, model);
        }
        simpleBlockItem(container, model);
    }

    private ModelFile buildContainerModel(String path, ClayContainerBlock container) {
        ResourceLocation base = modLoc("block/" + container.getBaseTexture());
        if ("clay_work_table".equals(path)) {
            ResourceLocation top = modLoc("block/clayworktable");
            ResourceLocation side = modLoc("block/clayworktable_side");
            ResourceLocation bottom = modLoc("block/compressedclay-0");
            return models().cube(path, bottom, top, side, side, side, side).texture("particle", top);
        }
        if ("storage_container".equals(path)) {
            ResourceLocation top = modLoc("block/storagecontainertop");
            ResourceLocation side = modLoc("block/storagecontainerside");
            ResourceLocation front = modLoc("block/storagecontainer");
            return models().cube(path, side, top, front, side, side, side).texture("particle", front);
        }
        if ("vacuum_container".equals(path)) {
            ResourceLocation top = modLoc("block/vacuumcontainertop");
            ResourceLocation side = modLoc("block/vacuumcontainerside");
            ResourceLocation front = modLoc("block/vacuumcontainer");
            return models().cube(path, side, top, front, side, side, side).texture("particle", front);
        }
        if ("auto_crafter".equals(path) || path.endsWith("_auto_crafter")) {
            ResourceLocation top = modLoc("block/autocraftertop");
            ResourceLocation side = modLoc("block/autocrafterside");
            return models().cube(path, base, top, side, side, side, side).texture("particle", top);
        }
        if ("auto_trader".equals(path) || path.endsWith("_auto_trader")) {
            ResourceLocation top = modLoc("block/autotradertop");
            ResourceLocation side = modLoc("block/autotraderside");
            return models().cube(path, base, top, side, side, side, side).texture("particle", top);
        }
        if (path.startsWith("solar_clay_fabricator") || "lithium_solar_clay_fabricator".equals(path)) {
            ResourceLocation top = modLoc("block/solar");
            return models().cube(path, base, top, base, base, base, base).texture("particle", top);
        }

        String overlayTex = container.getOverlayTexture();
        if (overlayTex == null || overlayTex.isEmpty()) {
            return models().cubeAll(path, base);
        }

        ResourceLocation overlay = modLoc("block/" + overlayTex);
        BlockModelBuilder builder = models().getBuilder("block/" + path)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType("minecraft:cutout")
                .texture("base", base)
                .texture("overlay", overlay)
                .texture("particle", base);

        builder.element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                .end();

        if (container.getFacingMode() == FacingMode.NONE) {
            builder.element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#overlay").cullface(dir))
                    .end();
        } else {
            builder.element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end()
                    .end();
        }
        return builder;
    }

    private void registerPanCable(String path, Block block) {
        ResourceLocation tex = modLoc("block/pancable");
        ModelFile core = models().getBuilder("block/pan_cable_core")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("cable", tex)
                .texture("particle", tex)
                .element().from(6, 6, 6).to(10, 10, 10)
                .allFaces((dir, face) -> face.texture("#cable"))
                .end();

        ModelFile arm = models().getBuilder("block/pan_cable_arm")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("cable", tex)
                .texture("particle", tex)
                .element().from(6, 6, 0).to(10, 10, 6)
                .allFaces((dir, face) -> face.texture("#cable"))
                .end();

        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        builder.part().modelFile(core).addModel().end();
        builder.part().modelFile(arm).addModel().condition(PanCableBlock.NORTH, true).end();
        builder.part().modelFile(arm).rotationY(90).addModel().condition(PanCableBlock.EAST, true).end();
        builder.part().modelFile(arm).rotationY(180).addModel().condition(PanCableBlock.SOUTH, true).end();
        builder.part().modelFile(arm).rotationY(270).addModel().condition(PanCableBlock.WEST, true).end();
        builder.part().modelFile(arm).rotationX(270).addModel().condition(PanCableBlock.UP, true).end();
        builder.part().modelFile(arm).rotationX(90).addModel().condition(PanCableBlock.DOWN, true).end();

        simpleBlockItem(block, core);
    }

    private void registerQuartzCrucible(String path, Block block) {
        ResourceLocation side = mcLoc("block/quartz_block_side");
        ResourceLocation bottom = mcLoc("block/quartz_block_bottom");
        ResourceLocation fill = modLoc("block/impuresilicon");

        ModelFile[] modelsByLevel = new ModelFile[10];
        for (int lvl = 0; lvl <= 9; lvl++) {
            BlockModelBuilder b = models().getBuilder("block/" + path + "_" + lvl)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .texture("side", side)
                    .texture("bottom", bottom)
                    .texture("fill", fill)
                    .texture("particle", side)
                    .element().from(0, 0, 0).to(16, 1, 16)
                    .allFaces((dir, face) -> face.texture("#bottom")).end()
                    .element().from(0, 0, 0).to(1, 12, 16)
                    .allFaces((dir, face) -> face.texture("#side")).end()
                    .element().from(15, 0, 0).to(16, 12, 16)
                    .allFaces((dir, face) -> face.texture("#side")).end()
                    .element().from(1, 0, 0).to(15, 12, 1)
                    .allFaces((dir, face) -> face.texture("#side")).end()
                    .element().from(1, 0, 15).to(15, 12, 16)
                    .allFaces((dir, face) -> face.texture("#side")).end();
            if (lvl > 0) {
                b.element().from(1, 1, 1).to(15, 1 + lvl, 15)
                        .allFaces((dir, face) -> face.texture("#fill"))
                        .end();
            }
            modelsByLevel[lvl] = b;
        }

        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(modelsByLevel[state.getValue(QuartzCrucibleBlock.LEVEL)])
                .build());
        simpleBlockItem(block, modelsByLevel[0]);
    }
}
