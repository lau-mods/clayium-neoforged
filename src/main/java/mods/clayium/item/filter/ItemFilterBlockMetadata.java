package mods.clayium.item.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import mods.clayium.core.ClayiumCore;
import net.minecraft.world.World;

public class ItemFilterBlockMetadata extends ItemFilterItemDamage {
    public ItemFilterBlockMetadata() {
        super();
    }

    public boolean shouldApplySpecialPatternForBlock(String filterString, World world, int x, int y, int z) {
        return true;
    }

    public boolean filterStringMatch(String filterString, World world, int x, int y, int z) {
        String filter = "^" + filterString + "$";
        if (world == null) {
            return false;
        } else {
            String name = String.valueOf(world.func_72805_g(x, y, z));

            try {
                Pattern pattern = Pattern.compile(filter);
                Matcher matcher = pattern.matcher(name);
                return matcher.find();
            } catch (PatternSyntaxException var10) {
                ClayiumCore.logger.error("Illegal Pattern! \n" + var10.getMessage());
                return false;
            }
        }
    }
}
