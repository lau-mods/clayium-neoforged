/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.energy;

/** Internal CE endpoint. FE exposure is intentionally not part of this API. */
public interface ClayEnergyReceiver {
    long receiveClayEnergy(long amount, boolean simulate);
}
