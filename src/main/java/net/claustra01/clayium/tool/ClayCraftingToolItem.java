package net.claustra01.clayium.tool;

import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.item.FoundationItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClayCraftingToolItem extends FoundationItem {
    private final int baseDurability;
    private final int brokenClayBalls;

    public ClayCraftingToolItem(Properties properties, int baseDurability, int brokenClayBalls) {
        super(properties.stacksTo(1));
        this.baseDurability = baseDurability;
        this.brokenClayBalls = brokenClayBalls;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copyWithCount(1);
        int nextDamage = remainder.getDamageValue() + 1;
        if (nextDamage >= remainder.getMaxDamage()) {
            return new ItemStack(Items.CLAY_BALL, brokenClayBalls);
        }
        remainder.setDamageValue(nextDamage);
        return remainder;
    }

    public int effectiveMaxDamage() {
        return ClayiumConfig.multiplyProgressionCount(baseDurability);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return effectiveMaxDamage();
    }

    public boolean willBreak(ItemStack stack) {
        return stack.getDamageValue() + 1 >= stack.getMaxDamage();
    }
}
