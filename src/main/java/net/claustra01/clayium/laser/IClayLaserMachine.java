package net.claustra01.clayium.laser;

import net.minecraft.core.Direction;

/**
 * Receiver interface for blocks irradiated by a {@link ClayLaser} (Clay Reactor core, Laser Interface).
 */
public interface IClayLaserMachine {
    boolean irradiateClayLaser(ClayLaser laser, Direction incomingSide);
}
