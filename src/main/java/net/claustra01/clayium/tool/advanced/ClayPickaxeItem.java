package net.claustra01.clayium.tool.advanced;

import java.util.List;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayPickaxeItem extends PickaxeItem {
    public ClayPickaxeItem(Properties properties) {
        super(ClayToolTier.CLAY_PICKAXE, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return ClayToolProperties.isClayOre(state) ? 32.0F : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return ClayToolProperties.isClayOre(state) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.tier",
                stack.getOrDefault(FoundationDataComponentRegistration.TIER.get(), 1)));
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.clay_pickaxe.line1"));
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.clay_pickaxe.line2"));
        AdvancedToolBehavior.addDurability(stack, tooltip);
    }
}
