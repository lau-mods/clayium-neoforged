package net.claustra01.clayium.registry;

import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.logistics.creative.CreativeEnergySourceBlock;
import net.claustra01.clayium.logistics.creative.CreativeEnergySourceBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CreativeSourceRegistration {
    public static final String ID = "creative_ce_source";

    public static final DeferredBlock<CreativeEnergySourceBlock> CREATIVE_ENERGY_SOURCE =
            ClayiumRegistries.BLOCKS.register(
                    ID,
                    () -> new CreativeEnergySourceBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(6.25F, 18.0F)
                                    .sound(SoundType.STONE)));
    public static final DeferredItem<FoundationBlockItem> CREATIVE_ENERGY_SOURCE_ITEM =
            ClayiumRegistries.ITEMS.register(
                    ID,
                    () -> new FoundationBlockItem(
                            CREATIVE_ENERGY_SOURCE.get(),
                            new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), 13)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeEnergySourceBlockEntity>>
            CREATIVE_ENERGY_SOURCE_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    ID,
                    () -> BlockEntityType.Builder.of(
                                    CreativeEnergySourceBlockEntity::new, CREATIVE_ENERGY_SOURCE.get())
                            .build(null));

    private CreativeSourceRegistration() {
    }
}
