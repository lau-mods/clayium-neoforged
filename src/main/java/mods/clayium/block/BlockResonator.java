package mods.clayium.block;

import java.util.List;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class BlockResonator extends BlockDamaged implements ICAResonator {
    public BlockResonator(Material material) {
        super(material);
    }

    public BlockResonator() {
        this(Material.field_151576_e);
    }

    public BlockDamaged addResonance(double resonance) {
        return this.putInfo("Resonance", new Double(resonance));
    }

    public double getResonance(String blockname) {
        Object obj = this.getInfo(blockname, "Resonance");
        return obj instanceof Double ? (Double)obj : 1.0;
    }

    public double getResonance(IBlockAccess world, int x, int y, int z) {
        return this.getResonance(this.getBlockName(world, x, y, z));
    }

    public List getTooltip(ItemStack itemStack) {
        List ret = UtilLocale.localizeTooltip("tooltip.Resonator");
        ret.addAll(super.getTooltip(itemStack));
        if (UtilLocale.canLocalize("tooltip.Resonator.resonance")) {
            ret.add(UtilLocale.localizeAndFormat("tooltip.Resonator.resonance", this.getResonance(this.getBlockName(itemStack.func_77960_j()))));
        }

        return ret;
    }
}
