/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

import net.claustra01.clayium.data.IoMemory;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.ItemStack;

public interface ConfigurableItemDevice {
    SideConfiguration sideConfiguration();

    BlockEntity ioOwner();

    BooleanProperty ioPipeProperty();

    DirectionProperty ioFacingProperty();

    void ioConfigurationChanged();

    default IoTransportKind ioTransportKind() {
        return IoTransportKind.ITEM;
    }

    default Direction ioFacing() {
        return ioOwner().getBlockState().getValue(ioFacingProperty());
    }

    default int cycleInsertRoute(Direction direction) {
        int route = sideConfiguration().cycleInsertion(ioFacing(), direction);
        ioConfigurationChanged();
        return route;
    }

    default int cycleExtractRoute(Direction direction) {
        int route = sideConfiguration().cycleExtraction(ioFacing(), direction);
        ioConfigurationChanged();
        return route;
    }

    default boolean togglePipe() {
        BlockEntity owner = ioOwner();
        if (owner.getLevel() == null) return false;
        boolean pipe = !pipeEnabled();
        owner.getLevel().setBlock(owner.getBlockPos(), owner.getBlockState().setValue(ioPipeProperty(), pipe), 3);
        ioConfigurationChanged();
        return pipe;
    }

    default boolean rotate(Direction clickedFace) {
        BlockEntity owner = ioOwner();
        if (owner.getLevel() == null || !clickedFace.getAxis().isHorizontal()) return false;
        Direction current = ioFacing();
        Direction next = clickedFace == current ? clickedFace.getOpposite() : clickedFace;
        owner.getLevel().setBlock(owner.getBlockPos(), owner.getBlockState().setValue(ioFacingProperty(), next), 3);
        ioConfigurationChanged();
        return true;
    }

    default int insertionRoute(Direction direction) {
        return sideConfiguration().insertionRoute(ioFacing(), direction);
    }

    default int extractionRoute(Direction direction) {
        return sideConfiguration().extractionRoute(ioFacing(), direction);
    }

    default boolean hasFilter(Direction direction) {
        return sideConfiguration().hasFilter(ioFacing(), direction);
    }

    default ItemStack filter(Direction direction) {
        return sideConfiguration().filter(ioFacing(), direction);
    }

    default void setFilter(Direction direction, ItemStack filter) {
        sideConfiguration().setFilter(ioFacing(), direction, filter);
        ioConfigurationChanged();
    }

    default boolean matchesFilter(Direction direction, ItemStack stack) {
        return sideConfiguration().matchesFilter(ioFacing(), direction, stack);
    }

    default boolean pipeEnabled() {
        return ioOwner().getBlockState().getValue(ioPipeProperty());
    }

    /** Pipe arms join every adjacent Clayium device in the same transport domain. */
    default boolean pipeConnects(Direction side) {
        return SideConfiguration.pipeConnects(this, side);
    }

    default String insertionIcon(Direction side) {
        return insertionRoute(side) >= 0 ? "import" : "";
    }

    default String extractionIcon(Direction side) {
        return extractionRoute(side) >= 0 ? "export" : "";
    }

    default IoMemory saveIoMemory() {
        return IoMemory.of(sideConfiguration().insertionRoutes(), sideConfiguration().extractionRoutes(),
                pipeEnabled(), ioFacing().getName());
    }

    default void loadIoMemory(IoMemory memory) {
        sideConfiguration().replaceRoutes(
                memory.insertionRoutesOrDefault(sideConfiguration().insertionRoutes()),
                memory.extractionRoutesOrDefault(sideConfiguration().extractionRoutes()));
        BlockEntity owner = ioOwner();
        if (owner.getLevel() != null) {
            Direction facing = Direction.byName(memory.facing());
            BlockState state = owner.getBlockState().setValue(ioPipeProperty(), memory.pipe());
            if (facing != null && facing.getAxis().isHorizontal()) state = state.setValue(ioFacingProperty(), facing);
            owner.getLevel().setBlock(owner.getBlockPos(), state, 3);
        }
        ioConfigurationChanged();
    }
}
