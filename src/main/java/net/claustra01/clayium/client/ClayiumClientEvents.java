package net.claustra01.clayium.client;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.client.renderer.CAReactorRenderer;
import net.claustra01.clayium.client.renderer.ClayContainerRenderer;
import net.claustra01.clayium.client.renderer.MetalChestRenderer;
import net.claustra01.clayium.client.renderer.StorageContainerRenderer;
import net.claustra01.clayium.client.renderer.SynchronizedInterfaceRenderer;
import net.claustra01.clayium.client.screen.AutoTraderScreen;
import net.claustra01.clayium.client.screen.ClayCraftingTableScreen;
import net.claustra01.clayium.client.screen.ClayWorkTableScreen;
import net.claustra01.clayium.client.screen.ItemFilterStringScreen;
import net.claustra01.clayium.client.screen.MultipageScreen;
import net.claustra01.clayium.client.screen.ProcessingMachineScreen;
import net.claustra01.clayium.client.screen.SimpleContainerScreen;
import net.claustra01.clayium.client.screen.StorageContainerScreen;
import net.claustra01.clayium.client.screen.WaterWheelScreen;
import net.claustra01.clayium.item.ClayMaterialItem;
import net.claustra01.clayium.item.FluidCapsuleItem;
import net.claustra01.clayium.menu.AutoClayCondenserMenu;
import net.claustra01.clayium.menu.AutoCrafterMenu;
import net.claustra01.clayium.menu.DistributorMenu;
import net.claustra01.clayium.menu.EnergyOnlyMenu;
import net.claustra01.clayium.menu.GadgetHolderMenu;
import net.claustra01.clayium.menu.ItemFilterWhitelistMenu;
import net.claustra01.clayium.menu.MultitrackBufferMenu;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
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

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ClayiumMenus.CLAY_WORK_TABLE.get(), ClayWorkTableScreen::new);
        event.register(ClayiumMenus.PROCESSING_MACHINE.get(), ProcessingMachineScreen::new);
        event.register(ClayiumMenus.AUTO_CLAY_CONDENSER.get(), SimpleContainerScreen<AutoClayCondenserMenu>::new);
        event.register(ClayiumMenus.AUTO_CRAFTER.get(), SimpleContainerScreen<AutoCrafterMenu>::new);
        event.register(ClayiumMenus.NORMAL_INVENTORY.get(), MultipageScreen::new);
        event.register(ClayiumMenus.MULTITRACK_BUFFER.get(), SimpleContainerScreen<MultitrackBufferMenu>::new);
        event.register(ClayiumMenus.DISTRIBUTOR.get(), SimpleContainerScreen<DistributorMenu>::new);
        event.register(ClayiumMenus.STORAGE_CONTAINER.get(), StorageContainerScreen::new);
        event.register(ClayiumMenus.WATER_WHEEL.get(), WaterWheelScreen::new);
        event.register(ClayiumMenus.ENERGY_ONLY.get(), SimpleContainerScreen<EnergyOnlyMenu>::new);
        event.register(ClayiumMenus.AUTO_TRADER.get(), AutoTraderScreen::new);
        event.register(ClayiumMenus.CLAY_CRAFTING_TABLE.get(), ClayCraftingTableScreen::new);
        event.register(ClayiumMenus.ITEM_FILTER_WHITELIST.get(), SimpleContainerScreen<ItemFilterWhitelistMenu>::new);
        event.register(ClayiumMenus.ITEM_FILTER_STRING.get(), ItemFilterStringScreen::new);
        event.register(ClayiumMenus.GADGET_HOLDER.get(), SimpleContainerScreen<GadgetHolderMenu>::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ClayiumBlockEntities.METAL_CHEST.get(), MetalChestRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.STORAGE_CONTAINER.get(), StorageContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.VACUUM_CONTAINER.get(), StorageContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CA_REACTOR.get(), CAReactorRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.REDSTONE_INTERFACE.get(), SynchronizedInterfaceRenderer::new);

        event.registerBlockEntityRenderer(ClayiumBlockEntities.BUFFER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.MULTITRACK_BUFFER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.FLUID_TRANSLATOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.DISTRIBUTOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CLAY_INTERFACE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.LASER_INTERFACE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.SIMPLE_MACHINE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.ASSEMBLER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CENTRIFUGE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CHEMICAL_REACTOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CHEMICAL_METAL_SEPARATOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CA_INJECTOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CA_CONDENSER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CA_COLLECTOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.BLAST_FURNACE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.REACTOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.AUTO_CLAY_CONDENSER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.SOLAR_CLAY_FABRICATOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CLAY_FABRICATOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.COBBLESTONE_GENERATOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.SALT_EXTRACTOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.WATER_WHEEL.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CREATIVE_CE_SOURCE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.AUTO_CRAFTER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.AUTO_TRADER.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.PAN_DUPLICATOR.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CLAY_CRAFTING_TABLE.get(), ClayContainerRenderer::new);
        event.registerBlockEntityRenderer(ClayiumBlockEntities.CLAY_CHUNK_LOADER.get(), ClayContainerRenderer::new);
    }
}
