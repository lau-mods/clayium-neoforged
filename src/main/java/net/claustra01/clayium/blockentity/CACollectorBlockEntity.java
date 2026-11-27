package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.claustra01.clayium.util.ProgressionRate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CA Collector (T10): collects antimatter passively from surrounding CA Resonators at {@code resonance - 1.0} per tick.
 */
public class CACollectorBlockEntity extends AbstractClayMachineBlockEntity {
    public int inventoryX = 3;
    public int inventoryY = 3;
    public long baseCraftTime = 10000L;

    public CACollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CA_COLLECTOR.get(), pos, state, 36);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(36, ItemStack.EMPTY);
        this.insertRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.extractRoutes = new int[]{-1, -1, 0, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.listSlotsInsert.clear();
        this.clayEnergySlot = -1;
        this.maxAutoInsertDefault = 64;
        this.maxAutoExtractDefault = 64;
        this.autoExtractInterval = 1;
        this.autoInsertInterval = 1;
        this.machineTimeToCraft = this.baseCraftTime;
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.inventoryX = 3;
        this.inventoryY = 3;
        int slotNum = this.inventoryX * this.inventoryY;
        int[] slots = new int[slotNum];
        int[] slotsRev = new int[slotNum];
        for (int i = 0; i < slotNum; i++) {
            slots[i] = i;
            slotsRev[i] = slotNum - i - 1;
        }
        this.listSlotsExtract.clear();
        this.listSlotsExtract.add(slotsRev);
        this.slotsDrop = slots;
    }

    @Override
    public int getInventorySize() {
        return this.inventoryX * this.inventoryY;
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    public double getResonance() {
        return this.level == null ? 1.0 : ClayEnergyUpgradeMath.scanResonance(this.level, this.worldPosition);
    }

    @Override
    public boolean canProceedCraft() {
        return true;
    }

    @Override
    public void proceedCraft() {
        this.machineTimeToCraft = Math.max(1L, (long) ((float) this.baseCraftTime * this.multCraftTime));
        this.machineCraftTime += (long) ProgressionRate.multiplyD(getResonance() - 1.0);
        this.doingWork = true;
        int slotNum = this.inventoryX * this.inventoryY;
        this.machineCraftTime = Math.min(this.machineCraftTime, this.machineTimeToCraft * (long) slotNum * 64L);
        boolean res = true;
        while (this.machineCraftTime >= this.machineTimeToCraft && res) {
            int n = Math.min((int) (this.machineCraftTime / this.machineTimeToCraft), 64);
            this.machineCraftTime -= (long) n * this.machineTimeToCraft;
            ItemStack produced = new ItemStack(ClayiumItems.byPath("antimatter").get(), n);
            res = ClayiumTransfer.produceItemStack(produced, this.items, 0, slotNum, getInventoryStackLimit(0)).isEmpty();
        }
        if (this.externalControlState > 0) {
            this.externalControlState--;
            if (this.externalControlState == 0) {
                this.externalControlState = -1;
            }
        }
        markForSync();
    }
}
