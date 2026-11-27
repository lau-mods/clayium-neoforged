package mods.clayium.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;

public class UtilNetwork {
    public UtilNetwork() {
        super();
    }

    public static void sendParticlePacketFromServer(
        WorldServer server,
        String particle,
        double posX,
        double posY,
        double posZ,
        int number,
        double motionX,
        double motionY,
        double motionZ,
        double diffusion,
        double distance
    ) {
        S2APacketParticles s2apacketparticles = new S2APacketParticles(
            particle, (float)posX, (float)posY, (float)posZ, (float)motionX, (float)motionY, (float)motionZ, (float)diffusion, number
        );

        for (int j = 0; j < server.field_73010_i.size(); j++) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)server.field_73010_i.get(j);
            ChunkCoordinates chunkcoordinates = entityplayermp.func_82114_b();
            double d7 = posX - (double)chunkcoordinates.field_71574_a;
            double d8 = posY - (double)chunkcoordinates.field_71572_b;
            double d9 = posZ - (double)chunkcoordinates.field_71573_c;
            double d10 = d7 * d7 + d8 * d8 + d9 * d9;
            if (d10 <= distance * distance) {
                entityplayermp.field_71135_a.func_147359_a(s2apacketparticles);
            }
        }
    }
}
