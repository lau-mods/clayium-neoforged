/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.data.IoMemory;
import net.minecraft.core.Direction;

public interface ConfigurableItemDevice {
    SideMode cycleSide(Direction direction);

    void setFilter(Direction direction, FilterSettings filter);

    IoMemory saveIoMemory();

    void loadIoMemory(IoMemory memory);
}
