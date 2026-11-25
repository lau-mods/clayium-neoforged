package net.claustra01.clayium.logistics;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Shared relative-side routing state for item and fluid devices. */
public final class SideConfiguration {
    private final int[] insertionRoutes;
    private final int[] extractionRoutes;
    @Nullable private final ItemStack[] filters;
    private final IntSupplier insertionRouteCount;
    private final IntSupplier extractionRouteCount;
    private final BiPredicate<ItemStack, ItemStack> filterMatcher;

    public SideConfiguration(
            int[] insertionRoutes,
            int[] extractionRoutes,
            @Nullable ItemStack[] filters,
            IntSupplier insertionRouteCount,
            IntSupplier extractionRouteCount,
            BiPredicate<ItemStack, ItemStack> filterMatcher) {
        if (insertionRoutes.length != RelativeFace.COUNT || extractionRoutes.length != RelativeFace.COUNT) {
            throw new IllegalArgumentException("Side route arrays must contain six entries");
        }
        if (filters != null && filters.length != RelativeFace.COUNT) {
            throw new IllegalArgumentException("Side filter array must contain six entries");
        }
        this.insertionRoutes = insertionRoutes;
        this.extractionRoutes = extractionRoutes;
        this.filters = filters;
        this.insertionRouteCount = insertionRouteCount;
        this.extractionRouteCount = extractionRouteCount;
        this.filterMatcher = filterMatcher;
    }

    public static SideConfiguration withoutFilters(
            int[] insertionRoutes,
            int[] extractionRoutes,
            IntSupplier insertionRouteCount,
            IntSupplier extractionRouteCount) {
        return new SideConfiguration(insertionRoutes, extractionRoutes, null,
                insertionRouteCount, extractionRouteCount, (filter, stack) -> true);
    }

    public int insertionRoute(Direction facing, Direction side) {
        return insertionRoutes[RelativeFace.index(facing, side)];
    }

    public int extractionRoute(Direction facing, Direction side) {
        return extractionRoutes[RelativeFace.index(facing, side)];
    }

    public int cycleInsertion(Direction facing, Direction side) {
        int index = RelativeFace.index(facing, side);
        insertionRoutes[index] = next(insertionRoutes[index], insertionRouteCount.getAsInt());
        return insertionRoutes[index];
    }

    public int cycleExtraction(Direction facing, Direction side) {
        int index = RelativeFace.index(facing, side);
        extractionRoutes[index] = next(extractionRoutes[index], extractionRouteCount.getAsInt());
        return extractionRoutes[index];
    }

    public SideMode mode(Direction facing, Direction side) {
        boolean input = insertionRoute(facing, side) >= 0;
        boolean output = extractionRoute(facing, side) >= 0;
        if (input && output) return SideMode.INPUT_OUTPUT;
        if (input) return SideMode.INPUT;
        if (output) return SideMode.OUTPUT;
        return SideMode.DISABLED;
    }

    public void setRoutes(Direction facing, Direction side, int insertionRoute, int extractionRoute) {
        int index = RelativeFace.index(facing, side);
        insertionRoutes[index] = sanitize(insertionRoute, insertionRouteCount.getAsInt());
        extractionRoutes[index] = sanitize(extractionRoute, extractionRouteCount.getAsInt());
    }

    public boolean hasFilter(Direction facing, Direction side) {
        return filters != null && !filters[RelativeFace.index(facing, side)].isEmpty();
    }

    public ItemStack filter(Direction facing, Direction side) {
        return filters == null ? ItemStack.EMPTY : filters[RelativeFace.index(facing, side)].copy();
    }

    public void setFilter(Direction facing, Direction side, ItemStack filter) {
        if (filters != null) {
            filters[RelativeFace.index(facing, side)] = filter.isEmpty()
                    ? ItemStack.EMPTY : filter.copyWithCount(1);
        }
    }

    public boolean matchesFilter(Direction facing, Direction side, ItemStack stack) {
        if (filters == null) return true;
        ItemStack filter = filters[RelativeFace.index(facing, side)];
        return filter.isEmpty() || filterMatcher.test(filter, stack);
    }

    public int[] insertionRoutes() {
        return insertionRoutes;
    }

    public int[] extractionRoutes() {
        return extractionRoutes;
    }

    @Nullable
    public ItemStack[] filters() {
        return filters;
    }

    public void replaceRoutes(int[] insertion, int[] extraction) {
        copySanitized(insertion, insertionRoutes, insertionRouteCount.getAsInt());
        copySanitized(extraction, extractionRoutes, extractionRouteCount.getAsInt());
    }

    public static int next(int value, int routeCount) {
        return value < 0 ? routeCount > 0 ? 0 : -1 : value + 1 < routeCount ? value + 1 : -1;
    }

    public static boolean pipeConnects(ConfigurableIoDevice device, Direction side) {
        BlockEntity owner = device.ioOwner();
        if (!device.pipeEnabled() || owner.getLevel() == null) return false;
        BlockEntity neighbor = owner.getLevel().getBlockEntity(owner.getBlockPos().relative(side));
        if (neighbor instanceof ConfigurableIoDevice adjacent) {
            if (adjacent.ioTransportKind() != device.ioTransportKind()) return false;
            Direction opposite = side.getOpposite();
            boolean ownPush = device.extractionRoute(side) >= 0
                    && (adjacent.insertionRoute(opposite) >= 0 || adjacent.allowsPassiveInsertion(opposite));
            boolean ownPull = device.insertionRoute(side) >= 0
                    && (adjacent.extractionRoute(opposite) >= 0 || adjacent.allowsPassiveExtraction(opposite));
            boolean adjacentPush = adjacent.extractionRoute(opposite) >= 0
                    && (device.insertionRoute(side) >= 0 || device.allowsPassiveInsertion(side));
            boolean adjacentPull = adjacent.insertionRoute(opposite) >= 0
                    && (device.extractionRoute(side) >= 0 || device.allowsPassiveExtraction(side));
            return ownPush || ownPull || adjacentPush || adjacentPull;
        }
        if (device.insertionRoute(side) < 0 && device.extractionRoute(side) < 0) return false;
        return device.ioTransportKind() == IoTransportKind.FLUID
                ? owner.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                        owner.getBlockPos().relative(side), side.getOpposite()) != null
                : owner.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                        owner.getBlockPos().relative(side), side.getOpposite()) != null;
    }

    private static int sanitize(int value, int count) {
        return value >= -1 && value < count ? value : -1;
    }

    private static void copySanitized(int[] source, int[] target, int count) {
        int[] values = source == target ? source.clone() : source;
        Arrays.fill(target, -1);
        for (int index = 0; index < Math.min(values.length, RelativeFace.COUNT); index++) {
            target[index] = sanitize(values[index], count);
        }
    }
}
