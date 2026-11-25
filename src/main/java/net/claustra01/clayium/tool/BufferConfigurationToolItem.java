package net.claustra01.clayium.tool;

import java.util.List;
import net.claustra01.clayium.item.FoundationItem;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.BufferConfiguration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class BufferConfigurationToolItem extends FoundationItem {
    public BufferConfigurationToolItem(Properties properties) {
        super(properties);
    }

    public static void copyFrom(ItemStack tool, BufferBlockEntity buffer) {
        tool.set(LogisticsRegistration.BUFFER_CONFIGURATION.get(), buffer.copyConfiguration());
    }

    public static boolean pasteTo(ItemStack tool, BufferBlockEntity buffer) {
        BufferConfiguration configuration = tool.get(LogisticsRegistration.BUFFER_CONFIGURATION.get());
        if (configuration == null) {
            return false;
        }
        buffer.applyConfiguration(configuration);
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.clayium.memory_card.copy").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.clayium.memory_card.paste").withStyle(ChatFormatting.GRAY));
        if (stack.has(LogisticsRegistration.BUFFER_CONFIGURATION.get())) {
            tooltip.add(Component.translatable("tooltip.clayium.memory_card.stored").withStyle(ChatFormatting.GREEN));
        }
    }
}
