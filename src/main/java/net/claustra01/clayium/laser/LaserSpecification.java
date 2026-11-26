package net.claustra01.clayium.laser;

import java.util.List;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;

public record LaserSpecification(
        int tier, String id, ClayEnergy energyPerTick, LaserBeam beam) {
    private static final OriginalRegistryCatalog.MachineFamily FAMILY = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
            .filter(candidate -> candidate.legacyName().equals("ClayEnergyLaser"))
            .findFirst().orElseThrow();
    public static final List<LaserSpecification> VALUES = List.of(
            new LaserSpecification(7, OriginalRegistryCatalog.machineId(FAMILY, 7), ClayEnergy.ofInternal(40_000L),
                    new LaserBeam(1L, 0L, 0L)),
            new LaserSpecification(8, OriginalRegistryCatalog.machineId(FAMILY, 8), ClayEnergy.ofInternal(400_000L),
                    new LaserBeam(0L, 1L, 0L)),
            new LaserSpecification(9, OriginalRegistryCatalog.machineId(FAMILY, 9), ClayEnergy.ofInternal(4_000_000L),
                    new LaserBeam(0L, 0L, 1L)),
            new LaserSpecification(10, OriginalRegistryCatalog.machineId(FAMILY, 10), ClayEnergy.ofInternal(40_000_000L),
                    new LaserBeam(3L, 3L, 3L)));
}
