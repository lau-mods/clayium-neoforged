package net.claustra01.clayium.laser;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.world.claytree.ClayTreeLaserConversion;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class LaserIrradiation {
    public static final int HISTORY_LENGTH = 10;
    public static final long SAPLING_STAGE_MINIMUM = 300L;
    public static final long SAPLING_STAGE_MAXIMUM = 999L;
    public static final long CLAY_TREE_MINIMUM = 300_000L;

    private static final Map<Block, Block> ORE_CONVERSIONS = Map.ofEntries(
            Map.entry(Blocks.COAL_ORE, Blocks.COAL_BLOCK),
            Map.entry(Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_BLOCK),
            Map.entry(Blocks.IRON_ORE, Blocks.IRON_BLOCK),
            Map.entry(Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_BLOCK),
            Map.entry(Blocks.GOLD_ORE, Blocks.GOLD_BLOCK),
            Map.entry(Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_BLOCK),
            Map.entry(Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK),
            Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_BLOCK),
            Map.entry(Blocks.REDSTONE_ORE, Blocks.REDSTONE_BLOCK),
            Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.REDSTONE_BLOCK),
            Map.entry(Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK),
            Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, Blocks.LAPIS_BLOCK),
            Map.entry(Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK),
            Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, Blocks.EMERALD_BLOCK));

    private final long[] history = new long[HISTORY_LENGTH];
    private long totalEnergy;
    private BlockPos target;
    private String targetBlock = "";
    private int targetDistance;

    public boolean irradiate(ServerLevel level, BlockPos source, LaserPath path, LaserBeam beam) {
        if (path.target() == null) {
            clearTarget();
            return false;
        }
        BlockState state = level.getBlockState(path.target());
        String blockKey = state.toString();
        if (!path.target().equals(target) || path.length() != targetDistance || !blockKey.equals(targetBlock)) {
            reset(path.target(), blockKey, path.length());
        }
        long current = Math.max(0L, (long)beam.energy());
        if (current <= 0L) {
            return false;
        }
        totalEnergy = saturatedAdd(totalEnergy, current);
        long minimum = current;
        for (long previous : history) {
            minimum = Math.min(minimum, previous);
        }
        totalEnergy = scale(totalEnergy, minimum, current);
        System.arraycopy(history, 1, history, 0, history.length - 1);
        history[history.length - 1] = current;

        UUID owner = UUID.nameUUIDFromBytes(
                ("clayium-laser:" + source.asLong()).getBytes(StandardCharsets.UTF_8));
        BlockPos position = path.target();
        Block block = state.getBlock();
        float hardness = state.getDestroySpeed(level, position);
        long rockThreshold = Math.max(0L, (long)(hardness + 1.0F)) * 100L;
        if (hardness >= 0.0F && state.is(BlockTags.MINEABLE_WITH_PICKAXE) && totalEnergy >= rockThreshold) {
            Block converted = ORE_CONVERSIONS.get(block);
            BlockOperationResult result = converted == null
                    ? BlockOperationService.breakBlockToWorld(level, source, position, owner)
                    : BlockOperationService.replaceBlock(level, source, position, owner, converted.defaultBlockState());
            return result == BlockOperationResult.SUCCESS;
        }
        if (state.is(ClayTreeLaserConversion.LASER_TREE_SOURCES) && totalEnergy >= CLAY_TREE_MINIMUM) {
            return BlockOperationService.replaceBlock(level, source, position, owner,
                    ClayTreeRegistration.CLAY_TREE_SAPLING.get().defaultBlockState()) == BlockOperationResult.SUCCESS;
        }
        if (state.is(ClayTreeLaserConversion.LASER_TREE_SOURCES)
                && totalEnergy >= SAPLING_STAGE_MINIMUM && totalEnergy <= SAPLING_STAGE_MAXIMUM
                && BlockOperationService.replaceBlock(level, source, position, owner, state)
                        == BlockOperationResult.SUCCESS) {
            if (block instanceof SaplingBlock sapling) {
                sapling.advanceTree(level, position, state, level.random);
                return true;
            }
            if (block instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, position, state)) {
                growable.performBonemeal(level, level.random, position, state);
                return true;
            }
        }
        return false;
    }

    public void clearTarget() {
        reset(null, "", 0);
    }

    private void reset(BlockPos position, String block, int distance) {
        Arrays.fill(history, 0L);
        totalEnergy = 0L;
        target = position == null ? null : position.immutable();
        targetBlock = block;
        targetDistance = distance;
    }

    public void save(CompoundTag tag) {
        tag.putLong("total", totalEnergy);
        tag.putLongArray("history", history);
        if (target != null) {
            tag.putLong("target", target.asLong());
        }
        tag.putString("target_block", targetBlock);
        tag.putInt("target_distance", targetDistance);
    }

    public void load(CompoundTag tag) {
        totalEnergy = Math.max(0L, tag.getLong("total"));
        long[] savedHistory = tag.getLongArray("history");
        Arrays.fill(history, 0L);
        System.arraycopy(savedHistory, 0, history, 0, Math.min(history.length, savedHistory.length));
        target = tag.contains("target") ? BlockPos.of(tag.getLong("target")) : null;
        targetBlock = tag.getString("target_block");
        targetDistance = tag.getInt("target_distance");
    }

    public static Block conversionFor(Block ore) {
        return ORE_CONVERSIONS.get(ore);
    }

    private static long scale(long value, long numerator, long denominator) {
        if (numerator == 0L) {
            return 0L;
        }
        double scaled = (double)value * numerator / denominator;
        return scaled >= Long.MAX_VALUE ? Long.MAX_VALUE : (long)scaled;
    }

    private static long saturatedAdd(long first, long second) {
        return Long.MAX_VALUE - first < second ? Long.MAX_VALUE : first + second;
    }
}
