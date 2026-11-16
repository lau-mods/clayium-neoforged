package net.claustra01.clayium.material;

import java.util.Arrays;
import java.util.Optional;

public enum CompressionTier {
    DENSE_CLAY(0, "dense_clay"),
    COMPRESSED_CLAY(1, "compressed_clay"),
    INDUSTRIAL_CLAY(2, "industrial_clay"),
    ADVANCED_INDUSTRIAL_CLAY(3, "advanced_industrial_clay"),
    ENERGETIC_CLAY(4, "energetic_clay"),
    COMPRESSED_ENERGETIC_CLAY(5, "compressed_energetic_clay"),
    DOUBLE_COMPRESSED_ENERGETIC_CLAY(6, "double_compressed_energetic_clay"),
    TRIPLE_COMPRESSED_ENERGETIC_CLAY(7, "triple_compressed_energetic_clay"),
    QUADRUPLE_COMPRESSED_ENERGETIC_CLAY(8, "quadruple_compressed_energetic_clay"),
    QUINTUPLE_COMPRESSED_ENERGETIC_CLAY(9, "quintuple_compressed_energetic_clay"),
    SEXTUPLE_COMPRESSED_ENERGETIC_CLAY(10, "sextuple_compressed_energetic_clay"),
    SEPTUPLE_COMPRESSED_ENERGETIC_CLAY(11, "septuple_compressed_energetic_clay"),
    OCTUPLE_COMPRESSED_ENERGETIC_CLAY(12, "octuple_compressed_energetic_clay");

    private final int originalMetadata;
    private final String id;
    private final long clayUnits;
    private final long clayEnergy;

    CompressionTier(int originalMetadata, String id) {
        this.originalMetadata = originalMetadata;
        this.id = id;
        this.clayUnits = power(9, originalMetadata + 1);
        this.clayEnergy = originalMetadata < 4 ? 0 : power(10, originalMetadata - 4);
    }

    public int originalMetadata() {
        return originalMetadata;
    }

    public String id() {
        return id;
    }

    public long clayUnits() {
        return clayUnits;
    }

    public long clayEnergy() {
        return clayEnergy;
    }

    public static Optional<CompressionTier> fromId(String id) {
        return Arrays.stream(values()).filter(tier -> tier.id.equals(id)).findFirst();
    }

    private static long power(long base, int exponent) {
        long value = 1;
        for (int index = 0; index < exponent; index++) {
            value = Math.multiplyExact(value, base);
        }
        return value;
    }
}
