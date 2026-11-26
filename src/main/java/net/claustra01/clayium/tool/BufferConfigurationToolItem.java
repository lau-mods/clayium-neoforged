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

    /**
     * Original itemDirectionMemory.onItemUseFirst: apply a remembered ray trace to an activator,
     * otherwise remember the player's eye and look relative to the clicked block.
     */
    @Override
    public net.minecraft.world.InteractionResult onItemUseFirst(
            ItemStack stack, net.minecraft.world.item.context.UseOnContext context) {
        return useOn(context);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (level.getBlockEntity(context.getClickedPos()) instanceof BufferBlockEntity buffer) {
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    copyFrom(context.getItemInHand(), buffer);
                } else {
                    pasteTo(context.getItemInHand(), buffer);
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            ItemStack card = context.getItemInHand();
            var memory = card.get(LogisticsRegistration.RAY_TRACE_MEMORY.get());
            if (memory != null && level.getBlockEntity(context.getClickedPos())
                    instanceof net.claustra01.clayium.world.area.AreaMachineBlockEntity machine
                    && machine.isActivator()) {
                machine.setRayTraceMemory(memory);
            } else {
                card.set(LogisticsRegistration.RAY_TRACE_MEMORY.get(),
                        net.claustra01.clayium.world.area.RayTraceMemory.lookingFrom(
                                player, context.getClickedPos(), context.getClickedFace()));
            }
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.memory_card.copy").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.memory_card.paste").withStyle(ChatFormatting.GRAY));
        if (stack.has(LogisticsRegistration.BUFFER_CONFIGURATION.get())) {
            tooltip.add(Component.translatable("tooltip.clayium_neoforged.memory_card.stored").withStyle(ChatFormatting.GREEN));
        }
    }
}
