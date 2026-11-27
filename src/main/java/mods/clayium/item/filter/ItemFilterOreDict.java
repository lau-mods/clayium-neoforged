package mods.clayium.item.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilItemStack;
import net.minecraft.item.ItemStack;

public class ItemFilterOreDict extends ItemFilterString {
    public ItemFilterOreDict() {
        super();
    }

    public boolean filterStringMatch(String filterString, ItemStack itemstack) {
        String filter = filterString;
        String[] orenames = UtilItemStack.getOreNames(itemstack);

        for (String orename : orenames) {
            try {
                Pattern pattern = Pattern.compile(filter);
                Matcher matcher = pattern.matcher(orename);
                if (matcher.find()) {
                    return true;
                }
            } catch (PatternSyntaxException var11) {
                ClayiumCore.logger.error("Illegal Pattern! \n" + var11.getMessage());
                return false;
            }
        }

        return false;
    }
}
