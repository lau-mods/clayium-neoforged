/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ClaySteelPickaxeItem extends PickaxeItem {
    public ClaySteelPickaxeItem(Item.Properties properties) { super(ClaySteelToolTier.INSTANCE, properties); }
    @Override public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        boolean result = super.mineBlock(stack, level, state, pos, entity);
        ClaySteelToolHelper.mineArea(stack, level, state, pos, entity);
        return result;
    }
    @Override public InteractionResult useOn(UseOnContext context) { return ClaySteelToolHelper.cycleMode(context); }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ClaySteelToolHelper.addModeTooltip(stack, tooltip);
    }
}
