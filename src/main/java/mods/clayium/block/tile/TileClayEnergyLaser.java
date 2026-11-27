package mods.clayium.block.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.laser.ClayLaser;
import mods.clayium.block.laser.ClayLaserManager;
import mods.clayium.block.laser.IClayLaserManager;
import mods.clayium.util.UtilDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileClayEnergyLaser extends TileClayContainerTiered implements IClayLaserManager {
    protected int machineConsumingEnergy;
    protected ClayLaser machineClayLaser;
    protected ClayLaserManager manager;
    @SideOnly(Side.CLIENT)
    private AxisAlignedBB boundingBox;
    protected boolean powered;
    public static int consumingEnergyBlue = 40000;
    public static int consumingEnergyGreen = 400000;
    public static int consumingEnergyRed = 4000000;
    public static int consumingEnergyWhite = 40000000;

    public TileClayEnergyLaser() {
        super();
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.setInstantSyncFlag();
        this.powered = powered;
    }

    public void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = false;
        this.autoExtract = true;
        this.maxAutoInsertDefault = this.maxAutoExtractDefault = 1;
        this.autoExtractInterval = this.autoInsertInterval = 8;
        this.containerItemStacks = new ItemStack[1];
        this.listSlotsInsert.add(new int[]{0});
        this.slotsDrop = new int[]{0};
        this.clayEnergySlot = 0;
        this.manager = new ClayLaserManager();
    }

    public void setManager(World world, int x, int y, int z, UtilDirection direction) {
        if (this.manager == null) {
            this.manager = new ClayLaserManager(world, x, y, z, direction);
        } else {
            this.manager.reset(world, x, y, z, direction);
        }

        this.manager.clayLaser = this.machineClayLaser;
        this.setSyncFlag();
    }

    public void initParamsByTier(int tier) {
        switch (tier) {
            case 7:
                this.machineConsumingEnergy = consumingEnergyBlue;
                this.machineClayLaser = new ClayLaser(0, 1, 0, 0);
                break;
            case 8:
                this.machineConsumingEnergy = consumingEnergyGreen;
                this.machineClayLaser = new ClayLaser(0, 0, 1, 0);
                break;
            case 9:
                this.machineConsumingEnergy = consumingEnergyRed;
                this.machineClayLaser = new ClayLaser(0, 0, 0, 1);
                break;
            default:
                this.machineConsumingEnergy = consumingEnergyWhite;
                this.machineClayLaser = new ClayLaser(0, 3, 3, 3);
        }

        this.manager.clayLaser = this.machineClayLaser;
    }

    public void func_145845_h() {
        super.func_145845_h();
        if (this.manager != null) {
            this.manager
                .set(
                    this.func_145831_w(), this.field_145851_c, this.field_145848_d, this.field_145849_e, UtilDirection.getOrientation(this.getFrontDirection())
                );
            this.manager.update(!this.isPowered() && this.consumeClayEnergy((long)this.machineConsumingEnergy));
        }
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.manager.readFromNBT(tagCompound.func_74775_l("ClayEnergyManager"));
        this.setPowered(tagCompound.func_74767_n("Powered"));
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        NBTTagCompound tagCompound1 = new NBTTagCompound();
        this.manager.writeToNBT(tagCompound1);
        tagCompound.func_74782_a("ClayEnergyManager", tagCompound1);
        tagCompound.func_74757_a("Powered", this.isPowered());
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public ClayLaser getClayLaser() {
        return this.manager == null ? null : this.manager.clayLaser;
    }

    public UtilDirection getDirection() {
        return this.manager == null ? null : this.manager.getDirection();
    }

    public int getLaserLength() {
        return this.manager == null ? 0 : this.manager.getLaserLength();
    }

    public int[] getTargetCoord() {
        return this.manager == null ? null : this.manager.getTargetCoord();
    }

    public boolean hasTarget() {
        return this.manager == null ? false : this.manager.hasTarget();
    }

    public boolean isIrradiating() {
        return this.manager == null ? false : this.manager.isIrradiating();
    }

    @SideOnly(Side.CLIENT)
    public double func_145833_n() {
        return Double.POSITIVE_INFINITY;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        UtilDirection direction = this.getDirection();
        if (direction != null) {
            if (this.boundingBox == null) {
                this.boundingBox = super.getRenderBoundingBox().func_72329_c();
            }

            int l = this.getLaserLength();
            this.boundingBox.field_72336_d = (double)Math.max(this.field_145851_c, this.field_145851_c + direction.offsetX * l) + 1.0;
            this.boundingBox.field_72340_a = (double)Math.min(this.field_145851_c, this.field_145851_c + direction.offsetX * l);
            this.boundingBox.field_72337_e = (double)Math.max(this.field_145848_d, this.field_145848_d + direction.offsetY * l) + 1.0;
            this.boundingBox.field_72338_b = (double)Math.min(this.field_145848_d, this.field_145848_d + direction.offsetY * l);
            this.boundingBox.field_72334_f = (double)Math.max(this.field_145849_e, this.field_145849_e + direction.offsetZ * l) + 1.0;
            this.boundingBox.field_72339_c = (double)Math.min(this.field_145849_e, this.field_145849_e + direction.offsetZ * l);
            return this.boundingBox;
        } else {
            return super.getRenderBoundingBox();
        }
    }

    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
}
