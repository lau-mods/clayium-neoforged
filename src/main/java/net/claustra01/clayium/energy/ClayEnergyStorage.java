package net.claustra01.clayium.energy;

import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class ClayEnergyStorage implements INBTSerializable<CompoundTag> {
    public static final String ENERGY_NBT_KEY = "clay_energy";
    public static final boolean EXPOSES_FE_CAPABILITY = false;

    private ClayEnergy stored;

    public ClayEnergyStorage() {
        this(ClayEnergy.ZERO);
    }

    public ClayEnergyStorage(ClayEnergy initialEnergy) {
        stored = initialEnergy;
    }

    public ClayEnergy stored() {
        return stored;
    }

    public ClayEnergy add(ClayEnergy energy) {
        ClayEnergy before = stored;
        stored = stored.plus(energy);
        return ClayEnergy.ofInternal(stored.internalValue() - before.internalValue());
    }

    public boolean consume(ClayEnergy required) {
        if (!stored.canAfford(required)) {
            return false;
        }
        stored = stored.minus(required);
        return true;
    }

    public boolean consume(ClayEnergy required, ItemStack fuelSlot) {
        if (!stored.canAfford(required) && !produceClayEnergy(fuelSlot)) {
            return false;
        }
        return consume(required);
    }

    public boolean produceClayEnergy(ItemStack fuelSlot) {
        ClayEnergy fuelValue = fuelValue(fuelSlot);
        if (fuelValue.internalValue() == 0L) {
            return false;
        }
        ClayEnergy accepted = add(fuelValue);
        if (accepted.internalValue() == 0L) {
            return false;
        }
        fuelSlot.shrink(1);
        return true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(ENERGY_NBT_KEY, stored.internalValue());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        stored = ClayEnergy.ofInternal(tag.getLong(ENERGY_NBT_KEY));
    }

    public static ClayEnergy fuelValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return ClayEnergy.ZERO;
        }
        for (CompressionTier compressionTier : CompressionTier.values()) {
            if (compressionTier.clayEnergy() > 0L
                    && stack.is(MaterialRegistration.COMPRESSION_BLOCKS.get(compressionTier.id()).get().asItem())) {
                return ClayEnergy.ofCe(compressionTier.clayEnergy());
            }
        }
        return ClayEnergy.ZERO;
    }
}
