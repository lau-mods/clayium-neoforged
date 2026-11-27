package mods.clayium.gui.container;

import mods.clayium.block.tile.TilePANCore;
import mods.clayium.core.ClayiumCore;
import mods.clayium.network.PANCoreListPacket;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;

public class ContainerPANCore extends ContainerTemp {
    public ContainerPANCore(InventoryPlayer player, TilePANCore tile, Block block) {
        super(player, tile, block);
    }

    protected void initParameters(InventoryPlayer player) {
        this.machineGuiSizeY += 72;
        super.initParameters(player);
    }

    public void setMachineInventorySlots(InventoryPlayer player) {
    }

    public boolean canTransferToMachineInventory(ItemStack itemstack1) {
        return false;
    }

    public boolean transferStackToMachineInventory(ItemStack itemstack1) {
        return false;
    }

    public boolean transferStackFromMachineInventory(ItemStack itemstack1, int slot) {
        return false;
    }

    public void func_75132_a(ICrafting p_75132_1_) {
        super.func_75132_a(p_75132_1_);
        if (p_75132_1_ instanceof EntityPlayerMP) {
            ClayiumCore.packetDispatcher.sendTo(new PANCoreListPacket(this.field_75152_c, (TilePANCore)this.tile), (EntityPlayerMP)p_75132_1_);
        }
    }
}
