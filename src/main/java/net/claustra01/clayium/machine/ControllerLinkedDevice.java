/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.minecraft.core.BlockPos;

/** A multiblock component whose only persistent relationship is its controller position. */
public interface ControllerLinkedDevice {
    void linkMachine(BlockPos controller);
    void unlinkMachine(BlockPos controller);
}
