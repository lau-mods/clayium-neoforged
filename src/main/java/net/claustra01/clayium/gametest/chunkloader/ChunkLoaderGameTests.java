package net.claustra01.clayium.gametest.chunkloader;

import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderBlockEntity;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderTicketService;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChunkLoaderGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void loadsExactlyNineTickingChunks(GameTestHelper helper) {
        BlockPos relative = boundaryPosition(helper);
        helper.setBlock(relative, ChunkLoaderRegistration.CHUNK_LOADER.get());
        ChunkLoaderBlockEntity chunkLoader = chunkLoader(helper, relative);
        BlockPos owner = chunkLoader.getBlockPos();
        ChunkPos center = new ChunkPos(owner);
        require((owner.getX() & 15) == 0 || (owner.getZ() & 15) == 0,
                "Chunk loader is not on a chunk boundary");
        helper.runAfterDelay(5, () -> {
            try {
                require(chunkLoader.ticketStatus() == ChunkLoaderBlockEntity.TicketStatus.ACTIVE,
                        "Chunk loader did not become active");
                require(helper.getBlockState(relative).getValue(ChunkLoaderBlock.STATUS)
                                == ChunkLoaderBlockEntity.TicketStatus.ACTIVE,
                        "Active ticket status was not published to the block appearance");
                Set<Long> expected = ChunkLoaderTicketService.expectedChunks(owner);
                require(expected.size() == 9, "The target area is not exactly nine chunks");
                require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, true).equals(expected),
                        "The ticking ticket set is not the exact 3x3 area");
                require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, false).isEmpty(),
                        "Chunk loader created non-ticking tickets");
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        long chunk = ChunkPos.asLong(center.x + x, center.z + z);
                        if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                            require(helper.getLevel().shouldTickBlocksAt(chunk),
                                    "A target chunk is not ticking: " + new ChunkPos(chunk));
                        } else {
                            require(!ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, true).contains(chunk),
                                    "The loader ticketed a chunk outside its range");
                        }
                    }
                }
            } finally {
                helper.setBlock(relative, Blocks.AIR);
            }
            require(helper.getBlockState(relative).isAir()
                            && helper.getLevel().getBlockEntity(helper.absolutePos(relative)) == null,
                    "Removing an active chunk loader restored a block without its block entity");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void ticketsReleaseAndRestartValidationRemovesOrphans(GameTestHelper helper) {
        BlockPos relative = boundaryPosition(helper);
        helper.setBlock(relative, ChunkLoaderRegistration.CHUNK_LOADER.get());
        ChunkLoaderBlockEntity chunkLoader = chunkLoader(helper, relative);
        BlockPos owner = chunkLoader.getBlockPos();
        ChunkLoaderBlockEntity.serverTick(helper.getLevel(), owner, chunkLoader.getBlockState(), chunkLoader);
        Set<Long> expected = ChunkLoaderTicketService.expectedChunks(owner);
        require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, true).equals(expected),
                "Initial loader tickets are incomplete");
        CompoundTag unavailable = new CompoundTag();
        unavailable.putString("ticket_status", "unavailable");
        chunkLoader.loadWithComponents(unavailable, helper.getLevel().registryAccess());
        require(chunkLoader.getBlockState().getValue(ChunkLoaderBlock.STATUS)
                        == ChunkLoaderBlockEntity.TicketStatus.UNAVAILABLE,
                "Unavailable ticket status was not published to the block appearance");
        ChunkLoaderBlockEntity.serverTick(helper.getLevel(), owner, chunkLoader.getBlockState(), chunkLoader);
        require(chunkLoader.ticketStatus() == ChunkLoaderBlockEntity.TicketStatus.ACTIVE,
                "An unavailable loader did not retry on its next original-style tick");
        require(chunkLoader.getBlockState().getValue(ChunkLoaderBlock.STATUS)
                        == ChunkLoaderBlockEntity.TicketStatus.ACTIVE,
                "Retried ticket status was not published to the block appearance");

        BlockPos orphan = owner.offset(160, 0, 160);
        ChunkPos orphanCenter = new ChunkPos(orphan);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ChunkLoaderTicketService.CONTROLLER.forceChunk(
                        helper.getLevel(), orphan, orphanCenter.x + x, orphanCenter.z + z, true, true);
            }
        }
        ForcedChunksSavedData savedData = helper.getLevel().getDataStorage()
                .computeIfAbsent(ForcedChunksSavedData.factory(), ForcedChunksSavedData.FILE_ID);
        CompoundTag persisted = savedData.save(new CompoundTag(), helper.getLevel().registryAccess());
        try {
            // A restart begins with saved tickets but without the previous level's live tickets.
            ChunkLoaderTicketService.release(helper.getLevel(), owner);
            ChunkLoaderTicketService.release(helper.getLevel(), orphan);
            require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), orphan, true).isEmpty(),
                    "Orphan live tickets survived level unload");
            ForcedChunkManager.readModForcedChunks(
                    persisted, savedData.getBlockForcedChunks(), savedData.getEntityForcedChunks());
            ForcedChunkManager.reinstatePersistentChunks(helper.getLevel(), savedData);
            require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), orphan, true).isEmpty(),
                    "Restart validation retained orphan saved tickets");
            require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, true).equals(expected),
                    "Restart validation discarded legitimate saved tickets");
            ChunkLoaderTicketService.CONTROLLER.forceChunk(
                    helper.getLevel(), owner, new ChunkPos(owner).x - 1, new ChunkPos(owner).z - 1, false, true);
            CompoundTag active = new CompoundTag();
            active.putString("ticket_status", "active");
            chunkLoader.loadWithComponents(active, helper.getLevel().registryAccess());
            require(chunkLoader.ticketStatus() == ChunkLoaderBlockEntity.TicketStatus.WAITING,
                    "An active status was trusted across a reload");
            require(chunkLoader.getBlockState().getValue(ChunkLoaderBlock.STATUS)
                            == ChunkLoaderBlockEntity.TicketStatus.WAITING,
                    "Reload validation did not publish its waiting state");
            ChunkLoaderBlockEntity.serverTick(helper.getLevel(), owner, chunkLoader.getBlockState(), chunkLoader);
            require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, true).equals(expected),
                    "A reloaded loader did not reacquire a missing ticket");
        } finally {
            ChunkLoaderTicketService.release(helper.getLevel(), orphan);
            helper.setBlock(relative, Blocks.AIR);
        }
        require(ChunkLoaderTicketService.heldChunks(helper.getLevel(), owner, true).isEmpty(),
                "Removing the chunk loader did not release its tickets");
        require(helper.getBlockState(relative).isAir()
                        && helper.getLevel().getBlockEntity(helper.absolutePos(relative)) == null,
                "Removing the chunk loader restored a block without its block entity");
        helper.runAfterDelay(20, () -> {
            require(!helper.getLevel().shouldTickBlocksAt(orphanCenter.toLong()),
                    "An orphan chunk is still ticking after restart validation");
            helper.succeed();
        });
    }

    private static BlockPos boundaryPosition(GameTestHelper helper) {
        for (int x = 1; x <= 16; x++) {
            BlockPos relative = new BlockPos(x, 2, 1);
            if ((helper.absolutePos(relative).getX() & 15) == 0) {
                return relative;
            }
        }
        throw new GameTestAssertException("No chunk boundary is reachable");
    }

    private static ChunkLoaderBlockEntity chunkLoader(GameTestHelper helper, BlockPos relative) {
        if (helper.getBlockEntity(relative) instanceof ChunkLoaderBlockEntity chunkLoader) {
            return chunkLoader;
        }
        throw new GameTestAssertException("Chunk loader block entity is missing");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private ChunkLoaderGameTests() {
    }
}
