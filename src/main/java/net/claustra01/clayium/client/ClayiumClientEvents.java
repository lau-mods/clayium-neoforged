package net.claustra01.clayium.client;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.item.ClayMaterialItem;
import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Client-only block/item color handlers and ItemProperties predicates. Never loaded on dedicated servers.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClayiumClientEvents {
    private static final int WHITE = 0xFFFFFFFF;

    private ClayiumClientEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ClayiumItems.byPath("storage_container").get(),
                ClayiumMod.id("upgraded"),
                (stack, level, entity, seed) -> StorageContainerBlock.isUpgradedStack(stack) ? 1.0F : 0.0F
        ));
    }

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
            } else if (holder.get() instanceof FluidCapsuleItem capsule) {
                event.register(
                        (stack, tintIndex) -> {
                            if (tintIndex != 0) {
                                return WHITE;
                            }
                            SimpleFluidContent content = stack.get(ClayiumDataComponents.FLUID_CONTENT.get());
                            if (content == null || content.isEmpty()) {
                                return WHITE;
                            }
                            FluidStack fluidStack = content.copy();
                            int color = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
                            return FastColor.ARGB32.opaque(color);
                        },
                        capsule
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
