/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.item;

import java.util.List;
import java.util.Locale;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.data.StorageContents;
import net.claustra01.clayium.world.level.block.entity.LogisticsBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class StorageContainerBlockItem extends BlockItem {
    public StorageContainerBlockItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        StorageContents contents = stack.get(ClayiumDataComponents.STORAGE_CONTENTS.get());
        return contents != null && contents.count() > 0 ? 1 : super.getMaxStackSize(stack);
    }

    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                          List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        long capacity = stack.getOrDefault(ClayiumDataComponents.STORAGE_CAPACITY.get(),
                LogisticsBlockEntity.DEFAULT_STORAGE_CAPACITY);
        tooltip.add(Component.translatable("tooltip.clayium_neoforged.storage_capacity",
                String.format(Locale.ROOT, "%,d", capacity)));
        StorageContents contents = stack.get(ClayiumDataComponents.STORAGE_CONTENTS.get());
        if (contents != null && contents.count() > 0) {
            tooltip.add(Component.translatable("tooltip.clayium_neoforged.storage_contents",
                    contents.item().getHoverName(), String.format(Locale.ROOT, "%,d", contents.count())));
        }
    }
}
