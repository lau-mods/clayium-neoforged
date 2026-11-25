package net.claustra01.clayium.logistics;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    default boolean allowsPassiveInsertion(Direction side) {
        return false;
    }

    default boolean allowsPassiveExtraction(Direction side) {
        return false;
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

    default void loadIoMemory(CompoundTag tag) {
        if (tag.contains("insertion_routes") || tag.contains("extraction_routes")) {
            int[] insertion = tag.contains("insertion_routes")
                    ? tag.getIntArray("insertion_routes") : sideConfiguration().insertionRoutes().clone();
            int[] extraction = tag.contains("extraction_routes")
                    ? tag.getIntArray("extraction_routes") : sideConfiguration().extractionRoutes().clone();
            sideConfiguration().replaceRoutes(insertion, extraction);
        } else {
            Direction savedFacing = Direction.byName(tag.getString("facing"));
            Direction relativeFacing = savedFacing != null && savedFacing.getAxis().isHorizontal()
                    ? savedFacing : ioFacing();
            for (Direction side : Direction.values()) {
                String legacy = tag.getString(side.getName());
                int insertion = switch (legacy) {
                    case "INPUT", "INPUT_OUTPUT" -> 0;
                    case "FUEL_INPUT" -> 1;
                    default -> -1;
                };
                int extraction = switch (legacy) {
                    case "OUTPUT", "INPUT_OUTPUT" -> 0;
                    default -> -1;
                };
                if (!legacy.isEmpty()) sideConfiguration().setRoutes(relativeFacing, side, insertion, extraction);
            }
        }
        BlockEntity owner = ioOwner();
        if (owner.getLevel() != null) {
            BlockState state = tag.contains("pipe")
                    ? owner.getBlockState().setValue(ioPipeProperty(), tag.getBoolean("pipe"))
                    : owner.getBlockState();
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
