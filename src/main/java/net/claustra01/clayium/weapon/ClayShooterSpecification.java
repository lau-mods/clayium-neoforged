package net.claustra01.clayium.weapon;

public record ClayShooterSpecification(
        String id,
        String texture,
        int durability,
        int straightFlightTicks,
        float initialVelocity,
        float diffusion,
        int damage,
        int originalShootingRate,
        int chargeTicks,
        int tier) {
    public static final ClayShooterSpecification BASIC = new ClayShooterSpecification(
            "basic_clay_shooter", "clayshooter0", 10_000, 2, 1.2F, 25.0F, 4, 6, 0, 6);
    public static final ClayShooterSpecification ADVANCED = new ClayShooterSpecification(
            "advanced_clay_shooter", "clayshooter1", 1_000, 4, 4.5F, 3.0F, 7, 12, 0, 6);
    public static final ClayShooterSpecification SNIPER = new ClayShooterSpecification(
            "sniper_clay_shooter", "clayshooter2", 8_000, 2, 15.0F, 0.0F, 25, 8, 20, 6);
    public static final ClayShooterSpecification SNIPER_PRO = new ClayShooterSpecification(
            "sniper_clay_shooter_pro", "clayshooter3", 2_500, 3, 30.0F, 0.0F, 100, 8, 40, 6);
    public static final ClayShooterSpecification TELEPORTER = new ClayShooterSpecification(
            "instant_teleporter", "instantteleporter", 2_500, 3, 30.0F, 0.0F, 100, 8, 40, 11);

    public int cooldownTicks() {
        return originalShootingRate / 3;
    }

    public float chargePower(int usedTicks) {
        if (chargeTicks == 0) {
            return 1.0F;
        }
        float fraction = Math.min(usedTicks, chargeTicks) / (float) chargeTicks;
        return Math.min((fraction * fraction + fraction * 2.0F) / 3.0F, 1.0F);
    }
}
