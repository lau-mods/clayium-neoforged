/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block;

import java.util.List;
import net.claustra01.clayium.storage.MetalStorageCatalog;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public final class DecorativeMetalBlock extends Block {
    private final MetalStorageCatalog.Metal definition;

    public DecorativeMetalBlock(Properties properties, MetalStorageCatalog.Metal definition) {
        super(properties);
        this.definition = definition;
    }

    public MetalStorageCatalog.Metal definition() { return definition; }

    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
