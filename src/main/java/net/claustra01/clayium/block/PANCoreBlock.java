package net.claustra01.clayium.block;

import net.claustra01.clayium.blockentity.PANCoreBlockEntity;
import net.claustra01.clayium.pan.IPANConductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * PAN Core block: conducts PAN cables and triggers an immediate network/cost refresh before opening its GUI.
 */
public class PANCoreBlock extends HorizontalClayContainerBlock implements IPANConductor {
    public PANCoreBlock(Properties properties) {
        super(properties, 11, "machinehull-10", "pancore", "pan", null);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PANCoreBlockEntity core) {
            core.refreshNetwork();
            core.refreshItemSet();
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
