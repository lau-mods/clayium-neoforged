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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.claustra01.clayium.logistics.ConfigurableIoDevice;

public final class UnifiedItemFilterItem extends Item {
    public UnifiedItemFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ConfigurableIoDevice device)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            device.setFilter(context.getClickedFace(),
                    context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                            ? ItemStack.EMPTY : context.getItemInHand());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Original ContainerTemp title: the held filter's display name.
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new FilterMenu(id, inventory),
                    player.getItemInHand(hand).getHoverName()));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemFilterSettings settings = stack.getOrDefault(
                LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.EMPTY);
        tooltip.add(Component.translatable(
                "tooltip.clayium.item_filter.mode." + settings.mode().name().toLowerCase(java.util.Locale.ROOT)));
        addEntries(settings, tooltip, 0);
    }

    /** Original ItemFilterWhitelist.addTooltip: one line per remembered stack, nested filters indented. */
    public static void addEntries(ItemFilterSettings settings, List<Component> tooltip, int indent) {
        for (ItemStack remembered : settings.items()) {
            if (remembered.isEmpty() || tooltip.size() >= 100) {
                continue;
            }
            tooltip.add(Component.literal(" ".repeat(indent)).append(remembered.getHoverName()));
            ItemFilterSettings nested = remembered.is(LogisticsRegistration.ITEM_FILTER.get())
                    ? remembered.get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get()) : null;
            if (nested != null && indent < ItemFilter.MAX_NESTING) {
                addEntries(nested, tooltip, indent + 1);
            }
        }
    }
}
