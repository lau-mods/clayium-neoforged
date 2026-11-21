package net.claustra01.clayium.world.chunkloading;

import java.util.HashSet;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketSet;

public final class ChunkLoaderTicketService {
    public static final int RADIUS = 1;
    public static final int CHUNK_COUNT = 9;
    public static final TicketController CONTROLLER =
            new TicketController(ClayiumMod.id("chunk_loader"), ChunkLoaderTicketService::validateTickets);

    public static void register(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    public static boolean acquire(ServerLevel level, BlockPos owner) {
        ChunkPos center = new ChunkPos(owner);
        for (int offsetX = -RADIUS; offsetX <= RADIUS; offsetX++) {
            for (int offsetZ = -RADIUS; offsetZ <= RADIUS; offsetZ++) {
                CONTROLLER.forceChunk(
                        level, owner, center.x + offsetX, center.z + offsetZ, true, true);
            }
        }
        if (!heldChunks(level, owner, true).containsAll(expectedChunks(owner))) {
            release(level, owner);
            return false;
        }
        return true;
    }

    public static void release(ServerLevel level, BlockPos owner) {
        ChunkPos center = new ChunkPos(owner);
        for (int offsetX = -RADIUS; offsetX <= RADIUS; offsetX++) {
            for (int offsetZ = -RADIUS; offsetZ <= RADIUS; offsetZ++) {
                CONTROLLER.forceChunk(
                        level, owner, center.x + offsetX, center.z + offsetZ, false, true);
            }
        }
    }

    public static Set<Long> expectedChunks(BlockPos owner) {
        ChunkPos center = new ChunkPos(owner);
        Set<Long> chunks = new HashSet<>(CHUNK_COUNT);
        for (int offsetX = -RADIUS; offsetX <= RADIUS; offsetX++) {
            for (int offsetZ = -RADIUS; offsetZ <= RADIUS; offsetZ++) {
                chunks.add(ChunkPos.asLong(center.x + offsetX, center.z + offsetZ));
            }
        }
        return Set.copyOf(chunks);
    }

    static boolean isValidTicketSet(ServerLevel level, BlockPos owner, TicketSet tickets) {
        return level.getBlockState(owner).is(ChunkLoaderRegistration.CHUNK_LOADER.get())
                && tickets.nonTicking().isEmpty()
                && tickets.ticking().size() == CHUNK_COUNT
                && expectedChunks(owner).stream().allMatch(tickets.ticking()::contains);
    }

    public static Set<Long> heldChunks(ServerLevel level, BlockPos owner, boolean ticking) {
        ForcedChunksSavedData savedData = level.getDataStorage()
                .computeIfAbsent(ForcedChunksSavedData.factory(), ForcedChunksSavedData.FILE_ID);
        CompoundTag root = savedData.save(new CompoundTag(), level.registryAccess());
        Set<Long> chunks = new HashSet<>();
        ListTag controllers = root.getList("ModForced", Tag.TAG_COMPOUND);
        for (int controllerIndex = 0; controllerIndex < controllers.size(); controllerIndex++) {
            CompoundTag controller = controllers.getCompound(controllerIndex);
            if (!CONTROLLER.id().toString().equals(controller.getString("Controller"))) {
                continue;
            }
            ListTag forced = controller.getList("ModForced", Tag.TAG_COMPOUND);
            for (int chunkIndex = 0; chunkIndex < forced.size(); chunkIndex++) {
                CompoundTag entry = forced.getCompound(chunkIndex);
                ListTag owners = entry.getList(ticking ? "TickingBlocks" : "Blocks", Tag.TAG_COMPOUND);
                for (int ownerIndex = 0; ownerIndex < owners.size(); ownerIndex++) {
                    CompoundTag savedOwner = owners.getCompound(ownerIndex);
                    if (savedOwner.getInt("X") == owner.getX()
                            && savedOwner.getInt("Y") == owner.getY()
                            && savedOwner.getInt("Z") == owner.getZ()) {
                        chunks.add(entry.getLong("Chunk"));
                    }
                }
            }
        }
        return chunks;
    }

    private static void validateTickets(
            ServerLevel level, net.neoforged.neoforge.common.world.chunk.TicketHelper tickets) {
        tickets.getBlockTickets().forEach((owner, ticketSet) -> {
            if (!isValidTicketSet(level, owner, ticketSet)) {
                tickets.removeAllTickets(owner);
            }
        });
        tickets.getEntityTickets().keySet().forEach(tickets::removeAllTickets);
    }

    private ChunkLoaderTicketService() {
    }
}
