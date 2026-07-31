/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.energy;

import java.util.List;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Original compressed energetic-clay CE values (10^(tier + 1)). */
public final class EnergeticClayFuel {
    private static final long BASE_CE = 100_000L;
    private static final List<String> IDS = List.of(
            "energetic_clay",
            "compressed_energetic_clay",
            "double_compressed_energetic_clay",
            "triple_compressed_energetic_clay",
            "quadruple_compressed_energetic_clay",
            "quintuple_compressed_energetic_clay",
            "sextuple_compressed_energetic_clay",
            "septuple_compressed_energetic_clay",
            "octuple_compressed_energetic_clay");

    private EnergeticClayFuel() {
    }

    public static long value(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        long value = BASE_CE;
        for (String id : IDS) {
            if (item == ClayiumRegistries.COMPRESSED_CLAY_BLOCK_ITEMS.get(id).get()) {
                return value;
            }
            value = Math.multiplyExact(value, 10L);
        }
        return 0;
    }

    public static boolean isFuel(ItemStack stack) {
        return value(stack) > 0;
    }
}
