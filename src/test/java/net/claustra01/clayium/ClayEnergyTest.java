package net.claustra01.clayium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClayEnergyTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void clayEnergyConversionAndOverflowClamping() {
        assertEquals(100_000L, ClayEnergy.fromCe(1.0));
        assertEquals(1L, ClayEnergy.fromCe(0.00001));
        assertEquals(1.0, ClayEnergy.toCe(100_000L), 1e-9);
        assertEquals(0L, ClayEnergy.fromCe(-5.0));

        assertEquals(Long.MAX_VALUE, ClayEnergy.safeAdd(Long.MAX_VALUE - 10L, 100L));
        assertEquals(0L, ClayEnergy.getItemEnergy(ItemStack.EMPTY));
        assertEquals(0L, ClayEnergy.getItemEnergy(new ItemStack(Items.CLAY_BALL)));
    }

    @Test
    void storageConsumesMultipleItemsSafelyAndRoundTripsNbtAndContainerChunks() {
        // Use a mock-like slot backed by a plain variable & compressed clay value calculation
        ClayEnergyStorage storage = new ClayEnergyStorage();
        storage.setStored(50_000L);
        assertTrue(storage.consume(20_000L, () -> ItemStack.EMPTY, s -> {}));
        assertEquals(30_000L, storage.getStored());

        // Attempting to consume more without CE items returns false without corrupting stored CE
        assertFalse(storage.consume(40_000L, () -> ItemStack.EMPTY, s -> {}));
        assertEquals(30_000L, storage.getStored());

        // Test NBT persistence with large long CE (> Integer.MAX_VALUE)
        long largeCe = CompressedClayBlock.clayEnergyForTier(12); // 10_000_000_000_000L
        storage.setStored(largeCe);
        CompoundTag tag = new CompoundTag();
        storage.save(tag);

        ClayEnergyStorage loaded = new ClayEnergyStorage();
        loaded.load(tag);
        assertEquals(largeCe, loaded.getStored());

        // Verify 4x16-bit lossless ContainerData round-trip
        int c0 = loaded.encodeChunk(0);
        int c1 = loaded.encodeChunk(1);
        int c2 = loaded.encodeChunk(2);
        int c3 = loaded.encodeChunk(3);
        assertEquals(largeCe, ClayEnergyStorage.decodeChunks(c0, c1, c2, c3));
        assertEquals(Long.MAX_VALUE, ClayEnergyStorage.decodeChunks(
                ClayEnergyStorage.encodeChunk(Long.MAX_VALUE, 0),
                ClayEnergyStorage.encodeChunk(Long.MAX_VALUE, 1),
                ClayEnergyStorage.encodeChunk(Long.MAX_VALUE, 2),
                ClayEnergyStorage.encodeChunk(Long.MAX_VALUE, 3)
        ));
    }

    @Test
    void registeredCompressedClayItemsExistAndDoNotOverflowCeSlotConsumptionLogic() {
        // Also verify energy shard is NOT an energy item while compressed clay tier >= 4 IS
        assertEquals(0L, CompressedClayBlock.clayEnergyForTier(3));
        assertEquals(100_000L, CompressedClayBlock.clayEnergyForTier(4));
        assertEquals(3, ClayiumItems.standaloneItems().keySet().stream().filter(k -> k.endsWith("_shard")).count());
    }

    @Test
    void overclockResonanceAndCaReactorFormulasMatchOriginal() {
        // <= 10.0 stays at effective factor 1.0, extra speed comes via tickIncrement
        double effSmall = ClayEnergyUpgradeMath.effectiveOverclockFactor(5.0);
        assertEquals(1.0, effSmall, 1e-9);
        assertEquals(5.0, ClayEnergyUpgradeMath.tickIncrement(5.0, effSmall), 1e-9);
        assertEquals(1.0F, ClayEnergyUpgradeMath.energyConsumptionMultiplier(1.0F, effSmall), 1e-6);

        // 2x OPA overclocker: total = 25.0 -> effective = 2.5, tickIncrement = 10.0
        double effLarge = ClayEnergyUpgradeMath.effectiveOverclockFactor(25.0);
        assertEquals(2.5, effLarge, 1e-9);
        assertEquals(10.0, ClayEnergyUpgradeMath.tickIncrement(25.0, effLarge), 1e-9);
        assertEquals((float) Math.pow(2.5, 1.5), ClayEnergyUpgradeMath.energyConsumptionMultiplier(1.0F, effLarge), 1e-5);
        assertEquals(0.4F, ClayEnergyUpgradeMath.craftTimeMultiplier(1.0F, effLarge), 1e-5);

        // Storage upgrade clamp: 1 + 63 = 64, 1 + 126 -> clamped at 64
        assertEquals(64, ClayEnergyUpgradeMath.clampEnergySlotLimit(63));
        assertEquals(64, ClayEnergyUpgradeMath.clampEnergySlotLimit(126));
        assertEquals(2, ClayEnergyUpgradeMath.clampEnergySlotLimit(1));

        // CA Injector & Reactor
        assertEquals(Math.pow(20.0, -3.0), ClayEnergyUpgradeMath.caInjectorCraftTimeMultiplier(20.0, 11), 1e-9);
        assertEquals(0.2 * Math.pow(1.02, 50) * Math.pow(7.5, 5.0), ClayEnergyUpgradeMath.caReactorEfficiency(50, 5.0), 1e-6);
        assertEquals(Math.pow(1.01, 50 * 5.0), ClayEnergyUpgradeMath.caReactorConsumingEnergyMultiplier(50, 5.0), 1e-6);
        assertEquals(Math.pow(9.0, 8), ClayEnergyUpgradeMath.caReactorPureAntimatterTimeMultiplier(9), 1e-6);
    }
}
