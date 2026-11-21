package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoilBlock;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlock;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.claustra01.clayium.multiblock.careactor.CaReactorHullBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CaReactorRegistration {
    private static final String[] MARKS = {"i", "ii", "iii", "iv"};
    private static final String[] COIL_TEXTURES = {
            "antimatter", "pureantimatter", "oec", "opa"
    };

    public static final Map<Integer, CoreDefinition> CORES;
    public static final Map<Integer, CoilDefinition> COILS;
    public static final Map<Integer, HullDefinition> HULLS;
    public static final List<DeferredBlock<? extends Block>> BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<Integer, CoreDefinition> cores = new LinkedHashMap<>();
        Map<Integer, CoilDefinition> coils = new LinkedHashMap<>();
        Map<Integer, HullDefinition> hulls = new LinkedHashMap<>();
        List<DeferredBlock<? extends Block>> blocks = new ArrayList<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (int index = 0; index < MARKS.length; index++) {
            int tier = index + 10;
            String coreId = "ca_reactor_core_mk_" + MARKS[index];
            DeferredBlock<CaReactorCoreBlock> core = ClayiumRegistries.BLOCKS.register(
                    coreId, () -> new CaReactorCoreBlock(properties(tier), tier));
            DeferredItem<FoundationBlockItem> coreItem = blockItem(coreId, core, tier);
            cores.put(tier, new CoreDefinition(coreId, tier, core, coreItem));
            blocks.add(core);
            items.add(coreItem);

            String coilId = "ca_reactor_coil_mk_" + MARKS[index];
            DeferredBlock<CaReactorCoilBlock> coil = ClayiumRegistries.BLOCKS.register(
                    coilId, () -> new CaReactorCoilBlock(properties(tier), tier));
            DeferredItem<FoundationBlockItem> coilItem = blockItem(coilId, coil, tier);
            coils.put(tier, new CoilDefinition(coilId, tier, COIL_TEXTURES[index], coil, coilItem));
            blocks.add(coil);
            items.add(coilItem);
        }
        for (int rank = 1; rank <= 10; rank++) {
            int hullRank = rank;
            String id = "ca_reactor_hull_rank_" + rank;
            DeferredBlock<CaReactorHullBlock> hull = ClayiumRegistries.BLOCKS.register(
                    id, () -> new CaReactorHullBlock(properties(Math.min(9 + hullRank, 13)), hullRank));
            DeferredItem<FoundationBlockItem> hullItem = blockItem(id, hull, Math.min(9 + rank, 13));
            hulls.put(rank, new HullDefinition(id, rank, hull, hullItem));
            blocks.add(hull);
            items.add(hullItem);
        }
        CORES = Collections.unmodifiableMap(cores);
        COILS = Collections.unmodifiableMap(coils);
        HULLS = Collections.unmodifiableMap(hulls);
        BLOCKS = List.copyOf(blocks);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CaReactorCoreBlockEntity>>
            CORE_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "ca_reactor_core",
                    () -> BlockEntityType.Builder.of(
                            CaReactorCoreBlockEntity::new,
                            CORES.values().stream().map(definition -> definition.block().get())
                                    .toArray(Block[]::new)).build(null));

    private static DeferredItem<FoundationBlockItem> blockItem(
            String id, DeferredBlock<? extends Block> block, int tier) {
        return ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
    }

    private static BlockBehaviour.Properties properties(int tier) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F + tier * 0.25F, 5.0F + tier)
                .sound(SoundType.METAL);
    }

    public record CoreDefinition(
            String id, int tier, DeferredBlock<CaReactorCoreBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    public record CoilDefinition(
            String id, int tier, String texture, DeferredBlock<CaReactorCoilBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    public record HullDefinition(
            String id, int rank, DeferredBlock<CaReactorHullBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private CaReactorRegistration() {
    }
}
