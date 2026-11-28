package net.claustra01.clayium.laser;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;

/**
 * Contract for BlockEntities emitting Clay Lasers (Clay Energy Laser, Laser Reflector).
 */
public interface IClayLaserManager {
    boolean isIrradiating();

    int getLaserLength();

    Direction getLaserDirection();

    @Nullable
    ClayLaser getIrradiatingClayLaser();
}
