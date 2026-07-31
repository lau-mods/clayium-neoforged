/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import java.util.List;

/** Chemical materials introduced by the original progression through Precision tier. */
public final class Phase6ItemCatalog {
    public record Entry(String id, String displayName, String originalTexture) {}

    public static final List<Entry> ENTRIES = List.of(
            item("salt_dust", "Salt Dust", "saltdust"),
            item("calcium_chloride_dust", "Calcium Chloride Dust", "calciumchloridedust"),
            item("sodium_carbonate_dust", "Sodium Carbonate Dust", "sodiumcarbonatedust"),
            item("quartz_dust", "Quartz Dust", "quartzdust"),
            item("impure_silicon_ingot", "Impure Silicon Ingot", "impuresiliconingot"),
            item("impure_silicon_dust", "Impure Silicon Dust", "dust_base"),
            item("impure_aluminium_dust", "Impure Aluminium Dust", "dust_base"),
            item("aluminium_dust", "Aluminium Dust", "dust_base"),
            item("impure_magnesium_dust", "Impure Magnesium Dust", "dust_base"),
            item("magnesium_dust", "Magnesium Dust", "dust_base"),
            item("impure_sodium_dust", "Impure Sodium Dust", "dust_base"),
            item("sodium_dust", "Sodium Dust", "dust_base"),
            item("impure_lithium_dust", "Impure Lithium Dust", "dust_base"),
            item("lithium_dust", "Lithium Dust", "dust_base"),
            item("impure_zirconium_dust", "Impure Zirconium Dust", "dust_base"),
            item("zirconium_dust", "Zirconium Dust", "dust_base"),
            item("impure_zinc_dust", "Impure Zinc Dust", "dust_base"),
            item("zinc_dust", "Zinc Dust", "dust_base"));

    private Phase6ItemCatalog() {}

    private static Entry item(String id, String name, String texture) {
        return new Entry(id, name, texture);
    }
}
