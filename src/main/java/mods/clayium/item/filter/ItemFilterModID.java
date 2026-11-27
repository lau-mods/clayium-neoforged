package mods.clayium.item.filter;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import mods.clayium.core.ClayiumCore;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemFilterModID extends ItemFilterString {
    public ItemFilterModID() {
        super();
    }

    public boolean filterStringMatch(String filterString, ItemStack itemstack) {
        String filter = filterString;
        if (itemstack != null && itemstack.func_77973_b() != null) {
            String name;
            if (itemstack.func_77973_b() instanceof ItemBlock) {
                name = GameRegistry.findUniqueIdentifierFor(((ItemBlock)itemstack.func_77973_b()).field_150939_a).modId;
            } else {
                name = GameRegistry.findUniqueIdentifierFor(itemstack.func_77973_b()).modId;
            }

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
