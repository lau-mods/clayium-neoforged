package mods.clayium.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotWithTexture extends Slot {
    private ITexture texture;
    private boolean restricted = false;
    protected Container listener = null;

    public SlotWithTexture(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        this(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_, RectangleTexture.SmallSlotTexture);
    }

    public SlotWithTexture(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ITexture texture) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.texture = texture;
    }

    public SlotWithTexture(Container listener, IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        this(listener, p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_, RectangleTexture.SmallSlotTexture);
    }

    public SlotWithTexture(Container listener, IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ITexture texture) {
        this(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_, texture);
        this.listener = listener;
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiScreen gui, int offsetX, int offsetY) {
        this.texture
            .draw(gui, offsetX + this.field_75223_e - (this.texture.getSizeX() - 16) / 2, offsetY + this.field_75221_f - (this.texture.getSizeY() - 16) / 2);
    }

    public boolean func_75214_a(ItemStack itemstack) {
        return this.restricted ? this.field_75224_c.func_94041_b(this.getSlotIndex(), itemstack) : true;
    }

    public void setRestricted() {
        this.restricted = true;
    }

    public void func_75218_e() {
        if (this.listener != null) {
            this.listener.func_75130_a(this.field_75224_c);
        }

        super.func_75218_e();
    }

    public ItemStack func_75209_a(int p_75209_1_) {
        if (this.listener != null) {
            this.listener.func_75130_a(this.field_75224_c);
        }

        return super.func_75209_a(p_75209_1_);
    }
}
