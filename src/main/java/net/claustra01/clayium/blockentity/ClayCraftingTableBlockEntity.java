package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Crafting Board (Tier 0): 9-slot persistent 3x3 crafting grid inventory matching 1.7.10 {@code TileClayCraftingTable}.
 */
public class ClayCraftingTableBlockEntity extends TieredClayContainerBlockEntity {
    public ClayCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CLAY_CRAFTING_TABLE.get(), pos, state, 9);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(9, ItemStack.EMPTY);
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }
}
