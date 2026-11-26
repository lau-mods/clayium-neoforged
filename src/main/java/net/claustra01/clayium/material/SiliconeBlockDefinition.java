package net.claustra01.clayium.material;

import java.util.List;
import net.minecraft.world.item.DyeColor;

public record SiliconeBlockDefinition(String id, DyeColor color) {
    public static final List<SiliconeBlockDefinition> ORIGINAL_COLORS = List.of(
            color(DyeColor.WHITE),
            color(DyeColor.ORANGE),
            color(DyeColor.MAGENTA),
            color(DyeColor.LIGHT_BLUE),
            color(DyeColor.YELLOW),
            color(DyeColor.LIME),
            color(DyeColor.PINK),
            color(DyeColor.GRAY),
            color(DyeColor.LIGHT_GRAY),
            color(DyeColor.CYAN),
            color(DyeColor.PURPLE),
            color(DyeColor.BLUE),
            color(DyeColor.BROWN),
            color(DyeColor.GREEN),
            color(DyeColor.RED),
            color(DyeColor.BLACK));

    private static SiliconeBlockDefinition color(DyeColor color) {
        return new SiliconeBlockDefinition(color.getName() + "_silicone_block", color);
    }
}
