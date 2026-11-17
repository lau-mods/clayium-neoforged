package net.claustra01.clayium.logistics.filter;

import java.util.List;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class UnifiedItemFilterItem extends Item {
    public UnifiedItemFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new FilterMenu(id, inventory),
                    Component.translatable("item.clayium.white_list_item_filter")));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemFilterSettings settings = stack.getOrDefault(
                LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL);
        tooltip.add(Component.translatable(
                "tooltip.clayium.item_filter.mode." + settings.mode().name().toLowerCase()));
        tooltip.add(Component.translatable("tooltip.clayium.item_filter.rules", settings.rules().size()));
    }
}
