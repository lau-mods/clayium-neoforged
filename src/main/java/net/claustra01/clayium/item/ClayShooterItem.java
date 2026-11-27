package net.claustra01.clayium.item;

/**
 * Clay Shooters (tiers 0..3) and Instant Teleporter. Projectile firing mechanics are wired in Step 11.
 */
public class ClayShooterItem extends SimpleTieredItem {
    private final int burstCount;
    private final float projectileSpeed;
    private final float spread;
    private final int baseDamage;
    private final int lifespanTicks;
    private final int cooldownTicks;
    private final boolean teleporter;

    public ClayShooterItem(
            int durability,
            int tier,
            String iconName,
            int burstCount,
            float projectileSpeed,
            float spread,
            int baseDamage,
            int lifespanTicks,
            int cooldownTicks,
            boolean teleporter) {
        super(new Properties().durability(durability), tier, iconName);
        this.burstCount = burstCount;
        this.projectileSpeed = projectileSpeed;
        this.spread = spread;
        this.baseDamage = baseDamage;
        this.lifespanTicks = lifespanTicks;
        this.cooldownTicks = cooldownTicks;
        this.teleporter = teleporter;
    }

    public int getBurstCount() {
        return this.burstCount;
    }

    public float getProjectileSpeed() {
        return this.projectileSpeed;
    }

    public float getSpread() {
        return this.spread;
    }

    public int getBaseDamage() {
        return this.baseDamage;
    }

    public int getLifespanTicks() {
        return this.lifespanTicks;
    }

    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    public boolean isTeleporter() {
        return this.teleporter;
    }
}
