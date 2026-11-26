package net.claustra01.clayium.world.claytree;

import java.util.Optional;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ClayTreeSaplingBlock extends SaplingBlock {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CLAY_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ClayiumMod.id("clay_tree"));
    private static final TreeGrower GROWER = new TreeGrower(
            "clayium_clay_tree",
            Optional.empty(),
            Optional.of(CLAY_TREE),
            Optional.empty());

    public ClayTreeSaplingBlock(BlockBehaviour.Properties properties) {
        super(GROWER, properties);
    }
}
