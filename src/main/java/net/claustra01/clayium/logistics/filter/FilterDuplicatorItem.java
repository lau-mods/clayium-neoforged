package net.claustra01.clayium.logistics.filter;

import java.util.List;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.item.FoundationItem;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class FilterDuplicatorItem extends FoundationItem {
    public FilterDuplicatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrDefault(LogisticsRegistration.FILTER_COPY.get(), false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!isFoil(stack)) {
            return;
        }
        ItemFilterSettings settings = stack.getOrDefault(
                LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.ALLOW_ALL);
        tooltip.add(Component.translatable("tooltip.clayium.filter_copy.stored"));
        tooltip.add(Component.translatable("tooltip.clayium.item_filter.mode." + settings.mode().name().toLowerCase()));
        tooltip.add(Component.translatable("tooltip.clayium.item_filter.rules", settings.rules().size()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack duplicator = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(duplicator);
        }
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                duplicator.set(LogisticsRegistration.FILTER_COPY.get(), false);
                player.displayClientMessage(Component.translatable("message.clayium.filter_copy.cleared"), true);
                play(level, player);
            }
            return InteractionResultHolder.sidedSuccess(duplicator, level.isClientSide);
        }
        ItemStack other = player.getOffhandItem();
        if (!other.is(LogisticsRegistration.ITEM_FILTER.get())) {
            return InteractionResultHolder.fail(duplicator);
        }
        if (!level.isClientSide) {
            boolean hasCopy = duplicator.getOrDefault(LogisticsRegistration.FILTER_COPY.get(), false);
            boolean copied = hasCopy
                    ? FilterCopyValidator.copy(duplicator, other)
                    : FilterCopyValidator.copy(other, duplicator);
            if (!copied) {
                player.displayClientMessage(Component.translatable("message.clayium.filter_copy.rejected"), true);
                return InteractionResultHolder.fail(duplicator);
            }
            player.displayClientMessage(Component.translatable(
                    hasCopy ? "message.clayium.filter_copy.applied" : "message.clayium.filter_copy.stored"), true);
            play(level, player);
        }
        return InteractionResultHolder.sidedSuccess(duplicator, level.isClientSide);
    }

    private static void play(Level level, Player player) {
        level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS, 0.5F, 1.0F);
    }
}
