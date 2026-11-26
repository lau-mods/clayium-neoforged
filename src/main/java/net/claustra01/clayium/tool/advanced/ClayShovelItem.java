package net.claustra01.clayium.tool.advanced;

import java.util.List;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayShovelItem extends ShovelItem {
    public ClayShovelItem(Properties properties) {
        super(ClayToolTier.CLAY_SHOVEL, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (ClayToolProperties.isClayBlock(state)) {
            return 32.0F;
        }
        return ClayToolProperties.isClayOre(state) ? 12.0F : super.getDestroySpeed(stack, state);
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
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.clay_shovel"));
        AdvancedToolBehavior.addDurability(stack, tooltip);
    }
}
