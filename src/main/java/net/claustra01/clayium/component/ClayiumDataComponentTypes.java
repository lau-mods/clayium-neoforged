package net.claustra01.clayium.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Serializable DataComponent payload records used across Clayium items and blocks.
 */
public final class ClayiumDataComponentTypes {
    private ClayiumDataComponentTypes() {}

    public record DirectionMemoryData(
            double startX,
            double startY,
            double startZ,
            double hitX,
            double hitY,
            double hitZ,
            Direction side) {

        public static final Codec<DirectionMemoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("start_x").forGetter(DirectionMemoryData::startX),
                Codec.DOUBLE.fieldOf("start_y").forGetter(DirectionMemoryData::startY),
                Codec.DOUBLE.fieldOf("start_z").forGetter(DirectionMemoryData::startZ),
                Codec.DOUBLE.fieldOf("hit_x").forGetter(DirectionMemoryData::hitX),
                Codec.DOUBLE.fieldOf("hit_y").forGetter(DirectionMemoryData::hitY),
                Codec.DOUBLE.fieldOf("hit_z").forGetter(DirectionMemoryData::hitZ),
                Direction.CODEC.fieldOf("side").forGetter(DirectionMemoryData::side)
        ).apply(instance, DirectionMemoryData::new));

        public static final StreamCodec<ByteBuf, DirectionMemoryData> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public DirectionMemoryData decode(ByteBuf buf) {
                return new DirectionMemoryData(
                        ByteBufCodecs.DOUBLE.decode(buf),
                        ByteBufCodecs.DOUBLE.decode(buf),
                        ByteBufCodecs.DOUBLE.decode(buf),
                        ByteBufCodecs.DOUBLE.decode(buf),
                        ByteBufCodecs.DOUBLE.decode(buf),
                        ByteBufCodecs.DOUBLE.decode(buf),
                        Direction.STREAM_CODEC.decode(buf)
                );
            }

            @Override
            public void encode(ByteBuf buf, DirectionMemoryData value) {
                ByteBufCodecs.DOUBLE.encode(buf, value.startX());
                ByteBufCodecs.DOUBLE.encode(buf, value.startY());
                ByteBufCodecs.DOUBLE.encode(buf, value.startZ());
                ByteBufCodecs.DOUBLE.encode(buf, value.hitX());
                ByteBufCodecs.DOUBLE.encode(buf, value.hitY());
                ByteBufCodecs.DOUBLE.encode(buf, value.hitZ());
                Direction.STREAM_CODEC.encode(buf, value.side());
            }
        };
    }

    public record FilterConfigData(
            String pattern,
            boolean regex,
            ItemContainerContents slots) {

        public static final Codec<FilterConfigData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("pattern", "").forGetter(FilterConfigData::pattern),
                Codec.BOOL.optionalFieldOf("regex", false).forGetter(FilterConfigData::regex),
                ItemContainerContents.CODEC.optionalFieldOf("slots", ItemContainerContents.EMPTY).forGetter(FilterConfigData::slots)
        ).apply(instance, FilterConfigData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FilterConfigData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, FilterConfigData::pattern,
                ByteBufCodecs.BOOL, FilterConfigData::regex,
                ItemContainerContents.STREAM_CODEC, FilterConfigData::slots,
                FilterConfigData::new
        );
    }

    public record MarkerPosData(
            Optional<BlockPos> first,
            Optional<BlockPos> second) {

        public static final Codec<MarkerPosData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.optionalFieldOf("first").forGetter(MarkerPosData::first),
                BlockPos.CODEC.optionalFieldOf("second").forGetter(MarkerPosData::second)
        ).apply(instance, MarkerPosData::new));

        public static final StreamCodec<ByteBuf, MarkerPosData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC), MarkerPosData::first,
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC), MarkerPosData::second,
                MarkerPosData::new
        );
    }

    public record StorageContentData(
            Item item,
            long count,
            int maxStorage) {

        public static final int DEFAULT_MAX_STORAGE = 65536;

        public StorageContentData(Item item, long count) {
            this(item, count, DEFAULT_MAX_STORAGE);
        }

        public static final Codec<StorageContentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(StorageContentData::item),
                Codec.LONG.fieldOf("count").forGetter(StorageContentData::count),
                Codec.INT.optionalFieldOf("max_storage", DEFAULT_MAX_STORAGE).forGetter(StorageContentData::maxStorage)
        ).apply(instance, StorageContentData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, StorageContentData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM), StorageContentData::item,
                ByteBufCodecs.VAR_LONG, StorageContentData::count,
                ByteBufCodecs.VAR_INT, StorageContentData::maxStorage,
                StorageContentData::new
        );
    }
}
