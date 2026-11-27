package mods.clayium.gui.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.tile.TileClayWorkTable;
import mods.clayium.util.crafting.ClayWorkTableRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerClayWorkTable extends Container {
    private TileClayWorkTable tileClayWorkTable;
    private int lastCookTime;
    private int lastBurnTime;
    private int lastItemBurnTime;
    private int lastCookingMethod;
    private int lastTimeToCook;
    private static int slotNum = 4;
    int xCoord;
    int yCoord;
    int zCorrd;

    public ContainerClayWorkTable(int x, int y, int z) {
        super();
        this.xCoord = x;
        this.yCoord = y;
        this.zCorrd = z;
    }

    public ContainerClayWorkTable(InventoryPlayer player, TileClayWorkTable tileEntityClayWorkTable) {
        super();
        this.tileClayWorkTable = tileEntityClayWorkTable;
        this.func_75146_a(new Slot(tileEntityClayWorkTable, 0, 17, 30));
        this.func_75146_a(new Slot(tileEntityClayWorkTable, 1, 80, 17));
        this.func_75146_a(new SlotFurnace(player.field_70458_d, tileEntityClayWorkTable, 2, 143, 30));
        this.func_75146_a(new SlotFurnace(player.field_70458_d, tileEntityClayWorkTable, 3, 143, 55));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.func_75146_a(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int var5 = 0; var5 < 9; var5++) {
            this.func_75146_a(new Slot(player, var5, 8 + var5 * 18, 142));
        }
    }

    public void func_75132_a(ICrafting craft) {
        super.func_75132_a(craft);
        craft.func_71112_a(this, 0, this.tileClayWorkTable.furnaceCookTime);
        craft.func_71112_a(this, 1, this.tileClayWorkTable.furnaceBurnTime);
        craft.func_71112_a(this, 3, this.tileClayWorkTable.furnaceBurnTime);
        craft.func_71112_a(this, 4, this.tileClayWorkTable.furnaceTimeToCook);
    }

    public void func_75142_b() {
        super.func_75142_b();

        for (int i = 0; i < this.field_75149_d.size(); i++) {
            ICrafting craft = (ICrafting)this.field_75149_d.get(i);
            if (this.lastCookTime != this.tileClayWorkTable.furnaceCookTime) {
                craft.func_71112_a(this, 0, this.tileClayWorkTable.furnaceCookTime);
            }

            if (this.lastBurnTime != this.tileClayWorkTable.furnaceBurnTime) {
                craft.func_71112_a(this, 1, this.tileClayWorkTable.furnaceBurnTime);
            }

            if (this.lastBurnTime != this.tileClayWorkTable.furnaceBurnTime) {
                craft.func_71112_a(this, 3, this.tileClayWorkTable.furnaceCookingMethod);
            }

            if (this.lastBurnTime != this.tileClayWorkTable.furnaceBurnTime) {
                craft.func_71112_a(this, 4, this.tileClayWorkTable.furnaceTimeToCook);
            }
        }

        this.lastBurnTime = this.tileClayWorkTable.furnaceBurnTime;
        this.lastCookTime = this.tileClayWorkTable.furnaceCookTime;
        this.lastCookingMethod = this.tileClayWorkTable.furnaceCookingMethod;
        this.lastTimeToCook = this.tileClayWorkTable.furnaceTimeToCook;
    }

    @SideOnly(Side.CLIENT)
    public void func_75137_b(int par1, int par2) {
        if (par1 == 0) {
            this.tileClayWorkTable.furnaceCookTime = par2;
        }

        if (par1 == 1) {
            this.tileClayWorkTable.furnaceBurnTime = par2;
        }

        if (par1 == 2) {
        }

        if (par1 == 3) {
            this.tileClayWorkTable.furnaceCookingMethod = par2;
        }

        if (par1 == 4) {
            this.tileClayWorkTable.furnaceTimeToCook = par2;
        }
    }

    public boolean func_75145_c(EntityPlayer player) {
        return this.tileClayWorkTable.func_70300_a(player);
    }

    public ItemStack func_82846_b(EntityPlayer player, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.field_75151_b.get(par2);
        if (slot != null && slot.func_75216_d()) {
            ItemStack itemstack1 = slot.func_75211_c();
            itemstack = itemstack1.func_77946_l();
            if (par2 == 2 || par2 == 3) {
                if (!this.func_75135_a(itemstack1, slotNum, slotNum + 36, true)) {
                    return null;
                }

                slot.func_75220_a(itemstack1, itemstack);
            } else if (par2 >= slotNum) {
                if (ClayWorkTableRecipes.smelting().hasKneadingResult(itemstack1)) {
                    if (!this.func_75135_a(itemstack1, 0, 1, false)) {
                        return null;
                    }
                } else if (TileClayWorkTable.isItemTool(itemstack1)) {
                    if (!this.func_75135_a(itemstack1, 1, 2, false)) {
                        return null;
                    }
                } else if (par2 >= slotNum && par2 < slotNum + 27) {
                    if (!this.func_75135_a(itemstack1, slotNum + 27, slotNum + 36, false)) {
                        return null;
                    }
                } else if (par2 >= slotNum + 27 && par2 < slotNum + 36 && !this.func_75135_a(itemstack1, slotNum, slotNum + 27, false)) {
                    return null;
                }
            } else if (!this.func_75135_a(itemstack1, slotNum, slotNum + 36, false)) {
                return null;
            }

            if (itemstack1.field_77994_a == 0) {
                slot.func_75215_d((ItemStack)null);
            } else {
                slot.func_75218_e();
            }

            if (itemstack1.field_77994_a == itemstack.field_77994_a) {
                return null;
            }

            slot.func_82870_a(player, itemstack1);
        }

        return itemstack;
    }

    public boolean func_75140_a(EntityPlayer player, int action) {
        this.tileClayWorkTable.pushButton(action);
        return true;
    }
}
