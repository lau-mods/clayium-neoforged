/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.energy;

import net.claustra01.clayium.config.ClayiumConfig;

/** Server-side policy for throttled display synchronization. */
public final class ClayEnergySyncPolicy {
    private ClayEnergySyncPolicy() {
    }

    public static boolean shouldSync(long gameTime, long lastSyncGameTime) {
        if (lastSyncGameTime < 0) {
            return true;
        }
        return gameTime - lastSyncGameTime >= ClayiumConfig.CE_SYNC_INTERVAL_TICKS.get();
    }
}
