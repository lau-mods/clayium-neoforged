package net.claustra01.clayium.world.claytree;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ClayTreeLaserConversion {
    public static final double REQUIRED_ENERGY = 1_000.0D;
    public static final TagKey<Block> LASER_TREE_SOURCES = TagKey.create(
            Registries.BLOCK, ClayiumMod.id("laser_tree_sources"));

    public static LaserReceiver receiver(ServerLevel level, BlockPos position) {
        return beam -> receive(level, position, beam);
    }

    public static boolean receive(ServerLevel level, BlockPos position, LaserBeam beam) {
        if (!level.getBlockState(position).is(LASER_TREE_SOURCES)
                || !ClayTreeRegistration.CLAY_TREE_SAPLING.get().defaultBlockState().canSurvive(level, position)) {
            return false;
        }
        if (beam.energy() < REQUIRED_ENERGY) {
            return true;
        }
        UUID owner = UUID.nameUUIDFromBytes(("clayium-laser:" + position.asLong())
                .getBytes(StandardCharsets.UTF_8));
        return BlockOperationService.replaceBlock(level, position, position, owner,
                ClayTreeRegistration.CLAY_TREE_SAPLING.get().defaultBlockState())
                == BlockOperationResult.SUCCESS;
    }

    private ClayTreeLaserConversion() {
    }
}
