package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vacuum Container (Tier 6): pulls or accepts matching items (including ItemFilter matching on slot 1)
 * and immediately voids them instead of retaining inventory.
 */
public class VacuumContainerBlockEntity extends StorageContainerBlockEntity {
    public VacuumContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.VACUUM_CONTAINER.get(), pos, state);
    }

    @Override
    public boolean isVacuumContainer() {
        return true;
    }

    @Override
    protected boolean checkFilterSlot(ItemStack candidate, ItemStack filter) {
        return super.checkFilterSlot(candidate, filter) || ClayiumFilterMatcher.match(filter, candidate);
    }

    @Override
    public int insertIntoStorage(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !acceptsItem(stack)) {
            return 0;
        }
        return stack.getCount();
    }

    @Override
    public ItemStack extractFromStorage(int maxAmount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getItemUseMode(ItemStack stack, Player player) {
        int m = super.getItemUseMode(stack, player);
        return m == 99 ? -1 : m;
    }
}
