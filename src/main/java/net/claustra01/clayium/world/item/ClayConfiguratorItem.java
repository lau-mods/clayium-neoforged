/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.item;

import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Unified replacement for the original input/output/piping tools and memory card. */
public final class ClayConfiguratorItem extends Item {
    private final boolean memory;

    public ClayConfiguratorItem(Properties properties, boolean memory) {
        super(properties);
        this.memory = memory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof ConfigurableItemDevice device)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (memory) {
            if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
                context.getItemInHand().set(ClayiumDataComponents.IO_MEMORY.get(), device.saveIoMemory());
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.clayium_neoforged.io_saved").withStyle(ChatFormatting.AQUA),
                        true);
            } else {
                IoMemory saved = context.getItemInHand().getOrDefault(
                        ClayiumDataComponents.IO_MEMORY.get(), IoMemory.DEFAULT);
                device.loadIoMemory(saved);
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.clayium_neoforged.io_loaded").withStyle(ChatFormatting.AQUA),
                            true);
                }
            }
        } else {
            var mode = device.cycleSide(context.getClickedFace());
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable(
                                "message.clayium_neoforged.side_mode",
                                context.getClickedFace().getName(),
                                mode.name()),
                        true);
            }
        }
        return InteractionResult.CONSUME;
    }
}
