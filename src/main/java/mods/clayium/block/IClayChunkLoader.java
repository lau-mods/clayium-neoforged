package mods.clayium.block;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public interface IClayChunkLoader {
    boolean isPassive(World var1, int var2, int var3, int var4);

    boolean hasTicket();

    Ticket requestTicket();

    void releaseTicket();

    void appendTicket(Ticket var1);

    void forceChunk();
}
