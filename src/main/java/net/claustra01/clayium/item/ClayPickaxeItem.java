package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.util.ClayiumTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Pickaxe (Stone tier, 500 durability, 32.0F speed on Clay Ore).
 */
public class ClayPickaxeItem extends PickaxeItem implements ITieredItem {
    public static final float EFFICIENCY_ON_CLAY_ORE = 32.0F;
    private static final String ICON_NAME = "claypickaxe";

    public ClayPickaxeItem() {
        super(ClayToolItem.tierWithDurability(Tiers.STONE, 500), new Properties()
                .attributes(PickaxeItem.createAttributes(Tiers.STONE, 1.0F, -2.8F)));
    }

    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public int getTier(ItemStack stack) {
        return -1;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.getBlock() instanceof ClayOreBlock) {
            return EFFICIENCY_ON_CLAY_ORE;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ClayiumTooltips.appendLocalizedTooltip(tooltip, stack.getDescriptionId() + ".tooltip", flag);
    }
}
