package net.claustra01.clayium.world.area;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.AABB;

public record AreaBounds(BlockPos minimum, BlockPos maximum) {
    public AreaBounds {
        BlockPos first = minimum;
        BlockPos second = maximum;
        minimum = new BlockPos(
                Math.min(first.getX(), second.getX()),
                Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()));
        maximum = new BlockPos(
                Math.max(first.getX(), second.getX()),
                Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ()));
    }

    public static AreaBounds between(
            LevelHeightAccessor level, AreaMarkerType markerType, BlockPos first, BlockPos second) {
        AreaBounds selected = new AreaBounds(first, second);
        int minimumY = switch (markerType) {
            case OPEN_PIT, PRISM -> level.getMinBuildHeight();
            case NORMAL, GROUND_LEVELING -> selected.minimum.getY();
        };
        int maximumY = switch (markerType) {
            case GROUND_LEVELING, PRISM -> level.getMaxBuildHeight() - 1;
            case NORMAL, OPEN_PIT -> selected.maximum.getY();
        };
        return new AreaBounds(
                new BlockPos(selected.minimum.getX(), minimumY, selected.minimum.getZ()),
                new BlockPos(selected.maximum.getX(), maximumY, selected.maximum.getZ()));
    }

    public AreaBounds clampHeight(LevelHeightAccessor level) {
        int minimumBuildHeight = level.getMinBuildHeight();
        int maximumBuildHeight = level.getMaxBuildHeight() - 1;
        int low = Mth.clamp(minimum.getY(), minimumBuildHeight, maximumBuildHeight);
        int high = Mth.clamp(maximum.getY(), minimumBuildHeight, maximumBuildHeight);
        return new AreaBounds(
                new BlockPos(minimum.getX(), Math.min(low, high), minimum.getZ()),
                new BlockPos(maximum.getX(), Math.max(low, high), maximum.getZ()));
    }

    public long volume() {
        return saturatedMultiply(
                saturatedMultiply((long)maximum.getX() - minimum.getX() + 1L,
                        (long)maximum.getY() - minimum.getY() + 1L),
                (long)maximum.getZ() - minimum.getZ() + 1L);
    }

    public BlockPos positionAt(long cursor) {
        long width = (long)maximum.getX() - minimum.getX() + 1L;
        long depth = (long)maximum.getZ() - minimum.getZ() + 1L;
        long layerSize = saturatedMultiply(width, depth);
        long normalized = Math.floorMod(cursor, volume());
        long y = normalized / layerSize;
        long inLayer = normalized % layerSize;
        long z = inLayer / width;
        long x = inLayer % width;
        return new BlockPos(
                minimum.getX() + (int)x,
                minimum.getY() + (int)y,
                minimum.getZ() + (int)z);
    }

    public AABB box() {
        return new AABB(
                minimum.getX(), minimum.getY(), minimum.getZ(),
                maximum.getX() + 1.0D, maximum.getY() + 1.0D, maximum.getZ() + 1.0D);
    }

    private static long saturatedMultiply(long first, long second) {
        return first != 0L && second > Long.MAX_VALUE / first ? Long.MAX_VALUE : first * second;
    }
}
