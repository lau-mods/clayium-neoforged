/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.item;

import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Raw clay crafting tools also remove an installed side filter in the original mod. */
public final class RawClayCraftingToolItem extends Item {
    public RawClayCraftingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ConfigurableItemDevice device)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            ItemStack removed = device.filter(context.getClickedFace());
            if (!removed.isEmpty()) {
                device.setFilter(context.getClickedFace(), ItemStack.EMPTY);
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.clayium_neoforged.filter_removed", removed.getHoverName()),
                            true);
                }
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
