package mods.clayium.misc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class ClayTank extends FluidTank {
    public ClayTank(int capacity) {
        super(capacity);
    }

    public ClayTank(FluidStack stack, int capacity) {
        super(stack, capacity);
    }

    public ClayTank(Fluid fluid, int amount, int capacity) {
        super(fluid, amount, capacity);
    }

    public boolean isEmpty() {
        return this.getFluid() == null || this.getFluid().getFluid() == null || this.getFluid().amount <= 0;
    }

    public boolean isFull() {
        return this.getFluid() != null && this.getFluid().amount == this.getCapacity();
    }

    public Fluid getFluidType() {
        return this.getFluid() != null ? this.getFluid().getFluid() : null;
    }

    public String getFluidName() {
        return this.fluid != null && this.fluid.getFluid() != null ? this.fluid.getFluid().getLocalizedName(this.fluid) : "Empty";
    }

    @SideOnly(Side.CLIENT)
    public void setAmount(int par1) {
        if (this.fluid != null && this.fluid.getFluid() != null) {
            this.fluid.amount = par1;
        }
    }
}
