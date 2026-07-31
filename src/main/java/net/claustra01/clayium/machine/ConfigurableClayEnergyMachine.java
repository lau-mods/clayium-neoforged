/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.energy.ClayEnergyReceiver;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;

/** Architectural contract: every CE-powered item machine exposes configurable side I/O and piping. */
public interface ConfigurableClayEnergyMachine extends ClayEnergyReceiver, ConfigurableItemDevice {
}
