package mods.clayium.block.tile;

import mods.clayium.item.CItems;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class TileRedstoneInterface extends TileGeneric implements ISynchronizedInterface {
    protected int coreBlockX = 0;
    protected int coreBlockY = 0;
    protected int coreBlockZ = 0;
    protected boolean synced = false;
    protected int lastSignal = 0;
    protected int lastPower = 0;
    public static String[] stateSequence = new String[]{
        "None", "Emit if idle", "Emit if work scheduled", "Emit if doing work", "Do work", "Do not work", "Start work", "Stop work", "Do work once"
    };
    protected String state = "";
    protected boolean loop = false;
    public boolean acceptCoordChanger = true;
    protected boolean interDimensional = false;
    protected int dimentionId = 0;

    public TileRedstoneInterface() {
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

    public void func_145845_h() {
        this.syncCoreBlock();
        super.func_145845_h();
        int signal = this.getSignal();
        int power = this.getProvidingWeakPower();
        if (this.isSynced()) {
            IExternalControl te = this.getCore();
            if (this.state.equals("Do work")) {
                if (this.lastSignal <= 0 && signal > 0) {
                    te.startWork();
                }

                if (this.lastSignal != 0 && signal == 0) {
                    te.stopWork();
                }
            }

            if (this.state.equals("Do not work")) {
                if (this.lastSignal <= 0 && signal > 0) {
                    te.stopWork();
                }

                if (this.lastSignal != 0 && signal == 0) {
                    te.startWork();
                }
            }

            if (this.state.equals("Start work") && this.lastSignal <= 0 && signal > 0) {
                te.startWork();
            }

            if (this.state.equals("Stop work") && this.lastSignal <= 0 && signal > 0) {
                te.stopWork();
            }

            if (this.state.equals("Do work once") && this.lastSignal <= 0 && signal > 0) {
                te.doWorkOnce();
            }
        }

        if (this.lastPower != power || this.lastSignal != signal) {
            this.field_145850_b
                .func_147444_c(
                    this.field_145851_c,
                    this.field_145848_d,
                    this.field_145849_e,
                    this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e)
                );
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        }

        this.lastSignal = signal;
        this.lastPower = power;
    }

    public int getItemUseMode(ItemStack itemStack, EntityPlayer player) {
        if (itemStack == null) {
            return -1;
        } else if (itemStack.func_77973_b() == CItems.itemSynchronizer) {
            return 90;
        } else {
            return UtilItemStack.areItemDamageEqual(itemStack, CItems.itemMisc.get("SynchronousParts")) ? 91 : -1;
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

    public int getProvidingWeakPower() {
        if (this.isSynced()) {
            IExternalControl te = this.getCore();
            if (this.state.equals("Emit if work scheduled")) {
                return te.isScheduled() ? 15 : 0;
            }

            if (this.state.equals("Emit if doing work")) {
                return te.isDoingWork() ? 15 : 0;
            }

            if (this.state.equals("Emit if idle")) {
                return te.isDoingWork() ? 0 : 15;
            }
        }

        return 0;
    }

    public IExternalControl getCore() {
        if (this.isSynced()) {
            int coreX = this.field_145851_c + this.getCoreBlockXCoord();
            int coreY = this.field_145848_d + this.getCoreBlockYCoord();
            int coreZ = this.field_145849_e + this.getCoreBlockZCoord();
            return (IExternalControl)UtilBuilder.safeGetTileEntity(this.field_145850_b, coreX, coreY, coreZ);
        } else {
            return null;
        }
    }

    public int getSignal() {
        return this.func_145831_w().func_94572_D(this.field_145851_c, this.field_145848_d, this.field_145849_e);
    }

    public String changeState() {
        for (int i = 0; i < stateSequence.length; i++) {
            if (stateSequence[i].equals(this.state)) {
                int j = i == stateSequence.length - 1 ? 0 : i + 1;
                this.changeState(stateSequence[j]);
                return stateSequence[j];
            }
        }

        this.changeState(stateSequence[0]);
        return stateSequence[0];
    }

    public void changeState(String state) {
        this.lastSignal = -1;

        for (String s : stateSequence) {
            if (s.equals(state)) {
                this.state = state;
            }
        }

        this.setInstantSyncFlag();
    }

    public String getState() {
        return this.state;
    }

    protected boolean syncCoreBlock(int x, int y, int z) {
        int coreX = this.field_145851_c + x;
        int coreY = this.field_145848_d + y;
        int coreZ = this.field_145849_e + z;
        if (this.getCoreWorld() == null
            || !(UtilBuilder.safeGetTileEntity(this.getCoreWorld(), coreX, coreY, coreZ) instanceof IExternalControl)
            || x == 0 && y == 0 && z == 0) {
            this.coreBlockX = this.coreBlockY = this.coreBlockZ = 0;
            this.synced = false;
            return false;
        } else {
            this.synced = true;
            return true;
        }
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        this.syncCoreBlock();
        this.readCoordFromNBT(tagCompound);
        this.coreBlockX = tagCompound.func_74762_e("CoreBlockX");
        this.coreBlockY = tagCompound.func_74762_e("CoreBlockY");
        this.coreBlockZ = tagCompound.func_74762_e("CoreBlockZ");
        this.acceptCoordChanger = tagCompound.func_74767_n("AcceptCoordChanger");
        this.interDimensional = tagCompound.func_74767_n("InterDimensional");
        this.dimentionId = tagCompound.func_74762_e("CoreBlockDimentionId");
        this.state = tagCompound.func_74779_i("State");
        this.lastSignal = tagCompound.func_74762_e("LastSignal");
        this.lastPower = tagCompound.func_74762_e("LastPower");
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        this.syncCoreBlock();
        this.writeCoordToNBT(tagCompound);
        tagCompound.func_74768_a("CoreBlockX", this.coreBlockX);
        tagCompound.func_74768_a("CoreBlockY", this.coreBlockY);
        tagCompound.func_74768_a("CoreBlockZ", this.coreBlockZ);
        tagCompound.func_74757_a("AcceptCoordChanger", this.acceptCoordChanger);
        tagCompound.func_74757_a("InterDimensional", this.interDimensional);
        tagCompound.func_74768_a("CoreBlockDimentionId", this.dimentionId);
        tagCompound.func_74778_a("State", this.state);
        tagCompound.func_74768_a("LastSignal", this.lastSignal);
        tagCompound.func_74768_a("LastPower", this.lastPower);
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
