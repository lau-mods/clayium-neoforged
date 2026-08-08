/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.pan;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** One item known to a PAN Core, with original material cost and total CE consumption. */
public record PanCoreEntry(ItemStack stack, double cost, double consumption, boolean prohibited) {
    public PanCoreEntry {
        stack = stack.copyWithCount(1);
        cost = finiteNonNegative(cost);
        consumption = finiteNonNegative(consumption);
    }

    public static void encode(RegistryFriendlyByteBuf buffer, PanCoreEntry entry) {
        ItemStack.STREAM_CODEC.encode(buffer, entry.stack);
        buffer.writeDouble(entry.cost);
        buffer.writeDouble(entry.consumption);
        buffer.writeBoolean(entry.prohibited);
    }

    public static PanCoreEntry decode(RegistryFriendlyByteBuf buffer) {
        return new PanCoreEntry(ItemStack.STREAM_CODEC.decode(buffer), buffer.readDouble(),
                buffer.readDouble(), buffer.readBoolean());
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : Double.MAX_VALUE;
    }
}
