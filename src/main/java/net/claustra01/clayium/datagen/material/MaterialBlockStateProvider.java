package net.claustra01.clayium.datagen.material;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
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
    }

    private void cubeAll(Block block, ResourceLocation texture) {
        ModelFile model = models().cubeAll(BuiltInRegistries.BLOCK.getKey(block).getPath(), texture);
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }
}
