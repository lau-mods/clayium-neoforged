package net.claustra01.clayium.client;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.item.ClayMaterialItem;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client-only block and item color handlers. Never loaded on dedicated servers.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClayiumClientEvents {
    private static final int WHITE = 0xFFFFFFFF;

    private ClayiumClientEvents() {}

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (var holder : ClayiumBlocks.all().values()) {
            Block block = holder.get();
            if (block instanceof MetalMaterialBlock metalBlock && metalBlock.isLayeredTint()) {
                event.register(
                        (state, level, pos, tintIndex) -> metalBlock.getMaterial().opaqueArgbForLayer(tintIndex),
                        metalBlock
                );
            } else if (block instanceof MetalChestBlock chestBlock) {
                event.register(
                        (state, level, pos, tintIndex) -> chestBlock.getMaterial().opaqueArgbForLayer(tintIndex),
                        chestBlock
                );
            } else if (block instanceof SiliconeColoredBlock silicone) {
                event.register(
                        (state, level, pos, tintIndex) -> tintIndex == 0 ? silicone.getOpaqueTintArgb() : WHITE,
                        silicone
                );
            }
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (var holder : ClayiumItems.standaloneItems().values()) {
            if (holder.get() instanceof ClayMaterialItem matItem && matItem.isLayered()) {
                event.register(
                        (stack, tintIndex) -> matItem.getMaterial().opaqueArgbForLayer(tintIndex),
                        matItem
                );
            }
        }
        for (var holder : ClayiumItems.blockItems().values()) {
            if (holder.get() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block instanceof MetalMaterialBlock metalBlock && metalBlock.isLayeredTint()) {
                    event.register(
                            (stack, tintIndex) -> metalBlock.getMaterial().opaqueArgbForLayer(tintIndex),
                            blockItem
                    );
                } else if (block instanceof MetalChestBlock chestBlock) {
                    event.register(
                            (stack, tintIndex) -> chestBlock.getMaterial().opaqueArgbForLayer(tintIndex),
                            blockItem
                    );
                } else if (block instanceof SiliconeColoredBlock silicone) {
                    event.register(
                            (stack, tintIndex) -> tintIndex == 0 ? silicone.getOpaqueTintArgb() : WHITE,
                            blockItem
                    );
                }
            }
        }
    }
}
