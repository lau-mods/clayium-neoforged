/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.pan;

import net.minecraft.core.BlockPos;

public interface PanNetworkMember {
    void linkPanCore(BlockPos core, int lifetimeTicks);
}
