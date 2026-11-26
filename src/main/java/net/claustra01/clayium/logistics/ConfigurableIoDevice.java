package net.claustra01.clayium.logistics;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Common contract used by tools, pipe shapes, overlays, capabilities, and automation. */
public interface ConfigurableIoDevice {
    SideConfiguration sideConfiguration();

    BlockEntity ioOwner();

    BooleanProperty ioPipeProperty();

    @Nullable DirectionProperty ioFacingProperty();

    void ioConfigurationChanged();

    default void ioNeighborsChanged() {
        BlockEntity owner = ioOwner();
        if (owner.getLevel() == null) return;
        owner.getLevel().updateNeighborsAt(owner.getBlockPos(), owner.getBlockState().getBlock());
        if (!owner.getLevel().isClientSide) {
            for (Direction side : Direction.values()) {
                net.minecraft.core.BlockPos adjacent = owner.getBlockPos().relative(side);
                BlockState state = owner.getLevel().getBlockState(adjacent);
                owner.getLevel().sendBlockUpdated(adjacent, state, state, 2);
            }
        }
    }

    default IoTransportKind ioTransportKind() {
        return IoTransportKind.ITEM;
    }

    default IoTransportKind insertionTransportKind(int route) {
        return ioTransportKind();
    }

    default IoTransportKind extractionTransportKind(int route) {
        return ioTransportKind();
    }

    /** True when an active neighboring route may insert through this otherwise unconfigured face. */
    default boolean allowsPassiveInsertion(Direction side, IoTransportKind kind) {
        return false;
    }

    /** True when an active neighboring route may extract through this otherwise unconfigured face. */
    default boolean allowsPassiveExtraction(Direction side, IoTransportKind kind) {
        return false;
    }

    default String insertionFaceTexture(int route) {
        return switch (insertionTransportKind(route)) {
            case FLUID -> "import_l";
            case ENERGY -> "import_energy";
            case ITEM -> route == 1 ? "import_energy" : "import";
        };
    }

    default String extractionFaceTexture(int route) {
        return switch (extractionTransportKind(route)) {
            case FLUID -> "export_l";
            case ENERGY -> "export_rf";
            case ITEM -> "export";
        };
    }

    default Direction ioFacing() {
        DirectionProperty property = ioFacingProperty();
        return property == null ? Direction.NORTH : ioOwner().getBlockState().getValue(property);
    }

    default int cycleInsertRoute(Direction side) {
        int route = sideConfiguration().cycleInsertion(ioFacing(), side);
        ioConfigurationChanged();
        return route;
    }

    default int cycleExtractRoute(Direction side) {
        int route = sideConfiguration().cycleExtraction(ioFacing(), side);
        ioConfigurationChanged();
        return route;
    }

    default int insertionRoute(Direction side) {
        return sideConfiguration().insertionRoute(ioFacing(), side);
    }

    default int extractionRoute(Direction side) {
        return sideConfiguration().extractionRoute(ioFacing(), side);
    }

    default SideMode sideMode(Direction side) {
        return sideConfiguration().mode(ioFacing(), side);
    }

    default boolean matchesFilter(Direction side, ItemStack stack) {
        return sideConfiguration().matchesFilter(ioFacing(), side, stack);
    }

    default boolean hasFilter(Direction side) {
        return sideConfiguration().hasFilter(ioFacing(), side);
    }

    default ItemStack filter(Direction side) {
        return sideConfiguration().filter(ioFacing(), side);
    }

    default void setFilter(Direction side, ItemStack filter) {
        sideConfiguration().setFilter(ioFacing(), side, filter);
        ioConfigurationChanged();
    }

    default boolean pipeEnabled() {
        return ioOwner().getBlockState().getValue(ioPipeProperty());
    }

    default boolean togglePipe() {
        BlockEntity owner = ioOwner();
        if (owner.getLevel() == null) return false;
        owner.getLevel().setBlock(owner.getBlockPos(),
                owner.getBlockState().setValue(ioPipeProperty(), !pipeEnabled()), 3);
        ioConfigurationChanged();
        return true;
    }

    default boolean rotate(Direction clickedFace) {
        BlockEntity owner = ioOwner();
        DirectionProperty property = ioFacingProperty();
        if (owner.getLevel() == null || property == null || !clickedFace.getAxis().isHorizontal()) return false;
        Direction current = ioFacing();
        Direction next = current == clickedFace ? clickedFace.getOpposite() : clickedFace;
        owner.getLevel().setBlock(owner.getBlockPos(), owner.getBlockState().setValue(property, next), 3);
        ioConfigurationChanged();
        return true;
    }

    default boolean pipeConnects(Direction side) {
        return SideConfiguration.pipeConnects(this, side);
    }

    default CompoundTag saveIoMemory() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("insertion_routes", sideConfiguration().insertionRoutes().clone());
        tag.putIntArray("extraction_routes", sideConfiguration().extractionRoutes().clone());
        tag.putBoolean("pipe", pipeEnabled());
        tag.putString("facing", ioFacing().getName());
        return tag;
    }

    default void saveIoState(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putIntArray("insertion_routes", sideConfiguration().insertionRoutes().clone());
        tag.putIntArray("extraction_routes", sideConfiguration().extractionRoutes().clone());
        ItemStack[] filters = sideConfiguration().filters();
        if (filters != null) {
            ListTag savedFilters = new ListTag();
            for (ItemStack filter : filters) savedFilters.add(filter.saveOptional(registries));
            tag.put("side_filters", savedFilters);
        }
    }

    default void loadIoState(CompoundTag tag, HolderLookup.Provider registries) {
        sideConfiguration().replaceRoutes(
                tag.getIntArray("insertion_routes"), tag.getIntArray("extraction_routes"));
        ItemStack[] filters = sideConfiguration().filters();
        if (filters != null) {
            java.util.Arrays.fill(filters, ItemStack.EMPTY);
            ListTag savedFilters = tag.getList("side_filters", Tag.TAG_COMPOUND);
            for (int index = 0; index < Math.min(savedFilters.size(), filters.length); index++) {
                filters[index] = ItemStack.parseOptional(registries, savedFilters.getCompound(index));
            }
        }
    }

    default void loadIoMemory(CompoundTag tag) {
        sideConfiguration().replaceRoutes(
                tag.getIntArray("insertion_routes"), tag.getIntArray("extraction_routes"));
        BlockEntity owner = ioOwner();
        if (owner.getLevel() != null) {
            BlockState state = owner.getBlockState().setValue(ioPipeProperty(), tag.getBoolean("pipe"));
            DirectionProperty property = ioFacingProperty();
            Direction facing = Direction.byName(tag.getString("facing"));
            if (property != null && facing != null && facing.getAxis().isHorizontal()) {
                state = state.setValue(property, facing);
            }
            owner.getLevel().setBlock(owner.getBlockPos(), state, 3);
        }
        ioConfigurationChanged();
    }
}
