package net.claustra01.clayium.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Baked clay crafting tools (Rolling Pin, Slicer, Spatula) that take damage in crafting grids
 * and turn into clay balls once exhausted.
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
}
