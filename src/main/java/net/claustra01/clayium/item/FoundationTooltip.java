package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

final class FoundationTooltip {
    static void add(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        Integer tier = stack.get(FoundationDataComponentRegistration.TIER.get());
        if (tier != null) {
            tooltip.add(Component.translatable("tooltip.clayium.tier", tier));
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int lines = path.equals("gadget_holder") || path.equals("clay_pickaxe") ? 2 :
                path.equals("clay_bending_machine") || path.equals("clay_wrench") ? 1 : 0;
        for (int line = 1; line <= lines; line++) {
            tooltip.add(Component.translatable("tooltip." + ClayiumMod.MOD_ID + "." + path + ".line" + line));
        }
        if (!flag.isAdvanced()) {
            tooltip.add(Component.literal(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private FoundationTooltip() {
    }
}
