package net.claustra01.clayium.blockentity;

import java.util.Set;
import javax.annotation.Nullable;
import net.claustra01.clayium.block.PANDuplicatorBlock;
import net.claustra01.clayium.blockentity.IPANCoreAccess.PANConversionEntry;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * PAN Duplicator (Rank 4..13): duplicates registered PAN items alongside an Antimatter gem
 * using {@code 100000 * 10^(rank - 5)} CE/t up to the PAN item's energy cost.
 */
public class PANDuplicatorBlockEntity extends AbstractClayMachineBlockEntity {
    public static final float BASE_CONSUMING_ENERGY = 100000.0F;

    @Nullable
    private IPANCoreAccess core;
    private int remainingTime = 0;

    public PANDuplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.PAN_DUPLICATOR.get(), pos, state, 6);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(6, ItemStack.EMPTY);
        this.clayEnergySlot = 5;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{5});
        this.listSlotsExtract.add(new int[]{2});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 5};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int rank) {
        setDefaultTransportationParamsByTier(rank, ParamMode.MACHINE);
        if (getBlockState().getBlock() instanceof PANDuplicatorBlock dup) {
            this.initConsumingEnergy = (float) Math.pow(10.0, dup.getDuplicatorRank() - 5);
            this.multConsumingEnergy = this.initConsumingEnergy;
        }
    }

    @Override
    public boolean acceptEnergyClay() {
        return true;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level != null && !this.level.isClientSide) {
            if (this.remainingTime >= 0) {
                this.remainingTime--;
            } else {
                this.core = null;
            }
        }
    }

    public void setPANCore(@Nullable IPANCoreAccess core, int time) {
        this.core = core;
        this.remainingTime = time;
    }

    @Nullable
    public IPANCoreAccess getPANCore() {
        return this.core;
    }

    private static ItemStack antimatterTemplate() {
        return new ItemStack(ClayiumItems.byPath("antimatter").get());
    }

    @Nullable
    protected PANConversionEntry findEntry(ItemStack a, ItemStack b) {
        if (this.core == null) {
            return null;
        }
        ItemStack anti = antimatterTemplate();
        if (!ItemStack.isSameItemSameComponents(a, anti) && !ItemStack.isSameItemSameComponents(b, anti)) {
            return null;
        }
        Set<PANConversionEntry> entries = this.core.getConversionEntries();
        if (entries == null) {
            return null;
        }
        for (PANConversionEntry entry : entries) {
            if (ItemStack.isSameItemSameComponents(entry.stack(), a) || ItemStack.isSameItemSameComponents(entry.stack(), b)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public PANConversionEntry getResult() {
        boolean buffersEmpty = getStack(3).isEmpty() && getStack(4).isEmpty();
        return buffersEmpty ? findEntry(getStack(0), getStack(1)) : findEntry(getStack(3), getStack(4));
    }

    @Override
    public boolean canProceedCraft() {
        PANConversionEntry entry = getResult();
        return entry != null && ClayiumTransfer.canProduceItemStack(entry.stack(), this.items, 2, 3, getInventoryStackLimit(2)) >= 1;
    }

    @Override
    public void proceedCraft() {
        PANConversionEntry entry = getResult();
        if (entry == null) {
            return;
        }
        this.machineConsumingEnergy = (long) (BASE_CONSUMING_ENERGY * this.multConsumingEnergy);
        if (this.machineConsumingEnergy <= 0L) {
            return;
        }
        double rest = entry.ceCost() - (double) this.machineCraftTime * (double) this.machineConsumingEnergy;
        this.machineTimeToCraft = (long) (entry.ceCost() / (double) this.machineConsumingEnergy);
        long consumption = rest > (double) this.machineConsumingEnergy ? this.machineConsumingEnergy : Math.max(0L, (long) rest);

        if (consumeClayEnergy(consumption)) {
            if (getStack(3).isEmpty() && getStack(4).isEmpty()) {
                ItemStack anti = antimatterTemplate();
                for (int i = 0; i < 2; i++) {
                    ItemStack src = getStack(i);
                    ItemStack moved = src.split(1);
                    if (!ItemStack.isSameItemSameComponents(moved, anti)) {
                        // Target pattern item is duplicated without consuming the original
                        src.grow(1);
                    }
                    if (src.isEmpty()) {
                        setStack(i, ItemStack.EMPTY);
                    }
                    setStack(i + 3, moved);
                }
            }

            this.machineCraftTime++;
            this.doingWork = true;
            if (rest <= (double) this.machineConsumingEnergy) {
                setStack(3, ItemStack.EMPTY);
                setStack(4, ItemStack.EMPTY);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                ClayiumTransfer.produceItemStack(entry.stack().copy(), this.items, 2, 3, getInventoryStackLimit(2));
                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
                markForSync();
            }
        }
    }
}
