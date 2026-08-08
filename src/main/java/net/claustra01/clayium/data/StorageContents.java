/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** Portable single-type Storage Container payload. */
public record StorageContents(ItemStack item, long count) {
    public static final Codec<StorageContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(StorageContents::item),
            Codec.LONG.fieldOf("count").forGetter(StorageContents::count)
    ).apply(instance, StorageContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, StorageContents::item,
            ByteBufCodecs.VAR_LONG, StorageContents::count,
            StorageContents::new);

    public StorageContents {
        item = item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1);
        count = item.isEmpty() ? 0 : Math.max(0, count);
    }
}
