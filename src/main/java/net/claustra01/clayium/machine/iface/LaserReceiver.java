package net.claustra01.clayium.machine.iface;

import net.claustra01.clayium.laser.LaserBeam;

public interface LaserReceiver {
    boolean receiveLaser(LaserBeam beam);

    default boolean receiveLaser(long blue, long green, long red) {
        return receiveLaser(new LaserBeam(blue, green, red));
    }
}
