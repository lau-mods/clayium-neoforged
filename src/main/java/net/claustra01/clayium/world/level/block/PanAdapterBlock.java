/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.pan.PanConductor;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.PanAdapterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class PanAdapterBlock extends BaseEntityBlock implements PanConductor {
    public static final MapCodec<PanAdapterBlock> CODEC=simpleCodec(p->new PanAdapterBlock(p,ClayTier.ANTIMATTER));
    private final ClayTier tier;
    public PanAdapterBlock(Properties properties,ClayTier tier){super(properties);this.tier=tier;}
    public ClayTier tier(){return tier;}
    public int pages(){return switch(tier.progressionIndex()){case 11->2;case 12->4;case 13->8;default->1;};}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new PanAdapterBlockEntity(pos,state);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState newState,boolean movedByPiston){
        if(!state.is(newState.getBlock())&&level.getBlockEntity(pos) instanceof PanAdapterBlockEntity adapter){
            for(int slot=0;slot<PanAdapterBlockEntity.PAGE_SIZE;slot++){
                var stack=adapter.removeItemNoUpdate(PanAdapterBlockEntity.AUXILIARY_START+slot);
                if(!stack.isEmpty())Containers.dropItemStack(level,pos.getX(),pos.getY(),pos.getZ(),stack);
            }
        }
        super.onRemove(state,level,pos,newState,movedByPiston);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!level.isClientSide&&player instanceof ServerPlayer server&&level.getBlockEntity(pos) instanceof PanAdapterBlockEntity adapter)
            server.openMenu(adapter,data->data.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
