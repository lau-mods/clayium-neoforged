package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.util.ClayiumTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

/**
 * Baked clay crafting tools (Rolling Pin, Slicer, Spatula) that take damage in crafting grids,
 * disallow anvil repair, and turn into clay balls once exhausted.
 */
public class CraftingToolItem extends Item {
    private final int clayBallsOnBreak;
    private final String iconName;

    public CraftingToolItem(int durability, int clayBallsOnBreak, String iconName) {
        super(new Properties().durability(durability));
        this.clayBallsOnBreak = clayBallsOnBreak;
        this.iconName = iconName;
    }

    public int getClayBallsOnBreak() {
        return this.clayBallsOnBreak;
    }

    public String getIconName() {
        return this.iconName;
    }

    public static boolean isExhausted(int damage, int maxDamage) {
        return damage >= maxDamage;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (isExhausted(stack.getDamageValue(), stack.getMaxDamage())) {
            return new ItemStack(Items.CLAY_BALL, this.clayBallsOnBreak);
        }
        ItemStack copy = stack.copy();
        copy.setDamageValue(stack.getDamageValue() + 1);
        return copy;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ClayiumTooltips.appendLocalizedTooltip(tooltip, stack.getDescriptionId() + ".tooltip", flag);
    }
}
