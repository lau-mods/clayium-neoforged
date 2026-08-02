/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.tier.ClayTier;
import net.claustra01.clayium.world.level.block.entity.ClayLaserInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ClayLaserInterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<ClayLaserInterfaceBlock> CODEC = simpleCodec(p -> new ClayLaserInterfaceBlock(p, ClayTier.CLAY_STEEL));
    private final ClayTier tier;
    public ClayLaserInterfaceBlock(Properties properties, ClayTier tier) { super(properties); this.tier = tier; }
    public ClayTier tier() { return tier; }
    @Override protected MapCodec<? extends ClayLaserInterfaceBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ClayLaserInterfaceBlockEntity(pos, state); }
}
