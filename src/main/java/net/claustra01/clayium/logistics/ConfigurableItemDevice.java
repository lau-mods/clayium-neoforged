/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.data.IoMemory;
import net.minecraft.core.Direction;

public interface ConfigurableItemDevice {
    int cycleInsertRoute(Direction direction);

    int cycleExtractRoute(Direction direction);

    boolean togglePipe();

    boolean rotate(Direction clickedFace);

    int insertionRoute(Direction direction);

    int extractionRoute(Direction direction);

    boolean hasFilter(Direction direction);

    void setFilter(Direction direction, FilterSettings filter);

    IoMemory saveIoMemory();

    void loadIoMemory(IoMemory memory);
}
