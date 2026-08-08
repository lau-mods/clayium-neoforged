/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.world.item.ItemStack;

public final class LateGameMaterials {
    private static final String[] PURE_ANTIMATTER = {
        "pure_antimatter", "compressed_pure_antimatter", "double_compressed_pure_antimatter",
        "triple_compressed_pure_antimatter", "quadruple_compressed_pure_antimatter",
        "quintuple_compressed_pure_antimatter", "sextuple_compressed_pure_antimatter",
        "septuple_compressed_pure_antimatter", "opa"
    };

    private LateGameMaterials() {}

    public static ItemStack pureAntimatter(int rank) {
        return ClayiumRegistries.MATERIAL_ITEMS.get(PURE_ANTIMATTER[Math.max(0, Math.min(8, rank))])
                .get().getDefaultInstance();
    }
}
