package net.claustra01.clayium.tool.advanced;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ClaySteelPickaxeItem extends PickaxeItem {
    public ClaySteelPickaxeItem(Properties properties) {
        super(ClayToolTier.CLAY_STEEL, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return super.getDestroySpeed(stack, state) * 6.0F;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        boolean result = super.mineBlock(stack, level, state, pos, entity);
        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player && !stack.isEmpty()) {
            AdvancedToolMining.breakAdditionalBlocks(
                    serverLevel, player, stack, pos, AdvancedToolMining.hitFace(player, pos));
        }
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return AdvancedToolBehavior.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        AdvancedToolBehavior.addTooltip(stack, tooltip);
    }
}
