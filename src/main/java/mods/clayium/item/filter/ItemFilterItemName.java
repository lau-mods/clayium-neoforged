package mods.clayium.item.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import mods.clayium.core.ClayiumCore;
import net.minecraft.item.ItemStack;

public class ItemFilterItemName extends ItemFilterString {
    public ItemFilterItemName() {
        super();
    }

    public boolean filterStringMatch(String filterString, ItemStack itemstack) {
        String filter = filterString;
        if (itemstack != null && itemstack.func_77973_b() != null) {
            String name = itemstack.func_82833_r();

            try {
                Pattern pattern = Pattern.compile(filter);
                Matcher matcher = pattern.matcher(name);
                return matcher.find();
            } catch (PatternSyntaxException var7) {
                ClayiumCore.logger.error("Illegal Pattern! \n" + var7.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }
}
