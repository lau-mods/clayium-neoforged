/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.logistics;

import java.util.Arrays;
import java.util.function.IntSupplier;
import net.claustra01.clayium.world.item.ClayFilterItem;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Shared mutable side-I/O state used by machines, buffers, and utility devices. */
public final class SideConfiguration {
    public static final int SIDE_COUNT = 6;

    private final int[] insertionRoutes;
    private final int[] extractionRoutes;
    private final ItemStack[] filters;
    private final IntSupplier insertionRouteCount;
    private final IntSupplier extractionRouteCount;

    public SideConfiguration(
            int[] insertionRoutes,
            int[] extractionRoutes,
            ItemStack[] filters,
            IntSupplier insertionRouteCount,
            IntSupplier extractionRouteCount) {
        if (insertionRoutes.length != SIDE_COUNT || extractionRoutes.length != SIDE_COUNT) {
            throw new IllegalArgumentException("Side route arrays must contain six entries");
        }
        if (filters != null && filters.length != SIDE_COUNT) {
            throw new IllegalArgumentException("Side filter array must contain six entries");
        }
        this.insertionRoutes = insertionRoutes;
        this.extractionRoutes = extractionRoutes;
        this.filters = filters;
        this.insertionRouteCount = insertionRouteCount;
        this.extractionRouteCount = extractionRouteCount;
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

    public boolean hasFilter(Direction facing, Direction side) {
        return filters != null && !filters[RelativeFace.index(facing, side)].isEmpty();
    }

    public ItemStack filter(Direction facing, Direction side) {
        return filters == null ? ItemStack.EMPTY : filters[RelativeFace.index(facing, side)].copy();
    }

    public void setFilter(Direction facing, Direction side, ItemStack filter) {
        if (filters != null) {
            filters[RelativeFace.index(facing, side)] = filter.isEmpty()
                    ? ItemStack.EMPTY
                    : filter.copyWithCount(1);
        }
    }

    public boolean matchesFilter(Direction facing, Direction side, ItemStack stack) {
        return filters == null
                || filters[RelativeFace.index(facing, side)].isEmpty()
                || ClayFilterItem.matches(filters[RelativeFace.index(facing, side)], stack);
    }

    public int[] insertionRoutes() {
        return insertionRoutes;
    }

    public int[] extractionRoutes() {
        return extractionRoutes;
    }

    public void replaceRoutes(int[] insertion, int[] extraction) {
        copySanitized(insertion, insertionRoutes, insertionRouteCount.getAsInt());
        copySanitized(extraction, extractionRoutes, extractionRouteCount.getAsInt());
    }

    public static int next(int value, int routeCount) {
        return value < 0 ? routeCount > 0 ? 0 : -1 : value + 1 < routeCount ? value + 1 : -1;
    }

    public static boolean pipeConnects(ConfigurableItemDevice device, Direction side) {
        BlockEntity owner = device.ioOwner();
        if (!device.pipeEnabled() || owner.getLevel() == null) return false;
        BlockEntity neighbor = owner.getLevel().getBlockEntity(owner.getBlockPos().relative(side));
        if (neighbor instanceof ConfigurableItemDevice adjacent) {
            return adjacent.ioTransportKind() == device.ioTransportKind();
        }
        if (device.insertionRoute(side) < 0 && device.extractionRoute(side) < 0) return false;
        return device.ioTransportKind() == IoTransportKind.FLUID
                ? owner.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                        owner.getBlockPos().relative(side), side.getOpposite()) != null
                : owner.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                        owner.getBlockPos().relative(side), side.getOpposite()) != null;
    }

    private static void copySanitized(int[] source, int[] target, int routeCount) {
        int[] values = source == target ? source.clone() : source;
        Arrays.fill(target, -1);
        for (int index = 0; index < Math.min(values.length, SIDE_COUNT); index++) {
            int value = values[index];
            target[index] = value >= -1 && value < routeCount ? value : -1;
        }
    }
}
