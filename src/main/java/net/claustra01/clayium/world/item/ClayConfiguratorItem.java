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

/** Original Clayium I/O tools and their combined variants. */
public final class ClayConfiguratorItem extends Item {
    public enum Mode {
        INSERT,
        EXTRACT,
        PIPE,
        ROTATE,
        IO_COMBINED,
        PIPE_COMBINED,
        MEMORY
    }

    private final Mode mode;

    public ClayConfiguratorItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    public boolean showsPipingOverlay() {
        return mode == Mode.IO_COMBINED || mode == Mode.PIPE_COMBINED || mode == Mode.MEMORY;
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
        if (mode == Mode.MEMORY) {
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
        } else if (mode == Mode.INSERT
                || mode == Mode.IO_COMBINED && (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown())) {
            int route = device.cycleInsertRoute(context.getClickedFace());
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable(
                                "message.clayium_neoforged.insert_route",
                                context.getClickedFace().getName(),
                                route),
                        true);
            }
        } else if (mode == Mode.EXTRACT || mode == Mode.IO_COMBINED) {
            int route = device.cycleExtractRoute(context.getClickedFace());
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable(
                                "message.clayium_neoforged.extract_route",
                                context.getClickedFace().getName(),
                                route),
                        true);
            }
        } else if (mode == Mode.PIPE
                || mode == Mode.PIPE_COMBINED && (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown())) {
            boolean pipe = device.togglePipe();
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.clayium_neoforged.pipe_mode", pipe),
                        true);
            }
        } else if (mode == Mode.ROTATE || mode == Mode.PIPE_COMBINED) {
            device.rotate(context.getClickedFace());
        }
        return InteractionResult.CONSUME;
    }
}
