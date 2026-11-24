package net.claustra01.clayium.datagen.material;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MaterialBlockStateProvider extends BlockStateProvider {
    private static final Map<String, String> MATERIAL_TEXTURES = Map.ofEntries(
                        Map.entry("aluminium_block", "aluminium"),
            Map.entry("antimatter_block", "antimatter"),
                        Map.entry("clay_steel_block", "claysteel"),
            Map.entry("impure_silicon_block", "impuresilicon"),
            Map.entry("oec_block", "oec"),
                                    Map.entry("opa_block", "opa"),
            Map.entry("pure_antimatter_block", "pureantimatter"),
            Map.entry("silicon_block", "silicon"),
            Map.entry("silicone_block", "silicone"),
            Map.entry("ultimate_alloy_block", "ultimatealloy"));

    public MaterialBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Material Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        for (CompressionTier compressionTier : CompressionTier.values()) {
            if (compressionTier.originalMetadata() < 2) {
                continue;
            }
            cubeAll(
                    MaterialRegistration.COMPRESSION_BLOCKS.get(compressionTier.id()).get(),
                    ClayiumMod.id("block/compressedclay-" + compressionTier.originalMetadata()));
        }
        cubeAll(MaterialRegistration.OEC_BLOCK.get(), ClayiumMod.id("block/oec"));
        for (var entry : MaterialRegistration.MATERIAL_BLOCKS.entrySet()) {
            if (CompressionTier.fromId(entry.getKey()).isPresent() || entry.getKey().equals("clayium_block")) {
                continue;
            }
            String textureName = MATERIAL_TEXTURES.get(entry.getKey());
            if (textureName == null) {
                throw new IllegalStateException("Missing original block texture mapping: " + entry.getKey());
            }
            ResourceLocation texture = textureName.contains(":")
                    ? ResourceLocation.parse(textureName)
                    : ClayiumMod.id("block/" + textureName);
            cubeAll(entry.getValue().get(), texture);
        }
        for (var entry : MaterialRegistration.METAL_BLOCKS.entrySet()) {
            Block block = entry.getValue().get();
            var model = models().getBuilder(entry.getKey())
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .texture("base", ClayiumMod.id("block/metalblock_base"))
                    .texture("dark", ClayiumMod.id("block/metalblock_dark"))
                    .texture("light", ClayiumMod.id("block/metalblock_light"))
                    .texture("particle", ClayiumMod.id("block/metalblock_base"))
                    .renderType("translucent");
            tintedCube(model, "#base", 0, 0.0F, 16.0F);
            tintedCube(model, "#dark", 1, -0.01F, 16.01F);
            tintedCube(model, "#light", 2, -0.02F, 16.02F);
            simpleBlock(block, model);
            simpleBlockItem(block, model);
        }
        for (var entry : MaterialRegistration.SILICONE_BLOCKS.entrySet()) {
            Block block = entry.getValue().get();
            var model = models().getBuilder(entry.getKey())
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .texture("all", ClayiumMod.id("block/siliconecolored"))
                    .texture("particle", ClayiumMod.id("block/siliconecolored"));
            tintedCube(model, "#all", 0, 0.0F, 16.0F);
            simpleBlock(block, model);
            simpleBlockItem(block, model);
        }
    }

    private static void tintedCube(
            net.neoforged.neoforge.client.model.generators.BlockModelBuilder model,
            String texture,
            int tintIndex,
            float from,
            float to) {
        var element = model.element().from(from, from, from).to(to, to, to);
        for (Direction direction : Direction.values()) {
            element.face(direction).texture(texture).tintindex(tintIndex).cullface(direction).end();
        }
        element.end();
    }

    private void cubeAll(Block block, ResourceLocation texture) {
        ModelFile model = models().cubeAll(BuiltInRegistries.BLOCK.getKey(block).getPath(), texture);
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }
}
