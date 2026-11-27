package mods.clayium.plugin;

import java.lang.reflect.Method;
import mods.clayium.core.ClayiumCore;
import net.minecraft.item.ItemStack;

public class UtilGT {
    private static boolean gtLoaded = true;
    private static Method gtuAddToBlacklist = null;

    public UtilGT() {
        super();
    }

    public static void addItemToBlackListGTUnification(ItemStack item) {
        if (gtLoaded && ClayiumCore.IntegrationID.GT.loaded()) {
            try {
                if (gtuAddToBlacklist == null) {
                    Class clazz = null;
                    clazz = Class.forName("gregtech.api.util.GT_OreDictUnificator");
                    if (clazz != null) {
                        gtuAddToBlacklist = clazz.getMethod("addToBlacklist", ItemStack.class);
                    }
                }

                if (gtuAddToBlacklist != null) {
                    gtuAddToBlacklist.invoke(null, item);
                }
            } catch (Exception var2) {
                ClayiumCore.logger.info("Failed to add item to black list of GT Unificator.");
                ClayiumCore.logger.catching(var2);
                gtLoaded = false;
            }
        }
    }
}
