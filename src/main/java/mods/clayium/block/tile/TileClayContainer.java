package mods.clayium.block.tile;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.block.ClayContainer;
import mods.clayium.block.ClayMachines;
import mods.clayium.block.itemblock.IOverridableBlock;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CItems;
import mods.clayium.item.IClayEnergy;
import mods.clayium.item.filter.IItemFilter;
import mods.clayium.item.filter.ItemFilterTemp;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.UtilItemStack;
import mods.clayium.util.UtilTransfer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public abstract class TileClayContainer extends TileGeneric implements ISidedInventory, IInventoryFlexibleStackLimit, IOverridableBlock {
    public ItemStack[] containerItemStacks;
    protected String containerName;
    public ArrayList<int[]> listSlotsInsert = new ArrayList<>();
    public ArrayList<int[]> listSlotsExtract = new ArrayList<>();
    public int[] insertRoutes = new int[]{-1, -1, -1, -1, -1, -1};
    public int[] extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
    public ItemStack[] filters = new ItemStack[6];
    public int[] slotsDrop = new int[0];
    public boolean autoInsert = false;
    public boolean autoExtract = false;
    public int[] maxAutoInsert;
    public int maxAutoInsertDefault = 8;
    public int[] maxAutoExtract;
    public int maxAutoExtractDefault = 8;
    public int autoInsertInterval = 20;
    public int autoExtractInterval = 20;
    protected int autoInsertCount = 0;
    protected int autoExtractCount = 0;
    public long clayEnergy = 0L;
    public int clayEnergySlot = -1;
    public int clayEnergyStorageSize = 1;
    public boolean renderAsPipingMode = false;
    public static boolean restrictionTestMode = false;
    int lastSlotGetStackInSlot = -1;
    int enderIOFirstFreeSlot = -1;
    boolean enderIODoInsertItemInv = false;

    public TileClayContainer() {
        super();
        this.initParams();
    }

    protected void initParams() {
    }

    public void containerName(String string) {
        this.containerName = string;
    }

    public int func_70302_i_() {
        return this.containerItemStacks.length;
    }

    public ItemStack func_70301_a(int slot) {
        if (restrictionTestMode) {
            if (slot <= this.lastSlotGetStackInSlot) {
                this.enderIOFirstFreeSlot = -1;
            }

            this.lastSlotGetStackInSlot = slot;
        }

        if (this.containerItemStacks != null && this.containerItemStacks.length > slot && slot >= 0) {
            if (restrictionTestMode && this.containerItemStacks[slot] == null && this.enderIOFirstFreeSlot == -1) {
                this.enderIOFirstFreeSlot = slot;
            }

            return this.containerItemStacks != null && this.containerItemStacks.length > slot && slot >= 0 ? this.containerItemStacks[slot] : null;
        } else {
            return null;
        }
    }

    public ItemStack func_70298_a(int par1, int par2) {
        this.setSyncFlag();
        if (this.containerItemStacks[par1] != null) {
            if (this.containerItemStacks[par1].field_77994_a <= par2) {
                ItemStack itemstack = this.containerItemStacks[par1];
                this.containerItemStacks[par1] = null;
                return itemstack;
            } else {
                ItemStack itemstack = this.containerItemStacks[par1].func_77979_a(par2);
                if (this.containerItemStacks[par1].field_77994_a == 0) {
                    this.containerItemStacks[par1] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack func_70304_b(int par1) {
        if (this.containerItemStacks[par1] != null) {
            ItemStack itemstack = this.containerItemStacks[par1];
            this.containerItemStacks[par1] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        this.setSyncFlag();
        this.containerItemStacks[slot] = itemstack;
        int stackLimit = restrictionTestMode ? this.getInventoryStackLimit(slot) : this.func_70297_j_();
        if (itemstack != null && itemstack.field_77994_a > stackLimit) {
            itemstack.field_77994_a = stackLimit;
        }
    }

    public String func_145825_b() {
        return this.func_145818_k_() ? this.containerName : this.getDefaultInventoryName();
    }

    public String getDefaultInventoryName() {
        return this.func_145838_q().func_149732_F();
    }

    public boolean func_145818_k_() {
        return this.containerName != null && this.containerName.length() > 0;
    }

    public int func_70297_j_() {
        if (restrictionTestMode) {
            StackTraceElement stackCallingThis = Thread.currentThread().getStackTrace()[2];
            String methodName = stackCallingThis.getMethodName();
            this.enderIODoInsertItemInv = "doInsertItemInv".equals(methodName);
            if (this.enderIODoInsertItemInv) {
                if (stackCallingThis.getLineNumber() < 400) {
                    return this.getInventoryStackLimit(this.lastSlotGetStackInSlot);
                }

                if (this.enderIOFirstFreeSlot != -1) {
                    return this.getInventoryStackLimit(this.enderIOFirstFreeSlot);
                }
            }
        }

        return this.getDefaultInventoryStackLimit();
    }

    public int getDefaultInventoryStackLimit() {
        return 64;
    }

    public int getInventoryStackLimit(int slot) {
        return slot == this.clayEnergySlot ? this.clayEnergyStorageSize : this.getDefaultInventoryStackLimit();
    }

    public boolean func_70300_a(EntityPlayer player) {
        return UtilBuilder.safeGetTileEntity(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e) == this;
    }

    public abstract void func_70295_k_();

    public abstract void func_70305_f();

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        return this.isItemValidForSlotDefault(slot, itemstack);
    }

    public boolean isItemValidForSlotDefault(int slot, ItemStack itemstack) {
        return slot == this.clayEnergySlot ? this.isEnergyClayValid(slot, itemstack) : this.checkInsertionList(slot, itemstack);
    }

    public boolean checkInsertionList(int slot, ItemStack itemstack) {
        for (int i = 0; i < this.listSlotsInsert.size(); i++) {
            int[] slots = this.listSlotsInsert.get(i);

            for (int j = 0; j < slots.length; j++) {
                if (slots[j] == slot) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEnergyClayValid(int slot, ItemStack itemstack) {
        return this.acceptEnergyClay()
            && hasClayEnergy(itemstack)
            && (this.containerItemStacks[slot] == null || this.containerItemStacks[slot].field_77994_a < this.clayEnergyStorageSize);
    }

    public int[] func_94128_d(int side) {
        return this.getAccessibleSlotsFromSideDefault(side);
    }

    public int[] getAccessibleSlotsFromSideDefault(int side) {
        if (!this.canGetFrontDirection()) {
            return new int[0];
        } else {
            int front = this.getFrontDirection();
            int[] flag = new int[this.containerItemStacks.length];

            for (int i = 0; i < this.listSlotsInsert.size(); i++) {
                if (this.insertRoutes[UtilDirection.direction2Side(front, side) - 6] == i) {
                    for (int j = 0; j < ((int[])this.listSlotsInsert.get(i)).length; j++) {
                        flag[((int[])this.listSlotsInsert.get(i))[j]] = 1;
                    }
                }
            }

            for (int ix = 0; ix < this.listSlotsExtract.size(); ix++) {
                if (this.extractRoutes[UtilDirection.direction2Side(front, side) - 6] == ix) {
                    for (int j = 0; j < ((int[])this.listSlotsExtract.get(ix)).length; j++) {
                        flag[((int[])this.listSlotsExtract.get(ix))[j]] = 1;
                    }
                }
            }

            int c = 0;

            for (int ixx = 0; ixx < flag.length; ixx++) {
                if (flag[ixx] == 1) {
                    c++;
                }
            }

            int[] res = new int[c];
            c = 0;

            for (int ixxx = 0; ixxx < flag.length; ixxx++) {
                if (flag[ixxx] == 1) {
                    res[c] = ixxx;
                    c++;
                }
            }

            return res;
        }
    }

    public boolean func_102007_a(int slot, ItemStack itemstack, int side) {
        return this.canInsertItemDefault(slot, itemstack, side);
    }

    public boolean canInsertItemUnsafe(int slot, ItemStack itemstack, int route) {
        if (route >= 0 && route < this.listSlotsInsert.size()) {
            int[] slots = this.listSlotsInsert.get(route);

            for (int j = 0; j < slots.length; j++) {
                if (slots[j] == slot && this.func_94041_b(slot, itemstack)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean canInsertItemDefault(int slot, ItemStack itemstack, int side) {
        if (!this.canGetFrontDirection()) {
            return false;
        } else {
            int front = this.getFrontDirection();
            int d = UtilDirection.direction2Side(front, side) - 6;
            ItemStack filter = this.filters[d];
            return ItemFilterTemp.isFilter(filter) && !ItemFilterTemp.match(filter, itemstack)
                ? false
                : this.canInsertItemUnsafe(slot, itemstack, this.insertRoutes[d]);
        }
    }

    public boolean func_102008_b(int slot, ItemStack itemstack, int side) {
        return this.canExtractItemDefault(slot, itemstack, side);
    }

    public boolean canExtractItemUnsafe(int slot, ItemStack itemstack, int route) {
        if (route >= 0 && route < this.listSlotsExtract.size()) {
            int[] slots = this.listSlotsExtract.get(route);

            for (int j = 0; j < slots.length; j++) {
                if (slots[j] == slot) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean canExtractItemDefault(int slot, ItemStack itemstack, int side) {
        if (!this.canGetFrontDirection()) {
            return false;
        } else {
            int front = this.getFrontDirection();
            int d = UtilDirection.direction2Side(front, side) - 6;
            ItemStack filter = this.filters[d];
            return ItemFilterTemp.isFilter(filter) && !ItemFilterTemp.match(filter, itemstack)
                ? false
                : this.canExtractItemUnsafe(slot, itemstack, this.extractRoutes[d]);
        }
    }

    public void func_145845_h() {
        if (!this.removeFlag) {
            if (!this.field_145850_b.field_72995_K) {
                if (this.autoExtract) {
                    this.autoExtractCount++;
                    if (this.autoExtractCount >= this.autoExtractInterval) {
                        this.autoExtractCount = 0;
                        this.doAutoExtract();
                    }
                } else {
                    this.autoExtractCount = 0;
                }

                if (this.autoInsert) {
                    this.autoInsertCount++;
                    if (this.autoInsertCount >= this.autoInsertInterval) {
                        this.autoInsertCount = 0;
                        this.doAutoInsert();
                    }
                } else {
                    this.autoInsertCount = 0;
                }
            } else {
                Block block = this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                if (block instanceof ClayContainer
                    && ((ClayContainer)block).renderAsPipe(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e)) {
                    boolean flag = ClayiumCore.proxy.renderAsPipingMode();
                    if (flag != this.renderAsPipingMode) {
                        this.weakReRenderFlag = true;
                    }

                    this.renderAsPipingMode = flag;
                }
            }
        }

        super.func_145845_h();
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        this.readCoordFromNBT(tagCompound);
        if (tagCompound.func_74764_b("Items")) {
            this.containerItemStacks = new ItemStack[this.func_70302_i_()];
            UtilItemStack.tagList2Items(tagCompound.func_150295_c("Items", 10), this.containerItemStacks);
        }

        if (tagCompound.func_74764_b("CustomName")) {
            this.containerName = tagCompound.func_74779_i("CustomName");
        }

        if (tagCompound.func_74764_b("InsertRoutes")) {
            int[] temp = tagCompound.func_74759_k("InsertRoutes");
            if (temp.length >= 6) {
                this.insertRoutes = temp;
            }
        }

        if (tagCompound.func_74764_b("ExtractRoutes")) {
            int[] temp = tagCompound.func_74759_k("ExtractRoutes");
            if (temp.length >= 6) {
                this.extractRoutes = temp;
            }
        }

        if (tagCompound.func_74764_b("AutoInsert")) {
            this.autoInsert = tagCompound.func_74767_n("AutoInsert");
        }

        if (tagCompound.func_74764_b("AutoInsertCount")) {
            this.autoInsertCount = tagCompound.func_74765_d("AutoInsertCount");
        }

        if (tagCompound.func_74764_b("AutoExtract")) {
            this.autoExtract = tagCompound.func_74767_n("AutoExtract");
        }

        if (tagCompound.func_74764_b("AutoExtractCount")) {
            this.autoExtractCount = tagCompound.func_74765_d("AutoExtractCount");
        }

        if (tagCompound.func_74764_b("ClayEnergy")) {
            this.clayEnergy = tagCompound.func_74763_f("ClayEnergy");
        }

        if (tagCompound.func_74764_b("Filters")) {
            this.filters = new ItemStack[6];
            UtilItemStack.tagList2Items(tagCompound.func_150295_c("Filters", 10), this.filters);
        }
    }

    public void readCoordFromNBT(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        this.writeCoordToNBT(tagCompound);
        tagCompound.func_74782_a("Items", UtilItemStack.items2TagList(this.containerItemStacks));
        if (this.func_145818_k_()) {
            tagCompound.func_74778_a("CustomName", this.containerName);
        }

        tagCompound.func_74782_a("InsertRoutes", new NBTTagIntArray(this.insertRoutes));
        tagCompound.func_74782_a("ExtractRoutes", new NBTTagIntArray(this.extractRoutes));
        tagCompound.func_74757_a("AutoInsert", this.autoInsert);
        tagCompound.func_74777_a("AutoInsertCount", (short)this.autoInsertCount);
        tagCompound.func_74757_a("AutoExtract", this.autoExtract);
        tagCompound.func_74777_a("AutoExtractCount", (short)this.autoExtractCount);
        tagCompound.func_74772_a("ClayEnergy", this.clayEnergy);
        tagCompound.func_74782_a("Filters", UtilItemStack.items2TagList(this.filters));
    }

    public void writeCoordToNBT(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
    }

    public int[] getSlotsDrop() {
        return this.slotsDrop;
    }

    public void doAutoExtract() {
        if (this.canGetFrontDirection()) {
            for (int i = 0; i < 6; i++) {
                if (this.insertRoutes[i] >= this.listSlotsInsert.size()) {
                    this.insertRoutes[i] = -1;
                }
            }

            int front = this.getFrontDirection();

            for (int side = 0; side < 6; side++) {
                this.doAutoExtractFromSide(side, front);
            }
        }
    }

    public void doAutoExtractFromSide(int side, int front) {
        int route = this.insertRoutes[side];
        if (route >= 0 && route < this.listSlotsInsert.size()) {
            UtilTransfer.extract(
                this,
                this.listSlotsInsert.get(route),
                UtilDirection.getOrientation(front),
                UtilDirection.getSide(side),
                this.maxAutoExtract != null && route < this.maxAutoExtract.length && this.maxAutoExtract[route] >= 0
                    ? this.maxAutoExtract[route]
                    : this.maxAutoExtractDefault
            );
        }
    }

    public void doAutoInsert() {
        if (this.canGetFrontDirection()) {
            for (int i = 0; i < 6; i++) {
                if (this.extractRoutes[i] >= this.listSlotsExtract.size()) {
                    this.extractRoutes[i] = -1;
                }
            }

            int front = this.getFrontDirection();

            for (int side = 0; side < 6; side++) {
                this.doAutoInsertToSide(side, front);
            }
        }
    }

    public void doAutoInsertToSide(int side, int front) {
        int route = this.extractRoutes[side];
        if (route >= 0 && route < this.listSlotsExtract.size()) {
            UtilTransfer.insert(
                this,
                this.listSlotsExtract.get(route),
                UtilDirection.getOrientation(front),
                UtilDirection.getSide(side),
                this.maxAutoInsert != null && route < this.maxAutoInsert.length && this.maxAutoInsert[route] >= 0
                    ? this.maxAutoInsert[route]
                    : this.maxAutoInsertDefault
            );
        }
    }

    public int getItemUseMode(ItemStack itemStack, EntityPlayer player) {
        if (itemStack == null) {
            return -1;
        } else if (itemStack.func_77973_b() == CItems.itemClayRollingPin) {
            return 0;
        } else if (itemStack.func_77973_b() == CItems.itemClaySlicer) {
            return 1;
        } else if (itemStack.func_77973_b() == CItems.itemClaySpatula) {
            return 2;
        } else if (itemStack.func_77973_b() instanceof ItemFilterTemp) {
            return 10;
        } else if (itemStack.func_77973_b() == CItems.itemRawClayCraftingTools) {
            return 11;
        } else if (itemStack.func_77973_b() == CItems.itemClayWrench) {
            return 20;
        } else if (itemStack.func_77973_b() == CItems.itemSynchronizer) {
            return 90;
        } else if (UtilItemStack.areTypeEqual(itemStack, CItems.itemClayPipingTools.get("IO"))) {
            return player.func_70093_af() ? 1 : 0;
        } else if (UtilItemStack.areTypeEqual(itemStack, CItems.itemClayPipingTools.get("Piping"))) {
            return player.func_70093_af() ? 20 : 2;
        } else if (UtilItemStack.areItemDamageEqual(itemStack, CItems.itemClayPipingTools.get("Memory"))) {
            return player.func_70093_af() ? 30 : 31;
        } else {
            return -1;
        }
    }

    public void useItemFromSide(ItemStack itemStack, EntityPlayer player, int side, int mode) {
        switch (mode) {
            case 0:
                this.insertRoutes[side]++;
                if (this.listSlotsInsert.size() <= this.insertRoutes[side]) {
                    this.insertRoutes[side] = -1;
                    player.func_145747_a(new ChatComponentText("Disabled"));
                } else {
                    player.func_145747_a(new ChatComponentText("Set insert route " + String.valueOf(this.insertRoutes[side])));
                }
                break;
            case 1:
                this.extractRoutes[side]++;
                if (this.listSlotsExtract.size() <= this.extractRoutes[side]) {
                    this.extractRoutes[side] = -1;
                    player.func_145747_a(new ChatComponentText("Disabled"));
                } else {
                    player.func_145747_a(new ChatComponentText("Set extract route " + String.valueOf(this.extractRoutes[side])));
                }
                break;
            case 2:
                if (((ClayContainer)this.func_145831_w().func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e)).canChangeRenderType()) {
                    int meta = this.func_145831_w().func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                    this.func_145831_w().func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, meta < 6 ? meta + 6 : meta - 6, 3);
                }
                break;
            case 10:
                ItemStack filter = player.func_71045_bC();
                IItemFilter itemfilter = (IItemFilter)filter.func_77973_b();
                if (!itemfilter.isCopy(filter)) {
                    this.filters[side] = filter.func_77946_l();
                    player.func_145747_a(new ChatComponentText("Applied " + this.filters[side].func_82833_r()));
                    ItemStack appliedFilter = this.filters[side];
                    List<String> list = new ArrayList<>();
                    ((IItemFilter)appliedFilter.func_77973_b()).addFilterInformation(appliedFilter, player, list, true);

                    for (String s : list) {
                        player.func_145747_a(new ChatComponentText(" " + s));
                    }
                } else if (this.filters[side] != null) {
                    IItemFilter itemfilterinv = (IItemFilter)this.filters[side].func_77973_b();
                    player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = itemfilterinv.setCopyFlag(this.filters[side].func_77946_l());
                    player.func_145747_a(new ChatComponentText("Copied " + this.filters[side].func_82833_r()));
                    ItemStack appliedFilter = this.filters[side];
                    List<String> list = new ArrayList<>();
                    ((IItemFilter)appliedFilter.func_77973_b()).addFilterInformation(appliedFilter, player, list, true);

                    for (String s : list) {
                        player.func_145747_a(new ChatComponentText(" " + s));
                    }
                }
                break;
            case 11:
                if (this.filters[side] != null) {
                    player.func_145747_a(new ChatComponentText("Removed " + this.filters[side].func_82833_r()));
                    ItemStack appliedFilter = this.filters[side];
                    List<String> list = new ArrayList<>();
                    ((ItemFilterTemp)appliedFilter.func_77973_b()).addFilterInformation(appliedFilter, player, list, true);

                    for (String s : list) {
                        player.func_145747_a(new ChatComponentText(" " + s));
                    }

                    this.filters[side] = null;
                }
                break;
            case 20:
                if (this.field_145850_b != null) {
                    Block block = this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                    if (block instanceof ClayContainer && ((ClayContainer)block).metaMode != 0) {
                        int front = ((ClayContainer)block).getFront(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
                        int direction = UtilDirection.side2Direction(front, side);
                        if (((ClayContainer)block).metaMode != 1 || direction != 0 && direction != 1) {
                            if (direction != this.field_145850_b.func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e)) {
                                this.field_145850_b.func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, direction, 3);
                            } else {
                                this.field_145850_b
                                    .func_72921_c(
                                        this.field_145851_c,
                                        this.field_145848_d,
                                        this.field_145849_e,
                                        UtilDirection.getOrientation(direction).getOpposite().ordinal(),
                                        3
                                    );
                            }
                        }
                    }
                }
                break;
            case 30:
                if (this.field_145850_b != null) {
                    Block block1 = this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                    if (block1 instanceof ClayContainer) {
                        NBTTagCompound itemtag = itemStack.func_77942_o() ? itemStack.func_77978_p() : new NBTTagCompound();
                        NBTTagCompound memorytag = new NBTTagCompound();
                        memorytag.func_74768_a("MetaMode", ((ClayContainer)block1).metaMode);
                        memorytag.func_74768_a("Metadata", this.field_145850_b.func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e));
                        memorytag.func_74783_a("InsertionRoutes", this.insertRoutes);
                        memorytag.func_74783_a("ExtractionRoutes", this.extractRoutes);
                        itemtag.func_74782_a("ClayContainerIOMemory", memorytag);
                        itemStack.func_77982_d(itemtag);
                        player.func_145747_a(new ChatComponentText("Saved settings to memory"));
                    }
                }
                break;
            case 31:
                if (this.field_145850_b != null) {
                    Block block2 = this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                    if (block2 instanceof ClayContainer) {
                        NBTTagCompound itemtag = itemStack.func_77942_o() ? itemStack.func_77978_p() : new NBTTagCompound();
                        if (itemtag.func_74764_b("ClayContainerIOMemory")) {
                            NBTTagCompound memorytag = itemtag.func_74775_l("ClayContainerIOMemory");
                            int metaMode = memorytag.func_74762_e("MetaMode");
                            if (metaMode == ((ClayContainer)block2).metaMode) {
                                this.field_145850_b
                                    .func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, memorytag.func_74762_e("Metadata"), 3);
                                this.insertRoutes = memorytag.func_74759_k("InsertionRoutes");
                                this.extractRoutes = memorytag.func_74759_k("ExtractionRoutes");
                                player.func_145747_a(new ChatComponentText("Loaded settings from memory"));
                            } else {
                                player.func_145747_a(new ChatComponentText("Invalid data"));
                            }
                        }
                    }
                }
                break;
            case 90:
                if (this.field_145850_b != null && this.acceptInterfaceSync()) {
                    NBTTagCompound tag = itemStack.func_77942_o() ? itemStack.func_77978_p() : new NBTTagCompound();
                    tag.func_74768_a("CoordMemoryX", this.field_145851_c);
                    tag.func_74768_a("CoordMemoryY", this.field_145848_d);
                    tag.func_74768_a("CoordMemoryZ", this.field_145849_e);
                    tag.func_74768_a("CoordMemoryDimID", this.field_145850_b.field_73011_w.field_76574_g);
                    itemStack.func_77982_d(tag);
                    player.func_145747_a(
                        new ChatComponentText(
                            "Loaded Coord ("
                                + this.field_145851_c
                                + ","
                                + this.field_145848_d
                                + ","
                                + this.field_145849_e
                                + ") in dim "
                                + this.field_145850_b.field_73011_w.field_76574_g
                        )
                    );
                }
        }
    }

    public boolean consumeClayEnergy(long energy) {
        if (energy > this.clayEnergy) {
            return this.produceClayEnergy() ? this.consumeClayEnergy(energy) : false;
        } else {
            this.clayEnergy -= energy;
            return true;
        }
    }

    public boolean produceClayEnergy() {
        if (this.clayEnergySlot >= 0 && this.clayEnergySlot < this.containerItemStacks.length) {
            ItemStack itemstack = this.containerItemStacks[this.clayEnergySlot];
            if (!hasClayEnergy(itemstack)) {
                return false;
            } else {
                this.clayEnergy = this.clayEnergy + getClayEnergy(itemstack);
                itemstack.field_77994_a--;
                if (itemstack.field_77994_a <= 0) {
                    this.containerItemStacks[this.clayEnergySlot] = null;
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public boolean acceptEnergyClay() {
        return true;
    }

    public boolean acceptInterfaceSync() {
        return true;
    }

    static boolean hasClayEnergy(ItemStack itemstack) {
        return getClayEnergy(itemstack) > 0L;
    }

    static long getClayEnergy(ItemStack itemstack) {
        if (itemstack == null) {
            return 0L;
        } else {
            return itemstack.func_77973_b() instanceof IClayEnergy ? ((IClayEnergy)itemstack.func_77973_b()).getClayEnergy(itemstack) : 0L;
        }
    }

    public void overrideTo(
        ItemStack itemStack,
        EntityPlayer player,
        World world,
        int x,
        int y,
        int z,
        int side,
        float hitX,
        float hitY,
        float hitZ,
        Block overriddenBlock,
        int overriddenMeta,
        Class overriddenTileEntityClass,
        NBTTagCompound overriddenTileEntityTag
    ) {
        if (overriddenTileEntityTag != null) {
            this.insertRoutes = overriddenTileEntityTag.func_74759_k("InsertRoutes");
            this.extractRoutes = overriddenTileEntityTag.func_74759_k("ExtractRoutes");
            this.clayEnergy = overriddenTileEntityTag.func_74763_f("ClayEnergy");
            NBTTagList tagList = overriddenTileEntityTag.func_150295_c("Filters", 10);
            this.filters = new ItemStack[6];

            for (int i = 0; i < tagList.func_74745_c(); i++) {
                NBTTagCompound tagCompound1 = tagList.func_150305_b(i);
                short byte0 = tagCompound1.func_74765_d("Slot");
                if (byte0 >= 0 && byte0 < this.filters.length) {
                    this.filters[byte0] = ItemStack.func_77949_a(tagCompound1);
                }
            }
        }

        world.func_72921_c(x, y, z, overriddenMeta, 3);
    }

    public boolean canOverride(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (itemStack != null && itemStack.func_77973_b() instanceof ItemBlock) {
            Block block = ((ItemBlock)itemStack.func_77973_b()).field_150939_a;
            if (!(block instanceof ClayContainer)) {
                return false;
            } else if (this.getClass().equals(((ClayContainer)block).tileEntityClass)) {
                String recipeid1 = null;
                String recipeid2 = null;
                if (this instanceof TileClayMachines) {
                    recipeid1 = ((TileClayMachines)this).getRecipeId();
                }

                if (block instanceof ClayMachines) {
                    recipeid2 = ((ClayMachines)block).getRecipeId();
                }

                if (recipeid1 == null) {
                    recipeid1 = "";
                }

                if (recipeid2 == null) {
                    recipeid2 = "";
                }

                return recipeid1.equals(recipeid2);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void onOverridden(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
    }
}
