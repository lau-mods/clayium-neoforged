package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.util.ClayiumTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Steel Shovel (Diamond/Emerald tier, 10000 durability, 6x dig speed, AoE mode).
 */
public class ClaySteelShovelItem extends ShovelItem implements ITieredItem {
    private static final String ICON_NAME = "claysteelshovel";

    public ClaySteelShovelItem() {
        super(ClayToolItem.tierWithDurability(Tiers.DIAMOND, 10000), new Properties()
                .attributes(ShovelItem.createAttributes(Tiers.DIAMOND, 1.5F, -3.0F)));
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
        return super.getDestroySpeed(stack, state) * 6.0F;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult res = ClaySteelToolHelper.handleUseOn(context);
        return res != InteractionResult.PASS ? res : super.useOn(context);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        boolean result = super.mineBlock(stack, level, state, pos, entity);
        ClaySteelToolHelper.handleMineBlock(stack, level, pos, entity);
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ClaySteelToolHelper.appendModeTooltip(stack, tooltip);
        ClayiumTooltips.appendLocalizedTooltip(tooltip, stack.getDescriptionId() + ".tooltip", flag);
    }
}
