package mods.clayium.entity;

import mods.clayium.util.UtilNetwork;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public class EntityTeleportBall extends EntityClayBall {
    public EntityTeleportBall(World world) {
        super(world);
    }

    public EntityTeleportBall(World world, double xCoord, double yCoord, double zCoord, int lifespan) {
        super(world, xCoord, yCoord, zCoord, lifespan);
    }

    public EntityTeleportBall(
        World world, EntityLivingBase thrower, int lifespan, float initialVelocity, float diffusion, int damage, int numberOfTick, boolean critical
    ) {
        super(world, thrower, lifespan, initialVelocity, diffusion, damage, numberOfTick, critical);
    }

    protected void onDeath() {
        if (!this.field_70170_p.field_72995_K) {
            if (this.getThrower() != null && this.getThrower() instanceof EntityPlayerMP) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP)this.getThrower();
                if (entityplayermp.field_71135_a.func_147362_b().func_150724_d() && entityplayermp.field_70170_p == this.field_70170_p) {
                    EnderTeleportEvent event = new EnderTeleportEvent(entityplayermp, this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0F);
                    if (!MinecraftForge.EVENT_BUS.post(event)) {
                        this.field_70170_p.func_72956_a(this.getThrower(), "mob.endermen.portal", 1.0F, 1.0F);
                        double oldX = this.getThrower().field_70165_t;
                        double oldY = this.getThrower().field_70163_u;
                        double oldZ = this.getThrower().field_70161_v;
                        WorldServer server = (WorldServer)this.field_70170_p;
                        if (this.getThrower().func_70115_ae()) {
                            this.getThrower().func_70078_a((Entity)null);
                        }

                        double dx = 0.0;
                        double dy = 0.0;
                        double dz = 0.0;
                        int c = 0;

                        double x;
                        double y;
                        double z;
                        do {
                            x = Math.floor(event.targetX + dx);
                            y = Math.floor(event.targetY + dy);
                            z = Math.floor(event.targetZ + dz);
                            dx = this.field_70146_Z.nextGaussian() * (double)c / 10.0;
                            dy = this.field_70146_Z.nextGaussian() * (double)c / 10.0;
                            dz = this.field_70146_Z.nextGaussian() * (double)c / 10.0;
                        } while ((this.isCollidable(this.field_70170_p, x, y, z) || this.isCollidable(this.field_70170_p, x, y + 1.0, z)) && c++ < 100);

                        this.getThrower().func_70634_a(x + 0.5, y, z + 0.5);
                        this.getThrower().field_70143_R = 0.0F;
                        this.getThrower().func_70097_a(DamageSource.field_76379_h, event.attackDamage);

                        for (int i = 0; i < 30; i++) {
                            UtilNetwork.sendParticlePacketFromServer(
                                server,
                                "portal",
                                event.targetX,
                                event.targetY + this.field_70146_Z.nextDouble() * 2.0,
                                event.targetZ,
                                0,
                                this.field_70146_Z.nextGaussian(),
                                0.0,
                                this.field_70146_Z.nextGaussian(),
                                1.0,
                                20.0
                            );
                        }

                        int length = (int)Math.sqrt(
                            (event.targetX - this.getThrower().field_70165_t) * (event.targetX - this.getThrower().field_70165_t)
                                + (event.targetY - this.getThrower().field_70163_u) * (event.targetY - this.getThrower().field_70163_u)
                                + (event.targetZ - this.getThrower().field_70161_v) * (event.targetZ - this.getThrower().field_70161_v)
                        );

                        for (int i = 0; i < length; i++) {
                            double r = (double)i / (double)length;
                            UtilNetwork.sendParticlePacketFromServer(
                                server,
                                "portal",
                                (event.targetX - oldX) * r + oldX,
                                (event.targetY - oldY) * r + oldY + 1.0,
                                (event.targetZ - oldZ) * r + oldZ,
                                0,
                                (oldX - this.field_70165_t) / (double)length,
                                (oldY - this.field_70163_u) / (double)length,
                                (oldZ - this.field_70161_v) / (double)length,
                                1.0,
                                200.0
                            );
                        }

                        this.field_70170_p.func_72956_a(this, "mob.endermen.portal", 1.0F, 1.0F);
                    }
                }
            }

            this.func_70106_y();
        }
    }

    protected boolean isCollidable(World world, double x, double y, double z) {
        return world.func_147439_a((int)x, (int)y, (int)z).func_149668_a(world, (int)x, (int)y, (int)z) != null;
    }

    protected boolean checkDeath(MovingObjectPosition moposition) {
        return true;
    }

    protected void spawnFlyingDustPartcle() {
        this.field_70170_p
            .func_72869_a(
                "blockdust_" + Block.func_149682_b(Blocks.field_150325_L) + "_13",
                this.field_70142_S,
                this.field_70137_T,
                this.field_70136_U,
                this.field_70159_w / 2.0,
                this.field_70181_x / 2.0,
                this.field_70179_y / 2.0
            );
        this.field_70170_p
            .func_72869_a(
                "blockdust_" + Block.func_149682_b(Blocks.field_150325_L) + "_13",
                (this.field_70165_t + this.field_70142_S) / 2.0,
                (this.field_70163_u + this.field_70137_T) / 2.0,
                (this.field_70161_v + this.field_70136_U) / 2.0,
                this.field_70159_w / 2.0,
                this.field_70181_x / 2.0,
                this.field_70179_y / 2.0
            );
    }

    protected void spawnImpactDustParticle() {
        for (int i = 0; i < 8; i++) {
            this.field_70170_p
                .func_72869_a(
                    "blockdust_" + Block.func_149682_b(Blocks.field_150325_L) + "_13",
                    this.field_70165_t,
                    this.field_70163_u,
                    this.field_70161_v,
                    0.0,
                    0.0,
                    0.0
                );
        }
    }

    protected void playImpactSound() {
        this.field_70170_p.func_72956_a(this, "step.stone", 0.06F * (float)this.damage, 1.4F / (this.field_70146_Z.nextFloat() * 0.4F + 0.6F));
    }
}
