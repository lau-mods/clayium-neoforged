package mods.clayium.gui;

import cpw.mods.fml.common.FMLCommonHandler;
import mods.clayium.block.tile.Inventories;
import mods.clayium.gui.container.ContainerClayCraftingTable;
import mods.clayium.util.UtilItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

public class SlotCCrafting extends SlotCrafting {
    private final IInventory[] inventories;
    public int[] starts;
    public int[] widths;
    public int[] heights;
    private EntityPlayer thePlayer;
    public ContainerClayCraftingTable listener;

    public SlotCCrafting(
        EntityPlayer p_i1823_1_,
        ContainerClayCraftingTable listener,
        IInventory[] p_i1823_2_,
        int[] starts,
        int[] widths,
        int[] heights,
        IInventory p_i1823_3_,
        int p_i1823_4_,
        int p_i1823_5_,
        int p_i1823_6_
    ) {
        super(p_i1823_1_, new Inventories(p_i1823_2_), p_i1823_3_, p_i1823_4_, p_i1823_5_, p_i1823_6_);
        this.thePlayer = p_i1823_1_;
        this.inventories = p_i1823_2_;
        this.starts = starts;
        this.widths = widths;
        this.heights = heights;
        this.listener = listener;
    }

    public boolean func_75214_a(ItemStack p_75214_1_) {
        return false;
    }

    public void func_82870_a(EntityPlayer p_82870_1_, ItemStack p_82870_2_) {
        IInventory craftMatrix = this.inventories[0];
        FMLCommonHandler.instance().firePlayerCraftingEvent(p_82870_1_, p_82870_2_, craftMatrix);
        if (!this.listener.world.field_72995_K) {
            this.func_75208_c(p_82870_2_);
        }

        for (int i = this.starts[0]; i < this.widths[0] * this.heights[0]; i++) {
            ItemStack itemstack1 = craftMatrix.func_70301_a(i);
            if (itemstack1 != null) {
                boolean flag = true;

                label59:
                for (int j = 1; j < this.inventories.length; j++) {
                    for (int k = this.starts[j]; k < this.widths[j] * this.heights[j]; k++) {
                        ItemStack itemstack_ = this.inventories[j].func_70301_a(k);
                        if (UtilItemStack.areItemDamageEqual(itemstack_, itemstack1)) {
                            flag = false;
                            this.inventories[j].func_70298_a(k, 1);
                            break label59;
                        }
                    }
                }

                if (flag) {
                    craftMatrix.func_70298_a(i, 1);
                }

                if (itemstack1.func_77973_b().hasContainerItem(itemstack1)) {
                    ItemStack itemstack2 = itemstack1.func_77973_b().getContainerItem(itemstack1);
                    if (itemstack2 != null && itemstack2.func_77984_f() && itemstack2.func_77960_j() > itemstack2.func_77958_k()) {
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this.thePlayer, itemstack2));
                    } else if (!itemstack1.func_77973_b().func_77630_h(itemstack1) || !this.thePlayer.field_71071_by.func_70441_a(itemstack2)) {
                        if (craftMatrix.func_70301_a(i) == null) {
                            craftMatrix.func_70299_a(i, itemstack2);
                        } else {
                            this.thePlayer.func_71019_a(itemstack2, false);
                        }
                    }
                }
            }
        }

        this.listener.func_75130_a(this.field_75224_c);
    }
}
