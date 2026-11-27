package net.claustra01.clayium.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class CoreUtilsTest {
    @Test
    void testClayTierPredicatesAndPrefixes() {
        assertEquals("", ClayTier.NONE.prefix());
        assertEquals("clay_steel", ClayTier.CLAY_STEEL.idPrefix());
        assertEquals("OPA", ClayTier.OPA.prefix());

        assertTrue(UtilTier.canManualCraft(2));
        assertFalse(UtilTier.canManualCraft(3));
        assertTrue(UtilTier.canAutoTransfer(3));
        assertFalse(UtilTier.canAutoTransfer(2));
        assertTrue(UtilTier.acceptWaterWheel(2));
        assertTrue(UtilTier.acceptWaterWheel(3));
        assertFalse(UtilTier.acceptWaterWheel(4));
        assertTrue(UtilTier.acceptEnergyClay(4));
        assertFalse(UtilTier.acceptEnergyClay(3));
    }

    @Test
    void testUtilTierScalingTables() {
        assertEquals(0.25F, UtilTier.GENERIC.multCraftTime(5));
        assertEquals(5.0F, UtilTier.GENERIC.multConsumingEnergy(5));
        assertEquals(0.01F, UtilTier.GENERIC.multCraftTime(10));
        assertEquals(250.0F, UtilTier.GENERIC.multConsumingEnergy(10));

        assertEquals(2.0F, UtilTier.SMELTER.multCraftTime(4));
        assertEquals(560000.0F, UtilTier.SMELTER.multConsumingEnergy(9));

        assertEquals(0.01F, UtilTier.CA_CONDENSER.multCraftTime(11));
        assertEquals(100.0F, UtilTier.CA_CONDENSER.multConsumingEnergy(11));

        assertEquals(20, UtilTier.MACHINE_TRANSPORT.autoInsertInterval(4));
        assertEquals(2, UtilTier.MACHINE_TRANSPORT.autoInsertInterval(5));
        assertEquals(64, UtilTier.MACHINE_TRANSPORT.maxAutoExtract(6));
        assertEquals(6400, UtilTier.BUFFER_TRANSPORT.maxAutoExtract(13));
    }

    @Test
    void testClayDirectionRoundTrip() {
        for (Direction front : Direction.values()) {
            for (RelativeSide side : RelativeSide.values()) {
                Direction worldDir = ClayDirection.sideToDirection(front, side);
                assertEquals(side, ClayDirection.directionToSide(front, worldDir));
            }
        }
        assertEquals(Direction.NORTH, ClayDirection.sideToDirection(Direction.NORTH, RelativeSide.FRONT));
        assertEquals(Direction.SOUTH, ClayDirection.sideToDirection(Direction.NORTH, RelativeSide.BACK));
        assertEquals(Direction.WEST, ClayDirection.sideToDirection(Direction.NORTH, RelativeSide.LEFT));
        assertEquals(Direction.EAST, ClayDirection.sideToDirection(Direction.NORTH, RelativeSide.RIGHT));
        assertEquals(Direction.UP, ClayDirection.sideToDirection(Direction.NORTH, RelativeSide.UP));
        assertEquals(Direction.DOWN, ClayDirection.sideToDirection(Direction.NORTH, RelativeSide.DOWN));
    }

    @Test
    void testFormattingMatchesOriginal() {
        assertEquals("0", ClayiumFormatting.formatClayEnergy(0L));
        assertEquals("10u", ClayiumFormatting.formatClayEnergy(1L));
        assertEquals("100u", ClayiumFormatting.formatClayEnergy(10L));
        assertEquals("1m", ClayiumFormatting.formatClayEnergy(100L));
        assertEquals("1", ClayiumFormatting.formatClayEnergy(100_000L));
        assertEquals("1.000", ClayiumFormatting.formatClayEnergy(100_000L, false));
        assertEquals("10k", ClayiumFormatting.formatClayEnergy(1_000_000_000L));

        assertEquals("99999", ClayiumFormatting.formatStackSize(99_999L));
        assertEquals("100k", ClayiumFormatting.formatStackSize(100_000L, true));
        assertEquals("100k", ClayiumFormatting.formatStackSize(100_000L, false));
        assertEquals("1M", ClayiumFormatting.formatStackSize(1_000_000L, true));
        assertEquals("1.00M", ClayiumFormatting.formatStackSize(1_000_000L, false));
        assertEquals("1.23M", ClayiumFormatting.formatStackSize(1_234_567L, true));
        assertEquals("12.3M", ClayiumFormatting.formatStackSize(12_345_678L, true));
        assertEquals("1,000,000", ClayiumFormatting.formatFE(1_000_000L));
    }

    @Test
    void testProgressionRateClamping() {
        assertEquals(1, ProgressionRate.multiplyI(1, 0.01));
        assertEquals(-1, ProgressionRate.multiplyI(-1, 0.01));
        assertEquals(0, ProgressionRate.multiplyI(0, 10.0));
        assertEquals(64, ProgressionRate.multiplyStackSize(32, 5.0));
        assertEquals(1L, ProgressionRate.divideL(1L, 10.0));
        assertEquals(64, ProgressionRate.divideStackSize(16, 0.1));
    }
}
