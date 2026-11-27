package net.claustra01.clayium.block;

import java.util.Optional;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;

public class ClayTreeSaplingBlock extends SaplingBlock implements ITieredBlock {
    public static final TreeGrower CLAY_TREE_GROWER = new TreeGrower(
            ClayiumMod.MOD_ID + ":clay_tree",
            Optional.empty(),
            Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, ClayiumMod.id("clay_tree"))),
            Optional.empty()
    );

    public ClayTreeSaplingBlock(Properties properties) {
        super(CLAY_TREE_GROWER, properties);
    }

    @Override
    public int getTier() {
        return 7;
    }
}
