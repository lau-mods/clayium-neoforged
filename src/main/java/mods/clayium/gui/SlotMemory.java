package mods.clayium.gui;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.item.filter.IItemFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

public class SlotMemory extends SlotWithTexture {
    public SlotMemory(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.setRestricted();
    }

    public SlotMemory(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ITexture texture) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_, texture);
        this.setRestricted();
    }

    public SlotMemory(Container listener, IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        super(listener, p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.setRestricted();
    }

    public SlotMemory(Container listener, IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ITexture texture) {
        super(listener, p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_, texture);
        this.setRestricted();
    }

    public boolean func_82869_a(EntityPlayer player) {
        boolean flag = false;
        ItemStack playerstack = player.field_71071_by.func_70445_o();
        ItemStack slotstack = this.func_75211_c();
        if (playerstack != null && playerstack.func_77973_b() instanceof IItemFilter && slotstack != null && slotstack.func_77973_b() instanceof IItemFilter) {
            IItemFilter playerfilter = (IItemFilter)playerstack.func_77973_b();
            if (playerfilter.isCopy(playerstack)) {
                playerstack = slotstack.func_77946_l();
                playerfilter = (IItemFilter)playerstack.func_77973_b();
                playerstack = playerfilter.setCopyFlag(playerstack);
                if (!player.field_70170_p.field_72995_K) {
                    player.func_145747_a(new ChatComponentText("Copied " + slotstack.func_82833_r()));
                    List<String> list = new ArrayList<>();
                    ((IItemFilter)slotstack.func_77973_b()).addFilterInformation(slotstack, player, list, true);

                    for (String s : list) {
                        player.func_145747_a(new ChatComponentText(" " + s));
                    }
                }

                player.field_71071_by.func_70437_b(playerstack);
                flag = true;
            }
        }

        if (!flag) {
            this.func_75215_d(null);
        }

        return false;
    }

    public boolean func_75214_a(ItemStack itemstack) {
        return super.func_75214_a(itemstack);
    }

    public ItemStack func_75211_c() {
        return super.func_75211_c() == null ? null : super.func_75211_c().func_77946_l();
    }
}
