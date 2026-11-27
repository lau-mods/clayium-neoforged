package mods.clayium.block.tile;

import java.util.ArrayList;
import java.util.Random;
import mods.clayium.block.ClayContainer;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileGeneric extends TileEntity implements IGeneralInterface {
    protected boolean syncFlag;
    protected boolean instantSyncFlag;
    protected boolean strongSyncMode = false;
    protected boolean weakSyncMode = false;
    protected boolean renderSyncFlag = false;
    protected boolean strongReRenderFlag = false;
    protected boolean weakReRenderFlag = false;
    protected boolean removeFlag = false;
    private int syncTiming = -1;
    protected static Random random = new Random();
    private int oldMetadata = -1;
    private int oldFrontDirection = -1;
    protected NBTTagCompound oldSendedTag = null;
    protected NBTTagCompound oldReceivedTag = null;

    public TileGeneric() {
        super();
    }

    public void func_145845_h() {
        if (this.removeFlag) {
            this.func_145831_w().func_147475_p(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        } else {
            if (this.syncFlag) {
                this.markDirtyWithoutSync();
                if (this.syncTiming == -1) {
                    this.syncTiming = random.nextInt(ClayiumCore.cfgPacketSendingRate);
                }

                if (this.field_145850_b.func_82737_E() % (long)ClayiumCore.cfgPacketSendingRate == (long)this.syncTiming
                    || this.instantSyncFlag && ClayiumCore.cfgEnableInstantSync) {
                    this.strongSyncMode = true;
                    this.markForStrongUpdate();
                    this.syncFlag = false;
                    this.instantSyncFlag = false;
                }
            }

            if (this.field_145850_b.field_72995_K
                && (this.strongReRenderFlag || this.weakReRenderFlag && this.field_145850_b.func_82737_E() % (long)ClayiumCore.cfgRenderingRate == 0L)) {
                this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                this.weakReRenderFlag = this.strongReRenderFlag = false;
            }
        }
    }

    public boolean isUsable(ItemStack itemStack, EntityPlayer player, int direction, float hitX, float hitY, float hitZ) {
        return this.getItemUseMode(itemStack, player) != -1;
    }

    public void useItem(ItemStack itemStack, EntityPlayer player, int direction, float hitX, float hitY, float hitZ) {
        if (!this.field_145850_b.field_72995_K) {
            int side = UtilDirection.direction2Side(this.getFrontDirection(), direction) - 6;
            this.useItemFromSide(itemStack, player, side, this.getItemUseMode(itemStack, player));
        }

        this.setInstantSyncFlag();

        for (ForgeDirection direction1 : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = UtilDirection.getTileEntity(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, direction1);
            if (tile instanceof IGeneralInterface) {
                ((IGeneralInterface)tile).setInstantSyncFlag();
            }
        }
    }

    public void setRemoveFlag() {
        this.removeFlag = true;
    }

    public boolean getRemoveFlag() {
        return this.removeFlag;
    }

    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
        return (world.field_72995_K || this.shouldRefresh() || newBlock.hasTileEntity(newMeta)) && oldBlock != newBlock;
    }

    public boolean shouldRefresh() {
        return true;
    }

    public boolean hasSpecialDrops() {
        return false;
    }

    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, Block block, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<>();
        ItemStack itemstack = getNormalDrop(world, block, metadata, fortune);
        writeTileEntityTagToItemStack(itemstack, this);
        ret.add(itemstack);
        return ret;
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
        readTileEntityTagFromItemStack(itemstack, this, x, y, z);
    }

    public static ItemStack getNormalDrop(World world, Block block, int metadata, int fortune) {
        Item item = block.func_149650_a(metadata, world.field_73012_v, fortune);
        return new ItemStack(item, 1, block.func_149692_a(metadata));
    }

    public static void writeTileEntityTagToItemStack(ItemStack item, TileEntity tile) {
        if (item != null && tile != null) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound tetag = new NBTTagCompound();
            tile.func_145841_b(tetag);
            tetag.func_82580_o("x");
            tetag.func_82580_o("y");
            tetag.func_82580_o("z");
            tag.func_74782_a("TileEntityNBTTag", tetag);
            item.func_77982_d(tag);
        }
    }

    public static void readTileEntityTagFromItemStack(ItemStack item, TileEntity tile, int x, int y, int z) {
        NBTTagCompound tetag = getTileEntityTag(item);
        if (tetag != null) {
            tetag.func_74768_a("x", x);
            tetag.func_74768_a("y", y);
            tetag.func_74768_a("z", z);
            tile.func_145839_a(tetag);
        }
    }

    public static NBTTagCompound getTileEntityTag(ItemStack item) {
        if (item != null && item.func_77942_o()) {
            NBTTagCompound tag = item.func_77978_p();
            if (tag.func_74764_b("TileEntityNBTTag")) {
                return (NBTTagCompound)tag.func_74781_a("TileEntityNBTTag");
            }
        }

        return null;
    }

    public int getFrontDirection() {
        if (this.field_145850_b == null) {
            return -1;
        } else {
            int newMetadata = this.func_145832_p();
            if (this.oldMetadata != newMetadata || this.oldFrontDirection == -1) {
                this.oldMetadata = newMetadata;
                this.oldFrontDirection = this.refreshFrontDirection();
            }

            return this.oldFrontDirection;
        }
    }

    public int refreshFrontDirection() {
        Block block = this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        return block instanceof ClayContainer ? ((ClayContainer)block).getFront(this) : this.func_145832_p();
    }

    public boolean canGetFrontDirection() {
        return this.field_145850_b != null
            && this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e) instanceof ClayContainer;
    }

    public int getItemUseMode(ItemStack itemStack, EntityPlayer player) {
        return -1;
    }

    public void useItemFromSide(ItemStack itemStack, EntityPlayer player, int side, int mode) {
    }

    public Packet func_145844_m() {
        boolean fullTag = this.strongSyncMode || !this.weakSyncMode;
        this.strongSyncMode = false;
        this.weakSyncMode = false;
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.func_145841_b(nbtTagCompound);
        if (fullTag) {
            this.oldSendedTag = (NBTTagCompound)nbtTagCompound.func_74737_b();
            nbtTagCompound.func_74757_a("__FullTag", true);
            if (this.renderSyncFlag) {
                nbtTagCompound.func_74757_a("__ReRender", true);
                this.renderSyncFlag = false;
            }

            return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTagCompound);
        } else {
            NBTTagCompound tagToSend = new NBTTagCompound();
            NBTTagList deletedList = null;
            if (this.oldSendedTag == null) {
                tagToSend = nbtTagCompound;
            } else {
                for (Object o : this.oldSendedTag.func_150296_c()) {
                    if (o instanceof String) {
                        String key = (String)o;
                        NBTBase oldTag = this.oldSendedTag.func_74781_a(key);
                        if (nbtTagCompound.func_74764_b(key)) {
                            NBTBase newTag = nbtTagCompound.func_74781_a(key);
                            if (!newTag.equals(oldTag)) {
                                tagToSend.func_74782_a(key, newTag);
                            }
                        } else {
                            if (deletedList == null) {
                                deletedList = new NBTTagList();
                            }

                            deletedList.func_74742_a(new NBTTagString(key));
                        }
                    }
                }

                if (deletedList != null) {
                    tagToSend.func_74782_a("__DeletedTags", deletedList);
                }
            }

            this.oldSendedTag = (NBTTagCompound)nbtTagCompound.func_74737_b();
            if (this.renderSyncFlag) {
                tagToSend.func_74757_a("__ReRender", true);
                this.renderSyncFlag = false;
            }

            return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, tagToSend);
        }
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.func_70296_d();
        NBTTagCompound tagReceived = pkt.func_148857_g();
        if (tagReceived.func_74764_b("__ReRender")) {
            tagReceived.func_82580_o("__ReRender");
            this.strongReRenderFlag = true;
        }

        if (tagReceived.func_74764_b("__FullTag")) {
            tagReceived.func_82580_o("__FullTag");
            this.oldReceivedTag = (NBTTagCompound)tagReceived.func_74737_b();
            this.func_145839_a(tagReceived);
            this.weakReRenderFlag = true;
        } else {
            NBTTagCompound tagToRead = new NBTTagCompound();
            if (this.oldReceivedTag != null) {
                this.func_145841_b(tagToRead);
                if (tagReceived.func_74764_b("__DeletedTags")) {
                    NBTTagList deletedList = tagReceived.func_150295_c("__DeletedTags", 8);

                    for (int i = 0; i < deletedList.func_74745_c(); i++) {
                        tagToRead.func_82580_o(deletedList.func_150307_f(i));
                    }
                }

                for (Object o : tagReceived.func_150296_c()) {
                    if (o instanceof String) {
                        String key = (String)o;
                        NBTBase newTag = tagReceived.func_74781_a(key);
                        tagToRead.func_74782_a(key, newTag);
                    }
                }

                this.oldReceivedTag = (NBTTagCompound)tagToRead.func_74737_b();
                this.func_145839_a(tagToRead);
            }
        }
    }

    public void func_70296_d() {
        this.markDirtyWithoutSync();
        this.setSyncFlag();
    }

    public void markDirtyWithoutSync() {
        super.func_70296_d();
    }

    public void markForStrongUpdate() {
        if (this.field_145850_b != null && !this.field_145850_b.field_72995_K) {
            this.strongSyncMode = true;
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        }
    }

    public void markForWeakUpdate() {
        if (this.field_145850_b != null && !this.field_145850_b.field_72995_K) {
            this.weakSyncMode = true;
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        }
    }

    public void setSyncFlag() {
        if (this.field_145850_b != null && !this.field_145850_b.field_72995_K) {
            this.syncFlag = true;
        }
    }

    public void setInstantSyncFlag() {
        if (this.field_145850_b != null && !this.field_145850_b.field_72995_K) {
            this.syncFlag = true;
            this.instantSyncFlag = true;
            this.setRenderSyncFlag();
        }
    }

    public void setRenderSyncFlag() {
        if (this.field_145850_b != null && !this.field_145850_b.field_72995_K) {
            this.renderSyncFlag = true;
        }
    }

    public void readCoordFromNBT(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
    }

    public void writeCoordToNBT(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
    }

    public void pushButton(EntityPlayer player, int action) {
    }
}
