package mods.clayium.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.util.UtilNetwork;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityClayBall extends Entity implements IProjectile {
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inBlock;
    protected boolean inGround;
    public int throwableShake;
    private EntityLivingBase thrower;
    private String throwerName;
    private int ticksInGround;
    private int ticksInAir;
    protected int age = 0;
    protected int lifespan = 2;
    protected int damage = 5;
    private int numberOfTick = 1;
    protected boolean critical = false;

    public EntityClayBall(World world) {
        super(world);
        this.func_70105_a(0.25F, 0.25F);
    }

    protected void func_70088_a() {
    }

    @SideOnly(Side.CLIENT)
    public boolean func_70112_a(double distance) {
        double d1 = this.field_70121_D.func_72320_b() * 4.0;
        d1 *= 64.0;
        return distance < d1 * d1;
    }

    public EntityClayBall(
        World world, EntityLivingBase thrower, int lifespan, float initialVelocity, float diffusion, int damage, int numberOfTick, boolean critical
    ) {
        super(world);
        this.thrower = thrower;
        this.func_70105_a(0.25F, 0.25F);
        this.func_70012_b(
            thrower.field_70165_t, thrower.field_70163_u + (double)thrower.func_70047_e(), thrower.field_70161_v, thrower.field_70177_z, thrower.field_70125_A
        );
        this.field_70165_t = this.field_70165_t - (double)(MathHelper.func_76134_b(this.field_70177_z / 180.0F * (float) Math.PI) * 0.16F);
        this.field_70163_u -= 0.1F;
        this.field_70161_v = this.field_70161_v - (double)(MathHelper.func_76126_a(this.field_70177_z / 180.0F * (float) Math.PI) * 0.16F);
        this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        double d4 = thrower.field_70165_t
            - (double)(MathHelper.func_76126_a(thrower.field_70177_z / 180.0F * (float) Math.PI) * initialVelocity) * (double)lifespan
            - this.field_70165_t;
        double d5 = thrower.field_70161_v
            + (double)(MathHelper.func_76134_b(thrower.field_70177_z / 180.0F * (float) Math.PI) * initialVelocity) * (double)lifespan
            - this.field_70161_v;
        this.field_70177_z = (float)(Math.atan2(d5, d4) * 180.0 / Math.PI) - 90.0F;
        this.field_70129_M = 0.0F;
        float f = 0.4F;
        this.field_70159_w = (double)(
            -MathHelper.func_76126_a(this.field_70177_z / 180.0F * (float) Math.PI)
                * MathHelper.func_76134_b(this.field_70125_A / 180.0F * (float) Math.PI)
                * f
        );
        this.field_70179_y = (double)(
            MathHelper.func_76134_b(this.field_70177_z / 180.0F * (float) Math.PI) * MathHelper.func_76134_b(this.field_70125_A / 180.0F * (float) Math.PI) * f
        );
        this.field_70181_x = (double)(-MathHelper.func_76126_a((this.field_70125_A + this.getInitialPitch()) / 180.0F * (float) Math.PI) * f);
        this.func_70186_c(this.field_70159_w, this.field_70181_x, this.field_70179_y, initialVelocity, diffusion);
        this.lifespan = lifespan;
        this.damage = damage;
        this.numberOfTick = numberOfTick;
        this.critical = critical;
    }

    public EntityClayBall(World world, double xCoord, double yCoord, double zCoord, int lifespan) {
        super(world);
        this.ticksInGround = 0;
        this.func_70105_a(0.25F, 0.25F);
        this.func_70107_b(xCoord, yCoord, zCoord);
        this.field_70129_M = 0.0F;
        this.lifespan = lifespan;
    }

    protected float getInitialVeolcity() {
        return 1.5F;
    }

    protected float getInitialPitch() {
        return 0.0F;
    }

    public void func_70186_c(double directionX, double directionY, double directionZ, float velocity, float diffusion) {
        float f2 = MathHelper.func_76133_a(directionX * directionX + directionY * directionY + directionZ * directionZ);
        directionX /= (double)f2;
        directionY /= (double)f2;
        directionZ /= (double)f2;
        directionX += this.field_70146_Z.nextGaussian() * 0.0075F * (double)diffusion;
        directionZ += this.field_70146_Z.nextGaussian() * 0.0075F * (double)diffusion;
        directionX *= (double)velocity / 10.0;
        directionY *= (double)velocity / 10.0;
        directionZ *= (double)velocity / 10.0;
        this.field_70159_w = directionX;
        this.field_70181_x = directionY;
        this.field_70179_y = directionZ;
        float f3 = MathHelper.func_76133_a(directionX * directionX + directionZ * directionZ);
        this.field_70126_B = this.field_70177_z = (float)(Math.atan2(directionX, directionZ) * 180.0 / Math.PI);
        this.field_70127_C = this.field_70125_A = (float)(Math.atan2(directionY, (double)f3) * 180.0 / Math.PI);
        this.ticksInGround = 0;
    }

    @SideOnly(Side.CLIENT)
    public void func_70016_h(double motionX, double motionY, double motionZ) {
        this.field_70159_w = motionX;
        this.field_70181_x = motionY;
        this.field_70179_y = motionZ;
        if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
            float f = MathHelper.func_76133_a(motionX * motionX + motionZ * motionZ);
            this.field_70126_B = this.field_70177_z = (float)(Math.atan2(motionX, motionZ) * 180.0 / Math.PI);
            this.field_70127_C = this.field_70125_A = (float)(Math.atan2(motionY, (double)f) * 180.0 / Math.PI);
        }
    }

    public void func_70071_h_() {
        if (!this.field_70170_p.field_72995_K) {
            for (int i = 0; i < this.numberOfTick; i++) {
                if (!this.field_70128_L) {
                    this.onUpdatePer();
                }
            }
        } else {
            this.onUpdatePer();
        }
    }

    public void onUpdatePer() {
        this.field_70142_S = this.field_70165_t;
        this.field_70137_T = this.field_70163_u;
        this.field_70136_U = this.field_70161_v;
        super.func_70071_h_();
        if (this.throwableShake > 0) {
            this.throwableShake--;
        }

        if (this.inGround) {
            if (this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile) == this.inBlock) {
                this.ticksInGround++;
                if (this.ticksInGround == 1200) {
                    this.func_70106_y();
                }

                return;
            }

            this.inGround = false;
            this.field_70159_w = this.field_70159_w * (double)(this.field_70146_Z.nextFloat() * 0.2F);
            this.field_70181_x = this.field_70181_x * (double)(this.field_70146_Z.nextFloat() * 0.2F);
            this.field_70179_y = this.field_70179_y * (double)(this.field_70146_Z.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        } else {
            this.ticksInAir++;
        }

        Vec3 vec3 = Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        Vec3 vec31 = Vec3.func_72443_a(
            this.field_70165_t + this.field_70159_w * 10.0, this.field_70163_u + this.field_70181_x * 10.0, this.field_70161_v + this.field_70179_y * 10.0
        );
        MovingObjectPosition movingobjectposition = this.field_70170_p.func_72933_a(vec3, vec31);
        vec3 = Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        vec31 = Vec3.func_72443_a(
            this.field_70165_t + this.field_70159_w * 10.0, this.field_70163_u + this.field_70181_x * 10.0, this.field_70161_v + this.field_70179_y * 10.0
        );
        if (movingobjectposition != null) {
            vec31 = Vec3.func_72443_a(
                movingobjectposition.field_72307_f.field_72450_a,
                movingobjectposition.field_72307_f.field_72448_b,
                movingobjectposition.field_72307_f.field_72449_c
            );
        }

        if (!this.field_70170_p.field_72995_K) {
            Entity entity = null;
            List list = this.field_70170_p
                .func_72839_b(
                    this,
                    this.field_70121_D
                        .func_72321_a(this.field_70159_w * 10.0, this.field_70181_x * 10.0, this.field_70179_y * 10.0)
                        .func_72314_b(1.0, 1.0, 1.0)
                );
            double d0 = 0.0;
            EntityLivingBase entitylivingbase = this.getThrower();

            for (int j = 0; j < list.size(); j++) {
                Entity entity1 = (Entity)list.get(j);
                if (entity1.func_70067_L() && (entity1 != entitylivingbase || this.ticksInAir >= 5)) {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.field_70121_D.func_72314_b((double)f, (double)f, (double)f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.func_72327_a(vec3, vec31);
                    if (movingobjectposition1 != null) {
                        double d1 = vec3.func_72438_d(movingobjectposition1.field_72307_f);
                        if (d1 < d0 || d0 == 0.0) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
        }

        if (movingobjectposition != null) {
            if (movingobjectposition.field_72313_a == MovingObjectType.BLOCK
                && this.field_70170_p.func_147439_a(movingobjectposition.field_72311_b, movingobjectposition.field_72312_c, movingobjectposition.field_72309_d)
                    == Blocks.field_150427_aO) {
                this.func_70063_aa();
            } else {
                this.onImpact(movingobjectposition);
            }
        }

        this.field_70165_t = this.field_70165_t + this.field_70159_w * 10.0;
        this.field_70163_u = this.field_70163_u + this.field_70181_x * 10.0;
        this.field_70161_v = this.field_70161_v + this.field_70179_y * 10.0;
        float f1 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
        this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0 / Math.PI);
        this.field_70125_A = (float)(Math.atan2(this.field_70181_x, (double)f1) * 180.0 / Math.PI);

        while (this.field_70125_A - this.field_70127_C < -180.0F) {
            this.field_70127_C -= 360.0F;
        }

        while (this.field_70125_A - this.field_70127_C >= 180.0F) {
            this.field_70127_C += 360.0F;
        }

        while (this.field_70177_z - this.field_70126_B < -180.0F) {
            this.field_70126_B -= 360.0F;
        }

        while (this.field_70177_z - this.field_70126_B >= 180.0F) {
            this.field_70126_B += 360.0F;
        }

        float f2 = this.getFriction();
        float f3 = this.getGravityVelocity();
        if (this.func_70090_H()) {
            this.spawnInWaterParticle();
            f2 = 0.8F;
        }

        this.field_70159_w *= (double)f2;
        if (this.field_70181_x >= 0.0) {
            this.field_70181_x *= (double)f2;
        }

        this.field_70179_y *= (double)f2;
        this.field_70181_x -= (double)f3;
        this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        this.age++;
        if (this.field_70170_p.field_72995_K) {
            this.spawnFlyingDustPartcle();
            this.spawnTwinklingPartcle();
        }
    }

    protected float getFriction() {
        return this.age < this.lifespan ? 1.0F : 0.6F;
    }

    protected float getGravityVelocity() {
        return this.age < this.lifespan ? 0.0F : 0.45F;
    }

    protected void onImpact(MovingObjectPosition moposition) {
        if (!this.field_70128_L) {
            double xx = this.field_70165_t;
            double yy = this.field_70163_u;
            double zz = this.field_70161_v;
            if (moposition.field_72313_a == MovingObjectType.BLOCK) {
                this.field_70165_t = moposition.field_72307_f.field_72450_a;
                this.field_70163_u = moposition.field_72307_f.field_72448_b;
                this.field_70161_v = moposition.field_72307_f.field_72449_c;
            } else if (moposition.field_72308_g != null) {
                this.field_70165_t = moposition.field_72307_f.field_72450_a;
                this.field_70163_u = moposition.field_72307_f.field_72448_b;
                this.field_70161_v = moposition.field_72307_f.field_72449_c;
            }

            if (moposition.field_72308_g != null) {
                this.onEntityHit(moposition.field_72308_g);
            }

            this.spawnImpactDustParticle();
            this.playImpactSound();
            float s = (float)Math.sqrt(
                this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y
            );
            if (s >= 0.5F && this.age <= this.lifespan) {
                this.playImpactExplodeSound(s);
                this.spawnImpactExplodeParticle();
            }

            if (this.checkDeath(moposition)) {
                this.onDeath();
            }

            this.field_70165_t = xx;
            this.field_70163_u = yy;
            this.field_70161_v = zz;
        }
    }

    protected void onEntityHit(Entity entity) {
        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).func_110143_aJ() > 0.0F) {
            if (this.critical) {
                if (!this.field_70170_p.field_72995_K) {
                    this.spawnCriticalParticle();
                }

                this.playCriticalSound();
            }

            this.playHitSound();
        }

        if (!this.field_70170_p.field_72995_K) {
            this.causeDamage(entity);
        }
    }

    protected void causeDamage(Entity entity) {
        int b0 = this.damage;
        double motionX = entity.field_70159_w;
        double motionY = entity.field_70181_x;
        double motionZ = entity.field_70179_y;
        if (entity != this.thrower) {
            entity.func_70097_a(DamageSource.func_76356_a(this, this.getThrower()), (float)b0);
        }

        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).field_70172_ad = 2;
        }

        entity.field_70159_w = motionX;
        entity.field_70181_x = motionY;
        entity.field_70179_y = motionZ;
    }

    protected void spawnFlyingDustPartcle() {
        this.field_70170_p
            .func_72869_a(
                "blockdust_" + Block.func_149682_b(Blocks.field_150435_aG) + "_0",
                this.field_70142_S,
                this.field_70137_T,
                this.field_70136_U,
                this.field_70159_w / 2.0,
                this.field_70181_x / 2.0,
                this.field_70179_y / 2.0
            );
        this.field_70170_p
            .func_72869_a(
                "blockdust_" + Block.func_149682_b(Blocks.field_150435_aG) + "_0",
                (this.field_70165_t + this.field_70142_S) / 2.0,
                (this.field_70163_u + this.field_70137_T) / 2.0,
                (this.field_70161_v + this.field_70136_U) / 2.0,
                this.field_70159_w / 2.0,
                this.field_70181_x / 2.0,
                this.field_70179_y / 2.0
            );
    }

    protected void spawnTwinklingPartcle() {
        int s = (int)Math.floor(this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y)
            * 100
            / 16;
        if (this.age >= this.lifespan) {
            s = 0;
        }

        s *= this.numberOfTick * this.numberOfTick;

        for (int i = 0; i < s; i++) {
            this.field_70170_p
                .func_72869_a(
                    "crit",
                    this.field_70165_t + this.field_70159_w * 10.0 * (double)this.numberOfTick * (double)(i - s) / (double)s,
                    this.field_70163_u + this.field_70181_x * 10.0 * (double)this.numberOfTick * (double)(i - s) / (double)s,
                    this.field_70161_v + this.field_70179_y * 10.0 * (double)this.numberOfTick * (double)(i - s) / (double)s,
                    this.field_70159_w * 10.0 * (double)this.numberOfTick / (double)s / 2.0,
                    this.field_70181_x * 10.0 * (double)this.numberOfTick / (double)s / 2.0,
                    this.field_70179_y * 10.0 * (double)this.numberOfTick / (double)s / 2.0
                );
        }
    }

    protected void spawnInWaterParticle() {
        for (int i = 0; i < 4; i++) {
            float f4 = 0.25F;
            this.field_70170_p
                .func_72869_a(
                    "bubble",
                    this.field_70165_t - this.field_70159_w * (double)f4,
                    this.field_70163_u - this.field_70181_x * (double)f4,
                    this.field_70161_v - this.field_70179_y * (double)f4,
                    this.field_70159_w,
                    this.field_70181_x,
                    this.field_70179_y
                );
        }
    }

    protected void spawnImpactDustParticle() {
        for (int i = 0; i < 8; i++) {
            this.field_70170_p
                .func_72869_a(
                    "blockdust_" + Block.func_149682_b(Blocks.field_150435_aG) + "_0",
                    this.field_70165_t,
                    this.field_70163_u,
                    this.field_70161_v,
                    0.0,
                    0.0,
                    0.0
                );
        }
    }

    protected void spawnImpactExplodeParticle() {
        this.field_70170_p.func_72869_a("largeexplode", this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0, 0.0, 0.0);
    }

    protected void spawnCriticalParticle() {
        WorldServer server = (WorldServer)this.field_70170_p;
        UtilNetwork.sendParticlePacketFromServer(
            server, "fireworksSpark", this.field_70165_t, this.field_70163_u, this.field_70161_v, 50, 0.0, -1.0, 0.0, 0.5, 200.0
        );
    }

    protected void playImpactSound() {
        this.field_70170_p.func_72956_a(this, "mob.slime.big", 0.06F * (float)this.damage, 1.4F / (this.field_70146_Z.nextFloat() * 0.4F + 0.6F));
    }

    protected void playImpactExplodeSound(float speed) {
        if (speed >= 0.5F && this.age <= this.lifespan) {
            this.field_70170_p.func_72956_a(this, "random.explode", 0.6F * speed, 0.5F / (this.field_70146_Z.nextFloat() * 0.4F + 0.6F));
        }
    }

    protected void playHitSound() {
        this.field_70170_p.func_72956_a(this, "game.player.hurt", 0.1F * (float)this.damage, 0.7F);
        this.field_70170_p.func_72956_a(this, "mob.blaze.hit", 0.05F * (float)this.damage, 0.5F);
        this.field_70170_p.func_72956_a(this, "random.wood_click", 0.2F * (float)this.damage, 1.2F);
        this.field_70170_p.func_72956_a(this, "note.hat", 0.3F * (float)this.damage, 1.3F);
        this.field_70170_p.func_72956_a(this, "note.snare", 0.1F * (float)this.damage, 1.2F);
    }

    protected void playCriticalSound() {
        this.field_70170_p.func_72956_a(this.thrower, "fireworks.twinkle", 10.0F, 1.0F);
    }

    protected boolean checkDeath(MovingObjectPosition moposition) {
        return !(moposition.field_72308_g instanceof EntityLivingBase) || ((EntityLivingBase)moposition.field_72308_g).func_110143_aJ() > 0.0F;
    }

    protected void onDeath() {
        if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
        }
    }

    public void func_70014_b(NBTTagCompound tagcompound) {
        tagcompound.func_74777_a("xTile", (short)this.xTile);
        tagcompound.func_74777_a("yTile", (short)this.yTile);
        tagcompound.func_74777_a("zTile", (short)this.zTile);
        tagcompound.func_74774_a("inTile", (byte)Block.func_149682_b(this.inBlock));
        tagcompound.func_74774_a("shake", (byte)this.throwableShake);
        tagcompound.func_74774_a("inGround", (byte)(this.inGround ? 1 : 0));
        if ((this.throwerName == null || this.throwerName.length() == 0) && this.thrower != null && this.thrower instanceof EntityPlayer) {
            this.throwerName = this.thrower.func_70005_c_();
        }

        tagcompound.func_74778_a("ownerName", this.throwerName == null ? "" : this.throwerName);
        tagcompound.func_74777_a("age", (short)this.age);
        tagcompound.func_74777_a("lifespan", (short)this.lifespan);
        tagcompound.func_74777_a("clayBallDamage", (short)this.damage);
        tagcompound.func_74777_a("numberOfTick", (short)this.numberOfTick);
        tagcompound.func_74757_a("critical", this.critical);
    }

    public void func_70037_a(NBTTagCompound tagcompound) {
        this.xTile = tagcompound.func_74765_d("xTile");
        this.yTile = tagcompound.func_74765_d("yTile");
        this.zTile = tagcompound.func_74765_d("zTile");
        this.inBlock = Block.func_149729_e(tagcompound.func_74771_c("inTile") & 255);
        this.throwableShake = tagcompound.func_74771_c("shake") & 255;
        this.inGround = tagcompound.func_74771_c("inGround") == 1;
        this.throwerName = tagcompound.func_74779_i("ownerName");
        if (this.throwerName != null && this.throwerName.length() == 0) {
            this.throwerName = null;
        }

        this.age = tagcompound.func_74765_d("age");
        this.lifespan = tagcompound.func_74765_d("lifespan");
        this.damage = tagcompound.func_74765_d("clayBallDamage");
        this.numberOfTick = tagcompound.func_74765_d("numberOfTick");
        this.critical = tagcompound.func_74767_n("critical");
    }

    @SideOnly(Side.CLIENT)
    public float func_70053_R() {
        return 0.0F;
    }

    public EntityLivingBase getThrower() {
        if (this.thrower == null && this.throwerName != null && this.throwerName.length() > 0) {
            this.thrower = this.field_70170_p.func_72924_a(this.throwerName);
        }

        return this.thrower;
    }
}
