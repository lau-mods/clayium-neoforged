package net.claustra01.clayium.registry;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class FoundationBlockRegistration {
    public static final DeferredBlock<Block> DENSE_CLAY = registerClayBlock("dense_clay", 2.5F, 4.0F);
    public static final DeferredBlock<Block> COMPRESSED_CLAY = registerClayBlock("compressed_clay", 3.5F, 5.0F);
    public static final DeferredBlock<Block> CLAY_ORE = ClayiumRegistries.BLOCKS.registerSimpleBlock(
            "clay_ore", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));
    public static final DeferredBlock<Block> CLAYIUM_BLOCK = ClayiumRegistries.BLOCKS.registerSimpleBlock(
            "clayium_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(6.0F, 8.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> CLAY_BENDING_MACHINE = ClayiumRegistries.BLOCKS.registerSimpleBlock(
            "clay_bending_machine",
            BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(3.0F, 5.0F).sound(SoundType.STONE));

    public static final List<DeferredBlock<? extends Block>> ALL = List.of(
            DENSE_CLAY, COMPRESSED_CLAY, CLAY_ORE, CLAYIUM_BLOCK, CLAY_BENDING_MACHINE);

    private static DeferredBlock<Block> registerClayBlock(String id, float hardness, float resistance) {
        return ClayiumRegistries.BLOCKS.registerSimpleBlock(
                id,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.CLAY)
                        .strength(hardness, resistance)
                        .sound(SoundType.STONE));
    }

    private FoundationBlockRegistration() {
    }
}
