package net.claustra01.clayium.laser;

import net.minecraft.core.Direction;

public interface LaserVisual {
    Direction direction();

    LaserBeam visibleBeam();

    int laserLength();

    boolean isIrradiating();
}
