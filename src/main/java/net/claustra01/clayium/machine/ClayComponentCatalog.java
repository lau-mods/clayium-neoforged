/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.machine;

import java.util.List;

/** Clay components and circuits used throughout the original progression. */
public final class ClayComponentCatalog {
    public record Entry(String id, String displayName, String originalTexture) {
    }

    public static final List<Entry> ENTRIES = List.of(
            item("clay_needle", "Clay Needle", "clayneedle"),
            item("clay_pipe", "Clay Pipe", "claypipe"),
            item("clay_grinding_head", "Clay Grinding Head", "claygrindinghead"),
            item("clay_bearing", "Clay Bearing", "claybearing"),
            item("clay_spindle", "Clay Spindle", "clayspindle"),
            item("clay_cutting_head", "Clay Cutting Head", "claycuttinghead"),
            item("clay_water_wheel_component", "Clay Water Wheel", "claywaterwheel"),
            item("clay_dust", "Clay Dust", "claydust"),
            item("dense_clay_short_stick", "Short Dense Clay Stick", "shortdenseclaystick"),
            item("dense_clay_ring", "Dense Clay Ring", "denseclayring"),
            item("dense_clay_small_ring", "Small Dense Clay Ring", "smalldenseclayring"),
            item("dense_clay_blade", "Dense Clay Blade", "denseclayblade"),
            item("dense_clay_needle", "Dense Clay Needle", "denseclayneedle"),
            item("dense_clay_disc", "Dense Clay Disc", "denseclaydisc"),
            item("dense_clay_small_disc", "Small Dense Clay Disc", "smalldenseclaydisc"),
            item("dense_clay_cylinder", "Dense Clay Cylinder", "denseclaycylinder"),
            item("dense_clay_pipe", "Dense Clay Pipe", "denseclaypipe"),
            item("dense_clay_large_plate", "Large Dense Clay Plate", "largedenseclayplate"),
            item("dense_clay_grinding_head", "Dense Clay Grinding Head", "denseclaygrindinghead"),
            item("dense_clay_bearing", "Dense Clay Bearing", "denseclaybearing"),
            item("dense_clay_spindle", "Dense Clay Spindle", "denseclayspindle"),
            item("dense_clay_cutting_head", "Dense Clay Cutting Head", "denseclaycuttinghead"),
            item("dense_clay_water_wheel_component", "Dense Clay Water Wheel", "denseclaywaterwheel"),
            item("dense_clay_dust", "Dense Clay Dust", "denseclaydust"),
            item("industrial_clay_plate", "Industrial Clay Plate", "indclayplate"),
            item("industrial_clay_large_plate", "Large Industrial Clay Plate", "indclaylargeplate"),
            item("industrial_clay_dust", "Industrial Clay Dust", "indclaydust"),
            item("advanced_industrial_clay_plate", "Advanced Industrial Clay Plate", "advindclayplate"),
            item("advanced_industrial_clay_large_plate", "Large Advanced Industrial Clay Plate", "advindclaylargeplate"),
            item("advanced_industrial_clay_dust", "Advanced Industrial Clay Dust", "advindclaydust"),
            item("energetic_clay_dust", "Energetic Clay Dust", "engclaydust"),
            item("calcareous_clay_dust", "Calcareous Clay Dust", "calclaydust"),
            item("compressed_clay_shard", "Compressed Clay Shard", "compressedclay-shard-1"),
            item("industrial_clay_shard", "Industrial Clay Shard", "compressedclay-shard-2"),
            item("advanced_industrial_clay_shard", "Advanced Industrial Clay Shard", "compressedclay-shard-3"),
            item("clay_circuit", "Clay Circuit", "claycircuit"),
            item("simple_circuit", "Simple Circuit", "simplecircuit"),
            item("basic_circuit", "Basic Circuit", "basiccircuit"),
            item("advanced_circuit", "Advanced Circuit", "advancedcircuit"),
            item("precision_circuit", "Precision Circuit", "precisioncircuit"),
            item("integrated_circuit", "Integrated Circuit", "integratedcircuit"),
            item("cee_board", "CEE Board", "ceeboard"),
            item("cee_circuit", "CEE Circuit", "ceecircuit"),
            item("clay_energy_excitor", "Clay Energy Excitor", "cee"),
            item("laser_parts", "Laser Parts", "laserparts"),
            item("impure_silicon_plate", "Impure Silicon Plate", "impuresiliconplate"),
            item("silicon_plate", "Silicon Plate", "siliconplate"),
            item("clay_core", "Clay Core", "claycore"),
            item("clay_brain", "Clay Brain", "claybrain"));

    private ClayComponentCatalog() {
    }

    private static Entry item(String id, String displayName, String originalTexture) {
        return new Entry(id, displayName, originalTexture);
    }
}
