/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

/** Machines whose original behavior cannot be represented by the common machine recipe runtime. */
public enum SpecialMachineKind {
    AUTO_CLAY_CONDENSER(5, 22),
    AUTO_CRAFTER(5, 43),
    CHEMICAL_METAL_SEPARATOR(6, 19);

    private final int minimumTier;
    private final int slots;

    SpecialMachineKind(int minimumTier, int slots) {
        this.minimumTier = minimumTier;
        this.slots = slots;
    }

    public int minimumTier() { return minimumTier; }
    public int slots() { return slots; }
}
