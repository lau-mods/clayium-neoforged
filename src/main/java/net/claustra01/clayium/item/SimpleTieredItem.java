package net.claustra01.clayium.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * General Clayium item with optional tier tooltip and texture identifier for DataGen.
 */
public class SimpleTieredItem extends Item implements ITieredItem {
    private final int tier;
    private final String iconName;

    public SimpleTieredItem(Properties properties, @Nullable Integer tier, String iconName) {
        super(properties);
        this.tier = tier != null ? tier : -1;
        this.iconName = iconName;
    }

    public String getIconName() {
        return this.iconName;
    }

    @Override
    public int getTier(ItemStack stack) {
        return this.tier;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (this.tier >= 0) {
            tooltip.add(Component.translatable("gui.Common.tier", this.tier).withStyle(ChatFormatting.GRAY));
        }
    }
}
