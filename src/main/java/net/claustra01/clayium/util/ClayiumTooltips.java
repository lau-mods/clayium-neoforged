package net.claustra01.clayium.util;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.TieredBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * Shared server/client-safe tooltip localization matching original UtilLocale.localizeTooltip.
 */
public final class ClayiumTooltips {
    private static final int MAX_LINE_TOOLTIP = 12;

    private ClayiumTooltips() {}

    public static void appendLocalizedTooltip(
            List<Component> tooltip,
            @Nullable String prefix,
            TooltipFlag flag) {
        appendLocalizedTooltip(tooltip, prefix, flag.hasShiftDown(), Language.getInstance());
    }

    public static void appendLocalizedTooltip(
            List<Component> tooltip,
            @Nullable String prefix,
            boolean shiftDown,
            Language lang) {
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        for (int line = 1; line <= MAX_LINE_TOOLTIP; line++) {
            String key = prefix + ".line" + line;
            if (!lang.has(key)) {
                break;
            }
            String value = lang.getOrDefault(key);
            if ("__DETAIL__".equals(value)) {
                if (!shiftDown) {
                    tooltip.add(Component.translatable("tooltip.HoldShiftForDetails").withStyle(ChatFormatting.GRAY));
                    break;
                }
                continue;
            }
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }

    public static String resolveBlockTooltipPrefix(Block block, String descriptionId) {
        if (block instanceof ClayContainerBlock cc && cc.getTooltipKey() != null) {
            return cc.getTooltipKey();
        }
        if (block instanceof TieredBlock tb && tb.getTooltipKey() != null) {
            return tb.getTooltipKey();
        }
        return descriptionId + ".tooltip";
    }

    public static Component formatLocalizedLine(String key, Object... args) {
        String pattern = Language.getInstance().getOrDefault(key);
        return Component.literal(String.format(java.util.Locale.ROOT, pattern, args)).withStyle(ChatFormatting.GRAY);
    }

    public static String blockPrefix(String path) {
        return "block." + ClayiumMod.MOD_ID + "." + path + ".tooltip";
    }
}
