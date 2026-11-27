package net.claustra01.clayium.block;

/**
 * Compressed Clay blocks (tiers 0..12). Tiers >= 4 carry 10^(tier+1) internal Clay Energy units.
 */
public class CompressedClayBlock extends TieredBlock {
    public CompressedClayBlock(Properties properties, int tier) {
        super(properties, tier, "compressedclay-" + tier);
    }

    public long getClayEnergyValue() {
        return clayEnergyForTier(getTier());
    }

    public static long clayEnergyForTier(int tier) {
        if (tier < 4) {
            return 0L;
        }
        long ce = 1L;
        for (int i = 0; i < tier + 1; i++) {
            ce *= 10L;
        }
        return ce;
    }
}
