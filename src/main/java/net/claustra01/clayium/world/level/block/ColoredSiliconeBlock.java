/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Grayscale silicone block tinted with the selected vanilla dye color. */
public final class ColoredSiliconeBlock extends Block {
    private final DyeColor color;

    public ColoredSiliconeBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    public DyeColor color() {
        return color;
    }
}
