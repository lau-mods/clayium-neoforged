package mods.clayium.block.tile;

import java.util.ArrayList;
import mods.clayium.item.CItems;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class TileClayContainerInterface extends TileClayContainerTiered implements ISynchronizedInterface {
    protected int coreBlockX = 0;
    protected int coreBlockY = 0;
    protected int coreBlockZ = 0;
    protected boolean synced = false;
    TileClayContainer core = null;
    protected boolean loop = false;
    public boolean acceptCoordChanger = false;
    public boolean interDimensional = false;
    protected int dimentionId = 0;

    public TileClayContainerInterface() {
        super();
    }

    public boolean setCoreBlockCoord(int x, int y, int z) {
        this.setSyncFlag();
        this.coreBlockX = x;
        this.coreBlockY = y;
        this.coreBlockZ = z;
        return this.syncCoreBlock();
    }

    public int getCoreBlockXCoord() {
        return this.coreBlockX;
    }

    public int getCoreBlockYCoord() {
        return this.coreBlockY;
    }

    public int getCoreBlockZCoord() {
        return this.coreBlockZ;
    }

    protected boolean syncCoreBlock() {
        return this.syncCoreBlock(this.coreBlockX, this.coreBlockY, this.coreBlockZ);
    }

    public boolean isSynced() {
        return this.synced;
    }

    protected boolean syncCoreBlock(int x, int y, int z) {
        int coreX = this.field_145851_c + x;
        int coreY = this.field_145848_d + y;
        int coreZ = this.field_145849_e + z;
        TileEntity te;
        if (this.getCoreWorld() != null
            && (te = UtilBuilder.safeGetTileEntity(this.getCoreWorld(), coreX, coreY, coreZ)) instanceof TileClayContainer
            && ((TileClayContainer)te).acceptInterfaceSync()
            && (x != 0 || y != 0 || z != 0)) {
            this.core = (TileClayContainer)te;
            this.containerItemStacks = this.core.containerItemStacks;
            this.listSlotsInsert = this.core.listSlotsInsert;
            this.listSlotsExtract = this.core.listSlotsExtract;

            for (int i = 0; i < 6; i++) {
                if (this.insertRoutes[i] >= this.listSlotsInsert.size()) {
                    this.insertRoutes[i] = -1;
                }

                if (this.extractRoutes[i] >= this.listSlotsExtract.size()) {
                    this.extractRoutes[i] = -1;
                }
            }

            this.slotsDrop = new int[0];
            this.autoInsert = this.core.autoInsert;
            this.autoExtract = this.core.autoExtract;
            this.maxAutoInsert = this.core.maxAutoInsert;
            this.maxAutoInsertDefault = this.core.maxAutoInsertDefault;
            this.maxAutoExtract = this.core.maxAutoExtract;
            this.maxAutoExtractDefault = this.core.maxAutoExtractDefault;
            this.autoInsertInterval = this.core.autoInsertInterval;
            this.autoExtractInterval = this.core.autoExtractInterval;
            this.clayEnergySlot = this.core.clayEnergySlot;
            this.synced = true;
            return true;
        } else {
            this.containerItemStacks = new ItemStack[]{null};
            this.listSlotsInsert = new ArrayList<>();
            this.listSlotsInsert.add(new int[]{0});
            this.listSlotsExtract = new ArrayList<>();
            this.listSlotsExtract.add(new int[]{0});
            this.slotsDrop = new int[0];
            this.autoInsert = false;
            this.autoExtract = false;
            this.maxAutoInsert = null;
            this.maxAutoInsertDefault = 1;
            this.maxAutoExtract = null;
            this.maxAutoExtractDefault = 1;
            this.autoInsertInterval = 1;
            this.autoExtractInterval = 1;
            this.clayEnergySlot = 0;
            this.synced = false;
            this.core = null;
            return false;
        }
    }

    public int func_70302_i_() {
        this.syncCoreBlock();
        return this.core != null ? this.core.func_70302_i_() : super.func_70302_i_();
    }

    public ItemStack func_70301_a(int slot) {
        this.syncCoreBlock();
        return this.core != null ? this.core.func_70301_a(slot) : super.func_70301_a(slot);
    }

    public ItemStack func_70298_a(int par1, int par2) {
        this.syncCoreBlock();
        return this.core != null ? this.core.func_70298_a(par1, par2) : super.func_70298_a(par1, par2);
    }

    public ItemStack func_70304_b(int par1) {
        this.syncCoreBlock();
        return this.core != null ? this.core.func_70304_b(par1) : super.func_70304_b(par1);
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        if (this.syncCoreBlock()) {
            this.core.func_70299_a(slot, itemstack);
        }
    }

    public int func_70297_j_() {
        this.syncCoreBlock();
        return this.core != null ? this.core.func_70297_j_() : super.func_70297_j_();
    }

    public int getInventoryStackLimit(int slot) {
        this.syncCoreBlock();
        return this.core != null ? this.core.getInventoryStackLimit(slot) : super.getInventoryStackLimit(slot);
    }

    public boolean func_70300_a(EntityPlayer player) {
        return this.syncCoreBlock() && super.func_70300_a(player);
    }

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        return this.syncCoreBlock() && this.core.func_94041_b(slot, itemstack);
    }

    public int[] func_94128_d(int side) {
        this.syncCoreBlock();
        return super.func_94128_d(side);
    }

    public boolean canInsertItemUnsafe(int slot, ItemStack itemstack, int route) {
        return this.syncCoreBlock() && super.canInsertItemUnsafe(slot, itemstack, route);
    }

    public boolean canExtractItemUnsafe(int slot, ItemStack itemstack, int route) {
        return this.syncCoreBlock() && super.canExtractItemUnsafe(slot, itemstack, route);
    }

    public void func_145845_h() {
        this.syncCoreBlock();
        super.func_145845_h();
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        this.syncCoreBlock();
        this.readCoordFromNBT(tagCompound);
        this.insertRoutes = tagCompound.func_74759_k("InsertRoutes");
        this.extractRoutes = tagCompound.func_74759_k("ExtractRoutes");
        if (this.insertRoutes == null || this.insertRoutes.length < 6) {
            this.insertRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        }

        if (this.extractRoutes == null || this.extractRoutes.length < 6) {
            this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        }

        this.autoInsertCount = tagCompound.func_74765_d("AutoInsertCount");
        this.autoExtractCount = tagCompound.func_74765_d("AutoExtractCount");
        this.filters = new ItemStack[6];
        UtilItemStack.tagList2Items(tagCompound.func_150295_c("Filters", 10), this.filters);
        if (tagCompound.func_150297_b("CustomName", 8)) {
            this.containerName = tagCompound.func_74779_i("CustomName");
        }

        this.coreBlockX = tagCompound.func_74762_e("CoreBlockX");
        this.coreBlockY = tagCompound.func_74762_e("CoreBlockY");
        this.coreBlockZ = tagCompound.func_74762_e("CoreBlockZ");
        this.acceptCoordChanger = tagCompound.func_74767_n("AcceptCoordChanger");
        this.interDimensional = tagCompound.func_74767_n("InterDimensional");
        this.dimentionId = tagCompound.func_74762_e("CoreBlockDimentionId");
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        this.syncCoreBlock();
        this.writeCoordToNBT(tagCompound);
        tagCompound.func_74777_a("AutoInsertCount", (short)this.autoInsertCount);
        tagCompound.func_74777_a("AutoExtractCount", (short)this.autoExtractCount);
        tagCompound.func_74782_a("InsertRoutes", new NBTTagIntArray(this.insertRoutes));
        tagCompound.func_74782_a("ExtractRoutes", new NBTTagIntArray(this.extractRoutes));
        tagCompound.func_74782_a("Filters", UtilItemStack.items2TagList(this.filters));
        if (this.func_145818_k_()) {
            tagCompound.func_74778_a("CustomName", this.containerName);
        }

        tagCompound.func_74768_a("CoreBlockX", this.coreBlockX);
        tagCompound.func_74768_a("CoreBlockY", this.coreBlockY);
        tagCompound.func_74768_a("CoreBlockZ", this.coreBlockZ);
        tagCompound.func_74757_a("AcceptCoordChanger", this.acceptCoordChanger);
        tagCompound.func_74757_a("InterDimensional", this.interDimensional);
        tagCompound.func_74768_a("CoreBlockDimentionId", this.dimentionId);
    }

    public int[] getSlotsDrop() {
        this.syncCoreBlock();
        return super.getSlotsDrop();
    }

    public int getFrontDirection() {
        this.syncCoreBlock();
        return super.getFrontDirection();
    }

    public void doAutoExtract() {
        if (this.syncCoreBlock()) {
            super.doAutoExtract();
        }
    }

    public void doAutoInsert() {
        if (this.syncCoreBlock()) {
            super.doAutoInsert();
        }
    }

    public int getItemUseMode(ItemStack itemStack, EntityPlayer player) {
        if (itemStack == null) {
            return -1;
        } else if (UtilItemStack.areItemDamageEqual(itemStack, CItems.itemMisc.get("SynchronousParts"))) {
            return 91;
        } else {
            this.syncCoreBlock();
            return super.getItemUseMode(itemStack, player);
        }
    }

    public boolean isUsable(ItemStack itemStack, EntityPlayer player, int direction, float hitX, float hitY, float hitZ) {
        return this.getItemUseMode(itemStack, player) != 90 && this.getItemUseMode(itemStack, player) != 91
            ? this.syncCoreBlock() && super.isUsable(itemStack, player, direction, hitX, hitY, hitZ)
            : true;
    }

    public void useItemFromSide(ItemStack itemStack, EntityPlayer player, int side, int mode) {
        if (mode == 91 && !this.acceptCoordChanger) {
            this.acceptCoordChanger = true;
            if (--player.func_71045_bC().field_77994_a == 0) {
                player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = null;
            }
        }

        if (mode == 90 && itemStack.func_77942_o() && this.acceptCoordChanger) {
            NBTTagCompound tag = itemStack.func_77978_p();
            int cx = tag.func_74762_e("CoordMemoryX");
            int cy = tag.func_74762_e("CoordMemoryY");
            int cz = tag.func_74762_e("CoordMemoryZ");
            int cd = tag.func_74762_e("CoordMemoryDimID");
            if (this.setCoreBlockDimension(cd) && this.setCoreBlockCoord(cx - this.field_145851_c, cy - this.field_145848_d, cz - this.field_145849_e)) {
                player.func_145747_a(new ChatComponentText("Set Core block Coord (" + cx + "," + cy + "," + cz + ") in dim " + cd));
            }
        } else if (this.syncCoreBlock()) {
            super.useItemFromSide(itemStack, player, side, mode);
        }

        this.setSyncFlag();
    }

    public boolean consumeClayEnergy(long energy) {
        return false;
    }

    public boolean produceClayEnergy() {
        return false;
    }

    public boolean acceptEnergyClay() {
        return this.syncCoreBlock() && super.acceptEnergyClay();
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public void initParamsByTier(int tier) {
    }

    public void setSyncFlag() {
        if (!this.field_145850_b.field_72995_K && !this.loop) {
            int coreX = this.field_145851_c + this.coreBlockX;
            int coreY = this.field_145848_d + this.coreBlockY;
            int coreZ = this.field_145849_e + this.coreBlockZ;
            if (this.getCoreWorld() != null) {
                TileEntity te = UtilBuilder.safeGetTileEntity(this.getCoreWorld(), coreX, coreY, coreZ);
                if (te instanceof IGeneralInterface && te != this) {
                    this.loop = true;
                    ((IGeneralInterface)te).setSyncFlag();
                    this.loop = false;
                }
            }
        }

        super.setSyncFlag();
    }

    public void setInstantSyncFlag() {
        if (!this.field_145850_b.field_72995_K && !this.loop) {
            int coreX = this.field_145851_c + this.coreBlockX;
            int coreY = this.field_145848_d + this.coreBlockY;
            int coreZ = this.field_145849_e + this.coreBlockZ;
            if (this.getCoreWorld() != null) {
                TileEntity te = UtilBuilder.safeGetTileEntity(this.getCoreWorld(), coreX, coreY, coreZ);
                if (te instanceof IGeneralInterface && te != this) {
                    this.loop = true;
                    ((IGeneralInterface)te).setInstantSyncFlag();
                    this.loop = false;
                }
            }
        }

        super.setInstantSyncFlag();
    }

    public World getCoreWorld() {
        return (World)(this.interDimensional ? DimensionManager.getWorld(this.dimentionId) : this.field_145850_b);
    }

    public boolean setCoreBlockDimension(int d) {
        this.dimentionId = d;
        this.interDimensional = true;
        if (this.field_145850_b != null && this.field_145850_b.field_73011_w.field_76574_g == d) {
            this.interDimensional = false;
            return true;
        } else {
            if (this.field_145850_b != null && !this.field_145850_b.field_72995_K) {
                if (this.getCoreWorld() == null) {
                    this.interDimensional = false;
                    return false;
                }

                if (this.getCoreWorld() == this.field_145850_b) {
                    this.interDimensional = false;
                    return true;
                }
            }

            return true;
        }
    }

    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    public boolean acceptCoordChanger() {
        return this.acceptCoordChanger;
    }
}
