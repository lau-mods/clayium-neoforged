/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.item;

import net.claustra01.clayium.data.FilterSettings;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Data-component Smart Filter replacing the legacy family of metadata filters. */
public final class SmartFilterItem extends Item {
    public SmartFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            FilterSettings settings = stack.getOrDefault(
                    ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
            FilterSettings toggled = settings.toggleListType();
            stack.set(ClayiumDataComponents.FILTER_SETTINGS.get(), toggled);
            player.displayClientMessage(
                    Component.translatable(
                            "message.clayium_neoforged.filter_mode",
                            toggled.blacklist() ? "BLACKLIST" : "WHITELIST"),
                    true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        FilterSettings settings = context.getItemInHand().getOrDefault(
                ClayiumDataComponents.FILTER_SETTINGS.get(), FilterSettings.DEFAULT);
        var blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof ConfigurableItemDevice device && !context.isSecondaryUseActive()) {
            if (!context.getLevel().isClientSide) {
                device.setFilter(context.getClickedFace(), settings);
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.clayium_neoforged.filter_applied"), true);
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        if (context.isSecondaryUseActive()) {
            var item = context.getLevel().getBlockState(context.getClickedPos()).getBlock().asItem();
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (!context.getLevel().isClientSide && item != net.minecraft.world.item.Items.AIR) {
                context.getItemInHand().set(ClayiumDataComponents.FILTER_SETTINGS.get(), settings.add(id));
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.clayium_neoforged.filter_added", id), true);
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
