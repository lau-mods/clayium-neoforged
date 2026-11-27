package net.claustra01.clayium.energy;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Per-machine long Clay Energy buffer, compressed clay consumption, NBT persistence,
 * and lossless 4x16-bit splitting for Menu ContainerData synchronization.
 */
public final class ClayEnergyStorage {
    public static final String NBT_KEY = "ClayEnergy";
    public static final int DEFAULT_ENERGY_SLOT_LIMIT = 1;
    public static final int MAX_ENERGY_SLOT_LIMIT = 64;

    private long stored;
    private int energySlotLimit = DEFAULT_ENERGY_SLOT_LIMIT;

    public long getStored() {
        return this.stored;
    }

    public void setStored(long stored) {
        this.stored = Math.max(0L, stored);
    }

    public void addEnergy(long delta) {
        if (delta <= 0L) {
            return;
        }
        this.stored = ClayEnergy.safeAdd(this.stored, delta);
    }

    public int getEnergySlotLimit() {
        return this.energySlotLimit;
    }

    public void setEnergySlotLimit(int limit) {
        this.energySlotLimit = Math.max(1, Math.min(MAX_ENERGY_SLOT_LIMIT, limit));
    }

    /**
     * Attempts to consume {@code amount} internal CE units, drawing compressed clay items from the CE slot if needed.
     */
    public boolean consume(long amount, Supplier<ItemStack> slotGetter, Consumer<ItemStack> slotSetter) {
        if (amount <= 0L) {
            return true;
        }
        while (this.stored < amount) {
            if (!produceOneFromSlot(slotGetter, slotSetter)) {
                return false;
            }
        }
        this.stored -= amount;
        return true;
    }

    /**
     * Consumes a single item from the energy slot and adds its CE to {@link #stored}.
     */
    public boolean produceOneFromSlot(Supplier<ItemStack> slotGetter, Consumer<ItemStack> slotSetter) {
        ItemStack stack = slotGetter.get();
        long itemEnergy = ClayEnergy.getItemEnergy(stack);
        if (itemEnergy <= 0L) {
            return false;
        }
        this.stored = ClayEnergy.safeAdd(this.stored, itemEnergy);
        ItemStack remaining = stack.copy();
        remaining.shrink(1);
        slotSetter.accept(remaining);
        return true;
    }

    public void save(CompoundTag tag) {
        tag.putLong(NBT_KEY, this.stored);
    }

    public void load(CompoundTag tag) {
        if (tag.contains(NBT_KEY)) {
            setStored(tag.getLong(NBT_KEY));
        }
    }

    /**
     * Extracts one of four 16-bit chunks (index 0..3, little-endian) for vanilla ContainerData sync.
     */
    public int encodeChunk(int chunkIndex) {
        return encodeChunk(this.stored, chunkIndex);
    }

    public static int encodeChunk(long value, int chunkIndex) {
        if (chunkIndex < 0 || chunkIndex > 3) {
            throw new IllegalArgumentException("chunkIndex must be 0..3, got " + chunkIndex);
        }
        return (int) ((value >>> (chunkIndex * 16)) & 0xFFFFL);
    }

    public static long decodeChunks(int c0, int c1, int c2, int c3) {
        return ((long) c0 & 0xFFFFL)
                | (((long) c1 & 0xFFFFL) << 16)
                | (((long) c2 & 0xFFFFL) << 32)
                | (((long) c3 & 0xFFFFL) << 48);
    }
}
