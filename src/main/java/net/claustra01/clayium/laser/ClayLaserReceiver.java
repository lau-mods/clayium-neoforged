/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.laser;

import net.minecraft.core.Direction;

public interface ClayLaserReceiver {
    boolean receiveClayLaser(ClayLaser laser, Direction incomingSide);
}
