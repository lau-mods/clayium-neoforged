package net.claustra01.clayium.fluid;

import java.util.List;

public record FluidTransportSpecification(int tier, String id, int capacity, int transferPerTick) {
    public static final List<FluidTransportSpecification> TRANSLATORS = List.of(
            new FluidTransportSpecification(4, "fluid_translator_tier_4", 4_000, 250),
            new FluidTransportSpecification(5, "fluid_translator_tier_5", 8_000, 500),
            new FluidTransportSpecification(6, "fluid_translator_tier_6", 16_000, 1_000),
            new FluidTransportSpecification(7, "fluid_translator_tier_7", 64_000, 4_000),
            new FluidTransportSpecification(8, "fluid_translator_tier_8", 128_000, 8_000),
            new FluidTransportSpecification(9, "fluid_translator_tier_9", 256_000, 16_000),
            new FluidTransportSpecification(10, "fluid_translator_tier_10", 1_024_000, 64_000),
            new FluidTransportSpecification(11, "fluid_translator_tier_11", 4_096_000, 256_000),
            new FluidTransportSpecification(12, "fluid_translator_tier_12", 16_384_000, 1_024_000),
            new FluidTransportSpecification(13, "fluid_translator_tier_13", 65_536_000, 4_096_000));
    public static final FluidTransportSpecification TRANSFER_MACHINE =
            new FluidTransportSpecification(5, "fluid_transfer_machine_tier_5", 8_000, 500);

    public static final List<FluidTransportSpecification> VALUES = java.util.stream.Stream
            .concat(TRANSLATORS.stream(), java.util.stream.Stream.of(TRANSFER_MACHINE))
            .toList();
}
