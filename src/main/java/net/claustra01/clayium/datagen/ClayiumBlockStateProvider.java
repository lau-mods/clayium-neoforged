package net.claustra01.clayium.datagen;

import java.util.function.Function;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.ClayContainerBlock.FacingMode;
import net.claustra01.clayium.block.ClayContainerBlock.OverlayMode;
import net.claustra01.clayium.block.ClayCraftingTableBlock;
import net.claustra01.clayium.block.ClayMarkerBlock;
import net.claustra01.clayium.block.ClayTreeLeavesBlock;
import net.claustra01.clayium.block.ClayTreeLogBlock;
import net.claustra01.clayium.block.ClayTreeSaplingBlock;
import net.claustra01.clayium.block.LaserReflectorBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.PanCableBlock;
import net.claustra01.clayium.block.QuartzCrucibleBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.block.TieredBlock;
import net.claustra01.clayium.block.UpgradeBlock;
import net.claustra01.clayium.block.UpgradeBlock.UpgradeKind;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ClayiumBlockStateProvider extends BlockStateProvider {
    private static final String TRANSLUCENT = "minecraft:translucent";
    private static final String CUTOUT = "minecraft:cutout";

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
                ModelFile model = models().cross(path, modLoc("block/sapling_claytree")).renderType(CUTOUT);
                simpleBlock(block, model);
                itemModels().withExistingParent(path, mcLoc("item/generated")).texture("layer0", modLoc("block/sapling_claytree"));
            } else if (block instanceof ClayTreeLeavesBlock) {
                ModelFile model = models().cubeAll(path, modLoc("block/leaves_claytree")).renderType("minecraft:cutout_mipped");
                simpleBlockWithItem(block, model);
            } else if (block instanceof ClayTreeLogBlock logBlock) {
                axisBlock(logBlock, modLoc("block/log_claytree"), modLoc("block/log_claytree_top"));
                simpleBlockItem(logBlock, models().getExistingFile(modLoc("block/" + path)));
            } else if (block instanceof PanCableBlock) {
                registerPanCable(block);
            } else if (block instanceof QuartzCrucibleBlock) {
                registerQuartzCrucible(path, block);
            } else if (block instanceof ClayMarkerBlock marker) {
                registerClayMarker(path, marker);
            } else if (block instanceof LaserReflectorBlock reflector) {
                registerLaserReflector(path, reflector);
            } else if (block instanceof StorageContainerBlock storage) {
                registerStorageContainer(path, storage);
            } else if (block instanceof ClayContainerBlock container) {
                registerContainerBlock(path, container);
            } else if (block instanceof UpgradeBlock upgrade) {
                ModelFile model = upgrade.getKind() == UpgradeKind.ENERGY_STORAGE
                        ? cubeAllTranslucent(path, upgrade.getTextureName())
                        : cubeAll(path, upgrade.getTextureName());
                simpleBlockWithItem(block, model);
            } else if (block instanceof TieredBlock tiered) {
                ModelFile model = cubeAll(path, tiered.getTextureName());
                simpleBlockWithItem(block, model);
            }
        }
    }

    private ResourceLocation resolveBlockTexture(String tex) {
        return tex.contains(":") ? ResourceLocation.parse(tex) : modLoc("block/" + tex);
    }

    private ModelFile cubeAll(String name, String textureStem) {
        return models().cubeAll(name, resolveBlockTexture(textureStem));
    }

    private ModelFile cubeAllTranslucent(String name, String textureStem) {
        return models().cubeAll(name, resolveBlockTexture(textureStem)).renderType(TRANSLUCENT);
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
                .renderType(TRANSLUCENT)
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

    private void registerClayMarker(String path, ClayMarkerBlock marker) {
        ResourceLocation tex = resolveBlockTexture(marker.getTextureName());
        ModelFile model = models().getBuilder("block/" + path)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("all", tex)
                .texture("particle", tex)
                .element().from(5, 5, 5).to(11, 11, 11)
                .allFaces((dir, face) -> face.texture("#all"))
                .end();
        simpleBlockWithItem(marker, model);
    }

    private void registerLaserReflector(String path, LaserReflectorBlock reflector) {
        ResourceLocation tex = modLoc("block/laserreflector");
        ModelFile model = models().getBuilder("block/" + path)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(TRANSLUCENT)
                .texture("all", tex)
                .texture("particle", tex)
                // Stepped pyramid oriented along NORTH (-Z is apex, +Z is base), matching SHAPE_Z (4,4,2 -> 12,12,14)
                .element().from(4, 4, 12).to(12, 12, 14).allFaces((d, f) -> f.texture("#all")).end()
                .element().from(4.5F, 4.5F, 10).to(11.5F, 11.5F, 12).allFaces((d, f) -> f.texture("#all")).end()
                .element().from(5, 5, 8).to(11, 11, 10).allFaces((d, f) -> f.texture("#all")).end()
                .element().from(6, 6, 6).to(10, 10, 8).allFaces((d, f) -> f.texture("#all")).end()
                .element().from(7, 7, 4).to(9, 9, 6).allFaces((d, f) -> f.texture("#all")).end()
                .element().from(7.5F, 7.5F, 2).to(8.5F, 8.5F, 4).allFaces((d, f) -> f.texture("#all")).end();
        directionalNorthBlock(reflector, state -> model);
        simpleBlockItem(reflector, model);
    }

    private void registerStorageContainer(String path, StorageContainerBlock storage) {
        ResourceLocation hull = modLoc("block/az91dhull");
        ResourceLocation front = modLoc("block/storagecontainer");
        ResourceLocation side = modLoc("block/storagecontainerside");
        ResourceLocation top = modLoc("block/storagecontainertop");
        ResourceLocation ex = modLoc("block/storagecontainerex");

        ModelFile normal = models().getBuilder("block/" + path)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(TRANSLUCENT)
                .texture("base", hull)
                .texture("front", front)
                .texture("side", side)
                .texture("top", top)
                .texture("particle", hull)
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                .end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).texture("#front").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                .face(Direction.DOWN).texture("#top").cullface(Direction.DOWN).end()
                .end();

        ModelFile upgraded = models().getBuilder("block/" + path + "_upgraded")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(TRANSLUCENT)
                .texture("base", hull)
                .texture("front", front)
                .texture("side", side)
                .texture("top", top)
                .texture("ex", ex)
                .texture("particle", hull)
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                .end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).texture("#front").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                .face(Direction.DOWN).texture("#top").cullface(Direction.DOWN).end()
                .end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#ex").cullface(dir))
                .end();

        getVariantBuilder(storage).forAllStates(state -> {
            ModelFile m = state.getValue(StorageContainerBlock.UPGRADED) ? upgraded : normal;
            return ConfiguredModel.builder()
                    .modelFile(m)
                    .rotationY(horizontalRotationY(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                    .build();
        });

        itemModels().withExistingParent("item/" + path, modLoc("block/" + path))
                .override()
                .predicate(ClayiumMod.id("upgraded"), 1.0F)
                .model(upgraded)
                .end();
    }

    private void registerContainerBlock(String path, ClayContainerBlock container) {
        ModelFile inactiveModel = buildContainerModel(path, container, false);
        ModelFile activeModel = container.hasActiveOverlay() ? buildContainerModel(path + "_active", container, true) : inactiveModel;

        FacingMode mode = container.getFacingMode();
        if (mode == FacingMode.HORIZONTAL) {
            if (container.hasActiveOverlay()) {
                getVariantBuilder(container).forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(ClayContainerBlock.ACTIVE) ? activeModel : inactiveModel)
                        .rotationY(horizontalRotationY(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                        .build());
            } else {
                horizontalBlock(container, inactiveModel);
            }
        } else if (mode == FacingMode.ALL) {
            if (container.hasActiveOverlay()) {
                directionalNorthBlock(container, state -> state.getValue(ClayContainerBlock.ACTIVE) ? activeModel : inactiveModel);
            } else {
                directionalNorthBlock(container, state -> inactiveModel);
            }
        } else {
            simpleBlock(container, inactiveModel);
        }

        // Buffers, Multitrack Buffers, and Fluid Translators display an IO overlay on the front face in inventory
        ModelFile itemModel = inactiveModel;
        if (path.endsWith("_multitrack_buffer")) {
            itemModel = buildInventoryOverlayModel(path + "_item", container.getBaseTexture(), "import_m0");
        } else if (path.endsWith("_buffer")) {
            itemModel = buildInventoryOverlayModel(path + "_item", container.getBaseTexture(), "import");
        } else if (path.endsWith("_fluid_translator")) {
            itemModel = buildInventoryOverlayModel(path + "_item", container.getBaseTexture(), "import_l");
        }
        simpleBlockItem(container, itemModel);
    }

    private ModelFile buildInventoryOverlayModel(String modelName, String baseTex, String frontOverlay) {
        ResourceLocation base = modLoc("block/" + baseTex);
        ResourceLocation overlay = modLoc("block/" + frontOverlay);
        return models().getBuilder("block/" + modelName)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(CUTOUT)
                .texture("base", base)
                .texture("overlay", overlay)
                .texture("particle", base)
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                .end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end()
                .end();
    }

    private ModelFile buildContainerModel(String modelName, ClayContainerBlock container, boolean active) {
        if ("clay_work_table".equals(modelName)) {
            ResourceLocation topBottom = modLoc("block/clayworktable");
            ResourceLocation side = modLoc("block/clayworktable_side");
            return models().cubeBottomTop(modelName, side, topBottom, topBottom);
        }

        ResourceLocation base = modLoc("block/" + container.getBaseTexture());
        if (container instanceof ClayCraftingTableBlock) {
            ResourceLocation topOverlay = modLoc("block/claycraftingtable");
            return models().getBuilder("block/" + modelName)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .renderType(CUTOUT)
                    .texture("base", base)
                    .texture("overlay", topOverlay)
                    .texture("particle", base)
                    .element().from(0, 0, 0).to(16, 4, 16)
                    .allFaces((dir, face) -> face.texture("#base"))
                    .end()
                    .element().from(0, 0, 0).to(16, 4, 16)
                    .face(Direction.UP).texture("#overlay").end()
                    .end();
        }

        OverlayMode overlayMode = container.getOverlayMode();
        if (overlayMode == OverlayMode.VACUUM_CONTAINER) {
            ResourceLocation front = modLoc("block/vacuumcontainer");
            ResourceLocation side = modLoc("block/vacuumcontainerside");
            ResourceLocation top = modLoc("block/vacuumcontainertop");
            return models().getBuilder("block/" + modelName)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .renderType(TRANSLUCENT)
                    .texture("base", base)
                    .texture("front", front)
                    .texture("side", side)
                    .texture("top", top)
                    .texture("particle", base)
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                    .end()
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#front").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                    .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                    .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                    .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                    .end();
        }
        if (overlayMode == OverlayMode.AUTO_CRAFTER || overlayMode == OverlayMode.AUTO_TRADER) {
            String prefix = overlayMode == OverlayMode.AUTO_CRAFTER ? "autocrafter" : "autotrader";
            ResourceLocation top = modLoc("block/" + prefix + "top");
            ResourceLocation side = modLoc("block/" + prefix + "side");
            return models().getBuilder("block/" + modelName)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .renderType(CUTOUT)
                    .texture("base", base)
                    .texture("top", top)
                    .texture("side", side)
                    .texture("particle", base)
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                    .end()
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                    .face(Direction.NORTH).texture("#side").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                    .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                    .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                    .end();
        }
        if (overlayMode == OverlayMode.PAN_DUPLICATOR) {
            ResourceLocation front = modLoc("block/panduplicator");
            ResourceLocation other = modLoc("block/pancasing");
            return models().getBuilder("block/" + modelName)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .renderType(CUTOUT)
                    .texture("base", base)
                    .texture("front", front)
                    .texture("other", other)
                    .texture("particle", base)
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                    .end()
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#front").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#other").cullface(Direction.SOUTH).end()
                    .face(Direction.EAST).texture("#other").cullface(Direction.EAST).end()
                    .face(Direction.WEST).texture("#other").cullface(Direction.WEST).end()
                    .face(Direction.UP).texture("#other").cullface(Direction.UP).end()
                    .face(Direction.DOWN).texture("#other").cullface(Direction.DOWN).end()
                    .end();
        }

        String overlayTex = container.getOverlayTexture();
        if (overlayTex == null || overlayTex.isEmpty()) {
            return models().cubeAll(modelName, base);
        }

        String effectiveOverlay = active ? overlayTex + "_1" : overlayTex;
        ResourceLocation overlay = modLoc("block/" + effectiveOverlay);
        BlockModelBuilder builder = models().getBuilder("block/" + modelName)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(CUTOUT)
                .texture("base", base)
                .texture("overlay", overlay)
                .texture("particle", base);

        builder.element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture("#base").cullface(dir))
                .end();

        switch (overlayMode) {
            case ALL_FACES -> builder.element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture("#overlay").cullface(dir))
                    .end();
            case TOP_ONLY -> builder.element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.UP).texture("#overlay").cullface(Direction.UP).end()
                    .end();
            case FRONT_AND_BACK_MINER -> builder.texture("back", modLoc("block/minerback"))
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#back").cullface(Direction.SOUTH).end()
                    .end();
            default -> builder.element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end()
                    .end();
        }
        return builder;
    }

    private void directionalNorthBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            int rotX = switch (dir) {
                case UP -> 270;
                case DOWN -> 90;
                default -> 0;
            };
            int rotY = switch (dir) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder()
                    .modelFile(modelFunc.apply(state))
                    .rotationX(rotX)
                    .rotationY(rotY)
                    .build();
        });
    }

    private static int horizontalRotationY(Direction dir) {
        return switch (dir) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private void registerPanCable(Block block) {
        ResourceLocation tex = modLoc("block/pancable");
        ModelFile core = models().getBuilder("block/pan_cable_core")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(TRANSLUCENT)
                .texture("cable", tex)
                .texture("particle", tex)
                .element().from(6, 6, 6).to(10, 10, 10)
                .allFaces((dir, face) -> face.texture("#cable"))
                .end();

        ModelFile arm = models().getBuilder("block/pan_cable_arm")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(TRANSLUCENT)
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
