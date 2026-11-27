package net.claustra01.clayium.world;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/**
 * Registers NeoForge chunk-loading {@link TicketController} for Clay Chunk Loader blocks.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClayiumChunkLoading {
    public static final TicketController CONTROLLER = new TicketController(ClayiumMod.id("chunk_loader"), (level, ticketHelper) -> {
        for (var entry : ticketHelper.getBlockTickets().entrySet()) {
            BlockPos pos = entry.getKey();
            if (!(level.getBlockEntity(pos) instanceof net.claustra01.clayium.blockentity.ClayChunkLoaderBlockEntity)) {
                ticketHelper.removeAllTickets(pos);
            }
        }
    });

    private ClayiumChunkLoading() {}

    @SubscribeEvent
    public static void onRegisterTicketControllers(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    public static void forceSquare3x3(ServerLevel level, BlockPos ownerPos, boolean add) {
        int cx = ownerPos.getX() >> 4;
        int cz = ownerPos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                CONTROLLER.forceChunk(level, ownerPos, cx + dx, cz + dz, add, true);
            }
        }
    }
}
