package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.util.CompressedClayHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Creative CE Source (Tier 13): infinitely provides 64 Octuple Compressed Energetic Clay blocks in slot 0.
 */
public class CreativeEnergySourceBlockEntity extends BufferBlockEntity {
    public CreativeEnergySourceBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CREATIVE_CE_SOURCE.get(), pos, state);
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.BUFFER);
        this.inventoryX = 1;
        this.inventoryY = 1;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsExtract.add(new int[]{0});
        this.slotsDrop = new int[0];
        this.items.set(0, CompressedClayHelper.getCompressedClay(13, 64));
    }

    public static ItemStack getEnergeticClay() {
        return CompressedClayHelper.getCompressedClay(13, 64);
    }

    @Override
    public void serverTick() {
        this.items.set(0, getEnergeticClay());
        super.serverTick();
        this.items.set(0, getEnergeticClay());
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return ItemStack.isSameItemSameComponents(getEnergeticClay(), stack);
    }

    @Override
    public boolean canExtractItemUnsafe(int slot, ItemStack stack, int route) {
        this.items.set(0, getEnergeticClay());
        return slot == 0;
    }

    @Override
    public boolean canInsertItemUnsafe(int slot, ItemStack stack, int route) {
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 ? getEnergeticClay() : ItemStack.EMPTY;
    }
}
