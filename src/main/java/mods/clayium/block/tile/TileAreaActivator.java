package mods.clayium.block.tile;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.entity.RayTraceMemory;
import mods.clayium.item.filter.ItemFilterTemp;
import mods.clayium.util.UtilPlayer;
import mods.clayium.util.UtilTransfer;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.common.util.ForgeDirection;

public class TileAreaActivator extends TileAreaMiner implements IRayTracer {
    public RayTraceMemory rayTraceMemory;
    public int target = 0;
    public boolean enableRayTrace = false;
    public boolean sneak = false;
    public boolean scanningBlock = true;
    public List<Entity> scannedEntityList = new ArrayList<>();

    public TileAreaActivator() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, 0, 0};
        this.clayEnergySlot = 49;
        this.containerItemStacks = new ItemStack[53];
    }

    public void initParamsByTier(int tier) {
        this.inventoryX = this.inventoryY = 3;
        switch (tier) {
            case 6:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 16;
                this.autoExtractInterval = this.autoInsertInterval = 2;
                break;
            case 7:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 64;
                this.autoExtractInterval = this.autoInsertInterval = 1;
                break;
            case 8:
            case 9:
            case 10:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 64;
                this.autoExtractInterval = this.autoInsertInterval = 1;
        }

        int slotNum = this.inventoryX * this.inventoryY;
        int[] slots = new int[slotNum];
        this.slotsDrop = new int[slotNum + 1];

        for (int i = 0; i < slots.length; this.slotsDrop[i] = i++) {
            slots[i] = i;
        }

        this.listSlotsInsert.add(slots);
        this.listSlotsInsert.add(new int[]{this.clayEnergySlot});
        this.listSlotsExtract.add(slots);
        this.slotsDrop[slotNum] = this.clayEnergySlot;
        if (tier >= 7) {
            this.areaMode = true;
        }

        if (!this.areaMode) {
            this.listSlotsInsert.remove(1);

            for (int i = 0; i < this.insertRoutes.length; i++) {
                if (this.insertRoutes[i] == 1) {
                    this.insertRoutes[i] = -1;
                }
            }
        }

        this.filterHarvestSlot = 50;
        this.filterFortuneSlot = 51;
        this.filterSilktouchSlot = 52;
    }

    public void initState() {
        this.setState(3);
    }

    public void doWork() {
        if (this.rayTraceMemory == null && this.field_145850_b != null) {
            if (!this.areaMode) {
                int m = this.getFrontDirection();
                if (m >= 0 && m < 6) {
                    this.rayTraceMemory = RayTraceMemory.getStandardMemory(ForgeDirection.getOrientation(m).getOpposite());
                }
            } else {
                this.rayTraceMemory = RayTraceMemory.getStandardMemory(ForgeDirection.DOWN);
            }
        }

        if (this.rayTraceMemory != null) {
            AxisAlignedBB aabb = this.getAxisAlignedBB();
            if (this.target == 1 && !this.enableRayTrace) {
                this.scanningBlock = false;
            }

            if (this.target == 0 || this.enableRayTrace) {
                this.scanningBlock = true;
            }

            switch (this.getState()) {
                case 0:
                case 3:
                    if (this.scanningBlock) {
                        this.miningX = (int)Math.floor(aabb.field_72340_a + 0.5);
                        this.miningY = (int)Math.floor(aabb.field_72338_b + 0.5);
                        this.miningZ = (int)Math.floor(aabb.field_72339_c + 0.5);
                    }

                    this.scannedEntityList.clear();
                    this.setState(this.getState() + 1);
                    break;
                case 1:
                case 4:
                    if (this.scanningBlock) {
                        long c = this.areaMode ? (long)((double)this.energyPerTick * (1.0 + 4.0 * Math.log10((double)(this.laserEnergy / 1000L + 1L)))) : 0L;
                        if (this.miningX > (int)Math.floor(aabb.field_72336_d - 0.5)) {
                            this.miningX = (int)Math.floor(aabb.field_72340_a + 0.5);
                            this.miningZ++;
                        }

                        if (this.miningZ > (int)Math.floor(aabb.field_72334_f - 0.5)) {
                            this.miningZ = (int)Math.floor(aabb.field_72339_c + 0.5);
                            this.miningY++;
                        }

                        if (this.miningY > (int)Math.floor(aabb.field_72337_e - 0.5)) {
                            if (this.getState() == 1) {
                                this.setState(2);
                                return;
                            }

                            this.miningX = (int)Math.floor(aabb.field_72340_a + 0.5);
                            this.miningY = (int)Math.floor(aabb.field_72338_b + 0.5);
                            this.miningZ = (int)Math.floor(aabb.field_72339_c + 0.5);
                        }

                        if (this.consumeClayEnergy(c)) {
                            this.setSyncFlag();
                            this.progress = this.progress
                                + (long)((double)this.progressPerTick * (1.0 + 4.0 * Math.log10((double)(this.laserEnergy / 1000L + 1L))));
                            this.laserEnergy = 0L;
                            ItemStack filter = this.containerItemStacks[this.filterHarvestSlot];
                            boolean filterFlag = ItemFilterTemp.isFilter(filter);
                            int slotNum = this.inventoryX * this.inventoryY;
                            int max = this.getState() == 1 ? this.maxJobCount : this.maxJobCountInLoop;
                            long i = (long)((double)this.progressPerJob * 1.0);
                            boolean flag = true;

                            for (int count = 0; count < max; count++) {
                                if (this.isCrowded()) {
                                    this.progress = 0L;
                                }

                                if (this.progress < i) {
                                    flag = false;
                                    break;
                                }

                                if (this.enableRayTrace || this.field_145850_b.func_147439_a(this.miningX, this.miningY, this.miningZ) == Blocks.field_150350_a
                                    )
                                 {
                                    ItemStack itemstack = null;

                                    int j;
                                    for (j = 0; j < 9; j++) {
                                        if (this.containerItemStacks[j] != null) {
                                            itemstack = this.containerItemStacks[j];
                                            break;
                                        }
                                    }

                                    InventoryPlayer inv = null;
                                    if (!this.enableRayTrace) {
                                        inv = this.rayTraceMemory
                                            .interactWithBlockFrom(itemstack, this.field_145850_b, this.miningX, this.miningY, this.miningZ, this.sneak);
                                    } else {
                                        MovingObjectPosition mopBlock = null;
                                        MovingObjectPosition mopEntity = null;
                                        if (this.target == 0 || this.target == 2) {
                                            mopBlock = this.rayTraceMemory
                                                .rayTraceBlockFrom(
                                                    this.field_145850_b,
                                                    this.miningX,
                                                    this.miningY,
                                                    this.miningZ,
                                                    Math.max(this.rayTraceMemory.getLook().func_72433_c(), 3.0),
                                                    false,
                                                    false,
                                                    false
                                                );
                                        }

                                        if (this.target == 1 || this.target == 2) {
                                            mopEntity = this.rayTraceMemory
                                                .rayTraceEntityFrom(
                                                    this.field_145850_b,
                                                    this.miningX,
                                                    this.miningY,
                                                    this.miningZ,
                                                    Math.max(this.rayTraceMemory.getLook().func_72433_c(), 3.0),
                                                    Entity.class,
                                                    IEntitySelector.field_94557_a
                                                );
                                        }

                                        Vec3 pos = this.rayTraceMemory.getPos().func_72441_c((double)this.miningX, (double)this.miningY, (double)this.miningZ);
                                        MovingObjectPosition mop = mopBlock == null
                                            ? mopEntity
                                            : (
                                                mopEntity == null
                                                    ? mopBlock
                                                    : (
                                                        pos.func_72438_d(mopBlock.field_72307_f) < pos.func_72438_d(mopEntity.field_72307_f)
                                                            ? mopBlock
                                                            : mopEntity
                                                    )
                                            );
                                        if (!filterFlag
                                            || mop == null
                                            || mop.field_72313_a != MovingObjectType.BLOCK
                                            || ItemFilterTemp.match(filter, this.field_145850_b, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d)) {
                                            inv = this.rayTraceMemory
                                                .useItemFrom(itemstack, this.field_145850_b, this.miningX, this.miningY, this.miningZ, mop, this.sneak);
                                        }
                                    }

                                    if (inv != null) {
                                        this.containerItemStacks[j] = inv.func_70448_g();
                                    }

                                    this.toMachineInventory(inv);
                                    this.progress -= i;
                                }

                                if (flag) {
                                    this.miningX++;
                                    if (this.miningX > (int)Math.floor(aabb.field_72336_d - 0.5)) {
                                        this.miningX = (int)Math.floor(aabb.field_72340_a + 0.5);
                                        this.miningZ++;
                                    }

                                    if (this.miningZ > (int)Math.floor(aabb.field_72334_f - 0.5)) {
                                        this.miningZ = (int)Math.floor(aabb.field_72339_c + 0.5);
                                        this.miningY++;
                                    }

                                    if (this.miningY > (int)Math.floor(aabb.field_72337_e - 0.5)) {
                                        if (this.target == 2 && !this.enableRayTrace) {
                                            this.scanningBlock = false;
                                            this.scannedEntityList.clear();
                                        } else if (this.getState() == 1) {
                                            this.setState(2);
                                            break;
                                        }

                                        this.miningX = (int)Math.floor(aabb.field_72340_a + 0.5);
                                        this.miningY = (int)Math.floor(aabb.field_72338_b + 0.5);
                                        this.miningZ = (int)Math.floor(aabb.field_72339_c + 0.5);
                                    }
                                }

                                if (count == max - 1) {
                                    this.progress = 0L;
                                }
                            }
                        }
                    } else {
                        long cx = (long)((double)this.energyPerTick * (1.0 + 4.0 * Math.log10((double)(this.laserEnergy / 1000L + 1L))));
                        if (this.consumeClayEnergy(cx)) {
                            this.setSyncFlag();
                            this.progress = this.progress
                                + (long)((double)this.progressPerTick * (1.0 + 4.0 * Math.log10((double)(this.laserEnergy / 1000L + 1L))));
                            this.laserEnergy = 0L;

                            for (int count = 0; count < this.maxJobCount; count++) {
                                long i = (long)((double)this.progressPerJob * 1.0);
                                if (i > this.progress) {
                                    break;
                                }

                                List list = this.field_145850_b.func_82733_a(Entity.class, this.aabb, IEntitySelector.field_94557_a);
                                ItemStack filter = this.containerItemStacks[this.filterHarvestSlot];
                                boolean flag = true;

                                for (Object a : list) {
                                    if (this.isCrowded()) {
                                        this.progress = 0L;
                                    }

                                    if (i > this.progress) {
                                        flag = false;
                                        break;
                                    }

                                    if (a instanceof Entity && !this.scannedEntityList.contains(a)) {
                                        this.scannedEntityList.add((Entity)a);
                                        ItemStack itemstack = null;

                                        int jx;
                                        for (jx = 0; jx < 9; jx++) {
                                            if (this.containerItemStacks[jx] != null) {
                                                itemstack = this.containerItemStacks[jx];
                                                break;
                                            }
                                        }

                                        EntityPlayer player = UtilPlayer.getFakePlayerWithItem(null, itemstack);
                                        RayTraceMemory.interactWithEntity(player, (Entity)a);
                                        InventoryPlayer invx = player.field_71071_by;
                                        this.containerItemStacks[jx] = invx.func_70448_g();
                                        this.toMachineInventory(invx);
                                        this.progress -= i;
                                    }
                                }

                                if (flag) {
                                    if (this.getState() == 1) {
                                        this.progress = 0L;
                                        this.setState(2);
                                    } else if (this.target == 2 && !this.enableRayTrace) {
                                        this.scanningBlock = true;
                                    }

                                    this.scannedEntityList.clear();
                                }
                            }
                        }
                    }
                case 2:
            }
        }
    }

    public void pushButton(EntityPlayer player, int action) {
        super.pushButton(player, action);
        switch (action) {
            case 4:
                if (++this.target >= 3) {
                    this.target = 0;
                }
                break;
            case 5:
                this.enableRayTrace = !this.enableRayTrace;
                break;
            case 6:
                this.sneak = !this.sneak;
        }
    }

    public void readWorkdataFromNBT(NBTTagCompound tagCompound) {
        super.readWorkdataFromNBT(tagCompound);
        this.target = tagCompound.func_74771_c("Target");
        this.enableRayTrace = tagCompound.func_74767_n("EnableRayTrace");
        this.sneak = tagCompound.func_74767_n("Sneak");
        this.scanningBlock = tagCompound.func_74767_n("IsScanningBlock");
        if (tagCompound.func_150297_b("RayTraceMemory", 10)) {
            if (this.rayTraceMemory == null) {
                this.rayTraceMemory = RayTraceMemory.getFromNBT(tagCompound.func_74775_l("RayTraceMemory"));
            } else {
                this.rayTraceMemory.readFromNBT(tagCompound.func_74775_l("RayTraceMemory"));
            }
        }
    }

    public void writeWorkdataToNBT(NBTTagCompound tagCompound) {
        super.writeWorkdataToNBT(tagCompound);
        tagCompound.func_74774_a("Target", (byte)this.target);
        tagCompound.func_74757_a("EnableRayTrace", this.enableRayTrace);
        tagCompound.func_74757_a("Sneak", this.sneak);
        tagCompound.func_74757_a("IsScanningBlock", this.scanningBlock);
        if (this.rayTraceMemory != null) {
            NBTTagCompound tag = new NBTTagCompound();
            this.rayTraceMemory.writeToNBT(tag);
            tagCompound.func_74782_a("RayTraceMemory", tag);
        }
    }

    public void setRayTraceMemory(RayTraceMemory memory) {
        this.rayTraceMemory = memory;
        this.func_70296_d();
    }

    public RayTraceMemory getRayTraceMemory() {
        return this.rayTraceMemory;
    }

    public boolean acceptRayTraceMemory(RayTraceMemory memory) {
        return this.areaMode;
    }

    protected void toMachineInventory(InventoryPlayer inv) {
        if (inv != null) {
            inv.func_70299_a(inv.field_70461_c, null);
            inv.field_70462_a = UtilTransfer.produceItemStacks(inv.field_70462_a, this.containerItemStacks, 9, 49, this.func_70297_j_());
            inv.field_70460_b = UtilTransfer.produceItemStacks(inv.field_70460_b, this.containerItemStacks, 9, 49, this.func_70297_j_());
        }
    }

    protected boolean isCrowded() {
        boolean ret = false;

        for (int i = 9; i < 49; i++) {
            this.containerItemStacks[i] = UtilTransfer.produceItemStack(this.containerItemStacks[i], this.containerItemStacks, 0, 9, this.func_70297_j_());
            ret |= this.containerItemStacks[i] != null;
        }

        return ret;
    }
}
