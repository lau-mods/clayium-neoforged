/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Server-owned Clay Energy storage. CE is deliberately independent from FE;
 * exposing a NeoForge FE capability belongs only to the dedicated converter.
 */
public final class ClayEnergyStorage {
    private static final String ENERGY_KEY = "Energy";

    private final long capacity;
    private final long maxReceive;
    private final long maxExtract;
    private long energy;

    public ClayEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        if (capacity < 0 || maxReceive < 0 || maxExtract < 0) {
            throw new IllegalArgumentException("Clay Energy limits must not be negative");
        }
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    public long capacity() {
        return capacity;
    }

    public long energyStored() {
        return energy;
    }

    public long maxReceive() {
        return maxReceive;
    }

    public long maxExtract() {
        return maxExtract;
    }

    public long receive(long amount, boolean simulate) {
        if (amount <= 0 || maxReceive == 0 || energy >= capacity) {
            return 0;
        }
        long accepted = Math.min(Math.min(amount, maxReceive), capacity - energy);
        if (!simulate) {
            energy += accepted;
        }
        return accepted;
    }

    public long extract(long amount, boolean simulate) {
        if (amount <= 0 || maxExtract == 0 || energy <= 0) {
            return 0;
        }
        long extracted = Math.min(Math.min(amount, maxExtract), energy);
        if (!simulate) {
            energy -= extracted;
        }
        return extracted;
    }

    public void setEnergy(long energy) {
        if (energy < 0) {
            this.energy = 0;
        } else if (energy > capacity) {
            this.energy = capacity;
        } else {
            this.energy = energy;
        }
    }

    public ClayEnergySnapshot snapshot() {
        return new ClayEnergySnapshot(energy, capacity);
    }

    public void applySnapshot(ClayEnergySnapshot snapshot) {
        if (snapshot.capacity() != capacity) {
            throw new IllegalArgumentException("Clay Energy capacity changed during synchronization");
        }
        setEnergy(snapshot.energy());
    }

    public void save(CompoundTag tag) {
        tag.putLong(ENERGY_KEY, energy);
    }

    public void load(CompoundTag tag) {
        if (tag.contains(ENERGY_KEY, Tag.TAG_LONG)) {
            setEnergy(tag.getLong(ENERGY_KEY));
        } else {
            energy = 0;
        }
    }
}
