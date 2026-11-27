package mods.clayium.block.tile;

import java.util.List;
import mods.clayium.item.filter.ItemFilterTemp;
import mods.clayium.util.UtilTransfer;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

public class TileAreaCollector extends TileAreaMiner {
    public TileAreaCollector() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.energyPerTick = 100L;
        this.progressPerTick = 100;
        this.progressPerJob = 10;
    }

    public void initState() {
        this.setState(3);
    }

    public void doWork() {
        AxisAlignedBB aabb = this.getAxisAlignedBB();
        switch (this.getState()) {
            case 0:
            case 3:
                this.setState(this.getState() + 1);
                break;
            case 1:
            case 4:
                long c = (long)((double)this.energyPerTick * (1.0 + 4.0 * Math.log10((double)(this.laserEnergy / 1000L + 1L))));
                if (this.consumeClayEnergy(c)) {
                    this.setSyncFlag();
                    this.progress = this.progress + (long)((double)this.progressPerTick * (1.0 + 4.0 * Math.log10((double)(this.laserEnergy / 1000L + 1L))));
                    this.laserEnergy = 0L;

                    for (int count = 0; count < this.maxJobCount; count++) {
                        List list = this.field_145850_b.func_82733_a(EntityItem.class, this.aabb, IEntitySelector.field_94557_a);
                        long i = (long)((double)this.progressPerJob * 1.0);
                        ItemStack filter = this.containerItemStacks[this.filterHarvestSlot];

                        for (Object a : list) {
                            if (a instanceof EntityItem) {
                                EntityItem eitem = (EntityItem)a;
                                ItemStack item = eitem.func_92059_d().func_77946_l();
                                ItemStack item1 = item.func_77946_l();
                                item1.field_77994_a = 1;
                                boolean flag = true;
                                if (ItemFilterTemp.isFilter(filter)) {
                                    flag = ItemFilterTemp.match(filter, item);
                                }

                                if (flag) {
                                    int slotNum = this.inventoryX * this.inventoryY;
                                    boolean flag1 = true;

                                    while (flag1) {
                                        if (UtilTransfer.canProduceItemStack(item1, this.containerItemStacks, 0, slotNum, this.func_70297_j_()) >= 1
                                            && i <= this.progress) {
                                            this.progress -= i;
                                            UtilTransfer.produceItemStack(item1, this.containerItemStacks, 0, slotNum, this.func_70297_j_());
                                            if (--item.field_77994_a <= 0) {
                                                eitem.func_70106_y();
                                                flag1 = false;
                                            } else {
                                                eitem.func_92058_a(item);
                                            }

                                            if (this.getState() == 1) {
                                                flag1 = false;
                                                this.progress = 0L;
                                                this.setState(2);
                                            }
                                        } else {
                                            flag1 = false;
                                        }
                                    }
                                }

                                if (i > this.progress) {
                                    break;
                                }
                            }
                        }
                    }
                }
            case 2:
        }
    }
}
