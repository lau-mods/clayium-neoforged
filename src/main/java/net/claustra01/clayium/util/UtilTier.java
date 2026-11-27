package net.claustra01.clayium.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exact tier progression predicates and scaling tables ported from 1.7.10 UtilTier.
 */
public final class UtilTier {
    public static final int MAX_TIER = 13;

    public static final CraftingTierTable GENERIC = createGenericTable();
    public static final CraftingTierTable SMELTER = createSmelterTable();
    public static final CraftingTierTable CA_CONDENSER = createCACondenserTable();
    public static final TransportTierTable MACHINE_TRANSPORT = createMachineTransportTable();
    public static final TransportTierTable BUFFER_TRANSPORT = createBufferTransportTable();

    private UtilTier() {}

    public static boolean canAutoTransfer(int tier) {
        return tier >= 3;
    }

    public static boolean canManualCraft(int tier) {
        return tier <= 2;
    }

    public static boolean acceptWaterWheel(int tier) {
        return tier == 2 || tier == 3;
    }

    public static boolean acceptEnergyClay(int tier) {
        return tier >= 4;
    }

    private static CraftingTierTable createGenericTable() {
        Map<Integer, Float> craftTime = new HashMap<>();
        craftTime.put(5, 0.25F);
        craftTime.put(6, 0.0625F);
        craftTime.put(10, 0.01F);

        Map<Integer, Float> energy = new HashMap<>();
        energy.put(5, 5.0F);
        energy.put(6, 25.0F);
        energy.put(10, 250.0F);

        return new CraftingTierTable(1.0F, craftTime, 1.0F, energy);
    }

    private static CraftingTierTable createSmelterTable() {
        Map<Integer, Float> craftTime = new HashMap<>();
        craftTime.put(4, 2.0F);
        craftTime.put(5, 0.5F);
        craftTime.put(6, 0.125F);
        craftTime.put(7, 0.03F);
        craftTime.put(8, 0.01F);
        craftTime.put(9, 0.0025F);

        Map<Integer, Float> energy = new HashMap<>();
        energy.put(4, 1.0F);
        energy.put(5, 14.0F);
        energy.put(6, 200.0F);
        energy.put(7, 2800.0F);
        energy.put(8, 40000.0F);
        energy.put(9, 560000.0F);

        return new CraftingTierTable(1.0F, craftTime, 1.0F, energy);
    }

    private static CraftingTierTable createCACondenserTable() {
        Map<Integer, Float> craftTime = new HashMap<>();
        craftTime.put(10, 0.1F);
        craftTime.put(11, 0.01F);

        Map<Integer, Float> energy = new HashMap<>();
        energy.put(10, 10.0F);
        energy.put(11, 100.0F);

        return new CraftingTierTable(1.0F, craftTime, 1.0F, energy);
    }

    private static TransportTierTable createMachineTransportTable() {
        Map<Integer, Integer> insertInterval = new HashMap<>();
        Map<Integer, Integer> extractInterval = new HashMap<>();
        Map<Integer, Integer> maxInsert = new HashMap<>();
        Map<Integer, Integer> maxExtract = new HashMap<>();

        for (int tier = 0; tier <= MAX_TIER; tier++) {
            if (tier <= 4) {
                insertInterval.put(tier, 20);
                extractInterval.put(tier, 20);
                maxInsert.put(tier, 8);
                maxExtract.put(tier, 8);
            } else if (tier == 5) {
                insertInterval.put(tier, 2);
                extractInterval.put(tier, 2);
                maxInsert.put(tier, 16);
                maxExtract.put(tier, 16);
            } else {
                insertInterval.put(tier, 1);
                extractInterval.put(tier, 1);
                maxInsert.put(tier, 64);
                maxExtract.put(tier, 64);
            }
        }
        return new TransportTierTable(20, insertInterval, 20, extractInterval, 8, maxInsert, 8, maxExtract);
    }

    private static TransportTierTable createBufferTransportTable() {
        Map<Integer, Integer> insertInterval = new HashMap<>();
        Map<Integer, Integer> extractInterval = new HashMap<>();
        Map<Integer, Integer> maxInsert = new HashMap<>();
        Map<Integer, Integer> maxExtract = new HashMap<>();

        for (int tier = 0; tier <= MAX_TIER; tier++) {
            if (tier <= 4) {
                insertInterval.put(tier, 8);
                extractInterval.put(tier, 8);
                maxInsert.put(tier, 1);
                maxExtract.put(tier, 1);
            } else if (tier == 5) {
                insertInterval.put(tier, 4);
                extractInterval.put(tier, 4);
                maxInsert.put(tier, 4);
                maxExtract.put(tier, 4);
            } else if (tier == 6) {
                insertInterval.put(tier, 2);
                extractInterval.put(tier, 2);
                maxInsert.put(tier, 16);
                maxExtract.put(tier, 16);
            } else {
                insertInterval.put(tier, 1);
                extractInterval.put(tier, 1);
                int amount = switch (tier) {
                    case 7 -> 64;
                    case 8 -> 128;
                    case 9 -> 192;
                    case 10 -> 256;
                    case 11 -> 512;
                    case 12 -> 1024;
                    case 13 -> 6400;
                    default -> 64;
                };
                maxInsert.put(tier, amount);
                maxExtract.put(tier, amount);
            }
        }
        return new TransportTierTable(20, insertInterval, 20, extractInterval, 8, maxInsert, 8, maxExtract);
    }

    public static final class CraftingTierTable {
        private final float defaultCraftTimeMult;
        private final Map<Integer, Float> craftTimeMult;
        private final float defaultEnergyMult;
        private final Map<Integer, Float> energyMult;

        private CraftingTierTable(
                float defaultCraftTimeMult,
                Map<Integer, Float> craftTimeMult,
                float defaultEnergyMult,
                Map<Integer, Float> energyMult) {
            this.defaultCraftTimeMult = defaultCraftTimeMult;
            this.craftTimeMult = Collections.unmodifiableMap(craftTimeMult);
            this.defaultEnergyMult = defaultEnergyMult;
            this.energyMult = Collections.unmodifiableMap(energyMult);
        }

        public float multCraftTime(int tier) {
            return this.craftTimeMult.getOrDefault(tier, this.defaultCraftTimeMult);
        }

        public float multConsumingEnergy(int tier) {
            return this.energyMult.getOrDefault(tier, this.defaultEnergyMult);
        }
    }

    public static final class TransportTierTable {
        private final int defaultInsertInterval;
        private final Map<Integer, Integer> insertInterval;
        private final int defaultExtractInterval;
        private final Map<Integer, Integer> extractInterval;
        private final int defaultMaxInsert;
        private final Map<Integer, Integer> maxInsert;
        private final int defaultMaxExtract;
        private final Map<Integer, Integer> maxExtract;

        private TransportTierTable(
                int defaultInsertInterval,
                Map<Integer, Integer> insertInterval,
                int defaultExtractInterval,
                Map<Integer, Integer> extractInterval,
                int defaultMaxInsert,
                Map<Integer, Integer> maxInsert,
                int defaultMaxExtract,
                Map<Integer, Integer> maxExtract) {
            this.defaultInsertInterval = defaultInsertInterval;
            this.insertInterval = Collections.unmodifiableMap(insertInterval);
            this.defaultExtractInterval = defaultExtractInterval;
            this.extractInterval = Collections.unmodifiableMap(extractInterval);
            this.defaultMaxInsert = defaultMaxInsert;
            this.maxInsert = Collections.unmodifiableMap(maxInsert);
            this.defaultMaxExtract = defaultMaxExtract;
            this.maxExtract = Collections.unmodifiableMap(maxExtract);
        }

        public int autoInsertInterval(int tier) {
            return this.insertInterval.getOrDefault(tier, this.defaultInsertInterval);
        }

        public int autoExtractInterval(int tier) {
            return this.extractInterval.getOrDefault(tier, this.defaultExtractInterval);
        }

        public int maxAutoInsert(int tier) {
            return this.maxInsert.getOrDefault(tier, this.defaultMaxInsert);
        }

        public int maxAutoExtract(int tier) {
            return this.maxExtract.getOrDefault(tier, this.defaultMaxExtract);
        }
    }
}
