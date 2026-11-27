package mods.clayium.misc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileFluidTab extends TileEntity implements IFluidHandler {
    public ClayTank productTank = new ClayTank(1000);

    public TileFluidTab() {
        super();
    }

    public void func_145839_a(NBTTagCompound par1NBTTagCompound) {
        super.func_145839_a(par1NBTTagCompound);
        this.productTank = new ClayTank(1000);
        if (par1NBTTagCompound.func_74764_b("productTank")) {
            this.productTank.readFromNBT(par1NBTTagCompound.func_74775_l("productTank"));
        }
    }

    public void func_145841_b(NBTTagCompound par1NBTTagCompound) {
        super.func_145841_b(par1NBTTagCompound);
        NBTTagCompound tank = new NBTTagCompound();
        this.productTank.writeToNBT(tank);
        par1NBTTagCompound.func_74782_a("productTank", tank);
    }

    public Packet func_145844_m() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.func_145841_b(nbtTagCompound);
        return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTagCompound);
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.func_145839_a(pkt.func_148857_g());
    }

    @SideOnly(Side.CLIENT)
    public int getMetadata() {
        return this.field_145850_b.func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFluidIcon() {
        Fluid fluid = this.productTank.getFluidType();
        return fluid != null ? fluid.getIcon() : null;
    }

    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        } else {
            return this.productTank.getFluidType() == resource.getFluid() ? this.productTank.drain(resource.amount, doDrain) : null;
        }
    }

    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return this.productTank.drain(maxDrain, doDrain);
    }

    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource != null && resource.getFluid() != null) {
            FluidStack current = this.productTank.getFluid();
            FluidStack resourceCopy = resource.copy();
            if (current != null && current.amount > 0 && !current.isFluidEqual(resourceCopy)) {
                return 0;
            } else {
                int i = 0;
                int used = this.productTank.fill(resourceCopy, doFill);
                resourceCopy.amount -= used;
                return i + used;
            }
        } else {
            return 0;
        }
    }

    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid != null && this.productTank.isEmpty();
    }

    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{this.productTank.getInfo()};
    }
}
