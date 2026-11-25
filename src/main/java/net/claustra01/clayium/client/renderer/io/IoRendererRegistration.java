package net.claustra01.clayium.client.renderer.io;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class IoRendererRegistration {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MachineRegistration.MACHINE_BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(LogisticsRegistration.BUFFER_BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(FluidRegistration.BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(MultitrackRegistration.DISTRIBUTOR_BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(InterfaceRegistration.BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(EnvironmentMachineRegistration.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(AutoCrafterRegistration.BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(ClayProductionRegistration.BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(EnergyConverterRegistration.BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(PanRegistration.BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
        event.registerBlockEntityRenderer(StorageRegistration.VOID_CONTAINER_BLOCK_ENTITY.get(), context -> new IoDeviceRenderer<>());
    }

    private IoRendererRegistration() { }
}
